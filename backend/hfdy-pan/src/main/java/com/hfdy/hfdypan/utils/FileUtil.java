package com.hfdy.hfdypan.utils;

import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.enums.file.FileCategoryEnum;
import com.hfdy.hfdypan.domain.enums.file.FileMediaTypeEnum;

import java.lang.reflect.Field;

/**
 * @author hf-dy
 * @date 2025/3/5 15:39
 */
public class FileUtil {

    public static boolean isVideo(File file) {
        return file.getCategory().equals(FileCategoryEnum.VIDEO.getCategory());
    }

    public static boolean isImage(File file) {
        return file.getCategory().equals(FileCategoryEnum.IMAGE.getCategory());
    }

    public static boolean isAudio(File file) {
        return file.getCategory().equals(FileCategoryEnum.AUDIO.getCategory());
    }

    public static boolean isFolder(File file) {
        return file.getMediaType().equals(FileMediaTypeEnum.FOLDER.getMediaType());
    }

    public static boolean isNotFolder(File file) {
        return !isFolder(file);
    }

    /**
     * l1是否是l2的子文件/文件夹，包括自身
     *
     * @param level1
     * @param level2
     * @return
     */
    public static boolean isChildOrSelf(String level1, String level2) {
        return level1.startsWith(level2);
    }

    /**
     * 根据文件路径获取文件夹路径
     *
     * @param filename
     * @return
     */
    public static String getFolderPath(String filename) {
        int index = filename.lastIndexOf("/");
        return index == -1 ? "" : filename.substring(0, index);
    }

    /**
     * 根据文件路径获取文件名
     *
     * @param filename
     * @return
     */
    public static String getFileName(String filename) {
        int index = filename.lastIndexOf("/");
        return index == -1 ? filename : filename.substring(index + 1);
    }

    /**
     * 获取和m3u8同目录下的原始视频路径
     *
     * @param file
     * @return
     */
    public static String getRawVideoPath(File file) {
        return getFolderPath(file.getPath()) + "/" + file.getName();
    }

    /**
     * 获取层级
     *
     * @param file
     * @return
     */
    public static Integer getLevelNum(File file) {
        return file.getLevel().split("/").length;
    }

    /**
     * 生成文件用户+内容id
     *
     * @param md5
     * @return
     */
    public static String generateContentId(String md5) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        return StringUtil.encodeByMd5(userId + md5);
    }

    /**
     * 非主线程时，手动传用户id
     *
     * @param userId
     * @param md5
     * @return
     */
    public static String generateContentId(String userId, String md5) {
        return StringUtil.encodeByMd5(userId + md5);
    }
}
