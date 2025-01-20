package com.hfdy.hfdypan.controller;

import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.utils.MinIOUtil;
import jakarta.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hf-dy
 * @date 2025/1/15 23:54
 */
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    @Resource
    private MinIOUtil minIOUtil;

    /**
     * 根据minio中的文件名获取文件
     *
     * @param filename
     * @return
     */
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getFileByName(@PathVariable String filename) {
        try {
            byte[] bytes = minIOUtil.getFile(filename);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentLength(bytes.length);
            return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            throw new BusinessException(HttpCodeEnum.RESOURCE_ERROR);
        }
    }
}
