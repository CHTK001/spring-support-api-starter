package com.chua.starter.admin.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.admin.entity.SysMessageTemplate;
import com.chua.starter.admin.mapper.SysMessageTemplateMapper;
import com.chua.starter.admin.service.SysMessageTemplateService;
@Service
public class SysMessageTemplateServiceImpl extends ServiceImpl<SysMessageTemplateMapper, SysMessageTemplate> implements SysMessageTemplateService{

    @Override
    public boolean delete(String messageId) {
        return baseMapper.deleteById(messageId) > 0;
    }
}
