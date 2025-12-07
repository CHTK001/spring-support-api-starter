package com.chua.starter.project.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.project.SysAiGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.NonNull;

import java.util.List;

/**
 * AI分组服务接口
 *
 * @author CH
 * @since 2025/9/4 13:07
 */
public interface SysAiGroupService extends IService<SysAiGroup> {

    /**
     * 根据项目ID获取AI分组列表
     *
     * @param sysProjectId 项目ID，不能为空，例如：1001
     * @return 分组列表结果，包含分组信息或错误信息
     */
    ReturnResult<List<SysAiGroup>> group(@NonNull Integer sysProjectId);

    /**
     * 保存或更新AI分组信息
     *
     * @param sysAiGroup AI分组实体对象，不能为空，包含分组的完整信息
     * @return 操作结果，成功返回true，失败返回false及错误信息
     */
    ReturnResult<Boolean> saveOrUpdateForGroup(@NonNull SysAiGroup sysAiGroup);

    /**
     * 删除指定的AI分组
     *
     * @param sysAiGroupId AI分组ID，不能为空，例如：2001
     * @return 操作结果，成功返回true，失败返回false及错误信息
     */
    ReturnResult<Boolean> deleteForGroup(@NonNull Integer sysAiGroupId);
}
