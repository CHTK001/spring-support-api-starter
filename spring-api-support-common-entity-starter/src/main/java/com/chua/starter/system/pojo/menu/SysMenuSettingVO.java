package com.chua.starter.system.pojo.menu;

import com.chua.starter.system.entity.SysMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author CH
 * @since 2024/8/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "菜单")
public class SysMenuSettingVO extends SysMenu {

    /**
     * 子菜单
     */
    @Schema(description = "子菜单")
    private List<SysMenuSettingVO> children;

}
