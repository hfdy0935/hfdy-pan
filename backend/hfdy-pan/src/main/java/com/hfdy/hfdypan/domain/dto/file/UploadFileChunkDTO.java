package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

/**
 * @author hf-dy
 * @date 2025/3/1 08:56
 */
@Data
@Builder
public class UploadFileChunkDTO {
    /**
     * 文件名
     */
    @NotEmpty
    private String filename;
    /**
     * 分块下索引，从0开始
     */
    @Min(0)
    private Integer chunkIndex;
    /**
     * 总分块数
     */
    @Min(0)
    private Integer totalChunkNum;
    /**
     * 整个文件的md5
     */
    @NotEmpty
    private String md5;
    /**
     * 文件总大小，单位B
     */
    @Min(0)
    private Long totalSize;
    /**
     * 父文件夹id
     */
    private String pid;
    /**
     * 分类
     */
    @NotEmpty
    private String category;
    /**
     * 类型
     */
    @NotEmpty
    private String mediaType;
}
