package com.chua.starter.service.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.service.system.mapper.SysTenantMapper;
import com.chua.starter.service.system.service.SysTenantService;
import com.chua.starter.system.entity.SysTenant;
import org.springframework.stereotype.Service;

@Service
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantService {

}
