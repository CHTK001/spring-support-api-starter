package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenMessagePushTemplate;
import com.chua.starter.monitor.service.MonitorSysGenMessagePushTemplateService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息推送模板管理
 */
@RestController
@RequestMapping("/v1/gen/server/alert/push-template")
@Tag(name = "消息推送模板管理")
@RequiredArgsConstructor
public class MonitorSysGenMessagePushTemplateController {

    private final MonitorSysGenMessagePushTemplateService templateService;

    @PostMapping
    @Operation(summary = "创建/保存模板")
    public ReturnResult<Boolean> save(@RequestBody MonitorSysGenMessagePushTemplate template) {
        return ReturnResult.ok(templateService.saveOrUpdate(template));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public ReturnResult<Boolean> delete(@PathVariable("id") Integer id) {
        return ReturnResult.ok(templateService.removeById(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询模板")
    public ReturnResult<MonitorSysGenMessagePushTemplate> get(@PathVariable("id") Integer id) {
        return ReturnResult.ok(templateService.getById(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询模板")
    public ReturnPageResult<MonitorSysGenMessagePushTemplate> page(Query<MonitorSysGenMessagePushTemplate> page,
                                                                   MonitorSysGenMessagePushTemplate entity) {
        Page<MonitorSysGenMessagePushTemplate> p = page.createPage();
        return PageResultUtils.ok(templateService.page(p, Wrappers.lambdaQuery(entity)));
    }
}

