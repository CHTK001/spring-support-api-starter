package com.chua.starter.collect.controller;

import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.collect.entity.CollectTask;
import com.chua.starter.collect.service.CollectTaskService;
import com.chua.starter.collect.vo.CollectTaskLogVO;
import com.chua.starter.collect.vo.CollectTaskVO;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 采集任务接口。
 *
 * <p>任务是流程的一次执行，节点日志按执行顺序返回，节点状态用于设计器回显每个节点的最近结果。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Ignore
@Tag(name = "数据采集-任务")
@RestController
@RequestMapping("/v2/collect/task")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.collect", name = "enable", havingValue = "true", matchIfMissing = true)
public class CollectTaskController {

    /**
     * 任务服务。
     */
    private final CollectTaskService taskService;

    /**
     * 任务分页列表。
     *
     * @param query     分页参数
     * @param condition 查询条件
     * @return 任务分页
     */
    @Operation(summary = "任务分页列表")
    @GetMapping("/page")
    public ReturnPageResult<CollectTaskVO> page(@ParameterObject Query<CollectTask> query,
                                                @ParameterObject CollectTask condition) {
        return PageResultUtils.ok(taskService.pageTasks(query.createPage(), condition));
    }

    /**
     * 任务详情。
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @Operation(summary = "任务详情")
    @GetMapping("/detail")
    public ReturnResult<CollectTaskVO> detail(@RequestParam Long taskId) {
        return ReturnResult.ok(taskService.detail(taskId));
    }

    /**
     * 任务的节点执行日志。
     *
     * @param taskId 任务ID
     * @return 日志列表
     */
    @Operation(summary = "节点执行日志")
    @GetMapping("/logs")
    public ReturnResult<List<CollectTaskLogVO>> logs(@RequestParam Long taskId) {
        return ReturnResult.ok(taskService.logs(taskId));
    }

    /**
     * 每个节点的最近一次状态，供设计器给节点着色。
     *
     * @param taskId 任务ID
     * @return 节点状态列表
     */
    @Operation(summary = "节点最近状态")
    @GetMapping("/node-states")
    public ReturnResult<List<CollectTaskLogVO>> nodeStates(@RequestParam Long taskId) {
        return ReturnResult.ok(taskService.nodeStates(taskId));
    }

    /**
     * 删除任务及其日志，运行中拒绝。
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    @Operation(summary = "删除任务")
    @DeleteMapping("/delete")
    public ReturnResult<Boolean> delete(@RequestParam Long taskId) {
        return ReturnResult.ok(taskService.deleteTask(taskId));
    }

    /**
     * 清理历史任务。
     *
     * @param flowId 流程ID，空表示全部流程
     * @param days   清理多少天之前结束的任务
     * @return 删除的任务数
     */
    @Operation(summary = "清理历史任务")
    @PostMapping("/clean")
    public ReturnResult<Integer> clean(@RequestParam(required = false) Integer flowId,
                                       @RequestParam(defaultValue = "7") int days) {
        return ReturnResult.ok(taskService.cleanTasks(flowId, days));
    }
}
