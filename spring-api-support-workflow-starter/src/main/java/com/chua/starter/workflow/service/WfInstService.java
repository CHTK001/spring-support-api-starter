package com.chua.starter.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.workflow.dto.WfInstCreateRequest;
import com.chua.starter.workflow.dto.WfInstVO;
import com.chua.starter.workflow.entity.WfInst;

/**
 * 流程实例服务
 *
 * @author CH
 * @since 2026/5/28
 */
public interface WfInstService extends IService<WfInst> {

    /**
     * 分页查询我的申请
     */
    IPage<WfInst> pageMyApply(Query<WfInst> query, WfInst inst, Integer userId);

    /**
     * 分页查询我的待办
     */
    IPage<WfInst> pageMyTodo(Query<WfInst> query, Integer userId);

    /**
     * 启动流程(草稿)
     */
    ReturnResult<WfInst> create(WfInstCreateRequest request, Integer userId, String userName, Integer deptId);

    /**
     * 提交审批(草稿→审批中)
     */
    ReturnResult<Boolean> submit(Integer instId, Integer userId);

    /**
     * 获取实例详情(含任务和历史)
     */
    WfInstVO getDetail(Integer instId, Integer userId);

    /**
     * 撤销申请
     */
    ReturnResult<Boolean> withdraw(Integer instId, Integer userId);

    /**
     * 删除实例(仅草稿/已驳回/已撤销)
     */
    ReturnResult<Boolean> deleteInst(Integer instId, Integer userId);
}
