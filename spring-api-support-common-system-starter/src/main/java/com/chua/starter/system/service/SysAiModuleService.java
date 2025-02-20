package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysAiModule;

import java.util.List;

/**
 * 模型
 *
 * @author CH
 */
public interface SysAiModuleService extends IService<SysAiModule> {


    /**
     * 模型列表
     *
     * @param sysProjectId 项目ID
     * @return List
     */
    List<SysAiModule> list(Integer sysProjectId);

    /**
     * 分页模型
     *
     * @param sysProjectId 项目ID
     * @return List
     */
    IPage<SysAiModule> pageFor(Query<?> query, Integer sysProjectId);

    /**
     * 更新模型
     *
     * @param sysAiModule 模型
     * @return ReturnResult
     */
    ReturnResult<Boolean> updateForModule(SysAiModule sysAiModule);

    /**
     * 保存模型
     *
     * @param sysAiModule 模型
     * @return ReturnResult
     */
    ReturnResult<SysAiModule> saveForModule(SysAiModule sysAiModule);

    /**
     * 删除模型
     *
     * @param sysAiModuleId 模型ID
     * @return ReturnResult
     */
    ReturnResult<Boolean> deleteForModule(Integer sysAiModuleId);

    /**
     * 根据编码获取模型
     *
     * @param sysProjectId 项目ID
     * @param model        编码
     * @return SysAiModule
     */
    SysAiModule getOneByCode(Integer sysProjectId, String model);
}
