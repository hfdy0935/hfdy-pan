package com.hfdy.hfdypan.constants;

/**
 * @author hf-dy
 * @date 2024/10/21 16:26
 */

public class UserConstants {
    /**
     * 注册账号邮件标题
     */
    public static final String REGISTER_EMAIL_TITLE = "终于等到你~欢迎注册hfdy云盘";

    /**
     * 修改密码邮件标题
     */
    public static final String UPDATE_PASSWORD_EMAIL_TITLE = "修改密码";

    /**
     * 生成的邮箱验证码长度
     */
    public static final Integer EMAIL_CHECK_CODE_LENGTH = 6;


    /**
     * 用户空间的初始大小/B，默认1G
     */
    public static final Long USER_DEFAULT_SPACE_SIZE = 1024 * 1024 * 1024L;


    /**
     * 默认头像路径
     */
    public static final String DEFAULT_AVATAR = "/api/static/avatar.png";
}
