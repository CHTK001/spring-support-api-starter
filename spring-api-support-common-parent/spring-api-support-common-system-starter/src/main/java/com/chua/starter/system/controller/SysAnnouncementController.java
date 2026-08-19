package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.entity.system.SysAnnouncement;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.service.SysAnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 系统公告接口
 *
 * @since 4.0.0.42
 * @author CH
 */
@Tag(name = "系统公告接口")
@RestController
@RequestMapping("/v2/announcement")
@RequiredArgsConstructor
public class SysAnnouncementController {

    /** SYSannouncement服务 */
    private final SysAnnouncementService sysAnnouncementService;

    /**
     * 公告分页列表（管理端）
     *
     * @param query           分页参数
     * @param sysAnnouncement 查询条件
     * @param roles           当前用户角色
     * @return 公告分页结果
     */
    @Operation(summary = "公告分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysAnnouncement> page(
            @ParameterObject Query<SysAnnouncement> query,
            @ParameterObject SysAnnouncement sysAnnouncement,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysAnnouncementService.pageForManage(query, sysAnnouncement, roles));
    }

    /**
     * 获取公告详情
     *
     * @param sysAnnouncementId 公告ID
     * @return 公告详情
     */
    @Operation(summary = "获取公告详情")
    @GetMapping("/get")
    public ReturnResult<SysAnnouncement> get(Long sysAnnouncementId) {
        if (sysAnnouncementId == null) {
            return ReturnResult.illegal("公告ID不能为空");
        }
        SysAnnouncement entity = sysAnnouncementService.getById(sysAnnouncementId);
        if (entity == null) {
            return ReturnResult.illegal("公告不存在");
        }
        return ReturnResult.ok(entity);
    }

    /**
     * 新增公告
     *
     * @param sysAnnouncement 公告数据
     * @param bindingResult   校验结果
     * @param roles           当前用户角色
     * @param userId          当前用户ID
     * @return 操作结果
     */
    @Operation(summary = "新增公告")
    @PostMapping("/save")
    public ReturnResult<SysAnnouncement> save(
            @RequestBody @Validated SysAnnouncement sysAnnouncement,
            BindingResult bindingResult,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Parameter(hidden = true) @UserValue("sysUserId") Integer userId) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysAnnouncementService.saveAnnouncement(sysAnnouncement, roles, userId);
    }

    /**
     * 更新公告
     *
     * @param sysAnnouncement 公告数据
     * @param bindingResult   校验结果
     * @param roles           当前用户角色
     * @return 操作结果
     */
    @Operation(summary = "更新公告")
    @PutMapping("/update")
    public ReturnResult<Boolean> update(
            @RequestBody @Validated SysAnnouncement sysAnnouncement,
            BindingResult bindingResult,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysAnnouncementService.updateAnnouncement(sysAnnouncement, roles);
    }

    /**
     * 删除公告
     *
     * @param sysAnnouncementId 公告ID
     * @param roles             当前用户角色
     * @return 操作结果
     */
    @Operation(summary = "删除公告")
    @DeleteMapping("/delete")
    public ReturnResult<Boolean> delete(
            Long sysAnnouncementId,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysAnnouncementService.deleteAnnouncement(
                sysAnnouncementId != null ? sysAnnouncementId.intValue() : null, roles);
    }

    /**
     * 发布公告
     *
     * @param sysAnnouncement 公告数据（含ID）
     * @param roles           当前用户角色
     * @param userId          当前用户ID
     * @return 操作结果
     */
    @Operation(summary = "发布公告")
    @PutMapping("/publish")
    public ReturnResult<Boolean> publish(
            @RequestBody SysAnnouncement sysAnnouncement,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Parameter(hidden = true) @UserValue("sysUserId") Integer userId) {
        return sysAnnouncementService.publishAnnouncement(
                sysAnnouncement.getSysAnnouncementId(), roles, userId);
    }

    /**
     * 下架公告
     *
     * @param sysAnnouncement 公告数据（含ID）
     * @param roles           当前用户角色
     * @return 操作结果
     */
    @Operation(summary = "下架公告")
    @PutMapping("/unpublish")
    public ReturnResult<Boolean> unpublish(
            @RequestBody SysAnnouncement sysAnnouncement,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysAnnouncementService.unpublishAnnouncement(
                sysAnnouncement.getSysAnnouncementId(), roles);
    }

    /**
     * 获取当前用户可见的活跃公告列表（前端调用，无权限限制）
     *
     * @param userId 当前用户ID
     * @param roles  当前用户角色
     * @return 活跃公告列表
     */
    @Operation(summary = "获取活跃公告列表")
    @GetMapping("/active")
    public ReturnResult<List<SysAnnouncement>> active(
            @Parameter(hidden = true) @UserValue("sysUserId") Integer userId,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        List<SysAnnouncement> list = sysAnnouncementService.getActiveAnnouncements(userId, roles);
        return ReturnResult.ok(list);
    }
}