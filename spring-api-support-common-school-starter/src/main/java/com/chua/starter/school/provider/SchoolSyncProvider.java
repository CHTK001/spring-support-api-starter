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
     * 同步所有学校数据
     *
     * @param config 同步配置
     * @return 是否同步成功
     */
    boolean syncAllSchoolData(SchoolSyncConfig config);
    
    /**
     * 根据学校代码同步学校信息
     *
     * @param schoolCode 学校代码
     * @param config 同步配置
     * @return 是否同步成功
     */
    boolean syncSchoolInfoByCode(String schoolCode, SchoolSyncConfig config);
    
    /**
     * 根据学校代码同步学校专业
     *
     * @param schoolCode 学校代码
     * @param config 同步配置
     * @return 是否同步成功
     */
    boolean syncSchoolMajorByCode(String schoolCode, SchoolSyncConfig config);
    
    /**
     * 根据学校代码同步学校招生计划
     *
     * @param schoolCode 学校代码
     * @param config 同步配置
     * @return 是否同步成功
     */
    boolean syncSchoolAdmissionPlanByCode(String schoolCode, SchoolSyncConfig config);
    
    /**
     * 根据学校代码同步学校历年分数
     *
     * @param schoolCode 学校代码
     * @param config 同步配置
     * @return 是否同步成功
     */
    boolean syncSchoolHistoricalScoreByCode(String schoolCode, SchoolSyncConfig config);
    
    /**
     * 同步所有专业
     *
     * @param config 同步配置
     * @return 是否同步成功
     */
    boolean syncAllMajors(SchoolSyncConfig config);

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
    
    /**
     * 获取所有专业列表
     *
     * @param config 同步配置
     * @return 是否获取成功
     */
    boolean getAllSpecialties(SchoolSyncConfig config);
}