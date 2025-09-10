package com.chua.starter.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.entity.SysAiGroup;
import com.chua.starter.project.entity.SysAiGroupMessage;

import java.util.List;

/**
 * AI分组消息服务接口
 *
 * @author CH
 * @since 2025-09-04
 */
public interface SysAiGroupMessageService extends IService<SysAiGroupMessage> {

    /**
     * 保存分组消息
     *
     * @param sysAiGroupMessages 分组信息 {@link SysAiGroup}
     * @return {@link ReturnResult}<{@link Boolean}> 操作结果
     * @example sysAiGroup: {"sysAiGroupId":1,"sysAiGroupName":"测试分组","sysProjectId":1,"sysUserId":"user001","sysAiGroupColor":"#FF0000","sysAiGroupConfig":"{}"}
     */
    ReturnResult<Boolean> saveGroupMessage(List<SysAiGroupMessage> sysAiGroupMessages);

    /**
     * 分页查询分组消息
     *
     * @param sysAiGroupId 分组id
     * @param current      当前页
     * @param size         每页数量
     * @return {@link ReturnPageResult}<{@link SysAiGroupMessage}> 分组信息
     */
    ReturnPageResult<SysAiGroupMessage> list(Integer sysAiGroupId, long current, long size);
}
