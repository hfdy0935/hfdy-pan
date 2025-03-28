package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/8 13:55
 */
@Data
public class GetFileStatusDTO {
    @NotNull
    List<String> ids;
}
