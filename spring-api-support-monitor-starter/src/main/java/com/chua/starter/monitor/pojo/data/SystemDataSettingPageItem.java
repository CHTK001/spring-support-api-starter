package com.chua.starter.monitor.pojo.data;

import com.chua.common.support.data.query.FeatureSupportLevel;
import com.chua.starter.monitor.entity.SystemDataSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 数据源分页项，携带能力信息
 *
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据源分页项，包含能力标识")
public class SystemDataSettingPageItem extends SystemDataSetting {

    /** 是否支持备份 */
    @Schema(description = "是否支持备份")
    private boolean capabilitiesBackup;

    /** 是否支持文档 */
    @Schema(description = "是否支持文档")
    private boolean capabilitiesDocument;

    /** 是否文件型 */
    @Schema(description = "是否文件型数据源")
    private boolean capabilitiesFile;

    /** 是否支持层级元数据 */
    @Schema(description = "是否支持层级元数据")
    private boolean capabilitiesHierarchy;

    /** 是否支持命令执行 */
    @Schema(description = "是否支持命令执行")
    private boolean capabilitiesCommand;

    /** 是否支持 ORM Search */
    @Schema(description = "是否支持 ORM Search")
    private boolean capabilitiesOrmSearch;

    /** 是否支持 ORM Update */
    @Schema(description = "是否支持 ORM Update")
    private boolean capabilitiesOrmUpdate;

    /** 是否支持参数化 SQL 查询 */
    @Schema(description = "是否支持参数化 SQL 查询")
    private boolean capabilitiesSqlQuery;

    /** 是否支持参数化 SQL 更新 */
    @Schema(description = "是否支持参数化 SQL 更新")
    private boolean capabilitiesSqlUpdate;

    /** 是否支持结构管理 */
    @Schema(description = "是否支持结构管理")
    private boolean capabilitiesStructure;

    /** 是否支持索引管理 */
    @Schema(description = "是否支持索引管理")
    private boolean capabilitiesIndexes;

    /** 是否支持分区管理 */
    @Schema(description = "是否支持分区管理")
    private boolean capabilitiesPartitions;

    /** 是否支持行级写入 */
    @Schema(description = "是否支持行级写入")
    private boolean capabilitiesRowMutation;

    /** 是否支持 KV 能力 */
    @Schema(description = "是否支持 KV 能力")
    private boolean capabilitiesKv;

    /** 是否支持时序能力 */
    @Schema(description = "是否支持时序能力")
    private boolean capabilitiesTimeSeries;

    /** 是否支持 Topic 能力 */
    @Schema(description = "是否支持 Topic 能力")
    private boolean capabilitiesTopic;

    /** 是否支持 Topic 管理能力 */
    @Schema(description = "是否支持 Topic 管理能力")
    private boolean capabilitiesTopicAdmin;

    /** 是否支持集群能力 */
    @Schema(description = "是否支持集群能力")
    private boolean capabilitiesCluster;

    /** 是否支持节点能力 */
    @Schema(description = "是否支持节点能力")
    private boolean capabilitiesNode;

    /** 是否支持备份生命周期 */
    @Schema(description = "是否支持备份生命周期")
    private boolean capabilitiesBackupLifecycle;

    /** 是否支持表级备份 */
    @Schema(description = "是否支持表级备份")
    private boolean capabilitiesTableBackup;

    /** 能力支持级别 */
    @Schema(description = "能力支持级别，值为 NONE/ATTACHED/FULL")
    private Map<String, FeatureSupportLevel> capabilityLevels;
}


