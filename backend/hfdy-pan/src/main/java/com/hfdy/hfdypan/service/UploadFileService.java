package com.hfdy.hfdypan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfdy.hfdypan.domain.dto.file.UploadFileChunkDTO;
import com.hfdy.hfdypan.domain.entity.File;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author hf-dy
 * @date 2025/2/28 15:08
 */
public interface UploadFileService extends IService<File> {


    /**
     * 根据分块所在文件的id和分块索引生成minio保存路径
     *
     * @param id
     * @param chunkIndex
     * @return
     */
    String getMinIOChunkName(String id, int chunkIndex);

    /**
     * 上传文件分块
     *
     * @param dto
     * @param file
     * @return
     */
    String uploadChunk(UploadFileChunkDTO dto, MultipartFile file);

    /**
     * 合并视频、转码、存储，默认转码完成时视频已上传完毕
     *
     * @param dto
     * @param src    数据库中视频文件的路径
     * @param id     数据库中的文件id
     * @param userId
     */
    boolean transVideo(UploadFileChunkDTO dto, String src, String id, String userId);

    boolean transVideo(File file, String userId);

    /**
     * 合并分块
     *
     * @param dto
     * @return
     */
    String mergeChunks(UploadFileChunkDTO dto);


    /**
     * 上传整个文件，字段用到UploadFileChunkDTO的
     *
     * @param dto
     * @param file
     */
    int uploadFile(UploadFileChunkDTO dto, MultipartFile file);


    /**
     * 上传歌词
     *
     * @param fileId
     * @param lyric
     */
    void uploadLyric(String fileId, MultipartFile lyric);


}
