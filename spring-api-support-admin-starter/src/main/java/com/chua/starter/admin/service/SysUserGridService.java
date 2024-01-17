package com.chua.starter.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.admin.entity.SysUserSetting;

/**
 * @author CH
 */
public interface SysUserGridService extends IService<SysUserSetting> {

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
}
