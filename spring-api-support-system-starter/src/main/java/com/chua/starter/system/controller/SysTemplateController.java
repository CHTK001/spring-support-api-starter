package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysTemplate;
import com.chua.starter.system.service.SysTemplateService;
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
import java.util.Set;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 模板接口
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "模板接口")
@RestController
@RequestMapping("/v2/template")
@RequiredArgsConstructor
public class SysTemplateController {

    private final SysTemplateService sysTemplateService;


    /**
     * 模板分页列表
     *
     * @param sysTemplate 条件
     * @return 模板分页列表
     */
    @Operation(summary = "模板分页列表")
    @GetMapping("/page")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'cache:page:sys:template:id:' + #sysTemplate.sysTemplateGroupId")
    public ReturnPageResult<SysTemplate> pageForRole(@ParameterObject Query<SysTemplate> query, 
                                                     @Validated(SelectGroup.class) @ParameterObject SysTemplate sysTemplate,
                                                     @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                                                     BindingResult bindingResult
                                                     ) {
        if (bindingResult.hasErrors()) {
            return ReturnPageResult.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return PageResultUtils.ok(sysTemplateService.page(query, sysTemplate, roles));
    }

    /**
     * 删除模板
     *
     * @return 更新模板
     */
    @Operation(summary = "删除模板")
    @DeleteMapping("/delete")
    @OperateLog
    @UserLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:template:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysTemplateId) {
        if (null == sysTemplateId) {
            return Result.illegal("模板信息不存在");
        }

        return sysTemplateService.delete(sysTemplateId);
    }


    /**
     * 更新模板
     *
     * @return 更新模板
     */
    @Operation(summary = "更新模板")
    @PutMapping("/update")
    @OperateLog
    @Permission(value = "sys:template:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysTemplate sysTemplate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysTemplateService.update(roles, sysTemplate);
    }

    /**
     * 新增模板
     *
     * @return 新增模板
     */
    @Operation(summary = "新增模板")
    @PostMapping("/save")
    @OperateLog
    @Permission(value = "sys:template:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysTemplate> save(
                @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                @Validated(AddGroup.class) @RequestBody SysTemplate sysTemplate,
                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysTemplateService.save(sysTemplate, roles);
    }
}
