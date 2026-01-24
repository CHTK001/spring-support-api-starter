package com.chua.starter.monitor.pojo;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 配置管理DTO
 * @author CH
 * @since 2024/12/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigManagementDTO {

    /**
     * 配置ID
     */
    private Integer configId;

    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 255, message = "配置键长度不能超过255个字符")
    private String configKey;

    /**
     * 配置值
     */
    @Size(max = 5000, message = "配置值长度不能超过5000个字符")
    private String configValue;

    /**
     * 配置描述
     */
    @Size(max = 500, message = "配置描述长度不能超过500个字符")
    private String configDescription;

    /**
     * 环境
     */
    @Size(max = 50, message = "环境名称长度不能超过50个字符")
    private String configEnv;

    /**
     * 配置状态 0-禁用 1-启用
     */
    private Integer configStatus;

    /**
     * 所属应用
     */
    @Size(max = 100, message = "应用名称长度不能超过100个字符")
    private String configApp;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 配置查询DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigQueryDTO {
        
        /**
         * 关键词搜索（配置键/值/描述）
         */
        private String keyword;

        /**
         * 环境筛选
         */
        private String env;

        /**
         * 应用筛选
         */
        private String app;

        /**
         * 状态筛选
         */
        private Integer status;

        /**
         * 页码
         */
        @Builder.Default
        private Integer pageNum = 1;

        /**
         * 页大小
         */
        @Builder.Default
        private Integer pageSize = 10;
    }

    /**
     * 配置下发请求DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigPushRequest {
        
        /**
         * 配置ID列表
         */
        @NotNull(message = "配置ID列表不能为空")
        private List<Integer> configIds;

        /**
         * 服务器ID列表
         */
        @NotNull(message = "服务器ID列表不能为空")
        private List<Integer> serverIds;
    }

    /**
     * 配置下发结果DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigPushResult {
        
        /**
         * 总数
         */
        private int total;

        /**
         * 成功数
         */
        private int success;

        /**
         * 失败数
         */
        private int failed;

        /**
         * 各节点结果
         */
        private List<NodePushResult> results;
    }

    /**
     * 节点下发结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodePushResult {
        
        /**
         * 服务器ID
         */
        private Integer serverId;

        /**
         * 服务器名称
         */
        private String serverName;

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 错误信息
         */
        private String errorMessage;
    }

    /**
     * 推送历史查询DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PushHistoryQueryDTO {

        /**
         * 配置ID
         */
        private Integer configId;

        /**
         * 服务器ID
         */
        private Integer serverId;

        /**
         * 配置键关键词
         */
        private String keyword;

        /**
         * 推送状态 0-失败 1-成功
         */
        private Integer pushSuccess;

        /**
         * 页码
         */
        @Builder.Default
        private Integer pageNum = 1;

        /**
         * 页大小
         */
        @Builder.Default
        private Integer pageSize = 10;
    }

    /**
     * 配置下发历史
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigPushHistory {
        
        /**
         * 历史ID
         */
        private Long id;

        /**
         * 配置ID
         */
        private Integer configId;

        /**
         * 配置键
         */
        private String configKey;

        /**
         * 配置值（推送时的快照）
         */
        private String configValue;

        /**
         * 服务器ID
         */
        private Integer serverId;

        /**
         * 服务器名称
         */
        private String serverName;

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 下发时间
         */
        private LocalDateTime pushTime;

        /**
         * 操作人
         */
        private String operator;
    }
}
