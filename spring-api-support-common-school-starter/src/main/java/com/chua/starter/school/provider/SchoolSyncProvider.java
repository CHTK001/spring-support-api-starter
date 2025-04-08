package com.chua.starter.school.provider;

import com.chua.starter.school.entity.SchoolInfo;
import com.chua.starter.school.entity.SchoolSyncConfig;

import java.util.List;

/**
 * 学校同步提供者接口（SPI）
 *
 * @author CH
 */
public interface SchoolSyncProvider {

    /**
     * 获取同步类型
     *
     * @return 同步类型
     */
    String getType();

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

    /**
     * 同步所有学校相关数据
     *
     * @param config 同步配置
     * @return 是否同步成功
     */
    default boolean syncAllSchoolData(SchoolSyncConfig config) {
        for (String listSchoolId : listSchoolIds()) {
            try {
                syncAllSchoolData(listSchoolId, config);
            } catch (Exception ignored) {
            }
        }

        return true;
    }

    /**
     * 获取所有学校ID
     *
     * @return 学校ID列表
     */
    List<String> listSchoolIds();

    /**
     * 查找学校信息
     *
     * @param schoolName 学校名称
     * @return 学校信息列表
     */
    List<SchoolInfo> searchSchools(String schoolName);
} 