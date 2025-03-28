package com.hfdy.hfdypan.interceptor;

import com.hfdy.hfdypan.constants.JwtConstants;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.enums.UserStatusEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.utils.JwtUtil;
import com.hfdy.hfdypan.utils.StringUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
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
    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 判断要拦截的是controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) return true;
        // 如果是从获取分享页面来的，放行
        if (StringUtil.isFromPublicShareFile(request)) return true;
        // 校验token
        String token = request.getHeader(JwtConstants.TOKEN_FIELD_IN_HEADER);
        if (StringUtil.isEmpty(token)) throw new BusinessException(HttpCodeEnum.UN_AUTHORIZATION);
        StringBuffer url = request.getRequestURL();
        Claims claims;
        boolean isRefreshToken = url.toString().contains("/api/refreshToken");
        // 刷新token
        try {
            if (isRefreshToken) {
                claims = JwtUtil.parseRefreshToken(token);
            } else {
                claims = JwtUtil.parseAccessToken(token);
            }
        } catch (Exception e) {
            throw new BusinessException(HttpCodeEnum.UN_AUTHORIZATION);
        }
        String userId = claims.get(JwtConstants.CLAIMS_KEY).toString();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpCodeEnum.USER_NOT_EXISTS);
        }
        if (user.getStatus().equals(UserStatusEnum.DISABLE.getCode())) {
            throw new BusinessException(HttpCodeEnum.USER_FORBIDDEN);
        }
        ThreadLocalUtil.setCurrentUserId(userId);
        if (isRefreshToken) {
            // 生成新access token
            String accessToken = JwtUtil.createAccessToken(Map.of(JwtConstants.CLAIMS_KEY, userId));
            response.setHeader(JwtConstants.TOKEN_FIELD_IN_HEADER, accessToken);
            ThreadLocalUtil.setCurrentUserId(userId);
        }
        return true;
    }
}
