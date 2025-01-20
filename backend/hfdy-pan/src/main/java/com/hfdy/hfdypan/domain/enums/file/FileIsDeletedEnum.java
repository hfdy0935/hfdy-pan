package com.hfdy.hfdypan.domain.enums.file;

import lombok.Getter;

/**
 * @author hf-dy
 * @date 2025/1/17 23:57
 * @description 文件是否被删除
 */

@Getter
public enum FileIsDeletedEnum {
    YES(1),
    NO(0);

    private final Integer code;

    FileIsDeletedEnum(Integer code) {
        this.code = code;
    }
}
