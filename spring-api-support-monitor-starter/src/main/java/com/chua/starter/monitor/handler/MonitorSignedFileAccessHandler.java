package com.chua.starter.monitor.handler;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiOrder;
import com.chua.common.support.protocol.filter.storage.BucketAddress;
import com.chua.common.support.protocol.filter.storage.SignedFileAccessHandler;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.SystemFileShare;
import com.chua.starter.monitor.service.file.SystemFileShareService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Monitor 模块的签名文件访问处理器
 * 用于处理分享链接和闪图功能
 * 使用 SpiOrder(0) 设置高优先级，覆盖默认实现
 *
 * @author CH
 * @since 2025-12-08
 */
@Slf4j
@Spi("monitor")
@SpiOrder(0)
public class MonitorSignedFileAccessHandler implements SignedFileAccessHandler {

    @Resource
    private SystemFileShareService fileShareService;

    /**
     * 分享记录本地缓存，减少数据库查询
     * 缓存时间 5 分钟，最大 10000 条记录
     */
    private static final Cache<String, SystemFileShare> SHARE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    /**
     * 已失效分享的缓存，避免重复查询已删除的分享
     */
    private static final Cache<String, Boolean> INVALID_CACHE = CacheBuilder.newBuilder()
            .maximumSize(50000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    @Override
    public BucketAddress resolve(BucketAddress bucketAddress) {
        // 从 path 解析 shareId，格式：/s/{shareId}
        String path = bucketAddress.getPath();
        String shareId = parseShareIdFromPath(path);
        
        if (StringUtils.isEmpty(shareId)) {
            log.debug("无法解析 shareId: path={}", path);
            return null;
        }

        // 检查服务是否可用
        if (fileShareService == null) {
            log.warn("SystemFileShareService 未注入");
            return null;
        }

        // 先检查失效缓存
        if (Boolean.TRUE.equals(INVALID_CACHE.getIfPresent(shareId))) {
            log.debug("分享链接在失效缓存中: shareId={}", shareId);
            return null;
        }

        // 优先从缓存获取分享记录
        SystemFileShare share = SHARE_CACHE.getIfPresent(shareId);
        if (share == null) {
            share = fileShareService.getByShareId(shareId);
            if (share != null) {
                SHARE_CACHE.put(shareId, share);
            }
        }

        if (share == null || share.getIsDeleted() == 1) {
            INVALID_CACHE.put(shareId, Boolean.TRUE);
            return null;
        }

        // 检查是否过期
        if (share.getExpireTime() != null && share.getExpireTime().before(new Date())) {
            log.info("分享链接已过期: shareId={}", shareId);
            fileShareService.markDeleted(shareId);
            invalidateCache(shareId);
            return null;
        }

        // 检查查看次数
        if (share.getMaxViewCount() > 0 && share.getViewCount() >= share.getMaxViewCount()) {
            log.info("分享链接已达最大查看次数: shareId={}", shareId);
            fileShareService.markDeleted(shareId);
            invalidateCache(shareId);
            return null;
        }

        // 增加查看次数
        fileShareService.incrementViewCount(shareId);

        // 如果是闪图，查看后立即标记删除
        if ("FLASH".equals(share.getShareType())) {
            log.info("闪图已被查看，标记删除: shareId={}", shareId);
            fileShareService.markDeleted(shareId);
            invalidateCache(shareId);
        }

        log.debug("短链接解析成功: shareId={}, bucket={}, path={}", 
                shareId, share.getBucket(), share.getFilePath());

        // 返回转换后的真实文件地址
        return BucketAddress.builder()
                .bucket(share.getBucket())
                .path(share.getFilePath())
                .options(bucketAddress.getOptions())
                .build();
    }

    /**
     * 从 path 解析 shareId
     * 格式：/s/{shareId}
     *
     * @param path 路径
     * @return shareId
     */
    private String parseShareIdFromPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        // 移除查询参数
        int queryIndex = path.indexOf('?');
        String cleanPath = queryIndex > 0 ? path.substring(0, queryIndex) : path;
        // 检查格式
        if (cleanPath.startsWith("/s/")) {
            String shareId = cleanPath.substring(3);
            if (shareId.endsWith("/")) {
                shareId = shareId.substring(0, shareId.length() - 1);
            }
            return shareId.isEmpty() ? null : shareId;
        }
        return null;
    }

    /**
     * 使缓存失效
     *
     * @param shareId 分享ID
     */
    private void invalidateCache(String shareId) {
        SHARE_CACHE.invalidate(shareId);
        INVALID_CACHE.put(shareId, Boolean.TRUE);
    }

    /**
     * 清除所有缓存（供外部调用）
     */
    public static void clearAllCache() {
        SHARE_CACHE.invalidateAll();
        INVALID_CACHE.invalidateAll();
    }
}
