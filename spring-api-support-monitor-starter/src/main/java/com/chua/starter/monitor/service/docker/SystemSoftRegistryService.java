package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftRegistry;

import java.util.List;

/**
 * 系统软件镜像仓库服务接口
 *
 * @author CH
 * @since 2025/01/15
 * @version 1.0.0
 */
public interface SystemSoftRegistryService extends IService<SystemSoftRegistry> {

    /**
     * 测试仓库连接
     *
     * @param registryId 仓库ID
     * @return 连接测试结果
     */
    ReturnResult<Boolean> testConnection(Integer registryId);

    /**
     * 测试仓库连接(ForRegistry)
     *
     * @param registryId 仓库ID
     * @return 连接测试结果
     */
    default ReturnResult<Boolean> testConnectionForRegistry(Integer registryId) {
        return testConnection(registryId);
    }

    /**
     * 设置默认仓库
     *
     * @param registryId 仓库ID
     * @return 设置结果
     */
    ReturnResult<Boolean> setDefault(Integer registryId);

    /**
     * 设置默认仓库(ForRegistry)
     *
     * @param registryId 仓库ID
     * @return 设置结果
     */
    default ReturnResult<Boolean> setDefaultForRegistry(Integer registryId) {
        return setDefault(registryId);
    }

    /**
     * 批量更新仓库状态
     *
     * @param registryIds 仓库ID列表
     * @param status      状态
     * @return 更新结果
     */
    ReturnResult<Boolean> batchUpdateStatus(List<Integer> registryIds, Integer status);

    /**
     * 获取默认仓库
     *
     * @return 默认仓库信息
     */
    ReturnResult<SystemSoftRegistry> getDefaultRegistry();

    /**
     * 根据服务器获取默认仓库
     *
     * @param serverId 服务器ID
     * @return 默认仓库信息
     */
    ReturnResult<SystemSoftRegistry> getDefaultRegistryByServerId(Integer serverId);

    /**
     * 获取所有启用的仓库
     *
     * @return 启用的仓库列表
     */
    ReturnResult<List<SystemSoftRegistry>> getEnabledRegistries();

    /**
     * 根据类型获取仓库列表
     *
     * @param registryType 仓库类型
     * @return 仓库列表
     */
    ReturnResult<List<SystemSoftRegistry>> getRegistriesByType(String registryType);

    /**
     * 验证仓库配置
     *
     * @param registry 仓库信息
     * @return 验证结果
     */
    ReturnResult<String> validateRegistryConfig(SystemSoftRegistry registry);

    /**
     * 同步仓库信息
     *
     * @param registryId 仓库ID
     * @return 同步结果
     */
    ReturnResult<Boolean> syncRegistry(Integer registryId);

    /**
     * 同步仓库信息(ForRegistry)
     *
     * @param registryId 仓库ID
     * @return 同步结果
     */
    default ReturnResult<Boolean> syncRegistryForRegistry(Integer registryId) {
        return syncRegistry(registryId);
    }
}
