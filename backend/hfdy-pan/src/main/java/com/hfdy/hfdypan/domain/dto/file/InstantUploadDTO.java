package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author hf-dy
 * @date 2025/3/9 11:47
 */
@Data
public class InstantUploadDTO {
    @NotNull
    private String pid;
    @NotEmpty
    private String md5;
}
