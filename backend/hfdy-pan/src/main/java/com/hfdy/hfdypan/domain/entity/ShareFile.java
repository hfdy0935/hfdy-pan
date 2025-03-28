package com.hfdy.hfdypan.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author hf-dy
 * @date 2025/3/7 21:52
 */
@TableName("share_file")
@Data
public class ShareFile {
    @TableField("share_id")
    private String shareId;
    @TableField("file_id")
    private String fileId;
}
