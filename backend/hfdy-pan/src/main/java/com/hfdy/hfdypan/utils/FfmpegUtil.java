package com.hfdy.hfdypan.utils;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.enums.file.FileStatusEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hf-dy
 * @date 2025/3/4 12:54
 */
@Component
@Slf4j
public class FfmpegUtil {
    @Resource
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;


    private static final Path tempDir = Paths.get("./resources/temp_video");
    private static final String m3u8Filename = "playlist.m3u8";
    private static final String tsFilenameTemplate = "segment_%03d.ts";
    /**
     * 每个ts的时间
     */
    private static final Integer HlsTime = 20;

    static {
        if (!Files.exists(tempDir)) {
            try {
                Files.createDirectories(tempDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Resource
    private MinIOUtil minIOUtil;
    @Resource
    private FileMapper fileMapper;


    /**
     * 检查数据库中的文件是否存在，如果不存在就删除本地复制的文件
     *
     * @param id
     * @return
     * @throws Exception
     */
    private boolean fileExistsCheck(String id) throws Exception {
        return fileMapper.exists(Wrappers.<File>lambdaQuery().eq(File::getId, id));
    }

    /**
     * 转码
     *
     * @param src      MinIO中的原文件名
     * @param destDir  目标目录（在MinIO中）
     * @param id       数据库中m3u8文件的id
     * @param filename
     * @return m3u8 文件路径，空字符串表示转码失败
     * @throws Exception
     */
    public String transCode(String src, String destDir, String id, String filename) {
        // minio中文件路径列表
        List<String> filenames = new ArrayList<>();
        Path idDir = tempDir.resolve(id);
        try {
            // 根据 id 创建子目录
            if (!Files.exists(idDir)) {
                try {
                    Files.createDirectories(idDir);
                } catch (IOException e) {
                    return "";
                }
            }
            // 从 MinIO 下载视频到本地
            String tempFilepath = idDir.resolve(filename).toString();
            downloadFileFromMinio(src, tempFilepath);
            // 如果数据库中被删了，就不转码了
            if (!fileExistsCheck(id)) {
                return "";
            }

            // 构造 FFmpeg 命令
            List<String> command = Arrays.asList(
                    "ffmpeg",
                    "-i", tempFilepath,
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-f", "hls",
                    "-hls_time", HlsTime.toString(),
                    "-hls_list_size", "0", // 不限制数量
                    "-hls_segment_filename", idDir.resolve(tsFilenameTemplate).toString(),
                    idDir.resolve(m3u8Filename).toString()
            );
            log.info("开始转码");

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.inheritIO().start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("转码失败，使用原视频链接");
                throw new RuntimeException();
            }

            // 修改ts内容，加上 /api/file/{fileId}/ 前缀
            modifyM3U8File(id, idDir.resolve(m3u8Filename).toString());

            // 如果数据库中文件被删了，就不上传
            if (!fileExistsCheck(id)) {
                deleteDirectory(idDir);
                return "";
            }
            // 上传 M3U8 文件和 TS 分片到 MinIO
            uploadFilesToMinio(idDir, destDir, filenames);

            log.info("转码完成");
            return destDir + "/" + m3u8Filename;
        } catch (Exception e) {
            log.error("转码失败，{}", e.getMessage());
            // 更新状态
            LambdaUpdateWrapper<File> fileLambdaUpdateWrapper = Wrappers.<File>lambdaUpdate().eq(File::getId, id).set(File::getStatus, FileStatusEnum.TRANS_FAIL);
            fileMapper.update(fileLambdaUpdateWrapper);
            // 删除minio中的文件夹
            try {
                minIOUtil.deleteFiles(filenames);
            } catch (Exception ex) {
                log.error("删除minio文件失败");
            }
            return "";
        } finally {
            // 清理本地临时文件
            try {
                deleteDirectory(idDir);
            } catch (Exception ex) {
            }
        }
    }

    /**
     * 从MinIO下载文件到本地路径
     *
     * @param objectName 对象名称
     * @param filePath   本地文件夹路径
     * @throws Exception 如果发生错误
     */
    private void downloadFileFromMinio(String objectName, String filePath) throws Exception {
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        )) {
            Files.copy(inputStream, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 上传本地文件到 MinIO
     *
     * @param tempDir      本地临时目录
     * @param targetFolder MinIO 中的目标文件夹路径
     * @param filenames
     * @throws IOException 如果发生 IO 错误
     */
    private void uploadFilesToMinio(Path tempDir, String targetFolder, List<String> filenames) throws IOException {
        Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try (FileInputStream fis = new FileInputStream(file.toFile());) {
                        String objectName = targetFolder + "/" + file.getFileName();
                        filenames.add(objectName);
                        minioClient.putObject(
                                PutObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(objectName)
                                        .stream(fis, file.toFile().length(), -1)
                                        .contentType("video/MP2T")
                                        .build()
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * 删除本地文件
     */
    private void deleteDirectory(Path path) throws Exception {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                // 从底部开始删除，确保先删除子文件/目录
                walk.sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                log.error("Failed to delete: {}", p.toString(), e);
                            }
                        });
            }
            // 确保顶级目录也被删除
            Files.deleteIfExists(path);
        } else {
            log.warn("Directory does not exist: {}", path.toString());
        }
    }

    /**
     * 修改ts内容，加上url前缀
     *
     * @param fileId
     * @param m3u8FilePath
     * @throws IOException
     */
    public static void modifyM3U8File(String fileId, String m3u8FilePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(m3u8FilePath)));
        // 替换.ts文件的相对路径为自定义URL
        String modifiedContent = content.lines()
                .map(line -> {
                    if (line.endsWith(".ts")) {
                        // 假设每个.ts文件都按照"segment_XXX.ts"的格式命名
                        String tsFileName = line.trim();
                        return "/api/file/m3u8-ts/" + fileId + "/" + tsFileName;
                    }
                    return line;
                })
                .collect(Collectors.joining("\n"));
        // 将修改后的内容写回到原来的.m3u8文件或新文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(m3u8FilePath))) {
            writer.write(modifiedContent);
        }
    }
}