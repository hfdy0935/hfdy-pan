package com.hfdy.hfdypan.domain.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hfdy.hfdypan.constants.FileConstants;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author hf-dy
 * @date 2025/3/16 14:05
 */
@Data
public class UserListVO {
    private String id;
    private String nickName;
    private String email;
    private String avatar;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    private Integer status;
    // 已使用空间大小/B
    private Long usedSpace;
    // 总空间大小/B
    private Long totalSpace;
    // 是不是vip
    private Integer isVip;
    // 同时上传请求数量限制
    private Integer uploadLimit;
    // 下载速度
    private long downloadSpeed;
}
