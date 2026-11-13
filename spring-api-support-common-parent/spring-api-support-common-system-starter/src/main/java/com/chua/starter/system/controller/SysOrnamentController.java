package com.chua.starter.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysOrnament;
import com.chua.starter.entity.system.SysUserOrnament;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.mapper.SysOrnamentMapper;
import com.chua.starter.system.mapper.SysUserOrnamentMapper;
import com.chua.starter.system.pojo.ornament.OrnamentWearRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 饰品接口
 *
 * <p>饰品是「账号级」装扮：服务端下发目录，用户按槽位佩戴一件。
 * 只存佩戴关系（{@code sys_user_ornament}），素材本身由目录指向静态资源或 CSS。</p>
 *
 * <p>三条口径：</p>
 * <ul>
 *   <li>目标用户一律取令牌里的 {@code sysUserId}，不接受请求体指定他人；</li>
 *   <li>槽位与目录项的 slot 必须一致，避免把昵称牌戴到头像上；</li>
 *   <li>目录项必须启用：下架后即使库里有佩戴记录也不再下发。</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0
 */
@Tag(name = "饰品接口")
@RestController
@RequestMapping("/v2/ornament")
@RequiredArgsConstructor
@Ignore
public class SysOrnamentController {

    /** 目录启用状态 */
    private static final int STATUS_ENABLED = 1;

    /** 允许的槽位，与前端 OrnamentSlot 一致 */
    private static final Set<String> SLOTS = Set.of("avatar", "nameplate");

    private final SysOrnamentMapper ornamentMapper;
    private final SysUserOrnamentMapper userOrnamentMapper;

    /**
     * 饰品目录
     *
     * <p>只下发启用项，按排序号升序。远端不可用时前端用内置兜底清单。</p>
     *
     * @return 饰品目录
     */
    @GetMapping("/list")
    @Operation(summary = "饰品目录")
    public ReturnResult<List<SysOrnament>> list() {
        List<SysOrnament> list = ornamentMapper.selectList(
                new LambdaQueryWrapper<SysOrnament>()
                        .eq(SysOrnament::getSysOrnamentStatus, STATUS_ENABLED)
                        .orderByAsc(SysOrnament::getSysOrnamentSort)
                        .orderByAsc(SysOrnament::getSysOrnamentId));
        return ReturnResult.ok(list);
    }

    /**
     * 我的佩戴
     *
     * <p>只返回仍然启用、且槽位一致的目录项；目录下架或换了槽位的记录在这里被自然过滤，
     * 前端据此收起对应挂件。</p>
     *
     * @param userId 当前用户
     * @return 当前佩戴的饰品（按槽位顺序）
     */
    @GetMapping("/mine")
    @Operation(summary = "我的饰品佩戴")
    public ReturnResult<List<SysOrnament>> mine(@UserValue("sysUserId") Long userId) {
        return ReturnResult.ok(readWorn(userId == null ? null : toUserId(userId)));
    }

    /**
     * 佩戴 / 取下
     *
     * <p>{@code ornamentId} 为空时删掉该槽位的记录（即取下）；否则按
     * {@code (userId, slot)} upsert。返回值是操作后的完整佩戴列表，前端可直接覆盖本地状态。</p>
     *
     * @param userId  当前用户
     * @param request 槽位与饰品ID
     * @return 操作后的佩戴列表
     */
    @PutMapping("/mine")
    @Operation(summary = "佩戴/取下饰品")
    public ReturnResult<List<SysOrnament>> save(
            @UserValue("sysUserId") Long userId,
            @RequestBody OrnamentWearRequest request) {
        if (userId == null) {
            return ReturnResult.illegal("请先登录");
        }
        if (request == null) {
            return ReturnResult.illegal("参数不能为空");
        }
        String slot = trimToNull(request.getSlot());
        if (slot == null || !SLOTS.contains(slot)) {
            return ReturnResult.illegal("不支持的饰品槽位");
        }
        Integer uid = toUserId(userId);
        Integer ornamentId = request.getOrnamentId();

        if (ornamentId == null) {
            userOrnamentMapper.delete(
                    new LambdaQueryWrapper<SysUserOrnament>()
                            .eq(SysUserOrnament::getSysUserId, uid)
                            .eq(SysUserOrnament::getSysOrnamentSlot, slot));
            return ReturnResult.ok(readWorn(uid));
        }

        SysOrnament item = ornamentMapper.selectById(ornamentId);
        if (item == null
                || !Integer.valueOf(STATUS_ENABLED).equals(item.getSysOrnamentStatus())
                || !Objects.equals(slot, item.getSysOrnamentSlot())) {
            return ReturnResult.illegal("饰品不存在或已下架");
        }

        SysUserOrnament row = userOrnamentMapper.selectOne(
                new LambdaQueryWrapper<SysUserOrnament>()
                        .eq(SysUserOrnament::getSysUserId, uid)
                        .eq(SysUserOrnament::getSysOrnamentSlot, slot));
        if (row == null) {
            row = new SysUserOrnament();
            row.setSysUserId(uid);
            row.setSysOrnamentSlot(slot);
            row.setSysOrnamentId(ornamentId);
            userOrnamentMapper.insert(row);
        } else if (!Objects.equals(row.getSysOrnamentId(), ornamentId)) {
            row.setSysOrnamentId(ornamentId);
            userOrnamentMapper.updateById(row);
        }
        return ReturnResult.ok(readWorn(uid));
    }

    /**
     * 读取当前佩戴
     *
     * @param userId 用户ID，可为空
     * @return 启用中、槽位一致的饰品列表
     */
    private List<SysOrnament> readWorn(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<SysUserOrnament> rows = userOrnamentMapper.selectList(
                new LambdaQueryWrapper<SysUserOrnament>()
                        .eq(SysUserOrnament::getSysUserId, userId));
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> ids = rows.stream()
                .map(SysUserOrnament::getSysOrnamentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, SysOrnament> byId = ornamentMapper.selectByIds(ids).stream()
                .filter(item -> item != null
                        && Integer.valueOf(STATUS_ENABLED).equals(item.getSysOrnamentStatus()))
                .collect(Collectors.toMap(
                        SysOrnament::getSysOrnamentId,
                        item -> item,
                        (left, right) -> left));

        List<SysOrnament> worn = new ArrayList<>();
        for (SysUserOrnament row : rows) {
            SysOrnament item = byId.get(row.getSysOrnamentId());
            if (item == null || !Objects.equals(row.getSysOrnamentSlot(), item.getSysOrnamentSlot())) {
                continue;
            }
            worn.add(item);
        }
        return worn;
    }

    /**
     * 字符串去空白，空串返回 null
     *
     * @param value 原值
     * @return 去空白后的值
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 令牌用户ID转库内类型
     *
     * @param userId 令牌用户ID
     * @return 整型用户ID
     */
    private Integer toUserId(Long userId) {
        return userId == null ? null : Math.toIntExact(userId);
    }
}
