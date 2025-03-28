package com.hfdy.hfdypan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfdy.hfdypan.domain.dto.file.RecoverRecycleDTO;
import com.hfdy.hfdypan.domain.entity.File;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/10 23:56
 */
public interface RecycleService extends IService<File> {


    /**
     * 恢复文件
     *
     * @param fileList 要恢复的文件列表
     * @param dto
     */
    void recover(List<File> fileList, RecoverRecycleDTO dto);
}
