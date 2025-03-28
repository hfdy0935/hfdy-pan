package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author hf-dy
 * @date 2025/3/10 16:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMyShareOptionsDTO {
    @NotNull(message = "请指定要修改的分享")
    private String id;
    private Long expire;
    private String pwd;
}
