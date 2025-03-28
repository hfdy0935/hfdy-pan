package com.hfdy.hfdypan.domain.enums;

import lombok.Getter;

/**
 * @author hf-dy
 * @date 2024/10/21 17:34
 */
@Getter
public enum HttpCodeEnum {
    SUCCESS(200, "操作成功"),
    UN_AUTHORIZATION(401, "没有权限"),
    USER_FORBIDDEN(402, "用户被封禁"), //
    ERROR(600, "操作失败"),
    CAPTCHA_ERROR(601, "图形验证码不正确或已过期"), //
    CAPTCHA_WAITING(602, "邮箱验证码还在冷却中"), //
    EMAIL_EXISTED(604, "邮箱已经存在"), // ok
    SEND_EMAIL_FAIL(604, "邮件发送失败"),
    AOP_VALIDATE_FAIL(605, "AOP参数校验失败(退出并重新登录即可)"),
    AOP_VALIDATE_TYPE_FAIL(606, "AOP参数校验的类型错误"),
    AOP_VALIDATE_EMPTY_FAIL(607, "AOP参数校验的参数为空"),
    AOP_VALIDATE_LENGTH_FAIL(608, "AOP参数校验的长度错误"),
    AOP_VALIDATE_REGEX_FAIL(609, "AOP参数校验的正则匹配错误"),
    EMAIL_CODE_WRONG(610, "邮箱验证码不正确或已过期"),
    USER_NOT_EXISTS(611, "用户不存在"), //
    PASSWORD_ERROR(612, "密码错误"), //
    USER_EXISTS(614, "用户已存在"),
    NICKNAME_EMPTY(615, "昵称不能为空"), //
    UPLOAD_FAIL(616, "上传失败"),
    RESOURCE_ERROR(617, "获取资源失败"), //
    FILE_CATEGORY_WRONG(618, "文件类型错误"), //
    FOLDER_CREATE_FAIL(619, "文件夹创建失败"), //
    FILE_NOT_EXISTS(620, "文件不存在"), //
    FILE_OWNER_ERROR(621, "该文件不属于你"), //
    FILE_RENAME_ERROR(622, "重命名失败"),//
    FILE_DELETE_ERROR(623, "删除失败"),//
    FILE_MOVE_ERROR(624, "移动失败"),
    FILE_COPY_ERROR(625, "复制失败"),
    FILE_UPLOAD_ERROR(626, "文件上传失败"),
    FILE_GET_ERROR(627, "文件获取失败"),
    SPACE_NOT_ENOUGH(628, "空间不足"),
    UPLOAD_SPEED_LIMIT(629, "上传太频繁，请稍后再试或开通会员"),
    FILE_TRANS_ERROR(630, "文件转码错误"),
    FILE_SHARE_ERROR(631, "分享失败"),
    FILE_GET_SHARE_ERROR(632, "获取分享文件失败"),
    FILE_SHARE_NOT_EXISTS(633, "分享不存在"),
    FILE_SAVE_ERROR(634, "保存失败"),
    FILE_MAX_LEVEL_LIMIT(645, "文件层级达到最大深度"),
    FILE_RECOVER_ERROR(646, "恢复失败"),
    USER_DELETE_ERROR(647, "删除失败");


    private final int code;
    private final String message;

    HttpCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }


}
