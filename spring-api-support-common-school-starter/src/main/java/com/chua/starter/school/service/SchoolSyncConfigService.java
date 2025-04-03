package com.chua.starter.school.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.school.entity.SchoolSyncConfig;

/**
 * 学校同步配置Service
 *
 * @author CH
 */
public interface SchoolSyncConfigService extends IService<SchoolSyncConfig> {

    /**
     * 分页查询同步配置
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 同步配置分页列表
     */
    IPage<SchoolSyncConfig> pageSyncConfig(Integer pageNum, Integer pageSize);

    /**
     * 获取同步配置详情
     *
     * @param id 配置ID
     * @return 同步配置详情
     */
    SchoolSyncConfig getSyncConfigDetail(Long id);

    /**
     * 保存同步配置
     *
     * @param config 同步配置
     * @return 是否保存成功
     */
    boolean saveSyncConfig(SchoolSyncConfig config);

    /**
     * 删除同步配置
     *
     * @param id 配置ID
     * @return 是否删除成功
     */
    boolean deleteSyncConfig(Long id);

    /**
     * 手动执行同步
     *
     * @param id 配置ID
     * @return 是否执行成功
     */
    boolean executeSync(Long id);

    /**
     * 手动同步指定学校
     *
     * @param schoolId 高考网学校ID
     * @return 是否同步成功
     */
    boolean syncSchool(String schoolId);
} 