package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysWechatUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信用户表 Mapper
 *
 * 负责微信用户基础信息的增删改查
 *
 * @since 2026/08/03
 * @author CH
 */
@Mapper
public interface SysWechatUserMapper extends BaseMapper<SysWechatUser> {
}

