package com.hfdy.hfdypan.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author hf-dy
 * @date 2024/10/21 20:07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {
    // 用户id
    @TableId("id")
    private String id;

    // 用户昵称
    @TableField("nick_name")
    private String nickName;

    // 邮箱
    @TableField("email")
    private String email;

    // 用户qq id
    @TableField("qq_open_id")
    private String qqOpenId;

    // 头像
    @TableField("avatar")
    private String avatar;

    // 密码
    @TableField("password")
    private String password;

    // 注册时间
    @TableField("register_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;

    // 上次登录时间
    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    // 状态，0禁用，1启用
    @TableField("status")
    private Integer status;

    // 云盘已使用空间大小，单位为byte
    @TableField("used_space")
    private Long usedSpace;

    // 云盘总空间大小，单位为byte
    @TableField("total_space")
    private Long totalSpace;
    
    @TableField("is_vip")
    private Integer isVip;

    @TableField("is_admin")
    private Integer isAdmin;

    @TableField("upload_limit")
    private Integer uploadLimit;

    @TableField("download_speed")
    private long downloadSpeed;

}
