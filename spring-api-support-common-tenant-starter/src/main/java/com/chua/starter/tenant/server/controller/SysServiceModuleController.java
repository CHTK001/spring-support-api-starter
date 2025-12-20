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
import com.chua.starter.tenant.entity.SysServiceModule;
import com.chua.starter.tenant.entity.SysServiceModuleItem;
import com.chua.starter.tenant.entity.SysServiceModuleStats;
import com.chua.starter.tenant.server.mapper.SysServiceModuleItemMapper;
import com.chua.starter.tenant.server.mapper.SysServiceModuleMapper;
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

/**
 * 服务模块管理接口
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/15
 */
@RestController
@Slf4j
@Api(tags = "服务模块管理接口")
@Tag(name = "服务模块管理接口")
@RequestMapping("/v2/service/module")
@RequiredArgsConstructor
public class SysServiceModuleController {

    private final SysServiceModuleMapper sysServiceModuleMapper;
    private final SysServiceModuleItemMapper sysServiceModuleItemMapper;

    /**
     * 分页查询服务模块
     *
     * @param query           分页查询对象
     * @param sysServiceModule 服务模块查询条件
     * @return 分页结果
     */
    @GetMapping("page")
    @Operation(summary = "分页查询服务模块")
    public ReturnPageResult<SysServiceModule> pageForSysServiceModule(@ParameterObject Query<SysServiceModule> query,
                                                                      @ParameterObject SysServiceModule sysServiceModule) {
        LambdaQueryWrapper<SysServiceModule> wrapper = createWrapper(sysServiceModule);
        Page<SysServiceModule> page = sysServiceModuleMapper.selectPage(query.createPage(), wrapper);
        return ReturnPageResultUtils.ok(page);
    }

    /**
     * 查询服务模块列表
     *
     * @param sysServiceModule 服务模块查询条件
     * @return 服务模块列表
     */
    @GetMapping("list")
    @Operation(summary = "查询服务模块列表")
    public ReturnResult<List<SysServiceModule>> listForSysServiceModule(@ParameterObject SysServiceModule sysServiceModule) {
        LambdaQueryWrapper<SysServiceModule> wrapper = createWrapper(sysServiceModule);
        List<SysServiceModule> list = sysServiceModuleMapper.selectList(wrapper);
        return ReturnResult.ok(list);
    }

    /**
     * 获取服务模块详情
     *
     * @param sysServiceModuleId 服务模块ID
     * @return 服务模块详情
     */
    @GetMapping("get")
    @Operation(summary = "获取服务模块详情")
    public ReturnResult<SysServiceModule> getForSysServiceModule(@Parameter(description = "服务模块ID") Integer sysServiceModuleId) {
        if (sysServiceModuleId == null) {
            return ReturnResult.error("服务模块ID不能为空");
        }
        return ReturnResult.ok(sysServiceModuleMapper.selectById(sysServiceModuleId));
    }

    /**
     * 保存服务模块
     *
     * @param sysServiceModule 服务模块对象
     * @param bindingResult    校验结果
     * @return 保存结果
     */
    @PostMapping("save")
    @Operation(summary = "保存服务模块")
    public ReturnResult<SysServiceModule> saveForSysServiceModule(@RequestBody @Validated(AddGroup.class) SysServiceModule sysServiceModule,
                                                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        // 检查编码是否重复
        if (isCodeExists(sysServiceModule.getSysServiceModuleCode(), null)) {
            return ReturnResult.error("服务模块编码已存在");
        }
        sysServiceModuleMapper.insert(sysServiceModule);
        return ReturnResult.ok(sysServiceModule);
    }

    /**
     * 更新服务模块
     *
     * @param sysServiceModule 服务模块对象
     * @param bindingResult    校验结果
     * @return 更新结果
     */
    @PutMapping("update")
    @Operation(summary = "更新服务模块")
    public ReturnResult<Boolean> updateForSysServiceModule(@RequestBody @Validated(UpdateGroup.class) SysServiceModule sysServiceModule,
                                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        // 检查编码是否重复
        if (isCodeExists(sysServiceModule.getSysServiceModuleCode(), sysServiceModule.getSysServiceModuleId())) {
            return ReturnResult.error("服务模块编码已存在");
        }
        sysServiceModuleMapper.updateById(sysServiceModule);
        return ReturnResult.ok(true);
    }

    /**
     * 删除服务模块
     *
     * @param sysServiceModuleId 服务模块ID
     * @return 删除结果
     */
    @DeleteMapping("delete")
    @Operation(summary = "删除服务模块")
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteForSysServiceModule(@Parameter(description = "服务模块ID") Integer sysServiceModuleId) {
        if (sysServiceModuleId == null) {
            return ReturnResult.error("服务模块ID不能为空");
        }
        // 检查是否被服务引用
        Long count = sysServiceModuleItemMapper.selectCount(
                Wrappers.<SysServiceModuleItem>lambdaQuery()
                        .eq(SysServiceModuleItem::getSysServiceModuleId, sysServiceModuleId)
        );
        if (count > 0) {
            return ReturnResult.error("该模块正在被服务使用，无法删除");
        }
        sysServiceModuleMapper.deleteById(sysServiceModuleId);
        return ReturnResult.ok(true);
    }

    /**
     * 更新服务模块状态
     *
     * @param sysServiceModuleId 服务模块ID
     * @param status             状态值 (0-启用, 1-禁用)
     * @return 更新结果
     */
    @PutMapping("status")
    @Operation(summary = "更新服务模块状态")
    public ReturnResult<Boolean> updateStatusForSysServiceModule(
            @Parameter(description = "服务模块ID") Integer sysServiceModuleId,
            @Parameter(description = "状态值: 0-启用, 1-禁用") Integer status) {
        if (sysServiceModuleId == null) {
            return ReturnResult.error("服务模块ID不能为空");
        }
        SysServiceModule sysServiceModule = new SysServiceModule();
        sysServiceModule.setSysServiceModuleId(sysServiceModuleId);
        sysServiceModule.setSysServiceModuleStatus(status);
        sysServiceModuleMapper.updateById(sysServiceModule);
        return ReturnResult.ok(true);
    }

    /**
     * 获取服务模块统计数据
     *
     * @return 统计数据（总数、启用数、禁用数、API模块数、服务模块数）
     */
    @GetMapping("stats")
    @Operation(summary = "获取服务模块统计数据")
    public ReturnResult<SysServiceModuleStats> statsForSysServiceModule() {
        SysServiceModuleStats stats = new SysServiceModuleStats();
        // 总数
        stats.setTotal(sysServiceModuleMapper.selectCount(Wrappers.emptyWrapper()));
        // 启用数 (status = 0 表示启用)
        stats.setEnabled(sysServiceModuleMapper.selectCount(
                Wrappers.<SysServiceModule>lambdaQuery().eq(SysServiceModule::getSysServiceModuleStatus, 0)
        ));
        // 禁用数 (status = 1 表示禁用)
        stats.setDisabled(sysServiceModuleMapper.selectCount(
                Wrappers.<SysServiceModule>lambdaQuery().eq(SysServiceModule::getSysServiceModuleStatus, 1)
        ));
        // API类型模块数
        stats.setApiCount(sysServiceModuleMapper.selectCount(
                Wrappers.<SysServiceModule>lambdaQuery().eq(SysServiceModule::getSysServiceModuleType, "API")
        ));
        // 服务类型模块数
        stats.setServiceCount(sysServiceModuleMapper.selectCount(
                Wrappers.<SysServiceModule>lambdaQuery().ne(SysServiceModule::getSysServiceModuleType, "API")
        ));
        return ReturnResult.ok(stats);
    }

    /**
     * 创建查询条件
     */
    private LambdaQueryWrapper<SysServiceModule> createWrapper(SysServiceModule sysServiceModule) {
        LambdaQueryWrapper<SysServiceModule> wrapper = Wrappers.lambdaQuery();
        if (sysServiceModule != null) {
            if (StringUtils.isNotBlank(sysServiceModule.getSysServiceModuleName())) {
                wrapper.like(SysServiceModule::getSysServiceModuleName, sysServiceModule.getSysServiceModuleName());
            }
            if (StringUtils.isNotBlank(sysServiceModule.getSysServiceModuleCode())) {
                wrapper.like(SysServiceModule::getSysServiceModuleCode, sysServiceModule.getSysServiceModuleCode());
            }
            if (sysServiceModule.getSysServiceModuleStatus() != null) {
                wrapper.eq(SysServiceModule::getSysServiceModuleStatus, sysServiceModule.getSysServiceModuleStatus());
            }
        }
        wrapper.orderByAsc(SysServiceModule::getSysServiceModuleSort);
        return wrapper;
    }

    /**
     * 检查编码是否存在
     */
    private boolean isCodeExists(String code, Integer excludeId) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        LambdaQueryWrapper<SysServiceModule> wrapper = Wrappers.<SysServiceModule>lambdaQuery()
                .eq(SysServiceModule::getSysServiceModuleCode, code);
        if (excludeId != null) {
            wrapper.ne(SysServiceModule::getSysServiceModuleId, excludeId);
        }
        return sysServiceModuleMapper.selectCount(wrapper) > 0;
    }
}
