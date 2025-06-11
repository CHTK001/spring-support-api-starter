package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SoftServiceInstall;
import com.chua.starter.monitor.pojo.SoftServiceStatusInfo;

import java.util.List;

/**
 * 软件服务安装记录服务接口
 *
 * @author CH
 * @since 2024/7/18
 */
public interface SoftServiceInstallService extends IService<SoftServiceInstall> {

    /**
     * 添加软件服务安装记录
     *
     * @param softServiceInstall 软件服务安装记录
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> addInstall(SoftServiceInstall softServiceInstall);
    
    /**
     * 更新软件服务安装记录
     *
     * @param softServiceInstall 软件服务安装记录
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> updateInstall(SoftServiceInstall softServiceInstall);

    /**
     * 根据ID删除软件服务安装记录
     *
     * @param id 软件服务安装记录ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> removeInstallById(Integer id);

    /**
     * 根据ID获取软件服务安装记录
     *
     * @param id 软件服务安装记录ID
     * @return 软件服务安装记录
     */
    SoftServiceInstall getInstallById(Integer id);

    /**
     * 根据SSH ID获取软件服务安装记录列表
     *
     * @param sshId SSH ID
     * @return 软件服务安装记录列表
     */
    List<SoftServiceInstall> listInstallBySshId(String sshId);

    /**
     * 根据软件服务ID获取软件服务安装记录列表
     *
     * @param softServiceId 软件服务ID
     * @return 软件服务安装记录列表
     */
    List<SoftServiceInstall> listInstallBySoftServiceId(Integer softServiceId);

    /**
     * 根据SSH ID和软件服务ID获取软件服务安装记录
     *
     * @param sshId         SSH ID
     * @param softServiceId 软件服务ID
     * @return 软件服务安装记录
     */
    SoftServiceInstall getInstallBySshIdAndSoftServiceId(String sshId, Integer softServiceId);

    /**
     * 批量安装软件服务
     *
     * @param softServiceId 软件服务ID
     * @param sshIds        SSH ID列表
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> batchInstall(Integer softServiceId, List<String> sshIds);

    /**
     * 开始安装软件
     *
     * @param softServiceInstall 软件服务安装记录
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> startInstall(SoftServiceInstall softServiceInstall);

    /**
     * 停止安装软件
     *
     * @param installId 安装记录ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> stopInstall(Integer installId);

    /**
     * 卸载软件
     *
     * @param installId 安装记录ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> uninstall(Integer installId);

    /**
     * 启动服务
     *
     * @param installId 安装记录ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> startService(Integer installId);

    /**
     * 停止服务
     *
     * @param installId 安装记录ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> stopService(Integer installId);

    /**
     * 重启服务
     *
     * @param installId 安装记录ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> restartService(Integer installId);

    /**
     * 查看服务日志
     *
     * @param installId 安装记录ID
     * @param lines     要查看的行数，默认100行
     * @return 操作结果，包含日志内容
     */
    ReturnResult<String> viewServiceLog(Integer installId, Integer lines);

    /**
     * 开始实时监控服务日志
     *
     * @param installId 安装记录ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> startMonitorServiceLog(Integer installId);

    /**
     * 停止实时监控服务日志
     *
     * @param installId 安装记录ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> stopMonitorServiceLog(Integer installId);

    /**
     * 检测服务状态
     *
     * @param installId 安装记录ID
     * @return 操作结果，包含服务状态信息
     */
    ReturnResult<SoftServiceStatusInfo> checkServiceStatus(Integer installId);

    /**
     * 批量检测服务状态
     *
     * @param installIds 安装记录ID列表
     * @return 操作结果，包含服务状态信息列表
     */
    ReturnResult<List<SoftServiceStatusInfo>> batchCheckServiceStatus(List<Integer> installIds);

    /**
     * 更新服务状态检查命令
     *
     * @param installId   安装记录ID
     * @param command     状态检查命令
     * @param commandType 命令类型（0:单行命令，1:脚本）
     * @param successFlag 成功标识
     * @return 结果
     */
    ReturnResult<Boolean> updateStatusCheckCommand(Integer installId, String command, Integer commandType, String successFlag);
} 