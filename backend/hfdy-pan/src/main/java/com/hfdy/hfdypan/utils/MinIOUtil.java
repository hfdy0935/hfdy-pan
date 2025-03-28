package com.hfdy.hfdypan.utils;

import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.enums.file.FileCategoryEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.UUID;

/**
 * @author hf-dy
 * @date 2024/11/18 23:32
 */
@Component
@Slf4j
public class MinIOUtil {
    @Getter
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
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        return uploadFile(file, filename);
    }

    /**
     * 上传文件
     *
     * @return 文件链接
     */
    public String uploadFile(MultipartFile file, String filename) {
        existsOrCreateBucket(bucketName);
        try (InputStream is = file.getInputStream()) {
            uploadFile(is, filename, file.getContentType());
            log.info("上传完成");
            // 获取在minio中的路径
            return filename;
        } catch (Exception e) {
            throw new BusinessException(HttpCodeEnum.UPLOAD_FAIL);
        }
    }

    /**
     * 上传文件
     *
     * @param is
     * @param filename
     * @param contentType
     * @return
     * @throws Exception
     */
    public void uploadFile(InputStream is, String filename, String contentType) throws Exception {
        existsOrCreateBucket(bucketName);
        minioClient.putObject(PutObjectArgs.builder().
                bucket(bucketName).
                object(filename).
                stream(is, is.available(), -1)
                .contentType(contentType)
                .build());
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


    /**
     * 读取文件输入流
     *
     * @param filename
     * @return
     * @throws Exception
     */
    public InputStream getFileStream(String filename) throws Exception {
        InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build()
        );
        // 等调用它的方法关
        return is;
    }


    /**
     * 复制视频
     *
     * @param id   新id
     * @param file 视频文件记录
     * @return 新的m3u8文件路径
     */
    private String copyVideo(String id, File file) throws Exception {
        int index = file.getPath().lastIndexOf("/");
        String srcFolder = file.getPath().substring(0, index);
        String destFolder = file.getCategory() + "/" + id;
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(srcFolder)
                        .recursive(true)
                        .build());
        for (Result<Item> object : objects) {
            String name = object.get().objectName();
            int index1 = name.lastIndexOf("/");
            String newName = destFolder + "/" + name.substring(index1);
            copyFile(name, newName);
        }
        return destFolder + file.getPath().substring(index);
    }

    public void copyFile(String src, String dest) throws Exception {
        CopySource copySource = CopySource.builder()
                .bucket(bucketName)
                .object(src)
                .build();
        minioClient.copyObject(CopyObjectArgs.builder()
                .bucket(bucketName)
                .object(dest)
                .source(copySource)
                .build());
    }


    /**
     * 复制文件，用于复制粘贴时
     *
     * @param id   新的id
     * @param file
     * @return
     */
    public String copyFile(String id, File file) throws Exception {
        existsOrCreateBucket(bucketName);
        if (FileUtil.isVideo(file)) {
            return copyVideo(id, file);
        } else {
            String dest = file.getCategory() + "/" + id + "_" + file.getName();
            copyFile(file.getPath(), dest);
            return dest;
        }
    }

    /**
     * 合并分块
     *
     * @param filenames
     */
    public String mergeFileChunks(List<String> filenames, String targetFilename) throws Exception {
        // 根据文件名找到分块
        List<ComposeSource> sources = filenames.stream().map(name -> ComposeSource.builder()
                .bucket(bucketName).object(name).build()).toList();
        // 合并对象
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucketName)
                .object(targetFilename)
                .sources(sources)
                .build();
        // 合并
        minioClient.composeObject(composeObjectArgs);
        // 删除原分块
        for (String name : filenames) {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(name)
                    .build();
            minioClient.removeObject(args);
        }
        return targetFilename;
    }

    /**
     * 移动文件，用于视频合并分块之后开始转码
     *
     * @param srcPath
     * @param dest
     * @throws Exception
     */
    public void moveFile(String srcPath, String dest) throws Exception {
        CopySource copySource = CopySource.builder()
                .bucket(bucketName)
                .object(srcPath)
                .build();
        minioClient.copyObject(CopyObjectArgs.builder()
                .bucket(bucketName)
                .object(dest)
                .source(copySource)
                .build());
        deleteFiles(srcPath);
    }


    /**
     * 删除文件
     *
     * @param filenames
     * @throws Exception
     */
    public void deleteFiles(String... filenames) throws Exception {
        for (String filename : filenames) {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build();
            minioClient.removeObject(removeObjectArgs);
        }
    }

    public void deleteFiles(List<String> filenames) throws Exception {
        for (String filename : filenames) {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build();
            minioClient.removeObject(removeObjectArgs);
        }
    }


    /**
     * 递归删除文件夹
     *
     * @param path
     */
    public void deleteFolder(String path) throws Exception {
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(true) // 递归路径下所有对象
                        .build());
        for (Result<Item> object : objects) {
            String name = object.get().objectName();
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(name).build());
        }
    }
}
