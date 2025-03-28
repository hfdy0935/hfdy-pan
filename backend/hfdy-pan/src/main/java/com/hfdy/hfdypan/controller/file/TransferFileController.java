package com.hfdy.hfdypan.controller.file;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.constants.UserConstants;
import com.hfdy.hfdypan.domain.dto.file.DownloadFileDTO;
import com.hfdy.hfdypan.domain.dto.file.SaveShareToMyPanDTO;
import com.hfdy.hfdypan.domain.dto.file.ShareFileDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.entity.Share;
import com.hfdy.hfdypan.domain.entity.ShareFile;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.vo.ApiResp;
import com.hfdy.hfdypan.domain.vo.file.GetFolderLevelInfoVO;
import com.hfdy.hfdypan.domain.vo.file.GetShareFileVO;
import com.hfdy.hfdypan.domain.vo.file.ShareFileVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.ShareFileMapper;
import com.hfdy.hfdypan.mapper.ShareMapper;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.ResourceService;
import com.hfdy.hfdypan.service.TransferFileService;
import com.hfdy.hfdypan.service.UserService;
import com.hfdy.hfdypan.utils.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hf-dy
 * @date 2025/3/1 20:23
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@CrossOrigin({"*"})
@Validated
public class TransferFileController {
    @Resource
    private FileMapper fileMapper;
    @Resource
    private MinIOUtil minIOUtil;
    @Value("${server.deploy_address}")
    private String deployAddress;
    @Resource
    private TransferFileService transferFileService;
    @Resource
    private RedisUtil<String> redisUtil;
    @Resource
    private ShareFileMapper shareFileMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private FileReqRespUtil fileReqRespUtil;
    @Resource
    private ResourceService resourceService;
    @Resource
    private UserService userService;


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
     * 预览文件
     *
     * @param fileId
     * @return
     */
    @GetMapping("/preview/{fileId}")
    public void preview(@PathVariable("fileId") @NotEmpty String fileId,
                        HttpServletResponse response,
                        HttpServletRequest request,
                        @RequestHeader(value = "shareId", defaultValue = "") String shareId
    ) throws IOException {
        boolean isFromShare = StringUtil.isFromPublicShareFile(request);
        if (isFromShare) ensureShareExist(shareId);
        String userId = ThreadLocalUtil.getCurrentUserId();
        File file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS);
        }
        // 不来自分享页面，需要验证是不是自己的文件`
        if (!isFromShare && !file.getUserId().equals(userId)) {
            throw new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR);
        }
        // 来自分享页面需要验证是不是该分享下的文件
        if (isFromShare) ensureShareContainsFile(shareId, file.getId());
        // 如果是m3u8，返回url，重新请求一次，防止前端URL.createObjectURl之后m3u8的ts文件请求了blob
        if (file.getPath().endsWith(".m3u8")) {
            String url = deployAddress + "/api/file/m3u8/" + fileId;
            response.setContentType("application/x-mpegURL");
            response.getWriter().write(url);
        } else {
            User user = userMapper.selectById(userId);
            long speed = Optional.ofNullable(user)
                    .map(User::getDownloadSpeed)
                    .orElse(UserConstants.DEFAULT_DOWNLOAD_SPEED);
            // 预览时快一点
            fileReqRespUtil.writeStreamToResponse(response, file.getPath(), URLConnection.guessContentTypeFromName(file.getName()), speed);
        }
    }


    /**
     * 获取m3u8
     *
     * @param id
     * @return
     */
    @GetMapping("/m3u8/{id}")
    public ResponseEntity<byte[]> getVideoById(@PathVariable String id,
                                               HttpServletRequest request,
                                               @RequestHeader(value = "shareId", defaultValue = "") String shareId
    ) {
        boolean isFromShare = StringUtil.isFromPublicShareFile(request);
        if (isFromShare) ensureShareExist(shareId);
        try {
            File file = fileMapper.selectById(id);
            if (isFromShare) ensureShareContainsFile(shareId, file.getId());
            byte[] bytes = minIOUtil.getFile(file.getPath());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentLength(bytes.length);
            return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            throw new BusinessException(HttpCodeEnum.RESOURCE_ERROR);
        }
    }


    /**
     * 获取m3u8的ts
     *
     * @param fileId
     * @param tsName
     * @param response
     */
    @GetMapping("/m3u8-ts/{fileId}/{tsName}")
    public void m3u8Ts(@PathVariable("fileId") String fileId,
                       @PathVariable("tsName") String tsName,
                       HttpServletResponse response,
                       @RequestHeader(value = "shareId", defaultValue = "") String shareId
    ) {
        boolean isFromShare = !shareId.isEmpty();
        if (isFromShare) ensureShareExist(shareId);
        File file = fileMapper.selectById(fileId);
        if (isFromShare) ensureShareContainsFile(shareId, file.getId());
        String userId = ThreadLocalUtil.getCurrentUserId();
        if (!isFromShare && !file.getUserId().equals(userId)) {
            throw new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR);
        }
        String[] m3u8FilePathParts = file.getPath().split("/");
        String m3u8DirPath = Arrays.stream(m3u8FilePathParts).map(s -> s.endsWith(".m3u8") ? "" : s).collect(Collectors.joining("/"));
        String tsMinioPath = m3u8DirPath + tsName;
        User user = userMapper.selectById(userId);
        fileReqRespUtil.writeStreamToResponse(response, tsMinioPath, "video/MP2T", user.getDownloadSpeed());
    }


    /**
     * 从文件列表界面下载自己的文件
     *
     * @param dto
     * @param response
     */
    @PostMapping("/download")
    public void downloadFile(@RequestBody @Valid DownloadFileDTO dto, HttpServletResponse response) {
        if (dto.getFileIds().isEmpty()) {
            throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS, "下载失败，文件不能为空");
        }
        String userId = ThreadLocalUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(HttpCodeEnum.UN_AUTHORIZATION);
        List<File> fileList = fileMapper.selectList(Wrappers.<File>lambdaQuery().in(File::getId, dto.getFileIds()));
        fileList.forEach(file -> {
            if (!file.getUserId().equals(userId)) {
                throw new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR);
            }
        });
        // 单个文件，不是文件夹，直接下载
        File first = fileList.get(0);
        if (fileList.size() == 1 && !FileUtil.isFolder(first)) {
            String filename = FileUtil.isVideo(first) ? FileUtil.getRawVideoPath(first) : first.getPath();
            fileReqRespUtil.writeStreamToResponse(response, filename, URLConnection.guessContentTypeFromName(first.getName()), user.getDownloadSpeed());
        } else {
            try {
                transferFileService.downloadAsZip(fileList, response, user.getDownloadSpeed());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }


    /**
     * 获取用户自己的目录，用于把分享的文件保存到自己的网盘
     *
     * @return
     */
    @GetMapping("/folderLevelInfo")
    public ApiResp<List<GetFolderLevelInfoVO>> getFolderLevelInfo() {
        String userId = ThreadLocalUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpCodeEnum.UN_AUTHORIZATION);
        }
        // 一级文件/文件夹
        List<File> files = fileMapper.selectList(Wrappers.<File>lambdaQuery().eq(File::getUserId, userId).eq(File::getPid, ""));
        List<GetFolderLevelInfoVO> vos = new ArrayList<>();
        for (File file : files) {
            vos.add(getFolderLevelInfoVO(file));
        }
        return ApiResp.success(vos);
    }


    private GetFolderLevelInfoVO getFolderLevelInfoVO(File file) {
        GetFolderLevelInfoVO vo = GetFolderLevelInfoVO.newWithChildren();
        BeanUtils.copyProperties(file, vo);
        if (FileUtil.isAudio(file) && !file.getLyricPath().isEmpty()) {
            vo.setLyricPath(resourceService.addPrefix(file.getLyricPath()));
        }
        if (!FileUtil.isFolder(file)) return vo;
        // children
        List<File> files = fileMapper.selectList(Wrappers.<File>lambdaQuery().eq(File::getPid, file.getId()));
        if (files.isEmpty()) return vo;
        vo.setChildren(files.stream().map(this::getFolderLevelInfoVO).toList());
        return vo;
    }


    /**
     * 把分享文件转存到我的网盘
     *
     * @param dto
     * @return
     */
    @PostMapping("saveShareToMyPan")

    public ApiResp<Void> saveShareToMyPan(@RequestBody @Valid SaveShareToMyPanDTO dto) {
        // 验证to是当前用户的
        String userId = ThreadLocalUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(HttpCodeEnum.USER_NOT_EXISTS);
        // 如果不保存到根目录，需要验证to是当前用户的
        File to = null;
        if (!dto.getTo().isEmpty()) {
            to = fileMapper.selectOne(Wrappers.<File>lambdaQuery().eq(File::getUserId, userId).eq(File::getId, dto.getTo()));
            if (to == null) throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS, "转存失败，文件夹不存在");
        }
        // 验证shareId存在
        ensureShareExist(dto.getShareId());
        // 验证文件都是该分享下的
        for (String fileId : dto.getSrcIds()) ensureShareContainsFile(dto.getShareId(), fileId);
        transferFileService.saveShareToMyPan(dto, to);
        // 更新用户空间
        userService.updateUserUsedSpace();
        return ApiResp.success();
    }
}
