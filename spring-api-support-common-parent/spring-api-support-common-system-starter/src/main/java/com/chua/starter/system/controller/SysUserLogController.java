package com.chua.starter.system.controller;

import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.system.pojo.user.log.SysUserLogQuery;
import com.chua.starter.system.service.SysUserLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 操作日志接口
 * @since 2024/8/7
 * @author CH
 */
@Tag(name = "操作日志接口")
@RestController
@RequestMapping("/v2/user/log")
@RequiredArgsConstructor
public class SysUserLogController {

    /** SYS用户日志服务 */
    private final SysUserLogService sysUserLogService;
    /**
     * 操作日志分页列表
     *
     * @param sysUserLogQuery 条件
     * @return 操作日志分页列表
     */
    @Operation(summary = "操作日志分页列表")
    @GetMapping("/mine-logs")
    public ReturnPageResult<SysUserLog> mineLogs(@ParameterObject Query<SysUserLog> query,
                                                 @ParameterObject SysUserLogQuery sysUserLogQuery,
                                                 @Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId,
                                                 @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysUserLogService.mineLogs(query, sysUserLogQuery, sysUserId, roles));
    }

    /**
     * 操作日志分页列表
     *
     * @param sysUserLogQuery 条件
     * @return 操作日志分页列表
     */
    @Operation(summary = "操作日志分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysUserLog> pageForRole(@ParameterObject Query<SysUserLog> query,
                                                    @ParameterObject SysUserLogQuery sysUserLogQuery,
                                                    @Parameter(hidden = true) @UserValue("sysUserId") Integer sysUserId,
                                                    @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                                                    @Parameter(hidden = true) @UserValue("managedDeptTreeIds") Object managedDeptTreeIds) {
        return PageResultUtils.ok(sysUserLogService.pageForUserLog(
                query,
                sysUserLogQuery,
                sysUserId,
                roles,
                normalizeManagedDeptTreeIds(managedDeptTreeIds)
        ));
    }

    /**
     * normalizedept树id令牌
     *
     * @param value 值
     * @return normalizedept树id令牌的结果
     */
    private String normalizeDeptTreeIdToken(Object value) {
        if (value == null) {
            return null;
        }
        String normalized = String.valueOf(value).trim();
        if (normalized.isBlank()) {
            return null;
        }
        while (normalized.startsWith("[") || normalized.startsWith("\"") || normalized.startsWith("'")) {
            normalized = normalized.substring(1).trim();
        }
        while (normalized.endsWith("]") || normalized.endsWith("\"") || normalized.endsWith("'")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        return normalized.isBlank() ? null : normalized;
    }

    /**
     * normalizemanageddept树标识
     *
     * @param managedDeptTreeIds manageddept树标识
     * @return normalizemanageddept树标识的结果
     */
    private List<String> normalizeManagedDeptTreeIds(Object managedDeptTreeIds) {
        if (managedDeptTreeIds == null) {
            return Collections.emptyList();
        }
        if (managedDeptTreeIds instanceof Collection<?> collection) {
            List<String> values = new ArrayList<>(collection.size());
            for (Object item : collection) {
                String normalized = normalizeDeptTreeIdToken(item);
                if (normalized != null) {
                    values.add(normalized);
                }
            }
            return values;
        }
        if (managedDeptTreeIds.getClass().isArray()) {
            int length = Array.getLength(managedDeptTreeIds);
            List<String> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                String normalized = normalizeDeptTreeIdToken(Array.get(managedDeptTreeIds, i));
                if (normalized != null) {
                    values.add(normalized);
                }
            }
            return values;
        }
        if (managedDeptTreeIds instanceof String value) {
            if (value.isBlank()) {
                return Collections.emptyList();
            }
            String cleaned = value.trim();
            if (cleaned.startsWith("[") && cleaned.endsWith("]") && cleaned.length() >= 2) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }
            if (cleaned.isBlank()) {
                return Collections.emptyList();
            }
            String[] split = cleaned.split(",");
            List<String> values = new ArrayList<>(split.length);
            for (String item : split) {
                String normalized = normalizeDeptTreeIdToken(item);
                if (normalized != null) {
                    values.add(normalized);
                }
            }
            return values;
        }
        String normalized = normalizeDeptTreeIdToken(managedDeptTreeIds);
        return normalized == null ? Collections.emptyList() : Collections.singletonList(normalized);
    }

}
