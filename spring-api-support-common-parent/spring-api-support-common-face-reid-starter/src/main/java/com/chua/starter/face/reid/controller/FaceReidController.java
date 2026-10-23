package com.chua.starter.face.reid.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.face.reid.properties.FaceReidConfig;
import com.chua.starter.face.reid.reid.FaceFeatureLibrary;
import com.chua.starter.face.reid.reid.FaceReidService;
import com.chua.starter.face.reid.setting.FaceReidSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 人脸重识别接口。
 *
 * <p>三组能力对应 Re-ID 的完整闭环：</p>
 * <ul>
 *   <li>{@code /register}、{@code /register/batch}：录入人脸并自动归并轨迹；</li>
 *   <li>{@code /identify}：以图搜人（1:N），返回命中的轨迹与跨镜证据；</li>
 *   <li>{@code /config}、{@code /stats}：重识别参数读写与运行态统计。</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0.43
 */
@RestController
@RequestMapping("/v2/face/reid")
@Tag(name = "Face Re-ID", description = "人脸重识别：跨摄像头/跨帧同一人脸追踪")
@RequiredArgsConstructor
public class FaceReidController {

    /**
     * 重识别服务。
     */
    private final FaceReidService faceReidService;

    /**
     * 配置服务。
     */
    private final FaceReidSettingService faceReidSettingService;

    /**
     * 注册单张图片。
     *
     * @param file     图片
     * @param cameraId 摄像头 ID
     * @param sourceId 来源图片 ID
     * @return 注册结果
     * @throws IOException 读取失败
     */
    @Operation(summary = "Register faces from an image (detect + embed + track merge)")
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReturnResult<FaceReidService.RegisterResult> register(
            @Parameter(description = "人脸图片") @RequestParam("file") MultipartFile file,
            @Parameter(description = "摄像头/来源标识") @RequestParam(required = false) String cameraId,
            @Parameter(description = "来源图片 ID") @RequestParam(required = false) String sourceId)
            throws IOException {
        if (file == null || file.isEmpty()) {
            return ReturnResult.error("图片不能为空");
        }
        return wrap(faceReidService.register(cameraId, sourceId, file.getBytes()));
    }

    /**
     * 批量注册：一组「摄像头 + 图片」上车，用于回放录像或批量导入。
     *
     * @param frames 帧列表
     * @return 每帧的注册结果
     */
    @Operation(summary = "Register faces from multiple frames in one call")
    @PostMapping(value = "/register/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReturnResult<List<FaceReidService.RegisterResult>> registerBatch(
            @Parameter(description = "帧：cameraId + 图片") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "摄像头，与 files 一一对应（可选）") @RequestParam(required = false) List<String> cameraIds)
            throws IOException {
        if (files == null || files.isEmpty()) {
            return ReturnResult.error("图片不能为空");
        }
        List<FaceReidService.RegisterResult> results = new java.util.ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String camera = cameraIds != null && cameraIds.size() > i ? cameraIds.get(i) : null;
            results.add(faceReidService.register(camera, null, file.getBytes()));
        }
        return ReturnResult.ok(results);
    }

    /**
     * 以图搜人。
     *
     * @param file        查询图片
     * @param topK        返回上限
     * @param matchedOnly 是否只返回达阈值结果
     * @return 识别结果
     * @throws IOException 读取失败
     */
    @Operation(summary = "Identify faces against the library (1:N Re-ID)")
    @PostMapping(value = "/identify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReturnResult<FaceReidService.IdentifyResult> identify(
            @Parameter(description = "查询图片") @RequestParam("file") MultipartFile file,
            @Parameter(description = "返回上限") @RequestParam(defaultValue = "0") int topK,
            @Parameter(description = "仅返回达阈值结果") @RequestParam(defaultValue = "false") boolean matchedOnly)
            throws IOException {
        if (file == null || file.isEmpty()) {
            return ReturnResult.error("图片不能为空");
        }
        return wrap(faceReidService.identify(file.getBytes(), topK, matchedOnly));
    }

    /**
     * 轨迹列表（谁的轨迹跨了几个摄像头）。
     *
     * @param cameraId  摄像头过滤
     * @param crossOnly 是否只看跨镜轨迹
     * @param limit     上限
     * @return 轨迹列表
     */
    @Operation(summary = "List person tracks (cross-camera identity)")
    @GetMapping("/tracks")
    public ReturnResult<List<com.chua.starter.face.reid.model.TrackSummary>> tracks(
            @RequestParam(required = false) String cameraId,
            @RequestParam(defaultValue = "false") boolean crossOnly,
            @RequestParam(defaultValue = "100") int limit) {
        return ReturnResult.ok(faceReidService.tracks(cameraId, crossOnly, limit));
    }

    /**
     * 单条轨迹详情。
     *
     * @param trackId 轨迹 ID
     * @return 轨迹
     */
    @Operation(summary = "Get track detail")
    @GetMapping("/tracks/{trackId}")
    public ReturnResult<com.chua.starter.face.reid.model.TrackSummary> track(@PathVariable String trackId) {
        com.chua.starter.face.reid.model.TrackSummary summary = faceReidService.track(trackId);
        return summary == null ? ReturnResult.error("轨迹不存在: " + trackId) : ReturnResult.ok(summary);
    }

    /**
     * 直接按特征检索（离线比对）。
     *
     * @param request 特征查询
     * @return 候选
     */
    @Operation(summary = "Search by raw embedding (offline matching)")
    @PostMapping("/search")
    public ReturnResult<List<FaceFeatureLibrary.SearchHit>> search(@RequestBody EmbeddingQuery request) {
        if (request == null || request.embedding() == null || request.embedding().isEmpty()) {
            return ReturnResult.error("特征向量不能为空");
        }
        float[] vector = new float[request.embedding().size()];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = request.embedding().get(i);
        }
        return ReturnResult.ok(faceReidService.search(vector, request.topK(), request.matchedOnly()));
    }

    /**
     * 当前生效的重识别配置。
     *
     * @return 配置及全部可调项说明
     */
    @Operation(summary = "Effective Re-ID config with editable descriptors")
    @GetMapping("/config")
    public ReturnResult<Map<String, Object>> config() {
        Map<String, Object> result = new LinkedHashMap<>();
        FaceReidConfig config = faceReidSettingService.config();
        result.put("config", FaceReidSettingService.toMap(config));
        result.put("descriptors", FaceReidSettingService.DESCRIPTORS);
        result.put("storage", faceReidSettingService.available() ? "db" : "application");
        return ReturnResult.ok(result);
    }

    /**
     * 更新重识别配置并热生效。
     *
     * @param updates 名称 → 值
     * @return 是否生效
     */
    @Operation(summary = "Update Re-ID config (hot reload)")
    @PostMapping("/config")
    public ReturnResult<Map<String, Object>> updateConfig(@RequestBody Map<String, String> updates) {
        boolean changed = faceReidSettingService.update(updates);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("changed", changed);
        result.put("config", FaceReidSettingService.toMap(faceReidSettingService.config()));
        return changed ? ReturnResult.ok(result) : ReturnResult.error("没有可更新的已知配置项", null, result);
    }

    /**
     * 恢复出厂配置。
     *
     * @return 是否成功
     */
    @Operation(summary = "Reset Re-ID config to factory defaults")
    @PostMapping("/config/reset")
    public ReturnResult<Boolean> resetConfig() {
        return ReturnResult.ok(faceReidSettingService.reset());
    }

    /**
     * 运行态统计。
     *
     * @return 统计
     */
    @Operation(summary = "Re-ID runtime statistics")
    @GetMapping("/stats")
    public ReturnResult<Map<String, Object>> stats() {
        return ReturnResult.ok(faceReidService.stats());
    }

    /**
     * 清空特征库。
     *
     * @return 清空的记录数
     */
    @Operation(summary = "Clear the face feature library")
    @PostMapping("/clear")
    public ReturnResult<Integer> clear() {
        return ReturnResult.ok(faceReidService.clear());
    }

    /**
     * 统一包装：模块不可用/检测失败时返回错误而不是空成功。
     *
     * @param value 注册结果
     * @param <T>   结果类型
     * @return 响应
     */
    @SuppressWarnings("unchecked")
    private <T> ReturnResult<T> wrap(FaceReidService.RegisterResult value) {
        return value.ok() ? ReturnResult.ok((T) value) : ReturnResult.error(value.reason());
    }

    /**
     * 统一包装（识别结果）。
     *
     * @param value 识别结果
     * @param <T>   结果类型
     * @return 响应
     */
    @SuppressWarnings("unchecked")
    private <T> ReturnResult<T> wrap(FaceReidService.IdentifyResult value) {
        return value.ok() ? ReturnResult.ok((T) value) : ReturnResult.error(value.reason());
    }

    /**
     * 特征检索请求。
     *
     * @param embedding   特征向量
     * @param topK        返回上限
     * @param matchedOnly 仅返回达阈值结果
     * @author CH
     * @since 4.0.0.43
     */
    public record EmbeddingQuery(List<Float> embedding, int topK, boolean matchedOnly) {
    }
}
