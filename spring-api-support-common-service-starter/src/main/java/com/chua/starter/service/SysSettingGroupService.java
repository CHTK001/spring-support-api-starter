package com.chua.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysSettingGroup;
import lombok.NonNull;

import java.util.List;
import java.util.Set;

/**
 * 配置组服务接口
 *
 * @author CH
 * @since 2025/9/10 16:26
 */
public interface SysSettingGroupService extends IService<SysSettingGroup> {

    /**
     * 保存或更新配置组信息
     *
     * @param sysSettingGroup 配置组实体对象，包含配置组的详细信息。
     *                        示例：new SysSettingGroup().setSysSettingGroupName("系统配置")
     * @param roles           用户角色集合，用于权限校验。
     *                        示例：Set.of("admin", "user")
     * @return 返回操作结果，包含保存或更新后的配置组信息。
     * 成功时返回 {@link ReturnResult#ok(Object)}，
     * 失败时返回 {@link ReturnResult#illegal(String)}
     */
    ReturnResult<Boolean> saveOrUpdateForGroup(@NonNull SysSettingGroup sysSettingGroup, @NonNull Set<String> roles);

    /**
     * 删除指定ID的配置组
     *
     * @param sysSettingGroupMapperId 配置组主键ID。
     *                                示例：1L
     * @return 返回操作结果，布尔值表示是否删除成功。
     * 成功时返回 {@link ReturnResult#ok(Object)}，
     * 失败时返回 {@link ReturnResult#illegal(String)}
     */
    ReturnResult<Boolean> deleteForGroup(@NonNull Long sysSettingGroupMapperId);

    /**
     * 根据配置组获取配置列表
     *
     * @param sysSettingGroup 配置组实体对象，用于筛选配置。
     *                        示例：new SysSettingGroup().setSysSettingGroupCode("system")
     * @param roles           用户角色集合，用于权限过滤。
     *                        示例：Set.of("admin", "user")
     * @return 返回配置列表，根据角色和配置组进行筛选。
     * 成功时返回配置列表，
     * 失败时返回空列表或抛出异常
     */
    List<SysSettingGroup> listForSettingGroup(@NonNull SysSettingGroup sysSettingGroup, @NonNull Set<String> roles);

    /**
     * 批量更新配置组信息
     *
     * @param sysSettingGroups 配置组列表，包含多个配置组信息。
     *                         示例：List.of(new SysSettingGroup().setSysSettingGroupName("系统配置"))
     * @param roles            用户角色集合，用于权限校验。
     *                         示例：Set.of("admin", "user")
     * @return 批量更新结果，包含更新后的配置组列表。
     * 批量更新成功时返回 {@link ReturnResult#ok(Object)}，
     */
    ReturnResult<Boolean> batchUpdate(List<SysSettingGroup> sysSettingGroups, Set<String> roles);
}
