package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import lombok.Data;

@Data
@TableName("sys_ai_vincent_template")
public class SysAiVincentTemplate extends SysBase {

    @TableId(value = "sys_ai_vincent_template_id", type = IdType.AUTO)
    private Integer sysAiVincentTemplateId;
    private Integer sysAiModuleId;
    private Integer sysAiVincentTemplateSort;
    private String sysAiVincentTemplateName;
    private String sysAiVincentTemplateType;
    private String sysAiVincentTemplateAddress;
    private String sysAiVincentTemplateRemark;
    private String sysAiVincentTemplateModelId;
    private String sysAiVincentTemplateModelName;
    private String sysAiVincentTemplateCategory;
    private String sysAiVincentTemplateTrainModel;
    private String sysAiVincentTemplateRecommendPrompt;
    private String sysAiVincentTemplateResolutions;
    private String sysAiVincentTemplateWord;
}
