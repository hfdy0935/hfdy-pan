package com.hfdy.hfdypan.service;

import com.hfdy.hfdypan.domain.dto.user.SendEmailCheckCodeDTO;

/**
 * @author hf-dy
 * @date 2024/10/21 20:04
 */

public interface EmailService {
    /**
     * 发送邮件验证码
     *
     * @param dto
     * @return
     */
    String sendEmailCode(SendEmailCheckCodeDTO dto);
}
