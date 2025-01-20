package com.hfdy.hfdypan.domain.dto.file;

import lombok.Builder;
import lombok.Data;

/**
 * @author hf-dy
 * @date 2025/1/17 11:00
 * @description 查询文件列表的请求体
 */
@Data
@Builder
public class QueryFileListDTO {
    // 文件分类，'all' | 'video' | 'audio' | 'image' | 'docs' | 'others'
    private String category;

    // 页码
    private Integer page;

    // 每页数量
    private Integer pageSize;

    // 父文件夹id，如果有
    private String pid;

    // 搜索关键字，可为null
    private String keyword;

    // 是否按照更新时间排序，null不排序，1升序，0降序
    private Integer orderByUpdateTime;

    // 同上
    private Integer orderBySize;
}
