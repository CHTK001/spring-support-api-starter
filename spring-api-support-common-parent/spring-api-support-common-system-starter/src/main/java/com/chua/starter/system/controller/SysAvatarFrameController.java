package com.chua.starter.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysAvatarFrame;
import com.chua.starter.entity.system.SysUserAvatarFrame;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.mapper.SysAvatarFrameMapper;
import com.chua.starter.system.mapper.SysUserAvatarFrameMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 头像框接口
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "头像框接口")
@RestController
@RequestMapping("/v2/avatar-frame")
@RequiredArgsConstructor
@Ignore
public class SysAvatarFrameController {

    final SysAvatarFrameMapper sysAvatarFrameMapper;
    final SysUserAvatarFrameMapper sysUserAvatarFrameMapper;

    @GetMapping("/list")
    @Operation(summary = "头像框列表")
    public ReturnResult<List<SysAvatarFrame>> list() {
        return ReturnResult.ok(sysAvatarFrameMapper.selectList(
                new LambdaQueryWrapper<SysAvatarFrame>()
                        .eq(SysAvatarFrame::getSysAvatarFrameStatus, 1)
                        .orderByAsc(SysAvatarFrame::getSysAvatarFrameSort)
        ));
    }

    @GetMapping("/user")
    @Operation(summary = "用户已获取的头像框")
    public ReturnResult<List<SysUserAvatarFrame>> userFrames(
            @UserValue("sysUserId") Long userId) {
        List<SysUserAvatarFrame> list = sysUserAvatarFrameMapper.selectList(
                new LambdaQueryWrapper<SysUserAvatarFrame>()
                        .eq(SysUserAvatarFrame::getSysUserId, Math.toIntExact(userId)));
        return ReturnResult.ok(list);
    }

    @PostMapping("/acquire")
    @Operation(summary = "获取头像框")
    public ReturnResult<Boolean> acquire(
            @UserValue("sysUserId") Long userId,
            @RequestParam Integer frameId) {
        SysUserAvatarFrame existing = sysUserAvatarFrameMapper.selectOne(
                new LambdaQueryWrapper<SysUserAvatarFrame>()
                        .eq(SysUserAvatarFrame::getSysUserId, Math.toIntExact(userId))
                        .eq(SysUserAvatarFrame::getSysAvatarFrameId, frameId));
        if (existing != null) {
            return ReturnResult.ok(true);
        }
        SysAvatarFrame frame = sysAvatarFrameMapper.selectById(frameId);
        if (frame == null) {
            return ReturnResult.illegal("头像框不存在");
        }
        SysUserAvatarFrame uaf = new SysUserAvatarFrame();
        uaf.setSysUserId(Math.toIntExact(userId));
        uaf.setSysAvatarFrameId(frameId);
        uaf.setAcquireTime(LocalDateTime.now());
        if ("birthday".equals(frame.getSysAvatarFrameAcquire())) {
            uaf.setExpireTime(LocalDateTime.now().plusDays(7));
        }
        sysUserAvatarFrameMapper.insert(uaf);
        return ReturnResult.ok(true);
    }
}