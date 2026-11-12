package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 壁纸目录表
 *
 * <p>服务端下发的内置壁纸。{@code kind=css} 时用 {@code sysWallpaperCss} 直接作背景值，
 * 不需要图片资源；{@code kind=image} 时用 {@code sysWallpaperUrl} 指向位图。</p>
 *
 * @author CH
 * @since 4.0.0
 */
@Schema(description = "壁纸目录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_wallpaper")
public class SysWallpaper extends SysBase {

    @TableId(value = "sys_wallpaper_id", type = IdType.AUTO)
    @Schema(description = "壁纸ID")
    private Integer sysWallpaperId;

    @TableField(value = "sys_wallpaper_code")
    @Schema(description = "壁纸编码（唯一，前端回放用）")
    private String sysWallpaperCode;

    @TableField(value = "sys_wallpaper_name")
    @Schema(description = "壁纸名称")
    private String sysWallpaperName;

    @TableField(value = "sys_wallpaper_group")
    @Schema(description = "分组标题")
    private String sysWallpaperGroup;

    @TableField(value = "sys_wallpaper_kind")
    @Schema(description = "类型: css-渐变/图案 image-位图")
    private String sysWallpaperKind;

    @TableField(value = "sys_wallpaper_css")
    @Schema(description = "CSS 背景值（kind=css）")
    private String sysWallpaperCss;

    @TableField(value = "sys_wallpaper_css_dark")
    @Schema(description = "暗色 CSS 背景值（缺省沿用亮色）")
    private String sysWallpaperCssDark;

    @TableField(value = "sys_wallpaper_url")
    @Schema(description = "位图地址（kind=image）")
    private String sysWallpaperUrl;

    @TableField(value = "sys_wallpaper_tile")
    @Schema(description = "推荐平铺: none/cover/contain/repeat/repeat-x/repeat-y")
    private String sysWallpaperTile;

    @TableField(value = "sys_wallpaper_opacity")
    @Schema(description = "推荐不透明度 0~1")
    private BigDecimal sysWallpaperOpacity;

    @TableField(value = "sys_wallpaper_blur")
    @Schema(description = "推荐模糊半径 0~24")
    private Integer sysWallpaperBlur;

    @TableField(value = "sys_wallpaper_dim")
    @Schema(description = "推荐压暗 0~0.8")
    private BigDecimal sysWallpaperDim;

    @TableField(value = "sys_wallpaper_sort")
    @Schema(description = "排序")
    private Integer sysWallpaperSort;

    @TableField(value = "sys_wallpaper_status")
    @Schema(description = "状态: 0-禁用 1-启用")
    private Integer sysWallpaperStatus;
}
