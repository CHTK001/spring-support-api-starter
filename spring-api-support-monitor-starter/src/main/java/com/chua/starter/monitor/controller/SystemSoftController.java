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
 */
@RestController
@RequestMapping("v1/soft")
@Tag(name = "软件信息")
@RequiredArgsConstructor
public class SystemSoftController {

    private final SystemSoftService systemSoftService;

    @Operation(summary = "分页查询")
    @GetMapping("page")
    public ReturnPageResult<SystemSoft> page(Query<SystemSoft> page, SystemSoft entity) {
        return PageResultUtils.ok(systemSoftService.page(page.createPage(), null));
    }

    @Operation(summary = "保存")
    @PostMapping("save")
    public ReturnResult<SystemSoft> save(@Validated(AddGroup.class) @RequestBody SystemSoft entity, BindingResult br) {
        if (br.hasErrors()) {
            return ReturnResult.illegal(br.getAllErrors().getFirst().getDefaultMessage());
        }
        systemSoftService.save(entity);
        return ReturnResult.ok(entity);
    }

    @Operation(summary = "更新")
    @PutMapping("update")
    public ReturnResult<Boolean> update(@Validated(UpdateGroup.class) @RequestBody SystemSoft entity, BindingResult br) {
        if (br.hasErrors()) {
            return ReturnResult.illegal(br.getAllErrors().getFirst().getDefaultMessage());
        }
        return ReturnResult.ok(systemSoftService.updateById(entity));
    }

    @Operation(summary = "同步（Docker）")
    @PostMapping("sync")
    public ReturnResult<Boolean> sync() {
        return systemSoftService.syncFromDocker();
    }
}

