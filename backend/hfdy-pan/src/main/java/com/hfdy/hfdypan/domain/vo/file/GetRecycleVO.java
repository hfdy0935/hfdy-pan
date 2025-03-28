package com.hfdy.hfdypan.domain.vo.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author hf-dy
 * @date 2025/3/10 23:58
 */
@Data
public class GetRecycleVO {
    private String id;
    private String name;
    private String category;
    private String mediaType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;
    private String level;
    private String pid;
}
