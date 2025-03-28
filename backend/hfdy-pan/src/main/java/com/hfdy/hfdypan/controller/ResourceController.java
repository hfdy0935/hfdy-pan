package com.hfdy.hfdypan.controller;

import com.hfdy.hfdypan.constants.UserConstants;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.utils.FileReqRespUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.net.URLConnection;

/**
 * @author hf-dy
 * @date 2025/1/15 23:54
 */
@RestController
@RequestMapping("/api/resource")
@Slf4j
@CrossOrigin({"*"})
public class ResourceController {

    @Resource
    private FileReqRespUtil fileReqRespUtil;
    @Resource
    private UserMapper userMapper;
    @Resource
    private FileMapper fileMapper;

    /**
     * 根据minio中的文件名获取文件，只支持avatar
     *
     * @param filename
     * @return
     */
    @GetMapping("/avatar/{filename}")
    public void getAvatar(@PathVariable String filename, HttpServletResponse response) {
        String ct = URLConnection.guessContentTypeFromName(filename);
        User user = userMapper.selectById(ThreadLocalUtil.getCurrentUserId());
        long speed = user == null ? UserConstants.DEFAULT_DOWNLOAD_SPEED : user.getDownloadSpeed();
        fileReqRespUtil.writeStreamToResponse(response, "/avatar/" + filename, ct, speed);
    }

    @GetMapping("/lyric/{filename}")
    public void getLyric(@PathVariable String filename, HttpServletResponse response) {
        log.info("获取歌词");
        String userId = ThreadLocalUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        long speed = user == null ? UserConstants.DEFAULT_DOWNLOAD_SPEED : user.getDownloadSpeed();
        fileReqRespUtil.writeStreamToResponse(response, "/lyric/" + filename, "text/plain", speed);
    }
}
