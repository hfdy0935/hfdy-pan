package com.hfdy.hfdypan.controller.file;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.domain.dto.file.ShareFileDTO;
import com.hfdy.hfdypan.domain.dto.file.UpdateMyShareFilesDTO;
import com.hfdy.hfdypan.domain.dto.file.UpdateMyShareOptionsDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.entity.Share;
import com.hfdy.hfdypan.domain.entity.ShareFile;
import com.hfdy.hfdypan.domain.entity.User;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.domain.vo.ApiResp;
import com.hfdy.hfdypan.domain.vo.file.GetMyShareVO;
import com.hfdy.hfdypan.domain.vo.file.ShareFileVO;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.ShareFileMapper;
import com.hfdy.hfdypan.mapper.ShareMapper;
import com.hfdy.hfdypan.mapper.UserMapper;
import com.hfdy.hfdypan.service.TransferFileService;
import com.hfdy.hfdypan.utils.RedisUtil;
import com.hfdy.hfdypan.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/10 13:46
 */
@RestController
//  /share/的是公开分享的
@RequestMapping("/api/shareFile")
@Validated
public class ShareController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private ShareFileMapper shareFileMapper;
    @Resource
    private ShareMapper shareMapper;
    @Resource
    private FileMapper fileMapper;
    @Resource
    private RedisUtil<String> redisUtil;
    @Resource
    private TransferFileService transferFileService;

    /**
     * 永不过期设置
     */
    LocalDateTime farFuture = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999999999);

    /**
     * 获取我的分享列表
     *
     * @return
     */
    @GetMapping
    public ApiResp<List<GetMyShareVO>> getMyShare() {
        User user = userMapper.selectById(ThreadLocalUtil.getCurrentUserId());
        List<Share> shares = shareMapper.selectList(Wrappers.<Share>lambdaQuery().eq(Share::getUserId, user.getId()));
        List<GetMyShareVO> vos = new ArrayList<>();
        for (Share share : shares) {
            List<String> fileIds = shareFileMapper.selectList(Wrappers.<ShareFile>lambdaQuery().eq(ShareFile::getShareId, share.getId()))
                    .stream().map(ShareFile::getFileId).toList();
            List<File> files = fileIds.isEmpty() ? new ArrayList<>() : fileMapper.selectList(Wrappers.<File>lambdaQuery().in(File::getId, fileIds));
            GetMyShareVO vo = new GetMyShareVO();
            vo.setId(share.getId());
            vo.setCreateTime(share.getCreateTime());
            vo.setVisitNum(share.getVisitNum());
            vo.setItemNum(files.size());
            vo.setExpire(share.getExpire() < 0 ? farFuture : share.getCreateTime().plusSeconds(share.getExpire()));
            vo.setPwd(share.getPwd());
            vo.setFileIds(fileIds);
            vos.add(vo);
        }
        return ApiResp.success(vos);
    }

    @DeleteMapping
    @Transactional(rollbackFor = BusinessException.class)
    public ApiResp<Void> deleteMyShare(@RequestParam("id") @NotNull(message = "分享id不能为空") String id) {
        User user = userMapper.selectById(ThreadLocalUtil.getCurrentUserId());
        List<Share> shares = shareMapper.selectList(Wrappers.<Share>lambdaQuery().eq(Share::getUserId, user.getId()).eq(Share::getId, id));
        if (shares.isEmpty()) throw new BusinessException(HttpCodeEnum.FILE_SHARE_NOT_EXISTS);
        // 先清缓存
        String key = RedisConstants.SHARED_FILE_KEY + ":" + id;
        redisUtil.delete(key);
        // 删除share
        shareMapper.deleteByIds(shares);
        // 删除shareFile
        LambdaQueryWrapper<ShareFile> shareFileLambdaQueryWrapper = Wrappers.<ShareFile>lambdaQuery().eq(ShareFile::getShareId, id);
        shareFileMapper.delete(shareFileLambdaQueryWrapper);
        return ApiResp.success();
    }

    /**
     * 修改分享配置
     *
     * @param dto
     * @return
     */
    @PutMapping("/options")
    public ApiResp<Void> updateMyShareOptions(@Valid @RequestBody UpdateMyShareOptionsDTO dto) {
        User user = userMapper.selectById(ThreadLocalUtil.getCurrentUserId());
        List<Share> shares = shareMapper.selectList(Wrappers.<Share>lambdaQuery().eq(Share::getUserId, user.getId()).eq(Share::getId, dto.getId()));
        if (shares.isEmpty()) throw new BusinessException(HttpCodeEnum.FILE_SHARE_NOT_EXISTS);
        Share share = shares.get(0);
        share.setPwd(dto.getPwd());
        share.setExpire(dto.getExpire());
        share.setCreateTime(LocalDateTime.now());
        shareMapper.updateById(share);
        boolean shouldSetExpire = dto.getExpire() != null && dto.getExpire() > 0;
        long expire = shouldSetExpire ? dto.getExpire() : -1L;
        redisUtil.set(RedisConstants.SHARED_FILE_KEY + ":" + dto.getId(), "1", expire);
        return ApiResp.success();
    }


    /**
     * 修改分享文件
     *
     * @param dto
     * @return
     */
    @PutMapping("/files")
    public ApiResp<Void> updateMyShareFiles(@Valid @RequestBody UpdateMyShareFilesDTO dto) {
        User user = userMapper.selectById(ThreadLocalUtil.getCurrentUserId());
        Share share = shareMapper.selectById(dto.getShareId());
        if (share == null) throw new BusinessException(HttpCodeEnum.FILE_SHARE_NOT_EXISTS);
        List<File> files = fileMapper.selectList(Wrappers.<File>lambdaQuery().in(File::getId, dto.getFIleIds()));
        // 要插入的分享-文件记录
        List<ShareFile> shareFiles = new ArrayList<>();
        for (File file : files) {
            if (!file.getUserId().equals(user.getId())) {
                throw new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR, "修改失败，请确保所有文件都属于你");
            }
            ShareFile shareFile = new ShareFile();
            shareFile.setShareId(share.getId());
            shareFile.setFileId(file.getId());
            shareFiles.add(shareFile);
        }
        // 删除原来的
        shareFileMapper.delete(Wrappers.<ShareFile>lambdaQuery().eq(ShareFile::getShareId, share.getId()));
        if (!shareFiles.isEmpty())
            shareFileMapper.insert(shareFiles);
        return ApiResp.success();
    }

    /**
     * 分享文件
     *
     * @param dto
     * @return
     */
    @PostMapping
    public ApiResp<ShareFileVO> shareFiles(@Valid @RequestBody ShareFileDTO dto) {
        String userId = ThreadLocalUtil.getCurrentUserId();
        List<File> fileList = fileMapper.selectList(Wrappers.<File>lambdaQuery().eq(File::getUserId, userId).in(File::getId, dto.getIds()));
        if (fileList.size() != dto.getIds().size()) {
            throw new BusinessException(HttpCodeEnum.FILE_OWNER_ERROR, "分享失败，文件不存在或已被删除或没有权限");
        }
        ShareFileVO vo = transferFileService.shareFiles(dto, fileList);
        // 把用户已分享的文件id添加到redis
        boolean shouldSetExpire = dto.getExpire() != null && dto.getExpire() > 0;
        long expire = shouldSetExpire ? dto.getExpire() : -1L;
        redisUtil.set(RedisConstants.SHARED_FILE_KEY + ":" + vo.getShareId(), "1", expire);
        return ApiResp.success(vo);
    }
}
