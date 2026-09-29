package com.chua.starter.collect.scheduler.controller;

import com.chua.common.support.lang.code.PageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.job.support.dispatch.JobQuery;
import com.chua.starter.job.support.log.JobLogQuery;
import com.chua.starter.job.support.manage.JobGovernanceService;
import com.chua.starter.job.support.manage.dto.JobScheduleDTO;
import com.chua.starter.job.support.manage.dto.JobSpecDTO;
import com.chua.starter.job.support.manage.dto.JobStatusDTO;
import com.chua.starter.job.support.manage.vo.JobExecutorVO;
import com.chua.starter.job.support.manage.vo.JobLogVO;
import com.chua.starter.job.support.manage.vo.JobNodeVO;
import com.chua.starter.job.support.manage.vo.JobRuntimeVO;
import com.chua.starter.job.support.manage.vo.JobSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 调度控制台出口 (HTTP)。
 *
 * <p>与 job-starter 自带的 {@code /v2/job/**} 共用 {@link JobGovernanceService}，两组地址都受
 * 统一认证过滤器保护，凭据来自框架登录接口 {@code /v2/user/login} 签发的令牌。</p>
 *
 * <p>调度与采集是两个平级系统，这里挂在 {@code /v2/scheduler/**} 而不是 {@code /v2/collect/**}，
 * 菜单与接口都按子系统分面，采集只是共用 {@code sys_job} 台账。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Tag(name = "调度-控制台")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/scheduler")
@ConditionalOnProperty(prefix = "plugin.job.manage", name = "enable", havingValue = "true")
public class CollectSchedulerConsoleController {

    /**
     * 任务治理实现
     */
    private final JobGovernanceService governance;

    /**
     * 工作台概览。
     *
     * @return 概览
     */
    @Operation(summary = "调度概览")
    @GetMapping("/summary")
    public ReturnResult<JobSummaryVO> summary() {
        return governance.summary();
    }

    /**
     * 分页查询任务运行态。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询任务")
    @GetMapping("/page")
    public ReturnResult<PageResult<JobRuntimeVO>> page(JobQuery query) {
        return governance.page(query);
    }

    /**
     * 列出当前进程可用的脚本引擎（驱动前端「片段语言」下拉）。
     *
     * @return 引擎名列表
     */
    @Operation(summary = "列出可用脚本引擎")
    @GetMapping("/glue/engines")
    public ReturnResult<List<String>> glueEngines() {
        return governance.glueEngines();
    }

    /**
     * 读取单个任务运行态。
     *
     * @param jobKey 任务键
     * @return 运行态
     */
    @Operation(summary = "任务详情")
    @GetMapping("/detail/{jobKey}")
    public ReturnResult<JobRuntimeVO> detail(@PathVariable String jobKey) {
        return governance.detail(jobKey);
    }

    /**
     * 登记或更新任务。
     *
     * @param dto 任务规格
     * @return 是否成功
     */
    @Operation(summary = "登记或更新任务")
    @PostMapping("/save")
    public ReturnResult<Boolean> save(@RequestBody JobSpecDTO dto) {
        return governance.save(dto);
    }

    /**
     * 修改调度配置并立即生效。
     *
     * @param dto 调度配置
     * @return 是否成功
     */
    @Operation(summary = "修改调度配置")
    @PutMapping("/schedule")
    public ReturnResult<Boolean> updateSchedule(@RequestBody JobScheduleDTO dto) {
        return governance.updateSchedule(dto);
    }

    /**
     * 启停自动调度。
     *
     * @param dto 启停入参
     * @return 是否成功
     */
    @Operation(summary = "启停自动调度")
    @PutMapping("/status")
    public ReturnResult<Boolean> changeStatus(@RequestBody JobStatusDTO dto) {
        return governance.changeStatus(dto);
    }

    /**
     * 立即派发一次。
     *
     * @param jobKey 任务键
     * @return 是否已投递
     */
    @Operation(summary = "立即派发一次")
    @PostMapping("/trigger")
    public ReturnResult<Boolean> trigger(@RequestParam String jobKey) {
        return governance.trigger(jobKey);
    }

    /**
     * 删除任务。
     *
     * @param jobKey 任务键
     * @return 是否成功
     */
    @Operation(summary = "删除任务")
    @DeleteMapping("/{jobKey}")
    public ReturnResult<Boolean> remove(@PathVariable String jobKey) {
        return governance.remove(jobKey);
    }

    /**
     * 本节点已注册执行器清单。
     *
     * @return 执行器清单
     */
    @Operation(summary = "已注册执行器清单")
    @GetMapping("/executors")
    public ReturnResult<List<JobExecutorVO>> executors() {
        return governance.executors();
    }

    /**
     * 在线执行器节点清单（远程作业页）。
     *
     * @return 节点清单
     */
    @Operation(summary = "在线执行器节点清单")
    @GetMapping("/nodes")
    public ReturnResult<List<JobNodeVO>> nodes() {
        return governance.nodes();
    }

    /**
     * 分页查询执行日志。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询执行日志")
    @GetMapping("/log/page")
    public ReturnResult<PageResult<JobLogVO>> pageLogs(JobLogQuery query) {
        return governance.pageLogs(query);
    }

    /**
     * 清理执行日志。
     *
     * @param retentionDays 保留天数，0 为清空全部
     * @return 删除条数
     */
    @Operation(summary = "清理执行日志")
    @PostMapping("/log/cleanup")
    public ReturnResult<Integer> cleanupLogs(@RequestParam(defaultValue = "30") int retentionDays) {
        return governance.cleanupLogs(retentionDays);
    }
}
