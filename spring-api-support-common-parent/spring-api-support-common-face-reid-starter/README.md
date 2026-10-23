# 人脸重识别（Face Re-ID）Starter

跨摄像头 / 跨帧识别**同一个人**的能力模块：提取人脸特征、建立特征库、按相似度检索并聚类成轨迹（track）。

> 与「人脸识别」的区别：识别通常只回答「这张脸是谁」（1:1 或核对），
> Re-ID 回答的是「这个人在别的摄像头里出现过吗、属于哪条轨迹」（1:N 检索 + 跨镜聚类）。

---

## 一、快速开始

```xml
<dependency>
  <groupId>com.chua</groupId>
  <artifactId>spring-api-support-common-face-reid-starter</artifactId>
</dependency>
```

引入即装配，无需任何数据库或额外依赖。

```yaml
spring:
  face:
    reid:
      enabled: true          # 总开关
      engine: local          # 引擎：local（内置）或自定义引擎名
      threshold: 0.995       # 同人判定阈值（见「阈值标定」）
      top_k: 10              # 1:N 检索返回条数
      max_faces: 20000       # 特征库容量
```

---

## 二、配置项

配置分两层，**数据库优先**：

1. 出厂值：`application.yml` 的 `spring.face.reid.*`
2. 运行期覆盖：`sys_setting`（group=`faceReid`）

首次启动用出厂值播种，之后以数据库为准——运维在管理端改阈值即时生效，无需重启。
宿主没有配置服务时自动退回 yml 配置，识别功能不受影响。

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `enabled` | `true` | 模块总开关 |
| `engine` | `local` | 引擎名，对应 SPI 实现 |
| `dimension` | `0` | 特征维度，0 表示由引擎决定 |
| `max_faces` | `20000` | 特征库容量，超出按 LRU 淘汰 |
| `metric` | `cosine` | 相似度度量：`cosine` / `euclidean` |
| `threshold` | `0.58` | 同人判定阈值，**必须按引擎标定** |
| `top_k` | `10` | 检索返回条数 |
| `include_merged` | `false` | 检索是否返回同一轨迹的全部观测 |
| `track_gap_seconds` | `30` | 超过该间隔不再并入同一轨迹 |
| `track_max_misses` | `5` | 轨迹连续未命中的淘汰阈值 |
| `track_ttl_seconds` | `0` | 轨迹保留时长，0 表示永久 |
| `max_faces_per_image` | `20` | 单图最多处理人脸数 |
| `max_image_pixels` | `20000000` | 超大图自动缩放上限 |
| `default_camera_id` | - | 未指定摄像头时的默认值 |

### 阈值标定（重要）

阈值与引擎强相关，**不能一套数字通用**：

| 引擎 | 同人得分 | 不同人得分 | 建议阈值 |
|---|---|---|---|
| 深度模型（ArcFace/FaceNet，经 SPI 接入） | ≥ 0.70 | ≤ 0.45 | 0.55 ~ 0.60 |
| 内置 `local` 引擎 | ≈ 0.999+ | 0.97 ~ 0.99 | **0.995 以上** |

接入新引擎后，请先用真实样本跑 `/v2/face/reid/search`，观察同人/不同人的得分分布再定值。

---

## 三、HTTP 接口

统一前缀 `/v2/face/reid`。

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/register` | 上传图片，检测人脸并入库（跨帧同人自动归并） |
| POST | `/register/batch` | 批量注册 |
| POST | `/identify` | 以图搜人：返回图中每张脸的候选轨迹 |
| POST | `/search` | 直接用特征向量检索 |
| GET | `/tracks` | 轨迹列表（可通过 `crossOnly` 只看跨镜轨迹） |
| GET | `/tracks/{trackId}` | 轨迹详情与全部观测 |
| GET | `/config` | 当前生效配置（含表单元数据） |
| POST | `/config` | 更新配置（热生效） |
| POST | `/config/reset` | 重置为出厂配置 |
| GET | `/stats` | 运行态统计 |
| POST | `/clear` | 清空特征库 |

### 典型链路

```bash
# 1. 摄像头 A 拍到某人
curl -F "file=@a.jpg" -F "cameraId=cam-A" -F "sourceId=frame-001" \
     http://localhost:8080/v2/face/reid/register

# 2. 摄像头 B 又拍到同一人 —— 自动并入同一条轨迹
curl -F "file=@b.jpg" -F "cameraId=cam-B" -F "sourceId=frame-002" \
     http://localhost:8080/v2/face/reid/register

# 3. 查看跨镜轨迹
curl "http://localhost:8080/v2/face/reid/tracks?crossOnly=true"

# 4. 新图查人
curl -F "file=@c.jpg" http://localhost:8080/v2/face/reid/identify
```

---

## 四、架构

```
HTTP Controller
      ↓
FaceReidService ──── FaceReidSettingService（配置：yml + sys_setting 热加载）
      ↓                      ↓
FaceDetector (SPI)     FaceReidSettingStore
  ├─ LocalFaceDetector（内置）   ├─ SysSettingStore（存在配置服务时启用）
  └─ 你的实现                    └─ Noop（降级）
      ↓
FaceFeatureLibrary（特征库 + 轨迹聚类 + 淘汰）
```

### 核心语义

- **一条轨迹 = 一个人**。每次观测是一条**独立记录**，携带**本次**的摄像头/来源帧/检测框，共享 `trackId`。这样才能回答「这个人被哪些摄像头在何时拍到」。
- **同帧不去重**。同一张图里的不同人脸永远不会被合并成同一轨迹（`blockedTracks` 保证），否则合影会被认成一个人。
- **检索按轨迹去重**。默认同一轨迹只返回最高分的一条，避免一个人出现 20 次就占满 `topK`。

---

## 五、接入自己的引擎

实现 `FaceDetector` 并注册为 Bean，即自动替换内置引擎：

```java
@Bean
public FaceDetector arcFaceDetector() {
    return new ArcFaceDetector();   // 实现 FaceDetector
}
```

```java
public interface FaceDetector {
    String name();                                  // 引擎名，与配置 engine 对应
    boolean supports(String engine);
    boolean ready();
    int dimension();                                // 特征维度
    DetectionResult detect(byte[] imageBytes, String sourceName);
}
```

约定：特征须 **L2 归一化**；`DetectionResult.reason()` 只在失败时非空。
检索、聚类、轨迹、配置等全部上层逻辑无需改动。

---

## 六、内置 local 引擎的定位

`LocalFaceDetector` 是**纯 Java 零依赖的参考实现**：YCbCr 肤色分割 + 连通域检测，
8×8 网格的「颜色矩 + 梯度方向直方图」描述子（896 维，颜色块与梯度块各自归一化）。

**它能做**：验证整条链路、离线演示、无依赖环境兜底。
**它不能做**：生产级精度的身份判别——它不认识五官语义，只统计外观分布，
同人/不同人的得分区间很窄（0.97~0.999+），必须配合 `0.995` 这类高阈值使用，
换角度、光照后的鲁棒性远不如深度模型。

**生产环境请通过 SPI 接入 ArcFace / FaceNet 等深度模型**，上层代码无需改动。

---

## 七、测试

```bash
mvn -o -pl spring-api-support-common-parent/spring-api-support-common-face-reid-starter test
```

覆盖（42 个用例，全部通过）：

- `LocalFaceDetectorTest` — 检测、归一化、确定性、空图、非法输入、降采样
- `FaceFeatureLibraryTest` — 跨帧归并、同帧不合并、阈值热切换、轨迹统计、容量淘汰
- `FaceReidServiceTest` — **端到端**：同人跨摄像头合并为一条轨迹、不同人分离、以图搜人、陌生人不误认、引擎降级
- `FaceReidSettingServiceTest` — 播种、不覆盖已有值、热更新、非法值忽略、重置、无存储降级
- `FaceReidAutoConfigurationTest` — 自动装配、开关关闭、未知引擎降级、配置绑定
