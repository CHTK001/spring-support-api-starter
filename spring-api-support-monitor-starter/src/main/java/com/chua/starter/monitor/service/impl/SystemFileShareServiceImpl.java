package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.SystemFileShare;
import com.chua.starter.monitor.mapper.SystemFileShareMapper;
import com.chua.starter.monitor.service.SystemFileShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * 文件分享服务实现
 *
 * @author CH
 * @since 2025-12-08
 */
@Slf4j
@Service
public class SystemFileShareServiceImpl extends ServiceImpl<SystemFileShareMapper, SystemFileShare> 
        implements SystemFileShareService {

    @Override
    public SystemFileShare getByShareId(String shareId) {
        return getOne(new LambdaQueryWrapper<SystemFileShare>()
                .eq(SystemFileShare::getShareId, shareId)
                .eq(SystemFileShare::getIsDeleted, 0));
    }

    @Override
    public SystemFileShare createShareLink(Long serverId, String bucket, String filePath, 
                                            String fileName, Integer expireMinutes) {
        SystemFileShare share = new SystemFileShare();
        share.setShareId(UUID.randomUUID().toString().replace("-", ""));
        share.setServerId(serverId);
        share.setBucket(bucket);
        share.setFilePath(filePath);
        share.setFileName(fileName);
        share.setShareType("SHARE");
        share.setViewCount(0);
        // -1 表示无限制
        share.setMaxViewCount(-1);
        share.setCreateTime(new Date());
        share.setIsDeleted(0);

        // 设置过期时间
        if (expireMinutes != null && expireMinutes > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, expireMinutes);
            share.setExpireTime(calendar.getTime());
        }

        save(share);
        log.info("创建分享链接: shareId={}, file={}, expireMinutes={}", 
                share.getShareId(), fileName, expireMinutes);
        return share;
    }

    @Override
    public SystemFileShare createFlashImage(Long serverId, String bucket, String filePath, String fileName) {
        SystemFileShare share = new SystemFileShare();
        share.setShareId(UUID.randomUUID().toString().replace("-", ""));
        share.setServerId(serverId);
        share.setBucket(bucket);
        share.setFilePath(filePath);
        share.setFileName(fileName);
        share.setShareType("FLASH");
        share.setViewCount(0);
        // 闪图只能查看一次
        share.setMaxViewCount(1);
        share.setCreateTime(new Date());
        share.setIsDeleted(0);
        // 闪图不设置过期时间，查看后自动删除
        share.setExpireTime(null);

        save(share);
        log.info("创建闪图: shareId={}, file={}", share.getShareId(), fileName);
        return share;
    }

    @Override
    public void incrementViewCount(String shareId) {
        update(new LambdaUpdateWrapper<SystemFileShare>()
                .eq(SystemFileShare::getShareId, shareId)
                .setSql("view_count = view_count + 1"));
    }

    @Override
    public void markDeleted(String shareId) {
        update(new LambdaUpdateWrapper<SystemFileShare>()
                .eq(SystemFileShare::getShareId, shareId)
                .set(SystemFileShare::getIsDeleted, 1));
        log.info("标记分享已删除: shareId={}", shareId);
    }

    @Override
    public int cleanExpiredShares() {
        int count = (int) count(new LambdaQueryWrapper<SystemFileShare>()
                .eq(SystemFileShare::getIsDeleted, 0)
                .lt(SystemFileShare::getExpireTime, new Date()));
        
        if (count > 0) {
            update(new LambdaUpdateWrapper<SystemFileShare>()
                    .eq(SystemFileShare::getIsDeleted, 0)
                    .lt(SystemFileShare::getExpireTime, new Date())
                    .set(SystemFileShare::getIsDeleted, 1));
            log.info("清理过期分享记录: count={}", count);
        }
        return count;
    }

    @Override
    public String generateShortUrl(String baseUrl, String shareId) {
        // 生成短链接：baseUrl/s/{shareId}
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        if (!baseUrl.endsWith("/")) {
            urlBuilder.append("/");
        }
        urlBuilder.append("s/").append(shareId);
        
        String shortUrl = urlBuilder.toString();
        log.debug("生成短链接: shareId={}, url={}", shareId, shortUrl);
        return shortUrl;
    }
}
