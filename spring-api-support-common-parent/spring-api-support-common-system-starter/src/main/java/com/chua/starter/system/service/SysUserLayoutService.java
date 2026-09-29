package com.chua.starter.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.starter.entity.system.SysUserLayout;

import java.util.List;

/**
 * 用户布局偏好服务
 *
 * @since 4.0.0.42
 * @author CH
 */
public interface SysUserLayoutService extends IService<SysUserLayout> {

    /**
     * 查询用户的全部布局偏好。
     *
     * @param userId 用户主键
     * @return 布局列表
     */
    List<SysUserLayout> listByUserId(Integer userId);

    /**
     * 查询用户指定 键 的偏好(layout/grid/组件/preference)。
     *
     * @param userId 用户主键
     * @param layoutKey 布局 键
     * @return 偏好实体
     */
    SysUserLayout getByUserIdAndKey(Integer userId, String layoutKey);

    /**
     * 保存或更新偏好(upsert)。
     *
     * @param layout 偏好对象
     * @return 保存后的对象
     */
    SysUserLayout saveOrUpdateLayout(SysUserLayout layout);

    /**
     * 删除用户的所有布局。
     *
     * @param userId 用户主键
     * @return 删除条数
     */
    int deleteByUserId(Integer userId);
}
