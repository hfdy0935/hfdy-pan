package com.hfdy.hfdypan.domain.dto.file;

import lombok.Data;

/**
 * @author hf-dy
 * @date 2025/1/18 20:45
 * @description 修改文件名请求体
 */
@Data
public class RenameFileDTO {
    private String id;
    private String name;
}
