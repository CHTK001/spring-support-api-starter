package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 饰品目录表
 *
 * <p>服务端下发的可佩戴小件。按 {@code slot} 分槽位，每个槽位每人最多佩戴一件：</p>
 * <ul>
 *   <li>{@code avatar}：头像挂件，{@code kind=glyph} 用字符（★ / ❀ 等），
 *       也可以给 {@code image} 指向位图；</li>
 *   <li>{@code nameplate}：昵称牌，{@code css} 是昵称底色的 CSS 背景值，
 *       {@code color} 是文字色，前端直接写行内样式。</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0
 */
@Schema(description = "饰品目录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ornament")
public class SysOrnament extends SysBase {

    @TableId(value = "sys_ornament_id", type = IdType.AUTO)
    @Schema(description = "饰品ID")
    private Integer sysOrnamentId;

    @TableField(value = "sys_ornament_code")
    @Schema(description = "饰品编码（唯一，前端回放用）")
    private String sysOrnamentCode;

    @TableField(value = "sys_ornament_name")
    @Schema(description = "饰品名称")
    private String sysOrnamentName;

    @TableField(value = "sys_ornament_slot")
    @Schema(description = "槽位: avatar-头像挂件 nameplate-昵称牌")
    private String sysOrnamentSlot;

    @TableField(value = "sys_ornament_kind")
    @Schema(description = "类型: glyph-字符 css-样式")
    private String sysOrnamentKind;

    @TableField(value = "sys_ornament_glyph")
    @Schema(description = "字符（kind=glyph）")
    private String sysOrnamentGlyph;

    @TableField(value = "sys_ornament_image")
    @Schema(description = "位图地址（可选，优先于字符）")
    private String sysOrnamentImage;

    @TableField(value = "sys_ornament_css")
    @Schema(description = "CSS 背景（nameplate）")
    private String sysOrnamentCss;

    @TableField(value = "sys_ornament_color")
    @Schema(description = "前景色（字符色 / 昵称文字色）")
    private String sysOrnamentColor;

    @TableField(value = "sys_ornament_sort")
    @Schema(description = "排序")
    private Integer sysOrnamentSort;

    @TableField(value = "sys_ornament_status")
    @Schema(description = "状态: 0-禁用 1-启用")
    private Integer sysOrnamentStatus;
}
