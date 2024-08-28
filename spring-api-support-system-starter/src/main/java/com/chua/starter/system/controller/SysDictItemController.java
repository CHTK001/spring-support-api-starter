package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.UserLogger;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;
import static com.chua.starter.system.constant.SystemConstant.CACHE_WITH_REDIS;

/**
 * 字典子项接口
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
     * 字典子项列表
     *
     * @return 字典子项列表
     */
    @Operation(summary = "字典子项列表")
    @GetMapping("/list")
    @Cacheable(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:sys:dict:id:' + #sysDictId")
    public ReturnResult<List<SysDictItem>> list(@ParameterObject Query<SysDictItem> query, Integer sysDictId, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if(null == sysDictId) {
            return ReturnResult.illegal("字典不能为空");
        }
        return ReturnResult.ok(sysDictItemService.listForItem(query, sysDictId, roles));
    }
    /**
     * 字典子项分页列表
     *
     * @return 字典子项分页列表
     */
    @Operation(summary = "字典子项列表")
    @GetMapping("/page")
    @Cacheable(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:sys:dict:id:' + #sysDictId")
    public ReturnPageResult<SysDictItem> page(@ParameterObject Query<SysDictItem> query, Integer sysDictId, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if(null == sysDictId) {
            return ReturnPageResult.illegal("字典不能为空");
        }
        return PageResultUtils.ok(sysDictItemService.pageForItem(query, sysDictId, roles));
    }

    /**
     * 删除字典子项
     *
     * @return 更新字典子项
     */
    @Operation(summary = "删除字典子项")
    @DeleteMapping("/delete")
    @OperateLog
    @UserLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:dict:item:delete", role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:sys:dict:item:id:' + #sysDictItemId")
    public ReturnResult<Boolean> delete(Long sysDictItemId) {
        if (null == sysDictItemId) {
            return Result.illegal("字典子项信息不存在");
        }

        return sysDictItemService.delete(sysDictItemId);
    }


    /**
     * 更新字典子项
     *
     * @return 更新字典子项
     */
    @Operation(summary = "更新字典子项")
    @PutMapping("/update")
    @OperateLog
    @CacheEvict(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:sys:dict:item:id:' + #sysDictItem.sysDictItemId")
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
     * 新增字典子项
     *
     * @return 新增字典子项
     */
    @Operation(summary = "新增字典子项")
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
