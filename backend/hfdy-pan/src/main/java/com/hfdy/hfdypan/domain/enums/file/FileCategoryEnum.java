package com.hfdy.hfdypan.domain.enums.file;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hf-dy
 * @date 2025/1/17 11:01
 * @description 文件分类枚举，和前端二级菜单对应
 */
public enum FileCategoryEnum {
    ALL("all", "所有分类"),
    VIDEO("video", "视频"),
    AUDIO("audio", "音频"),
    IMAGE("image", "图片"),
    DOCS("docs", "文档"),
    OTHERS("others", "其他");

    @Getter
    private final String category;
    private final String description;

    FileCategoryEnum(String category, String description) {
        this.category = category;
        this.description = description;
    }


    /**
     * 存k v，快速查询
     */
    private static final Map<String, FileCategoryEnum> lookup = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(el -> lookup.put(el.category, el));
    }

    /**
     * 根据字符串获取枚举值
     *
     * @param c
     * @return 找到的结果或null
     */
    public static FileCategoryEnum getByStr(String c) {
        if (c == null || c.isEmpty()) return null;
        return FileCategoryEnum.lookup.get(c);
    }
}
