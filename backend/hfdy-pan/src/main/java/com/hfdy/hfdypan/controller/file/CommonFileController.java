package com.hfdy.hfdypan.controller.file;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.domain.dto.file.*;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.entity.Share;
import com.hfdy.hfdypan.domain.entity.ShareFile;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.vo.ApiResp;
import com.hfdy.hfdypan.domain.vo.file.GetFileStatusVO;
import com.hfdy.hfdypan.domain.vo.file.ItemDetailVO;
import com.hfdy.hfdypan.domain.vo.file.QueryItemListVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.ShareFileMapper;
import com.hfdy.hfdypan.mapper.ShareMapper;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.CommonFileService;
import com.hfdy.hfdypan.service.TransferFileService;
import com.hfdy.hfdypan.service.UserService;
import com.hfdy.hfdypan.utils.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.simpleframework.xml.core.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/1/17 13:47
 * @description
 */

@RestController
@RequestMapping("/api/file")
@Validated
public class CommonFileController {
    private static final Logger log = LoggerFactory.getLogger(CommonFileController.class);
    @Resource
    private CommonFileService fileService;
    @Resource
    private UserService userService;
    @Resource
    private RedisUtil<Integer> redisUtil;
    @Resource
    private ShareFileMapper shareFileMapper;
    @Resource
    private ShareMapper shareMapper;

    /**
     * 获取文件列表
     *
     * @param dto
     * @return
     */
    @GetMapping("/list")
    @Validated
    public ApiResp<QueryItemListVO> queryFileList(@Valid QueryItemListDTO dto) {
        log.info("获取文件列表");
        QueryItemListVO vo = fileService.queryItemList(dto);
        return ApiResp.success(vo);
    }

    /**
     * 根据id获取文件/文件夹详情
     *
     * @param id
     * @return
     */
    @GetMapping("/detail")
    public ApiResp<ItemDetailVO> getInfoById(@RequestParam("id") String id) {
        return ApiResp.success(fileService.getDetailById(id));
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
     * 重命名文件/文件夹
     *
     * @param dto
     * @return
     */
    @PutMapping("/rename")
    public ApiResp<Void> renameItem(@Valid @RequestBody RenameItemDTO dto) {
        fileService.renameItem(dto);
        return ApiResp.success();
    }

    /**
     * 根据id删除
     *
     * @param dto
     * @return
     */
    @DeleteMapping
    @Transactional(rollbackFor = BusinessException.class)
    public ApiResp<Void> deleteById(@Valid @RequestBody DeleteItemDTO dto) {
        // 删除文件
        fileService.deleteByIds(dto);
        userService.updateUserUsedSpace();
        // 删除用户所有已分享文件中该文件的记录
        String userId = ThreadLocalUtil.getCurrentUserId();
        List<Share> shares = shareMapper.selectList(Wrappers.<Share>lambdaQuery().eq(Share::getUserId, userId));
        if (!shares.isEmpty())
            shareFileMapper.delete(Wrappers.<ShareFile>lambdaQuery()
                    .in(ShareFile::getFileId, dto.getIds())
                    .in(ShareFile::getShareId, shares.stream().map(Share::getId).toList()));
        return ApiResp.success();
    }

    /**
     * 移动文件/文件夹
     *
     * @param dto
     * @return
     */
    @PutMapping("/move")
    public ApiResp<Void> moveItem(@Valid @RequestBody MoveItemDTO dto) {
        fileService.moveItem(dto);
        userService.updateUserUsedSpace();
        return ApiResp.success();
    }

    /**
     * 获取文件状态
     *
     * @param dto
     * @return
     */
    @PostMapping("/transCodeStatus")
    public ApiResp<List<GetFileStatusVO>> getFileStatus(@NotNull @RequestBody GetFileStatusDTO dto) {
        List<GetFileStatusVO> vos = new ArrayList<>();
        for (String id : dto.getIds()) {
            String key = RedisConstants.STATUS_KEY + ":" + id;
            Integer status = redisUtil.get(key);
            if (status == null) {
                File file = fileService.getById(id);
                if (file == null)
                    throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS);
                redisUtil.set(key, file.getStatus(), RedisConstants.STATUS_EXPIRE);
                status = file.getStatus();
            }
            GetFileStatusVO vo = new GetFileStatusVO();
            vo.setId(id);
            vo.setStatus(status);
            vos.add(vo);
        }
        return ApiResp.success(vos);
    }
}
