package com.hfdy.hfdypan.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfdy.hfdypan.constants.MinIOConstants;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.domain.dto.file.UploadFileChunkDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.enums.file.FileCategoryEnum;
import com.hfdy.hfdypan.domain.enums.file.FileIsDeletedEnum;
import com.hfdy.hfdypan.domain.enums.file.FileStatusEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.UploadFileService;
import com.hfdy.hfdypan.service.UserService;
import com.hfdy.hfdypan.utils.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hfdy.hfdypan.utils.FileUtil.generateContentId;

/**
 * @author hf-dy
 * @date 2025/2/28 15:08
 */
@Service
@Slf4j
public class UploadFileServiceImpl extends ServiceImpl<FileMapper, File> implements UploadFileService {
    @Resource
    private MinIOUtil minIOUtil;
    @Resource
    private FileMapper fileMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private FfmpegUtil ffmpegUtil;
    @Resource
    private RedisUtil<Integer> redisUtil;


    @Override
    public String getMinIOChunkName(String id, int chunkIndex) {
        return MinIOConstants.CHUNK_FOLDER + "/" + id + "_" + chunkIndex;
    }

    /**
     * 生成整个文件的存储路径
     *
     * @param category 文件分类
     * @param filename 文件名，也可能是路径
     * @return
     */
    private String getMinIOFilename(String category, String filename) {
        return category + "/" + filename;
    }

    /**
     * 限制用户使用空间
     *
     * @param addSize
     */
    private void limitUsedSpace(Long addSize) {
        User user = userMapper.selectById(ThreadLocalUtil.getCurrentUserId());
        if (user.getUsedSpace() + addSize > user.getTotalSpace()) {
            throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR, "上传失败，用户空间不足");
        }
    }


    @Override
    public String uploadChunk(UploadFileChunkDTO dto, MultipartFile file) {
        limitUsedSpace(dto.getTotalSize());
        String contentId = generateContentId(dto.getMd5());
        // 分块不需要上传到具体类型
        String filename = getMinIOChunkName(contentId, dto.getChunkIndex());
        minIOUtil.uploadFile(file, filename);
        return contentId;
    }


    /**
     * 用于转码视频的线程池
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 转码
     *
     * @param category 视频分类
     * @param filename 视频文件名
     * @param md5      视频md5
     * @param src      原视频minio路径
     * @param id       视频数据库id
     * @param userId
     * @return 是否成功
     */
    private boolean transVideo(String category, String filename, String md5, String src, String id, String userId) {
        String folderPath = category + "/" + id;
        try {
            // 读取新的视频文件，转码，结果保存到对应文件夹下
            String target = ffmpegUtil.transCode(src, folderPath, id, filename);
            // 最终minio中视频/m3u8的路径
            String finalPath = src;
            // 转码成功后再移动
            if (!target.isEmpty()) {
                String newPath = getMinIOFilename(category, id + "/" + filename);
                minIOUtil.moveFile(src, newPath);
                finalPath = target;
            }
            // 修改path和转码状态，如果视频被删了就不改
            redisUtil.set(RedisConstants.STATUS_KEY + ":" + id, FileStatusEnum.TRANS_OK.getValue(), RedisConstants.STATUS_EXPIRE);
            lambdaUpdate().eq(File::getId, id).set(File::getPath, finalPath).set(File::getStatus, FileStatusEnum.TRANS_OK.getValue()).update();
            // 转码完成后再redis中该文件上传数量+1
            String contentId = generateContentId(userId, md5);
            String key = RedisConstants.UPLOAD_FILE_KEY + ":" + contentId;
            redisUtil.incr(key);
            return true;
        } catch (Exception e) {
            redisUtil.set(RedisConstants.STATUS_KEY + ":" + id, FileStatusEnum.TRANS_FAIL.getValue(), RedisConstants.STATUS_EXPIRE);
            lambdaUpdate().eq(File::getId, id).set(File::getStatus, FileStatusEnum.TRANS_FAIL.getValue()).update();
            return false;
        }
    }

    @Override
    public boolean transVideo(UploadFileChunkDTO dto, String src, String id, String userId) {
        String category = dto.getCategory();
        String filename = dto.getFilename();
        return transVideo(category, filename, dto.getMd5(), src, id, userId);
    }

    @Override
    public boolean transVideo(File file, String userId) {
        return transVideo(file.getCategory(), file.getName(), file.getMd5(), file.getPath(), file.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public String mergeChunks(UploadFileChunkDTO dto) {
        List<String> filenames = new ArrayList<>();
        String contentId = generateContentId(dto.getMd5()); // 内容唯一id
        // 文件数据库id
        String dbId = StringUtil.getRandomUUid();
        for (int i = 0; i < dto.getTotalChunkNum(); i++) {
            String filename = getMinIOChunkName(contentId, i);
            filenames.add(filename);
        }
        // 权限校验
        String userId = ThreadLocalUtil.getCurrentUserId();
        File parent = getById(dto.getPid());
        if (parent != null && !Objects.equals(parent.getUserId(), userId)) {
            throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR, "上传失败，没有权限");
        }
        // 合并
        String path = getMinIOFilename(dto.getCategory(), dbId + "_" + dto.getFilename());
        try {
            minIOUtil.mergeFileChunks(filenames, path);
        } catch (Exception e) {
            throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR);
        }
        // 是否需要转码
        boolean needTransCode = dto.getCategory().equals(FileCategoryEnum.VIDEO.getCategory());
        int status = needTransCode ? FileStatusEnum.TRANS_ING.getValue() : FileStatusEnum.NO_NEED_TRANS.getValue();
        // redis设置状态
        redisUtil.set(RedisConstants.STATUS_KEY + ":" + dbId, status, RedisConstants.STATUS_EXPIRE);
        // 视频转码，完成后修改数据库和redis的状态，以及redis上传次数+1
        if (needTransCode) {
            executorService.execute(() -> {
                // userId要传进去，这个线程ThreadLocal是另一个
                if (!transVideo(dto, path, dbId, userId)) {
                    log.error("转码失败");
                }
            });
        } else {
            // 其他文件，无需转码，只加上传次数
            String key = RedisConstants.UPLOAD_FILE_KEY + ":" + contentId;
            redisUtil.incr(key);
        }
        String level = parent == null ? dto.getFilename() : (parent.getLevel() + "/") + dto.getFilename();
        File file = File.builder()
                .id(dbId)
                .userId(ThreadLocalUtil.getCurrentUserId())
                .level(level)
                .md5(dto.getMd5())
                .name(dto.getFilename())
                .path(path)
                .createTime(LocalDateTime.now())
                .size(dto.getTotalSize())
                .pid(dto.getPid())
                .category(dto.getCategory())
                .mediaType(dto.getMediaType())
                .isDeleted(FileIsDeletedEnum.NO.getCode())
                .status(status)
                .build();
        fileMapper.insert(file);
        LambdaUpdateWrapper<User> userLambdaQueryWrapper = Wrappers.<User>lambdaUpdate()
                .eq(User::getId, ThreadLocalUtil.getCurrentUserId())
                .setIncrBy(User::getUsedSpace, file.getSize());
        userMapper.update(userLambdaQueryWrapper);
        // 视频的
        return contentId;
    }

    @Override
    public void uploadLyric(String fileId, MultipartFile lyric) {
        String minioPath = MinIOConstants.LYRIC_FOLDER + "/" + StringUtil.getRandomUUid() + ".lrc";
        minIOUtil.uploadFile(lyric, minioPath);
        lambdaUpdate().eq(File::getId, fileId).set(File::getLyricPath, minioPath).update();
    }


    @Override
    public int uploadFile(UploadFileChunkDTO dto, MultipartFile file) {
        limitUsedSpace(file.getSize());
        String userId = ThreadLocalUtil.getCurrentUserId();
        // 1. 权限验证
        File parent = getById(dto.getPid());
        if (parent != null && !Objects.equals(parent.getUserId(), userId)) {
            throw new BusinessException(HttpCodeEnum.FILE_UPLOAD_ERROR, "上传失败，没有权限");
        }
        // 2. 上传文件
        String dbId = StringUtil.getRandomUUid();
        String filename = getMinIOFilename(dto.getCategory(), dbId + "_" + file.getOriginalFilename());
        String path = minIOUtil.uploadFile(file, filename);

        // 是否需要转码
        boolean needTransCode = dto.getCategory().equals(FileCategoryEnum.VIDEO.getCategory());
        int status = needTransCode ? FileStatusEnum.TRANS_ING.getValue() : FileStatusEnum.NO_NEED_TRANS.getValue();
        // redis设置开始转码状态
        redisUtil.set(RedisConstants.STATUS_KEY + ":" + dbId, status, RedisConstants.STATUS_EXPIRE);
        // 开始转码
        if (needTransCode) {
            executorService.execute(() -> {
                if (!transVideo(dto, path, dbId, userId)) {
                    log.error("转码失败");
                }
            });
        } else {
            // 其他文件，无需转码，只加上传次数
            String contentId = generateContentId(dto.getMd5()); // 内容唯一id
            String key = RedisConstants.UPLOAD_FILE_KEY + ":" + contentId;
            redisUtil.incr(key);
        }
        // 3. 入库
        String level = parent == null ? dto.getFilename() : (parent.getLevel() + "/" + dto.getFilename());
        File dbFile = File.builder()
                .id(dbId)
                .userId(ThreadLocalUtil.getCurrentUserId())
                .level(level)
                .md5(dto.getMd5())
                .name(dto.getFilename())
                .path(path)
                .createTime(LocalDateTime.now())
                .size(dto.getTotalSize())
                .pid(dto.getPid())
                .category(dto.getCategory())
                .mediaType(dto.getMediaType())
                .isDeleted(FileIsDeletedEnum.NO.getCode())
                .status(status)
                .build();
        int rows = fileMapper.insert(dbFile);
        LambdaUpdateWrapper<User> userLambdaQueryWrapper = Wrappers.<User>lambdaUpdate()
                .eq(User::getId, ThreadLocalUtil.getCurrentUserId())
                .setIncrBy(User::getUsedSpace, file.getSize());
        userMapper.update(userLambdaQueryWrapper);
        return rows;
    }
}
