package com.hfdy.hfdypan.aspect;

import com.hfdy.hfdypan.annotation.UploadSpeedLimit;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author hf-dy
 * @date 2025/3/4 07:48
 */
@Aspect
@Component
@Slf4j
public class UploadSpeedLimitAspect {
    @Resource
    private UserMapper userMapper;
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * 令牌桶
     */
    private final Map<String, Integer> tokenBucket = new ConcurrentHashMap<>();

    @Pointcut("@annotation(com.hfdy.hfdypan.annotation.UploadSpeedLimit)")
    private void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("进入上传限流切面");
        String userId = ThreadLocalUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(HttpCodeEnum.UN_AUTHORIZATION);
        boolean isVip = user.getIsVip() == 1;
        // vip无限制
        if (isVip) return joinPoint.proceed();
        // 并发数量
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 从签名中获取方法
        Method method = signature.getMethod();
        // 获取方法上的注解
        UploadSpeedLimit anno = method.getAnnotation(UploadSpeedLimit.class);
        int limit = anno.value();
        int bucketCapacity = limit <= 0 ? user.getUploadLimit() : limit;
        log.info("并发数量：{}", bucketCapacity);
        try {
            locks.computeIfAbsent(userId, k -> new ReentrantLock()).lock();
            tokenBucket.putIfAbsent(userId, bucketCapacity);
            // 检查是否有可用令牌，并减少令牌数量
            tokenBucket.compute(userId, (key, value) -> {
                if (value == null || value <= 0) {
                    throw new BusinessException(HttpCodeEnum.UPLOAD_SPEED_LIMIT);
                }
                return value - 1; // 减少一个令牌
            });
            Object result = joinPoint.proceed();
            tokenBucket.compute(userId, (key, value) -> value + 1);
            // 执行原方法
            return result;
        } finally {
            locks.get(userId).unlock();
        }
    }
}
