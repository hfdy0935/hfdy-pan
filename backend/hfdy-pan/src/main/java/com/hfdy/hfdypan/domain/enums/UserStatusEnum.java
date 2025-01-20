package com.hfdy.hfdypan.domain.enums;

import lombok.Getter;

/**
 * @author hf-dy
 * @date 2024/10/22 07:39
 */

@Getter
public enum UserStatusEnum {
    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    private final Integer code;
    private final String desc;

    UserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据整数（0或1）获取对应的状态
     *
     * @param status Integer
     * @return AccountStatusEnum
     */
    public static UserStatusEnum fromInt(Integer status) {
        for (UserStatusEnum item : UserStatusEnum.values()) {
            if (item.getCode().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
