package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.entity.system.SysMessageTemplate;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysMessageTemplateMapper;
import com.chua.starter.system.pojo.message.TemplatePreviewRequest;
import com.chua.starter.system.pojo.message.TemplatePreviewResponse;
import com.chua.starter.system.service.SysMessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息模板服务实现
 *
 * <p>本类是 {@code {key}} / {@code ${key}} 占位符语义的唯一实现者，
 * 发送服务需要渲染模板时统一调用 {@link #render(String, Map)}。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class SysMessageTemplateServiceImpl extends ServiceImpl<SysMessageTemplateMapper, SysMessageTemplate>
        implements SysMessageTemplateService {

    /**
     * 占位符：${key} 与 {key} 两种写法等价，键名允许字母、数字、下划线与点
     */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$?\\{([\\w.]+)}");

    @Override
    /**
     * 分页模板
    */
    public IPage<SysMessageTemplate> pageForTemplate(Query<SysMessageTemplate> query, SysMessageTemplate template) {
        SysMessageTemplate c = template == null ? new SysMessageTemplate() : template;
        return page(query.createPage(), Wrappers.<SysMessageTemplate>lambdaQuery()
                .like(StringUtils.isNotBlank(c.getSysMessageTemplateName()), SysMessageTemplate::getSysMessageTemplateName, c.getSysMessageTemplateName())
                .like(StringUtils.isNotBlank(c.getSysMessageTemplateCode()), SysMessageTemplate::getSysMessageTemplateCode, c.getSysMessageTemplateCode())
                .eq(StringUtils.isNotBlank(c.getSysMessageTemplateType()), SysMessageTemplate::getSysMessageTemplateType, c.getSysMessageTemplateType())
                .eq(StringUtils.isNotBlank(c.getSysMessageChannel()), SysMessageTemplate::getSysMessageChannel, c.getSysMessageChannel())
                .eq(null != c.getSysMessageTemplateStatus(), SysMessageTemplate::getSysMessageTemplateStatus, c.getSysMessageTemplateStatus())
                .orderByAsc(SysMessageTemplate::getSysMessageTemplateSort)
                .orderByDesc(SysMessageTemplate::getSysMessageTemplateId)
        );
    }

    @Override
    /**
     * 列表模板
     */
    public List<SysMessageTemplate> listForTemplate() {
        return list(Wrappers.<SysMessageTemplate>lambdaQuery()
                .eq(SysMessageTemplate::getSysMessageTemplateStatus, 1)
                .orderByAsc(SysMessageTemplate::getSysMessageTemplateSort)
                .orderByDesc(SysMessageTemplate::getSysMessageTemplateId));
    }

    @Override
    /**
     * 保存模板
    */
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
    /**
     * 更新模板
    */
    public ReturnResult<Boolean> updateTemplate(SysMessageTemplate template) {
        if (null == template.getSysMessageTemplateId()) {
            return ReturnResult.error("模板ID不能为空");
        }
        // 模板编码唯一性校验 (排除自身)
        if (StringUtils.isNotBlank(template.getSysMessageTemplateCode())) {
            SysMessageTemplate exist = getOne(Wrappers.<SysMessageTemplate>lambdaQuery()
                    .eq(SysMessageTemplate::getSysMessageTemplateCode, template.getSysMessageTemplateCode())
                    .ne(SysMessageTemplate::getSysMessageTemplateId, template.getSysMessageTemplateId()));
            if (null != exist) {
                return ReturnResult.error("模板编码已存在");
            }
        }
        return ReturnResult.of(baseMapper.updateById(template) > 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 删除模板
    */
    public ReturnResult<Boolean> deleteTemplate(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return ReturnResult.illegal("模板ID不能为空");
        }
        if (baseMapper.selectByIds(ids).isEmpty()) {
            return ReturnResult.error("模板不存在");
        }
        return ReturnResult.of(baseMapper.deleteBatchIds(ids) > 0);
    }

    @Override
    /**
     * 预览模板
     */
    public ReturnResult<TemplatePreviewResponse> previewTemplate(TemplatePreviewRequest request) {
        if (null == request || null == request.getSysMessageTemplateId()) {
            return ReturnResult.illegal("模板ID不能为空");
        }
        SysMessageTemplate template = getById(request.getSysMessageTemplateId());
        if (null == template) {
            return ReturnResult.error("模板不存在");
        }
        Map<String, Object> renderParams = new java.util.LinkedHashMap<>();
        renderParams.put("date", java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        renderParams.put("time", java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        if (null != request.getParams()) {
            renderParams.putAll(request.getParams());
        }
        List<String> unresolvedParams = findUnresolved(renderParams, template.getSysMessageTemplateTitle(),
                template.getSysMessageTemplateContent());
        TemplatePreviewResponse response = new TemplatePreviewResponse(
                template.getSysMessageTemplateId(),
                render(template.getSysMessageTemplateTitle(), renderParams),
                render(template.getSysMessageTemplateContent(), renderParams),
                unresolvedParams,
                unresolvedParams.isEmpty(),
                List.of("date", "time"));
        return ReturnResult.ok(response);
    }

    @Override
    /**
     * 渲染模板
    */
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

    @Override
    /**
     * 渲染占位符
     */
    public String render(String text, Map<String, ?> params) {
        if (StringUtils.isBlank(text) || null == params || params.isEmpty()) {
            return text;
        }
        String result = text;
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            String value = null == entry.getValue() ? "" : String.valueOf(entry.getValue());
            // 先替换 ${key}，再替换 {key}，避免残留美元符号
            result = result.replace("${" + entry.getKey() + "}", value)
                    .replace("{" + entry.getKey() + "}", value);
        }
        return result;
    }

    /**
     * 找出标题与内容中未被参数填充的占位符
     *
     * @param texts   待扫描文本
     * @param params  模板变量
     * @return 未填充的占位符键名(去重、保持出现顺序)
     */
    private List<String> findUnresolved(Map<String, ?> params, String... texts) {
        List<String> unresolved = new ArrayList<>();
        for (String text : texts) {
            if (StringUtils.isBlank(text)) {
                continue;
            }
            Matcher matcher = PLACEHOLDER.matcher(text);
            while (matcher.find()) {
                String key = matcher.group(1);
                boolean filled = null != params && params.containsKey(key) && null != params.get(key);
                if (!filled && !unresolved.contains(key)) {
                    unresolved.add(key);
                }
            }
        }
        return unresolved;
    }
}
