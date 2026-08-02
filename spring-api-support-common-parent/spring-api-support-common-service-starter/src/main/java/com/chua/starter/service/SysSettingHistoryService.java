package com.chua.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.service.SysSettingHistory;
import com.chua.starter.mybatis.entity.Query;

import java.util.List;

/**
 * 系统配置变更历史服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface SysSettingHistoryService extends IService<SysSettingHistory> {

    /**
     * 记录配置新增历史
     *
     * @param sysSetting   新增的配置
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @param batchNo      批次号
     */
    void recordCreate(SysSetting sysSetting, String operatorId, String operatorName, String batchNo);

    /**
     * 记录配置更新历史
     *
     * @param oldSetting   旧配置
     * @param newSetting   新配置
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @param batchNo      批次号
     */
    void recordUpdate(SysSetting oldSetting, SysSetting newSetting, String operatorId, String operatorName, String batchNo);

    /**
     * 记录配置删除历史
     *
     * @param sysSetting   删除的配置
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @param batchNo      批次号
     */
    void recordDelete(SysSetting sysSetting, String operatorId, String operatorName, String batchNo);

    /**
     * 记录配置导入历史（新增）
     *
     * @param settings     导入的配置列表
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @param batchNo      批次号
     */
    void recordImport(List<SysSetting> settings, String operatorId, String operatorName, String batchNo);

    /**
     * 记录配置导入更新历史（覆盖更新）
     *
     * @param oldSetting   旧配置
     * @param newSetting   新配置
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @param batchNo      批次号
     */
    void recordImportUpdate(SysSetting oldSetting, SysSetting newSetting, String operatorId, String operatorName, String batchNo);

    /**
     * 分页查询变更历史
     *
     * @param query     分页参数
     * @param settingId 配置ID（可选）
     * @param group     配置分组（可选）
     * @return 分页结果
     */
    IPage<SysSettingHistory> pageHistory(Query<SysSettingHistory> query, Integer settingId, String group);

    /**
     * 根据批次号查询变更历史
     *
     * @param batchNo 批次号
     * @return 变更历史列表
     */
    List<SysSettingHistory> listByBatchNo(String batchNo);

    /**
     * 回滚到指定批次
     *
     * @param batchNo      批次号
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @return 回滚结果
     */
    ReturnResult<Boolean> rollbackToBatch(String batchNo, String operatorId, String operatorName);

    /**
     * 回滚单条配置到指定历史记录
     *
     * @param historyId    历史记录ID
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @return 回滚结果
     */
    ReturnResult<Boolean> rollbackToHistory(Long historyId, String operatorId, String operatorName);
}
