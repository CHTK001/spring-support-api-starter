package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.message.MessagePush;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.spi.SpiOption;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.admin.entity.SysMessageConfig;
import com.chua.starter.admin.entity.SysMessageTemplate;
import com.chua.starter.admin.service.SysMessageConfigService;
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

import java.util.List;

/**
 *消息推送接口
 * @author CH
 */
@Tag(name = "消息推送配置接口")
@RestController
@RequestMapping("/v2/message/config")
@RequiredArgsConstructor
@Slf4j
public class SysMessageConfigController {

    private final SysMessageConfigService sysMessageConfigService;
    /**
     * 分页查询设置列表配置
     *
     * @return 分页查询设置列表
     */
    @Operation(summary = "查询SMS消息实现方式")
    @GetMapping("smsType")
    public ReturnResult<List<SpiOption<String>>> smsType() {
        return ReturnResult.ok(ServiceProvider.of(MessagePush.class).options());
    }
    /**
     * 分页查询设置列表配置
     *
     * @return 分页查询设置列表
     */
    @Operation(summary = "查询消息模板配置")
    @GetMapping("list")
    public ReturnResult<List<SysMessageConfig>> list(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return ReturnResult.ok(sysMessageConfigService.page(new Page<>(1, 100),
                        Wrappers.<SysMessageConfig>lambdaQuery()
                                .like(StringUtils.isNotBlank(keyword), SysMessageConfig::getConfigName, keyword)
                ).getRecords()
        );
    }
    /**
     * 分页查询设置列表
     *
     * @return 分页查询设置列表
     */
    @Operation(summary = "分页查询消息模板配置")
    @GetMapping("page")
    public ReturnPageResult<SysMessageConfig> page(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return PageResultUtils.ok(sysMessageConfigService.page(new Page<>(page, pageSize),
                        Wrappers.<SysMessageConfig>lambdaQuery()
                                .like(StringUtils.isNotBlank(keyword), SysMessageConfig::getConfigName, keyword)
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
    @Operation(summary = "新增消息模板配置")
    public ReturnResult<SysMessageTemplate> save(@Validated(AddGroup.class) @RequestBody SysMessageConfig template, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getFieldError().getDefaultMessage());
        }
        // 保存消息模板
        sysMessageConfigService.save(template);
        // 返回新增的消息模板对象
        return ReturnResult.of(template);
    }
    /**
     * 更新消息模板
     *
     * @param template 消息模板对象
     * @return 返回新增的消息模板对象
     */
    @PutMapping("update")
    @Operation(summary = "更新消息模板配置")
    public ReturnResult<SysMessageTemplate> update(@Validated(UpdateGroup.class) @RequestBody SysMessageConfig template, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getFieldError().getDefaultMessage());
        }
        // 保存消息模板
        sysMessageConfigService.updateById(template);
        // 返回新增的消息模板对象
        return ReturnResult.of(template);
    }
    /**
     * 更新消息模板
     *
     * @return 返回新增的消息模板对象
     */
    @DeleteMapping("delete")
    @Operation(summary = "删除消息模板配置")
    public ReturnResult<Boolean> delete(String configId) {
        if(StringUtils.isEmpty(configId)) {
            return ReturnResult.illegal(LocaleUtils.getMessage("message.data.does.not.exist"));
        }

        return ReturnResult.of(sysMessageConfigService.removeById(configId));
    }
}
