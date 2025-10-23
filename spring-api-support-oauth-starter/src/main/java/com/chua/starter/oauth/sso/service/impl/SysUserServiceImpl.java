package com.chua.starter.oauth.sso.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.oauth.sso.mapper.SysUserMapper;
import com.chua.starter.oauth.sso.service.SysUserService;
/**
 * @author yemen
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService{

}
