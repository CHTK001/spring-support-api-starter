package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysDept;
import com.chua.starter.system.pojo.dept.SysDeptVO;

import java.util.List;
import java.util.Set;

/**
 * 系统部门服务接口
 * 该接口定义了与系统部门相关的业务操作，包括查询、增删改�?
 * @since 2024/8/7
 * @author CH
 */
public interface SysDeptService extends IService<SysDept>{


    /**
     * 查询部门列表
     * 根据部门查询条件和角色集合，返回符合条件的部门列�?
     * @param sysDept 部门查询条件
     * @param roles 角色集合，用于权限控制或筛�?
     * @return 部门列表，包含每个部门的详细信息
     */
    List<SysDeptVO> list(SysDept sysDept, Set<String> roles);

    /**
     * 删除部门
     * 根据部门ID进行部门删除操作，返回删除结�?
     * @param deptId 部门ID，唯一标识要删除的部门
     * @return 删除结果，包含成功与否的标志和提示信�?
     */
    ReturnResult<Boolean> delete(Long deptId);

    /**
     * 更新部门信息
     * 根据角色集合和部门信息更新部门，返回更新结果
     * @param roles 角色集合，用于权限控制或筛�?
     * @param sysDept 待更新的部门信息
     * @return 更新结果，包含成功与否的标志和提示信�?
     */
    ReturnResult<Boolean> update(Set<String> roles, SysDept sysDept);

    /**
     * 新增部门
     * 根据部门信息和角色集合，新增一个部门，返回新增结果
     * @param sysDept 待新增的部门信息
     * @param roles 角色集合，用于权限控制或筛�?
     * @return 新增结果，包含成功与否的标志和提示信�?
     */
    ReturnResult<SysDept> save(SysDept sysDept, Set<String> roles);
}

