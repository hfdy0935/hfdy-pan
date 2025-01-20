package com.hfdy.hfdypan.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hf-dy
 * @date 2024/10/21 17:36
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApiResp<T> implements Serializable {
    private static final Long serialVersionUID = 73295878128471L;

    private final Integer code;
    private String message;
    private T data;

    public ApiResp(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public ApiResp(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ApiResp(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ApiResp<Void> fail(Integer code, String message) {
        return new ApiResp<>(code, message);
    }

    public static ApiResp<Void> fail(HttpCodeEnum httpCodeEnum) {
        return ApiResp.fail(httpCodeEnum.getCode(), httpCodeEnum.getMessage());
    }

    public static ApiResp<Void> success() {
        return ApiResp.success(HttpCodeEnum.SUCCESS);
    }

    public static <U> ApiResp<U> success(Integer code, String message, U data) {
        return new ApiResp<>(code, message, data);
    }

    public static <U> ApiResp<U> success(HttpCodeEnum httpCodeEnum, U data) {
        return ApiResp.success(httpCodeEnum.getCode(), httpCodeEnum.getMessage(), data);
    }

    public static <U> ApiResp<U> success(Integer code, U data) {
        return ApiResp.success(code, "", data);
    }

    public static ApiResp<Void> success(Integer code, String message) {
        return ApiResp.success(code, message, null);
    }

    public static ApiResp<Void> success(HttpCodeEnum httpCodeEnum) {
        return ApiResp.success(httpCodeEnum.getCode(), httpCodeEnum.getMessage());
    }

    public static <U> ApiResp<U> success(U data) {
        return ApiResp.success(HttpCodeEnum.SUCCESS, data);
    }


}
