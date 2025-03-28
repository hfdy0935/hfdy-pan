package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/1 17:35
 */
@Data
public class DeleteItemDTO {
    @NotEmpty
    private List<String> ids;
    @NotNull
    boolean complete;
}
