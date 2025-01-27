package com.chua.starter.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.system.entity.SysSmsTemplate;
import com.chua.starter.service.mapper.SysSmsTemplateMapper;
import com.chua.starter.service.SysSmsTemplateService;
@Service
public class SysSmsTemplateServiceImpl extends ServiceImpl<SysSmsTemplateMapper, SysSmsTemplate> implements SysSmsTemplateService{

}
