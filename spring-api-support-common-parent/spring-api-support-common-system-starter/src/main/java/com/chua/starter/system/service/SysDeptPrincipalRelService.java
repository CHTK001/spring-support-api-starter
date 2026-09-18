package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.entity.system.SysDeptPrincipalRel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
* 部门负责人关系服务
*
* @since 2026/4/5
* @author CH
 */
public interface SysDeptPrincipalRelService extends IService<SysDeptPrincipalRel> {

    /**
     * syncDeptPrincipals。
     *
     * @param deptId deptID，不允许为 null
     * @param userIds 用户Ids，不允许为 null
     */
    void syncDeptPrincipals(Integer deptId, Collection<Integer> userIds);

    /**
     * 列出ManagedDeptScopes。
     *
     * @param userId 用户ID，不允许为 null
     * @return 结果列表，无数据时为空列表
     */
    List<SysDept> listManagedDeptScopes(Integer userId);

    /**
     * 列出ManagedDeptIds。
     *
     * @param userId 用户ID，不允许为 null
     * @return 结果列表，无数据时为空列表
     */
    List<Integer> listManagedDeptIds(Integer userId);

    /**
     * 列出ManagedDeptTreeIds。
     *
     * @param userId 用户ID，不允许为 null
     * @return 结果列表，无数据时为空列表
     */
    List<String> listManagedDeptTreeIds(Integer userId);

    /**
     * 列出ManagedDeptTreeIds。
     *
     * @param deptIds 方法入参 deptIds
     * @return 结果列表，无数据时为空列表
     */
    List<String> listManagedDeptTreeIds(Collection<Integer> deptIds);

    /**
     * fillPrincipalInfo。
     *
     * @param departments 方法入参 departments
     */
    void fillPrincipalInfo(List<? extends SysDept> departments);

    /**
     * 映射Principal用户Ids。
     *
     * @param deptIds 方法入参 deptIds
     * @return 结果映射，无数据时为空映射
     */
    Map<Integer, List<Integer>> mapPrincipalUserIds(Collection<Integer> deptIds);
}
