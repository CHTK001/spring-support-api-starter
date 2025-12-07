package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.common.support.result.Result;
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

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * 字典子项接口(缓存)
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
    @Operation(summary = "字典子项列表(父code码查询)")
    @GetMapping("/pCode")
    @ApiCacheKey("'sys:dict:pcode:' + #sysDictCode")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
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
     */
    @Operation(summary = "字典子项列表(code码查询)")
    @GetMapping("/code")
    @ApiCacheKey("'sys:dict:code:' + #sysDictCode")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
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
     */
    @Operation(summary = "字典子项列表(父ID)")
    @GetMapping("/pList")
    @ApiCacheKey("'sys:dict:pid:' + #sysDictId")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
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
     */
    @Operation(summary = "字典子项列表")
    @GetMapping("/list")
    @ApiCacheKey("'sys:dict:id:' + #sysDictId")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
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
     */
    @Operation(summary = "字典子项分页")
    @GetMapping("/page")
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
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:dict:item:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
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
    @Permission(value = "sys:dict:item:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysDictItem sysDictItem,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
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
    @Permission(value = "sys:dict:item:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysDictItem> save(
                @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                @Validated(AddGroup.class) @RequestBody SysDictItem sysDictItem,
                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysDictItemService.save(sysDictItem, roles);
    }
}
