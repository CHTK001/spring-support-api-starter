package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SoftServiceInstall;

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
} 