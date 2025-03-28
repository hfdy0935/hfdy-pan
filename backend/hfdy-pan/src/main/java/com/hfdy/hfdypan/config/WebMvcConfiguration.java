package com.hfdy.hfdypan.config;

import com.hfdy.hfdypan.interceptor.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author hf-dy
 * @date 2024/10/22 22:30
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Resource
    private JwtInterceptor jwtInterceptor;

    /**
     * 拦截器
     *
     * @param registry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/login", "/api/register", "/api/sendEmailCheckCode",
                        "/api/captcha", "/api/updatePassword", "/api/emailCheckCodeExpires",
                        "/api/resource/**");
    }

    /**
     * 静态资源
     *
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
