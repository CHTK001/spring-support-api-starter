package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysSetting;
import com.chua.starter.system.pojo.setting.SysSettingUpdate;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.Set;

import static com.chua.starter.system.constant.SystemConstant.CACHE_WITH_REDIS;

/**
 * 系统全局配置
 * @author CH
 * @since 2024/8/7
 */
public interface SysSettingService extends IService<SysSetting> {
    /**
     * 根据条件查询系统设置列表
     *
     * @param sysSettingUpdate 查询条件对象，用于筛选需要更新的系统设置
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 系统设置列表
     */
    List<SysSetting> list(SysSettingUpdate sysSettingUpdate, Set<String> roles);

    /**
     * 删除指定的系统设置
     *
     * @param settingId 需要删除的系统设置的ID
     * @return 删除结果，包含成功与否的标志和消息
     */
    ReturnResult<Boolean> delete(Long settingId);

    /**
     * 更新系统设置信息
     *
     * @param roles 当前用户的角色集合，用于权限控制
     * @param sysSetting 需要更新的系统设置信息
     * @return 更新结果，包含成功与否的标志和消息
     */
    ReturnResult<Boolean> update(Set<String> roles, SysSetting sysSetting);

    /**
     * 保存一个新的系统设置
     *
     * @param sysSetting 待保存的系统设置对象
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 保存结果，包含成功与否的标志和消息以及保存的实体
     */
    ReturnResult<SysSetting> saveForEntity(SysSetting sysSetting, Set<String> roles);
    /**
     * 清除缓存
     *
     * @param sysSetting 需要清除缓存的系统设置对象
     * @return 清除结果，包含成功与否的标志和消息
     */
    @CacheEvict(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:sys:setting:id:' + #sysSetting.sysSettingGroup")
    SysSetting clearCache(SysSetting sysSetting);
    /**
     * 分页查询系统设置
     *
     * @param query 分页查询对象，包含分页参数和排序信息
     * @param sysSetting 查询条件对象，用于筛选系统设置
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 分页结果，包含分页数据和相关分页信息
     */
    IPage<SysSetting> pageForSetting(Query<SysSetting> query, SysSetting sysSetting, Set<String> roles);
}
