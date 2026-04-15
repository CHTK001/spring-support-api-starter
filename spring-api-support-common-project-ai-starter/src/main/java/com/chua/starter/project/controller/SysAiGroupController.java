package com.chua.starter.project.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.project.SysAiGroup;
import com.chua.starter.project.service.SysAiGroupService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能组
 *
 * @author CH
 * @since 2025/9/4 13:16
 */
@RestController
@Slf4j
@Api(tags = "会话组接口")
@Tag(name = "会话组接口")
@RequestMapping("/v2/project/ai/group")
@RequiredArgsConstructor
public class SysAiGroupController {

    final SysAiGroupService sysAiGroupService;

    /**
     * 获取智能组
     *
     * @param sysProjectId 项目id
     * @return 智能组
     */
    @GetMapping("group")
    @Operation(summary = "获取智能组")
    public ReturnResult<List<SysAiGroup>> group(Integer sysProjectId) {
        return sysAiGroupService.group(sysProjectId);
    }


    /**
     * 保存或更新智能组
     *
     * @param sysAiGroup 智能组
     * @return 结果
     */
    @Operation(summary = "保存或更新智能组")
    @PostMapping("saveOrUpdate")
    public ReturnResult<Boolean> saveOrUpdateGroup(@RequestBody SysAiGroup sysAiGroup) {
        return sysAiGroupService.saveOrUpdateForGroup(sysAiGroup);
    }

    /**
     * 删除智能组
     *
     * @param sysAiGroupId 智能组id
     * @return 结果
     */
    @Operation(summary = "删除智能组")
    @DeleteMapping("delete")
    public ReturnResult<Boolean> deleteGroup(Integer sysAiGroupId) {
        return sysAiGroupService.deleteForGroup(sysAiGroupId);
    }
}
