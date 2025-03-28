package com.hfdy.hfdypan.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.domain.dto.file.RecoverRecycleDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.enums.file.FileIsDeletedEnum;
import com.hfdy.hfdypan.domain.enums.file.FileMediaTypeEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.service.RecycleService;
import com.hfdy.hfdypan.utils.FileUtil;
import com.hfdy.hfdypan.utils.RedisUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/10 23:57
 */
@Service
public class RecycleServiceImpl extends ServiceImpl<FileMapper, File> implements RecycleService {
    @Resource
    private FileMapper fileMapper;
    @Resource
    private RedisUtil<Integer> redisUtil;

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public void recover(List<File> fileList, RecoverRecycleDTO dto) {
        String UserId = ThreadLocalUtil.getCurrentUserId();
        File parent = fileMapper.selectById(dto.getPid());
        for (File file : fileList) {
            Long peerWithSameNameNum = fileMapper.selectCount(Wrappers.<File>lambdaQuery()
                    .eq(File::getPid, dto.getPid())
                    .eq(File::getName, file.getName())
                    .eq(File::getUserId, UserId)
            );
            if (peerWithSameNameNum > 0) {
                throw new BusinessException(HttpCodeEnum.FILE_RECOVER_ERROR, "恢复失败，父文件夹下有重名的文件，请先解决冲突");
            }
            String parentLevel = parent == null ? "" : parent.getLevel();
            file.setPid(dto.getPid());
            String newLevel = parentLevel + (parentLevel.isEmpty() ? "" : "/") + FileUtil.getFileName(file.getLevel());
            file.setLevel(newLevel);
            file.setIsDeleted(FileIsDeletedEnum.NO.getCode());
            file.setUpdateTime(LocalDateTime.now());
            file.setDeleteTime(null);
        }
        // 批量恢复
        fileMapper.recoverFiles(fileList);
        // 修改缓存
        for (File file : fileList) {
            // 文件夹没有redis缓存
            if (file.getMediaType().equals(FileMediaTypeEnum.FOLDER.getMediaType())) continue;
            String contentId = FileUtil.generateContentId(file.getMd5());
            redisUtil.set(RedisConstants.STATUS_KEY + ":" + file.getId(), file.getStatus());
            String key = RedisConstants.UPLOAD_FILE_KEY + ":" + contentId;
            redisUtil.incr(key);
        }
    }
}
