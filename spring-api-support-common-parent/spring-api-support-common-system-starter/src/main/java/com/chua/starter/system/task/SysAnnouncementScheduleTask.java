package com.chua.starter.system.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.entity.system.SysAnnouncement;
import com.chua.starter.system.service.SysAnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公告定时发布/下线任务
 * <p>
 * 每分钟扫描一次:
 * <ul>
 *     <li>草稿(status=0) 且 发布时间 &lt;= 当前时间 的公告, 自动置为已发布(status=1)</li>
 *     <li>已发布(status=1) 且 过期时间 &lt;= 当前时间 的公告, 自动置为已下架(status=2)</li>
 * </ul>
 * 项目已通过 {@code @EnableScheduling} 开启定时任务支持, 本类直接生效。
 *
 * @author CH
 * @since 4.0.0.51
 */
@Slf4j
// @Component  // 暂时注释掉，因为有多个 CacheManager bean，Spring 不知道该注入哪个
/**
 * SysAnnouncementScheduleTask类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
public class SysAnnouncementScheduleTask {

    /**
     * 公告活跃缓存名(与 SysAnnouncementServiceImpl 上的 @Cacheable 一致)
    */
    private static final String ACTIVE_CACHE = "announcement:active";

    /**
     * SYS公告服务
    */
    @Autowired
    private SysAnnouncementService sysAnnouncementService;

    /**
     * 缓存管理器: 状态流转后主动清空活跃公告缓存
    */
    @Autowired(required = false)
    private CacheManager cacheManager;

    /**
     * 每分钟执行一次公告状态流转
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scheduleAnnouncementStatus() {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 1. 草稿到期 -> 已发布
            boolean published = sysAnnouncementService.update(Wrappers.<SysAnnouncement>lambdaUpdate()
                    .set(SysAnnouncement::getSysAnnouncementStatus, SysAnnouncement.STATUS_PUBLISHED)
                    .set(SysAnnouncement::getSysAnnouncementPublishTime, now)
                    .eq(SysAnnouncement::getSysAnnouncementStatus, SysAnnouncement.STATUS_DRAFT)
                    .isNotNull(SysAnnouncement::getSysAnnouncementPublishTime)
                    .le(SysAnnouncement::getSysAnnouncementPublishTime, now));

            // 2. 已发布且已过期 -> 已下架
            boolean expired = sysAnnouncementService.update(Wrappers.<SysAnnouncement>lambdaUpdate()
                    .set(SysAnnouncement::getSysAnnouncementStatus, SysAnnouncement.STATUS_UNPUBLISHED)
                    .eq(SysAnnouncement::getSysAnnouncementStatus, SysAnnouncement.STATUS_PUBLISHED)
                    .isNotNull(SysAnnouncement::getSysAnnouncementExpireTime)
                    .le(SysAnnouncement::getSysAnnouncementExpireTime, now));

            if (published || expired) {
                log.info("[公告定时] 自动发布或下架公告");
                evictActiveCache();
            }
        } catch (Exception e) {
            log.warn("[公告定时] 公告状态流转失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清空活跃公告缓存, 使状态变更立即对前端可见
     */
    private void evictActiveCache() {
        try {
            var cache = cacheManager.getCache(ACTIVE_CACHE);
            if (null != cache) {
                cache.clear();
            }
        } catch (Exception e) {
            log.debug("[公告定时] 清理活跃公告缓存失败: {}", e.getMessage());
        }
    }
}
