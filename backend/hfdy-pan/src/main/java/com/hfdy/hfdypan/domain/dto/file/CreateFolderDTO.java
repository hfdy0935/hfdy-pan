package com.hfdy.hfdypan.domain.dto.file;

import lombok.Data;

/**
 * @author hf-dy
 * @date 2025/1/17 23:40
 * @description 创建文件夹请求体
 */
@Data
public class CreateFolderDTO {
    private String name;
    private String pid;
}

