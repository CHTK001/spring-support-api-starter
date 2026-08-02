package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysUserTripartiteBind;
import com.chua.starter.entity.system.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户三方绑定 Mapper
 *
 * @author CH
 * @since 1.0.0
 */
@Mapper
public interface SysUserTripartiteBindMapper extends BaseMapper<SysUserTripartiteBind> {
    
    /**
     * 获取系统用户
     *
     * @param uuid      uuid
     * @param loginType 登录类型
     * @return 系统用户
     */
    SysUser getSysUser(@Param("uuid") String uuid, @Param("loginType") String loginType);
}
