package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author CH
 * @since 2024/8/7
 */
@Service
@Slf4j
public class SimpleSysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SimpleSysUserService {

}
