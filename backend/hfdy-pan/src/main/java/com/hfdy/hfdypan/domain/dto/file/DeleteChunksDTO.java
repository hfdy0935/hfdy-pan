package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/3 11:14
 */
@Data
public class DeleteChunksDTO {
    @NotEmpty
    private String md5;
    /**
     * 要删除的分块索引列表
     */
    private List<Integer> chunkIndexes;
    @NotEmpty
    private String category;
}
