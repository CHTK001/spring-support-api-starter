package com.chua.starter.admin.result;

import com.chua.common.support.datasource.annotation.Column;
import com.chua.starter.admin.entity.SysMenu;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 树形结构
 * @author CH
 */
@Data
@Schema(description = "菜单树形结构")
public class SysMenuTreeResult {
    /**
     * 主键
     */
    @Schema(description = "主键")
    private String id;
    /**
     * 父主键
     */
    @Schema(description = "父主键")
    private String parentId;
    /**
     * 路径
     */
    @Schema(description = "路径")
    private String path;
    /**
     * 元数据
     */
    @Schema(description = "元数据")
    private Meta meta;
    /**
     * 子节点数据
     */
    @Schema(description = "子节点数据")
    private List<SysMenuTreeResult> children;

    /**
     * 组件路径(vue页面完整路径，省略.vue后缀)
     */
    @ApiModelProperty("组件路径(vue页面完整路径，省略.vue后缀)")
    private String component;
    /**
     * 标签
     */
    @ApiModelProperty("标签")
    private String menuTag;
    /**
     * 父节点路径，以英文逗号(,)分割
     */
    @ApiModelProperty("父节点路径，以英文逗号(,)分割")
    private String menuTreePath;
    /**
     * 显示状态(1:显示;0:隐藏)
     */
    @ApiModelProperty("显示状态(1:显示;0:隐藏)")
    @Column(defaultValue = "1")
    private Integer menuVisible;
    /**
     * 别名
     */
    @ApiModelProperty("别名")
    private String menuPerm;

    /**
     * 排序
     */
    @ApiModelProperty("排序")
    private Integer menuSort;

    public SysMenu toSysMenu() {
        SysMenu rs = new SysMenu();
        try {
            rs.setMenuId(Long.valueOf(id));
        } catch (Exception ignored) {
        }
        try {
            rs.setMenuParentId(StringUtils.isBlank(parentId) ? 0 : Long.parseLong(parentId));
        } catch (NumberFormatException ignored) {
        }
        rs.setMenuAffix(meta.affix);
        rs.setMenuName(meta.title);
        rs.setMenuIcon(meta.icon);
        rs.setMenuPath(path);
        rs.setMenuComponent(component);
        rs.setMenuTreePath(menuTreePath);
        rs.setMenuType(meta.type);
        rs.setMenuTag(meta.tag);
        rs.setMenuPerm(menuPerm);
        rs.setMenuStatus(meta.status ? 0 : 1);
        rs.setMenuColor(meta.color);
        rs.setMenuSort(meta.sort);
        rs.setMenuVisible(meta.hidden ? 0 : 1);
        return rs;
    }

    @Data
    @Schema(description = "菜单元数据")
    public static class Meta {
        /**
         * 图标
         */
        @Schema(description = "图标")
        private String icon;
        /**
         * 标题
         */
        @Schema(description = "标题")
        private String title;
        /**
         * 标题
         */
        @Schema(description = "是否隐藏")
        private boolean hidden;
        /**
         * 标题
         */
        @Schema(description = "系统是否返回")
        private boolean status;
        /**
         * 类型
         */
        @Schema(description = "类型")
        private String type;
        /**
         * 颜色
         */
        @Schema(description = "颜色")
        private String color;
        /**
         * 优先级
         */
        @Schema(description = "优先级")
        private Integer sort;
        /**
         * 标签
         */
        @Schema(description = "标签")
        private String tag;
        /**
         * 是否固定
         */
        @Schema(description = "是否固定; 1: 固定")
        private Integer affix;
    }
}
