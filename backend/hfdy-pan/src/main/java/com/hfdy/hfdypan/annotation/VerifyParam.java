package com.hfdy.hfdypan.annotation;

import com.hfdy.hfdypan.domain.enums.VerifyRegexpEnum;
import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * @author hf-dy
 * @date 2024/10/21 21:47
 */

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface VerifyParam {

    /**
     * 最小值
     *
     * @return
     */
    int min() default -1;

    /**
     * 最大值
     *
     * @return
     */
    int max() default -1;

    /**
     * 是否必需
     *
     * @return
     */
    boolean required() default false;

    /**
     * 要按照哪个正则表达式校验
     *
     * @return
     */
    VerifyRegexpEnum regex() default VerifyRegexpEnum.NO;
}
