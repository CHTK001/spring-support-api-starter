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

    void syncDeptPrincipals(Integer deptId, Collection<Integer> userIds);

    List<SysDept> listManagedDeptScopes(Integer userId);

    List<Integer> listManagedDeptIds(Integer userId);

    List<String> listManagedDeptTreeIds(Integer userId);

    List<String> listManagedDeptTreeIds(Collection<Integer> deptIds);

    void fillPrincipalInfo(List<? extends SysDept> departments);

    Map<Integer, List<Integer>> mapPrincipalUserIds(Collection<Integer> deptIds);
}
