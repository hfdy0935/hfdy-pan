package com.hfdy.hfdypan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfdy.hfdypan.domain.dto.file.*;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.vo.file.ItemDetailVO;
import com.hfdy.hfdypan.domain.vo.file.QueryItemListVO;

import java.util.List;


/**
 * @author hf-dy
 * @date 2025/1/17 10:54
 */

public interface CommonFileService extends IService<File> {

    /**
     * 分页查询文件列表
     *
     * @param dto
     * @return
     */
    QueryItemListVO queryItemList(QueryItemListDTO dto);


    /**
     * 根据id获取文件/文件夹详情
     *
     * @param id
     * @return
     */
    ItemDetailVO getDetailById(String id);

    /**
     * 创建文件夹
     *
     * @param dto
     */
    void createFolder(CreateFolderDTO dto);

    /**
     * 重命名文件
     *
     * @param dto
     */
    void renameItem(RenameItemDTO dto);


    /**
     * 删除一个文件/文件夹，逻辑/物理删除
     *
     * @param file
     * @param deleteCompletely
     * @throws Exception
     */
    void deleteItem(File file, boolean deleteCompletely) throws Exception;


    /**
     * 根据id删除
     *
     * @param dto
     */
    void deleteByIds(DeleteItemDTO dto);

    /**
     * 移动
     *
     * @param dto
     */
    void moveItem(MoveItemDTO dto);

    /**
     * 复制文件
     *
     * @param files       源文件
     * @param targetLevel 要复制到的文件夹level
     * @param targetId    要复制到的文件夹id
     * @return
     * @throws Exception
     */
    List<File> copyFile(List<File> files, String targetLevel, String targetId);
}
