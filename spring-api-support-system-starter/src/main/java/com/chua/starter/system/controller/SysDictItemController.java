package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.logger.SysLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.service.SysDictItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 字典接口
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "字典子项接口")
@RestController
@RequestMapping("/v2/dict/item")
@RequiredArgsConstructor
public class SysDictItemController {

    private final SysDictItemService sysDictItemService;


    /**
     * 字典分页列表
     *
     * @param sysDictItem 条件
     * @return 字典分页列表
     */
    @Operation(summary = "字典分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysDictItem> pageForRole(Query<SysDictItem> query, @Validated(SelectGroup.class) SysDictItem sysDictItem, @Parameter(hidden = true) @UserValue("roles") Set<String> roles, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnPageResult.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return PageResultUtils.ok(sysDictItemService.pageForDictItem(query, sysDictItem, roles));
    }

    /**
     * 删除字典
     *
     * @return 更新字典
     */
    @Operation(summary = "删除字典")
    @DeleteMapping("/delete")
    @OperateLog
    @SysLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:dict:item:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long dictItemId) {
        if (null == dictItemId) {
            return Result.illegal("字典信息不存在");
        }

        return sysDictItemService.delete(dictItemId);
    }


    /**
     * 更新字典
     *
     * @return 更新字典
     */
    @Operation(summary = "更新字典")
    @PutMapping("/update")
    @OperateLog
    @Permission(value = "sys:dict:item:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysDictItem sysDictItem,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysDictItemService.update(roles, sysDictItem);
    }

    /**
     * 新增字典
     *
     * @return 新增字典
     */
    @Operation(summary = "新增字典")
    @PostMapping("/save")
    @OperateLog
    @Permission(value = "sys:dict:item:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysDictItem> save(
                @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                @Validated(AddGroup.class) @RequestBody SysDictItem sysDictItem,
                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysDictItemService.save(sysDictItem, roles);
    }
}
