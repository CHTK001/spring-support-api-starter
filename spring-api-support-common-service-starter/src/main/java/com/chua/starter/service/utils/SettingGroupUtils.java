package com.chua.starter.service.utils;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.service.SysSettingGroupService;
import com.chua.starter.system.entity.SysSettingGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 配置组工具类
 *
 * @author CH
 * @since 2025/9/28
 */
@Component
@RequiredArgsConstructor
public class SettingGroupUtils {

    private final SysSettingGroupService sysSettingGroupService;

    /**
     * 检查指定编码的配置组是否使用项目接口
     *
     * @param groupCode 配置组编码
     * @return true: 使用项目接口, false: 不使用项目接口
     */
    public boolean isUseProjectInterface(String groupCode) {
        if (groupCode == null || groupCode.isEmpty()) {
            return true; // 默认使用项目接口
        }

        List<SysSettingGroup> settingGroups = sysSettingGroupService.list(Wrappers.<SysSettingGroup>lambdaQuery()
                .eq(SysSettingGroup::getSysSettingGroupCode, groupCode));

        if (settingGroups.isEmpty()) {
            return true; // 默认使用项目接口
        }

        SysSettingGroup settingGroup = settingGroups.get(0);
        return settingGroup.getSysSettingGroupUseProjectInterface() == null || settingGroup.getSysSettingGroupUseProjectInterface();
    }

    /**
     * 检查email配置组是否使用项目接口
     *
     * @return true: 使用项目接口, false: 不使用项目接口
     */
    public boolean isEmailUseProjectInterface() {
        return isUseProjectInterface("email");
    }

    /**
     * 检查sms配置组是否使用项目接口
     *
     * @return true: 使用项目接口, false: 不使用项目接口
     */
    public boolean isSmsUseProjectInterface() {
        return isUseProjectInterface("sms");
    }
}