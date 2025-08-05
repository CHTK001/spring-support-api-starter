package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 脚本分类实体
 *
 * @author CH
 * @since 2024/12/19
 */
@Schema(description = "脚本分类实体")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_script_category")
public class MonitorSysGenScriptCategory extends SysBase {

    /**
     * 分类ID
     */
    @Schema(description = "分类ID", example = "1")
    @TableId(value = "monitor_sys_gen_script_category_id", type = IdType.AUTO)
    private Integer monitorSysGenScriptCategoryId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "系统监控")
    @TableField("monitor_sys_gen_script_category_name")
    private String monitorSysGenScriptCategoryName;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述", example = "系统监控相关脚本分类")
    @TableField("monitor_sys_gen_script_category_description")
    private String monitorSysGenScriptCategoryDescription;

    /**
     * 父分类ID
     */
    @Schema(description = "父分类ID", example = "0")
    @TableField("monitor_sys_gen_script_category_parent_id")
    private Integer monitorSysGenScriptCategoryParentId;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1", minimum = "0")
    @TableField("monitor_sys_gen_script_category_sort")
    private Integer monitorSysGenScriptCategorySort;

    /**
     * 状态
     */
    @Schema(description = "状态", implementation = CategoryStatus.class)
    @TableField("monitor_sys_gen_script_category_status")
    private CategoryStatus monitorSysGenScriptCategoryStatus;

    /**
     * 分类状态枚举
     */
    @Schema(description = "分类状态枚举")
    public enum CategoryStatus {
        @Schema(description = "禁用")
        DISABLED(0, "禁用"), @Schema(description = "启用")
        ENABLED(1, "启用");

        private final Integer code;
        private final String desc;

        CategoryStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static CategoryStatus fromCode(Integer code) {
            if (code == null) {
                return null;
            }
            for (CategoryStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 检查分类是否启用
     */
    public boolean isEnabled() {
        return CategoryStatus.ENABLED.equals(monitorSysGenScriptCategoryStatus);
    }

    /**
     * 检查是否为根分类
     */
    public boolean isRootCategory() {
        return monitorSysGenScriptCategoryParentId == null || monitorSysGenScriptCategoryParentId == 0;
    }

    /**
     * 获取排序值
     */
    public int getSortOrDefault() {
        return monitorSysGenScriptCategorySort != null ? monitorSysGenScriptCategorySort : 0;
    }
}
