package com.chua.starter.face.reid.reid;

import com.chua.starter.face.reid.model.FaceRecord;
import com.chua.starter.face.reid.model.TrackSummary;
import com.chua.starter.face.reid.properties.FaceReidConfig;
import com.chua.starter.face.reid.spi.FaceBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.LongSupplier;

/**
 * 人脸特征库：Re-ID 的记忆部分。
 *
 * <p>本类只做「存特征 + 找相似 + 维护轨迹」，不关心图片从哪来、引擎是什么。
 * 它实现的是重识别里真正难的那一半：<b>把同一张脸在不同摄像头、不同帧的多次观测，
 * 归并成同一条轨迹（track）</b>。</p>
 *
 * <h3>为什么用线性扫描而不是 ANN 索引</h3>
 * <p>人脸务日通常是「万级」，128~512 维 float 全量内积是几百微秒量级，远小于一次
 * 图像解码/推理的开销；引入 HNSW/IVF 会增加依赖与调参负担，且在小库上 recall 反而
 * 不稳定。因此这里用扁平数组（缓存友好）+ 全量打分，并在 {@code maxFaces} 处做
 * 容量淘汰。若将来需要百万级，可另行实现本类的检索路径（{@link #search} 是唯一入口）。</p>
 *
 * <h3>并发模型</h3>
 * <p>读写锁分离：检索走读锁可并发，入库/淘汰走写锁。入库是写少读多的场景，
 * 这样检索不会被入库阻塞得太久。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
public class FaceFeatureLibrary {

    /**
     * 当前配置。
     */
    private volatile FaceReidConfig config;

    /**
     * 人脸 ID → 记录。
     */
    private final Map<String, FaceRecord> faces = new LinkedHashMap<>();

    /**
     * 轨迹 ID → 该轨迹的活跃状态。
     */
    private final Map<String, TrackState> tracks = new HashMap<>();

    /**
     * 读写锁。
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 时钟（便于测试注入）。
     */
    private final LongSupplier clock;

    /**
     * 构造特征库。
     *
     * @param config 初始配置
     */
    public FaceFeatureLibrary(FaceReidConfig config) {
        this(config, System::currentTimeMillis);
    }

    /**
     * 构造特征库（可注入时钟）。
     *
     * @param config 初始配置
     * @param clock  毫秒时钟
     */
    public FaceFeatureLibrary(FaceReidConfig config, LongSupplier clock) {
        this.config = config == null ? new FaceReidConfig(true, "local", FaceReidConfig.FALLBACK_DIMENSION,
                20000, SimilarityMetric.COSINE, 0.58, 10, false, 30, 5, 0, 20).normalized() : config.normalized();
        this.clock = clock == null ? System::currentTimeMillis : clock;
    }

    /**
     * 当前配置。
     *
     * @return 配置
     */
    public FaceReidConfig config() {
        return config;
    }

    /**
     * 热切换配置；容量下调时立即淘汰超额部分。
     *
     * @param next 新配置
     */
    public void updateConfig(FaceReidConfig next) {
        if (next == null) {
            return;
        }
        FaceReidConfig normalized = next.normalized();
        lock.writeLock().lock();
        try {
            this.config = normalized;
            evictIfNeeded(normalized);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 特征库规模。
     *
     * @return 人脸记录条数
     */
    public int size() {
        lock.readLock().lock();
        try {
            return faces.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 轨迹数量（≈ 不同的人）。
     *
     * @return 轨迹数
     */
    public int trackCount() {
        lock.readLock().lock();
        try {
            return tracks.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 读取单条记录。
     *
     * @param faceId 人脸记录 ID
     * @return 记录，不存在返回 null
     */
    public FaceRecord get(String faceId) {
        lock.readLock().lock();
        try {
            return faces.get(faceId);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 清空特征库。
     *
     * @return 清空的记录数
     */
    public int clear() {
        lock.writeLock().lock();
        try {
            int removed = faces.size();
            faces.clear();
            tracks.clear();
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 入库一张人脸，并完成 Re-ID 归并。
     *
     * <p>流程：</p>
     * <ol>
     *   <li>与主记录集合做 1:N 相似度检索；</li>
     *   <li>若最高分 ≥ 阈值，且该候选所在的轨迹<b>尚未出现在同一来源帧</b>（避免同帧重复计数），
     *       则视为同一人：把本次观测并入该候选轨迹，不新建主记录；</li>
     *   <li>否则新建轨迹与主记录。</li>
     * </ol>
     *
     * @param cameraId  摄像头 ID
     * @param sourceId  来源图片/帧 ID
     * @param box       检测框
     * @param quality   入库质量
     * @param embedding 归一化特征
     * @return 入库结果
     */
    public EnrollResult enroll(String cameraId, String sourceId, FaceBox box, double quality, float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            throw new IllegalArgumentException("特征向量不能为空");
        }
        lock.writeLock().lock();
        try {
            FaceReidConfig cfg = this.config;
            long now = clock.getAsLong();
            FaceRecord best = findMergeTarget(cfg, cameraId, sourceId, embedding, now);
            if (best != null) {
                // 每次观测独立成一条记录（带本帧的摄像头/来源/框），共享轨迹 ID，跨镜证据才不会丢
                FaceRecord observation = best.observe(newFaceId(), cameraId, sourceId, now,
                        box, quality, embedding);
                faces.put(observation.faceId(), observation);
                TrackState state = tracks.get(best.trackId());
                if (state == null) {
                    state = new TrackState(best.trackId(), now);
                    tracks.put(best.trackId(), state);
                }
                state.lastSeen = now;
                state.misses = 0;
                evictIfNeeded(cfg);
                return new EnrollResult(observation, false, best.faceId());
            }
            String trackId = newTrackId();
            FaceRecord created = new FaceRecord(newFaceId(), trackId, true, cameraId, sourceId,
                    now, now, 1, box, quality, embedding);
            faces.put(created.faceId(), created);
            tracks.put(trackId, new TrackState(trackId, now));
            evictIfNeeded(cfg);
            return new EnrollResult(created, true, null);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 生成人脸记录 ID。
     *
     * @return ID
     */
    private static String newFaceId() {
        return "F" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 生成轨迹 ID。
     *
     * @return ID
     */
    private static String newTrackId() {
        return "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 查找可归并的既有轨迹。
     *
     * @param cfg       配置
     * @param cameraId  摄像头
     * @param sourceId  来源帧
     * @param embedding 特征
     * @param now       当前时间
     * @return 最佳主记录；无可归并项返回 null
     */
    private FaceRecord findMergeTarget(FaceReidConfig cfg, String cameraId, String sourceId,
                                       float[] embedding, long now) {
        double threshold = cfg.threshold();
        long gapMs = cfg.trackGapSeconds() * 1000L;
        // 同一帧里已经出现过的轨迹：该帧中的另一张脸即便特征接近，也不可能是同一个人
        Set<String> blockedTracks = new LinkedHashSet<>();
        for (FaceRecord record : faces.values()) {
            if (sourceId != null && sourceId.equals(record.sourceId())) {
                blockedTracks.add(record.trackId());
            }
        }
        FaceRecord best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (FaceRecord record : faces.values()) {
            if (blockedTracks.contains(record.trackId())) {
                continue;
            }
            // 轨迹消失过久则不再归并，交给新轨迹（避免把隔天出现的人绑在一起）
            if (gapMs > 0 && now - record.lastSeen() > gapMs) {
                continue;
            }
            double score = cfg.metric().score(embedding, record.embedding());
            if (score > bestScore) {
                bestScore = score;
                best = record;
            }
        }
        return bestScore >= threshold ? best : null;
    }

    /**
     * 1:N 检索：找出与给定特征最相似的人脸记录。
     *
     * <p><b>去重语义</b>：Re-ID 要回答的是「这个人是库里的哪一个人」，
     * 而不是「匹配了哪几条观测」。因此默认<b>每条轨迹只保留最高分的一条</b>——
     * 否则一个人出现 20 次就会把 topK 全部占满，导致真正要找的另一个人挤不进去。
     * 需要看全部观测时把 {@code includeMerged} 置为 true。</p>
     *
     * <p>返回按得分降序的候选，是否达到阈值由 {@link SearchHit#matched()} 标示——
     * 未达阈值的候选也返回，方便人工复核（阈值调参需要看到「差一点」的样本）。</p>
     *
     * @param embedding 查询特征
     * @param limit     返回上限，≤0 时用配置 topK
     * @param matchedOnly 是否只返回达阈值的结果
     * @return 候选列表（降序）
     */
    public List<SearchHit> search(float[] embedding, int limit, boolean matchedOnly) {
        if (embedding == null || embedding.length == 0) {
            return List.of();
        }
        lock.readLock().lock();
        try {
            FaceReidConfig cfg = this.config;
            int top = limit <= 0 ? cfg.topK() : limit;
            double threshold = cfg.threshold();
            List<SearchHit> hits = new ArrayList<>();
            // 默认按轨迹去重：同一轨迹只保留最高分观测，避免一个人占满 topK
            Map<String, SearchHit> bestPerTrack = cfg.includeMerged() ? null : new LinkedHashMap<>();
            for (FaceRecord record : faces.values()) {
                double score = cfg.metric().score(embedding, record.embedding());
                boolean matched = score >= threshold;
                if (matchedOnly && !matched) {
                    continue;
                }
                SearchHit hit = new SearchHit(record, score, matched);
                if (bestPerTrack == null) {
                    hits.add(hit);
                } else {
                    bestPerTrack.merge(record.trackId(), hit,
                            (old, candidate) -> candidate.score() > old.score() ? candidate : old);
                }
            }
            if (bestPerTrack != null) {
                hits.addAll(bestPerTrack.values());
            }
            hits.sort(Comparator.comparingDouble(SearchHit::score).reversed());
            return hits.size() > top ? new ArrayList<>(hits.subList(0, top)) : hits;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 按轨迹聚合查询。
     *
     * @param trackId 轨迹 ID
     * @return 轨迹摘要，不存在返回 null
     */
    public TrackSummary track(String trackId) {
        if (trackId == null) {
            return null;
        }
        lock.readLock().lock();
        try {
            List<FaceRecord> records = new ArrayList<>();
            for (FaceRecord record : faces.values()) {
                if (trackId.equals(record.trackId())) {
                    records.add(record);
                }
            }
            return FaceRecord.summarize(records);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 全部轨迹摘要（按最近出现时间降序）。
     *
     * @param cameraId   仅看某摄像头（null 表示不限）
     * @param crossOnly  是否只返回跨镜轨迹
     * @param limit      上限，≤0 表示不限
     * @return 轨迹列表
     */
    public List<TrackSummary> tracks(String cameraId, boolean crossOnly, int limit) {
        lock.readLock().lock();
        try {
            Map<String, List<FaceRecord>> grouped = new LinkedHashMap<>();
            for (FaceRecord record : faces.values()) {
                if (cameraId != null && !cameraId.isBlank() && !cameraId.equals(record.cameraId())) {
                    continue;
                }
                grouped.computeIfAbsent(record.trackId(), k -> new ArrayList<>()).add(record);
            }
            List<TrackSummary> summaries = new ArrayList<>();
            for (List<FaceRecord> records : grouped.values()) {
                TrackSummary summary = FaceRecord.summarize(records);
                if (summary != null && (!crossOnly || summary.crossCamera())) {
                    summaries.add(summary);
                }
            }
            summaries.sort(Comparator.comparingLong(TrackSummary::lastSeen).reversed());
            return limit > 0 && summaries.size() > limit
                    ? new ArrayList<>(summaries.subList(0, limit)) : summaries;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 查询某摄像头出现过的轨迹 ID 集合。
     *
     * @param cameraId 摄像头
     * @return 轨迹 ID
     */
    public Set<String> trackIdsOfCamera(String cameraId) {
        lock.readLock().lock();
        try {
            Set<String> ids = new LinkedHashSet<>();
            for (FaceRecord record : faces.values()) {
                if (Objects.equals(cameraId, record.cameraId())) {
                    ids.add(record.trackId());
                }
            }
            return ids;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 容量淘汰：超出 {@code maxFaces} 时，优先淘汰最久未命中的非主记录，
     * 其次按 {@code lastSeen} 从旧到新淘汰主记录。
     *
     * @param cfg 配置
     */
    private void evictIfNeeded(FaceReidConfig cfg) {
        int overflow = faces.size() - cfg.maxFaces();
        if (overflow <= 0) {
            return;
        }
        List<FaceRecord> ordered = new ArrayList<>(faces.values());
        ordered.sort(Comparator
                .comparing(FaceRecord::primary)
                .reversed()
                .thenComparingLong(FaceRecord::lastSeen));
        for (int i = 0; i < overflow && i < ordered.size(); i++) {
            FaceRecord victim = ordered.get(i);
            faces.remove(victim.faceId());
            if (victim.primary() && !faces.values().stream().anyMatch(r -> victim.trackId().equals(r.trackId()))) {
                tracks.remove(victim.trackId());
            }
        }
    }

    /**
     * 减少轨迹活跃计数；连续丢失超限或超过 TTL 时结案。
     *
     * @param trackId 轨迹
     * @param now     当前时间
     * @return 是否已结案
     */
    public boolean tickTrack(String trackId, long now) {
        lock.writeLock().lock();
        try {
            TrackState state = tracks.get(trackId);
            if (state == null) {
                return true;
            }
            state.misses++;
            FaceReidConfig cfg = this.config;
            boolean closed = state.misses >= cfg.trackMaxMisses()
                    || (cfg.trackTtlSeconds() > 0 && now - state.lastSeen > cfg.trackTtlSeconds() * 1000L);
            if (closed) {
                tracks.remove(trackId);
            }
            return closed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 轨迹活跃状态。
     */
    private static final class TrackState {

        /**
         * 轨迹 ID。
         */
        private final String trackId;

        /**
         * 最近命中时间。
         */
        private long lastSeen;

        /**
         * 连续丢失次数。
         */
        private int misses;

        private TrackState(String trackId, long lastSeen) {
            this.trackId = trackId;
            this.lastSeen = lastSeen;
        }
    }

    /**
     * 入库结果。
     *
     * @param record   本次观测落库后的记录
     * @param created  是否新建轨迹（true=新人，false=命中已有轨迹）
     * @param mergedInto 命中的主记录 faceId（新建时为 null）
     * @author CH
     * @since 4.0.0.43
     */
    public record EnrollResult(FaceRecord record, boolean created, String mergedInto) {

        /**
         * 轨迹 ID。
         *
         * @return 轨迹 ID
         */
        public String trackId() {
            return record.trackId();
        }
    }

    /**
     * 检索命中项。
     *
     * @param record  命中的记录
     * @param score   相似度得分
     * @param matched 是否达到阈值
     * @author CH
     * @since 4.0.0.43
     */
    public record SearchHit(FaceRecord record, double score, boolean matched) {
    }
}
