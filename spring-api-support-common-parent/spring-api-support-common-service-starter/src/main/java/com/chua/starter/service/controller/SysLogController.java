package com.chua.starter.service.controller;

import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.service.SysLogService;
import com.chua.starter.entity.service.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 系统日志接口
 * @since 2024/8/7
 * @author CH
 */
@Tag(name = "系统日志接口")
@RestController
@RequestMapping("/v2/log")
@RequiredArgsConstructor
public class SysLogController {

    /** SYS日志服务 */
    private final SysLogService sysLogService;

    /**
    * 系统日志分页列表
    *
    * @param sysLog 条件
    * @return 系统日志分页列表
    * @param query 查询
    * @param true true
    * @param roles 方法入参 roles
     */
    @Operation(summary = "系统日志分页列表")
    @GetMapping("/page")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnPageResult<SysLog> pageForRole(@ParameterObject Query<SysLog> query, @ParameterObject SysLog sysLog, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysLogService.pageForLog(query, sysLog, roles));
    }

    /**
     * 导出操作日志
     *
     * @param sysLog    筛选条件（账号/模块/地址/状态）
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return CSV文件下载
     */
    @Operation(summary = "导出操作日志")
    @GetMapping("/export")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ResponseEntity<byte[]> export(@ParameterObject SysLog sysLog,
                                         @RequestParam(required = false) String startDate,
                                         @RequestParam(required = false) String endDate) {
        byte[] data = sysLogService.exportLogs(sysLog, startDate, endDate);
        String filename = "sys_log_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(data);
    }
}
