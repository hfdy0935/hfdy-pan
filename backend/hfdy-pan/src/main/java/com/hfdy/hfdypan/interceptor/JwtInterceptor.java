package com.hfdy.hfdypan.interceptor;

import com.hfdy.hfdypan.constants.JwtConstants;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.utils.JwtUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * @author hf-dy
 * @date 2024/10/22 22:19
 */

@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        // 判断要拦截的是controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) return true;
        // 获取token
        String token = request.getHeader(JwtConstants.TOKEN_FIELD_IN_HEADER);
        // 校验令牌
        StringBuffer url = request.getRequestURL();
        System.out.println(url);
        try {
            // 如果是刷新token
            Claims claims;
            if (url.toString().contains("/api/refreshToken")) {
                claims = JwtUtil.parseRefreshToken(token);
                String userId = claims.get(JwtConstants.CLAIMS_KEY).toString();
                // 生成新access token
                String accessToken = JwtUtil.createAccessToken(Map.of(JwtConstants.CLAIMS_KEY, userId));
                response.setHeader(JwtConstants.TOKEN_FIELD_IN_HEADER, accessToken);
                ThreadLocalUtil.setCurrentUserId(userId);
            } else {
                // 其他请求
                claims = JwtUtil.parseAccessToken(token);
                String userId = claims.get(JwtConstants.CLAIMS_KEY).toString();
                ThreadLocalUtil.setCurrentUserId(userId);
            }
            return true;
        } catch (Exception e) {
            log.error("异常：{}", e.getMessage());
            throw new BusinessException(HttpCodeEnum.UN_AUTHORIZATION);
        }
    }
}
