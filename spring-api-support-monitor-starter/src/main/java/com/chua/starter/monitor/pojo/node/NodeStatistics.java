package com.chua.starter.monitor.pojo.node;

import com.chua.starter.monitor.pojo.node.OnlineNodeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 节点统计信息
 *
 * @author CH
 * @since 2024/12/30
 */
@ApiModel(description = "节点统计信息")
@Schema(description = "节点统计信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总节点数
     */
    @ApiModelProperty(value = "总节点数")
    @Schema(description = "总节点数")
    private Integer totalNodes;

    /**
     * 在线节点数
     */
    @ApiModelProperty(value = "在线节点数")
    @Schema(description = "在线节点数")
    private Integer onlineNodes;

    /**
     * 离线节点数
     */
    @ApiModelProperty(value = "离线节点数")
    @Schema(description = "离线节点数")
    private Integer offlineNodes;

    /**
     * 健康节点数
     */
    @ApiModelProperty(value = "健康节点数")
    @Schema(description = "健康节点数")
    private Integer healthyNodes;

    /**
     * 异常节点数
     */
    @ApiModelProperty(value = "异常节点数")
    @Schema(description = "异常节点数")
    private Integer errorNodes;

    /**
     * 维护中节点数
     */
    @ApiModelProperty(value = "维护中节点数")
    @Schema(description = "维护中节点数")
    private Integer maintenanceNodes;

    /**
     * 总连接数
     */
    @ApiModelProperty(value = "总连接数")
    @Schema(description = "总连接数")
    private Integer totalConnections;

    /**
     * 平均响应时间
     */
    @ApiModelProperty(value = "平均响应时间")
    @Schema(description = "平均响应时间（毫秒）")
    private Double averageResponseTime;

    /**
     * 平均CPU使用率
     */
    @ApiModelProperty(value = "平均CPU使用率")
    @Schema(description = "平均CPU使用率")
    private Double averageCpuUsage;

    /**
     * 平均内存使用率
     */
    @ApiModelProperty(value = "平均内存使用率")
    @Schema(description = "平均内存使用率")
    private Double averageMemoryUsage;

    /**
     * 平均磁盘使用率
     */
    @ApiModelProperty(value = "平均磁盘使用率")
    @Schema(description = "平均磁盘使用率")
    private Double averageDiskUsage;

    /**
     * 按状态分组的节点数量
     */
    @ApiModelProperty(value = "按状态分组的节点数量")
    @Schema(description = "按状态分组的节点数量")
    private Map<String, Integer> nodesByStatus;

    /**
     * 按应用名称分组的节点数量
     */
    @ApiModelProperty(value = "按应用名称分组的节点数量")
    @Schema(description = "按应用名称分组的节点数量")
    private Map<String, Integer> nodesByApplication;

    /**
     * 按节点类型分组的节点数量
     */
    @ApiModelProperty(value = "按节点类型分组的节点数量")
    @Schema(description = "按节点类型分组的节点数量")
    private Map<String, Integer> nodesByType;

    /**
     * 最近活跃的节点列表
     */
    @ApiModelProperty(value = "最近活跃的节点列表")
    @Schema(description = "最近活跃的节点列表")
    private List<OnlineNodeInfo> recentActiveNodes;

    /**
     * 响应时间最快的节点列表
     */
    @ApiModelProperty(value = "响应时间最快的节点列表")
    @Schema(description = "响应时间最快的节点列表")
    private List<OnlineNodeInfo> fastestNodes;

    /**
     * 负载最高的节点列表
     */
    @ApiModelProperty(value = "负载最高的节点列表")
    @Schema(description = "负载最高的节点列表")
    private List<OnlineNodeInfo> highestLoadNodes;

    /**
     * 获取在线率
     */
    public Double getOnlineRate() {
        if (totalNodes == null || totalNodes == 0) {
            return 0.0;
        }
        return (double) (onlineNodes != null ? onlineNodes : 0) / totalNodes * 100;
    }

    /**
     * 获取健康率
     */
    public Double getHealthyRate() {
        if (totalNodes == null || totalNodes == 0) {
            return 0.0;
        }
        return (double) (healthyNodes != null ? healthyNodes : 0) / totalNodes * 100;
    }

    /**
     * 获取平均连接数
     */
    public Double getAverageConnections() {
        if (totalNodes == null || totalNodes == 0) {
            return 0.0;
        }
        return (double) (totalConnections != null ? totalConnections : 0) / totalNodes;
    }

    /**
     * 判断整体状态是否良好
     */
    public boolean isOverallHealthy() {
        return getHealthyRate() >= 80.0 && getOnlineRate() >= 90.0;
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        if (isOverallHealthy()) {
            return "系统运行良好";
        } else if (getOnlineRate() < 50.0) {
            return "系统异常，大量节点离线";
        } else if (getHealthyRate() < 60.0) {
            return "系统不稳定，部分节点异常";
        } else {
            return "系统基本正常";
        }
    }
}
