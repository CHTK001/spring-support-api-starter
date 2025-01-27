package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.service.SysEmailTemplateService;
import com.chua.starter.service.mapper.SysEmailTemplateMapper;
import com.chua.starter.system.entity.SysEmailTemplate;
import org.springframework.stereotype.Service;
@Service
public class SysEmailTemplateServiceImpl extends ServiceImpl<SysEmailTemplateMapper, SysEmailTemplate> implements SysEmailTemplateService{

}
