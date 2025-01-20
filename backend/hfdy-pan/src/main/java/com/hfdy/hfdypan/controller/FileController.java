package com.hfdy.hfdypan.controller;

import com.hfdy.hfdypan.domain.dto.file.CreateFolderDTO;
import com.hfdy.hfdypan.domain.dto.file.QueryFileListDTO;
import com.hfdy.hfdypan.domain.dto.file.RenameFileDTO;
import com.hfdy.hfdypan.domain.vo.ApiResp;
import com.hfdy.hfdypan.domain.vo.file.FileDetailVO;
import com.hfdy.hfdypan.domain.vo.file.QueryFileListVO;
import com.hfdy.hfdypan.service.FileService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * @author hf-dy
 * @date 2025/1/17 13:47
 * @description
 */

@RestController
@RequestMapping("/api/file")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    @Resource
    private FileService fileService;

    /**
     * 获取文件列表
     *
     * @param dto
     * @return
     */
    @GetMapping("/list")
    public ApiResp<QueryFileListVO> queryFileList(QueryFileListDTO dto) {
        log.info("获取文件列表");
        QueryFileListVO vo = fileService.queryFileList(dto);
        return ApiResp.success(vo);
    }

    /**
     * 新建文件夹
     *
     * @param dto
     * @return
     */
    @PostMapping("/folder")
    public ApiResp<Void> createFolder(@RequestBody CreateFolderDTO dto) {
        fileService.createFolder(dto);
        return ApiResp.success();
    }


    /**
     * 获取新建文件夹的文件夹名，需要后端统计所有文件夹，生成不重复的文件夹名
     *
     * @return
     */
    @GetMapping("new-folder-name")
    public ApiResp<String> getNewFolderName(@RequestParam String pid) {
        return ApiResp.success(fileService.genNewFolderName(pid));
    }


    /**
     * 重命名文件
     *
     * @param dto
     * @return
     */
    @PutMapping("/rename")
    public ApiResp<Void> renameFile(@RequestBody RenameFileDTO dto) {
        fileService.renameFile(dto);
        return ApiResp.success();
    }

    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    @DeleteMapping
    public ApiResp<Void> deleteFileById(@RequestParam("id") String id) {
        fileService.deleteById(id);
        return ApiResp.success();
    }

    /**
     * 根据id获取文件/文件夹详情
     *
     * @param id
     * @return
     */
    @GetMapping("/detail")
    public ApiResp<FileDetailVO> getInfoById(@RequestParam("id") String id) {
        return ApiResp.success(fileService.getDetailById(id));
    }
}
