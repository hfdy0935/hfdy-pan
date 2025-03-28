package com.hfdy.hfdypan.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hfdy.hfdypan.annotation.NeedAdmin;
import com.hfdy.hfdypan.domain.dto.user.UpdateUserDTO;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.vo.ApiResp;
import com.hfdy.hfdypan.domain.vo.user.UserListVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/16 14:09
 */
@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {
    @Resource
    private UserMapper userMapper;


    @GetMapping("/users")
    @NeedAdmin
    public ApiResp<List<UserListVO>> getUserList() {
        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery()
                .eq(User::getIsAdmin, 0));
        List<UserListVO> userListVOs = new ArrayList<>();
        if (users.isEmpty()) return ApiResp.success(userListVOs);
        users.forEach(user -> {
            UserListVO userListVO = new UserListVO();
            BeanUtils.copyProperties(user, userListVO);
            userListVOs.add(userListVO);
        });
        return ApiResp.success(userListVOs);
    }

    @DeleteMapping("/users")
    @NeedAdmin
    public ApiResp<Void> deleteUser(@RequestParam("id") @NotEmpty String id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(HttpCodeEnum.USER_NOT_EXISTS);
        if (user.getIsAdmin() == 1)
            throw new BusinessException(HttpCodeEnum.USER_DELETE_ERROR, "删除失败，不能删除管理员");
        userMapper.deleteById(id);
        return ApiResp.success();
    }

    @PutMapping("/users")
    @NeedAdmin
    public ApiResp<Void> updateUser(@RequestBody UpdateUserDTO dto) {
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.<User>lambdaUpdate()
                .eq(User::getId, dto.getUserId())
                .set(User::getStatus, dto.getStatus())
                .set(User::getIsVip, dto.getIsVip())
                .set(User::getUploadLimit, dto.getUploadLimit())
                .set(User::getDownloadSpeed, dto.getDownloadSpeed());
        int rows = userMapper.update(updateWrapper);
        if (rows == 0) throw new BusinessException(HttpCodeEnum.USER_NOT_EXISTS, "修改失败");
        return ApiResp.success();
    }
}
