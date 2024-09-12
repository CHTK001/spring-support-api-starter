package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 根据用户id查询角色
     *
     * @param userIds 用户id
     * @return 角色
     */
    List<SysRole> listByUserId(@Param("userIds") Set<Integer> userIds);
}