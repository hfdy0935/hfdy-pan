package com.hfdy.hfdypan.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

/**
 * @author hf-dy
 * @date 2024/10/21 20:31
 */

public class StringUtil {

    /**
     * 生成随机数字
     *
     * @param count
     * @return
     */
    public static String getRandomNumber(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }

    /**
     * 是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (null == str || str.isEmpty() || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else return str.trim().isEmpty();
    }

    /**
     * 用户密码加密
     *
     * @param originalString
     * @return
     */
    public static String encodeByMd5(String originalString) {
        return isEmpty(originalString) ? null : DigestUtils.md5Hex(originalString);
    }

    /**
     * 生成随机uuid
     *
     * @return
     */
    public static String getRandomUUid() {
        return UUID.randomUUID().toString();
    }
}
