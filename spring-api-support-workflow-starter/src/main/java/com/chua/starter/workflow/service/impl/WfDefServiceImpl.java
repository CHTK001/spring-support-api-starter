package com.chua.starter.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.workflow.dto.WfDefVO;
import com.chua.starter.workflow.engine.WorkflowEngine;
import com.chua.starter.workflow.entity.WfDef;
import com.chua.starter.workflow.entity.WfDefEdge;
import com.chua.starter.workflow.entity.WfDefNode;
import com.chua.starter.workflow.mapper.WfDefEdgeMapper;
import com.chua.starter.workflow.mapper.WfDefMapper;
import com.chua.starter.workflow.mapper.WfDefNodeMapper;
import com.chua.starter.workflow.service.WfDefService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程定义服务实现
 *
 * @author CH
 * @since 2026/5/28
 */
@Service
@RequiredArgsConstructor
public class WfDefServiceImpl extends ServiceImpl<WfDefMapper, WfDef> implements WfDefService {

    private final WfDefNodeMapper wfDefNodeMapper;
    private final WfDefEdgeMapper wfDefEdgeMapper;

    @Override
    public IPage<WfDef> pageByQuery(Query<WfDef> query, WfDef def) {
        LambdaQueryWrapper<WfDef> wrapper = new LambdaQueryWrapper<WfDef>()
                .like(StringUtils.hasText(def.getWfDefName()), WfDef::getWfDefName, def.getWfDefName())
                .eq(StringUtils.hasText(def.getWfDefCategory()), WfDef::getWfDefCategory, def.getWfDefCategory())
                .eq(def.getWfDefStatus() != null, WfDef::getWfDefStatus, def.getWfDefStatus())
                .orderByDesc(WfDef::getCreateTime);
        return baseMapper.selectPage(query.createPage(), wrapper);
    }

    @Override
    public WfDefVO getDetail(Integer defId) {
        WfDef def = baseMapper.selectById(defId);
        if (def == null) return null;
        WfDefVO vo = new WfDefVO();
        copyFields(def, vo);
        vo.setNodes(wfDefNodeMapper.selectByDefIdOrderBySort(defId));
        vo.setEdges(wfDefEdgeMapper.selectByDefId(defId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<WfDef> saveFull(WfDefVO vo) {
        // 1. 保存流程定义
        WfDef def = new WfDef();
        copyFields(vo, def);
        if (def.getWfDefId() != null) {
            baseMapper.updateById(def);
        } else {
            def.setWfDefStatus(0);
            def.setWfDefVersion(1);
            baseMapper.insert(def);
        }
        Integer defId = def.getWfDefId();

        // 2. 删除旧节点和边(重新保存)
        wfDefNodeMapper.delete(new LambdaQueryWrapper<WfDefNode>()
                .eq(WfDefNode::getWfDefId, defId));
        wfDefEdgeMapper.delete(new LambdaQueryWrapper<WfDefEdge>()
                .eq(WfDefEdge::getWfDefId, defId));

        // 3. 保存节点，建立旧ID→新ID映射
        Map<Integer, Integer> idMapping = new HashMap<>();
        if (vo.getNodes() != null) {
            for (WfDefNode node : vo.getNodes()) {
                Integer oldId = node.getWfDefNodeId();
                node.setWfDefNodeId(null);
                node.setWfDefId(defId);
                wfDefNodeMapper.insert(node);
                if (oldId != null) {
                    idMapping.put(oldId, node.getWfDefNodeId());
                }
            }
        }

        // 4. 保存边，重映射节点ID
        if (vo.getEdges() != null) {
            for (WfDefEdge edge : vo.getEdges()) {
                edge.setWfDefEdgeId(null);
                edge.setWfDefId(defId);
                if (!idMapping.isEmpty()) {
                    edge.setSourceNodeId(idMapping.getOrDefault(edge.getSourceNodeId(), edge.getSourceNodeId()));
                    edge.setTargetNodeId(idMapping.getOrDefault(edge.getTargetNodeId(), edge.getTargetNodeId()));
                }
                wfDefEdgeMapper.insert(edge);
            }
        }

        return ReturnResult.ok(def);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> publish(Integer defId) {
        WfDef def = baseMapper.selectById(defId);
        if (def == null) {
            return ReturnResult.illegal("流程定义不存在");
        }
        if (def.getWfDefStatus() != 0) {
            return ReturnResult.illegal("仅草稿状态可发布");
        }

        List<WfDefNode> nodes = wfDefNodeMapper.selectByDefIdOrderBySort(defId);
        List<WfDefEdge> edges = wfDefEdgeMapper.selectByDefId(defId);

        if (nodes.isEmpty()) {
            return ReturnResult.illegal("流程定义无节点，无法发布");
        }

        boolean hasStart = nodes.stream().anyMatch(n -> "START".equals(n.getNodeType()));
        boolean hasEnd = nodes.stream().anyMatch(n -> "END".equals(n.getNodeType()));
        if (!hasStart || !hasEnd) {
            return ReturnResult.illegal("流程定义缺少 START 或 END 节点");
        }

        if (WorkflowEngine.hasCycle(nodes, edges)) {
            return ReturnResult.illegal("流程定义存在环路，无法发布");
        }

        def.setWfDefStatus(1);
        baseMapper.updateById(def);
        return ReturnResult.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> disable(Integer defId) {
        WfDef def = baseMapper.selectById(defId);
        if (def == null) {
            return ReturnResult.illegal("流程定义不存在");
        }
        def.setWfDefStatus(2);
        baseMapper.updateById(def);
        return ReturnResult.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteDef(Integer defId) {
        WfDef def = baseMapper.selectById(defId);
        if (def == null) {
            return ReturnResult.illegal("流程定义不存在");
        }
        if (def.getWfDefStatus() == 1) {
            return ReturnResult.illegal("已发布的流程定义不可删除，请先禁用");
        }
        wfDefNodeMapper.delete(new LambdaQueryWrapper<WfDefNode>().eq(WfDefNode::getWfDefId, defId));
        wfDefEdgeMapper.delete(new LambdaQueryWrapper<WfDefEdge>().eq(WfDefEdge::getWfDefId, defId));
        baseMapper.deleteById(defId);
        return ReturnResult.ok(true);
    }

    @Override
    public List<WfDef> listPublished() {
        return lambdaQuery()
                .eq(WfDef::getWfDefStatus, 1)
                .orderByDesc(WfDef::getCreateTime)
                .list();
    }

    @Override
    public List<WfDefNode> getNodesByDefId(Integer defId) {
        return wfDefNodeMapper.selectByDefIdOrderBySort(defId);
    }

    @Override
    public List<WfDefEdge> getEdgesByDefId(Integer defId) {
        return wfDefEdgeMapper.selectByDefId(defId);
    }

    private void copyFields(WfDef src, WfDef dst) {
        dst.setWfDefId(src.getWfDefId());
        dst.setWfDefName(src.getWfDefName());
        dst.setWfDefCode(src.getWfDefCode());
        dst.setWfDefCategory(src.getWfDefCategory());
        dst.setWfDefVersion(src.getWfDefVersion());
        dst.setWfDefStatus(src.getWfDefStatus());
        dst.setWfDefFormConfig(src.getWfDefFormConfig());
        dst.setWfDefRemark(src.getWfDefRemark());
    }
}
