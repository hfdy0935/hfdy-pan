package com.hfdy.hfdypan.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hfdy.hfdypan.domain.enums.file.FileStatusEnum;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author hf-dy
 * @date 2025/1/17 10:48
 */
@Data
@Builder
@TableName("file")
public class File {

    @TableId("id")
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("level")
    private String level;

    @TableField("md5")
    private String md5;

    @TableField("name")
    private String name;

    @TableField("path")
    private String path;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("delete_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;

    @TableField("size")
    private Long size;

    @TableField("pid")
    private String pid;

    /**
     * 文件/文件夹分类
     */
    @TableField("category")
    private String category;

    /**
     * 具体类型
     */
    @TableField("media_type")
    private String mediaType;

    @TableLogic
    private Integer isDeleted;

    @TableField("status")
    private Integer status;

    @TableField("lyric_path")
    private String lyricPath;

}
