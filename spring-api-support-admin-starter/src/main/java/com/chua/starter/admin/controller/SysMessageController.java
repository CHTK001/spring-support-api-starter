package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.admin.entity.SysMessageTemplate;
import com.chua.starter.admin.service.SysMessageTemplateService;
import com.chua.starter.common.support.utils.LocaleUtils;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *消息推送接口
 * @author CH
 */
@Tag(name = "消息推送接口")
@RestController
@RequestMapping("/v2/message")
@RequiredArgsConstructor
@Slf4j
public class SysMessageController {

    private final SysMessageTemplateService smsMessageTemplateService;
    /**
     * 分页查询设置列表
     *
     * @return 分页查询设置列表
     */
    @Operation(summary = "分页查询消息模板")
    @GetMapping("page")
    public ReturnPageResult<SysMessageTemplate> page(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return PageResultUtils.ok(smsMessageTemplateService.page(new Page<>(page, pageSize),
                        Wrappers.<SysMessageTemplate>lambdaQuery()
                                .like(StringUtils.isNotBlank(keyword), SysMessageTemplate::getMessageName, keyword)
                )
        );
    }


    /**
     * 新增消息模板
     *
     * @param template 消息模板对象
     * @return 返回新增的消息模板对象
     */
    @PostMapping("save")
    @Operation(summary = "新增消息模板")
    public ReturnResult<SysMessageTemplate> save(@Validated(AddGroup.class) @RequestBody SysMessageTemplate template, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getFieldError().getDefaultMessage());
        }
        // 生成唯一的消息码
        template.setMessageCode(IdUtils.createUlid());
        // 保存消息模板
        return ReturnResult.of(smsMessageTemplateService.saveTemplate(template));
    }
    /**
     * 更新消息模板
     *
     * @param template 消息模板对象
     * @return 返回新增的消息模板对象
     */
    @PutMapping("update")
    @Operation(summary = "更新消息模板")
    public ReturnResult<SysMessageTemplate> update(@Validated(UpdateGroup.class) @RequestBody SysMessageTemplate template, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getFieldError().getDefaultMessage());
        }
        // 返回新增的消息模板对象
        return ReturnResult.of(smsMessageTemplateService.updateTemplate(template));
    }
    /**
     * 更新消息模板
     *
     * @return 返回新增的消息模板对象
     */
    @DeleteMapping("delete")
    @Operation(summary = "删除消息模板")
    public ReturnResult<Boolean> delete(String messageId) {
        if(StringUtils.isEmpty(messageId)) {
            return ReturnResult.illegal(LocaleUtils.getMessage("message.data.does.not.exist"));
        }

        return ReturnResult.of(smsMessageTemplateService.deleteTemplate(messageId));
    }
}
