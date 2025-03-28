package com.hfdy.hfdypan.domain.enums.file;

import lombok.Getter;

/**
 * @author hf-dy
 * @date 2025/3/5 10:20
 */
@Getter
public enum FileStatusEnum {
    // 无需转码
    NO_NEED_TRANS(0),
    // 转码中
    TRANS_ING(1),
    // 转码成功
    TRANS_OK(2),
    // 转码失败
    TRANS_FAIL(3);

    public final Integer value;

    FileStatusEnum(Integer value) {
        this.value = value;
    }
}
