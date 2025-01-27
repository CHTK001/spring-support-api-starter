package com.chua.starter.service.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.common.support.utils.ArrayUtils;
import com.chua.starter.service.EmailService;
import com.chua.starter.service.SysEmailTemplateService;
import com.chua.starter.service.SysProjectService;
import com.chua.starter.service.mapper.SysEmailTemplateMapper;
import com.chua.starter.service.system.service.SysDictItemService;
import com.chua.starter.service.system.service.SysEmailService;
import com.chua.starter.system.entity.SysEmailTemplate;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.email.EmailPageV1Request;
import com.chua.starter.system.pojo.email.EmailRequest;
import com.chua.starter.system.pojo.email.EmailSyncV1Request;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 邮件服务
 * @author CH
 * @since 2024/9/6
 */
@Service
@RequiredArgsConstructor
public class SysEmailServiceImpl implements SysEmailService {

    final EmailService emailService;
    final SysDictItemService sysDictItemService;
    final SysProjectService sysProjectService;
    final SysEmailTemplateMapper sysEmailTemplateMapper;
    final SysEmailTemplateService sysEmailTemplateService;
    @Override
    public ReturnResult<List<PushResult>> send(EmailRequest request) {
        SysEmailTemplate emailTemplate = sysEmailTemplateService.getById(request.getSysEmailTemplateId());
        if(null == emailTemplate) {
            return ReturnResult.error("模板不存在");
        }

        if(ArrayUtils.isEmpty(request.getTarget())) {
            return ReturnResult.error("接收人不能为空");
        }
        SysProject sysProject = sysProjectService.getOne(emailTemplate.getSysProjectId());
        String sysTemplateCode = emailTemplate.getSysEmailTemplateCode();
        ReturnResult<List<PushResult>> send = emailService.send(sysTemplateCode, sysProject, emailTemplate,  request);
        if(!send.isOk()) {
            return send;
        }
        List<PushResult> data = send.getData();
        if(null == data) {
            return send;
        }
        List<PushResult> list = data.stream().filter(it -> !it.isSuccess()).toList();
        if(!list.isEmpty()) {
            if(list.size() == data.size()) {
                return ReturnResult.error("发送失败");
            }
            return ReturnResult.illegal("部分发送失败" + list.stream()
                    .map(PushResult::getTarget).collect(java.util.stream.Collectors.joining(","))
            );
        }
        return send;
    }

    @Override
    public ReturnResult<Boolean> syncTemplate(EmailSyncV1Request request) {
        return ReturnResult.illegal("不支持同步");
    }

    @Override
    public IPage<SysEmailTemplate> pageFor(EmailPageV1Request request) {
        if(null == request.getSysProjectId()) {
            return new Page<>();
        }
        return sysEmailTemplateMapper.pageFor(request.createPage(), request);
    }

    @Override
    public ReturnResult<Boolean> delete(SysEmailTemplate sysEmailTemplate) {
        return ReturnResult.of(
                sysEmailTemplateMapper.deleteById(sysEmailTemplate)
        );
    }

    @Override
    public ReturnResult<Boolean> update(SysEmailTemplate sysEmailTemplate) {
        sysEmailTemplate.setSysProjectType(null);
        sysEmailTemplate.setSysProjectId(null);
        return ReturnResult.of(sysEmailTemplateMapper.updateById(sysEmailTemplate) > 0);
    }

    @Override
    public ReturnResult<SysEmailTemplate> save(SysEmailTemplate sysEmailTemplate) {
        sysEmailTemplate.setSysEmailTemplateDelete(0);
        sysEmailTemplateMapper.insert(sysEmailTemplate);
        return ReturnResult.of(sysEmailTemplate);
    }
}
