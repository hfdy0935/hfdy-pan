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
    public static final Long EMAIL_EXP_TIME = 60 * 2L;

    /**
     * redis中图片验证码过期时间，单位秒
     */
    public static final Long CAPTCHA_EXI_TIME = 60L;
}
