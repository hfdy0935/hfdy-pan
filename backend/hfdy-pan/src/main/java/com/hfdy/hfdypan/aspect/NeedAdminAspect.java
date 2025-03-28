package com.hfdy.hfdypan.aspect;

import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author hf-dy
 * @date 2025/3/16 14:10
 */
@Aspect
@Component
public class NeedAdminAspect {

    @Resource
    private UserMapper userMapper;


    @Pointcut("@annotation(com.hfdy.hfdypan.annotation.NeedAdmin)")
    private void pointcut() {
    }


    @Before("pointcut()")
    public void before() {
        String userId = ThreadLocalUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsAdmin() == 0) {
            throw new BusinessException(HttpCodeEnum.UN_AUTHORIZATION);
        }
    }
}
