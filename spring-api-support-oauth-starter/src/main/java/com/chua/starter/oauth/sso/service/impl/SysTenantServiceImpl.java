package com.chua.starter.oauth.sso.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.oauth.sso.mapper.SysTenantMapper;
import com.chua.starter.system.entity.SysTenant;
import com.chua.starter.oauth.sso.service.SysTenantService;
@Service
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantService{

}
