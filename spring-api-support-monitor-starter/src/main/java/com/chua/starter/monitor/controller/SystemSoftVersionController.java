package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.monitor.entity.SystemSoftVersion;
import com.chua.starter.monitor.service.SystemSoftVersionService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 软件版本控制器
 *
 * 提供软件版本的分页查询、保存与更新。
 *
 * 作者: CH 时间: 2025/08/14
 */
@RestController
@RequestMapping("v1/soft/version")
@Tag(name = "软件版本")
@RequiredArgsConstructor
public class SystemSoftVersionController {

    private final SystemSoftVersionService systemSoftVersionService;

    /**
     * 分页查询版本 入参: page.pageNum/page.pageSize, entity.systemSoftId(可选) 示例: GET
     * /v1/soft/version/page?pageNum=1&pageSize=10&systemSoftId=1
     */
    @Operation(summary = "分页查询")
    @GetMapping("page")
    public ReturnPageResult<SystemSoftVersion> page(Query<SystemSoftVersion> page, SystemSoftVersion entity) {
        return PageResultUtils.ok(systemSoftVersionService.page(page.createPage(), null));
    }

    /**
     * 新增版本 示例(JSON): { "systemSoftId": 1, "version": "1.25", "imageTag":
     * "nginx:1.25" }
     */
    @Operation(summary = "保存")
    @PostMapping("save")
    public ReturnResult<SystemSoftVersion> save(@Validated(AddGroup.class) @RequestBody SystemSoftVersion entity,
            BindingResult br) {
        if (br.hasErrors()) {
            return ReturnResult.illegal(br.getAllErrors().getFirst().getDefaultMessage());
        }
        systemSoftVersionService.save(entity);
        return ReturnResult.ok(entity);
    }

    /**
     * 更新版本 示例(JSON): { "systemSoftVersionId": 10, "version": "1.26" }
     */
    @Operation(summary = "更新")
    @PutMapping("update")
    public ReturnResult<Boolean> update(@Validated(UpdateGroup.class) @RequestBody SystemSoftVersion entity,
            BindingResult br) {
        if (br.hasErrors()) {
            return ReturnResult.illegal(br.getAllErrors().getFirst().getDefaultMessage());
        }
        return ReturnResult.ok(systemSoftVersionService.updateById(entity));
    }
}
