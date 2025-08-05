package com.chua.starter.monitor.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 批量测试连接结果DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BatchTestConnectionResultDTO", description = "批量测试连接结果DTO")
@Schema(description = "批量测试连接结果DTO")
public class BatchTestConnectionResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总数
     */
    @ApiModelProperty(value = "总数")
    @Schema(description = "总数")
    private Integer total;

    /**
     * 成功数
     */
    @ApiModelProperty(value = "成功数")
    @Schema(description = "成功数")
    private Integer success;

    /**
     * 失败数
     */
    @ApiModelProperty(value = "失败数")
    @Schema(description = "失败数")
    private Integer failed;

    /**
     * 成功列表
     */
    @ApiModelProperty(value = "成功列表")
    @Schema(description = "成功列表")
    private List<ServerTestResultDTO> successList;

    /**
     * 失败列表
     */
    @ApiModelProperty(value = "失败列表")
    @Schema(description = "失败列表")
    private List<ServerTestResultDTO> failedList;

    /**
     * 获取成功率
     */
    public Double getSuccessRate() {
        if (total == null || total == 0 || success == null) {
            return 0.0;
        }
        return (double) success / total * 100;
    }

    /**
     * 获取失败率
     */
    public Double getFailureRate() {
        if (total == null || total == 0 || failed == null) {
            return 0.0;
        }
        return (double) failed / total * 100;
    }

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return total != null && success != null && total.equals(success);
    }

    /**
     * 是否全部失败
     */
    public boolean isAllFailed() {
        return total != null && failed != null && total.equals(failed);
    }

    /**
     * 服务器测试结果内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "ServerTestResultDTO", description = "服务器测试结果DTO")
    @Schema(description = "服务器测试结果DTO")
    public static class ServerTestResultDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 服务器ID
         */
        @ApiModelProperty(value = "服务器ID")
        @Schema(description = "服务器ID")
        private Integer serverId;

        /**
         * 服务器名称
         */
        @ApiModelProperty(value = "服务器名称")
        @Schema(description = "服务器名称")
        private String serverName;

        /**
         * 主机地址
         */
        @ApiModelProperty(value = "主机地址")
        @Schema(description = "主机地址")
        private String host;

        /**
         * 端口
         */
        @ApiModelProperty(value = "端口")
        @Schema(description = "端口")
        private Integer port;

        /**
         * 协议
         */
        @ApiModelProperty(value = "协议")
        @Schema(description = "协议")
        private String protocol;

        /**
         * 是否成功
         */
        @ApiModelProperty(value = "是否成功")
        @Schema(description = "是否成功")
        private Boolean success;

        /**
         * 消息
         */
        @ApiModelProperty(value = "消息")
        @Schema(description = "消息")
        private String message;
    }
}
