package com.hfdy.hfdypan.domain.vo.user;

import lombok.Builder;
import lombok.Data;

/**
 * @author hf-dy
 * @date 2024/11/19 11:45
 */
@Data
@Builder
public class UpdateUserInfoVO {
    private String nickname;
    private String avatar;
}
