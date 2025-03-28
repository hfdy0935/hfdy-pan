package com.hfdy.hfdypan.controller;


import com.hfdy.hfdypan.annotation.VerifyParam;
import com.hfdy.hfdypan.annotation.VerifyParamMethod;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.domain.dto.user.*;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.vo.ApiResp;
import com.hfdy.hfdypan.domain.vo.user.LoginVO;
import com.hfdy.hfdypan.domain.vo.user.RegisterVO;
import com.hfdy.hfdypan.domain.vo.user.UpdateUserInfoVO;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.vo.user.UserInfoVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.EmailService;
import com.hfdy.hfdypan.service.ResourceService;
import com.hfdy.hfdypan.service.UserService;
import com.hfdy.hfdypan.utils.CaptchaUtil;
import com.hfdy.hfdypan.utils.RedisUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import com.hfdy.hfdypan.utils.ThrowUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author hf-dy
 * @date 2024/10/21 16:32
 */

@RestController
@RequestMapping("/api")
@Slf4j
public class UserController {
    @Resource
    private EmailService emailService;
    @Resource
    private UserService userService;
    @Resource
    private RedisUtil<String> strRedisUtil;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ResourceService resourceService;

    /**
     * 获取邮箱验证码过期时间
     *
     * @return
     */
    @GetMapping("/emailCheckCodeExpires")
    public ApiResp<Long> getEmailCheckCodeExpire(HttpSession session) {
        String key = RedisConstants.EMAIL_CHECK_CODE_KEY + ":" + session.getId();
        Long expires = strRedisUtil.getExpire(key);
        // 没查到返回-1
        return ApiResp.success(expires == null ? -1 : expires);
    }

    /**
     * 验证邮箱验证码
     *
     * @param key
     * @param code
     */
    private void verifyEmailCheckCode(String key, String code) {
        String emailCacheCheckCode = strRedisUtil.get(key);
        ThrowUtil.throwIf(emailCacheCheckCode == null || !emailCacheCheckCode.equals(code), new BusinessException(HttpCodeEnum.EMAIL_CODE_WRONG));
    }

    /**
     * 验证图形验证码
     *
     * @param key
     * @param code
     */
    private void verifyCaptcha(String key, String code) {
        String cacheCaptcha = strRedisUtil.get(key);
        ThrowUtil.throwIf(cacheCaptcha == null || !cacheCaptcha.equals(code), new BusinessException(HttpCodeEnum.CAPTCHA_ERROR));
    }


    /**
     * 发送邮件验证码
     *
     * @param session
     */
    @PostMapping("/sendEmailCheckCode")
    public ApiResp<Void> sendEmailCheckCode(HttpSession session, @RequestBody SendEmailCheckCodeDTO dto) {
        String key = RedisConstants.EMAIL_CHECK_CODE_KEY + ":" + session.getId();
        Long expires = strRedisUtil.getExpire(key);
        ThrowUtil.throwIf(expires > 0, HttpCodeEnum.CAPTCHA_WAITING);
        log.info("发送验证码：{}", session.getId());
        String code = emailService.sendEmailCode(dto);
        strRedisUtil.set(key, code, RedisConstants.EMAIL_CHECK_CODE_EXPIRE);
        return ApiResp.success();
    }

    /**
     * 获取图形验证码
     *
     * @param response
     * @param session
     * @throws IOException
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
        CaptchaUtil captcha = new CaptchaUtil(130, 38, 4, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        response.setHeader("Access-Control-Allow-Origin", "*");
        String code = captcha.getCode();
        // 设置缓存
        String key = RedisConstants.CAPTCHA_KEY + ":" + session.getId();
        strRedisUtil.set(key, code, RedisConstants.CAPTCHA_EXPIRE);
        captcha.write(response.getOutputStream());
    }


    /**
     * 注册
     *
     * @param session
     * @param dto
     * @return
     */
    @PostMapping("/register")
    @VerifyParamMethod(checkParams = true)
    public ApiResp<RegisterVO> register(HttpSession session, @RequestBody @VerifyParam(required = true) RegisterDTO dto) {
        log.info("注册：{}", session.getId());
        // 验证邮箱验证码
        String emailKey = RedisConstants.EMAIL_CHECK_CODE_KEY + ":" + session.getId();
        verifyEmailCheckCode(emailKey, dto.getEmailCheckCode());
        // 验证图形验证码
        String captchaKey = RedisConstants.CAPTCHA_KEY + ":" + session.getId();
        verifyCaptcha(captchaKey, dto.getCaptcha());
        // 注册
        RegisterVO registerVO = userService.register(dto);
        // 删除缓存
        strRedisUtil.delete(emailKey, captchaKey);
        return ApiResp.success(registerVO);
    }


    /**
     * 登录
     *
     * @param session
     * @param dto
     * @return
     */
    @PostMapping("/login")
    @VerifyParamMethod(checkParams = true)
    public ApiResp<LoginVO> login(HttpSession session, @RequestBody @VerifyParam(required = true) LoginDTO dto
    ) {
        // 验证图形验证码
        String key = RedisConstants.CAPTCHA_KEY + ":" + session.getId();
        verifyCaptcha(key, dto.getCaptcha());
        // 登录
        return ApiResp.success(userService.login(dto));
    }


    @PutMapping("/updatePassword")
    @VerifyParamMethod(checkParams = true)
    public ApiResp<Void> updatePassword(HttpSession session, @RequestBody @VerifyParam(required = true) UpdatePasswordDTO dto) {
        // 检查验证码
        String emailKey = RedisConstants.EMAIL_CHECK_CODE_KEY + ":" + session.getId();
        String captchaKey = RedisConstants.CAPTCHA_KEY + ":" + session.getId();
        verifyEmailCheckCode(emailKey, dto.getEmailCheckCode());
        verifyCaptcha(captchaKey, dto.getCaptcha());
        // 修改密码
        userService.updatePassword(dto);
        // 清除缓存
        strRedisUtil.delete(emailKey, captchaKey);
        return ApiResp.success();
    }

    /**
     * 使用refreshToken更新accessToken
     *
     * @return
     */
    @PostMapping("refreshToken")
    public ApiResp<Void> refreshToken() {
        return ApiResp.success();
    }

    /**
     * 修改用户信息
     *
     * @return
     */
    @PostMapping("/updateUserInfo")
    @VerifyParamMethod(checkParams = true)
    public ApiResp<UpdateUserInfoVO> updateUserInfo(@RequestParam("nickname") @VerifyParam(required = true) String nickname, MultipartFile avatar) {
        return ApiResp.success(userService.updateUserInfo(nickname, avatar));
    }

    /**
     * 主页更新密码
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateHomePassword")
    @VerifyParamMethod(checkParams = true)
    public ApiResp<Void> updateHomePassword(@RequestBody @VerifyParam(required = true) UpdateHomePasswordDTO dto) {
        userService.updateHomePassword(dto);
        return ApiResp.success();
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @GetMapping("/userInfo")
    public ApiResp<UserInfoVO> getUserInfo() {
        String userId = ThreadLocalUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        String avatarPath = user.getAvatar().isEmpty() ? "" : resourceService.addPrefix(user.getAvatar());
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        userInfoVO.setUserId(userId);
        userInfoVO.setAvatar(avatarPath);
        return ApiResp.success(userInfoVO);
    }
}
