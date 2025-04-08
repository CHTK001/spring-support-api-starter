package com.chua.starter.school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.school.dto.SchoolSyncConfigDTO;
import com.chua.starter.school.entity.SchoolSyncConfig;

/**
 * 学校同步配置Service
 *
 * @author CH
 */
public interface SchoolSyncConfigService extends IService<SchoolSyncConfig> {

    /**
     * 分页查询学校同步配置
     *
     * @param query 查询条件
     * @return 学校同步配置分页列表
     */
    ReturnPageResult<SchoolSyncConfig> pageSyncConfig(SchoolSyncConfigDTO query);

    /**
     * 保存学校同步配置
     *
     * @param schoolSyncConfig 学校同步配置
     * @return 是否保存成功
     */
    ReturnResult<Boolean> saveSyncConfig(SchoolSyncConfig schoolSyncConfig);

    /**
     * 更新学校同步配置
     *
     * @param schoolSyncConfig 学校同步配置
     * @return 是否更新成功
     */
    ReturnResult<Boolean> updateSyncConfig(SchoolSyncConfig schoolSyncConfig);

    /**
     * 删除学校同步配置
     *
     * @param schoolSyncConfigId 学校同步配置ID
     * @return 是否删除成功
     */
    ReturnResult<Boolean> deleteSyncConfig(Long schoolSyncConfigId);

    /**
     * 执行同步
     *
     * @param schoolSyncConfigId 学校同步配置ID
     * @return 是否执行成功
     */
    boolean executeSync(Long schoolSyncConfigId);
}