package com.chua.starter.school.service.impl;

import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.school.entity.SchoolSyncConfig;
import com.chua.starter.school.provider.SchoolSyncProvider;
import com.chua.starter.school.service.SchoolSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 学校同步服务实现类
 *
 * @author CH
 */
@Slf4j
@Service
public class SchoolSyncServiceImpl implements SchoolSyncService {

    @Override
    public boolean syncSchoolInfo(String schoolId, SchoolSyncConfig config) {
        SchoolSyncProvider provider = getProvider(config);
        return provider.syncSchoolInfo(schoolId, config);
    }

    @Override
    public boolean syncSchoolMajors(String schoolId, SchoolSyncConfig config) {
        SchoolSyncProvider provider = getProvider(config);
        return provider.syncSchoolMajors(schoolId, config);
    }

    @Override
    public boolean syncAdmissionPlans(String schoolId, SchoolSyncConfig config) {
        SchoolSyncProvider provider = getProvider(config);
        return provider.syncAdmissionPlans(schoolId, config);
    }

    @Override
    public boolean syncHistoricalScores(String schoolId, SchoolSyncConfig config) {
        SchoolSyncProvider provider = getProvider(config);
        return provider.syncHistoricalScores(schoolId, config);
    }

    @Override
    public boolean syncAllSchoolData(String schoolId, SchoolSyncConfig config) {
        SchoolSyncProvider provider = getProvider(config);
        return provider.syncAllSchoolData(schoolId, config);
    }

    /**
     * 根据配置获取同步提供者
     *
     * @param config 同步配置
     * @return 同步提供者
     */
    private SchoolSyncProvider getProvider(SchoolSyncConfig config) {
        String syncType = config.getSyncType();
        log.info("根据同步类型[{}]获取同步提供者", syncType);
        return ServiceProvider.of(SchoolSyncProvider.class).getNewExtension(syncType);
    }
} 