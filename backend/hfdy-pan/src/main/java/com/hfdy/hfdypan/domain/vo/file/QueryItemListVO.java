package com.hfdy.hfdypan.domain.vo.file;

import com.hfdy.hfdypan.domain.bo.QueryFileListBO;
import com.hfdy.hfdypan.domain.enums.file.FileStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/1/17 13:17
 * @description 查询文件列表响应体
 */
@Data
@NoArgsConstructor
public class QueryItemListVO {
    // 文件分类，'all' | 'video' | 'audio' | 'image' | 'docs' | 'others'
    private String category;

    // 页码
    private Integer page;

    // 每页数量
    private Integer pageSize;

    // 父文件夹
    private QueryFileListBO parent;

    // 总数
    private Long total;

    // 搜索关键字，可为null
    private String keyword;

    // 是否按照更新时间排序，null不排序，1升序，0降序
    private Integer orderByUpdateTime;

    // 同上
    private Integer orderBySize;

    // 查询结果
    private List<QueryFileListBO> records;
}
