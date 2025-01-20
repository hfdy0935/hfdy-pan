package com.hfdy.hfdypan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hfdy.hfdypan.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hf-dy
 * @date 2024/10/21 20:18
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
