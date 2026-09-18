package com.chua.starter.service.controller;

import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.SysDictItemService;
import com.chua.starter.entity.service.SysDictItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

/**
 * 字典子项接口(缓存)
 * @since 2024/8/7
 * @author CH
 */
@Tag(name = "字典子项接口")
@RestController
@RequestMapping("/v2/dict/item")
@RequiredArgsConstructor
public class SysDictItemController {

    /** sysdictitem服务 */
    private final SysDictItemService sysDictItemService;


    /**
    * 字典子项列表
    *
    * @return 字典子项列表
    * @param query 查询
    * @param sysDictCode sysdict编码
    * @param true true
    * @param roles 方法入参 roles
     */
    @Operation(summary = "字典子项列表(父code码查询)")
    @GetMapping("/pCode")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:dict:pcode:' + #sysDictCode")
    public ReturnResult<List<SysDictItem>> pCode(@ParameterObject Query<SysDictItem> query, String sysDictCode, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if(null == sysDictCode) {
            return ReturnResult.illegal("字典不能为空");
        }
        return ReturnResult.ok(sysDictItemService.listForItemPcode(query, sysDictCode, roles));
    }

    /**
     * 字典子项列表
     *
     * @return 字典子项列表
     * @param query 查询
     * @param sysDictCode sysdict编码
     * @param true true
     * @param roles 方法入参 roles
     */
    @Operation(summary = "字典子项列表(code码查询)")
    @GetMapping("/code")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:dict:code:' + #sysDictCode")
    public ReturnResult<List<SysDictItem>> code(@ParameterObject Query<SysDictItem> query, String sysDictCode, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if(null == sysDictCode) {
            return ReturnResult.illegal("字典不能为空");
        }
        return ReturnResult.ok(sysDictItemService.listForItemCode(query, sysDictCode, roles));
    }


    /**
     * 字典子项列表
     *
     * @return 字典子项列表
     * @param query 查询
     * @param sysDictId sysDictId
     * @param true true
     * @param roles 方法入参 roles
     */
    @Operation(summary = "字典子项列表(父ID)")
    @GetMapping("/pList")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:dict:pid:' + #sysDictId")
    public ReturnResult<List<SysDictItem>> pList(@ParameterObject Query<SysDictItem> query, Integer sysDictId, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if(null == sysDictId) {
            return ReturnResult.illegal("字典不能为空");
        }
        return ReturnResult.ok(sysDictItemService.listForItemPid(query, sysDictId, roles));
    }

    /**
     * 字典子项列表
     *
     * @return 字典子项列表
     * @param query 查询
     * @param sysDictId sysDictId
     * @param true true
     * @param roles 方法入参 roles
     */
    @Operation(summary = "字典子项列表")
    @GetMapping("/list")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:dict:id:' + #sysDictId")
    public ReturnResult<List<SysDictItem>> list(@ParameterObject Query<SysDictItem> query, Integer sysDictId, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if(null == sysDictId) {
            return ReturnResult.illegal("字典不能为空");
        }
        return ReturnResult.ok(sysDictItemService.listForItemId(query, sysDictId, roles));
    }
    /**
     * 字典子项分页列表
     *
     * @return 字典子项分页列表
     * @param query 查询
     * @param sysDictId sysDictId
     * @param true true
     * @param roles 方法入参 roles
     */
    @Operation(summary = "字典子项分页")
    @GetMapping("/page")
    public ReturnPageResult<SysDictItem> page(@ParameterObject Query<SysDictItem> query, Integer sysDictId, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if(null == sysDictId) {
            return PageResultUtils.error("字典不能为空");
        }
        return PageResultUtils.ok(sysDictItemService.pageForItem(query, sysDictId, roles));
    }

    /**
     * 删除字典子项
     *
     * @return 更新字典子项
     * @param sysDictItemId sysDictItemId
     */
    @Operation(summary = "删除字典子项")
    @DeleteMapping("/delete")
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:dict:item:delete", role = {"ADMIN", AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysDictItemId) {
        if (null == sysDictItemId) {
            return ReturnResult.illegal("字典子项信息不存在");
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
    @Permission(value = "sys:dict:item:update", role = {"ADMIN", AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysDictItem sysDictItem,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
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
    @Permission(value = "sys:dict:item:add", role = {"ADMIN", AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysDictItem> save(
                @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                @Validated(AddGroup.class) @RequestBody SysDictItem sysDictItem,
                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysDictItemService.save(sysDictItem, roles);
    }
}
