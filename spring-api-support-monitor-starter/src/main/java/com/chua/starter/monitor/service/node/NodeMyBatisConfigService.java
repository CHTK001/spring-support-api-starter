package com.chua.starter.monitor.service.node;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.node.NodeMyBatisMapperDTO;

import java.util.List;
import java.util.Map;

/**
 * 节点 MyBatis 配置服务接口
 * <p>
 * 通过 SyncClient 与远程节点通信，管理 MyBatis 配置
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
public interface NodeMyBatisConfigService {

    /**
     * 获取节点的所有 Mapper 列表
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @param keyword 过滤关键词（可选）
     * @return Mapper 列表
     */
    ReturnResult<List<NodeMyBatisMapperDTO>> getNodeMappers(String nodeUrl, String keyword);

    /**
     * 获取 Mapper 详情
     *
     * @param nodeUrl    节点URL（Base64编码）
     * @param mapperName Mapper 全限定名
     * @return Mapper 详情
     */
    ReturnResult<NodeMyBatisMapperDTO> getMapperDetail(String nodeUrl, String mapperName);

    /**
     * 刷新指定 XML 文件
     *
     * @param nodeUrl      节点URL（Base64编码）
     * @param resourcePath XML 资源路径
     * @return 刷新结果
     */
    ReturnResult<Boolean> refreshXml(String nodeUrl, String resourcePath);

    /**
     * 刷新所有 XML 文件
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 刷新结果
     */
    ReturnResult<Map<String, Object>> refreshAllXml(String nodeUrl);

    /**
     * 获取 MyBatis 统计信息
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getStatistics(String nodeUrl);
}
