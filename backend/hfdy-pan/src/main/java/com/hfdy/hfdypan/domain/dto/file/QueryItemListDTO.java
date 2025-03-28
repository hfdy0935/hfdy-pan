package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import org.intellij.lang.annotations.RegExp;

/**
 * @author hf-dy
 * @date 2025/1/17 11:00
 * @description 查询文件列表的请求体
 */
@Data
@Builder
public class QueryItemListDTO {
    // 文件分类，'all' | 'video' | 'audio' | 'image' | 'docs' | 'others'
    @NotEmpty
    private String category;

    // 页码
    @Min(1)
    private Integer page;

    // 每页数量
    @Min(1)
    private Integer pageSize;

    // 父文件夹id，如果有
    private String pid;

    // 搜索关键字，可为null
    private String keyword;

    // 是否按照更新时间排序，-1不排序，1升序，0降序
    @NotNull
    private Integer orderByUpdateTime;

    // 同上
    @NotNull
    private Integer orderBySize;
}
