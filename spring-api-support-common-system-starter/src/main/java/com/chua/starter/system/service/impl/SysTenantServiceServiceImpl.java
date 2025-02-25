package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.entity.SysTenantService;
import com.chua.starter.system.mapper.SysTenantServiceMapper;
import com.chua.starter.system.service.SysTenantServiceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysTenantServiceServiceImpl extends ServiceImpl<SysTenantServiceMapper, SysTenantService> implements SysTenantServiceService {

    @Override
    public List<SysMenu> getMenuIds(Integer sysTenantId) {
        return baseMapper.getMenuByTenantId(sysTenantId);
    }
}
