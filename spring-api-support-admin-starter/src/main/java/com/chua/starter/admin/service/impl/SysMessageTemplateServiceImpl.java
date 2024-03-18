package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.admin.entity.SysMessageConfig;
import com.chua.starter.admin.entity.SysMessageTemplateConfig;
import com.chua.starter.admin.service.SysMessageTemplateConfigService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.admin.entity.SysMessageTemplate;
import com.chua.starter.admin.mapper.SysMessageTemplateMapper;
import com.chua.starter.admin.service.SysMessageTemplateService;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class SysMessageTemplateServiceImpl extends ServiceImpl<SysMessageTemplateMapper, SysMessageTemplate> implements SysMessageTemplateService{

    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private SysMessageTemplateConfigService sysMessageTemplateConfigService;

    @Override
    public SysMessageTemplate saveTemplate(SysMessageTemplate template) {
        return transactionTemplate.execute(status -> {
            baseMapper.insert(template);
            List<Integer> config1 = template.getConfig();
            if(CollectionUtils.isNotEmpty(config1)) {
                List<SysMessageTemplateConfig> temp = new ArrayList<>(config1.size());
                for (Integer integer : config1) {
                    SysMessageTemplateConfig item = new SysMessageTemplateConfig();
                    item.setMessageId(template.getMessageId());
                    item.setConfigId(integer);
                    temp.add(item);
                }
                sysMessageTemplateConfigService.saveBatch(temp);

            }
            return template;
        });
    }

    @Override
    public SysMessageTemplate updateTemplate(SysMessageTemplate template) {

        return transactionTemplate.execute(status -> {
            template.setMessageCode(null);
            // 保存消息模板
            baseMapper.updateById(template);
            List<Integer> config1 = template.getConfig();
            if(CollectionUtils.isNotEmpty(config1)) {
                sysMessageTemplateConfigService.remove(Wrappers.<SysMessageTemplateConfig>lambdaQuery().eq(SysMessageTemplateConfig::getMessageId, template.getMessageId()));
                List<SysMessageTemplateConfig> temp = new ArrayList<>(config1.size());
                for (Integer integer : config1) {
                    SysMessageTemplateConfig item = new SysMessageTemplateConfig();
                    item.setMessageId(template.getMessageId());
                    item.setConfigId(integer);
                    temp.add(item);
                }
                sysMessageTemplateConfigService.saveBatch(temp);

            }
            return template;
        });
    }

    @Override
    public Boolean deleteTemplate(String messageId) {
        // 保存消息模板
        return transactionTemplate.execute(status -> {
            // 保存消息模板
            baseMapper.deleteById(messageId);
            sysMessageTemplateConfigService.remove(Wrappers.<SysMessageTemplateConfig>lambdaQuery().eq(SysMessageTemplateConfig::getMessageId, messageId));
            return Boolean.TRUE;
        });
    }
}
