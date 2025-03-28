package com.hfdy.hfdypan.constants;

/**
 * @author hf-dy
 * @date 2024/10/22 09:52
 */

public class RedisConstants {
    /**
     * 用户图形验证码的key前缀
     */
    public static final String CAPTCHA_KEY = "captcha";

    /**
     * 用户邮件验证码的key前缀
     */
    public static final String EMAIL_CHECK_CODE_KEY = "email_check_code";


    /**
     * 用户剩余空间的key
     */
    public static final String USER_REST_SPACE_KEY = "user_rest_space";

    /**
     * 邮件验证码过期时间2min，单位为秒
     */
    public static final Long EMAIL_CHECK_CODE_EXPIRE = 60 * 2L;

    /**
     * redis中图片验证码过期时间，单位秒
     */
    public static final Long CAPTCHA_EXPIRE = 60L;

    /**
     * 上传文件分块在redis中的key
     */
    public static final String UPLOAD_FILE_CHUNK_KEY = "file_chunk";
    /**
     * 已上传文件的key，用于秒传
     */
    public static final String UPLOAD_FILE_KEY = "uploaded_file";
    /**
     * 转码状态的key
     */
    public static final String STATUS_KEY = "trans_code_status";
    /**
     * 转码状态有效期，60s
     */
    public static final Long STATUS_EXPIRE = 60L;
    /**
     * 已分享文件id的key
     */
    public static final String SHARED_FILE_KEY = "shared_file_key";
}
