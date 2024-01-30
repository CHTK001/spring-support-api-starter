package com.chua.starter.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.admin.entity.SysUserTripartiteBind;

import java.util.List;

public interface SysUserTripartiteBindService extends IService<SysUserTripartiteBind>{


    /**
     * 列表绑定
     *
     * @param userId 用户id
     * @return {@link List}<{@link SysUserTripartiteBind}>
     */
    List<SysUserTripartiteBind> listBind(Long userId);

    /**
     * 解绑
     *
     * @param userId           用户id
     * @param tripartiteBindId 三方绑定id
     * @return {@link List}<{@link SysUserTripartiteBind}>
     */
    boolean unBinder(Long userId, Long tripartiteBindId);
}
