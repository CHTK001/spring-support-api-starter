package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.result.PushResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.system.service.SysEmailService;
import com.chua.starter.system.entity.SysEmailTemplate;
import com.chua.starter.system.pojo.email.EmailPageV1Request;
import com.chua.starter.system.pojo.email.EmailRequest;
import com.chua.starter.system.pojo.email.EmailSyncV1Request;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 邮件接口
 *
 * @author CH
 * @since 2024/9/4
 */
@RestController
@Slf4j
@Api(tags = "邮件接口")
@Tag(name = "邮件接口")
@RequestMapping("/v2/project/template/email")
@RequiredArgsConstructor
public class SysProjectEmailController {

    final SysEmailService sysEmailService;

    /**
     * 校验码
     *
     * @param request 请求
     */
    @ApiOperation("发送邮件")
    @Operation(summary = "发送邮件")
    @PutMapping
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<PushResult>> send(@RequestBody EmailRequest request) {
        return sysEmailService.send(request);
    }

    /**
     * 分页
     *
     * @param request 请求
     */
    @ApiOperation("分页")
    @Operation(summary = "分页")
    @GetMapping("/page")
    public ReturnPageResult<SysEmailTemplate> page(@ParameterObject EmailPageV1Request request) {
        return ReturnPageResultUtils.ok(sysEmailService.pageFor(request));
    }

    /**
     * 同步模板
     *
     * @param request 密钥
     */
    @Operation(summary = "同步模板")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @PutMapping("/syncTemplate")
    @ApiOperation("同步模板")
    public ReturnResult<Boolean> syncTemplate(@Validated(SelectGroup.class) @ParameterObject @RequestBody EmailSyncV1Request request,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return sysEmailService.syncTemplate(request);
    }

    /**
     * 删除模板
     *
     * @param sysEmailTemplate 密钥
     */
    @Operation(summary = "删除模板")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @PutMapping("/delete")
    @ApiOperation("删除模板")
    public ReturnResult<Boolean> delete(@Validated(SelectGroup.class) @ParameterObject @RequestBody SysEmailTemplate sysEmailTemplate,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return sysEmailService.delete(sysEmailTemplate);
    }

    /**
     * 更新模板
     *
     * @param sysEmailTemplate 密钥
     */
    @Operation(summary = "更新模板")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @PutMapping("/update")
    @ApiOperation("更新模板")
    public ReturnResult<Boolean> update(@Validated(UpdateGroup.class) @ParameterObject @RequestBody SysEmailTemplate sysEmailTemplate,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return sysEmailService.update(sysEmailTemplate);
    }
    /**
     * 新增模板
     *
     * @param sysEmailTemplate 密钥
     */
    @Operation(summary = "新增模板")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @PostMapping("/save")
    @ApiOperation("新增模板")
    public ReturnResult<SysEmailTemplate> save(@Validated(AddGroup.class) @ParameterObject @RequestBody SysEmailTemplate sysEmailTemplate,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return sysEmailService.save(sysEmailTemplate);
    }
}
