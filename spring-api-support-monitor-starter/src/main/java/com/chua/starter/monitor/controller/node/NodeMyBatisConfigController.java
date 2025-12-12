package com.chua.starter.monitor.controller.node;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.node.NodeMyBatisMapperDTO;
import com.chua.starter.monitor.service.node.NodeMyBatisConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 节点 MyBatis 配置管理控制器
 * <p>
 * 提供节点 MyBatis Mapper 列表查询、详情查看、XML 刷新等功能
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@RestController
@RequestMapping("/v1/node/mybatis")
@RequiredArgsConstructor
@Tag(name = "节点 MyBatis 配置管理", description = "节点 MyBatis 配置管理相关接口")
public class NodeMyBatisConfigController {

    private final NodeMyBatisConfigService nodeMyBatisConfigService;

    /**
     * 获取节点的所有 Mapper 列表
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @param keyword 过滤关键词（可选）
     * @return Mapper 列表
     */
    @GetMapping("/mappers")
    @Operation(summary = "获取节点 Mapper 列表", description = "获取指定节点的所有 MyBatis Mapper 列表")
    public ReturnResult<List<NodeMyBatisMapperDTO>> getMappersForMyBatis(
            @Parameter(description = "节点URL（Base64编码）", required = true)
            @RequestParam String nodeUrl,
            @Parameter(description = "过滤关键词")
            @RequestParam(required = false) String keyword) {

        log.info("[MyBatisConfig] 获取 Mapper 列表: nodeUrl={}, keyword={}", nodeUrl, keyword);
        return nodeMyBatisConfigService.getNodeMappers(nodeUrl, keyword);
    }

    /**
     * 获取 Mapper 详情
     *
     * @param nodeUrl    节点URL（Base64编码）
     * @param mapperName Mapper 全限定名
     * @return Mapper 详情
     */
    @GetMapping("/mapper/detail")
    @Operation(summary = "获取 Mapper 详情", description = "获取指定 Mapper 的详细信息，包含所有 MappedStatement")
    public ReturnResult<NodeMyBatisMapperDTO> getMapperDetailForMyBatis(
            @Parameter(description = "节点URL（Base64编码）", required = true)
            @RequestParam String nodeUrl,
            @Parameter(description = "Mapper 全限定名", required = true)
            @RequestParam String mapperName) {

        log.info("[MyBatisConfig] 获取 Mapper 详情: nodeUrl={}, mapperName={}", nodeUrl, mapperName);
        return nodeMyBatisConfigService.getMapperDetail(nodeUrl, mapperName);
    }

    /**
     * 刷新指定 XML 文件
     *
     * @param nodeUrl      节点URL（Base64编码）
     * @param resourcePath XML 资源路径
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新 XML 文件", description = "刷新指定的 MyBatis XML 映射文件")
    public ReturnResult<Boolean> refreshXmlForMyBatis(
            @Parameter(description = "节点URL（Base64编码）", required = true)
            @RequestParam String nodeUrl,
            @Parameter(description = "XML 资源路径", required = true)
            @RequestParam String resourcePath) {

        log.info("[MyBatisConfig] 刷新 XML: nodeUrl={}, resourcePath={}", nodeUrl, resourcePath);
        return nodeMyBatisConfigService.refreshXml(nodeUrl, resourcePath);
    }

    /**
     * 刷新所有 XML 文件
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 刷新结果
     */
    @PostMapping("/refresh/all")
    @Operation(summary = "刷新所有 XML 文件", description = "刷新节点上所有 MyBatis XML 映射文件")
    public ReturnResult<Map<String, Object>> refreshAllXmlForMyBatis(
            @Parameter(description = "节点URL（Base64编码）", required = true)
            @RequestParam String nodeUrl) {

        log.info("[MyBatisConfig] 刷新所有 XML: nodeUrl={}", nodeUrl);
        return nodeMyBatisConfigService.refreshAllXml(nodeUrl);
    }

    /**
     * 获取 MyBatis 统计信息
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取 MyBatis 统计信息", description = "获取节点 MyBatis 的统计信息")
    public ReturnResult<Map<String, Object>> getStatisticsForMyBatis(
            @Parameter(description = "节点URL（Base64编码）", required = true)
            @RequestParam String nodeUrl) {

        log.info("[MyBatisConfig] 获取统计信息: nodeUrl={}", nodeUrl);
        return nodeMyBatisConfigService.getStatistics(nodeUrl);
    }
}
