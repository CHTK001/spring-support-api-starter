package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.service.SystemSoftService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 软件信息控制器
 *
 * 提供软件信息的分页查询、保存、更新以及从仓库同步功能。
 *
 * 作者: CH 时间: 2025/08/14
 */
@RestController
@RequestMapping("v1/soft")
@Tag(name = "软件信息")
@RequiredArgsConstructor
public class SystemSoftController {

    private final SystemSoftService systemSoftService;

    /**
     * 分页查询软件
     *
     * 入参说明: - page.pageNum: 页码 示例: 1 - page.pageSize: 每页数量 示例: 10 -
     * entity.systemSoftName: 软件名称(可选) 示例: nginx - entity.systemSoftCode: 软件编码(可选)
     * 示例: nginx
     *
     * 示例: GET /v1/soft/page?pageNum=1&pageSize=10&systemSoftName=nginx
     */
    @Operation(summary = "分页查询")
    @GetMapping("page")
    public ReturnPageResult<SystemSoft> page(Query<SystemSoft> page, SystemSoft entity) {
        return PageResultUtils.ok(systemSoftService.page(page.createPage(), null));
    }

    /**
     * 新增软件
     *
     * 入参示例(JSON): { "systemSoftName": "Nginx", "systemSoftCode": "nginx",
     * "systemSoftImage": "nginx", "systemSoftTags": "web,proxy" }
     */
    @Operation(summary = "保存")
    @PostMapping("save")
    public ReturnResult<SystemSoft> save(@Validated(AddGroup.class) @RequestBody SystemSoft entity, BindingResult br) {
        if (br.hasErrors()) {
            return ReturnResult.illegal(br.getAllErrors().getFirst().getDefaultMessage());
        }
        systemSoftService.save(entity);
        return ReturnResult.ok(entity);
    }

    /**
     * 更新软件
     *
     * 入参示例(JSON): { "systemSoftId": 1, "systemSoftName": "Nginx", "systemSoftCode":
     * "nginx" }
     */
    @Operation(summary = "更新")
    @PutMapping("update")
    public ReturnResult<Boolean> update(@Validated(UpdateGroup.class) @RequestBody SystemSoft entity,
            BindingResult br) {
        if (br.hasErrors()) {
            return ReturnResult.illegal(br.getAllErrors().getFirst().getDefaultMessage());
        }
        return ReturnResult.ok(systemSoftService.updateById(entity));
    }

    /**
     * 从仓库同步软件及版本信息
     *
     * 行为: 依据软件仓库配置与镜像名/仓库名拉取可用版本并更新到 SystemSoftVersion。 示例: POST /v1/soft/sync
     */
    @Operation(summary = "同步（Docker）")
    @PostMapping("sync")
    public ReturnResult<Boolean> sync() {
        return systemSoftService.syncFromDocker();
    }
}
