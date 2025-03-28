package com.hfdy.hfdypan.controller.file;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hfdy.hfdypan.annotation.UploadSpeedLimit;
import com.hfdy.hfdypan.constants.FileConstants;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.constants.UserConstants;
import com.hfdy.hfdypan.domain.dto.file.DeleteChunksDTO;
import com.hfdy.hfdypan.domain.dto.file.InstantUploadDTO;
import com.hfdy.hfdypan.domain.dto.file.UploadFileChunkDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.enums.file.FileCategoryEnum;
import com.hfdy.hfdypan.domain.enums.file.FileStatusEnum;
import com.hfdy.hfdypan.domain.vo.ApiResp;
import com.hfdy.hfdypan.domain.vo.file.GetUploadedChunkIndexesVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.service.CommonFileService;
import com.hfdy.hfdypan.service.UploadFileService;
import com.hfdy.hfdypan.service.UserService;
import com.hfdy.hfdypan.utils.FileUtil;
import com.hfdy.hfdypan.utils.MinIOUtil;
import com.hfdy.hfdypan.utils.RedisUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hf-dy
 * @date 2025/2/28 15:06
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@Validated
public class UploadFileController {
    @Resource
    private RedisUtil<Integer> redisUtil;
    @Resource
    private UploadFileService uploadFileService;
    @Resource
    private FileMapper fileMapper;
    @Resource
    private MinIOUtil minIOUtil;
    @Resource
    private CommonFileService commonFileService;
    @Resource
    private UserService userService;

    private static final Map<String, Object> folderLocks = new ConcurrentHashMap<>();

    /**
     * 获取文件夹的锁
     *
     * @param pid
     * @return
     */
    private Object getFolderLock(String pid) {
        return folderLocks.computeIfAbsent(pid, k -> new Object());
    }


    @PostMapping("/uploadChunk")
    @UploadSpeedLimit
    public ApiResp<Void> uploadChunk(
            @RequestParam("filename") @NotEmpty String filename,
            @RequestPart("chunkIndex") @NotNull String chunkIndex,
            @RequestParam("totalChunkNum") @Min(1) String totalChunkNum,
            @RequestPart("md5") @NotEmpty String md5,
            @RequestParam("totalSize") @Min(0) Long totalSize,
            @RequestParam("pid") String pid,
            @RequestPart MultipartFile file) {
        synchronized (getFolderLock(pid)) {
            pid = pid == null ? "" : pid;
            // 文件名不能和当前文件夹下的文件名重复
            LambdaQueryWrapper<File> wrapper = Wrappers.<File>lambdaQuery()
                    .eq(File::getPid, pid)
                    .eq(File::getName, filename);
            if (fileMapper.exists(wrapper)) {
                throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR, "上传失败，同一文件夹下的文件/文件夹名不能重复");
            }
            validateParentFolderExistsAndMaxLevel(pid);
        }
        UploadFileChunkDTO dto = UploadFileChunkDTO.builder()
                .filename(filename)
                .chunkIndex(Integer.parseInt(chunkIndex))
                .totalChunkNum(Integer.parseInt(totalChunkNum))
                .md5(md5)
                .totalSize(totalSize)
                .pid(pid)
                .build();
        String id = uploadFileService.uploadChunk(dto, file);
        // 把上传记录添加到redis
        String key = RedisConstants.UPLOAD_FILE_CHUNK_KEY + ":" + id;
        log.info("upload chunk id:{}, index:{}", id, chunkIndex);
        redisUtil.setAdd(key, dto.getChunkIndex());
        return ApiResp.success();
    }


    /**
     * 转码失败后手动请求转码
     *
     * @param id 文件id
     * @return
     */
    @PostMapping("/transVideoCode")
    public ApiResp<Void> transVideoCode(@RequestParam("id") @NotEmpty String id) {
        // 查redis看状态，只有转码失败的才能手动转码
        String key = RedisConstants.UPLOAD_FILE_CHUNK_KEY + ":" + id;
        Integer status = redisUtil.get(key);
        if (FileStatusEnum.TRANS_FAIL.getValue().equals(status)) {
            throw new BusinessException(HttpCodeEnum.FILE_TRANS_ERROR, "转码失败，文件状态错误");
        }
        String userId = ThreadLocalUtil.getCurrentUserId();
        File file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException(HttpCodeEnum.FILE_TRANS_ERROR, "转码失败，文件不存在");
        }
        if (!file.getUserId().equals(userId)) {
            throw new BusinessException(HttpCodeEnum.FILE_TRANS_ERROR, "转码失败，没有权限");
        }
        if (!FileUtil.isVideo(file)) {
            throw new BusinessException(HttpCodeEnum.FILE_TRANS_ERROR, "转码失败，文件类型错误");
        }
        // 开始转码
        redisUtil.set(key, FileStatusEnum.TRANS_ING.getValue(), RedisConstants.STATUS_EXPIRE);
        uploadFileService.lambdaUpdate().eq(File::getId, id).set(File::getStatus, FileStatusEnum.TRANS_ING.getValue()).update();
        boolean res = uploadFileService.transVideo(file, userId);
        if (!res) throw new BusinessException(HttpCodeEnum.FILE_TRANS_ERROR);
        return ApiResp.success();
    }

    /**
     * 取消上传时删除已上传的分块
     *
     * @param dto
     * @return
     */
    @DeleteMapping("/deleteChunks")
    public ApiResp<Void> deleteChunks(@RequestBody @Valid DeleteChunksDTO dto) {
        String contentId = FileUtil.generateContentId(dto.getMd5());
        // redis
        String key = RedisConstants.UPLOAD_FILE_CHUNK_KEY + ":" + contentId;
        redisUtil.delete(key);
        // minio
        for (Integer chunkIndex : dto.getChunkIndexes()) {
            String filename = uploadFileService.getMinIOChunkName(contentId, chunkIndex);
            try {
                minIOUtil.deleteFiles(filename);
            } catch (Exception ignored) {
            }
        }
        return ApiResp.success();
    }


    /**
     * 获取已上传分块的索引，同时也判断是否已上传，用于断点续传和秒传
     *
     * @return
     */
    @GetMapping("/uploadedChunkIndexes")
    public ApiResp<GetUploadedChunkIndexesVO> getUploadedChunkIndexes(@RequestParam("md5") @NotEmpty String md5) {
        GetUploadedChunkIndexesVO vo = new GetUploadedChunkIndexesVO();
        // 用户id + md5
        String contentId = FileUtil.generateContentId(md5);
        String fileKey = RedisConstants.UPLOAD_FILE_KEY + ":" + contentId;
        // 如果至少有一个文件，考虑到复制
        Integer res = redisUtil.get(fileKey);
        // 如果最终有
        if (res != null && res > 0) {
            vo.setUploaded(true);
            return ApiResp.success(vo);
        }
        // 没有文件。再看分块
        String key = RedisConstants.UPLOAD_FILE_CHUNK_KEY + ":" + contentId;
        List<Integer> indexes = redisUtil.setGetAll(key).stream().toList();
        vo.setUploaded(false);
        vo.setIndexes(indexes);
        return ApiResp.success(vo);
    }

    /**
     * 秒传
     *
     * @param dto
     * @return
     */
    @PostMapping("/instantUpload")
    public ApiResp<Void> instantUpload(@RequestBody @Valid InstantUploadDTO dto) {
        File parent = fileMapper.selectById(dto.getPid());
        if (!dto.getPid().isEmpty() && parent == null)
            throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR, "上传失败，文件夹不存在");
        String userId = ThreadLocalUtil.getCurrentUserId();
        String contentId = FileUtil.generateContentId(dto.getMd5());
        // 复制文件
        List<File> files = fileMapper.selectList(Wrappers.<File>lambdaQuery().eq(File::getMd5, dto.getMd5()).eq(File::getUserId, userId));
        if (files.isEmpty()) throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR);
        File firstFile = files.get(0);
        // 不能和pid文件夹的文件重名
        if (fileMapper.exists(Wrappers.<File>lambdaQuery().eq(File::getPid, dto.getPid()).eq(File::getName, firstFile.getName()).eq(File::getUserId, userId))) {
            throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR, "上传失败，文件名不能和当前目录下的文件/文件夹重复");
        }
        String pLevel = parent == null ? "" : parent.getLevel();
        String pid = parent == null ? "" : parent.getId();
        List<File> files1 = commonFileService.copyFile(List.of(firstFile), pLevel, pid);
        fileMapper.insert(files1);
        // redis中该文件上传完成次数+1
        String fileKey = RedisConstants.UPLOAD_FILE_KEY + ":" + contentId;
        redisUtil.incr(fileKey);
        // 重新计算已使用空间大小
        userService.updateUserUsedSpace();
        return ApiResp.success();
    }

    @PostMapping("/mergeChunks")
    public ApiResp<Void> mergeChunks(@RequestBody @Valid UploadFileChunkDTO dto) {
        File parent = fileMapper.selectById(dto.getPid());
        if (!dto.getPid().isEmpty() && parent == null)
            throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR, "上传失败，文件夹不存在");
        if (parent != null && Objects.equals(FileUtil.getLevelNum(parent), FileConstants.FILE_MAX_LEVEL))
            throw new BusinessException(HttpCodeEnum.FILE_MAX_LEVEL_LIMIT, "上传失败，文件层级达到最大深度");
        String id = uploadFileService.mergeChunks(dto);
        // 删除redis中的分块记录
        redisUtil.delete(RedisConstants.UPLOAD_FILE_CHUNK_KEY + ":" + id);
        return ApiResp.success();
    }


    /**
     * 上传歌词
     *
     * @param fileId
     * @param lyric
     * @return
     */
    @PostMapping("/uploadLyric")
    public ApiResp<Void> uploadLyric(@RequestParam("fileId") @NotEmpty String fileId, @RequestPart MultipartFile lyric) {
        File file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(HttpCodeEnum.FILE_NOT_EXISTS);
        }
        String userId = ThreadLocalUtil.getCurrentUserId();
        if (!file.getUserId().equals(userId)) {
            throw new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR);
        }
        if (!file.getCategory().equals(FileCategoryEnum.AUDIO.getCategory())) {
            throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR, "只支持给音频上传歌词");
        }
        uploadFileService.uploadLyric(fileId, lyric);
        return ApiResp.success();
    }

    /**
     * 上传小文件
     *
     * @return
     */
    @UploadSpeedLimit
    @PostMapping("/uploadFile")
    public ApiResp<Void> uploadFile(
            @RequestParam("filename") @NotEmpty String filename,
            @RequestPart("md5") @NotEmpty String md5,
            @RequestParam("totalSize") @NotNull Long totalSize,
            @RequestParam("pid") String pid,
            @RequestParam("category") @NotEmpty String category,
            @RequestParam("mediaType") @NotEmpty String mediaType,
            @RequestPart MultipartFile file
    ) {
        synchronized (getFolderLock(pid)) {
            pid = pid == null ? "" : pid;
            validateParentFolderExistsAndMaxLevel(pid);
            LambdaQueryWrapper<File> wrapper = Wrappers.<File>lambdaQuery()
                    .eq(File::getPid, pid)
                    .eq(File::getName, filename);
            if (fileMapper.exists(wrapper)) {
                throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR, "上传失败，同一文件夹下的文件/文件夹名不能重复");
            }
        }
        UploadFileChunkDTO dto = UploadFileChunkDTO.builder()
                .filename(filename)
                .md5(md5)
                .totalSize(totalSize)
                .pid(pid)
                .category(category)
                .mediaType(mediaType)
                .build();
        uploadFileService.uploadFile(dto, file);
        return ApiResp.success();
    }

    /**
     * 验证父文件夹存在和深度
     *
     * @param pid
     */
    private void validateParentFolderExistsAndMaxLevel(@RequestParam("pid") @NotEmpty String pid) {
        File parent = fileMapper.selectById(pid);
        if (!pid.isEmpty() && parent == null)
            throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR, "上传失败，文件夹不存在");
        if (parent != null && Objects.equals(FileUtil.getLevelNum(parent), FileConstants.FILE_MAX_LEVEL))
            throw new BusinessException(HttpCodeEnum.FILE_MAX_LEVEL_LIMIT, "上传失败，文件层级达到最大深度");
    }
}