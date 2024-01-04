package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.logger.Logger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.admin.entity.SysDictType;
import com.chua.starter.admin.service.SysDictTypeService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 机构
 *
 * @author CH
 */
@Api(tags = "字典类型接口")
@RestController
@RequestMapping("/v2/dict/type")
@RequiredArgsConstructor
public class SysDictTypeController implements Constant {


    private final SysDictTypeService sysDictTypeService;

    /**
     * 字典类型列表
     *
     * @return 字典类型列表
     */
    @Operation(summary = "字典类型列表")
    @GetMapping("list")
    @Cacheable(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dict-type'")
    public ReturnResult<List<SysDictType>> list() {
        return Result.ok(sysDictTypeService.list(Wrappers.<SysDictType>lambdaQuery()
                .orderByAsc(SysDictType::getDictTypeSort)
        ));
    }

    /**
     * 修改字典类型
     *
     * @return 修改字典类型
     */
    @Operation(summary = "修改字典类型")
    @PutMapping("update")
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dict-type'")
    public ReturnResult<Boolean> update(@RequestBody SysDictType sysDictType) {
        if (null == sysDictType.getDictTypeId()) {
            return Result.illegal("唯一标识不能为空");
        }

        if (sysDictTypeService.count(Wrappers.<SysDictType>lambdaQuery()
                .eq(SysDictType::getDictTypeCode, sysDictType.getDictTypeCode())
                .ne(SysDictType::getDictTypeId, sysDictType.getDictTypeId())
        ) > 0L) {
            return Result.illegal("字典编码已存在");
        }

        return Result.ok(sysDictTypeService.updateById(sysDictType));
    }

    /**
     * 新增字典类型
     *
     * @return 新增字典类型
     */
    @Operation(summary = "新增字典类型")
    @PostMapping("save")
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dict-type'")
    public ReturnResult<SysDictType> save(@RequestBody SysDictType sysDictType, @Valid BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if (sysDictTypeService.count(Wrappers.<SysDictType>lambdaQuery().eq(SysDictType::getDictTypeCode, sysDictType.getDictTypeCode())) > 0L) {
            return Result.illegal("字典编码已存在");
        }
        sysDictTypeService.save(sysDictType);
        return Result.ok(sysDictType);
    }

    /**
     * 删除字典类型
     *
     * @return 删除字典类型
     */
    @Logger
    @Operation(summary = "删除字典类型")
    @DeleteMapping("delete")
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dict-type'")
    public ReturnResult<Boolean> delete(String dictId) {
        if (StringUtils.isBlank(dictId)) {
            return Result.illegal("字典类型不存在");
        }

        SysDictType sysDictType = sysDictTypeService.getById(dictId);
        if (null == sysDictType) {
            return Result.illegal("字典类型不存在");
        }

        if (null != sysDictType.getDictTypeSys() && sysDictType.getDictTypeSys() == 1) {
            return Result.illegal("系统字典类型不允许删除");
        }


        return Result.ok(sysDictTypeService.removeById(dictId));
    }
}
