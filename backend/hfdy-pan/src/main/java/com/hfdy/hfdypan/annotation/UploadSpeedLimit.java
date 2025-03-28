package com.hfdy.hfdypan.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hf-dy
 * @date 2025/3/4 07:46
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UploadSpeedLimit {
    /**
     * 速度，默认-1，校验时根据当前用户的身份限制
     *
     * @return
     */
    int value() default -1;
}
