package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/7 16:57
 */
@Data
public class ShareFileDTO {
    @NotEmpty(message = "分享文件不能为空")
    private List<String> ids;
    private Long expire;
    private String pwd;
}
