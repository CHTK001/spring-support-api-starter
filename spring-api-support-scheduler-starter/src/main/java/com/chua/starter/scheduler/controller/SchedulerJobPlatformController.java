package com.chua.starter.scheduler.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.job.support.entity.SysJob;
import com.chua.starter.job.support.entity.SysJobLog;
import com.chua.starter.scheduler.dto.SchedulerTaskUpdateRequest;
import com.chua.starter.scheduler.service.SchedulerJobPlatformService;
import com.chua.starter.scheduler.vo.SchedulerTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 调度平台任务管理接口。
 */
@RestController
@RequestMapping("/v1/job-platform")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.scheduler.job-platform", name = "enabled", havingValue = "true", matchIfMissing = true)
@Tag(name = "调度平台任务中心")
public class SchedulerJobPlatformController {

    private final SchedulerJobPlatformService schedulerJobPlatformService;

    @GetMapping("/namespaces")
    @Operation(summary = "查询任务命名空间")
    public ReturnResult<Object> namespaces() {
        return ReturnResult.ok(schedulerJobPlatformService.namespaces());
    }

    @GetMapping("/{namespace}/job/page")
    @Operation(summary = "分页查询任务")
    public ReturnResult<Object> pageJobs(@PathVariable String namespace,
                                         @RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String jobName,
                                         @RequestParam(required = false) Integer triggerStatus) {
        Page<SysJob> page = schedulerJobPlatformService.pageJobs(namespace, pageNum, pageSize, jobName, triggerStatus);
        return ReturnResult.ok(pagePayload(page));
    }

    @GetMapping("/{namespace}/job/{jobId}")
    @Operation(summary = "查询任务详情")
    public ReturnResult<Object> getJob(@PathVariable String namespace, @PathVariable Integer jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.getJob(namespace, jobId));
    }

    @GetMapping("/{namespace}/job/no/{jobNo}")
    @Operation(summary = "按任务编号查询任务详情")
    public ReturnResult<Object> getJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.getJobByNo(namespace, jobNo));
    }

    @PostMapping("/{namespace}/job")
    @Operation(summary = "创建任务")
    public ReturnResult<Object> createJob(@PathVariable String namespace, @RequestBody SysJob job) {
        return ReturnResult.ok(schedulerJobPlatformService.createJob(namespace, job));
    }

    @PutMapping("/{namespace}/job/{jobId}")
    @Operation(summary = "更新任务")
    public ReturnResult<Object> updateJob(@PathVariable String namespace,
                                          @PathVariable Integer jobId,
                                          @RequestBody SysJob job) {
        return ReturnResult.ok(schedulerJobPlatformService.updateJob(namespace, jobId, job));
    }

    @PutMapping("/{namespace}/job/no/{jobNo}")
    @Operation(summary = "按任务编号更新任务")
    public ReturnResult<Object> updateJobByNo(@PathVariable String namespace,
                                              @PathVariable String jobNo,
                                              @RequestBody SysJob job) {
        return ReturnResult.ok(schedulerJobPlatformService.updateJobByNo(namespace, jobNo, job));
    }

    @DeleteMapping("/{namespace}/job/{jobId}")
    @Operation(summary = "删除任务")
    public ReturnResult<Object> deleteJob(@PathVariable String namespace, @PathVariable Integer jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.deleteJob(namespace, jobId));
    }

    @DeleteMapping("/{namespace}/job/no/{jobNo}")
    @Operation(summary = "按任务编号删除任务")
    public ReturnResult<Object> deleteJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.deleteJobByNo(namespace, jobNo));
    }

    @PostMapping("/{namespace}/job/{jobId}/restore")
    @Operation(summary = "恢复已删除任务")
    public ReturnResult<Object> restoreJob(@PathVariable String namespace, @PathVariable Integer jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.restoreJob(namespace, jobId));
    }

    @PostMapping("/{namespace}/job/no/{jobNo}/restore")
    @Operation(summary = "按任务编号恢复已删除任务")
    public ReturnResult<Object> restoreJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.restoreJobByNo(namespace, jobNo));
    }

    @PostMapping("/{namespace}/job/{jobId}/start")
    @Operation(summary = "启动任务")
    public ReturnResult<Object> startJob(@PathVariable String namespace, @PathVariable Integer jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.startJob(namespace, jobId));
    }

    @PostMapping("/{namespace}/job/no/{jobNo}/start")
    @Operation(summary = "按任务编号启动任务")
    public ReturnResult<Object> startJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.startJobByNo(namespace, jobNo));
    }

    @PostMapping("/{namespace}/job/{jobId}/stop")
    @Operation(summary = "停止任务")
    public ReturnResult<Object> stopJob(@PathVariable String namespace, @PathVariable Integer jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.stopJob(namespace, jobId));
    }

    @PostMapping("/{namespace}/job/no/{jobNo}/stop")
    @Operation(summary = "按任务编号停止任务")
    public ReturnResult<Object> stopJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.stopJobByNo(namespace, jobNo));
    }

    @PostMapping("/{namespace}/job/{jobId}/trigger")
    @Operation(summary = "立即执行任务")
    public ReturnResult<Object> triggerJob(@PathVariable String namespace,
                                           @PathVariable Integer jobId,
                                           @RequestParam(required = false) String param) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerJob(namespace, jobId, param));
    }

    @PostMapping("/{namespace}/job/no/{jobNo}/trigger")
    @Operation(summary = "按任务编号立即执行任务")
    public ReturnResult<Object> triggerJobByNo(@PathVariable String namespace,
                                               @PathVariable String jobNo,
                                               @RequestParam(required = false) String param) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerJobByNo(namespace, jobNo, param));
    }

    @GetMapping("/{namespace}/job-log/page")
    @Operation(summary = "分页查询任务日志")
    public ReturnResult<Object> pageLogs(@PathVariable String namespace,
                                         @RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String beanName,
                                         @RequestParam(required = false) String executeCode) {
        Page<SysJobLog> page = schedulerJobPlatformService.pageLogs(namespace, pageNum, pageSize, beanName, executeCode);
        return ReturnResult.ok(pagePayload(page));
    }

    @GetMapping("/{namespace}/task/list")
    @Operation(summary = "查询平台任务视图")
    public ReturnResult<Object> listTasks(@PathVariable String namespace) {
        return ReturnResult.ok(schedulerJobPlatformService.listTasks(namespace));
    }

    @PutMapping("/{namespace}/task/{taskKey}")
    @Operation(summary = "更新平台任务")
    public ReturnResult<SchedulerTaskVO> updateTask(@PathVariable String namespace,
                                                    @PathVariable String taskKey,
                                                    @RequestBody SchedulerTaskUpdateRequest request) {
        return ReturnResult.ok(schedulerJobPlatformService.updateTask(namespace, taskKey, request));
    }

    @PostMapping("/{namespace}/task/{taskKey}/trigger")
    @Operation(summary = "触发平台任务")
    public ReturnResult<SchedulerTaskVO> triggerTask(@PathVariable String namespace,
                                                     @PathVariable String taskKey) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerTask(namespace, taskKey));
    }

    private Map<String, Object> pagePayload(Page<?> page) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("records", page.getRecords());
        payload.put("total", page.getTotal());
        payload.put("current", page.getCurrent());
        payload.put("size", page.getSize());
        payload.put("pages", page.getPages());
        return payload;
    }
}
