package com.hfdy.hfdypan.domain.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hf-dy
 * @date 2024/10/22 09:22
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterVO {
    // 用户id
    private Long userId;
    // 用户昵称
    private String nickName;
    // accessToken
    private String accessToken;
    // refreshToken
    private String refreshToken;
    // 用户头像链接
    private String avatar;
    // 已使用空间大小/B
    private Long usedSpace;
    // 总空间大小/B
    private Long totalSpace;
}