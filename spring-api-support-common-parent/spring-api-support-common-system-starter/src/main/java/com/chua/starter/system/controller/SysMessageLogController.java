package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.entity.system.SysMessageLog;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.service.SysMessageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消息推送日志接口
 *
 * @since 4.0.0.50
 * @author CH
 */
@Tag(name = "消息推送日志接口")
@RestController
@RequestMapping("/v2/message/log")
@RequiredArgsConstructor
public class SysMessageLogController {

    /**
     * 推送日志服务
    */
    private final SysMessageLogService sysMessageLogService;

    /**
     * 推送日志分页列表
     */
    @Operation(summary = "消息推送日志分页列表")
    @GetMapping("/page")
    @Permission(value = "sys:message:log:page", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnPageResult<SysMessageLog> page(@ParameterObject Query<SysMessageLog> query,
                                                @ParameterObject SysMessageLog log) {
        return PageResultUtils.ok(sysMessageLogService.pageForLog(query, log));
    }

    /**
     * 清理指定时间之前的推送日志
     *
     * @param beforeTime 截止时间，支持 {@code yyyy-MM-dd} 与 ISO 日期时间两种写法
     * @return 删除条数
     */
    @Operation(summary = "清理消息推送日志")
    @PostMapping("/clean")
    @SysLog(module = "清理")
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:message:log:clean", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Integer> clean(@RequestParam String beforeTime) {
        return sysMessageLogService.cleanLog(parse(beforeTime));
    }

    /**
     * 解析截止时间，仅给日期时按当天零点处理
     *
     * @param beforeTime 截止时间字符串
     * @return 截止时间，格式非法时返回空由服务层给出提示
     */
    private LocalDateTime parse(String beforeTime) {
        if (null == beforeTime) {
            return null;
        }
        String value = beforeTime.trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
            // 不是完整日期时间，继续按日期解析
        }
        try {
            return LocalDate.parse(value).atStartOfDay();
        } catch (Exception ignored) {
            return null;
        }
    }
}
