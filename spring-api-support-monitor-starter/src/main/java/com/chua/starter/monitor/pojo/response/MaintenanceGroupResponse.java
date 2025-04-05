package com.chua.starter.monitor.pojo.response;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护组响应对象
 *
 * @author CH
 */
@Data
@Schema(description = "维护组响应结果")
public class MaintenanceGroupResponse {

    /**
     * 维护组ID
     */
    @Schema(description = "维护组ID", example = "1")
    private Long maintenanceGroupId;

    /**
     * 维护组名称
     */
    @Schema(description = "维护组名称", example = "生产环境服务器组")
    private String maintenanceGroupName;

    /**
     * 维护组描述
     */
    @Schema(description = "维护组描述", example = "包含所有生产环境的应用服务器")
    private String maintenanceGroupDescription;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用状态", example = "true")
    private Boolean maintenanceGroupEnabled;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-06-01T10:30:00")
    private LocalDateTime maintenanceGroupCreateTime;

    /**
     * 更新时间
     */
    @Schema(description = "最后更新时间", example = "2023-06-05T15:45:00")
    private LocalDateTime maintenanceGroupUpdateTime;
    
    /**
     * 主机数量（非数据库字段）
     * 由服务层计算得出
     */
    @TableField(exist = false)
    @Schema(description = "维护组下的主机数量", example = "5")
    private Integer hostCount;
    
    /**
     * 脚本数量（非数据库字段）
     * 由服务层计算得出
     */
    @TableField(exist = false)
    @Schema(description = "维护组下的脚本数量", example = "10")
    private Integer scriptCount;
} 