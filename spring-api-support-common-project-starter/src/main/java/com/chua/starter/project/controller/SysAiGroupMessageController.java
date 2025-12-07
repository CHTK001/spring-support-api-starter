package com.chua.starter.project.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.project.SysAiGroupMessage;
import com.chua.starter.project.service.SysAiGroupMessageService;
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
@Api(tags = "会话组内容接口")
@Tag(name = "会话组内容接口")
@RequestMapping("/v2/project/ai/group/message")
@RequiredArgsConstructor
public class SysAiGroupMessageController {

    final SysAiGroupMessageService sysAiGroupMessageService;


    /**
     * 列表
     *
     * @param sysAiGroupId 智能组id
     * @param current      当前页
     * @param size         页大小
     * @return 结果
     */
    @Operation(summary = "列表")
    @GetMapping("list")
    public ReturnPageResult<SysAiGroupMessage> list(Integer sysAiGroupId, long current, long size) {
        return sysAiGroupMessageService.list(sysAiGroupId, current, size);
    }

    /**
     * 保存或更新智能组
     *
     * @param sysAiGroupMessages 智能组
     * @return 结果
     */
    @Operation(summary = "保存或更新智能组")
    @PostMapping("saveOrUpdate")
    public ReturnResult<Boolean> saveGroupMessage(@RequestBody List<SysAiGroupMessage> sysAiGroupMessages) {
        return sysAiGroupMessageService.saveGroupMessage(sysAiGroupMessages);
    }

}
