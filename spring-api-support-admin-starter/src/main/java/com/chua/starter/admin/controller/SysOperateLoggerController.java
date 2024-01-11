package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.date.DateTime;
import com.chua.starter.admin.entity.SysOperateLogger;
import com.chua.starter.admin.result.LogStaticResult;
import com.chua.starter.admin.service.SysOperateLoggerService;
import com.chua.starter.admin.utils.ServicePageResultUtils;
import com.chua.starter.common.support.logger.LoggerIgnore;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.PageRequest;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * <p>
 * 系统日志信息表 前端控制器
 * </p>
 *
 * @author CH
 * @since 2023-05-25
 */
@RestController
@Tag(name = "系统日志信息")
@AllArgsConstructor
@RequestMapping("/v1/log")
public class SysOperateLoggerController {
    private final SysOperateLoggerService sysLoggerService;

    /**
     * 分页查询数据
     *
     * @return 分页结果
     */
    @Operation(summary = "分页查询基础数据")
    @GetMapping("page")
    @LoggerIgnore
    public ReturnPageResult<SysOperateLogger> page(PageRequest<SysOperateLogger> page,
                                                   @Parameter(description = "开始时间") Date startTime,
                                                   @Parameter(description = "结束时间") Date endTime,
                                                   @Parameter(description = "关键词") String keyword,
                                                   @Parameter(description = "结果状态") Long logStatus
    ) {
        return ServicePageResultUtils.ok(sysLoggerService.page(page.createPage(),
                Wrappers.<SysOperateLogger>lambdaQuery()
                        .ge(null != startTime, SysOperateLogger::getCreateTime, startTime)
                        .le(null != endTime, SysOperateLogger::getCreateTime, endTime)
                        .eq(null != logStatus, SysOperateLogger::getLogStatus, logStatus)
                        .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper.like(StringUtils.isNotEmpty(keyword), SysOperateLogger::getLogName, keyword)
                                .or().like(StringUtils.isNotEmpty(keyword), SysOperateLogger::getLogModule, keyword))
                        .orderByDesc(SysOperateLogger::getCreateTime)));
    }

    /**
     * 分页查询数据
     *
     * @return 分页结果
     */
    @Operation(summary = "当前账号的7天日志")
    @GetMapping("mylog")
    @LoggerIgnore
    public ReturnPageResult<SysOperateLogger> mylog(PageRequest<SysOperateLogger> page,
                                                    @Parameter(hidden = true) @UserValue("userId") String userId,
                                                    @Parameter(hidden = true) @UserValue("username") String username
    ) {
        DateTime dateTime = DateTime.now();
        return ServicePageResultUtils.ok(sysLoggerService.page(page.createPage(),
                Wrappers.<SysOperateLogger>lambdaQuery()
                        .eq(SysOperateLogger::getCreateName, username)
                        .between(SysOperateLogger::getCreateTime,
                                dateTime.minusDays(6).withFirstTimeOfDay().toLocalDateTime(),
                                dateTime.withLastTimeOfDay().toLocalDateTime()
                        )
                        .orderByDesc(SysOperateLogger::getCreateTime)));
    }

    /**
     * 查询近7数据
     *
     * @return 查询近7数据
     */
    @Operation(summary = "查询近7数据")
    @GetMapping("near")
    @LoggerIgnore
    public ReturnResult<LogStaticResult> near() {
        DateTime dateTime = DateTime.now();
        return Result.ok(sysLoggerService.near(
                dateTime.minusDays(6).withFirstTimeOfDay().toLocalDateTime(),
                dateTime.withLastTimeOfDay().toLocalDateTime()
        ));
    }
}
