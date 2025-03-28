package com.hfdy.hfdypan.exception;

import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author hf-dy
 * @date 2024/10/21 20:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;

    public BusinessException(HttpCodeEnum httpCodeEnum) {
        super(httpCodeEnum.getMessage());
        this.code = httpCodeEnum.getCode();
        this.message = httpCodeEnum.getMessage();
    }

    public BusinessException(HttpCodeEnum httpCodeEnum, String message) {
        super(message);
        this.code = httpCodeEnum.getCode();
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
