package com.chua.starter.admin.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.admin.entity.SysMessageTemplateConfig;
import com.chua.starter.admin.mapper.SysMessageTemplateConfigMapper;
import com.chua.starter.admin.service.SysMessageTemplateConfigService;
@Service
public class SysMessageTemplateConfigServiceImpl extends ServiceImpl<SysMessageTemplateConfigMapper, SysMessageTemplateConfig> implements SysMessageTemplateConfigService{

}
