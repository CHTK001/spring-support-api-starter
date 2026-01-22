package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.project.mapper.SysAiVincentTemplateMapper;
import com.chua.starter.project.service.SysAiVincentTemplateService;
import com.chua.starter.entity.project.SysAiVincentTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统ai模型模板
 *
 * @author CH
 */
@Service
public class SysAiVincentTemplateServiceImpl extends ServiceImpl<SysAiVincentTemplateMapper, SysAiVincentTemplate> implements SysAiVincentTemplateService {

    @Override
    public ReturnResult<List<SysAiVincentTemplate>> listForModuleTemplate(SysAiVincentTemplate sysAiVincentTemplate) {
        return ReturnResult.ok(
                list(Wrappers.<SysAiVincentTemplate>lambdaQuery()
                        .eq(SysAiVincentTemplate::getSysAiModuleId, sysAiVincentTemplate.getSysAiModuleId())
                        .eq(StringUtils.isNotBlank(sysAiVincentTemplate.getSysAiVincentTemplateType()), SysAiVincentTemplate::getSysAiVincentTemplateType, sysAiVincentTemplate.getSysAiVincentTemplateType())
                        .eq(StringUtils.isNotBlank(sysAiVincentTemplate.getSysAiVincentTemplateCategory()), SysAiVincentTemplate::getSysAiVincentTemplateCategory, sysAiVincentTemplate.getSysAiVincentTemplateCategory())
                        .likeRight(StringUtils.isNotBlank(sysAiVincentTemplate.getSysAiVincentTemplateName()), SysAiVincentTemplate::getSysAiVincentTemplateName, sysAiVincentTemplate.getSysAiVincentTemplateName())
                        .likeRight(StringUtils.isNotBlank(sysAiVincentTemplate.getSysAiVincentTemplateRemark()), SysAiVincentTemplate::getSysAiVincentTemplateRemark, sysAiVincentTemplate.getSysAiVincentTemplateRemark())
                        .orderByAsc(SysAiVincentTemplate::getSysAiVincentTemplateSort)
                        .orderByDesc(SysAiVincentTemplate::getCreateTime)
                )
        );
    }

    @Override
    public ReturnResult<Boolean> updateForModuleTemplate(SysAiVincentTemplate sysAiVincentTemplate) {
        return ReturnResult.ok(updateById(sysAiVincentTemplate));
    }

    @Override
    public ReturnResult<SysAiVincentTemplate> saveForModuleTemplate(SysAiVincentTemplate sysAiVincentTemplate) {
        sysAiVincentTemplate.setSysAiVincentTemplateId(null);
        boolean save = save(sysAiVincentTemplate);
        if (!save) {
            return ReturnResult.illegal("新增失败");
        }
        return ReturnResult.ok(sysAiVincentTemplate);
    }

    @Override
    public ReturnResult<Boolean> deleteForModuleTemplate(Integer sysAiVincentTemplateId) {
        if (null == sysAiVincentTemplateId) {
            return ReturnResult.illegal("模板唯一索引不能为空");
        }
        return ReturnResult.preconditioning(removeById(sysAiVincentTemplateId))
                .toResult();
    }
}
