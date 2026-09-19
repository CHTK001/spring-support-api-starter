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
import com.chua.starter.entity.system.SysPost;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.service.SysPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位接口
 *
 * @since 4.0.0.43
 * @author CH
 */
@Tag(name = "岗位接口")
@RestController
@RequestMapping("/v2/post")
@RequiredArgsConstructor
public class SysPostController {

    /**
     * syspost服务
    */
    private final SysPostService sysPostService;

    /**
     * 岗位列表
     *
     * @param sysPost 条件
     * @return 岗位列表
     */
    @Operation(summary = "岗位列表")
    @GetMapping("/list")
    public ReturnResult<List<SysPost>> list(@ParameterObject SysPost sysPost) {
        return ReturnResult.ok(sysPostService.list(sysPost));
    }

    /**
     * 岗位分页列表
     *
     * @param query   分页参数
     * @param sysPost 条件
     * @return 岗位分页列表
     */
    @Operation(summary = "岗位分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysPost> page(@ParameterObject Query<SysPost> query, @ParameterObject SysPost sysPost) {
        return PageResultUtils.ok(sysPostService.page(query, sysPost));
    }

    /**
     * 新增岗位
     *
     * @param sysPost 岗位
     * @return 新增后的岗位
     * @param bindingResult binding结果，不允许为 null
     */
    @Operation(summary = "新增岗位")
    @PostMapping("/save")
    @SysLog
    @Permission(value = "sys:post:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysPost> save(@Validated(AddGroup.class) @RequestBody SysPost sysPost, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysPostService.savePost(sysPost);
    }

    /**
     * 更新岗位
     *
     * @param sysPost 岗位
     * @return 更新结果
     * @param bindingResult binding结果，不允许为 null
     */
    @Operation(summary = "更新岗位")
    @PutMapping("/update")
    @SysLog
    @Permission(value = "sys:post:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(@Validated(UpdateGroup.class) @RequestBody SysPost sysPost, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysPostService.updatePost(sysPost);
    }

    /**
     * 删除岗位
     *
     * @param postId 岗位ID
     * @return 删除结果
     */
    @Operation(summary = "删除岗位")
    @DeleteMapping("/delete")
    @SysLog
    @Permission(value = "sys:post:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Integer postId) {
        if (null == postId) {
            return ReturnResult.illegal("岗位信息不存在");
        }
        return sysPostService.deletePost(postId);
    }

    /**
     * 查询岗位下的用户列表
     *
     * @param postId 岗位ID
     * @return 用户列表
     */
    @Operation(summary = "查询岗位下的用户")
    @GetMapping("/users")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<SysUser>> users(@RequestParam Integer postId) {
        if (null == postId) {
            return ReturnResult.illegal("岗位信息不存在");
        }
        return ReturnResult.ok(sysPostService.getUsersByPostId(postId));
    }
}
