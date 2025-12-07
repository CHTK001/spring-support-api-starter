package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 鑿滃崟
 *
 * @author CH
 * @since 2024/8/7
 */
@ApiModel(description = "鑿滃崟")
@Schema(description = "鑿滃崟")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_menu")
public class SysMenu extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "sys_menu_id", type = IdType.AUTO)
    @ApiModelProperty(value = "鑿滃崟ID")
    @Schema(description = "鑿滃崟ID")
    @NotNull(message = "涓嶈兘涓簄ull")
    private Integer sysMenuId;

    /**
     * 鑿滃崟鍚嶇О
     */
    @TableField(value = "sys_menu_name")
    @ApiModelProperty(value = "鑿滃崟鍚嶇О")
    @Schema(description = "鑿滃崟鍚嶇О")
    @Size(max = 255, message = "鑿滃崟鍚嶇О鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysMenuName;

    /**
     * 鏄惁棣栭〉
     */
    @TableField(value = "sys_menu_home")
    @ApiModelProperty(value = "鏄惁棣栭〉; 0:鍚")
    @Schema(description = "鏄惁棣栭〉; 0:鍚")
    private Integer sysMenuHome;

    /**
     * 鑿滃崟寮曟搸
     */
    @TableField(value = "sys_menu_engine")
    @ApiModelProperty(value = "鏄惁engine甯冨眬涓婚〉; 0:鍚")
    @Schema(description = "鏄惁engine甯冨眬涓婚〉; 0:鍚")
    private Integer sysMenuEngine;

    /**
     * 鑿滃崟鏍囬
     */
    @Schema(description = "鏍囬")
    @TableField(value = "sys_menu_title")
    @ApiModelProperty(value = "鏍囬")
    private String sysMenuTitle;

    /**
     * 鎸夐挳鏉冮檺
     */
    @Schema(description = "鏉冮檺")
    @TableField(value = "sys_menu_perm")
    private String sysMenuPerm;

    /**
     * 鑿滃崟婵€娲昏矾寰?
     */
    @Schema(description = "鑿滃崟婵€娲昏矾寰")
    @TableField(value = "sys_menu_active_path")
    private String sysMenuActivePath;

    /**
     * 閾炬帴鍦板潃
     */
    @Schema(description = "閾炬帴鍦板潃")
    @TableField(value = "sys_menu_frame_src")
    private String sysMenuFrameSrc;

    /**
     * 鑿滃崟璺緞
     */
    @TableField(value = "sys_menu_path")
    @ApiModelProperty(value = "鑿滃崟璺緞")
    @Schema(description = "鑿滃崟璺緞")
    private String sysMenuPath;

    /**
     * 鍥介檯鍖?
     */
    @TableField(value = "sys_menu_i18n")
    @ApiModelProperty(value = "鍥介檯鍖")
    @Schema(description = "鍥介檯鍖")
    @Size(max = 255, message = "鍥介檯鍖栨渶澶ч暱搴﹁灏忎簬 255")
    private String sysMenuI18n;

    /**
     * 鐖惰彍鍗?
     */
    @TableField(value = "sys_menu_pid")
    @ApiModelProperty(value = "鐖惰彍鍗")
    @Schema(description = "鐖惰彍鍗")
    private Integer sysMenuPid;

    /**
     * 鑿滃崟璺緞
     */
    @TableField(value = "sys_menu_tree_id")
    @ApiModelProperty(value = "鑿滃崟璺緞")
    @Schema(description = "鑿滃崟璺緞(绯荤粺鐢熸垚)")
    @Size(max = 255, message = "鑿滃崟璺緞鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysMenuTreeId;

    /**
     * 鑿滃崟缁勪欢
     */
    @TableField(value = "sys_menu_component")
    @ApiModelProperty(value = "鑿滃崟缁勪欢")
    @Schema(description = "鑿滃崟缁勪欢")
    @Size(max = 255, message = "鑿滃崟缁勪欢鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysMenuComponent;

    /**
     * 鑿滃崟鍥炬爣
     */
    @TableField(value = "sys_menu_icon")
    @ApiModelProperty(value = "鑿滃崟鍥炬爣")
    @Schema(description = "鑿滃崟鍥炬爣")
    @Size(max = 255, message = "鑿滃崟鍥炬爣鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysMenuIcon;

    /**
     * 鍙充晶鑿滃崟鍥炬爣
     */
    @TableField(value = "sys_menu_extra_icon")
    @ApiModelProperty(value = "鍙充晶鑿滃崟鍥炬爣")
    @Schema(description = "鍙充晶鑿滃崟鍥炬爣")
    @Size(max = 255, message = "鍙充晶鑿滃崟鍥炬爣鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysMenuExtraIcon;

    /**
     * 鑿滃崟绫诲瀷: 鑿滃崟:0 iframe:1 澶栭摼:2 鎸夐挳:3
     */
    @TableField(value = "sys_menu_type")
    @ApiModelProperty(value = "鑿滃崟:0 iframe:1 澶栭摼:2 鎸夐挳:3")
    @Schema(description = "鑿滃崟:0 iframe:1 澶栭摼:2 鎸夐挳:3")
    private Integer sysMenuType;

    /**
     * 浼樺厛绾?
     */
    @TableField(value = "sys_menu_sort")
    @ApiModelProperty(value = "浼樺厛绾")
    @Schema(description = "浼樺厛绾")
    private Integer sysMenuSort;

    /**
     * 鎵€灞炴潈闄?
     */
    @TableField(value = "sys_menu_role")
    @ApiModelProperty(value = "鎵€灞炴潈闄")
    @Schema(description = "鎵€灞炴潈闄")
    private String sysMenuRole;

    /**
     * 璺敱閲嶅畾鍚?
     */
    @TableField(value = "sys_menu_redirect")
    @ApiModelProperty(value = "璺敱閲嶅畾鍚")
    @Schema(description = "璺敱閲嶅畾鍚")
    private String sysMenuRedirect;

    /**
     * 杩涘満鍔ㄧ敾
     */
    @TableField(value = "sys_menu_enter_transition")
    @ApiModelProperty(value = "杩涘満鍔ㄧ敾")
    @Schema(description = "杩涘満鍔ㄧ敾")
    private String sysMenuEnterTransition;

    /**
     * 绂诲満鍔ㄧ敾
     */
    @TableField(value = "sys_menu_leave_transition")
    @ApiModelProperty(value = "绂诲満鍔ㄧ敾")
    @Schema(description = "绂诲満鍔ㄧ敾")
    private String sysMenuLeaveTransition;

    /**
     * 鏄惁缂撳瓨; 0:鍚?
     */
    @TableField(value = "sys_menu_keep_alive")
    @ApiModelProperty(value = "鏄惁缂撳瓨; 0:鍚")
    @Schema(description = "鏄惁缂撳瓨; 0:鍚")
    private Integer sysMenuKeepAlive;

    /**
     * 鏄惁鍦ㄦ爣绛炬爮闅愯棌; 0:鍚?
     */
    @TableField(value = "sys_menu_hidden_tag")
    @ApiModelProperty(value = "鏄惁鍦ㄦ爣绛炬爮闅愯棌; 0:鍚")
    @Schema(description = "鏄惁鍦ㄦ爣绛炬爮闅愯棌; 0:鍚")
    private Integer sysMenuHiddenTag;

    /**
     * 鏄惁鍥哄畾鏍囩; 0:鍚?
     */
    @TableField(value = "sys_menu_fixed_tag")
    @ApiModelProperty(value = "鏄惁鍥哄畾鏍囩; 0:鍚")
    @Schema(description = "鏄惁鍥哄畾鏍囩; 0:鍚")
    private Integer sysMenuFixedTag;

    /**
     * 鏄惁闅愯棌; 0:鏄剧ず
     */
    @TableField(value = "sys_menu_hidden")
    @ApiModelProperty(value = "鏄惁闅愯棌; 0:鏄剧ず")
    @Schema(description = "鏄惁闅愯棌; 0:鏄剧ず")
    private Integer sysMenuHidden;

    /**
     * 褰撲竴涓瓙绾ц彍鍗曟槸鍚﹂殣钘忕埗绾ц彍鍗; 0:鏄剧ず
     */
    @TableField(value = "sys_menu_show_parent")
    @ApiModelProperty(value = "褰撲竴涓瓙绾ц彍鍗曟槸鍚﹂殣钘忕埗绾ц彍鍗; 0:鏄剧ず")
    @Schema(description = "褰撲竴涓瓙绾ц彍鍗曟槸鍚﹂殣钘忕埗绾ц彍鍗; 0:鏄剧ず")
    private Integer sysMenuShowParent;
}