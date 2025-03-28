package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/11 09:04
 */
@Data
@NoArgsConstructor
public class RecoverRecycleDTO {
    @NotEmpty
    private List<String> ids;
    /**
     * 要恢复到哪个文件夹，id，空字符串表示根目录
     */
    @NotNull
    private String pid;
}
