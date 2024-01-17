package com.chua.starter.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.admin.entity.SysUserSetting;

import java.util.Set;

/**
 * @author CH
 */
public interface SysUserSettingService extends IService<SysUserSetting> {

    /**
     * 保存布局
     *
     * @param sysUserGrid sysUserGrid
     * @return 保存布局
     */
    boolean saveGrid(SysUserSetting sysUserGrid);

    /**
     * 获取布局
     *
     * @param userId userId
     * @return 获取布局
     */
    SysUserSetting getGridByUserId(String userId);

    /**
     * 保存使用者背景
     *
     * @param sysUserSetting    sys用户设置
     * @param userDashboardGrid 用户面板网格
     * @param userId            用户id
     * @param username          用户名
     * @param roles             角色
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    ReturnResult<Boolean> saveUserSetting(SysUserSetting sysUserSetting, String userDashboardGrid, Long userId, String username, Set<String> roles);
}
