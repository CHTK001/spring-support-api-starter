package com.chua.starter.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysAiVincentStyle;

import java.util.List;

/**
 * SysAiVincentStyle服务接口，继承自IService，提供了对SysAiVincentStyle实体的操作
 */
public interface SysAiVincentStyleService extends IService<SysAiVincentStyle> {

    /**
     * 根据模块样式ID删除SysAiVincentStyle实体
     *
     * @param sysAiVincentStyleId SysAiVincentStyle实体的ID
     * @return 返回删除操作的结果，包括是否成功
     */
    ReturnResult<Boolean> deleteForModuleStyle(Integer sysAiVincentStyleId);

    /**
     * 保存SysAiVincentStyle实体到数据库
     *
     * @param sysAiVincentStyle 要保存的SysAiVincentStyle实体
     * @return 返回保存操作的结果，包括实体信息
     */
    ReturnResult<SysAiVincentStyle> saveForModuleStyle(SysAiVincentStyle sysAiVincentStyle);

    /**
     * 更新数据库中的SysAiVincentStyle实体
     *
     * @param sysAiVincentStyle 要更新的SysAiVincentStyle实体
     * @return 返回更新操作的结果，包括是否成功
     */
    ReturnResult<Boolean> updateForModuleStyle(SysAiVincentStyle sysAiVincentStyle);

    /**
     * 根据SysAiVincentStyle实体的条件列出所有匹配的实体列表
     *
     * @param sysAiVincentStyle 用于查询条件的SysAiVincentStyle实体
     * @return 返回查询结果的列表，包括所有匹配的SysAiVincentStyle实体
     */
    ReturnResult<List<SysAiVincentStyle>> listForModuleStyle(SysAiVincentStyle sysAiVincentStyle);
}
