package com.hfdy.hfdypan.utils;

import com.hfdy.hfdypan.domain.enums.VerifyRegexpEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hf-dy
 * @date 2024/10/21 22:13
 */

public class VerifyUtil {
    public static boolean verify(String regex, String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static boolean verify(VerifyRegexpEnum regex, String value) {
        return verify(regex.getRegex(), value);
    }


    private static final Map<String, Boolean> fileCategoryMap = new HashMap<>();

    static {
        List.of("all", "video", "audio", "image", "docs", "others").forEach(c -> {
            fileCategoryMap.put(c, true);
        });
    }

    /**
     * 验证文件类型
     *
     * @param c
     * @return
     */
    public static boolean verifyFileCategory(String c) {
        Boolean res = VerifyUtil.fileCategoryMap.get(c);
        return res != null;
    }
}
