package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * @author hf-dy
 * @date 2025/1/18 20:45
 * @description 修改文件名请求体
 */
@Data
@NoArgsConstructor
public class RenameItemDTO {
    @NotNull
    private String id;
    @NotEmpty
    private String name;
}
