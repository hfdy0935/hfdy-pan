package com.hfdy.hfdypan.domain.dto.user;

import com.hfdy.hfdypan.annotation.VerifyParam;
import com.hfdy.hfdypan.domain.enums.VerifyRegexpEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hf-dy
 * @date 2024/10/21 23:32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    // 邮箱
    @VerifyParam(required = true, regex = VerifyRegexpEnum.EMAIL)
    private String email;
    // 密码
    @VerifyParam(required = true, regex = VerifyRegexpEnum.PASSWORD)
    private String password;
    // 验证码
    @VerifyParam(required = true)
    private String captcha;
}
