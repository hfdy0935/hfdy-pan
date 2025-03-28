package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/9 09:07
 */
@Data
public class DownloadFileDTO {
    @NotEmpty
    private List<String> fileIds;
}
