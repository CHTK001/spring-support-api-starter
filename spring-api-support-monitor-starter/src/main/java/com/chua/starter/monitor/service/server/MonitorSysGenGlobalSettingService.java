package com.chua.starter.monitor.service.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenGlobalSetting;

/**
 * 全局配置设置Service接口
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
public interface MonitorSysGenGlobalSettingService extends IService<MonitorSysGenGlobalSetting> {

    /**
     * 获取全局配置（系统只有一条全局配置记录）
     *
     * @return 全局配置
     */
    ReturnResult<MonitorSysGenGlobalSetting> getGlobalSetting();

    /**
     * 保存或更新全局配置
     *
     * @param setting 全局配置
     * @return 操作结果
     */
    ReturnResult<MonitorSysGenGlobalSetting> saveOrUpdateGlobalSetting(MonitorSysGenGlobalSetting setting);

    /**
     * 检查全局调度器是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isGlobalDispatcherEnabled();

    /**
     * 检查服务器状态汇总推送是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isStatusSummaryEnabled();

    /**
     * 检查性能趋势推送是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isPerformanceTrendsEnabled();

    /**
     * 检查连接统计推送是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isConnectionStatsEnabled();

    /**
     * 检查健康状态推送是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isHealthStatusEnabled();

    /**
     * 检查全局告警处理是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isGlobalAlertEnabled();

    /**
     * 检查无效任务清理是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isCleanupEnabled();

    /**
     * 检查配置刷新是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isConfigRefreshEnabled();

    /**
     * 检查文件上传检查是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isFileUploadCheckEnabled();

    /**
     * 检查数据清理是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isDataCleanupEnabled();

    /**
     * 检查备份任务是否启用
     *
     * @return true:启用 false:禁用
     */
    boolean isBackupEnabled();
}
