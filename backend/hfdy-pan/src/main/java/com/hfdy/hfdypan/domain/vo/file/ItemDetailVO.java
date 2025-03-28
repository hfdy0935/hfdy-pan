package com.hfdy.hfdypan.domain.vo.file;

/**
 * @author hf-dy
 * @date 2025/1/19 23:44
 * @description 获取文件详情
 */

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hfdy.hfdypan.domain.enums.file.FileStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemDetailVO {
    private String id;
    private String name;
    /**
     * 所属用户的用户名
     */
    private String username;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;
    /**
     * 子文件/文件夹数量，文件对应null
     */
    private Long childNum;
    // 大小，单位：B
    private Long size;
    private String pid;
    // 分类
    private String category;
    // 具体类型
    private String mediaType;
    // 状态
    private Integer status;
    // 歌词
    private String lyricPath;
}
