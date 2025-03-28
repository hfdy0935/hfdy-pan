package com.hfdy.hfdypan.domain.dto.file;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class MoveItemDTO {
    /**
     * 要移动的文件/文件夹id列表
     */
    @NotEmpty(message = "移动的文件/文件夹不能为空1")
    private List<String> idList;
    /**
     * 目标id
     */
    @NotNull("目标文件夹不能为空")
    private String targetId;
    /**
     * 操作，copy or cut
     */
    @NotNull("移动操作类型不能为空")
    @Pattern(regexp = "copy|cut")
    private String op;
}
