package com.chua.starter.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.sso.impl.SysUserTripartiteBind;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserTripartiteBindMapper extends BaseMapper<SysUserTripartiteBind> {
    /**
     * 计数
     *
     * @param uuid uuid
     * @return boolean
     */
    boolean count(String uuid);
}