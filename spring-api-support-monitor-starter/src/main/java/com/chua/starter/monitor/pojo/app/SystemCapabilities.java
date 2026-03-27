package com.chua.starter.monitor.pojo.app;

import com.chua.common.support.data.query.FeatureSupportLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 系统能力
 *
 * 表示数据源在备份、文档、文件等方面的能力支持。
 *
 * @author CH
 */
@Data
@Schema(description = "系统能力")
public class SystemCapabilities {

    /** 是否支持备份 */
    @Schema(description = "是否支持备份")
    private boolean backup;

    /** 是否支持文档 */
    @Schema(description = "是否支持文档")
    private boolean document;

    /** 是否支持文件 */
    @Schema(description = "是否支持文件")
    private boolean file;

    /** 是否支持层级元数据 */
    @Schema(description = "是否支持层级元数据")
    private boolean hierarchy;

    /** 是否支持命令执行 */
    @Schema(description = "是否支持命令执行")
    private boolean command;

    /** 是否支持 ORM Search */
    @Schema(description = "是否支持 ORM Search")
    private boolean ormSearch;

    /** 是否支持 ORM Update */
    @Schema(description = "是否支持 ORM Update")
    private boolean ormUpdate;

    /** 是否支持参数化 SQL 查询 */
    @Schema(description = "是否支持参数化 SQL 查询")
    private boolean sqlQuery;

    /** 是否支持参数化 SQL 更新 */
    @Schema(description = "是否支持参数化 SQL 更新")
    private boolean sqlUpdate;

    /** 是否支持结构管理 */
    @Schema(description = "是否支持结构管理")
    private boolean structure;

    /** 是否支持索引管理 */
    @Schema(description = "是否支持索引管理")
    private boolean indexes;

    /** 是否支持分区管理 */
    @Schema(description = "是否支持分区管理")
    private boolean partitions;

    /** 是否支持行级写入 */
    @Schema(description = "是否支持行级写入")
    private boolean rowMutation;

    /** 是否支持 KV 能力 */
    @Schema(description = "是否支持 KV 能力")
    private boolean kv;

    /** 是否支持时序能力 */
    @Schema(description = "是否支持时序能力")
    private boolean timeSeries;

    /** 是否支持 Topic 能力 */
    @Schema(description = "是否支持 Topic 能力")
    private boolean topic;

    /** 是否支持 Topic 管理能力 */
    @Schema(description = "是否支持 Topic 管理能力")
    private boolean topicAdmin;

    /** 是否支持集群能力 */
    @Schema(description = "是否支持集群能力")
    private boolean cluster;

    /** 是否支持节点能力 */
    @Schema(description = "是否支持节点能力")
    private boolean node;

    /** 能力支持级别 */
    @Schema(description = "能力支持级别，值为 NONE/ATTACHED/FULL")
    private Map<String, FeatureSupportLevel> capabilityLevels;

    /** 是否支持备份生命周期 */
    @Schema(description = "是否支持备份生命周期")
    private boolean backupLifecycle;

    /** 是否支持表级备份 */
    @Schema(description = "是否支持表级备份")
    private boolean tableBackup;

    /** 是否会主动推送新邮件 */
    @Schema(description = "邮箱 是否支持 IDLE")
    private boolean emailIdle;

    /** 是否会主动推送新邮件 */
    @Schema(description = "是否会主动推送新邮件")
    private boolean emailExists;

}
