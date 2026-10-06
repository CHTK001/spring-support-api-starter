package com.chua.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.service.dto.setting.SysSettingThirdPartyRequest;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.service.pojo.setting.SysSettingUpdate;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.request.AuthRequest;

import java.util.List;
import java.util.Set;

/**
 * 系统全局配置
 * @since 2024/8/7
 * @author CH
 */
public interface SysSettingService extends IService<SysSetting> {
    String HOT_RELOAD_GROUP = "hot_reload";

    String SETTING_LIST_CACHE_KEY_LEGACY_PREFIX = "sys:setting:";
    String SETTING_LIST_CACHE_KEY_PREFIX = "sys:setting:v2:";

    /**
     * 面向前端下发的"用户参数"分组：这些分组的配置通过 /default 接口返回给前端，
     * 直接影响登录页与用户界面；其余分组为后端运行/运维使用的"系统参数"。
     */
    List<String> USER_FACING_GROUPS = List.of("default", "sso", "default-1", "config");
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
     * @param settingId 需要删除的系统设置的标识
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
    ReturnResult<SysSetting> saveForEntity(SysSetting sysSetting, Set<String> roles, String operatorId, String operatorName);

    ReturnResult<Boolean> saveHotReload(SysSetting sysSetting);

    /**
     * 删除热更新配置（限 {@code hot_reload} 分组，并清该编码的值缓存）。
     *
     * @param settingId 配置主键
     * @return 是否成功
     */
    ReturnResult<Boolean> deleteHotReload(Long settingId);

    ReturnResult<Boolean> saveHotReload(SysSetting sysSetting, Set<String> runtimeConsumers);

    /**
     * 清除缓存
     *
     * @param sysSetting 需要清除缓存的系统设置对象
     * @return 清除结果，包含成功与否的标志和消息
     */
    SysSetting clearCache(SysSetting sysSetting);

    void clearHotReloadCache();

    /**
     * 分页查询系统设置
     *
     * @param query 分页查询对象，包含分页参数和排序信息
     * @param sysSetting 查询条件对象，用于筛选系统设置
     * @param scope 参数类别：user=仅用户参数(前端下发)，system=仅系统参数，null/空=全部
     * @param roles 当前用户的角色集合，用于权限控制
     * @return 分页结果，包含分页数据和相关分页信息
     */
    IPage<SysSetting> pageForSetting(Query<SysSetting> query, SysSetting sysSetting, String scope, Set<String> roles);

    /**
     * 批量更新系统设置
     *
     * @param roles 当前用户的角色集合，用于权限控制
     * @param sysSetting 需要更新的系统设置列表
     * @return 更新结果，包含成功与否的标志和消息
     */
    ReturnResult<Boolean> updateBatch(Set<String> roles, List<SysSetting> sysSetting);

    /**
     * 获取第三方登录请求
     *
     * @param loginData 第三方登录数据对象，包含第三方登录类型和回调URL
     * @return 第三方登录请求对象
     */
    SysSettingThirdPartyRequest<AuthRequest, AuthStateCache> getAuthRequest(ThirdLoginData loginData);

    /**
     * 查询已开启的三方登录来源（登录页与绑定面板共用同一配置源）。
     *
     * <p>仅返回 {@code sso} 分组下值为 {@code true} 的开关名（小写 loginType），不含任何密钥。</p>
     *
     * @return 已开启的 loginType 列表（小写）
     */
    List<String> listEnabledThirdPartyProviders();

    /**
     * 导出配置为JSON
     *
     * @param group 配置分组（可选，为空则导出全部）
     * @return 配置JSON字符串
     */
    ReturnResult<String> exportSettings(String group);

    /**
     * 导入配置
     *
     * @param json 配置JSON字符串
     * @param overwrite 是否覆盖已存在的配置
     * @param roles 当前用户的角色集合
     * @return 导入结果，返回批次号用于回滚
     */
    ReturnResult<String> importSettings(String json, boolean overwrite, Set<String> roles);

    /**
     * 获取敏感配置的明文值（需专属权限）
     *
     * @param settingId 配置标识
     * @return 明文配置值
     */
    ReturnResult<String> getPlainValue(Integer settingId);
}
