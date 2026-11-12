package com.chua.starter.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysUserWallpaper;
import com.chua.starter.entity.system.SysUserWallpaperUpload;
import com.chua.starter.entity.system.SysWallpaper;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.mapper.SysUserWallpaperMapper;
import com.chua.starter.system.mapper.SysUserWallpaperUploadMapper;
import com.chua.starter.system.mapper.SysWallpaperMapper;
import com.chua.starter.system.pojo.wallpaper.WallpaperSettingRequest;
import com.chua.starter.system.pojo.wallpaper.WallpaperSettingVO;
import com.chua.starter.system.pojo.wallpaper.WallpaperUploadRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 壁纸接口
 *
 * <p>壁纸是「账号级」设置：用户在哪台设备登录都看到同一张。
 * 文件本体走 {@code /v2/file/upload}（bucket=wallpaper），本控制器只存设置与上传记录。</p>
 *
 * <p>三条安全口径：</p>
 * <ul>
 *   <li>目标用户一律取令牌里的 {@code sysUserId}，不接受请求体指定他人；</li>
 *   <li>{@code css} / 名称只从壁纸目录解析，不接受前端传入，杜绝往库里塞任意 CSS；</li>
 *   <li>上传地址必须指向本站文件回显路径，挡住外链与 {@code javascript:} / {@code data:}。</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0
 */
@Tag(name = "壁纸接口")
@RestController
@RequestMapping("/v2/wallpaper")
@RequiredArgsConstructor
@Ignore
public class SysWallpaperController {

    /** 目录启用状态 */
    private static final int STATUS_ENABLED = 1;
    /** 图片地址长度上限，与建表宽度一致 */
    private static final int MAX_URL_LENGTH = 512;
    /** 展示名长度上限，与建表宽度一致 */
    private static final int MAX_NAME_LENGTH = 64;
    /** 每人保留的上传记录条数 */
    private static final int MAX_UPLOAD_RECORDS = 24;
    /** 模糊半径上限（px） */
    private static final int MAX_BLUR = 24;
    /** 不透明度上限 */
    private static final BigDecimal MAX_OPACITY = BigDecimal.ONE;
    /** 压暗上限，再高压得看不见壁纸 */
    private static final BigDecimal MAX_DIM = new BigDecimal("0.8");
    /** 未指定时的默认值 */
    private static final BigDecimal DEFAULT_OPACITY = BigDecimal.ONE;
    /** 上传图片的默认压暗：保证亮图下的正文对比度 */
    private static final BigDecimal DEFAULT_UPLOAD_DIM = new BigDecimal("0.12");
    /** 默认不铺图 */
    private static final String SOURCE_DEFAULT = "default";
    /** 内置壁纸 */
    private static final String SOURCE_BUILTIN = "builtin";
    /** 自己上传 */
    private static final String SOURCE_UPLOAD = "upload";
    /** 默认平铺方式 */
    private static final String DEFAULT_TILE = "none";
    /** 文件回显路径片段：上传地址必须命中它 */
    private static final String FILE_VIEW_PATH = "/v2/file/view/";

    /** 允许的来源 */
    private static final Set<String> SOURCES = Set.of(SOURCE_DEFAULT, SOURCE_BUILTIN, SOURCE_UPLOAD);
    /** 允许的平铺方式，与前端 WallpaperTile 一致 */
    private static final Set<String> TILES =
            Set.of("none", "cover", "contain", "repeat", "repeat-x", "repeat-y");

    private final SysWallpaperMapper wallpaperMapper;
    private final SysUserWallpaperMapper userWallpaperMapper;
    private final SysUserWallpaperUploadMapper uploadMapper;

    /**
     * 壁纸目录
     *
     * <p>只下发启用项，按排序号升序。前端拿不到时用内置静态清单兜底。</p>
     *
     * @return 壁纸目录
     */
    @GetMapping("/list")
    @Operation(summary = "壁纸目录")
    public ReturnResult<List<SysWallpaper>> list() {
        List<SysWallpaper> list = wallpaperMapper.selectList(
                new LambdaQueryWrapper<SysWallpaper>()
                        .eq(SysWallpaper::getSysWallpaperStatus, STATUS_ENABLED)
                        .orderByAsc(SysWallpaper::getSysWallpaperSort)
                        .orderByAsc(SysWallpaper::getSysWallpaperId));
        return ReturnResult.ok(list);
    }

    /**
     * 我的壁纸
     *
     * <p>没有记录时返回 {@code null} 而不是默认配置：前端据此判断「本机已有选择但还没上云」，
     * 把本地选择补推一次（老用户升级后的迁移路径）。</p>
     *
     * @param userId 当前用户
     * @return 当前壁纸设置，未设置时为 null
     */
    @GetMapping("/mine")
    @Operation(summary = "我的壁纸")
    public ReturnResult<WallpaperSettingVO> mine(@UserValue("sysUserId") Long userId) {
        if (userId == null) {
            return ReturnResult.<WallpaperSettingVO>ok(null);
        }
        return ReturnResult.ok(readSetting(toUserId(userId)));
    }

    /**
     * 保存我的壁纸
     *
     * @param userId  当前用户
     * @param request 壁纸设置
     * @return 保存后的完整设置（含解析出的 css）
     */
    @PutMapping("/mine")
    @Operation(summary = "保存我的壁纸")
    public ReturnResult<WallpaperSettingVO> save(
            @UserValue("sysUserId") Long userId,
            @RequestBody WallpaperSettingRequest request) {
        if (userId == null) {
            return ReturnResult.illegal("请先登录");
        }
        if (request == null) {
            return ReturnResult.illegal("参数不能为空");
        }
        Integer uid = toUserId(userId);
        String source = trimToNull(request.getSource());
        if (source == null) {
            source = SOURCE_DEFAULT;
        }
        if (!SOURCES.contains(source)) {
            return ReturnResult.illegal("不支持的壁纸来源");
        }

        SysWallpaper preset = null;
        String url = null;
        if (SOURCE_BUILTIN.equals(source)) {
            preset = findEnabledPreset(request.getPresetId(), request.getPresetCode());
            if (preset == null) {
                return ReturnResult.illegal("壁纸不存在或已下架");
            }
        } else if (SOURCE_UPLOAD.equals(source)) {
            url = trimToNull(request.getUrl());
            if (!isOwnFileUrl(url)) {
                return ReturnResult.illegal("图片地址不合法，请重新上传");
            }
        }

        SysUserWallpaper row = userWallpaperMapper.selectOne(
                new LambdaQueryWrapper<SysUserWallpaper>()
                        .eq(SysUserWallpaper::getSysUserId, uid));
        boolean insert = row == null;
        if (insert) {
            row = new SysUserWallpaper();
            row.setSysUserId(uid);
        }
        row.setSource(source);
        row.setPresetId(preset == null ? null : preset.getSysWallpaperId());
        row.setUrl(url);
        row.setTile(normalizeTile(request.getTile(), preset));
        row.setOpacity(clampDecimal(
                request.getOpacity(),
                preset == null ? DEFAULT_OPACITY : nvl(preset.getSysWallpaperOpacity(), DEFAULT_OPACITY),
                MAX_OPACITY));
        row.setBlur(clampBlur(
                request.getBlur(),
                preset == null ? 0 : preset.getSysWallpaperBlur()));
        row.setDim(clampDecimal(
                request.getDim(),
                preset == null
                        ? (SOURCE_UPLOAD.equals(source) ? DEFAULT_UPLOAD_DIM : BigDecimal.ZERO)
                        : nvl(preset.getSysWallpaperDim(), BigDecimal.ZERO),
                MAX_DIM));

        if (insert) {
            userWallpaperMapper.insert(row);
        } else {
            userWallpaperMapper.updateById(row);
        }
        return ReturnResult.ok(toVo(row, preset));
    }

    /**
     * 我的上传记录
     *
     * @param userId 当前用户
     * @return 上传记录（新的在前）
     */
    @GetMapping("/uploads")
    @Operation(summary = "我的壁纸上传记录")
    public ReturnResult<List<SysUserWallpaperUpload>> uploads(@UserValue("sysUserId") Long userId) {
        if (userId == null) {
            return ReturnResult.ok(Collections.<SysUserWallpaperUpload>emptyList());
        }
        List<SysUserWallpaperUpload> list = uploadMapper.selectList(
                new LambdaQueryWrapper<SysUserWallpaperUpload>()
                        .eq(SysUserWallpaperUpload::getSysUserId, toUserId(userId))
                        .orderByDesc(SysUserWallpaperUpload::getSysUserWallpaperUploadId));
        if (list.size() > MAX_UPLOAD_RECORDS) {
            return ReturnResult.ok(list.subList(0, MAX_UPLOAD_RECORDS));
        }
        return ReturnResult.ok(list);
    }

    /**
     * 记录一次上传
     *
     * <p>同一地址重复提交只保留一条；超过上限时挤掉最旧的一条。</p>
     *
     * @param userId  当前用户
     * @param request 图片地址与展示名
     * @return 记录行
     */
    @PostMapping("/uploads")
    @Operation(summary = "记录壁纸上传")
    public ReturnResult<SysUserWallpaperUpload> addUpload(
            @UserValue("sysUserId") Long userId,
            @RequestBody WallpaperUploadRequest request) {
        if (userId == null) {
            return ReturnResult.illegal("请先登录");
        }
        if (request == null) {
            return ReturnResult.illegal("参数不能为空");
        }
        String url = trimToNull(request.getUrl());
        if (!isOwnFileUrl(url)) {
            return ReturnResult.illegal("图片地址不合法，请重新上传");
        }
        Integer uid = toUserId(userId);
        SysUserWallpaperUpload exists = uploadMapper.selectOne(
                new LambdaQueryWrapper<SysUserWallpaperUpload>()
                        .eq(SysUserWallpaperUpload::getSysUserId, uid)
                        .eq(SysUserWallpaperUpload::getUrl, url));
        if (exists != null) {
            return ReturnResult.ok(exists);
        }

        Long count = uploadMapper.selectCount(
                new LambdaQueryWrapper<SysUserWallpaperUpload>()
                        .eq(SysUserWallpaperUpload::getSysUserId, uid));
        if (count != null && count >= MAX_UPLOAD_RECORDS) {
            List<SysUserWallpaperUpload> olds = uploadMapper.selectList(
                    new LambdaQueryWrapper<SysUserWallpaperUpload>()
                            .eq(SysUserWallpaperUpload::getSysUserId, uid)
                            .orderByAsc(SysUserWallpaperUpload::getSysUserWallpaperUploadId));
            if (!olds.isEmpty()) {
                uploadMapper.deleteById(olds.get(0).getSysUserWallpaperUploadId());
            }
        }

        SysUserWallpaperUpload row = new SysUserWallpaperUpload();
        row.setSysUserId(uid);
        row.setUrl(url);
        row.setName(truncate(trimToNull(request.getName()), MAX_NAME_LENGTH));
        uploadMapper.insert(row);
        return ReturnResult.ok(row);
    }

    /**
     * 删除一条上传记录
     *
     * <p>只删记录不删文件：同一份对象可能被其它账号或页面引用，误删不可恢复。
     * 若删的正是当前佩戴的那张，顺手把设置退回默认，避免前端引用一个空地址。</p>
     *
     * @param userId 当前用户
     * @param id     记录ID
     * @return 是否删除
     */
    @DeleteMapping("/uploads/{id}")
    @Operation(summary = "删除壁纸上传记录")
    public ReturnResult<Boolean> removeUpload(
            @UserValue("sysUserId") Long userId,
            @PathVariable("id") Integer id) {
        if (userId == null) {
            return ReturnResult.illegal("请先登录");
        }
        Integer uid = toUserId(userId);
        SysUserWallpaperUpload row = uploadMapper.selectById(id);
        if (row == null || !Objects.equals(row.getSysUserId(), uid)) {
            return ReturnResult.illegal("记录不存在");
        }
        uploadMapper.deleteById(id);

        SysUserWallpaper active = userWallpaperMapper.selectOne(
                new LambdaQueryWrapper<SysUserWallpaper>()
                        .eq(SysUserWallpaper::getSysUserId, uid));
        if (active != null
                && SOURCE_UPLOAD.equals(active.getSource())
                && Objects.equals(active.getUrl(), row.getUrl())) {
            active.setSource(SOURCE_DEFAULT);
            active.setUrl(null);
            active.setPresetId(null);
            userWallpaperMapper.updateById(active);
        }
        return ReturnResult.ok(true);
    }

    /**
     * 读取用户当前设置
     *
     * @param userId 用户ID
     * @return 设置；没有记录时为 null
     */
    private WallpaperSettingVO readSetting(Integer userId) {
        SysUserWallpaper row = userWallpaperMapper.selectOne(
                new LambdaQueryWrapper<SysUserWallpaper>()
                        .eq(SysUserWallpaper::getSysUserId, userId));
        if (row == null) {
            return null;
        }
        if (!SOURCE_BUILTIN.equals(row.getSource())) {
            return toVo(row, null);
        }
        SysWallpaper preset = findEnabledPreset(row.getPresetId(), null);
        if (preset == null) {
            // 目录项被删或下架：回落默认，别让前端拿着一个解析不出 css 的配置
            return defaultVo();
        }
        return toVo(row, preset);
    }

    /**
     * 按ID或编码取启用中的目录项
     *
     * <p>优先按主键查；主键缺失时按 {@code code} 查。前端静态清单里只有 code，
     * 没有库内主键，按 code 解析后老用户从本地升级上来的选择也能保存。</p>
     *
     * @param presetId   目录ID，可为 null
     * @param presetCode 目录编码，可为 null
     * @return 目录项；不存在或已停用时为 null
     */
    private SysWallpaper findEnabledPreset(Integer presetId, String presetCode) {
        SysWallpaper preset = null;
        if (presetId != null) {
            preset = wallpaperMapper.selectById(presetId);
        } else if (presetCode != null && !presetCode.isEmpty()) {
            preset = wallpaperMapper.selectOne(
                    new LambdaQueryWrapper<SysWallpaper>()
                            .eq(SysWallpaper::getSysWallpaperCode, presetCode));
        }
        if (preset == null || !Integer.valueOf(STATUS_ENABLED).equals(preset.getSysWallpaperStatus())) {
            return null;
        }
        return preset;
    }

    /**
     * 实体转返回体，并把目录里的 css / 名称解析进去
     *
     * @param row    用户设置
     * @param preset 目录项，可为 null
     * @return 返回体
     */
    private WallpaperSettingVO toVo(SysUserWallpaper row, SysWallpaper preset) {
        WallpaperSettingVO vo = new WallpaperSettingVO();
        vo.setId(row.getSysUserWallpaperId());
        vo.setSource(row.getSource());
        vo.setPresetId(row.getPresetId());
        vo.setUrl(row.getUrl());
        vo.setTile(row.getTile());
        vo.setOpacity(row.getOpacity());
        vo.setBlur(row.getBlur());
        vo.setDim(row.getDim());
        if (preset != null) {
            vo.setName(preset.getSysWallpaperName());
            vo.setPresetCode(preset.getSysWallpaperCode());
            vo.setCss(preset.getSysWallpaperCss());
            vo.setCssDark(preset.getSysWallpaperCssDark());
            if (vo.getUrl() == null) {
                vo.setUrl(preset.getSysWallpaperUrl());
            }
        }
        return vo;
    }

    /**
     * 默认设置（不铺图）
     *
     * @return 默认返回体
     */
    private WallpaperSettingVO defaultVo() {
        WallpaperSettingVO vo = new WallpaperSettingVO();
        vo.setSource(SOURCE_DEFAULT);
        vo.setTile(DEFAULT_TILE);
        vo.setOpacity(DEFAULT_OPACITY);
        vo.setBlur(0);
        vo.setDim(BigDecimal.ZERO);
        return vo;
    }

    /**
     * 判断地址是否指向本站文件回显
     *
     * <p>只放行相对路径或 http(s) 绝对地址，且必须命中 {@code /v2/file/view/}；
     * {@code javascript:} / {@code data:} / {@code //evil.com} 一律拒绝。</p>
     *
     * @param url 图片地址
     * @return 是否合法
     */
    private boolean isOwnFileUrl(String url) {
        if (url == null || url.isEmpty() || url.length() > MAX_URL_LENGTH) {
            return false;
        }
        String lower = url.toLowerCase(Locale.ROOT);
        boolean schemeAllowed = lower.startsWith("/")
                || lower.startsWith("http://")
                || lower.startsWith("https://");
        return schemeAllowed && lower.contains(FILE_VIEW_PATH);
    }

    /**
     * 平铺方式归一：请求值非法时用目录推荐值，再不行用默认值
     *
     * @param tile     请求值
     * @param preset   目录项，可为 null
     * @return 合法平铺方式
     */
    private String normalizeTile(String tile, SysWallpaper preset) {
        if (tile != null && TILES.contains(tile)) {
            return tile;
        }
        String fallback = preset == null ? null : preset.getSysWallpaperTile();
        if (fallback != null && TILES.contains(fallback)) {
            return fallback;
        }
        return DEFAULT_TILE;
    }

    /**
     * 小数参数归一：null 用兜底值，负数归零，超上限截断
     *
     * @param value    请求值
     * @param fallback 兜底值
     * @param max      上限
     * @return 归一后的值
     */
    private BigDecimal clampDecimal(BigDecimal value, BigDecimal fallback, BigDecimal max) {
        if (value == null) {
            return fallback;
        }
        if (value.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return value.compareTo(max) > 0 ? max : value;
    }

    /**
     * 模糊半径归一
     *
     * @param value    请求值
     * @param fallback 兜底值
     * @return 0~24 之间的整数
     */
    private int clampBlur(Integer value, Integer fallback) {
        int blur = value == null ? (fallback == null ? 0 : fallback) : value;
        if (blur < 0) {
            return 0;
        }
        return Math.min(blur, MAX_BLUR);
    }

    /**
     * 空值兜底
     *
     * @param value    原值
     * @param fallback 兜底值
     * @return 原值或兜底值
     */
    private BigDecimal nvl(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
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
     * 截断字符串
     *
     * @param value     原值
     * @param maxLength 最大长度
     * @return 截断后的值
     */
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
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
