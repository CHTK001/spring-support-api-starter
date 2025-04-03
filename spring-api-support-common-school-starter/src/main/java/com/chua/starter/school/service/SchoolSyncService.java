package com.chua.starter.school.service;

import com.chua.starter.school.entity.SchoolSyncConfig;

/**
 * 学校同步服务接口
 *
 * @author CH
 */
public interface SchoolSyncService {

    /**
     * 同步学校信息
     *
     * @param schoolId 高考网学校ID
     * @param config   同步配置
     * @return 是否同步成功
     */
    boolean syncSchoolInfo(String schoolId, SchoolSyncConfig config);

    /**
     * 同步学校专业信息
     *
     * @param schoolId 高考网学校ID
     * @param config   同步配置
     * @return 是否同步成功
     */
    boolean syncSchoolMajors(String schoolId, SchoolSyncConfig config);

    /**
     * 同步学校招生计划
     *
     * @param schoolId 高考网学校ID
     * @param config   同步配置
     * @return 是否同步成功
     */
    boolean syncAdmissionPlans(String schoolId, SchoolSyncConfig config);

    /**
     * 同步学校历年分数
     *
     * @param schoolId 高考网学校ID
     * @param config   同步配置
     * @return 是否同步成功
     */
    boolean syncHistoricalScores(String schoolId, SchoolSyncConfig config);

    /**
     * 同步学校所有相关数据
     *
     * @param schoolId 高考网学校ID
     * @param config   同步配置
     * @return 是否同步成功
     */
    boolean syncAllSchoolData(String schoolId, SchoolSyncConfig config);
} 