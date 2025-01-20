package com.hfdy.hfdypan.domain.dto.user;

import com.hfdy.hfdypan.annotation.VerifyParam;
import com.hfdy.hfdypan.domain.enums.VerifyRegexpEnum;
import lombok.Data;

/**
 * @author hf-dy
 * @date 2024/11/19 12:17
 */
@Data
public class UpdateHomePasswordDTO {
    /**
     * 旧密码
     */
    @VerifyParam(required = true, regex = VerifyRegexpEnum.PASSWORD)
    private String password;

    /**
     * 新密码
     */
    @VerifyParam(required = true, regex = VerifyRegexpEnum.PASSWORD)
    private String newPassword;
}
