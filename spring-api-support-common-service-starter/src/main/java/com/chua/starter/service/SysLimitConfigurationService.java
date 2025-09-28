package com.chua.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysLimitConfiguration;

import java.util.List;
import java.util.Set;

/**
 * 限流配置服务接口
 * 
 * @author CH
 * @since 2025/9/28
 */
public interface SysLimitConfigurationService extends IService<SysLimitConfiguration> {
    /**
     * 根据条件查询限流配置列表
     *
     * @param sysLimitConfiguration 查询条件对象，用于筛选限流配置
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 限流配置列表
     */
    List<SysLimitConfiguration> list(SysLimitConfiguration sysLimitConfiguration, Set<String> roles);

    /**
     * 删除指定的限流配置
     *
     * @param limitConfigurationId 需要删除的限流配置的ID
     * @return 删除结果，包含成功与否的标志和消息
     */
    ReturnResult<Boolean> delete(Long limitConfigurationId);

    /**
     * 更新限流配置信息
     *
     * @param roles 当前用户的角色集合，用于权限控制
     * @param sysLimitConfiguration 需要更新的限流配置信息
     * @return 更新结果，包含成功与否的标志和消息
     */
    ReturnResult<Boolean> update(Set<String> roles, SysLimitConfiguration sysLimitConfiguration);

    /**
     * 保存一个新的限流配置
     *
     * @param sysLimitConfiguration 待保存的限流配置对象
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 保存结果，包含成功与否的标志和消息以及保存的实体
     */
    ReturnResult<SysLimitConfiguration> saveForEntity(SysLimitConfiguration sysLimitConfiguration, Set<String> roles);

    /**
     * 分页查询限流配置
     *
     * @param query 分页查询对象，包含分页参数和排序信息
     * @param sysLimitConfiguration 查询条件对象，用于筛选限流配置
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 分页结果，包含分页数据和相关分页信息
     */
    IPage<SysLimitConfiguration> pageForLimitConfiguration(Query<SysLimitConfiguration> query, SysLimitConfiguration sysLimitConfiguration, Set<String> roles);

    /**
     * 批量更新限流配置
     *
     * @param roles 当前用户的角色集合，用于权限控制
     * @param sysLimitConfiguration 需要更新的限流配置列表
     * @return 更新结果，包含成功与否的标志和消息
     */
    ReturnResult<Boolean> updateBatch(Set<String> roles, List<SysLimitConfiguration> sysLimitConfiguration);

    /**
     * 根据接口路径获取限流配置
     *
     * @param path 接口路径
     * @return 限流配置列表
     */
    List<SysLimitConfiguration> getLimitConfigurationsByPath(String path);
}