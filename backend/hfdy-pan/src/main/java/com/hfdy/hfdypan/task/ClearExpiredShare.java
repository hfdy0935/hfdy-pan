package com.hfdy.hfdypan.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hfdy.hfdypan.constants.RedisConstants;
import com.hfdy.hfdypan.domain.entity.Share;
import com.hfdy.hfdypan.domain.entity.ShareFile;
import com.hfdy.hfdypan.mapper.FileMapper;
import com.hfdy.hfdypan.mapper.ShareFileMapper;
import com.hfdy.hfdypan.mapper.ShareMapper;
import com.hfdy.hfdypan.utils.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/8 15:24
 */
@Component
@Slf4j
public class ClearExpiredShare {
    @Resource
    private ShareMapper shareMapper;
    @Resource
    private RedisUtil<String> redisUtil;
    @Resource
    private ShareFileMapper shareFileMapper;

    /**
     * 每天2点清理过期的分享
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void clearExpiredShare() {
        List<Share> shares = shareMapper.selectList(Wrappers.<Share>lambdaQuery());
        List<String> shareIdsNeedClear = new ArrayList<>();
        for (Share share : shares) {
            String key = RedisConstants.SHARED_FILE_KEY + ":" + share.getId();
            LocalDateTime now = LocalDateTime.now();
            // 过期时间
            LocalDateTime expireTime = share.getCreateTime().plusSeconds(share.getExpire());
            // 只有数据库中的确认过期才清理
            if (expireTime.isAfter(now)) {
                // 重新写回缓存
                long restSeconds = Duration.between(now, expireTime).getSeconds();
                redisUtil.set(key, "1", restSeconds);
                continue;
            }
            if (!redisUtil.exists(key))
                shareIdsNeedClear.add(share.getId());
        }
        if (!shareIdsNeedClear.isEmpty()) {
            shareMapper.deleteByIds(shareIdsNeedClear);
            shareFileMapper.delete(Wrappers.<ShareFile>lambdaQuery().in(ShareFile::getShareId, shareIdsNeedClear));
            log.info("清理{}个过期分享", shareIdsNeedClear.size());
        }
    }
}
