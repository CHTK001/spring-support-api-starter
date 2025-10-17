package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.service.SystemSoftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 系统软件管理(v1) 控制器
 *
 * <p>提供与前端约定的 v1 路由别名，兼容现有前端接口（例如：v1/system/soft/page）。</p>
 *
 * 作者: CH
 * 创建时间: 2025-10-16
 * 版本: 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("v1/system/soft")
@RequiredArgsConstructor
@Tag(name = "系统软件管理(v1)", description = "系统软件 v1 版本兼容接口")
public class SystemSoftV1Controller {

    private final SystemSoftService systemSoftService;

    /**
     * 分页查询软件列表
     *
     * 方法说明: 提供与前端约定的分页查询接口，等价于旧接口的功能。
     * 入参: page 当前页, size 每页大小, softCode 软件代码, softName 软件名称, category 分类, status 状态
     * 出参: ReturnResult<IPage<SystemSoft>> 分页结果
     */
    @Operation(summary = "分页查询软件列表(v1)", description = "v1/system/soft/page")
    @GetMapping("/page")
    public ReturnResult<IPage<SystemSoft>> pageForSoft(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @Parameter(description = "软件代码") @RequestParam(required = false) String softCode,
            @Parameter(description = "软件名称") @RequestParam(required = false) String softName,
            @Parameter(description = "软件分类") @RequestParam(required = false) String category,
            @Parameter(description = "软件状态") @RequestParam(required = false) Integer status) {
        try {
            log.debug("[v1] 分页查询软件列表, page: {}, size: {}, softCode: {}, softName: {}, category: {}, status: {}",
                    page, size, softCode, softName, category, status);

            QueryWrapper<SystemSoft> wrapper = new QueryWrapper<>();
            if (Objects.nonNull(softCode) && !softCode.isEmpty()) {
                wrapper.like("system_soft_code", softCode);
            }
            if (Objects.nonNull(softName) && !softName.isEmpty()) {
                wrapper.like("system_soft_name", softName);
            }
            if (Objects.nonNull(category) && !category.isEmpty()) {
                wrapper.eq("system_soft_category", category);
            }
            if (Objects.nonNull(status)) {
                wrapper.eq("system_soft_status", status);
            }

            IPage<SystemSoft> result = systemSoftService.page(new Page<>(page, size), wrapper);
            log.debug("[v1] 分页查询软件列表成功, count: {}", result.getRecords().size());
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("[v1] 分页查询软件列表失败", e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }
}
