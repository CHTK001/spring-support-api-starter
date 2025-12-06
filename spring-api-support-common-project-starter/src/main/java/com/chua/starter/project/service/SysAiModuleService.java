package com.chua.starter.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.project.entity.SysAiModule;

import java.util.List;

/**
 * AI模型服务接口
 *
 * @author CH
 * @since 2023-09-15
 */
public interface SysAiModuleService extends IService<SysAiModule> {

    /**
     * 获取模型列表
     *
     * @param sysProjectId    项目ID，用于筛选特定项目的模型，例如：1001
     * @param sysAiModuleType 模型类型，用于筛选特定类型的模型，例如："text-generation"
     * @return 模型列表
     */
    List<SysAiModule> list(Integer sysProjectId, String sysAiModuleType);

    /**
     * 分页查询模型
     *
     * @param query           分页查询参数，包含页码、每页条数等信息
     * @param sysProjectId    项目ID，用于筛选特定项目的模型，例如：1001
     * @param sysAiModuleType 模型类型，用于筛选特定类型的模型，例如："text-generation"
     * @return 分页模型数据
     */
    IPage<SysAiModule> pageFor(Query<?> query, Integer sysProjectId, String sysAiModuleType);

    /**
     * 更新模型信息
     *
     * @param sysAiModule 模型对象，包含要更新的模型信息
     * @return 更新结果，成功返回true，失败返回false
     */
    ReturnResult<Boolean> updateForModule(SysAiModule sysAiModule);

    /**
     * 保存模型信息
     *
     * @param sysAiModule 模型对象，包含要保存的模型信息
     * @return 保存结果，包含保存后的模型信息
     */
    ReturnResult<SysAiModule> saveForModule(SysAiModule sysAiModule);

    /**
     * 删除模型
     *
     * @param sysAiModuleId 模型ID，用于标识要删除的模型，例如：2001
     * @return 删除结果，成功返回true，失败返回false
     */
    ReturnResult<Boolean> deleteForModule(Integer sysAiModuleId);

    /**
     * 根据编码获取模型
     *
     * @param sysProjectId 项目ID，用于筛选特定项目的模型，例如：1001
     * @param model        模型编码，用于唯一标识模型，例如："gpt-3.5-turbo"
     * @return 模型信息
     */
    SysAiModule getOneByCode(Integer sysProjectId, String model);
}
