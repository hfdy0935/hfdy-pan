package com.hfdy.hfdypan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author hf-dy
 * @date 2024/10/21 20:44
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class EmailConfig {

    /**
     * host
     */
    private String host;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}