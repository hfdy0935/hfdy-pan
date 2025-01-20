package com.hfdy.hfdypan.config;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.UUID;

/**
 * @author hf-dy
 * @date 2024/11/19 10:15
 */
@SpringBootTest
public class MinIOTest {
    private static final Logger log = LoggerFactory.getLogger(MinIOTest.class);
    @Resource
    private MinioClient minioClient;
    @Value("${minio.bucketName}")
    public String bucketName;

    @Test
    public void uploadTest() throws Exception {
        String config = "{\n" +
                "     \"Statement\": [\n" +
                "         {\n" +
                "             \"Action\": [\n" +
                "                 \"s3:GetBucketLocation\",\n" +
                "                 \"s3:ListBucket\"\n" +
                "             ],\n" +
                "             \"Effect\": \"Allow\",\n" +
                "             \"Principal\": \"*\",\n" +
                "             \"Resource\": \"arn:aws:s3:::" + bucketName + "\"\n" +
                "         },\n" +
                "         {\n" +
                "             \"Action\": \"s3:GetObject\",\n" +
                "             \"Effect\": \"Allow\",\n" +
                "             \"Principal\": \"*\",\n" +
                "             \"Resource\": \"arn:aws:s3:::" + bucketName + "/*" + "\"\n" +
                "         }\n" +
                "     ],\n" +
                "     \"Version\": \"2012-10-17\"\n" +
                "}";
        try (FileInputStream is = new FileInputStream("E:\\Desktop\\hfdy-pan\\backend\\hfdy-pan\\src\\main\\resources\\static\\avatar.png");) {
            int year = LocalDate.now().getYear();
            String filename = String.valueOf(year) + "/" + UUID.randomUUID() + "_" + "avatar.png";
            log.info(filename);
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            minioClient.putObject(PutObjectArgs.builder().
                    bucket(bucketName).
                    object(filename).
                    stream(is, is.available(), -1)
                    .contentType("image/png")
                    .build());
            log.info("上传完成");
            // 存储策略
//            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
//                    .bucket(bucketName)
//                    .config(config)
//                    .build());
            log.info(minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .method(Method.GET)
                    .build()));
//            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucketName).object(filename).build());
        } catch (Exception e) {
            log.error(e.getMessage());
//            throw new BusinessException(HttpCodeEnum.UPLOAD_FAIL);
        }
    }

}
