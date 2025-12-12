package com.chua.starter.monitor.pojo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 服务实例 DTO
 * <p>
 * 用于存储和展示服务之间的调用关系
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 服务实例信息")
public class AgentServiceInstanceDTO {

    /**
     * 服务名称
     */
    @Schema(description = "服务名称")
    private String name;

    /**
     * 来源类型
     */
    @Schema(description = "来源类型")
    private String sourceName;

    /**
     * 来源主机
     */
    @Schema(description = "来源主机")
    private String sourceHost;

    /**
     * 来源端口
     */
    @Schema(description = "来源端口")
    private Integer sourcePort;

    /**
     * 目标主机
     */
    @Schema(description = "目标主机")
    private String targetHost;

    /**
     * 目标端口
     */
    @Schema(description = "目标端口")
    private Integer targetPort;

    /**
     * 调用次数
     */
    @Schema(description = "调用次数")
    private Integer callCount;

    /**
     * 最后调用时间
     */
    @Schema(description = "最后调用时间（毫秒时间戳）")
    private Long lastCallTime;

    /**
     * 平均耗时（毫秒）
     */
    @Schema(description = "平均耗时（毫秒）")
    private Long avgCostTime;

    /**
     * 错误次数
     */
    @Schema(description = "错误次数")
    private Integer errorCount;
}
