package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/10 17:56
 */
@Data
@NoArgsConstructor
public class UpdateMyShareFilesDTO {
    @NotEmpty
    private List<String> FIleIds;
    @NotEmpty
    private String shareId;
}
