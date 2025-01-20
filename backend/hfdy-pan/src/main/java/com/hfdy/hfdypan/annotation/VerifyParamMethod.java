package com.hfdy.hfdypan.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * @author hf-dy
 * @date 2024/10/21 21:37
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface VerifyParamMethod {

    /**
     * 默认不校验参数
     *
     * @return
     */
    boolean checkParams() default false;
}
