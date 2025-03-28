package com.hfdy.hfdypan.domain.enums.file;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hf-dy
 * @date 2025/1/17 11:06
 * @description 文件具体类型枚举
 */

public enum FileMediaTypeEnum {
    // 音频
    MP3("mp3", "mp3"),
    WAV("wav", "wav"),
    FLAC("flac", "flac"),
    AAC("aac", "aac"),
    OGG("ogg", "ogg"),
    WEBM_AUDIO("webm", "webm"),  // 区分音频和视频的 WebM

    // 视频
    AVI("avi", "avi"),
    MP4("mp4", "mp4"),
    MKV("mkv", "mkv"),
    MOV("mov", "mov"),
    WMV("wmv", "wmv"),
    MPEG("mpeg", "mpeg"),
    FLV("flv", "flv"),
    WEBM_VIDEO("webm", "webm"),  // 区分音频和视频的 WebM

    // 图片
    JPG("jpg", "jpg"),
    PNG("png", "png"),
    GIF("gif", "gif"),
    BMP("bmp", "bmp"),
    TIFF("tiff", "tiff"),
    SVG("svg", "svg"),
    WEBP("webp", "webp"),
    // 文件夹
    FOLDER("folder", "文件夹"),
    // 压缩包
    ZIP("zip", "压缩包"),
    // 其他后缀的文件
    UNKNOWN("unknown", "其他未知文件"),
    // 文档
    DOC("doc", "文档"),
    DOCX("docx", "文档"),
    PPT("ppt", "ppt"),
    PPTX("pptx", "pptx"),
    CSV("csv", "csv"),
    XLSX("xlsx", "xlsx"),
    PDF("pdf", "pdf"),
    MD("md", "markdown"),
    TEXT("text", "文本"),
    CODE("code", "代码");

    @Getter
    private final String mediaType;
    private final String description;

    FileMediaTypeEnum(String mediaType, String description) {
        this.mediaType = mediaType;
        this.description = description;
    }

    /**
     * 存k v，快速查询
     */
    private static final Map<String, FileMediaTypeEnum> lookup = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(el -> lookup.put(el.mediaType, el));
    }

    /**
     * 根据字符串获取枚举值
     *
     * @param c
     * @return 找到的结果或null
     */
    public static FileMediaTypeEnum getByStr(String c) {
        if (c == null || c.isEmpty()) return null;
        return lookup.get(c);
    }
}
