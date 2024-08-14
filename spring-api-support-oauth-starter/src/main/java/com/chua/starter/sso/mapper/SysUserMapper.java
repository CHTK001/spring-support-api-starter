package com.chua.starter.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

/**
 * 用户持久层
 *
 * @author haoxr
 * @since 2022/1/14
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 用戶權限
     * @param userId 用户ID
     * @return 结果
     */
    Set<String> selectPermission(Integer userId);

    /**
     * 查詢賬號信息
     * @param accessKey 賬號
     * @return 用户信息
     */
    SysUser selectCustomOne(String accessKey);

    List<SysUser> test();
}
