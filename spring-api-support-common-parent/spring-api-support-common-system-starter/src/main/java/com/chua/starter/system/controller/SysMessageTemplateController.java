package com.chua.starter.system.controller;

import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.validator.group.AddGroup;
import com.chua.starter.system.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.entity.system.SysMessageTemplate;
import com.chua.starter.system.service.SysMessageTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;

/**
 * 消息模板接口
 *
 * @since 4.0.0.50
 * @author CH
 */
@Tag(name = "消息模板接口")
@RestController
@RequestMapping("/v2/message/template")
@RequiredArgsConstructor
public class SysMessageTemplateController {

    /**
     * 消息模板服务
    */
    private final SysMessageTemplateService sysMessageTemplateService;

    /**
     * 模板分页列表
     */
    @Operation(summary = "消息模板分页列表")
    @GetMapping("/page")
    @Permission(value = "sys:message:template:page", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnPageResult<SysMessageTemplate> page(@ParameterObject Query<SysMessageTemplate> query,
                                                     @ParameterObject SysMessageTemplate template) {
        return PageResultUtils.ok(sysMessageTemplateService.pageForTemplate(query, template));
    }

    /**
     * 新增模板
     */
    @Operation(summary = "新增消息模板")
    @PostMapping("/save")
    @SysLog
    @Permission(value = "sys:message:template:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysMessageTemplate> save(@Validated(AddGroup.class) @RequestBody SysMessageTemplate template,
                                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysMessageTemplateService.saveTemplate(template);
    }

    /**
     * 修改模板
     */
    @Operation(summary = "修改消息模板")
    @PutMapping("/update")
    @SysLog
    @Permission(value = "sys:message:template:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(@Validated(UpdateGroup.class) @RequestBody SysMessageTemplate template,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysMessageTemplateService.updateTemplate(template);
    }

    /**
     * 删除模板(支持批量)
     * @param ids 方法入参 ids
     * @return Return结果 对象
     */
    @Operation(summary = "删除消息模板")
    @DeleteMapping("/delete")
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:message:template:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(@RequestParam String ids) {
        if (null == ids || ids.trim().isEmpty()) {
            return ReturnResult.illegal("模板ID不能为空");
        }
        List<Long> idList = Arrays.stream(ids.split(",")).map(String::trim)
                .filter(s -> !s.isEmpty()).map(Long::valueOf).toList();
        return sysMessageTemplateService.deleteTemplate(idList);
    }
}
