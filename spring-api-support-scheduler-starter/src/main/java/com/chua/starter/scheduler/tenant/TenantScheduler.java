package com.chua.starter.scheduler.tenant;

import com.chua.starter.system.entity.SysTenant;
import com.chua.starter.system.pojo.tenant.SysTenantSyncV1Request;
import com.chua.starter.system.service.SysTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 租户定时任务
 *
 * @author CH
 */
@SpringBootConfiguration
@RequiredArgsConstructor
public class TenantScheduler {

    final SysTenantService sysTenantService;

    @Scheduled(cron = "0 0 0,1,2 * * *")
    public void jobForTenantUpdateForService() {
        List<SysTenant> list = sysTenantService.list();
        for (SysTenant sysTenant : list) {
            Thread.ofVirtual()
                    .start(() -> {
                        sysTenantService.sync(new SysTenantSyncV1Request(sysTenant.getSysTenantId()));
                    });
        }
    }
}
