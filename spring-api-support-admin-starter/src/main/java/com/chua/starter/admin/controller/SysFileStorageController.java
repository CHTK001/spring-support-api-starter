package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.admin.entity.SysFileStorage;
import com.chua.starter.admin.service.SysFileStorageService;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.filestorage.FileStorageService;
import com.chua.starter.common.support.logger.SysLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.chua.starter.admin.utils.FileStorageUtils.openFileStorage;

/**
 * sys文件存储控制器
 *
 * @author CH
 */
@Tag(name = "OSS接口")
@RestController
@RequestMapping("/v1/oss")
@RequiredArgsConstructor
public class SysFileStorageController {

    private final SysFileStorageService sysFileStorageService;


    public final FileStorageService fileStorageService;

    private final TransactionTemplate transactionTemplate;
    /**
     * bucket类型列表
     *
     * @return bucket类型列表
     */
    @Operation(summary = "oss类型列表")
    @GetMapping("type")
    public ReturnResult<JsonObject> type() {
        JsonObject res = new JsonObject();
        res.put("impl", fileStorageService.getType());
        res.put("filter", fileStorageService.getFilter());
        res.put("plugin", fileStorageService.getPlugin());
        return ReturnResult.ok(res);
    }
    /**
     * bucket列表
     *
     * @return 角色列表
     */
    @Operation(summary = "oss列表")
    @GetMapping("page")
    public ReturnPageResult<SysFileStorage> page(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return PageResultUtils.ok(sysFileStorageService.page(new Page<>(page, pageSize),
                        Wrappers.<SysFileStorage>lambdaQuery()
                                .like(StringUtils.isNotBlank(keyword), SysFileStorage::getFsBucket, keyword)
                                .orderByAsc(SysFileStorage::getCreateTime)
                )
        );
    }

    /**
     * 修改字典
     *
     * @return 修改字典
     */
    @Operation(summary = "修改OSS")
    @PutMapping("update")
    @OperateLog
    public ReturnResult<Boolean> update(@Validated(UpdateGroup.class)@RequestBody SysFileStorage sysFileStorage, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return Result.ok(transactionTemplate.execute(status -> {
            boolean b = sysFileStorageService.updateById(sysFileStorage);
            if(b) {
                SysFileStorage fileStorage = sysFileStorageService.getById(sysFileStorage.getFsId());
                if(fileStorage.getFsStatus() == 0) {
                    fileStorageService.unregister(fileStorage.getFsBucket());
                    return true;
                }
                openFileStorage(fileStorageService, fileStorage);
            }
            return b;
        }));
    }


    /**
     * 新增字典
     *
     * @return 新增字典
     */
    @Operation(summary = "新增OSS")
    @PostMapping("save")
    public ReturnResult<SysFileStorage> save(@RequestBody @Validated(AddGroup.class) SysFileStorage sysFileStorage, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return Result.ok(transactionTemplate.execute(status -> {
            sysFileStorageService.save(sysFileStorage);
            openFileStorage(fileStorageService, sysFileStorage);
            return sysFileStorage;
        }));
    }

    /**
     * 删除字典
     *
     * @return 删除字典
     */
    @SysLogger
    @Operation(summary = "删除OSS")
    @DeleteMapping("delete")
    @OperateLog
    public ReturnResult<Boolean> delete(Integer fsId) {
        if (null == fsId) {
            return Result.illegal("OSS不存在");
        }

        SysFileStorage fileStorage = sysFileStorageService.getById(fsId);
        if (null == fileStorage) {
            return Result.illegal("OSS不存在");
        }


        return Result.ok(transactionTemplate.execute(status -> {
            boolean b = sysFileStorageService.removeById(fsId);
            if(b) {
                fileStorageService.unregister(fileStorage.getFsBucket());
            }
            return b;
        }));
    }
}
