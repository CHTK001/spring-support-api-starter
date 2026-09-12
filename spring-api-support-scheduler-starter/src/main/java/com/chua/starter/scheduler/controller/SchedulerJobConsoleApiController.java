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
   * 这一层复用平台服务能力，但通过控制台 会话 做访问保护，
   * 让内置静态页不必再额外依赖 OAuth 令牌 才能完成常见管理操作。
 * </p>
 *
 * <p>平台基于 job-starter 新架构, 命名空间参数仅作为任务分组 ({@code sys_job_group})
 * 参与过滤, 任务统一存放在 {@code sys_job / sys_job_log} 单表中。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@RestController
@Ignore
@RequestMapping("/job-console/api")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.scheduler.job-platform", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerJobConsoleApiController {

    /**
     * 调度平台任务服务
     */
    private final SchedulerJobPlatformService schedulerJobPlatformService;

    /**
     * 查询可用命名空间。
     *
     * @return 命名空间列表
     */
    @GetMapping("/namespaces")
    public ReturnResult<Object> namespaces() {
        return ReturnResult.ok(List.of("default"));
    }

    /**
     * 分页查询任务。
     *
     * @param namespace     命名空间
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param jobName       任务名模糊查询
     * @param triggerStatus 启用状态
     * @return 任务分页结果
     */
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

    /**
     * 按任务编号查询任务详情。
     *
     * @param namespace 命名空间
     * @param jobNo     任务编号
     * @return 任务详情
     */
    @GetMapping("/{namespace}/job/no/{jobNo}")
    public ReturnResult<Object> getJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.getJobByNo(jobNo));
    }

    /**
     * 创建任务。
     *
     * @param namespace 命名空间
     * @param job       任务实体
     * @return 创建后的任务
     */
    @PostMapping("/{namespace}/job")
    public ReturnResult<Object> createJob(@PathVariable String namespace, @RequestBody JobEntity job) {
        job.setSysJobGroup(namespaceToGroup(namespace));
        return ReturnResult.ok(schedulerJobPlatformService.createJob(job));
    }

    /**
     * 按任务编号更新任务。
     *
     * @param namespace 命名空间
     * @param jobNo     任务编号
     * @param job       任务实体
     * @return 是否更新成功
     */
    @PutMapping("/{namespace}/job/no/{jobNo}")
    public ReturnResult<Object> updateJobByNo(@PathVariable String namespace,
                                              @PathVariable String jobNo,
                                              @RequestBody JobEntity job) {
        return ReturnResult.ok(schedulerJobPlatformService.updateJobByNo(jobNo, job));
    }

    /**
     * 按任务编号删除任务。
     *
     * @param namespace 命名空间
     * @param jobNo     任务编号
     * @return 是否删除成功
     */
    @DeleteMapping("/{namespace}/job/no/{jobNo}")
    public ReturnResult<Object> deleteJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.deleteJobByNo(jobNo));
    }

    /**
     * 按任务编号恢复已删除任务。
     *
     * @param namespace 命名空间
     * @param jobNo     任务编号
     * @return 是否恢复成功
     */
    @PostMapping("/{namespace}/job/no/{jobNo}/restore")
    public ReturnResult<Object> restoreJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.restoreJobByNo(jobNo));
    }

    /**
     * 按任务编号启动任务。
     *
     * @param namespace 命名空间
     * @param jobNo     任务编号
     * @return 是否启动成功
     */
    @PostMapping("/{namespace}/job/no/{jobNo}/start")
    public ReturnResult<Object> startJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.startJobByNo(jobNo));
    }

    /**
     * 按任务编号停止任务。
     *
     * @param namespace 命名空间
     * @param jobNo     任务编号
     * @return 是否停止成功
     */
    @PostMapping("/{namespace}/job/no/{jobNo}/stop")
    public ReturnResult<Object> stopJobByNo(@PathVariable String namespace, @PathVariable String jobNo) {
        return ReturnResult.ok(schedulerJobPlatformService.stopJobByNo(jobNo));
    }

    /**
     * 按任务编号立即执行任务。
     *
     * @param namespace 命名空间
     * @param jobNo     任务编号
     * @param param     执行参数
     * @return 是否触发成功
     */
    @PostMapping("/{namespace}/job/no/{jobNo}/trigger")
    public ReturnResult<Object> triggerJobByNo(@PathVariable String namespace,
                                               @PathVariable String jobNo,
                                               @RequestParam(required = false) String param) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerJobByNo(jobNo, param));
    }

    /**
     * 清理过期日志。
     *
     * @param namespace     命名空间
     * @param retentionDays 保留天数
     * @return 清理结果提示
     */
    @PostMapping("/{namespace}/job-log/cleanup")
    public ReturnResult<Object> cleanupLogs(@PathVariable String namespace,
                                            @RequestParam(defaultValue = "30") int retentionDays) {
        int deleted = schedulerJobPlatformService.cleanupLogs(retentionDays);
        return ReturnResult.ok("清理完成, 共删除 " + deleted + " 条日志");
    }

    /**
     * 分页查询任务日志。
     *
     * @param namespace   命名空间
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param beanName    处理器 名称模糊查询
     * @param executeCode 执行状态码
     * @return 日志分页结果
     */
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

    /**
     * 查询平台任务视图。
     *
     * @param namespace      命名空间
     * @param includeDeleted 是否包含已删除
     * @return 任务视图列表
     */
    @GetMapping("/{namespace}/task/list")
    public ReturnResult<Object> listTasks(@PathVariable String namespace,
                                          @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return ReturnResult.ok(schedulerJobPlatformService.listTasks(namespaceToGroup(namespace), includeDeleted));
    }

    /**
     * 更新平台任务。
     *
     * @param namespace 命名空间
     * @param taskKey   任务标识
     * @param request   更新请求
     * @return 更新后的任务视图
     */
    @PutMapping("/{namespace}/task/{taskKey}")
    public ReturnResult<SchedulerTaskVO> updateTask(@PathVariable String namespace,
                                                    @PathVariable String taskKey,
                                                    @RequestBody SchedulerTaskUpdateRequest request) {
        return ReturnResult.ok(schedulerJobPlatformService.updateTask(taskKey, request));
    }

    /**
     * 触发平台任务。
     *
     * @param namespace 命名空间
     * @param taskKey   任务标识
     * @return 触发后的任务视图
     */
    @PostMapping("/{namespace}/task/{taskKey}/trigger")
    public ReturnResult<SchedulerTaskVO> triggerTask(@PathVariable String namespace,
                                                     @PathVariable String taskKey) {
        return ReturnResult.ok(schedulerJobPlatformService.triggerTask(taskKey));
    }

    /**
     * 组装分页结果载体。
     *
     * @param page 分页结果
     * @return 分页数据 映射
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

    /**
      * 命名空间转换为任务分组, 默认命名空间对应 空。
     *
     * @param namespace 命名空间
     * @return 任务分组
     */
    private String namespaceToGroup(String namespace) {
        if (namespace == null || "default".equalsIgnoreCase(namespace) || "undefined".equalsIgnoreCase(namespace)) {
            return null;
        }
        return namespace;
    }
}
