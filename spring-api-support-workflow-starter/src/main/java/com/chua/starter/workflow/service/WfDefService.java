package com.chua.starter.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.workflow.dto.WfDefVO;
import com.chua.starter.workflow.entity.WfDef;
import com.chua.starter.workflow.entity.WfDefEdge;
import com.chua.starter.workflow.entity.WfDefNode;

import java.util.List;

/**
 * 流程定义服务
 *
 * @author CH
 * @since 2026/5/28
 */
public interface WfDefService extends IService<WfDef> {

    /**
     * 分页查询
     */
    IPage<WfDef> pageByQuery(Query<WfDef> query, WfDef def);

    /**
     * 查询详情(含节点和边)
     */
    WfDefVO getDetail(Integer defId);

    /**
     * 保存完整流程定义(含节点和边)
     */
    ReturnResult<WfDef> saveFull(WfDefVO vo);

    /**
     * 发布流程定义(草稿→已发布)
     */
    ReturnResult<Boolean> publish(Integer defId);

    /**
     * 禁用流程定义
     */
    ReturnResult<Boolean> disable(Integer defId);

    /**
     * 删除流程定义
     */
    ReturnResult<Boolean> deleteDef(Integer defId);

    /**
     * 查询所有已发布的流程定义(供新建实例时选择)
     */
    List<WfDef> listPublished();
    List<WfDefNode> getNodesByDefId(Integer defId);

    /**
     * 获取流程边列表
     */
    List<WfDefEdge> getEdgesByDefId(Integer defId);
}
