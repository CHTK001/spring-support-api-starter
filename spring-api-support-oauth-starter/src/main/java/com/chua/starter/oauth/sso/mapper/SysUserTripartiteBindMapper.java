package com.chua.starter.oauth.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.oauth.sso.impl.SysUserTripartiteBind;
import com.chua.starter.entity.system.SysUser;
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