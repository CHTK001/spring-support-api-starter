package com.chua.starter.scheduler.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.job.support.store.JobEntity;
import com.chua.starter.job.support.store.JobLogEntity;
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
 *
 * <p>平台基于 job-starter 新架构 (SCHEDULER 角色) 提供统一任务管理能力,
 * 任务统一存储在 {@code sys_job / sys_job_log} 单表中, 命名空间参数作为任务分组过滤。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@RestController
@RequestMapping("/v1/job-platform")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.scheduler.job-platform", name = "enabled", havingValue = "true", matchIfMissing = true)
@Tag(name = "调度平台任务中心")
public class SchedulerJobPlatformController {

    private final SchedulerJobPlatformService schedulerJobPlatformService;

    @GetMapping("/job/page")
    @Operation(summary = "分页查询任务")
    public ReturnResult<Object> pageJobs(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String jobName,
                                         @RequestParam(required = false) Integer triggerStatus,
                                         @RequestParam(required = false) String group) {
        Page<JobEntity> page = schedulerJobPlatformService.pageJobs(pageNum, pageSize, jobName, triggerStatus, group);
        return ReturnResult.ok(pagePayload(page));
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "查询任务详情")
    public ReturnResult<Object> getJob(@PathVariable Long jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.getJob(jobId));
    }

    @GetMapping("/job/no/{jobNo}")
    @Operation(summary = "按任务编号查询任务详情")
    public ReturnResult<Object> getJobByNo(@PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.getJobByNo(jobNo));
    }

    @PostMapping("/job")
    @Operation(summary = "创建任务")
    public ReturnResult<Object> createJob(@RequestBody JobEntity job) {
        return ReturnResult.ok(schedulerJobPlatformService.createJob(job));
    }

    @PutMapping("/job/{jobId}")
    @Operation(summary = "更新任务")
    public ReturnResult<Object> updateJob(@PathVariable Long jobId, @RequestBody JobEntity job) {
        return ReturnResult.ok(schedulerJobPlatformService.updateJob(jobId, job));
    }

    @PutMapping("/job/no/{jobNo}")
    @Operation(summary = "按任务编号更新任务")
    public ReturnResult<Object> updateJobByNo(@PathVariable String jobNo, @RequestBody JobEntity job) {
        return ReturnResult.ok(schedulerJobPlatformService.updateJobByNo(jobNo, job));
    }

    @DeleteMapping("/job/{jobId}")
    @Operation(summary = "删除任务")
    public ReturnResult<Object> deleteJob(@PathVariable Long jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.deleteJob(jobId));
    }

    @DeleteMapping("/job/no/{jobNo}")
    @Operation(summary = "按任务编号删除任务")
    public ReturnResult<Object> deleteJobByNo(@PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.deleteJobByNo(jobNo));
    }

    @PostMapping("/job/{jobId}/restore")
    @Operation(summary = "恢复已删除任务")
    public ReturnResult<Object> restoreJob(@PathVariable Long jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.restoreJob(jobId));
    }

    @PostMapping("/job/no/{jobNo}/restore")
    @Operation(summary = "按任务编号恢复已删除任务")
    public ReturnResult<Object> restoreJobByNo(@PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.restoreJobByNo(jobNo));
    }

    @PostMapping("/job/{jobId}/start")
    @Operation(summary = "启动任务")
    public ReturnResult<Object> startJob(@PathVariable Long jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.startJob(jobId));
    }

    @PostMapping("/job/no/{jobNo}/start")
    @Operation(summary = "按任务编号启动任务")
    public ReturnResult<Object> startJobByNo(@PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.startJobByNo(jobNo));
    }

    @PostMapping("/job/{jobId}/stop")
    @Operation(summary = "停止任务")
    public ReturnResult<Object> stopJob(@PathVariable Long jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.stopJob(jobId));
    }

    @PostMapping("/job/no/{jobNo}/stop")
    @Operation(summary = "按任务编号停止任务")
    public ReturnResult<Object> stopJobByNo(@PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.stopJobByNo(jobNo));
    }

    @PostMapping("/job/{jobId}/trigger")
    @Operation(summary = "立即执行任务")
    public ReturnResult<Object> triggerJob(@PathVariable Long jobId,
                                           @RequestParam(required = false) String param) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerJob(jobId, param));
    }

    @PostMapping("/job/no/{jobNo}/trigger")
    @Operation(summary = "按任务编号立即执行任务")
    public ReturnResult<Object> triggerJobByNo(@PathVariable String jobNo,
                                               @RequestParam(required = false) String param) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerJobByNo(jobNo, param));
    }

    @GetMapping("/job-log/page")
    @Operation(summary = "分页查询任务日志")
    public ReturnResult<Object> pageLogs(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String beanName,
                                         @RequestParam(required = false) String executeCode,
                                         @RequestParam(required = false) String group) {
        Page<JobLogEntity> page = schedulerJobPlatformService.pageLogs(pageNum, pageSize, beanName, executeCode, group);
        return ReturnResult.ok(pagePayload(page));
    }

    @GetMapping("/task/list")
    @Operation(summary = "查询平台任务视图")
    public ReturnResult<Object> listTasks(@RequestParam(defaultValue = "false") boolean includeDeleted,
                                          @RequestParam(required = false) String group) {
        return ReturnResult.ok(schedulerJobPlatformService.listTasks(group, includeDeleted));
    }

    @PutMapping("/task/{taskKey}")
    @Operation(summary = "更新平台任务")
    public ReturnResult<SchedulerTaskVO> updateTask(@PathVariable String taskKey,
                                                    @RequestBody SchedulerTaskUpdateRequest request) {
        return ReturnResult.ok(schedulerJobPlatformService.updateTask(taskKey, request));
    }

    @PostMapping("/task/{taskKey}/trigger")
    @Operation(summary = "触发平台任务")
    public ReturnResult<SchedulerTaskVO> triggerTask(@PathVariable String taskKey) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerTask(taskKey));
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
