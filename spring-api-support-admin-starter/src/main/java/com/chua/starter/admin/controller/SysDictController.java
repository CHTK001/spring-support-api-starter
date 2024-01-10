package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.admin.entity.SysDict;
import com.chua.starter.admin.service.SysDictService;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.logger.SysLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 机构
 *
 * @author CH
 */
@Tag(name = "字典接口")
@RestController
@RequestMapping("/v2/dict")
@RequiredArgsConstructor
public class SysDictController implements Constant {


    private final SysDictService sysDictService;

    /**
     * 字典列表
     *
     * @return 角色列表
     */
    @Operation(summary = "字典列表")
    @GetMapping("page")
    @Cacheable(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dict-' + #dictTypeId")
    public ReturnPageResult<SysDict> page(
            @Parameter(description = "关键词") String keyword,
            @Parameter(description = "字典类型") @RequestParam(value = "dictTypeId", required = false) String dictTypeId,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return PageResultUtils.ok(sysDictService.page(new Page<>(page, pageSize),
                        Wrappers.<SysDict>lambdaQuery()
                                .like(StringUtils.isNotBlank(keyword), SysDict::getDictName, keyword)
                                .like(StringUtils.isNotBlank(keyword), SysDict::getDictRemark, keyword)
                                .eq(SysDict::getDictTypeId, dictTypeId)
                                .orderByAsc(SysDict::getDictSort)
                )
        );
    }

    /**
     * 修改字典
     *
     * @return 修改字典
     */
    @Operation(summary = "修改字典")
    @PutMapping("update")
    @OperateLog
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dict-' + #sysDict.dictTypeId")
    public ReturnResult<Boolean> update(@RequestBody SysDict sysDict) {
        if (null == sysDict.getDictId()) {
            return Result.illegal("唯一标识不能为空");
        }

        return Result.ok(sysDictService.updateById(sysDict));
    }

    /**
     * 新增字典
     *
     * @return 新增字典
     */
    @Operation(summary = "新增字典")
    @PostMapping("save")
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dict-' + #sysDict.dictTypeId")
    public ReturnResult<SysDict> save(@RequestBody SysDict sysDict, @Valid BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        sysDictService.save(sysDict);
        return Result.ok(sysDict);
    }

    /**
     * 删除字典
     *
     * @return 删除字典
     */
    @SysLogger
    @Operation(summary = "删除字典")
    @DeleteMapping("delete")
    @OperateLog
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dict-' + #sysDict1.dictTypeId")
    public ReturnResult<Boolean> delete(@RequestBody SysDict sysDict1) {
        if (null == sysDict1.getDictId()) {
            return Result.illegal("字典不存在");
        }

        SysDict sysDict = sysDictService.getById(sysDict1.getDictId());
        if (null == sysDict) {
            return Result.illegal("字典不存在");
        }

        if (null != sysDict.getDictSys() && sysDict.getDictSys() == 1) {
            return Result.illegal("系统字典不允许删除");
        }


        return Result.ok(sysDictService.removeById(sysDict1.getDictId()));
    }
}
