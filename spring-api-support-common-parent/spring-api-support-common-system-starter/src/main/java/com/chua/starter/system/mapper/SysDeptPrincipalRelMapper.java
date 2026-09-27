package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.entity.system.SysDeptPrincipalRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门负责人关系 映射器
 *
 * @since 2026/4/5
 * @author CH
 */
@Mapper
public interface SysDeptPrincipalRelMapper extends BaseMapper<SysDeptPrincipalRel> {

    /**
     * 按用户查询其负责的部门范围。
     *
     * <p>该结果是数据权限过滤的输入，自身不能再被数据权限过滤，
     * 否则会与当前用户解析互相回调形成无限递归。</p>
     *
     * @param userId 用户ID，不允许为 null
     * @return 结果列表，无数据时为空列表
     */
    @InterceptorIgnore(dataPermission = "true")
    List<SysDept> selectManagedDeptScopesByUserId(@Param("userId") Integer userId);
}
