package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.system.pojo.user.log.SysUserLogQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*
* @since 2024/8/8
* @author CH
 */
@Mapper
public interface SysUserLogMapper extends BaseMapper<SysUserLog> {
    Long countForUserLog(@Param("query") SysUserLogQuery query,
                         @Param("sysUserId") Integer sysUserId,
                         @Param("isSuperAdmin") boolean isSuperAdmin,
                         @Param("isAdmin") boolean isAdmin,
                         @Param("isDeptLeader") boolean isDeptLeader,
                         @Param("managedDeptTreeIds") List<String> managedDeptTreeIds);

    List<SysUserLog> listForUserLog(@Param("query") SysUserLogQuery query,
                                    @Param("sysUserId") Integer sysUserId,
                                    @Param("isSuperAdmin") boolean isSuperAdmin,
                                    @Param("isAdmin") boolean isAdmin,
                                    @Param("isDeptLeader") boolean isDeptLeader,
                                    @Param("managedDeptTreeIds") List<String> managedDeptTreeIds,
                                    @Param("offset") long offset,
                                    @Param("limit") long limit);

    List<SysUserLog> listLatestLoginLogsByUsernames(@Param("usernames") List<String> usernames);

    int deleteForUserLog(@Param("query") SysUserLogQuery query,
                         @Param("sysUserId") Integer sysUserId,
                         @Param("isSuperAdmin") boolean isSuperAdmin,
                         @Param("isAdmin") boolean isAdmin,
                         @Param("isDeptLeader") boolean isDeptLeader,
                         @Param("managedDeptTreeIds") List<String> managedDeptTreeIds);
}
