package com.chua.starter.scheduler.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 租户定时任务
 * <p>
 * TODO: 待租户模块完善后启用
 * </p>
 *
 * @author CH
 * @since 2024/8/7
 */
@Slf4j
@SpringBootConfiguration
@RequiredArgsConstructor
public class TenantScheduler {

    /**
     * 租户服务更新定时任务
     * 每天0点、1点、2点执行
     */
    @Scheduled(cron = "0 0 0,1,2 * * *")
    public void jobForTenantUpdateForService() {
        // TODO: 实现租户同步逻辑，待租户模块依赖完善后启用
        log.debug("租户同步定时任务执行");
    }
}
