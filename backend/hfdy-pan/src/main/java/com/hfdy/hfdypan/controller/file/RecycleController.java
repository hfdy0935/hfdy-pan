package com.hfdy.hfdypan.controller.file;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hfdy.hfdypan.domain.dto.file.DeleteItemDTO;
import com.hfdy.hfdypan.domain.dto.file.RecoverRecycleDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.vo.ApiResp;
import com.hfdy.hfdypan.domain.vo.file.GetRecycleVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.CommonFileService;
import com.hfdy.hfdypan.service.RecycleService;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/10 23:56
 */

@RestController
@RequestMapping("/api/recycle")
@Validated
public class RecycleController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private FileMapper fileMapper;
    @Resource
    private CommonFileService commonFileService;
    @Resource
    private RecycleService recycleService;

    /**
     * 获取所有回收站文件，只涉及一级文件，如果删除的是文件夹，里面的文件不会在这显示，但恢复时可以一起恢复
     *
     * @return
     */
    @GetMapping
    public ApiResp<List<GetRecycleVO>> getRecycle() {
        User user = userMapper.selectById(ThreadLocalUtil.getCurrentUserId());
        List<File> fileList = fileMapper.getDeletedFiles(user.getId());
        List<GetRecycleVO> getRecycleVOS = new ArrayList<>();
        for (File file : fileList) {
            GetRecycleVO getRecycleVO = new GetRecycleVO();
            BeanUtils.copyProperties(file, getRecycleVO);
            getRecycleVOS.add(getRecycleVO);
        }
        return ApiResp.success(getRecycleVOS);
    }

    /**
     * 彻底删除回收站的文件
     *
     * @param dto
     * @return
     */
    @DeleteMapping
    @Transactional(rollbackFor = BusinessException.class)
    public ApiResp<Void> deleteRecycle(@Valid @RequestBody DeleteItemDTO dto) {
        if (dto.getIds().isEmpty()) return ApiResp.success();
        for (File file : fileMapper.getAllFilesByIds(dto.getIds())) {
            try {
                commonFileService.deleteItem(file, dto.isComplete());
            } catch (Exception e) {
                throw new BusinessException(HttpCodeEnum.FILE_DELETE_ERROR);
            }
        }
        return ApiResp.success();
    }

    /**
     * 恢复
     *
     * @param dto
     * @return
     */
    @PutMapping("/recover")
    @Transactional(rollbackFor = BusinessException.class)
    public ApiResp<Void> recoverRecycle(@Valid @RequestBody RecoverRecycleDTO dto) {
        User user = userMapper.selectById(ThreadLocalUtil.getCurrentUserId());
        List<File> files = fileMapper.getAllFilesByIds(dto.getIds());
        for (File file : files) {
            if (!file.getUserId().equals(user.getId())) {
                throw new BusinessException(HttpCodeEnum.FILE_RECOVER_ERROR, "恢复失败，没有权限");
            }
        }
        // 要么是根目录，要么存在id的目录
        if (!dto.getPid().isEmpty() && !fileMapper.exists(Wrappers.<File>lambdaQuery().eq(File::getId, dto.getPid()))) {
            throw new BusinessException(HttpCodeEnum.FILE_RECOVER_ERROR, "恢复失败，目标文件夹不存在");
        }
        recycleService.recover(files, dto);
        return ApiResp.success();
    }
}
