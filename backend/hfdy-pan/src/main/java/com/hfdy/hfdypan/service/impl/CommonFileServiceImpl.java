package com.hfdy.hfdypan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfdy.hfdypan.constants.FileConstants;
import com.hfdy.hfdypan.constants.MinIOConstants;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.domain.bo.QueryFileListBO;
import com.hfdy.hfdypan.domain.dto.file.*;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.entity.Share;
import com.hfdy.hfdypan.domain.entity.ShareFile;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.enums.file.FileCategoryEnum;
import com.hfdy.hfdypan.domain.enums.file.FileIsDeletedEnum;
import com.hfdy.hfdypan.domain.enums.file.FileMediaTypeEnum;
import com.hfdy.hfdypan.domain.enums.file.FileStatusEnum;
import com.hfdy.hfdypan.domain.vo.file.ItemDetailVO;
import com.hfdy.hfdypan.domain.vo.file.QueryItemListVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.ShareFileMapper;
import com.hfdy.hfdypan.mapper.ShareMapper;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.CommonFileService;
import com.hfdy.hfdypan.service.ResourceService;
import com.hfdy.hfdypan.utils.*;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author hf-dy
 * @date 2025/1/17 10:55
 */
@Service
public class CommonFileServiceImpl extends ServiceImpl<FileMapper, File> implements CommonFileService {

    @Resource
    private FileMapper fileMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private MinIOUtil minIOUtil;
    @Resource
    private RedisUtil<Integer> redisUtil;
    @Resource
    private ShareFileMapper shareFileMapper;
    @Resource
    private ShareMapper shareMapper;
    @Resource
    private ResourceService resourceService;

    @Override
    public QueryItemListVO queryItemList(QueryItemListDTO dto) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        // 1. 验证文件分类
        ThrowUtil.throwIf(!VerifyUtil.verifyFileCategory(dto.getCategory()), new BusinessException(HttpCodeEnum.FILE_CATEGORY_WRONG));
        // 2. 查询数据库，未删除的
        LambdaQueryWrapper<File> wrapper = Wrappers.<File>lambdaQuery()
                .eq(!Objects.equals(dto.getCategory(), "all"), File::getCategory, dto.getCategory())
                .like(dto.getKeyword() != null && !dto.getKeyword().isEmpty(), File::getName, dto.getKeyword())
                .eq(dto.getPid() != null, File::getPid, dto.getPid())
                .eq(dto.getPid() == null, File::getPid, "")
                .eq(File::getUserId, userId);
        if (dto.getOrderByUpdateTime() != -1) {
            wrapper.orderBy(true, dto.getOrderByUpdateTime() == 1, File::getUpdateTime);
        }
        if (dto.getOrderBySize() != -1) {
            wrapper.orderBy(true, dto.getOrderBySize() == 1, File::getSize);
        }
        // 分页
        Page<File> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<File> res = fileMapper.selectPage(page, wrapper);
        // 找到当前文件夹作为父文件夹
        QueryFileListBO parent = new QueryFileListBO();
        File file = fileMapper.selectById(dto.getPid());
        if (file != null) BeanUtils.copyProperties(file, parent);
        else parent.setId("");
        // 3. 组装结果
        List<QueryFileListBO> bos = res.getRecords().stream().map(r -> {
            QueryFileListBO bo = new QueryFileListBO();
            BeanUtils.copyProperties(r, bo);
            // 如果是音频且有歌词，添加歌词路径
            if (FileUtil.isAudio(r) && !r.getLyricPath().isEmpty()) {
                bo.setLyricPath(resourceService.addPrefix(bo.getLyricPath()));
            }
            return bo;
        }).toList();
        QueryItemListVO vo = new QueryItemListVO();
        vo.setRecords(bos);
        vo.setTotal(page.getTotal());
        vo.setParent(parent);
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }

    @Override
    public ItemDetailVO getDetailById(String id) {
        // 1. 确保存在
        File file = getById(id);
        ThrowUtil.throwIf(file == null, new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS));
        // 2. 确保是自己的
        String userId = ThreadLocalUtil.getCurrentUserId();
        ThrowUtil.throwIf(!Objects.equals(file.getUserId(), userId), new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR));
        // 3. 获取成功
        ItemDetailVO vo = new ItemDetailVO();
        BeanUtils.copyProperties(file, vo);
        // 找到对应用户的用户名
        User user = userMapper.selectById(userId);
        vo.setUsername(user.getNickName());
        // 如果是文件夹，查子文件数量
        if (Objects.equals(file.getMediaType(), FileMediaTypeEnum.FOLDER.getMediaType())) {
            Long num = lambdaQuery().eq(File::getPid, file.getId()).count();
            vo.setChildNum(num);
        }
        return vo;
    }


    @Override
    public void createFolder(CreateFolderDTO dto) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        // 1. 是否达到最大深度
        List<File> files = fileMapper.selectList(Wrappers.<File>lambdaQuery()
                .eq(File::getId, dto.getPid()).eq(File::getUserId, userId));
        if (!dto.getPid().isEmpty() && files.isEmpty()) throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS);
        File folder = files.isEmpty() ? null : files.get(0);
        // 如果当前的父文件夹已经>=最大深度
        if (folder != null && folder.getLevel().split("/").length >= FileConstants.FILE_MAX_LEVEL) {
            throw new BusinessException(HttpCodeEnum.FOLDER_CREATE_FAIL, "创建失败，已达到最大深度");
        }
        // 2. 文件名排除/和\
        ThrowUtil.throwIf(dto.getName().contains("/") || dto.getName().contains("\\"), new BusinessException(HttpCodeEnum.FOLDER_CREATE_FAIL, "文件夹创建失败，文件夹名不能包含'/'和'\\'"));
        // 3. 判断同一pid下文件夹名是否重复
        if (folder != null) {
            List<File> files1 = lambdaQuery().eq(File::getPid, dto.getPid()).list();
            for (File file : files1) {
                if (file.getName().equals(dto.getName()))
                    throw new BusinessException(HttpCodeEnum.FOLDER_CREATE_FAIL, "文件夹创建失败，文件夹名重复");
            }
        }
        // 4. 生成File
        LocalDateTime current = LocalDateTime.now();
        String id = StringUtil.getRandomUUid();
        String newLevel = folder == null ? dto.getName() : folder.getLevel() + "/" + dto.getName();
        if (newLevel.split("/").length > FileConstants.FILE_MAX_LEVEL) {
            throw new BusinessException(HttpCodeEnum.FOLDER_CREATE_FAIL, "创建失败，已达最大深度");
        }
        File newFile = File.builder()
                .id(id).name(dto.getName()).path(dto.getPid()).userId(userId).md5("").level(newLevel)
                .path("").createTime(current).updateTime(current).size(null).pid(dto.getPid())
                .category(FileCategoryEnum.OTHERS.getCategory()).mediaType(FileMediaTypeEnum.FOLDER.getMediaType())
                .isDeleted(FileIsDeletedEnum.NO.getCode())
                .build();
        // 5. 入库
        ThrowUtil.throwIf(fileMapper.insert(newFile) == 0, new BusinessException(HttpCodeEnum.FOLDER_CREATE_FAIL));
        // 6. 如果该文件夹所在的文件夹正在被分享，更新更新时间
        List<ShareFile> shareFiles = shareFileMapper.selectList(Wrappers.<ShareFile>lambdaQuery().eq(ShareFile::getFileId, dto.getPid()));
        List<String> shareIds = shareFiles.stream().map(ShareFile::getShareId).toList();
        LocalDateTime now = LocalDateTime.now();
        if (!shareFiles.isEmpty()) {
            shareMapper.update(Wrappers.<Share>lambdaUpdate().in(Share::getId, shareIds).set(Share::getCreateTime, now));
        }
    }


    /**
     * 给子文件/文件夹递归重命名修改前缀
     *
     * @param files
     * @param name
     */
    private List<File> renamePrefixImpl(List<File> files, String name) {
        List<File> res = new ArrayList<>();
        for (File child : files) {
            int first = child.getLevel().indexOf("/");
            String newLevel = name + child.getLevel().substring(first);
            child.setLevel(newLevel);
            res.add(child);
            if (FileUtil.isFolder(child)) {
                List<File> files1 = fileMapper.selectList(Wrappers.<File>lambdaQuery()
                        .eq(File::getPid, child.getId()));
                res.addAll(renamePrefixImpl(files1, name));
            }
        }
        return res;
    }


    @Override
    public void renameItem(RenameItemDTO dto) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        // 1. 文件名不能包含/和\
        ThrowUtil.throwIf(dto.getName().contains("/") || dto.getName().contains("\\"), new BusinessException(HttpCodeEnum.FILE_RENAME_ERROR, "文件重命名失败，文件名不能包含'/'和'\\'"));
        // 2. 确保文件存在
        File file = lambdaQuery().eq(File::getId, dto.getId()).one();
        ThrowUtil.throwIf(file == null, new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS));
        // 3. 确保是自己的文件
        ThrowUtil.throwIf(!Objects.equals(file.getUserId(), userId), new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR));
        // 4. 不和父文件夹中的文件和文件夹名重复，考虑null的情况
        boolean exists = lambdaQuery()
                .eq(File::getPid, file.getPid())
                .eq(File::getName, dto.getName()).exists();
        ThrowUtil.throwIf(exists, new BusinessException(HttpCodeEnum.FILE_RENAME_ERROR, "重命名失败，文件名或文件夹名重复"));
        // 5. 重命名
        file.setName(dto.getName());
        file.setUpdateTime(LocalDateTime.now());
        String parentPath = FileUtil.getFolderPath(file.getLevel());
        file.setLevel((parentPath.isEmpty() ? "" : "/") + dto.getName());
        lambdaUpdate().eq(File::getId, dto.getId()).update(file);
        // 如果是文件夹，递归修改子文件/文件夹
        if (FileUtil.isFolder(file)) {
            List<File> files = fileMapper.selectList(Wrappers.<File>lambdaQuery().eq(File::getPid, dto.getId()));
            fileMapper.updateById(renamePrefixImpl(files, dto.getName()));
        }
    }


    /**
     * 删除视频，考虑到m3u8和ts
     *
     * @param file
     */
    private void deleteVideo(File file) throws Exception {
        if (!file.getPath().endsWith(".m3u8")) minIOUtil.deleteFiles(file.getPath());
        else {
            String folderPath = FileUtil.getFolderPath(file.getPath());
            minIOUtil.deleteFolder(folderPath);
        }
    }

    /**
     * 删除歌词
     *
     * @param file
     * @throws Exception
     */
    private void deleteLyric(File file) throws Exception {
        minIOUtil.deleteFiles(file.getLyricPath());
    }


    /**
     * 删除
     *
     * @param file
     * @param complete 是否彻底删除
     * @throws Exception
     */
    private void deleteItemImpl(File file, boolean complete) throws Exception {
        // 物理删除
        if (complete) {
            fileMapper.deleteCompletelyById(file.getId());
            if (FileUtil.isVideo(file)) {
                deleteVideo(file);
            } else {
                if (!FileUtil.isFolder(file)) minIOUtil.deleteFiles(file.getPath());
                if (FileUtil.isAudio(file) && !file.getLyricPath().isEmpty()) {
                    deleteLyric(file);
                }
            }
        } else {
            // 逻辑删除
            file.setDeleteTime(LocalDateTime.now());
            fileMapper.updateById(file);
            fileMapper.deleteById(file);
        }
        // 删除redis中的状态
        redisUtil.delete(RedisConstants.STATUS_KEY + ":" + file.getId());
        // 减少redis中的上传次数
        String contentId = FileUtil.generateContentId(file.getMd5());
        String key = RedisConstants.UPLOAD_FILE_KEY + ":" + contentId;
        Integer count = redisUtil.get(key);
        if (count != null) {
            if (count != 0) redisUtil.decr(key);
            else redisUtil.delete(key);
        }
        // 如果是文件夹，递归删除
        if (FileUtil.isFolder(file)) {
            List<File> files = lambdaQuery().eq(File::getPid, file.getId()).list();
            for (File file1 : files) {
                deleteItemImpl(file1, complete);
            }
        }
    }

    /**
     * 删除单个文件
     *
     * @param file
     * @param deleteCompletely
     */
    @Override
    public void deleteItem(File file, boolean deleteCompletely) throws Exception {
        // 1. 确保文件是自己的
        String userId = ThreadLocalUtil.getCurrentUserId();
        ThrowUtil.throwIf(!Objects.equals(file.getUserId(), userId), new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR, "删除失败，没有权限"));
        // 2. 删除
        deleteItemImpl(file, deleteCompletely);
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public void deleteByIds(DeleteItemDTO dto) {
        // 考虑到树形结构时同时删父文件夹和子文件/文件夹，如果先删父文件夹时递归删除了子文件/文件夹，再删子文件/文件夹会报错，所以先删文件再删文件夹
        List<File> files = fileMapper.selectBatchIds(dto.getIds());
        for (File file : files) {
            try {
                ThrowUtil.throwIf(file == null, new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS));
                if (!FileUtil.isFolder(file))
                    deleteItem(file, dto.isComplete());
            } catch (Exception e) {
                throw new BusinessException(HttpCodeEnum.FILE_DELETE_ERROR);
            }
        }
        for (File file : files) {
            try {
                if (FileUtil.isFolder(file))
                    deleteItem(file, dto.isComplete());
            } catch (Exception e) {
                throw new BusinessException(HttpCodeEnum.FILE_DELETE_ERROR);
            }
        }
    }


    /**
     * 计算文件/文件夹(递归)大小
     *
     * @param file
     * @return
     */
    private Long calcItemTotalSize(File file) {
        if (FileUtil.isFolder(file)) {
            List<File> files = lambdaQuery().likeLeft(File::getLevel, file.getLevel()).list();
            return files.stream().filter(FileUtil::isNotFolder)
                    .mapToLong(File::getSize).sum();
        } else return file.getSize();
    }


    @Override
    public List<File> copyFile(List<File> files, String targetLevel, String targetId) {
        List<File> files1 = new ArrayList<>();
        try {
            for (File file : files) {
                // 新文件id
                String id = StringUtil.getRandomUUid();
                // 如果为空表示文件夹，继续为空就行了
                String newPath = file.getPath().isEmpty() ? "" : minIOUtil.copyFile(id, file);
                String mergedLevelPrefix = targetLevel.isEmpty() ? "" : (targetLevel + "/");
                String newLevel = mergedLevelPrefix + file.getName();
                if (newLevel.split("/").length > FileConstants.FILE_MAX_LEVEL) {
                    throw new BusinessException(HttpCodeEnum.FILE_MAX_LEVEL_LIMIT, "请求失败，已达到最大深度");
                }
                File file1 = File.builder().build();
                BeanUtils.copyProperties(file, file1);
                file1.setId(id);
                file1.setUserId(ThreadLocalUtil.getCurrentUserId());
                file1.setLevel(newLevel);
                file1.setPath(newPath);
                file1.setPid(targetId);
                file1.setCreateTime(LocalDateTime.now());
                file1.setUpdateTime(LocalDateTime.now());
                files1.add(file1);
                // 复制歌词
                if (FileUtil.isAudio(file) && !file.getLyricPath().isEmpty()) {
                    String newLyricPath = MinIOConstants.LYRIC_FOLDER + "/" + StringUtil.getRandomUUid() + ".lrc";
                    minIOUtil.copyFile(file.getLyricPath(), newLyricPath);
                    file1.setLyricPath(newLyricPath);
                }
                // 视频的放到minio中处理了
                // 如果是文件夹且有子文件，递归复制
                if (file.getMediaType().equals(FileMediaTypeEnum.FOLDER.getMediaType())) {
                    List<File> files2 = lambdaQuery().eq(File::getPid, file.getId()).list();
                    files1.addAll(copyFile(files2, file.getLevel(), file1.getId()));
                }
            }
            return files1;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpCodeEnum.FILE_COPY_ERROR);
        }
    }

    /**
     * 剪切文件，只需修改，无需新建
     *
     * @param files
     * @param targetLevel
     * @param targetId
     * @return
     */
    private List<File> cutFile(List<File> files, String targetLevel, String targetId) {
        List<File> files1 = new ArrayList<>();
        for (File value : files) {
            String mergedLevelPrefix = targetLevel.isEmpty() ? "" : targetLevel + "/";
            String newLevel = mergedLevelPrefix + value.getName();
            if (newLevel.split("/").length > FileConstants.FILE_MAX_LEVEL) {
                throw new BusinessException(HttpCodeEnum.FILE_MAX_LEVEL_LIMIT, "剪切失败，已达到最大深度");
            }
            value.setUpdateTime(LocalDateTime.now());
            value.setPid(targetId);
            value.setLevel(newLevel);
            files1.add(value);
            if (value.getMediaType().equals(FileMediaTypeEnum.FOLDER.getMediaType())) {
                List<File> files2 = lambdaQuery().eq(File::getPid, value.getId()).list();
                files1.addAll(cutFile(files2, value.getLevel(), value.getId()));
            }
        }
        return files1;
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public void moveItem(MoveItemDTO dto) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        // 1. 确保要移动和移动目标都是当前用户的
        List<File> files = lambdaQuery().in(File::getId, dto.getIdList()).eq(File::getUserId, userId).list();
        // 原来在哪就移动到哪
        if (Objects.equals(files.get(0).getPid(), dto.getTargetId())) {
            return;
        }
        List<String> filenames = files.stream().map(File::getName).toList();
        if (files.size() != dto.getIdList().size()) {
            throw new BusinessException(HttpCodeEnum.FILE_MOVE_ERROR, "移动失败，没有权限");
        }
        File target = getById(dto.getTargetId());
        // 不移动到根目录下才有的校验
        if (target != null) {
            if (!Objects.equals(target.getMediaType(), FileMediaTypeEnum.FOLDER.getMediaType())) {
                throw new BusinessException(HttpCodeEnum.FILE_MOVE_ERROR, "移动失败，目标文件夹不存在");
            }
            if (!Objects.equals(target.getUserId(), userId)) {
                throw new BusinessException(HttpCodeEnum.FILE_MOVE_ERROR, "移动失败，没有权限");
            }
        }
        if (files.stream().map(File::getPid).toList().stream().distinct().count() > 1) {
            throw new BusinessException(HttpCodeEnum.FILE_MOVE_ERROR, "移动失败，只支持移动同一个文件夹下的文件/文件夹");
        }
        // 2. 不能和target下的文件/文件夹重名
        Long existsFileCount = lambdaQuery().in(File::getName, filenames)
                .eq(File::getPid, target == null ? "" : target.getId())
                .eq(File::getUserId, userId)
                .count();
        if (existsFileCount > 0) {
            throw new BusinessException(HttpCodeEnum.FILE_MOVE_ERROR, "移动失败，文件/文件夹名重复");
        }
        // 4. 剪切的时候不能粘贴到其中的文件夹下
        if (dto.getOp().equals("cut")) {
            for (File file : files) {
                if (target == null) continue;
                String l1 = target.getLevel();
                String l2 = file.getLevel();
                if (l1.startsWith(l2))
                    throw new BusinessException(HttpCodeEnum.FILE_MOVE_ERROR, "移动失败，不能移动到本文件夹下");
            }
        }
        // 5. 可以移动
        String targetLevel = target == null ? "" : target.getLevel();
        String pid = target == null ? "" : target.getId();
        if (dto.getOp().equals("copy")) {
            // 用户空间限制
            Long totalSize = files.stream().mapToLong(this::calcItemTotalSize).sum();
            if (totalSize + user.getUsedSpace() > user.getTotalSpace()) {
                throw new BusinessException(HttpCodeEnum.SPACE_NOT_ENOUGH, "复制失败，空间不足");
            }
            List<File> files1 = copyFile(files, targetLevel, pid);
            fileMapper.insert(files1);
            files1.forEach(f -> {
                redisUtil.set(f.getId(), f.getStatus(), RedisConstants.STATUS_EXPIRE);
            });
        } else if (dto.getOp().equals("cut")) {
            List<File> files1 = cutFile(files, targetLevel, pid);
//            for (File file : files1) {
//                lambdaUpdate().eq(File::getId, file.getId()).set(File::getLevel, file.getLevel())
//                        .set(File::getPid, file.getPid())
//                        .set(File::getUpdateTime, LocalDateTime.now())
//                        .update();
//            }
//             有点问题，level不更新，所以先不用了
            fileMapper.updateById(files1);
        }
    }
}


