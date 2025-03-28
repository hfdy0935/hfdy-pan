package com.hfdy.hfdypan.domain.vo.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.hfdy.hfdypan.constants.FileConstants;
import lombok.Builder;
import lombok.Data;

/**
 * @author hf-dy
 * @date 2025/3/1 16:08
 */
@Data
public class UserInfoVO {
    // 用户id
    private String userId;
    // 用户昵称
    private String nickName;
    // 用户头像链接
    private String avatar;
    // 已使用空间大小/B
    private Long usedSpace;
    // 总空间大小/B
    private Long totalSpace;
    // 是不是vip
    private Integer isVip;
    private Integer maxFolderLevel = FileConstants.FILE_MAX_LEVEL;
    private Integer isAdmin;
}
