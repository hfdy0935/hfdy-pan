package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/10 10:15
 */
@Data
public class SaveShareToMyPanDTO {
    @NotEmpty
    private List<String> srcIds;
    private String to;
    @NotEmpty
    private String shareId;
}
