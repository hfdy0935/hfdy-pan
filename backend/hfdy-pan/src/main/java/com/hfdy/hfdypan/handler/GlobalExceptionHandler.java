package com.hfdy.hfdypan.handler;

import com.hfdy.hfdypan.domain.vo.ApiResp;
import com.hfdy.hfdypan.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author hf-dy
 * @date 2024/10/21 20:35
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResp<Void> handleException(BusinessException e) {
        log.error("出现异常：{}", e.getMessage());
        return ApiResp.fail(e.getCode(), e.getMessage());
    }
}
