package com.hfdy.hfdypan.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfdy.hfdypan.constants.FileConstants;
import com.hfdy.hfdypan.domain.bo.QueryFileListBO;
import com.hfdy.hfdypan.domain.dto.file.SaveShareToMyPanDTO;
import com.hfdy.hfdypan.domain.dto.file.ShareFileDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.entity.Share;
import com.hfdy.hfdypan.domain.entity.ShareFile;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.vo.file.GetShareFileVO;
import com.hfdy.hfdypan.domain.vo.file.ShareFileVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.ShareFileMapper;
import com.hfdy.hfdypan.mapper.ShareMapper;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.ResourceService;
import com.hfdy.hfdypan.service.TransferFileService;
import com.hfdy.hfdypan.utils.FileUtil;
import com.hfdy.hfdypan.utils.MinIOUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author hf-dy
 * @date 2025/3/7 17:05
 */
@Service
public class TransFileServiceImpl extends ServiceImpl<ShareMapper, Share> implements TransferFileService {
    @Resource
    private FileMapper fileMapper;
    @Resource
    private ShareMapper shareMapper;
    @Resource
    private ShareFileMapper shareFileMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ResourceService resourceService;
    @Resource
    private MinIOUtil minIOUtil;
    @Resource
    private CommonFileServiceImpl commonFileService;

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public ShareFileVO shareFiles(ShareFileDTO dto, List<File> fileList) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        Share share = new Share();
        share.setUserId(userId);
        share.setExpire(dto.getExpire());
        share.setPwd(dto.getPwd() == null ? "" : dto.getPwd());
        if (shareMapper.insert(share) != 1) {
            throw new BusinessException(HttpCodeEnum.FILE_SHARE_ERROR);
        }
        List<ShareFile> shareFileList = new ArrayList<>();
        for (File file : fileList) {
            ShareFile shareFile = new ShareFile();
            shareFile.setFileId(file.getId());
            shareFile.setShareId(share.getId());
            shareFileList.add(shareFile);
        }
        shareFileMapper.insert(shareFileList);
        ShareFileVO vo = new ShareFileVO();
        vo.setShareId(share.getId());
        vo.setPwd(dto.getPwd());
        return vo;
    }


    @Override
    public GetShareFileVO getSharedFile(Share share, String pid) {
        List<File> fileList = new ArrayList<>();
        // 如果传了父文件id
        if (!pid.isEmpty()) fileList = fileMapper.selectList(Wrappers.<File>lambdaQuery().eq(File::getPid, pid));
        else {
            // 查找所有分享的文件
            List<String> fileIds = shareFileMapper.selectList(Wrappers.<ShareFile>lambdaQuery()
                            .eq(ShareFile::getShareId, share.getId()))
                    .stream().map(ShareFile::getFileId).toList();
            if (!fileIds.isEmpty())
                fileList = fileMapper.selectList(Wrappers.<File>lambdaQuery().in(File::getId, fileIds));
        }
        // 组装结果
        List<QueryFileListBO> bos = new ArrayList<>();
        for (File file : fileList) {
            QueryFileListBO bo = new QueryFileListBO();
            BeanUtils.copyProperties(file, bo);
            if (FileUtil.isAudio(file) && !file.getLyricPath().isEmpty()) {
                bo.setLyricPath(resourceService.addPrefix(file.getLyricPath()));
            }
            bos.add(bo);
        }
        // 如果访问的是根目录，查看次数+1
        if (pid.isEmpty())
            share.setVisitNum(share.getVisitNum() + 1);
        shareMapper.updateById(share);
        User user = userMapper.selectById(share.getUserId());
        GetShareFileVO vo = new GetShareFileVO();
        vo.setId(share.getId());
        vo.setUsername(user.getNickName());
        vo.setAvatar(user.getAvatar().isEmpty() ? "" : resourceService.addPrefix(user.getAvatar()));
        vo.setCreateTime(share.getCreateTime());
        vo.setRecords(bos);
        File p = fileMapper.selectById(pid);
        QueryFileListBO bo = new QueryFileListBO();
        if (p != null) BeanUtils.copyProperties(p, bo);
        vo.setParent(bo);
        vo.setVisitNum(share.getVisitNum());
        return vo;
    }

    @Override
    public void downloadAsZip(List<File> fileList, HttpServletResponse response, long speed) throws Exception {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=download.zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            // 一级文件夹
            for (File file : fileList) {
                List<File> files = new ArrayList<>();
                if (FileUtil.isFolder(file)) {
                    // 查找子文件/文件夹
                    files = fileMapper.selectList(Wrappers.<File>lambdaQuery().likeRight(File::getLevel, file.getLevel() + "/"));
                }
                // 加上自己
                files.add(file);
                // 需要排除的路径，也就是文件夹/文件前面的路径
                String excludePath = FileUtil.getFolderPath(file.getLevel());
                for (File file1 : files) {
                    String relativePath = file1.getLevel().replace(excludePath, "");
                    // 如果原来不是顶级，还有个/
                    if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
                    // 文件夹，加一个后缀直接写入
                    if (FileUtil.isFolder(file1)) {
                        ZipEntry zipEntry = new ZipEntry(relativePath + "/");
                        zipOutputStream.putNextEntry(zipEntry);
                    } else {
                        // minio中存原始文件的路径
                        String filename = FileUtil.isVideo(file1) ? FileUtil.getRawVideoPath(file1) : file1.getPath();
                        try (InputStream inputStream = minIOUtil.getFileStream(filename)) {
                            // 使用文件名作为ZIP条目名称，确保唯一性
                            ZipEntry zipEntry = new ZipEntry(relativePath);
                            zipOutputStream.putNextEntry(zipEntry);
                            // 增加缓冲区大小以提高复制速度
                            byte[] buffer = new byte[8192];
                            int len;
                            long startTime = System.currentTimeMillis();
                            long totalBytesRead = 0;
                            while ((len = inputStream.read(buffer)) > -1) {
                                zipOutputStream.write(buffer, 0, len);
                                zipOutputStream.flush();
                                totalBytesRead += len;
                                long elapsedTime = System.currentTimeMillis() - startTime;
                                // 计算需要的总时间以保持目标速率
                                long expectedElapsedTime = (long) ((totalBytesRead * 1000.0) / speed);
                                // 如果当前经过的时间少于预期的经过时间，则休眠差值时间
                                if (elapsedTime < expectedElapsedTime) {
                                    Thread.sleep(Math.max(0, expectedElapsedTime - elapsedTime));
                                }
                            }
                            zipOutputStream.closeEntry();
                        }
                    }
                }
            }
            response.getOutputStream().flush();
        }
    }

    @Override
    public void saveShareToMyPan(SaveShareToMyPanDTO dto, File to) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        // 验证不重名
        List<File> files = fileMapper.selectBatchIds(dto.getSrcIds());
        List<String> fileIds = files.stream().map(File::getId).toList();
        String pid = to == null ? "" : to.getId();
        int pLevel = to == null ? 0 : FileUtil.getLevelNum(to);
        if (fileMapper.exists(Wrappers.<File>lambdaQuery().in(File::getId, fileIds).eq(File::getPid, pid).eq(File::getUserId, userId)))
            throw new BusinessException(HttpCodeEnum.FILE_SAVE_ERROR, "保存失败，文件名/文件夹重复");
        long totalSize = 0L;
        // 验证层级
        for (File file : files) {
            // 自己的大小
            totalSize += FileUtil.isNotFolder(file) ? file.getSize() : 0;
            if (FileUtil.isNotFolder(file)) continue;
            List<File> files1 = fileMapper.selectList(Wrappers.<File>lambdaQuery().likeLeft(File::getLevel, file.getLevel() + "%"));
            if (files1.isEmpty()) continue;
            files1.sort((a, b) -> FileUtil.getLevelNum(b) - FileUtil.getLevelNum(a));
            int oriMaxLevel = FileUtil.getLevelNum(files1.get(0));
            int aLevel = FileUtil.getLevelNum(file);
            if (oriMaxLevel - aLevel + pLevel > FileConstants.FILE_MAX_LEVEL)
                throw new BusinessException(HttpCodeEnum.FILE_MAX_LEVEL_LIMIT);
            // 所有子文件/文件夹的大小
            totalSize += files1.stream().filter(FileUtil::isNotFolder).mapToLong(File::getSize).sum();
        }
        if (user.getUsedSpace() + totalSize > user.getTotalSpace()) {
            throw new BusinessException(HttpCodeEnum.SPACE_NOT_ENOUGH, "保存失败，空间不足");
        }
        // 复制
        fileMapper.insert(commonFileService.copyFile(files, to == null ? "" : to.getLevel(), pid));
    }
}
