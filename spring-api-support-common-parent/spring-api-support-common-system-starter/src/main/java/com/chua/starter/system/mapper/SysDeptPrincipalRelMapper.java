package com.chua.starter.system.mapper;

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

    List<SysDept> selectManagedDeptScopesByUserId(@Param("userId") Integer userId);
}
