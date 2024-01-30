package com.chua.starter.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.sso.impl.SysUser;
import com.chua.starter.sso.impl.SysUserTripartiteBind;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserTripartiteBindMapper extends BaseMapper<SysUserTripartiteBind> {
    /**
     * 获取系统用户
     *
     * @param uuid      uuid
     * @param loginType 登录类型
     * @return boolean
     */
    SysUser getSysUser(@Param("uuid")String uuid, @Param("loginType") String loginType);
}