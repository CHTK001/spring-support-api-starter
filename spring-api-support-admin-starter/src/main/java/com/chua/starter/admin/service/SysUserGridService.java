package com.chua.starter.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.admin.entity.SysUserGrid;

/**
 * @author CH
 */
public interface SysUserGridService extends IService<SysUserGrid> {

    /**
     * 保存布局
     *
     * @param sysUserGrid sysUserGrid
     * @return 保存布局
     */
    boolean saveGrid(SysUserGrid sysUserGrid);

    /**
     * 获取布局
     *
     * @param userId userId
     * @return 获取布局
     */
    SysUserGrid getGridByUserId(String userId);
}
