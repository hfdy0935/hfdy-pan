package com.hfdy.hfdypan.domain.vo.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hfdy.hfdypan.domain.bo.QueryFileListBO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/7 17:26
 */
@Data
public class GetShareFileVO {
    /**
     * 分享记录id
     */
    private String id;
    private String username;
    private String avatar;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private QueryFileListBO parent;
    /**
     * 分享记录
     */
    private List<QueryFileListBO> records;
    /**
     * 围观次数
     */
    private Long visitNum;
}
