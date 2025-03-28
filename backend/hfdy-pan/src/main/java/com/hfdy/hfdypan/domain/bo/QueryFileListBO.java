package com.hfdy.hfdypan.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hfdy.hfdypan.domain.enums.file.FileStatusEnum;
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
public class QueryFileListBO {
    private String id;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    // 大小，单位：B
    private Long size;
    // 父级文件夹id
    private String pid;
    // 文件/文件夹层级
    private String level;
    // 分类
    private String category;
    // 具体类型
    private String mediaType;
    // 状态
    private Integer status;
    // 歌词路径
    private String lyricPath;
}
