package com.hfdy.hfdypan.utils;

import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.exception.BusinessException;

/**
 * @author hf-dy
 * @date 2024/10/21 20:56
 */

public class ThrowUtil {

    public static void throwIf(boolean condition, HttpCodeEnum httpCodeEnum) {
        if (condition) {
            throw new BusinessException(httpCodeEnum);
        }
    }

    public static <T extends RuntimeException> void throwIf(boolean condition, T e) {
        if (condition) {
            throw e;
        }
    }

}
