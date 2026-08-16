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

    /**
     * 调度平台任务服务
     */
    private final SchedulerJobPlatformService schedulerJobPlatformService;

    /**
     * 分页查询任务。
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param jobName       任务名模糊查询
     * @param triggerStatus 启用状态
     * @param group         任务分组
     * @return 任务分页结果
     */
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

    /**
     * 查询任务详情。
     *
     * @param jobId 任务 ID
     * @return 任务详情
     */
    @GetMapping("/job/{jobId}")
    @Operation(summary = "查询任务详情")
    public ReturnResult<Object> getJob(@PathVariable Long jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.getJob(jobId));
    }

    /**
     * 按任务编号查询任务详情。
     *
     * @param jobNo 任务编号
     * @return 任务详情
     */
    @GetMapping("/job/no/{jobNo}")
    @Operation(summary = "按任务编号查询任务详情")
    public ReturnResult<Object> getJobByNo(@PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.getJobByNo(jobNo));
    }

    /**
     * 创建任务。
     *
     * @param job 任务实体
     * @return 创建后的任务
     */
    @PostMapping("/job")
    @Operation(summary = "创建任务")
    public ReturnResult<Object> createJob(@RequestBody JobEntity job) {
        return ReturnResult.ok(schedulerJobPlatformService.createJob(job));
    }

    /**
     * 更新任务。
     *
     * @param jobId 任务 ID
     * @param job   任务实体
     * @return 是否更新成功
     */
    @PutMapping("/job/{jobId}")
    @Operation(summary = "更新任务")
    public ReturnResult<Object> updateJob(@PathVariable Long jobId, @RequestBody JobEntity job) {
        return ReturnResult.ok(schedulerJobPlatformService.updateJob(jobId, job));
    }

    /**
     * 按任务编号更新任务。
     *
     * @param jobNo 任务编号
     * @param job   任务实体
     * @return 是否更新成功
     */
    @PutMapping("/job/no/{jobNo}")
    @Operation(summary = "按任务编号更新任务")
    public ReturnResult<Object> updateJobByNo(@PathVariable String jobNo, @RequestBody JobEntity job) {
        return ReturnResult.ok(schedulerJobPlatformService.updateJobByNo(jobNo, job));
    }

    /**
     * 删除任务。
     *
     * @param jobId 任务 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/job/{jobId}")
    @Operation(summary = "删除任务")
    public ReturnResult<Object> deleteJob(@PathVariable Long jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.deleteJob(jobId));
    }

    /**
     * 按任务编号删除任务。
     *
     * @param jobNo 任务编号
     * @return 是否删除成功
     */
    @DeleteMapping("/job/no/{jobNo}")
    @Operation(summary = "按任务编号删除任务")
    public ReturnResult<Object> deleteJobByNo(@PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.deleteJobByNo(jobNo));
    }

    /**
     * 恢复已删除任务。
     *
     * @param jobId 任务 ID
     * @return 是否恢复成功
     */
    @PostMapping("/job/{jobId}/restore")
    @Operation(summary = "恢复已删除任务")
    public ReturnResult<Object> restoreJob(@PathVariable Long jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.restoreJob(jobId));
    }

    /**
     * 按任务编号恢复已删除任务。
     *
     * @param jobNo 任务编号
     * @return 是否恢复成功
     */
    @PostMapping("/job/no/{jobNo}/restore")
    @Operation(summary = "按任务编号恢复已删除任务")
    public ReturnResult<Object> restoreJobByNo(@PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.restoreJobByNo(jobNo));
    }

    /**
     * 启动任务。
     *
     * @param jobId 任务 ID
     * @return 是否启动成功
     */
    @PostMapping("/job/{jobId}/start")
    @Operation(summary = "启动任务")
    public ReturnResult<Object> startJob(@PathVariable Long jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.startJob(jobId));
    }

    /**
     * 按任务编号启动任务。
     *
     * @param jobNo 任务编号
     * @return 是否启动成功
     */
    @PostMapping("/job/no/{jobNo}/start")
    @Operation(summary = "按任务编号启动任务")
    public ReturnResult<Object> startJobByNo(@PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.startJobByNo(jobNo));
    }

    /**
     * 停止任务。
     *
     * @param jobId 任务 ID
     * @return 是否停止成功
     */
    @PostMapping("/job/{jobId}/stop")
    @Operation(summary = "停止任务")
    public ReturnResult<Object> stopJob(@PathVariable Long jobId) {
        return ReturnResult.ok(schedulerJobPlatformService.stopJob(jobId));
    }

    /**
     * 按任务编号停止任务。
     *
     * @param jobNo 任务编号
     * @return 是否停止成功
     */
    @PostMapping("/job/no/{jobNo}/stop")
    @Operation(summary = "按任务编号停止任务")
    public ReturnResult<Object> stopJobByNo(@PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.stopJobByNo(jobNo));
    }

    /**
     * 立即执行任务。
     *
     * @param jobId 任务 ID
     * @param param 执行参数
     * @return 是否触发成功
     */
    @PostMapping("/job/{jobId}/trigger")
    @Operation(summary = "立即执行任务")
    public ReturnResult<Object> triggerJob(@PathVariable Long jobId,
                                           @RequestParam(required = false) String param) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerJob(jobId, param));
    }

    /**
     * 按任务编号立即执行任务。
     *
     * @param jobNo 任务编号
     * @param param 执行参数
     * @return 是否触发成功
     */
    @PostMapping("/job/no/{jobNo}/trigger")
    @Operation(summary = "按任务编号立即执行任务")
    public ReturnResult<Object> triggerJobByNo(@PathVariable String jobNo,
                                               @RequestParam(required = false) String param) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerJobByNo(jobNo, param));
    }

    /**
     * 分页查询任务日志。
     *
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param beanName    handler 名称模糊查询
     * @param executeCode 执行状态码
     * @param group       任务分组
     * @return 日志分页结果
     */
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

    /**
     * 查询平台任务视图。
     *
     * @param includeDeleted 是否包含已删除
     * @param group          任务分组
     * @return 任务视图列表
     */
    @GetMapping("/task/list")
    @Operation(summary = "查询平台任务视图")
    public ReturnResult<Object> listTasks(@RequestParam(defaultValue = "false") boolean includeDeleted,
                                          @RequestParam(required = false) String group) {
        return ReturnResult.ok(schedulerJobPlatformService.listTasks(group, includeDeleted));
    }

    /**
     * 更新平台任务。
     *
     * @param taskKey 任务标识
     * @param request 更新请求
     * @return 更新后的任务视图
     */
    @PutMapping("/task/{taskKey}")
    @Operation(summary = "更新平台任务")
    public ReturnResult<SchedulerTaskVO> updateTask(@PathVariable String taskKey,
                                                    @RequestBody SchedulerTaskUpdateRequest request) {
        return ReturnResult.ok(schedulerJobPlatformService.updateTask(taskKey, request));
    }

    /**
     * 触发平台任务。
     *
     * @param taskKey 任务标识
     * @return 触发后的任务视图
     */
    @PostMapping("/task/{taskKey}/trigger")
    @Operation(summary = "触发平台任务")
    public ReturnResult<SchedulerTaskVO> triggerTask(@PathVariable String taskKey) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerTask(taskKey));
    }

    /**
     * 组装分页结果载体。
     *
     * @param page 分页结果
     * @return 分页数据 Map
     */
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
