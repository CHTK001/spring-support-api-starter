# spring-api-support-geo-starter

GeoD 地理空间数据下载服务 — Spring Boot 3 实现版本。

## 项目简介

本模块是 [geo-downloader](https://github.com/gaopengbin/geo-downloader) 的 Spring Boot 服务端替代方案，提供瓦片下载、GeoTIFF 导出、DEM 高程模型、3D Tiles、Overpass OSM 矢量等地理空间数据处理能力。

**原项目**: https://github.com/gaopengbin/geo-downloader (Rust + Tauri)  
**本项目**: `spring-api-support-geo-starter` (Spring Boot 3 + Java 25)

## 技术栈

- **Java 25** + **Spring Boot 3.5.9**
- **Virtual Threads** — 瓦片并发下载
- **Caffeine** — 瓦片本地缓存
- **Jackson** — JSON 解析（Wayback / 3D Tiles）
- **Spring Validation** — 请求参数校验
- **OpenAPI / Swagger** — API 文档

## 功能清单

| 功能模块 | 接口路径 | 说明 |
|---|---|---|
| 影像瓦片下载 | `POST /v1/geo/tile/download` | 并发下载瓦片，导出 GeoTIFF/PNG/JPEG |
| 瓦片估算 | `POST /v1/geo/tile/estimate` | 估算瓦片数量和大小 |
| 图源列表 | `GET /v1/geo/tile/sources` | 内置 12 个图源（Google/OSM/天地图/高德/Esri/Carto） |
| 任务管理 | `GET/POST/DELETE /v1/geo/tile/task/*` | 创建/查询/取消/删除下载任务 |
| DEM 高程模型 | `POST /v1/geo/dem/download` | Terrarium 编码 PNG → GeoTIFF 高程文件 |
| DEM 数据源 | `GET /v1/geo/dem/sources` | AWS Terrain (Terrarium) 全球免费 DEM |
| 3D Tiles | `POST /v1/geo/3dtiles/download` | Cesium Ion / 自定义 URL，递归下载，URI 本地化 |
| 3D Tiles 分析 | `POST /v1/geo/3dtiles/analyze` | 解析 tileset.json，返回节点数/层级/覆盖范围 |
| Overpass OSM | `POST /v1/geo/osm/query` | 执行 Overpass API 查询，返回元素统计 |
| Wayback 历史影像 | `GET /v1/geo/wayback/versions` | Esri World Imagery 历史版本列表 |
| 全局设置 | `GET/POST /v1/geo/settings` | Token/代理/并发/缓存等配置读写 |
| 下载历史 | `GET/DELETE /v1/geo/history` | JSON 文件持久化，分页查询 |

## 内置图源

| ID | 名称 | 最大缩放 | 版权 |
|---|---|---|---|
| `google_satellite` | Google 卫星 | z20 | © Google |
| `google_map` | Google 地图 | z20 | © Google |
| `google_hybrid` | Google 混合 | z20 | © Google |
| `osm` | OpenStreetMap | z19 | © OSM contributors |
| `arcgis_satellite` | ArcGIS 卫星 | z19 | © Esri |
| `arcgis_world` | ArcGIS 世界 | z19 | © Esri |
| `carto_light` | Carto Light | z19 | © CARTO |
| `carto_dark` | Carto Dark | z19 | © CARTO |
| `gaode_satellite` | 高德卫星 | z18 | © 高德 |
| `gaode_map` | 高德地图 | z18 | © 高德 |
| `esri_world` | Esri World Imagery | z19 | © Esri |
| `open_topo` | OpenTopoMap | z17 | © OpenTopoMap |
| `tianditu_img` | 天地图影像 | z18 | © 天地图（需 Token） |

## API 快速开始

### 1. 启动服务

```bash
cd spring-api-support-geo-starter
mvn spring-boot:run
```

服务默认监听 `http://localhost:18171/geo/api`

### 2. Swagger 文档

浏览器访问：`http://localhost:18171/geo/api/swagger-ui.html`

### 3. 示例请求

**获取图源列表：**
```bash
curl http://localhost:18171/geo/api/v1/geo/tile/sources
```

**估算瓦片数量：**
```bash
curl -X POST http://localhost:18171/geo/api/v1/geo/tile/estimate \
  -H "Content-Type: application/json" \
  -d '{
    "bounds": {"north": 39.95, "south": 39.85, "east": 116.45, "west": 116.35},
    "zoom": 15,
    "source": "google_satellite",
    "format": "geotiff"
  }'
```

**创建瓦片下载任务：**
```bash
curl -X POST http://localhost:18171/geo/api/v1/geo/tile/download \
  -H "Content-Type: application/json" \
  -d '{
    "bounds": {"north": 39.95, "south": 39.85, "east": 116.45, "west": 116.35},
    "zoom": 15,
    "source": "osm",
    "format": "png",
    "savePath": "D:/downloads",
    "sourceName": "北京_朝阳区_测试"
  }'
```

**查询 DEM 数据源：**
```bash
curl http://localhost:18171/geo/api/v1/geo/dem/sources
```

**查询 Wayback 版本：**
```bash
curl http://localhost:18171/geo/api/v1/geo/wayback/versions
```

## 配置说明

`src/main/resources/config/application-geo.yml`：

```yaml
geo:
  tile:
    size: 256              # 瓦片像素大小
    max-concurrency: 50    # 最大并发下载数
    retry-times: 2         # 重试次数
    timeout-seconds: 15    # 请求超时
    cache-enabled: true    # 启用瓦片缓存
    cache-max-size-mb: 2048 # 缓存容量上限（MB）
  export:
    compression: lzw       # TIFF 压缩：none/lzw/deflate
    build-pyramid: false   # 构建金字塔（需 GDAL）
    generate-sidecars: true # 生成 .prj 侧面文件
  proxy:
    enabled: false         # 启用代理
    url: ""                # 代理地址，如 http://127.0.0.1:7890
  allow-invalid-certs: false # 允许无效 HTTPS 证书
  history:
    dir: ""                # 历史记录文件目录（默认 ~/.geo-downloader/）
```

## 模块依赖

```xml
<!-- Web 服务 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- 参数校验 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- 本地缓存 -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.8</version>
</dependency>

<!-- 内部依赖 -->
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>utils-support-common-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>utils-support-image-starter</artifactId>
</dependency>
```

## 与原生项目的对比

| 特性 | geo-downloader (Rust) | geo-starter (Java) |
|---|---|---|
| 运行时 | Tauri 桌面应用 | Spring Boot HTTP 服务 |
| 并发模型 | tokio async/await | Virtual Threads |
| 图片处理 | image crate | javax.imageio |
| GeoTIFF | tiff crate（含 BigTIFF） | ImageIO + .prj 侧面文件 |
| 3D Tiles | serde_json 递归解析 | Jackson JsonNode 递归解析 |
| DEM | Terrarium 解码 + 拼接 | 同左（TerrariumDecoder） |
| 历史影像 | Esri Wayback API | 同左（HttpClient） |
| 矢量数据 | Overpass API | 同左（HttpClient） |
| 数据存储 | SQLite | JSON 文件 |
| 部署方式 | 桌面安装包 | jar 包 / Docker |

## 许可证

MIT License（与原项目一致）

## 参考

- 原项目：https://github.com/gaopengbin/geo-downloader
- 官网：https://geodownloader.pages.dev
- GeoTIFF 规范：https://gdal.org/enums.html#rasterformat_geotiff
- Overpass API：https://dev.overpass-api.de/
- Cesium Ion：https://cesium.com/platform/cesium-ion/
- Esri Wayback：https://opendata.arcgis.com/
