package com.hfdy.hfdypan.service.impl;

import com.hfdy.hfdypan.config.EmailConfig;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.constants.UserConstants;
import com.hfdy.hfdypan.domain.dto.user.SendEmailCheckCodeDTO;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.EmailService;
import com.hfdy.hfdypan.utils.RedisUtil;
import com.hfdy.hfdypan.utils.StringUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import com.hfdy.hfdypan.utils.ThrowUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author hf-dy
 * @date 2024/10/21 20:05
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private JavaMailSender mailSender;
    @Resource
    private RedisUtil<String> redisUtil;
    @Resource
    private EmailConfig emailConfig;


    @Override
    @Transactional
    public String sendEmailCode(SendEmailCheckCodeDTO dto) {
        // 0表示注册
        if (dto.getType() == 0) {
            User user = userMapper.selectById(ThreadLocalUtil.getCurrentUserId());
            // 用户已存在
            ThrowUtil.throwIf(user != null, new BusinessException(HttpCodeEnum.USER_EXISTS));
        }
        // 生成验证码
        String code = StringUtil.getRandomNumber(UserConstants.EMAIL_CHECK_CODE_LENGTH);
        // 发送
        String title = dto.getType() == 0 ? UserConstants.REGISTER_EMAIL_TITLE : UserConstants.UPDATE_PASSWORD_EMAIL_TITLE;
        sendEmailCode(dto.getEmail(), code, title);
        return code;
    }

    /**
     * 发哦是那个邮件
     *
     * @param to    target的邮箱
     * @param code  验证码
     * @param title 标题
     */
    private void sendEmailCode(String to, String code, String title) {
        var message = new SimpleMailMessage();
        // 发送者
        message.setFrom(emailConfig.getUsername());
        // 接收者
        message.setTo(to);
        // 标题
        message.setSubject(title);
        // 内容
        String content = "验证码是 " + code + "，" + (int) (RedisConstants.EMAIL_CHECK_CODE_EXPIRE / 60) + "分钟内有效，打死也不要告诉别人哦 ";
        message.setText(content);
        // 发送时间
        message.setSentDate(new Date());
        // 发送
        mailSender.send(message);
    }
}
