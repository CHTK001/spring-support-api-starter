package com.chua.starter.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysAvatarFrame;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserAvatarFrame;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.mapper.SysAvatarFrameMapper;
import com.chua.starter.system.mapper.SysUserAvatarFrameMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 头像框接口
 *
 * @since 2024/8/7
 * @author CH
 */
@Tag(name = "头像框接口")
@RestController
@RequestMapping("/v2/avatar-frame")
@RequiredArgsConstructor
@Ignore
public class SysAvatarFrameController {

    final SysAvatarFrameMapper sysAvatarFrameMapper; // sysavatar帧映射器
    final SysUserAvatarFrameMapper sysUserAvatarFrameMapper; // sys用户avatar帧映射器
    final SysUserMapper sysUserMapper; // sys用户映射器

    /** 生日窗口：前后 3 天共 7 天 */
    private static final int BIRTHDAY_WINDOW_DAYS = 3;

    /** 生日日期解析：yyyy-MM-dd / yyyy/MM/dd / yyyy.MM.dd */
    private static final Pattern BIRTHDAY_PATTERN = Pattern.compile("(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})");

    @GetMapping("/list")
    @Operation(summary = "头像框列表")
    /**
     * 列表
     *
     * @param @UserValue("sysUserId" @UserValue("sysUserId"
     * @return 列表的结果
     */
    public ReturnResult<List<SysAvatarFrame>> list(@UserValue("sysUserId") Long userId) {
        // 查询全部启用的头像框（含生日框记录）
        List<SysAvatarFrame> frames = sysAvatarFrameMapper.selectList(
                new LambdaQueryWrapper<SysAvatarFrame>()
                        .eq(SysAvatarFrame::getSysAvatarFrameStatus, 1)
                        .orderByAsc(SysAvatarFrame::getSysAvatarFrameSort)
        );
        // 非生日窗口（含未登录/未设置生日）过滤掉生日框，其余用户可见
        if (!isBirthdayWindow(userId)) {
            List<SysAvatarFrame> filtered = new ArrayList<>(frames.size());
            for (SysAvatarFrame frame : frames) {
                if (!"birthday".equals(frame.getSysAvatarFrameType())) {
                    filtered.add(frame);
                }
            }
            frames = filtered;
        }
        return ReturnResult.ok(frames);
    }

    /**
     * 判断当前用户是否处于生日窗口内
     * <p>按用户生日的月/日（忽略年份）与今天差值不超过 {@link #BIRTHDAY_WINDOW_DAYS} 天</p>
     *
     * @param userId 当前用户标识（未登录时为 空）
     * @return 是否处于生日窗口
     */
    private boolean isBirthdayWindow(Long userId) {
        if (null == userId) {
            return false;
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (null == user || StringUtils.isBlank(user.getSysUserBirthday())) {
            return false;
        }
        Matcher matcher = BIRTHDAY_PATTERN.matcher(user.getSysUserBirthday());
        if (!matcher.matches()) {
            return false;
        }
        LocalDate birth = LocalDate.of(2000, Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
        LocalDate now = LocalDate.now();
        LocalDate today = LocalDate.of(2000, now.getMonthValue(), now.getDayOfMonth());
        long diff = Math.abs(ChronoUnit.DAYS.between(birth, today));
        return diff <= BIRTHDAY_WINDOW_DAYS;
    }

    @GetMapping("/user")
    @Operation(summary = "用户已获取的头像框")
    /**
      * 用户帧
     * @param userId 用户id
     */
    public ReturnResult<List<SysUserAvatarFrame>> userFrames(
            @UserValue("sysUserId") Long userId) {
        List<SysUserAvatarFrame> list = sysUserAvatarFrameMapper.selectList(
                new LambdaQueryWrapper<SysUserAvatarFrame>()
                        .eq(SysUserAvatarFrame::getSysUserId, Math.toIntExact(userId)));
        return ReturnResult.ok(list);
    }

    @PostMapping("/acquire")
    @Operation(summary = "获取头像框")
    /**
     * 获取
     * @param userId 用户id
     * @param frameId 帧id
     */
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