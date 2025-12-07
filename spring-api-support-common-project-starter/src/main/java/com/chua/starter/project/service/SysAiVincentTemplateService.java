package com.chua.starter.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.project.SysAiVincentTemplate;

import java.util.List;

/**
 * SysAiVincentTemplate服务接口，继承自IService
 * 该接口定义了一系列针对SysAiVincentTemplate实体的操作，具体操作与模块模板相关
 *
 * @author CH
 */
public interface SysAiVincentTemplateService extends IService<SysAiVincentTemplate> {

    /**
     * 根据模块模板查询SysAiVincentTemplate列表
     *
     * @param sysAiVincentTemplate 包含查询条件的SysAiVincentTemplate对象
     * @return 包含查询结果的ReturnResult对象，其中泛型Boolean表示操作是否成功
     */
    ReturnResult<List<SysAiVincentTemplate>> listForModuleTemplate(SysAiVincentTemplate sysAiVincentTemplate);

    /**
     * 更新模块模板相关的SysAiVincentTemplate信息
     *
     * @param sysAiVincentTemplate 包含更新信息的SysAiVincentTemplate对象
     * @return 包含更新结果的ReturnResult对象，其中泛型Boolean表示操作是否成功
     */
    ReturnResult<Boolean> updateForModuleTemplate(SysAiVincentTemplate sysAiVincentTemplate);

    /**
     * 保存模块模板相关的SysAiVincentTemplate信息
     *
     * @param sysAiVincentTemplate 需要保存的SysAiVincentTemplate对象
     * @return 包含保存结果的ReturnResult对象，其中泛型SysAiModule表示保存后的模块信息
     */
    ReturnResult<SysAiVincentTemplate> saveForModuleTemplate(SysAiVincentTemplate sysAiVincentTemplate);

    /**
     * 根据模块模板ID删除SysAiVincentTemplate信息
     *
     * @param sysAiVincentTemplateId 模块模板ID
     * @return 包含删除结果的ReturnResult对象，其中泛型Boolean表示操作是否成功
     */
    ReturnResult<Boolean> deleteForModuleTemplate(Integer sysAiVincentTemplateId);
}
