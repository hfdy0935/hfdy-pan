package com.hfdy.hfdypan.domain.vo.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/10 13:44
 */
@Data
public class GetMyShareVO {
    private String id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Long visitNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expire;
    private Integer itemNum;
    private String pwd;
    /**
     * 该分享包含的文件id列表
     */
    private List<String> fileIds;
}
