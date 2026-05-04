package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI模块
 */
@Data
@TableName("sys_ai_module")
public class SysAiModule {
    private Integer sysAiModuleId;
    private String sysAiModuleName;
    private String sysAiModuleCode;
    private Integer sysProjectId;
    private String sysAiModuleRemark;
    private String createName;
    private Integer createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String updateName;
    private Integer updateBy;
}
