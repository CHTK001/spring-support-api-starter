package com.chua.starter.oauth.sso.service.impl;

import com.chua.starter.service.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.oauth.sso.service.SysUserService;
/**
 * @author yemen
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService{

}
