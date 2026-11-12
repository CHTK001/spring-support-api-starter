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
 * 用户壁纸设置表
 *
 * <p>一人一条：{@code sys_user_id} 上有唯一键，保存时按用户 upsert。
 * 存的是「当前佩戴」而不是历史，切走即覆盖。</p>
 *
 * @author CH
 * @since 4.0.0
 */
@Schema(description = "用户壁纸设置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_wallpaper")
public class SysUserWallpaper extends SysBase {

    @TableId(value = "sys_user_wallpaper_id", type = IdType.AUTO)
    @Schema(description = "用户壁纸ID")
    private Integer sysUserWallpaperId;

    @TableField(value = "sys_user_id")
    @Schema(description = "用户ID")
    private Integer sysUserId;

    @TableField(value = "source")
    @Schema(description = "来源: default-不铺图 builtin-内置 upload-自己上传")
    private String source;

    @TableField(value = "preset_id")
    @Schema(description = "内置壁纸ID（source=builtin）")
    private Integer presetId;

    @TableField(value = "url")
    @Schema(description = "位图地址（source=upload）")
    private String url;

    @TableField(value = "tile")
    @Schema(description = "平铺方式")
    private String tile;

    @TableField(value = "opacity")
    @Schema(description = "不透明度 0~1")
    private BigDecimal opacity;

    @TableField(value = "blur")
    @Schema(description = "模糊半径 0~24")
    private Integer blur;

    @TableField(value = "dim")
    @Schema(description = "压暗 0~0.8")
    private BigDecimal dim;
}
