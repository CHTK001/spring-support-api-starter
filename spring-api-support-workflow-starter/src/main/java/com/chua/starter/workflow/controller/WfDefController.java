package com.chua.starter.workflow.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.workflow.dto.WfDefVO;
import com.chua.starter.workflow.entity.WfDef;
import com.chua.starter.workflow.service.WfDefService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程定义 Controller
 *
 * @author CH
 * @since 2026/5/28
 */
@Tag(name = "流程定义")
@RestController
@RequestMapping("/v2/workflow/def")
@RequiredArgsConstructor
public class WfDefController {

    private final WfDefService wfDefService;

    @Operation(summary = "流程定义分页列表")
    @GetMapping("/page")
    public ReturnPageResult<WfDef> page(@ParameterObject Query<WfDef> query, @ParameterObject WfDef def) {
        return PageResultUtils.ok(wfDefService.pageByQuery(query, def));
    }

    @Operation(summary = "流程定义详情(含节点和边)")
    @GetMapping("/detail")
    public ReturnResult<WfDefVO> detail(Integer defId) {
        WfDefVO vo = wfDefService.getDetail(defId);
        if (vo == null) {
            return ReturnResult.illegal("流程定义不存在");
        }
        return ReturnResult.ok(vo);
    }

    @Operation(summary = "保存完整流程定义(含节点和边)")
    @PostMapping("/save")
    @Permission(value = "workflow:def:save")
    public ReturnResult<WfDef> save(@RequestBody WfDefVO vo) {
        return wfDefService.saveFull(vo);
    }

    @Operation(summary = "发布流程定义")
    @PostMapping("/publish")
    @Permission(value = "workflow:def:publish")
    public ReturnResult<Boolean> publish(Integer defId) {
        return wfDefService.publish(defId);
    }

    @Operation(summary = "禁用流程定义")
    @PostMapping("/disable")
    @Permission(value = "workflow:def:disable")
    public ReturnResult<Boolean> disable(Integer defId) {
        return wfDefService.disable(defId);
    }

    @Operation(summary = "查询所有已发布的流程定义(供新建实例选择)")
    @GetMapping("/list")
    public ReturnResult<List<WfDef>> list() {
        return ReturnResult.ok(wfDefService.listPublished());
    }

    @Operation(summary = "删除流程定义")
    @DeleteMapping("/delete")
    @Permission(value = "workflow:def:delete")
    public ReturnResult<Boolean> delete(Integer defId) {
        return wfDefService.deleteDef(defId);
    }
}
