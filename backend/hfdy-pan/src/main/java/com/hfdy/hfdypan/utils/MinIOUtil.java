package com.hfdy.hfdypan.utils;

import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import io.minio.*;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author hf-dy
 * @date 2024/11/18 23:32
 */
@Component
public class MinIOUtil {
    private static final Logger log = LoggerFactory.getLogger(MinIOUtil.class);
    @Resource
    private MinioClient minioClient;
    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 如果不存在buckwt就创建
     *
     * @param name
     */
    public void existsOrCreateBucket(String name) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
            }
        } catch (Exception e) {
            log.error("创建桶失败");
        }
    }

    /**
     * 上传文件
     *
     * @return 文件链接
     */
    public String uploadFile(MultipartFile file) {
        existsOrCreateBucket(bucketName);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder().
                    bucket(bucketName).
                    object(filename).
                    stream(is, is.available(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("上传完成");
            // 获取在minio中的路径
            return filename;
        } catch (Exception e) {
            throw new BusinessException(HttpCodeEnum.UPLOAD_FAIL);
        }
    }

    /**
     * 根据文件名获取文件
     *
     * @param filename 文件名
     * @return 字节数组
     * @throws Exception
     */
    public byte[] getFile(String filename) throws Exception {
        GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filename).build());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024 * 1024 * 3]; // 每次读3M
        int b;
        while ((b = response.read(bytes)) != -1) {
            stream.write(bytes, 0, b);
        }
        stream.flush();
        return stream.toByteArray();
    }


}
