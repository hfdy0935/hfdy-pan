package com.hfdy.hfdypan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfdy.hfdypan.domain.dto.user.UpdateHomePasswordDTO;
import com.hfdy.hfdypan.domain.dto.user.LoginDTO;
import com.hfdy.hfdypan.domain.dto.user.RegisterDTO;
import com.hfdy.hfdypan.domain.dto.user.UpdatePasswordDTO;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.vo.user.LoginVO;
import com.hfdy.hfdypan.domain.vo.user.RegisterVO;
import com.hfdy.hfdypan.domain.vo.user.UpdateUserInfoVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author hf-dy
 * @date 2024/10/21 20:18
 */

public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param dto
     */
    RegisterVO register(RegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto
     * @return
     */
    LoginVO login(LoginDTO dto);

    /**
     * 修改密码
     *
     * @param dto
     */
    void updatePassword(UpdatePasswordDTO dto);


    /**
     * 更新用户信息，昵称和头像
     *
     * @param nickname
     * @param avatar
     */
    UpdateUserInfoVO updateUserInfo(String nickname, MultipartFile avatar);

    /**
     * 主页修改密码
     *
     * @param dto
     */
    void updateHomePassword(UpdateHomePasswordDTO dto);
}
