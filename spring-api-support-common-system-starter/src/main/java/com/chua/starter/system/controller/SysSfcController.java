package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.common.support.validator.group.UploadGroup;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.entity.system.SysSfc;
import com.chua.starter.system.service.SysSfcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * 杩滅▼缁勪欢
 *
 * @author CH
 * @since 2024/10/24
 */
@Tag(name = "杩滅▼缁勪欢")
@RestController
@RequestMapping("/v2/sfc")
@RequiredArgsConstructor
public class SysSfcController {

    private final SysSfcService sysSfcService;

    /**
     * 角色分页列表
     *
     * @param sysSfc 鏉′欢
     * @return 角色分页列表
     */
    @Operation(summary = "缁勪欢分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysSfc> pageForRole(@ParameterObject Query<SysSfc> query, @ParameterObject SysSfc sysSfc,
                                                @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                                                @Parameter(hidden = true) @UserValue("userId") String userId
    ) {
        return PageResultUtils.ok(sysSfcService.pageForUserId(query, sysSfc, roles, userId));
    }



    /**
     * 获取组件列表
     *
     * @return 组件列表
     */
    @Operation(summary = "获取缁勪欢")
    @GetMapping("/get")
    public ReturnResult<String> getSfc(Long sysSfcId,
                                       @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                                       @Parameter(hidden = true) @UserValue("userId") String userId) {
        return sysSfcService.get(sysSfcId, roles, userId);
    }


    /**
     * 鎴戠殑缁勪欢
     *
     * @return 鎴戠殑缁勪欢
     */
    @Operation(summary = "鎴戠殑缁勪欢")
    @GetMapping("/mine")

    public ReturnResult<List<SysSfc>> mine(String sysSfcCategory, @Parameter(hidden = true) @UserValue("userId") String userId) {
        if(StringUtils.isBlank(sysSfcCategory)) {
            return ReturnResult.fail("鍒嗙被不能为空");
        }
        return sysSfcService.mine(sysSfcCategory, userId);
    }
    /**
     * 添加缁勪欢
     *
     * @param sysSfc 数据
     * @return 鏄惁成功
     */
    @Operation(summary = "添加缁勪欢")
    @PostMapping("/save")
    public ReturnResult<SysSfc> save(@RequestBody @Validated(AddGroup.class) SysSfc sysSfc, BindingResult bindingResult, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysSfcService.save(sysSfc, roles);
    }

    /**
     * 更新缁勪欢
     *
     * @param sysSfc 数据
     * @return 鏄惁成功
     */
    @Operation(summary = "更新缁勪欢")
    @PutMapping("/update")
    public ReturnResult<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysSfc sysSfc,
                                        BindingResult bindingResult,
                                        @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysSfcService.updateById(sysSfc, roles);
    }

    /**
     * 删除缁勪欢
     *
     * @param sysSfcId 数据
     * @return 鏄惁成功
     */
    @Operation(summary = "删除缁勪欢")
    @DeleteMapping("/delete")
    public ReturnResult<Boolean> delete(Long sysSfcId, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysSfcService.delete(sysSfcId, roles);
    }


    /**
     * 上传缁勪欢
     *
     * @param file 数据
     * @return 鏄惁成功
     */
    @Operation(summary = "上传缁勪欢")
    @PostMapping("/upload")
    public ReturnResult<Boolean> uploadSfc(@RequestParam(value = "file") MultipartFile file, @Validated(UploadGroup.class) SysSfc sysSfc, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return ReturnResult.of(sysSfcService.uploadSfc(file, sysSfc));
    }


    /**
     * 瀹夎缁勪欢
     *
     * @param sysSfc 数据
     * @return 鏄惁成功
     */
    @Operation(summary = "瀹夎缁勪欢")
    @PutMapping("/install")
    public ReturnResult<Boolean> install(@RequestBody @Validated(UpdateGroup.class) SysSfc sysSfc, BindingResult bindingResult, @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysSfcService.install(sysSfc, userId);
    }
    /**
     * 鍗歌浇缁勪欢
     *
     * @param sysSfc 数据
     * @return 鏄惁成功
     */
    @Operation(summary = "鍗歌浇缁勪欢")
    @PutMapping("/uninstall")
    public ReturnResult<Boolean> uninstall(@RequestBody @Validated(UpdateGroup.class) SysSfc sysSfc, BindingResult bindingResult, @Parameter(hidden = true) @UserValue("userId") Integer userId) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysSfcService.uninstall(sysSfc, userId);
    }

}
