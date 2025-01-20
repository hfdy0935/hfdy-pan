package com.hfdy.hfdypan.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hf-dy
 * @date 2024/10/22 08:15
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailCheckCodeDTO implements Serializable {
    // 邮箱
    private String email;
    // 类型 0注册、1修改密码
    private Integer type;
}
