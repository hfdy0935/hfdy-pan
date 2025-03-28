package com.hfdy.hfdypan.controller.file;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.constants.UserConstants;
import com.hfdy.hfdypan.domain.dto.file.DownloadFileDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.entity.Share;
import com.hfdy.hfdypan.domain.entity.ShareFile;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.vo.ApiResp;
import com.hfdy.hfdypan.domain.vo.file.GetShareFileVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.ShareFileMapper;
import com.hfdy.hfdypan.mapper.ShareMapper;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.TransferFileService;
import com.hfdy.hfdypan.utils.FileReqRespUtil;
import com.hfdy.hfdypan.utils.FileUtil;
import com.hfdy.hfdypan.utils.RedisUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

/**
 * @author hf-dy
 * @date 2025/3/11 23:59
 */
@RestController
@RequestMapping("/api/share")
@Slf4j
@Validated
public class PublicShareController {
    @Resource
    private RedisUtil<String> redisUtil;
    @Resource
    private ShareMapper shareMapper;
    @Resource
    private TransferFileService transferFileService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private FileMapper fileMapper;
    @Resource
    private FileReqRespUtil fileReqRespUtil;
    @Resource
    private ShareFileMapper shareFileMapper;

    /**
     * 在redis中查，确保分享存在
     *
     * @param shareId
     */
    private void ensureShareExist(String shareId) {
        if (!shareId.isEmpty() && !redisUtil.exists(RedisConstants.SHARED_FILE_KEY + ":" + shareId)) {
            throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS, "获取失败，该分享不存在");
        }
    }

    /**
     * 确保文件在分享下
     *
     * @param shareId
     * @param fileId
     */
    private void ensureShareContainsFile(String shareId, String fileId) {
        File file = fileMapper.selectById(fileId);
        ensureShareContainsFile(shareId, file);
    }


    private void ensureShareContainsFile(String shareId, File file) {
        // 找到该分享下的所有文件夹（一级），比较fileId是否是他们中任意一个的子文件/文件夹
        List<String> fileIds = shareFileMapper.selectList(Wrappers.<ShareFile>lambdaQuery().eq(ShareFile::getShareId, shareId)).stream().map(ShareFile::getFileId).toList();
        List<File> fileList = fileMapper.selectList(Wrappers.<File>lambdaQuery().in(File::getId, fileIds));
        for (File file1 : fileList) {
            if (FileUtil.isChildOrSelf(file.getLevel(), file1.getLevel())) {
                return;
            }
        }
        throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS, "获取失败，该文件还未被分享");
    }

    /**
     * 从分享界面下载文件
     *
     * @param dto
     * @param response
     */
    @PostMapping("/download")
    public void downloadShareFile(@Valid @RequestBody DownloadFileDTO dto, HttpServletResponse response, @RequestHeader(value = "shareId", defaultValue = "") String shareId) {
        boolean isFromShare = !shareId.isEmpty();
        if (isFromShare) ensureShareExist(shareId);
        String userId = ThreadLocalUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        long speed = Optional.ofNullable(user)
                .map(User::getDownloadSpeed)
                .orElse(UserConstants.DEFAULT_DOWNLOAD_SPEED);
        if (dto.getFileIds().isEmpty()) {
            throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS, "下载失败，文件不能为空");
        }
        List<File> fileList = fileMapper.selectList(Wrappers.<File>lambdaQuery().in(File::getId, dto.getFileIds()));
        if (isFromShare) {
            for (File file : fileList) {
                ensureShareContainsFile(shareId, file);
            }
        }
        // 单个文件，不是文件夹，直接下载
        File first = fileList.get(0);
        if (fileList.size() == 1 && !FileUtil.isFolder(first)) {
            String path = first.getPath();
            fileReqRespUtil.writeStreamToResponse(response, path, URLConnection.guessContentTypeFromName(first.getName()), speed);
        } else {
            try {
                transferFileService.downloadAsZip(fileList, response, speed);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * 获取分享文件列表
     *
     * @param id
     * @param pwd
     * @return
     */
    @GetMapping("/{id}")
    public ApiResp<GetShareFileVO> getSharedFile(@PathVariable("id") String id,
                                                 @RequestParam(value = "pwd", defaultValue = "") String pwd,
                                                 @RequestParam(value = "pid", defaultValue = "") String pid) {
        if (!redisUtil.exists(RedisConstants.SHARED_FILE_KEY + ":" + id)) {
            throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS, "获取失败，该分享不存在");
        }
        Share share = shareMapper.selectById(id);
        if (share == null) {
            throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS, "获取失败，该分享不存在");
        }
        if (!share.getPwd().isEmpty() && !share.getPwd().equals(pwd)) {
            throw new BusinessException(HttpCodeEnum.FILE_GET_SHARE_ERROR, "获取失败，提取码错误");
        }
        // 确保父文件id属于该分享
        if (!pid.isEmpty()) ensureShareContainsFile(share.getId(), pid);
        return ApiResp.success(transferFileService.getSharedFile(share, pid));
    }
}
