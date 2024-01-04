package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.admin.entity.SysRole;
import com.chua.starter.admin.mapper.SysRoleMapper;
import com.chua.starter.admin.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 角色业务实现类
 *
 * @author haoxr
 * @since 2022/6/3
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

}
