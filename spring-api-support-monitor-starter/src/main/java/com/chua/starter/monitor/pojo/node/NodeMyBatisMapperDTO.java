package com.chua.starter.monitor.pojo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 节点 MyBatis Mapper 信息 DTO
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Data
@Schema(description = "节点 MyBatis Mapper 信息")
public class NodeMyBatisMapperDTO {

    /**
     * Mapper 全限定名
     */
    @Schema(description = "Mapper 全限定名")
    private String name;

    /**
     * Mapper 简名
     */
    @Schema(description = "Mapper 简名")
    private String simpleName;

    /**
     * 方法数量
     */
    @Schema(description = "方法数量")
    private Long methodCount;

    /**
     * MappedStatement 列表
     */
    @Schema(description = "MappedStatement 列表")
    private List<MappedStatementInfo> statements;

    /**
     * MappedStatement 信息
     */
    @Data
    @Schema(description = "MappedStatement 信息")
    public static class MappedStatementInfo {

        /**
         * Statement ID
         */
        @Schema(description = "Statement ID")
        private String id;

        /**
         * 方法名
         */
        @Schema(description = "方法名")
        private String methodName;

        /**
         * SQL 命令类型
         */
        @Schema(description = "SQL 命令类型 (SELECT/INSERT/UPDATE/DELETE)")
        private String sqlCommandType;

        /**
         * 结果类型
         */
        @Schema(description = "结果类型")
        private String resultType;

        /**
         * 资源文件
         */
        @Schema(description = "资源文件")
        private String resource;
    }
}
