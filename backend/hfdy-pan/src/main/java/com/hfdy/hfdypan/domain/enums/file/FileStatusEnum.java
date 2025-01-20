package com.hfdy.hfdypan.domain.enums.file;

import lombok.Getter;

/**
 * @author hf-dy
 * @date 2025/1/17 23:57
 * @description 文件状态
 */

@Getter
public enum FileStatusEnum {
    ING(0), // 正在转码
    FAIL(1), //转码失败
    OK(2); //转码成功

    private final Integer status;

    FileStatusEnum(Integer status) {
        this.status = status;
    }
}
