package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.entity.system.SysMessageTemplate;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysMessageTemplateMapper;
import com.chua.starter.system.service.SysMessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
* 消息模板服务实现
*
* @since 4.0.0.50
* @author CH
 */
@Service
@RequiredArgsConstructor
public class SysMessageTemplateServiceImpl extends ServiceImpl<SysMessageTemplateMapper, SysMessageTemplate>
        implements SysMessageTemplateService {

    @Override
    /** 分页模板 */
    public IPage<SysMessageTemplate> pageForTemplate(Query<SysMessageTemplate> query, SysMessageTemplate template) {
        SysMessageTemplate c = template == null ? new SysMessageTemplate() : template;
        return page(query.createPage(), Wrappers.<SysMessageTemplate>lambdaQuery()
                .like(StringUtils.isNotBlank(c.getSysMessageTemplateName()), SysMessageTemplate::getSysMessageTemplateName, c.getSysMessageTemplateName())
                .like(StringUtils.isNotBlank(c.getSysMessageTemplateCode()), SysMessageTemplate::getSysMessageTemplateCode, c.getSysMessageTemplateCode())
                .eq(null != c.getSysMessageTemplateStatus(), SysMessageTemplate::getSysMessageTemplateStatus, c.getSysMessageTemplateStatus())
                .orderByDesc(SysMessageTemplate::getSysMessageTemplateId)
        );
    }

    @Override
    /** 保存模板 */
    public ReturnResult<SysMessageTemplate> saveTemplate(SysMessageTemplate template) {
        SysMessageTemplate exist = getOne(Wrappers.<SysMessageTemplate>lambdaQuery()
                .eq(SysMessageTemplate::getSysMessageTemplateCode, template.getSysMessageTemplateCode()));
        if (null != exist) {
            return ReturnResult.error("模板编码已存在");
        }
        if (null == template.getSysMessageTemplateStatus()) {
            template.setSysMessageTemplateStatus(1);
        }
        if (baseMapper.insert(template) > 0) {
            return ReturnResult.ok(template);
        }
        return ReturnResult.error("新增模板失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /** 更新模板 */
    public ReturnResult<Boolean> updateTemplate(SysMessageTemplate template) {
        if (null == template.getSysMessageTemplateId()) {
            return ReturnResult.error("模板ID不能为空");
        }
        return ReturnResult.of(baseMapper.updateById(template) > 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /** 删除模板 */
    public ReturnResult<Boolean> deleteTemplate(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return ReturnResult.illegal("模板ID不能为空");
        }
        return ReturnResult.of(baseMapper.deleteBatchIds(ids) > 0);
    }

    @Override
    /** 渲染模板 */
    public String[] renderByCode(String templateCode, Map<String, String> variables) {
        if (StringUtils.isBlank(templateCode)) {
            return null;
        }
        SysMessageTemplate template = getOne(Wrappers.<SysMessageTemplate>lambdaQuery()
                .eq(SysMessageTemplate::getSysMessageTemplateCode, templateCode)
                .eq(SysMessageTemplate::getSysMessageTemplateStatus, 1));
        if (null == template) {
            return null;
        }
        String title = render(template.getSysMessageTemplateTitle(), variables);
        String content = render(template.getSysMessageTemplateContent(), variables);
        return new String[]{title, content};
    }

    /**
    * 简单变量替换: {key} -> variables[key]
     */
    private String render(String text, Map<String, String> variables) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        if (variables == null || variables.isEmpty()) {
            return text;
        }
        String result = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}",
                    entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }
}
