package com.hfdy.hfdypan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfdy.hfdypan.domain.bo.QueryFileBO;
import com.hfdy.hfdypan.domain.dto.file.CreateFolderDTO;
import com.hfdy.hfdypan.domain.dto.file.QueryFileListDTO;
import com.hfdy.hfdypan.domain.dto.file.RenameFileDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.enums.file.FileCategoryEnum;
import com.hfdy.hfdypan.domain.enums.file.FileIsDeletedEnum;
import com.hfdy.hfdypan.domain.enums.file.FileMediaTypeEnum;
import com.hfdy.hfdypan.domain.enums.file.FileStatusEnum;
import com.hfdy.hfdypan.domain.vo.file.FileDetailVO;
import com.hfdy.hfdypan.domain.vo.file.QueryFileListVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.FileService;
import com.hfdy.hfdypan.utils.StringUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import com.hfdy.hfdypan.utils.ThrowUtil;
import com.hfdy.hfdypan.utils.VerifyUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author hf-dy
 * @date 2025/1/17 10:55
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
    //TODO
    // 创建文件夹的时候不考虑回收站的文件夹
    // 从回收站回复的时候需要判断是否和目前文件夹下的文件/文件夹重复

    @Resource
    private FileMapper fileMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    public QueryFileListVO queryFileList(QueryFileListDTO dto) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        // 1. 验证文件分类
        ThrowUtil.throwIf(!VerifyUtil.verifyFileCategory(dto.getCategory()), new BusinessException(HttpCodeEnum.FILE_CATEGORY_WRONG));
        // 2. 查询数据库，未删除的
        LambdaQueryWrapper<File> wrapper = Wrappers.<File>lambdaQuery()
                .eq(!Objects.equals(dto.getCategory(), "all"), File::getCategory, dto.getCategory())
                .like(dto.getKeyword() != null && !dto.getKeyword().isEmpty(), File::getName, dto.getKeyword())
                .eq(dto.getPid() != null, File::getPid, dto.getPid())
                .eq(dto.getPid() != null, File::getUserId, userId); // 如果获取的不是顶级目录，还需要验证该文件夹是否是该用户的
        if (dto.getOrderByUpdateTime() != null) {
            wrapper.orderBy(true, dto.getOrderByUpdateTime() == 1, File::getUpdateTime);
        }
        if (dto.getOrderBySize() != null) {
            wrapper.orderBy(true, dto.getOrderBySize() == 1, File::getSize);
        }
        // 分页
        Page<File> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<File> res = fileMapper.selectPage(page, wrapper);
        // 找到当前文件夹作为父文件夹
        QueryFileBO parent = new QueryFileBO();
        File file = fileMapper.selectById(dto.getPid());
        if (file != null) {
            BeanUtils.copyProperties(file, parent);
        }
        // 3. 组装结果
        List<QueryFileBO> bos = res.getRecords().stream().map(r -> {
            QueryFileBO bo = new QueryFileBO();
            BeanUtils.copyProperties(r, bo);
            return bo;
        }).toList();
        QueryFileListVO vo = new QueryFileListVO();
        vo.setRecords(bos);
        vo.setTotal(page.getTotal());
        vo.setParent(parent);
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }


    @Override
    public void createFolder(CreateFolderDTO dto) {
        // 1. 文件名排除/和\
        ThrowUtil.throwIf(dto.getName().contains("/") || dto.getName().contains("\\"), new BusinessException(HttpCodeEnum.FOLDER_CREATE_FAIL, "文件夹创建失败，文件夹名不能包含'/'和'\\'"));
        // 2. 判断同一pid下文件夹名是否重复
        File file = lambdaQuery()
                .eq(File::getMediaType, "folder")
                .eq(File::getName, dto.getName())
                .eq(dto.getPid() != null, File::getPid, dto.getPid())
                .one();
        ThrowUtil.throwIf(file != null, new BusinessException(HttpCodeEnum.FOLDER_CREATE_FAIL, "文件夹创建失败，该文件夹名已存在"));
        // 3. 生成File
        String userId = ThreadLocalUtil.getCurrentUserId();
        LocalDateTime current = LocalDateTime.now();
        String id = StringUtil.getRandomUUid();
        File newFile = File.builder()
                .id(id).name(dto.getName()).path(dto.getPid()).userId(userId).md5("")
                .path("").createTime(current).updateTime(current).size(null).pid(dto.getPid())
                .category(FileCategoryEnum.OTHERS.getCategory()).mediaType(FileMediaTypeEnum.FOLDER.getMediaTYpe())
                .status(FileStatusEnum.OK.getStatus()).isDeleted(FileIsDeletedEnum.NO.getCode())
                .build();
        // 3. 入库
        ThrowUtil.throwIf(fileMapper.insert(newFile) == 0, new BusinessException(HttpCodeEnum.FOLDER_CREATE_FAIL));
    }

    // 新建文件夹时默认的前缀
    private static final String folderNamePrefix = "新建文件夹";

    @Override
    public String genNewFolderName(String pid) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        // 该用户的该文件夹中找，包括删除和未删除的
        LambdaQueryWrapper<File> fileLambdaQueryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUserId, userId)
                .eq(pid != null && !pid.isEmpty(), File::getPid, pid)
                .select(File::getName);
        List<String> names = fileMapper.selectObjs(fileLambdaQueryWrapper).stream().map(Object::toString).toList();
        int i = 1;
        while (names.contains(folderNamePrefix + i)) {
            i++;
        }
        return folderNamePrefix + i;
    }

    @Override
    public void renameFile(RenameFileDTO dto) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        // 1. 文件名不能包含/和\
        ThrowUtil.throwIf(dto.getName().contains("/") || dto.getName().contains("\\"), new BusinessException(HttpCodeEnum.FILE_RENAME_ERROR, "文件重命名失败，文件名不能包含'/'和'\\'"));
        // 2. 确保文件存在
        File file = lambdaQuery().eq(File::getId, dto.getId()).one();
        ThrowUtil.throwIf(file == null, new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS));
        // 3. 确保是自己的文件
        ThrowUtil.throwIf(!Objects.equals(file.getUserId(), userId), new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR));
        // 4. 确保该父文件夹下文件名不重复，考虑null的情况
        List<String> names = lambdaQuery().isNull(file.getPid() == null, File::getPid)
                .eq(file.getPid() != null, File::getPid, file.getPid())
                .list().stream().map(File::getName).toList();
        ThrowUtil.throwIf(names.contains(dto.getName()), new BusinessException(HttpCodeEnum.FILE_RENAME_ERROR, "重命名失败，文件名重复"));
        // 5. 重命名
        file.setName(dto.getName());
        file.setUpdateTime(LocalDateTime.now());
        boolean res = lambdaUpdate().eq(File::getId, dto.getId()).update(file);
        ThrowUtil.throwIf(!res, new BusinessException(HttpCodeEnum.FILE_RENAME_ERROR));
    }


    /**
     * 递归删除文件夹、子文件夹和子文件
     *
     * @param id
     */
    private void deleteFolder(String id) {
        // 更新删除时间
        lambdaUpdate().eq(File::getId, id).set(File::getDeleteTime, LocalDateTime.now()).update();
        ThrowUtil.throwIf(!removeById(id), new BusinessException(HttpCodeEnum.FILE_DELETE_ERROR));
        LambdaQueryWrapper<File> fileLambdaQueryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getPid, id);
        List<File> files = list(fileLambdaQueryWrapper);
        for (File file : files) {
            if (file.getMediaType().equals("folder")) {
                deleteFolder(file.getId());
            } else {
                ThrowUtil.throwIf(!removeById(file.getId()), new BusinessException(HttpCodeEnum.FILE_DELETE_ERROR));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public void deleteById(String id) {
        // 1. 确保文件/文件夹存在
        File file = lambdaQuery().eq(File::getId, id).one();
        ThrowUtil.throwIf(file == null, new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS));
        // 2. 确保文件是自己的
        String userId = ThreadLocalUtil.getCurrentUserId();
        ThrowUtil.throwIf(!Objects.equals(file.getUserId(), userId), new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR, "删除失败，没有权限"));
        // 3. 逻辑删除
        if (!file.getMediaType().equals("folder")) {
            ThrowUtil.throwIf(!removeById(file.getId()), new BusinessException(HttpCodeEnum.FILE_DELETE_ERROR));
        } else {
            deleteFolder(id);
        }
    }


    @Override
    public FileDetailVO getDetailById(String id) {
        // 1. 确保存在
        File file = lambdaQuery().eq(File::getId, id).one();
        ThrowUtil.throwIf(file == null, new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS));
        // 2. 确保是自己的
        String userId = ThreadLocalUtil.getCurrentUserId();
        ThrowUtil.throwIf(!Objects.equals(file.getUserId(), userId), new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR));
        // 3. 获取成功
        FileDetailVO vo = new FileDetailVO();
        BeanUtils.copyProperties(file, vo);
        // 找到对应用户的用户名
        User user = userMapper.selectById(userId);
        vo.setUsername(user.getNickName());
        // 如果是文件夹，查其下子文件数量
        if (Objects.equals(file.getMediaType(), FileMediaTypeEnum.FOLDER.getMediaTYpe())) {
            Long num = lambdaQuery().eq(File::getPid, file.getId()).count();
            vo.setChildNum(num);
        }
        return vo;
    }
}


