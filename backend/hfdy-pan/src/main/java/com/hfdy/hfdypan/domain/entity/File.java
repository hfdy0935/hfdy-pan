package com.hfdy.hfdypan.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author hf-dy
 * @date 2025/1/17 10:48
 */
@Data
@Builder
@TableName("file")
public class File {

    @TableField("id")
    private String id;

    @TableField("user_id")
    private String userId;

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

    @TableField("category")
    private String category;

    @TableField("media_type")
    private String mediaType;

    @TableField("status")
    private Integer status;

    @TableLogic
    private Integer isDeleted;

}
