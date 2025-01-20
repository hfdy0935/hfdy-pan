package com.hfdy.hfdypan.domain.enums;

import lombok.Data;
import lombok.Getter;

/**
 * @author hf-dy
 * @date 2024/10/21 21:44
 */

@Getter
public enum VerifyRegexpEnum {
    NO("", "不校验"),

    IP("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5]))", "IP地址"),

    POSITIVE_INTEGER("^[1-9]\\d*$", "正整数"),

    NUMBER_LETTER_UNDER_LIVE("^\\w+$", "由数字、26个英文字母或者下划线组成的字符串"),

    EMAIL("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$", "邮箱"),

    COMMON("^[\\w\\u4e00-\\u9fa5]+$", "数字、字母、中文、下划线"),

    PASSWORD("^[\\da-zA-Z-_@#]{6,18}$", "只能是数字、字母、特殊字符、6-18位"),

    ACCOUNT("^[a-zA-Z][\\w]*$", "字母开头且由数字、英文字母或者下划线组成"),

    MONEY("^[0-9]+(\\.[0-9]{1,2})?$", "金额");

    private String regex;
    private String desc;

    VerifyRegexpEnum(String regex, String desc) {
        this.regex = regex;
        this.desc = desc;
    }

}
