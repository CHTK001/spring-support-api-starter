package com.chua.starter.scheduler.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.job.support.store.JobEntity;
import com.chua.starter.job.support.store.JobLogEntity;
import com.chua.starter.scheduler.dto.SchedulerTaskUpdateRequest;
import com.chua.starter.scheduler.service.SchedulerJobPlatformService;
import com.chua.starter.scheduler.vo.SchedulerTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 轻量控制台专用 API。
 * <p>
 * 这一层复用平台服务能力，但通过控制台 Session 做访问保护，
 * 让内置静态页不必再额外依赖 OAuth Token 才能完成常见管理操作。
 * </p>
 *
 * <p>平台基于 job-starter 新架构, 命名空间参数仅作为任务分组 ({@code sys_job_group})
 * 参与过滤, 任务统一存放在 {@code sys_job / sys_job_log} 单表中。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@RestController
@Ignore
@RequestMapping("/job-console/api")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.scheduler.job-platform", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerJobConsoleApiController {

    private final SchedulerJobPlatformService schedulerJobPlatformService;

    @GetMapping("/namespaces")
    public ReturnResult<Object> namespaces() {
        return ReturnResult.ok(List.of("default"));
    }

    @GetMapping("/{namespace}/job/page")
    public ReturnResult<Object> pageJobs(@PathVariable String namespace,
                                         @RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String jobName,
                                         @RequestParam(required = false) Integer triggerStatus) {
        Page<JobEntity> page = schedulerJobPlatformService.pageJobs(pageNum, pageSize, jobName, triggerStatus,
                namespaceToGroup(namespace));
        return ReturnResult.ok(pagePayload(page));
    }

    @GetMapping("/{namespace}/job/no/{jobNo}")
    public ReturnResult<Object> getJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.getJobByNo(jobNo));
    }

    @PostMapping("/{namespace}/job")
    public ReturnResult<Object> createJob(@PathVariable String namespace, @RequestBody JobEntity job) {
        job.setSysJobGroup(namespaceToGroup(namespace));
        return ReturnResult.ok(schedulerJobPlatformService.createJob(job));
    }

    @PutMapping("/{namespace}/job/no/{jobNo}")
    public ReturnResult<Object> updateJobByNo(@PathVariable String namespace,
                                              @PathVariable String jobNo,
                                              @RequestBody JobEntity job) {
        return ReturnResult.ok(schedulerJobPlatformService.updateJobByNo(jobNo, job));
    }

    @DeleteMapping("/{namespace}/job/no/{jobNo}")
    public ReturnResult<Object> deleteJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.deleteJobByNo(jobNo));
    }

    @PostMapping("/{namespace}/job/no/{jobNo}/restore")
    public ReturnResult<Object> restoreJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.restoreJobByNo(jobNo));
    }

    @PostMapping("/{namespace}/job/no/{jobNo}/start")
    public ReturnResult<Object> startJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.startJobByNo(jobNo));
    }

    @PostMapping("/{namespace}/job/no/{jobNo}/stop")
    public ReturnResult<Object> stopJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.stopJobByNo(jobNo));
    }

    @PostMapping("/{namespace}/job/no/{jobNo}/trigger")
    public ReturnResult<Object> triggerJobByNo(@PathVariable String namespace,
                                               @PathVariable String jobNo,
                                               @RequestParam(required = false) String param) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerJobByNo(jobNo, param));
    }

    @GetMapping("/{namespace}/job-log/page")
    public ReturnResult<Object> pageLogs(@PathVariable String namespace,
                                         @RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String beanName,
                                         @RequestParam(required = false) String executeCode) {
        Page<JobLogEntity> page = schedulerJobPlatformService.pageLogs(pageNum, pageSize, beanName, executeCode,
                namespaceToGroup(namespace));
        return ReturnResult.ok(pagePayload(page));
    }

    @GetMapping("/{namespace}/task/list")
    public ReturnResult<Object> listTasks(@PathVariable String namespace,
                                          @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return ReturnResult.ok(schedulerJobPlatformService.listTasks(namespaceToGroup(namespace), includeDeleted));
    }

    @PutMapping("/{namespace}/task/{taskKey}")
    public ReturnResult<SchedulerTaskVO> updateTask(@PathVariable String namespace,
                                                    @PathVariable String taskKey,
                                                    @RequestBody SchedulerTaskUpdateRequest request) {
        return ReturnResult.ok(schedulerJobPlatformService.updateTask(taskKey, request));
    }

    @PostMapping("/{namespace}/task/{taskKey}/trigger")
    public ReturnResult<SchedulerTaskVO> triggerTask(@PathVariable String namespace,
                                                     @PathVariable String taskKey) {
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

    private String namespaceToGroup(String namespace) {
        if (namespace == null || "default".equalsIgnoreCase(namespace) || "undefined".equalsIgnoreCase(namespace)) {
            return null;
        }
        return namespace;
    }
}
