package com.hfdy.hfdypan.domain.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author hf-dy
 * @date 2025/3/16 17:50
 */
@Data
public class UpdateUserDTO {
    @Max(1)
    @Min(0)
    private int status;
    @Max(1)
    @Min(0)
    private int isVip;
    @Min(1)
    @Max(100)
    private int uploadLimit;
    @Min(1)
    @Max(1024 * 1024 * 1024)
    private long downloadSpeed;
    @NotEmpty
    private String userId;
}
