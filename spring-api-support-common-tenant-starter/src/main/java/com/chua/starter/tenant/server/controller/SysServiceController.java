package com.chua.starter.tenant.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.tenant.entity.SysService;
import com.chua.starter.tenant.entity.SysServiceModuleItem;
import com.chua.starter.tenant.server.mapper.SysServiceMapper;
import com.chua.starter.tenant.server.mapper.SysServiceModuleItemMapper;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务管理接口
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/15
 */
@RestController
@Slf4j
@Api(tags = "服务管理接口")
@Tag(name = "服务管理接口")
@RequestMapping("/v2/service")
@RequiredArgsConstructor
public class SysServiceController {

    private final SysServiceMapper sysServiceMapper;
    private final SysServiceModuleItemMapper sysServiceModuleItemMapper;

    /**
     * 分页查询服务
     *
     * @param query      分页查询对象
     * @param sysService 服务查询条件
     * @return 分页结果
     */
    @GetMapping("page")
    @Operation(summary = "分页查询服务")
    public ReturnPageResult<SysService> pageForSysService(@ParameterObject Query<SysService> query,
                                                          @ParameterObject SysService sysService) {
        LambdaQueryWrapper<SysService> wrapper = createWrapper(sysService);
        Page<SysService> page = sysServiceMapper.selectPage(query.createPage(), wrapper);
        // 填充关联的模块ID
        fillServiceTags(page.getRecords());
        return ReturnPageResultUtils.ok(page);
    }

    /**
     * 查询服务列表
     *
     * @param sysService 服务查询条件
     * @return 服务列表
     */
    @GetMapping("list")
    @Operation(summary = "查询服务列表")
    public ReturnResult<List<SysService>> listForSysService(@ParameterObject SysService sysService) {
        LambdaQueryWrapper<SysService> wrapper = createWrapper(sysService);
        List<SysService> list = sysServiceMapper.selectList(wrapper);
        // 填充关联的模块ID
        fillServiceTags(list);
        return ReturnResult.ok(list);
    }

    /**
     * 获取服务详情
     *
     * @param sysServiceId 服务ID
     * @return 服务详情
     */
    @GetMapping("get")
    @Operation(summary = "获取服务详情")
    public ReturnResult<SysService> getForSysService(@Parameter(description = "服务ID") Integer sysServiceId) {
        if (sysServiceId == null) {
            return ReturnResult.error("服务ID不能为空");
        }
        SysService sysService = sysServiceMapper.selectById(sysServiceId);
        if (sysService != null) {
            fillServiceTags(List.of(sysService));
        }
        return ReturnResult.ok(sysService);
    }

    /**
     * 保存服务
     *
     * @param sysService    服务对象
     * @param bindingResult 校验结果
     * @return 保存结果
     */
    @PostMapping("save")
    @Operation(summary = "保存服务")
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysService> saveForSysService(@RequestBody @Validated(AddGroup.class) SysService sysService,
                                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        // 检查编码是否重复
        if (isCodeExists(sysService.getSysServiceCode(), null)) {
            return ReturnResult.error("服务编码已存在");
        }
        sysServiceMapper.insert(sysService);
        // 保存关联的模块
        saveServiceModuleItems(sysService);
        return ReturnResult.ok(sysService);
    }

    /**
     * 更新服务
     *
     * @param sysService    服务对象
     * @param bindingResult 校验结果
     * @return 更新结果
     */
    @PutMapping("update")
    @Operation(summary = "更新服务")
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateForSysService(@RequestBody @Validated(UpdateGroup.class) SysService sysService,
                                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        // 检查编码是否重复
        if (isCodeExists(sysService.getSysServiceCode(), sysService.getSysServiceId())) {
            return ReturnResult.error("服务编码已存在");
        }
        sysServiceMapper.updateById(sysService);
        // 更新关联的模块
        saveServiceModuleItems(sysService);
        return ReturnResult.ok(true);
    }

    /**
     * 删除服务
     *
     * @param sysServiceId 服务ID
     * @return 删除结果
     */
    @DeleteMapping("delete")
    @Operation(summary = "删除服务")
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteForSysService(@Parameter(description = "服务ID") Integer sysServiceId) {
        if (sysServiceId == null) {
            return ReturnResult.error("服务ID不能为空");
        }
        // 删除关联的模块关系
        sysServiceModuleItemMapper.delete(
                Wrappers.<SysServiceModuleItem>lambdaQuery()
                        .eq(SysServiceModuleItem::getSysServiceId, sysServiceId)
        );
        sysServiceMapper.deleteById(sysServiceId);
        return ReturnResult.ok(true);
    }

    /**
     * 绑定服务与模块
     *
     * @param sysService 包含服务ID和模块ID列表
     * @return 绑定结果
     */
    @PutMapping("bind")
    @Operation(summary = "绑定服务与模块")
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> bindModulesForSysService(@RequestBody SysService sysService) {
        if (sysService.getSysServiceId() == null) {
            return ReturnResult.error("服务ID不能为空");
        }
        saveServiceModuleItems(sysService);
        return ReturnResult.ok(true);
    }

    /**
     * 更新服务状态
     *
     * @param sysServiceId 服务ID
     * @param status       状态值 (0-启用, 1-禁用)
     * @return 更新结果
     */
    @PutMapping("status")
    @Operation(summary = "更新服务状态")
    public ReturnResult<Boolean> updateStatusForSysService(
            @Parameter(description = "服务ID") Integer sysServiceId,
            @Parameter(description = "状态值: 0-启用, 1-禁用") Integer status) {
        if (sysServiceId == null) {
            return ReturnResult.error("服务ID不能为空");
        }
        SysService sysService = new SysService();
        sysService.setSysServiceId(sysServiceId);
        sysService.setSysServiceStatus(status);
        sysServiceMapper.updateById(sysService);
        return ReturnResult.ok(true);
    }

    /**
     * 获取服务统计数据
     *
     * @return 统计数据（总数、启用数、禁用数）
     */
    @GetMapping("stats")
    @Operation(summary = "获取服务统计数据")
    public ReturnResult<java.util.Map<String, Object>> statsForSysService() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        // 总数
        Long total = sysServiceMapper.selectCount(Wrappers.emptyWrapper());
        // 启用数 (status = 0 表示启用)
        Long enabled = sysServiceMapper.selectCount(
                Wrappers.<SysService>lambdaQuery().eq(SysService::getSysServiceStatus, 0)
        );
        // 禁用数 (status = 1 表示禁用)
        Long disabled = sysServiceMapper.selectCount(
                Wrappers.<SysService>lambdaQuery().eq(SysService::getSysServiceStatus, 1)
        );
        stats.put("total", total);
        stats.put("enabled", enabled);
        stats.put("disabled", disabled);
        return ReturnResult.ok(stats);
    }

    /**
     * 创建查询条件
     */
    private LambdaQueryWrapper<SysService> createWrapper(SysService sysService) {
        LambdaQueryWrapper<SysService> wrapper = Wrappers.lambdaQuery();
        if (sysService != null) {
            if (StringUtils.isNotBlank(sysService.getSysServiceName())) {
                wrapper.like(SysService::getSysServiceName, sysService.getSysServiceName());
            }
            if (StringUtils.isNotBlank(sysService.getSysServiceCode())) {
                wrapper.like(SysService::getSysServiceCode, sysService.getSysServiceCode());
            }
            if (sysService.getSysServiceStatus() != null) {
                wrapper.eq(SysService::getSysServiceStatus, sysService.getSysServiceStatus());
            }
        }
        wrapper.orderByAsc(SysService::getSysServiceSort);
        return wrapper;
    }

    /**
     * 检查编码是否存在
     */
    private boolean isCodeExists(String code, Integer excludeId) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        LambdaQueryWrapper<SysService> wrapper = Wrappers.<SysService>lambdaQuery()
                .eq(SysService::getSysServiceCode, code);
        if (excludeId != null) {
            wrapper.ne(SysService::getSysServiceId, excludeId);
        }
        return sysServiceMapper.selectCount(wrapper) > 0;
    }

    /**
     * 填充服务关联的模块ID列表
     */
    private void fillServiceTags(List<SysService> services) {
        if (services == null || services.isEmpty()) {
            return;
        }
        for (SysService service : services) {
            List<SysServiceModuleItem> items = sysServiceModuleItemMapper.selectList(
                    Wrappers.<SysServiceModuleItem>lambdaQuery()
                            .eq(SysServiceModuleItem::getSysServiceId, service.getSysServiceId())
            );
            if (items != null && !items.isEmpty()) {
                service.setSysServiceTags(
                        items.stream()
                                .map(SysServiceModuleItem::getSysServiceModuleId)
                                .collect(Collectors.toList())
                );
            }
        }
    }

    /**
     * 保存服务与模块的关联关系
     */
    private void saveServiceModuleItems(SysService sysService) {
        if (sysService.getSysServiceId() == null) {
            return;
        }
        // 先删除旧的关联
        sysServiceModuleItemMapper.delete(
                Wrappers.<SysServiceModuleItem>lambdaQuery()
                        .eq(SysServiceModuleItem::getSysServiceId, sysService.getSysServiceId())
        );
        // 添加新的关联
        List<Integer> tags = sysService.getSysServiceTags();
        if (tags != null && !tags.isEmpty()) {
            for (Integer moduleId : tags) {
                SysServiceModuleItem item = new SysServiceModuleItem();
                item.setSysServiceId(sysService.getSysServiceId());
                item.setSysServiceModuleId(moduleId);
                sysServiceModuleItemMapper.insert(item);
            }
        }
    }
}
