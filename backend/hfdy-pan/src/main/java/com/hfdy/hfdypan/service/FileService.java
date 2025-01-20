package com.hfdy.hfdypan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfdy.hfdypan.domain.dto.file.CreateFolderDTO;
import com.hfdy.hfdypan.domain.dto.file.QueryFileListDTO;
import com.hfdy.hfdypan.domain.dto.file.RenameFileDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.vo.file.FileDetailVO;
import com.hfdy.hfdypan.domain.vo.file.QueryFileListVO;


/**
 * @author hf-dy
 * @date 2025/1/17 10:54
 */

public interface FileService extends IService<File> {

    /**
     * 分页查询文件列表
     *
     * @param dto
     * @return
     */
    QueryFileListVO queryFileList(QueryFileListDTO dto);

    /**
     * 创建求交集
     *
     * @param dto
     */
    void createFolder(CreateFolderDTO dto);

    /**
     * 生成该目录下不重复的文件夹名
     *
     * @param pid
     * @return
     */
    String genNewFolderName(String pid);

    /**
     * 重命名文件
     *
     * @param dto
     */
    void renameFile(RenameFileDTO dto);


    /**
     * 根据id删除
     *
     * @param id
     */
    void deleteById(String id);

    /**
     * 根据id获取文件/文件夹详情
     *
     * @param id
     * @return
     */
    FileDetailVO getDetailById(String id);
}
