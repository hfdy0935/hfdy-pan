package com.hfdy.hfdypan.domain.vo.file;

import lombok.Data;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/9 11:08
 */
@Data
public class GetUploadedChunkIndexesVO {
    /**
     * 是否上传过完整的文件
     */
    private boolean uploaded;
    /**
     * 已上传的分片
     */
    private List<Integer> indexes;
}
