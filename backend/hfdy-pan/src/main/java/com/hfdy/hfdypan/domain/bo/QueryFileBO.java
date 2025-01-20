package com.hfdy.hfdypan.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author hf-dy
 * @date 2025/1/17 13:22
 * @description 分页查询文件列表records的item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryFileBO {
    private String id;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    // 大小，单位：B
    private Long size;
    // 父级文件夹id
    private String pid;
    // 分类
    private String category;
    // 具体类型
    private String mediaType;
}
