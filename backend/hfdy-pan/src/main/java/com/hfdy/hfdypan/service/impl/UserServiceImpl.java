package com.hfdy.hfdypan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfdy.hfdypan.constants.UserConstants;
import com.hfdy.hfdypan.constants.JwtConstants;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.domain.dto.user.UpdateHomePasswordDTO;
import com.hfdy.hfdypan.domain.dto.user.LoginDTO;
import com.hfdy.hfdypan.domain.dto.user.RegisterDTO;
import com.hfdy.hfdypan.domain.dto.user.UpdatePasswordDTO;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.vo.user.LoginVO;
import com.hfdy.hfdypan.domain.vo.user.RegisterVO;
import com.hfdy.hfdypan.domain.vo.user.UpdateUserInfoVO;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.enums.UserStatusEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.UserService;
import com.hfdy.hfdypan.utils.*;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * @author hf-dy
 * @date 2024/10/21 20:19
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisUtil<Long> longRedisUtil;
    @Resource
    private MinIOUtil minIOUtil;
    @Value("${server.port}")
    private Integer port;

    /**
     * 给avatar添加前缀，变成能访问的url
     *
     * @param filename
     * @return
     */
    private String addPrefixToAvatar(String filename) {
        return "http://localhost:" + port + "/api/resource/" + filename;
    }

    @Override
    public RegisterVO register(RegisterDTO dto) {
        // 1. 创建用户
        User user = User.builder()
                .email(dto.getEmail())
                .password(StringUtil.encodeByMd5(dto.getPassword()))
                .nickName(dto.getNickname())
                .qqOpenId("")
                .avatar(UserConstants.DEFAULT_AVATAR)
                .registerTime(LocalDateTime.now())
                .lastLoginTime(LocalDateTime.now())
                .status(UserStatusEnum.ENABLE.getCode())
                .usedSpace(0L) // 注册时已使用0
                .totalSpace(UserConstants.USER_DEFAULT_SPACE_SIZE)
                .build();
        userMapper.insert(user);
        // 2. 修改redis中用户云盘可用空间缓存为注册时默认空间
        longRedisUtil.set(RedisConstants.USER_REST_SPACE_KEY + ":" + user.getId(), UserConstants.USER_DEFAULT_SPACE_SIZE);
        // 3. 生成token
        Map<String, Object> map = Map.of(JwtConstants.CLAIMS_KEY, user.getId());
        String accessToken = JwtUtil.createAccessToken(map);
        String refreshToken = JwtUtil.createRefreshToken(map);
        // 4. 本地线程设置用户id
        ThreadLocalUtil.setCurrentUserId(user.getId());
        // 5. 返回
        RegisterVO registerVO = new RegisterVO();
        BeanUtils.copyProperties(user, registerVO);
        registerVO.setAccessToken(accessToken);
        registerVO.setRefreshToken(refreshToken);
        // 头像加上host等前缀
        registerVO.setAvatar(addPrefixToAvatar(registerVO.getAvatar()));
        return registerVO;
    }


    /**
     * 校验用户邮箱，判断是否存在
     *
     * @param email
     * @return User
     */
    private User verifyUser(String email) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery().eq(User::getEmail, email);
        User user = getOne(wrapper);
        ThrowUtil.throwIf(user == null, new BusinessException(HttpCodeEnum.USER_NOT_EXISTS));
        return user;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 用户是否存在
        User user = verifyUser(dto.getEmail());
        // 2. 检查用户是否被封禁
        ThrowUtil.throwIf(user.getStatus().equals(UserStatusEnum.DISABLE.getCode()), HttpCodeEnum.USER_FORBIDDEN);
        // 3. 检查密码
        String password = StringUtil.encodeByMd5(dto.getPassword());
        ThrowUtil.throwIf(password == null || !password.equals(user.getPassword()), new BusinessException(HttpCodeEnum.PASSWORD_ERROR));
        // 4. 可以登录，设置token
        Map<String, Object> map = Map.of(JwtConstants.CLAIMS_KEY, user.getId());
        String accessToken = JwtUtil.createAccessToken(map);
        String refreshToken = JwtUtil.createRefreshToken(map);
        // 4. 本地线程设置id
        ThreadLocalUtil.setCurrentUserId(user.getId());
        // 5. 更新数据库中用户最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        // 6. 返回
        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        // 头像加上host等前缀
        loginVO.setAvatar(addPrefixToAvatar(loginVO.getAvatar()));
        return loginVO;
    }


    @Override
    public void updatePassword(UpdatePasswordDTO dto) {
        // 1. 检查用户是否存在
        User user = verifyUser(dto.getEmail());
        // 2. 检查用户是否被封禁
        ThrowUtil.throwIf(user.getStatus().equals(UserStatusEnum.DISABLE.getCode()), HttpCodeEnum.USER_FORBIDDEN);
        // 3. 修改密码
        LambdaUpdateWrapper<User> wrapper = Wrappers.<User>lambdaUpdate()
                .eq(User::getEmail, dto.getEmail())
                .set(User::getPassword, StringUtil.encodeByMd5(dto.getPassword()));
        userMapper.update(wrapper);
    }


    @Override
    public UpdateUserInfoVO updateUserInfo(String nickname, MultipartFile avatar) {
        User user = getById(ThreadLocalUtil.getCurrentUserId());
        // 在Minio中的路径
        String filename = avatar == null ? user.getAvatar() : minIOUtil.uploadFile(avatar);
        // 更新数据库
        LambdaUpdateWrapper<User> userInfoLambdaQueryWrapper = Wrappers.<User>lambdaUpdate().
                eq(User::getId, user.getId())
                .set(User::getNickName, nickname)
                .set(avatar != null, User::getAvatar, filename);
        userMapper.update(userInfoLambdaQueryWrapper);
        return UpdateUserInfoVO.builder().nickname(nickname).avatar(addPrefixToAvatar(filename)).build();
    }

    @Override
    public void updateHomePassword(UpdateHomePasswordDTO dto) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        // 如果旧密码错误
        User user = getById(userId);
        ThrowUtil.throwIf(!Objects.equals(user.getPassword(), StringUtil.encodeByMd5(dto.getPassword())), new BusinessException(HttpCodeEnum.PASSWORD_ERROR, "旧密码错误"));
        LambdaUpdateWrapper<User> userInfoLambdaUpdateWrapper = Wrappers.<User>lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getPassword, StringUtil.encodeByMd5(dto.getNewPassword()));
        userMapper.update(userInfoLambdaUpdateWrapper);
    }
}
