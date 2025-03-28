package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hf-dy
 * @date 2025/1/17 23:40
 * @description 创建文件夹请求体
 */
@Data
public class CreateFolderDTO implements Serializable {
    @NotEmpty(message = "文件夹名不能为空")
    private String name;
    private String pid;
}

