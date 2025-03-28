package com.hfdy.hfdypan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author hf-dy
 * @date 2025/3/7 17:14
 */
@TableName("share")
@Data
public class Share {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("user_id")
    private String userId;

    /**
     * 有效时长，单位s
     */
    @TableField("expire")
    private Long expire;

    /**
     * 提取码
     */
    @TableField("code")
    private String pwd;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    @TableField("visit_num")
    private Long visitNum;
}
