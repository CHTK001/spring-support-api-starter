package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.date.DateTime;
import com.chua.starter.common.support.logger.LoggerIgnore;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.PageRequest;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.admin.entity.SysLogger;
import com.chua.starter.admin.result.LogStaticResult;
import com.chua.starter.admin.service.SysLoggerService;
import com.chua.starter.admin.utils.ServicePageResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api(tags = "系统日志信息")
@AllArgsConstructor
@RequestMapping("/v1/log")
public class SysLogController {
    private final SysLoggerService sysLoggerService;

    /**
     * 分页查询数据
     *
     * @return 分页结果
     */
    @ApiOperation("分页查询基础数据")
    @GetMapping("page")
    @LoggerIgnore
    public ReturnPageResult<SysLogger> page(PageRequest<SysLogger> page,
                                            @ApiParam("开始时间") Date startTime,
                                            @ApiParam("结束时间") Date endTime,
                                            @ApiParam("关键词") String keyword,
                                            @ApiParam("结果状态") Long logStatus
    ) {
        return ServicePageResultUtils.ok(sysLoggerService.page(page.createPage(),
                Wrappers.<SysLogger>lambdaQuery()
                        .ge(null != startTime, SysLogger::getCreateTime, startTime)
                        .le(null != endTime, SysLogger::getCreateTime, endTime)
                        .eq(null != logStatus, SysLogger::getLogStatus, logStatus)
                        .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper.like(StringUtils.isNotEmpty(keyword), SysLogger::getLogName, keyword)
                                .or().like(StringUtils.isNotEmpty(keyword), SysLogger::getLogAction, keyword))
                        .orderByDesc(SysLogger::getCreateTime)));
    }

    /**
     * 分页查询数据
     *
     * @return 分页结果
     */
    @ApiOperation("当前账号的7天日志")
    @GetMapping("mylog")
    @LoggerIgnore
    public ReturnPageResult<SysLogger> mylog(PageRequest<SysLogger> page,
                                             @ApiParam(hidden = true) @UserValue("userId") String userId,
                                             @ApiParam(hidden = true) @UserValue("username") String username
    ) {
        DateTime dateTime = DateTime.now();
        return ServicePageResultUtils.ok(sysLoggerService.page(page.createPage(),
                Wrappers.<SysLogger>lambdaQuery()
                        .eq(SysLogger::getCreateName, username)
                        .between(SysLogger::getCreateTime,
                                dateTime.minusDays(6).withFirstTimeOfDay().toLocalDateTime(),
                                dateTime.withLastTimeOfDay().toLocalDateTime()
                        )
                        .orderByDesc(SysLogger::getCreateTime)));
    }

    /**
     * 查询近7数据
     *
     * @return 查询近7数据
     */
    @ApiOperation("查询近7数据")
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
