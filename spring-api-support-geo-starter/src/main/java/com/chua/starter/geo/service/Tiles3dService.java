package com.chua.starter.geo.service;

import com.chua.starter.geo.model.TilesetSummary;
import com.chua.starter.geo.model.Tiles3dSource;
import java.util.List;

/**
 * 3D Tiles 下载服务，支持 Cesium Ion 和自定义 URL 数据源。
 *
 * @author CH
 * @since 1.0.0
 */
public interface Tiles3dService {

    /**
     * 解析 tileset.json 并返回摘要信息。
     *
     * @param source 数据源配置
     * @param proxy  代理地址（可为 null）
     * @return tileset 摘要
     * @throws Exception 解析失败时抛出
     */
    TilesetSummary analyze(Tiles3dSource source, String proxy) throws Exception;

    /**
     * 下载 3D Tiles 资产到指定目录。
     *
     * @param source    数据源配置
     * @param polygon   选区多边形（可为 null，null 表示下载全部）
     * @param proxy     代理地址（可为 null）
     * @param savePath  保存根目录
     * @return 下载结果
     * @throws Exception 下载失败时抛出
     */
    Download3dResult download(Tiles3dSource source, List<List<Double>> polygon,
                               String proxy, String savePath) throws Exception;

    /**
     * 3D Tiles 下载结果。
     *
     * @param savePath        保存根目录
     * @param tilesetJsonPath tileset.json 路径（URI 已本地化）
     * @param contentCount    下载的内容文件数
     * @param totalTiles      tileset 总节点数
     */
    record Download3dResult(String savePath, String tilesetJsonPath, int contentCount, int totalTiles) {}
}
