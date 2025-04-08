package com.chua.starter.school.provider.impl;

import com.chua.starter.school.entity.SchoolInfo;
import com.chua.starter.school.entity.SchoolSyncConfig;
import com.chua.starter.school.provider.SchoolSyncProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 简易同步提供者（示例）
 *
 * @author CH
 */
@Slf4j
@Service
public class SimpleSyncProvider implements SchoolSyncProvider {

    @Override
    public String getType() {
        return "simple";
    }

    @Override
    public boolean syncSchoolInfo(String schoolId, SchoolSyncConfig config) {
        log.info("简易同步提供者 - 同步学校信息: {}", schoolId);
        // 简单实现，仅打印日志，不执行真正的同步
        return true;
    }

    @Override
    public boolean syncSchoolMajors(String schoolId, SchoolSyncConfig config) {
        log.info("简易同步提供者 - 同步学校专业信息: {}", schoolId);
        return true;
    }

    @Override
    public boolean syncAdmissionPlans(String schoolId, SchoolSyncConfig config) {
        log.info("简易同步提供者 - 同步招生计划: {}", schoolId);
        return true;
    }

    @Override
    public boolean syncHistoricalScores(String schoolId, SchoolSyncConfig config) {
        log.info("简易同步提供者 - 同步历年分数: {}", schoolId);
        return true;
    }

    @Override
    public boolean syncAllSchoolData(String schoolId, SchoolSyncConfig config) {
        log.info("简易同步提供者 - 同步所有学校数据: {}", schoolId);
        boolean result = true;

        result = result && syncSchoolInfo(schoolId, config);
        result = result && syncSchoolMajors(schoolId, config);
        result = result && syncAdmissionPlans(schoolId, config);
        result = result && syncHistoricalScores(schoolId, config);

        return result;
    }

    @Override
    public List<String> listSchoolIds() {
        return List.of();
    }

    @Override
    public List<SchoolInfo> searchSchools(String schoolName) {
        return List.of();
    }
} 