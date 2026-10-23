package com.chua.starter.face.reid.reid;

import com.chua.starter.face.reid.model.FaceRecord;
import com.chua.starter.face.reid.model.TrackSummary;
import com.chua.starter.face.reid.properties.FaceReidConfig;
import com.chua.starter.face.reid.spi.FaceBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FaceFeatureLibrary} 重识别语义测试。
 *
 * <p>用可控的合成特征向量（而非真实图片）驱动，聚焦验证 Re-ID 的判定规则：
 * 同人归并、异人不并、同帧不自并、容量淘汰、相似度排序、跨镜统计与阈值切换。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
class FaceFeatureLibraryTest {

    /**
     * 可控时钟。
     */
    private final AtomicLong now = new AtomicLong(1_000_000L);

    /**
     * 构造测试特征库。
     *
     * @param threshold 阈值
     * @return 特征库
     */
    private FaceFeatureLibrary library(double threshold) {
        FaceReidConfig config = new FaceReidConfig(true, "local", 4, 100, SimilarityMetric.COSINE,
                threshold, 10, false, 3600, 5, 0, 20);
        return new FaceFeatureLibrary(config, now::get);
    }

    /**
     * 构造归一化向量。
     *
     * @param values 分量
     * @return 归一化向量
     */
    private static float[] vec(double... values) {
        float[] raw = new float[values.length];
        double norm = 0;
        for (int i = 0; i < values.length; i++) {
            raw[i] = (float) values[i];
            norm += values[i] * values[i];
        }
        float inv = (float) (1.0 / Math.sqrt(norm));
        for (int i = 0; i < raw.length; i++) {
            raw[i] *= inv;
        }
        return raw;
    }

    /**
     * 构造检测框。
     *
     * @return 框
     */
    private static FaceBox box() {
        return new FaceBox(10, 10, 50, 50, 0.9, 0.8);
    }

    /**
     * 同一人（特征接近）再次出现应归并到同一轨迹，而不是新建人。
     */
    @Test
    @DisplayName("同人跨帧归并为同一轨迹且不新增主记录")
    void shouldMergeSamePersonAcrossFrames() {
        FaceFeatureLibrary library = library(0.90);
        float[] aliceA = vec(1, 0, 0, 0);
        float[] aliceB = vec(0.98, 0.05, 0.02, 0.01);

        FaceFeatureLibrary.EnrollResult first = library.enroll("cam-A", "frame-1", box(), 0.8, aliceA);
        now.addAndGet(1000);
        FaceFeatureLibrary.EnrollResult second = library.enroll("cam-B", "frame-2", box(), 0.8, aliceB);

        assertTrue(first.created(), "首次出现应新建轨迹");
        assertFalse(second.created(), "同一人再次出现不应新建轨迹");
        assertEquals(first.trackId(), second.trackId(), "应归并到同一轨迹");
        assertEquals(1, library.trackCount(), "应只有一个人");
        assertEquals(2, library.size(), "两次观测各留一条记录");
        assertEquals(1, second.record().observationCount(), "每条观测记录自身计数为 1");

        TrackSummary summary = library.track(first.trackId());
        assertNotNull(summary);
        assertEquals(2, summary.observations(), "轨迹累计观测次数应为 2");
        assertEquals(2, summary.recordCount(), "轨迹下应有 2 条记录");
        assertTrue(summary.crossCamera(), "两次出现在不同摄像头，应判定为跨镜");
        assertTrue(summary.cameraIds().containsAll(List.of("cam-A", "cam-B")), "应记录两个摄像头");
    }

    /**
     * 不同人（特征差异大）不应被误并。
     */
    @Test
    @DisplayName("异人不归并")
    void shouldSplitDifferentPeople() {
        FaceFeatureLibrary library = library(0.90);
        float[] alice = vec(1, 0, 0, 0);
        float[] bob = vec(0, 1, 0, 0);

        FaceFeatureLibrary.EnrollResult a = library.enroll("cam-A", "f1", box(), 0.8, alice);
        FaceFeatureLibrary.EnrollResult b = library.enroll("cam-A", "f2", box(), 0.8, bob);

        assertTrue(a.created());
        assertTrue(b.created(), "不同人应各自新建轨迹");
        assertNotEquals(a.trackId(), b.trackId());
        assertEquals(2, library.trackCount());
    }

    /**
     * 同一帧内出现两张相似的脸，不应视为同一人（防止一张合影被并成一个人）。
     */
    @Test
    @DisplayName("同帧不自并")
    void shouldNotMergeWithinSameFrame() {
        FaceFeatureLibrary library = library(0.99);
        float[] a = vec(1, 0, 0, 0);
        float[] b = vec(1, 0.001, 0, 0);

        FaceFeatureLibrary.EnrollResult first = library.enroll("cam-A", "same-frame", box(), 0.8, a);
        FaceFeatureLibrary.EnrollResult second = library.enroll("cam-A", "same-frame", box(), 0.8, b);

        assertTrue(first.created());
        assertTrue(second.created(), "同帧内的另一张脸应新建轨迹");
        assertEquals(2, library.trackCount());
    }

    /**
     * 超出容量时淘汰最久未命中的记录。
     */
    @Test
    @DisplayName("容量淘汰最旧记录")
    void shouldEvictOldestWhenOverCapacity() {
        FaceReidConfig config = new FaceReidConfig(true, "local", 4, 2, SimilarityMetric.COSINE,
                0.95, 10, false, 3600, 5, 0, 20);
        FaceFeatureLibrary library = new FaceFeatureLibrary(config, now::get);

        library.enroll("cam-A", "f1", box(), 0.8, vec(1, 0, 0, 0));
        now.addAndGet(10);
        library.enroll("cam-A", "f2", box(), 0.8, vec(0, 1, 0, 0));
        now.addAndGet(10);
        library.enroll("cam-A", "f3", box(), 0.8, vec(0, 0, 1, 0));

        assertEquals(2, library.size(), "容量应被限制为 2");
        assertEquals(2, library.trackCount(), "被淘汰人的轨迹也应移除");
    }

    /**
     * 检索按相似度降序返回，且 matched 标记正确。
     */
    @Test
    @DisplayName("1:N 检索按相似度排序并标记是否达阈值")
    void shouldSearchAndRank() {
        FaceFeatureLibrary library = library(0.90);
        library.enroll("cam-A", "f1", box(), 0.8, vec(1, 0, 0, 0));
        library.enroll("cam-A", "f2", box(), 0.8, vec(0.3, 0.95, 0, 0));
        library.enroll("cam-A", "f3", box(), 0.8, vec(0, 0, 1, 0));

        List<FaceFeatureLibrary.SearchHit> hits = library.search(vec(0.99, 0.05, 0, 0), 10, false);

        assertEquals(3, hits.size());
        assertTrue(hits.get(0).score() >= hits.get(1).score(), "应按得分降序");
        assertTrue(hits.get(0).matched(), "最佳候选应达阈值");
        assertFalse(hits.get(2).matched(), "最不相者不应达阈值");

        List<FaceFeatureLibrary.SearchHit> matchedOnly = library.search(vec(0.99, 0.05, 0, 0), 10, true);
        assertTrue(matchedOnly.stream().allMatch(FaceFeatureLibrary.SearchHit::matched));
        assertEquals(1, matchedOnly.size(), "只应返回最相似的那个人");
    }

    /**
     * 阈值调整即时生效：同一特征对在提高阈值后不再匹配。
     */
    @Test
    @DisplayName("阈值热切换影响判定")
    void shouldRespectThresholdChange() {
        FaceFeatureLibrary library = library(0.90);
        library.enroll("cam-A", "f1", box(), 0.8, vec(1, 0, 0, 0));

        // 与 vec(1,0,0,0) 的余弦相似度约为 0.80：低于 0.90，但高于 0.70
        float[] query = vec(8, 6, 0, 0);
        assertTrue(library.search(query, 10, true).isEmpty(), "0.90 阈值下应不命中");

        FaceReidConfig loose = new FaceReidConfig(true, "local", 4, 100, SimilarityMetric.COSINE,
                0.70, 10, false, 3600, 5, 0, 20);
        library.updateConfig(loose);
        assertEquals(1, library.search(query, 10, true).size(), "降到 0.70 后应命中");

        FaceReidConfig strict = new FaceReidConfig(true, "local", 4, 100, SimilarityMetric.COSINE,
                0.99, 10, false, 3600, 5, 0, 20);
        library.updateConfig(strict);
        assertTrue(library.search(query, 10, true).isEmpty(), "提高到 0.99 后应再次不命中");
    }

    /**
     * 轨迹列表与跨镜过滤。
     */
    @Test
    @DisplayName("轨迹列表支持跨镜过滤")
    void shouldListTracks() {
        FaceFeatureLibrary library = library(0.90);
        // 人 1：跨两个摄像头
        library.enroll("cam-A", "f1", box(), 0.8, vec(1, 0, 0, 0));
        now.addAndGet(10);
        library.enroll("cam-B", "f2", box(), 0.8, vec(1, 0.02, 0, 0));
        // 人 2：只在一个摄像头
        library.enroll("cam-A", "f3", box(), 0.8, vec(0, 1, 0, 0));

        List<TrackSummary> all = library.tracks(null, false, 10);
        assertEquals(2, all.size());

        List<TrackSummary> cross = library.tracks(null, true, 10);
        assertEquals(1, cross.size(), "只有一个人跨镜");
        assertEquals(2, cross.get(0).cameraIds().size());

        List<TrackSummary> camB = library.tracks("cam-B", false, 10);
        assertEquals(1, camB.size());
    }

    /**
     * 轨迹消失过久后不再归并（避免把隔天出现的人绑成同一人）。
     */
    @Test
    @DisplayName("轨迹超时后不归并")
    void shouldNotMergeAfterGap() {
        FaceReidConfig config = new FaceReidConfig(true, "local", 4, 100, SimilarityMetric.COSINE,
                0.90, 10, false, 10, 5, 0, 20);
        FaceFeatureLibrary library = new FaceFeatureLibrary(config, now::get);
        float[] same = vec(1, 0, 0, 0);

        FaceFeatureLibrary.EnrollResult first = library.enroll("cam-A", "f1", box(), 0.8, same);
        now.addAndGet(60_000);
        FaceFeatureLibrary.EnrollResult later = library.enroll("cam-A", "f2", box(), 0.8, same);

        assertTrue(first.created());
        assertTrue(later.created(), "超过 gap 后应视为新的出现，新建轨迹");
    }

    /**
     * 默认按轨迹去重（一个人只占一个候选位），includeMerged 时返回全部观测。
     */
    @Test
    @DisplayName("includeMerged 控制返回粒度")
    void shouldHideMergedByDefault() {
        FaceFeatureLibrary library = library(0.90);
        float[] same = vec(1, 0, 0, 0);
        library.enroll("cam-A", "f1", box(), 0.8, same);
        now.addAndGet(10);
        library.enroll("cam-B", "f2", box(), 0.8, same);
        assertEquals(2, library.size(), "两次观测两条记录");

        List<FaceFeatureLibrary.SearchHit> deduped = library.search(same, 10, false);
        assertEquals(1, deduped.size(), "默认按轨迹去重，一个人只占一个候选位");

        FaceReidConfig include = new FaceReidConfig(true, "local", 4, 100, SimilarityMetric.COSINE,
                0.90, 10, true, 3600, 5, 0, 20);
        library.updateConfig(include);
        assertEquals(2, library.search(same, 10, false).size(), "开启后返回全部观测");
    }

    /**
     * 空特征库检索安全返回空列表。
     */
    @Test
    @DisplayName("空库与非法输入安全")
    void shouldHandleEmptyLibrary() {
        FaceFeatureLibrary library = library(0.9);
        assertTrue(library.search(vec(1, 0), 10, false).isEmpty());
        assertTrue(library.tracks(null, false, 10).isEmpty());
        assertEquals(null, library.track("nope"));
        assertEquals(0, library.size());
        assertTrue(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> library.enroll("c", "s", box(), 0.5, new float[0])).getMessage().contains("特征"));
    }

    /**
     * 记录合并语义：保留轨迹归属，观测独立成条。
     */
    @Test
    @DisplayName("记录合并保留轨迹与主记录属性")
    void shouldKeepPrimaryFlagOnMerge() {
        FaceFeatureLibrary library = library(0.90);
        float[] same = vec(1, 0, 0, 0);
        FaceFeatureLibrary.EnrollResult first = library.enroll("cam-A", "f1", box(), 0.8, same);
        now.addAndGet(5);
        FaceFeatureLibrary.EnrollResult second = library.enroll("cam-B", "f2", box(), 0.7, same);

        FaceRecord record = second.record();
        assertTrue(record.primary(), "命中轨迹的观测仍记为主记录语义");
        assertEquals(first.trackId(), record.trackId());
        assertEquals("cam-B", record.cameraId(), "观测记录应保留自己的摄像头");
        assertEquals("f2", record.sourceId(), "观测记录应保留自己的来源帧");
        assertNotEquals(first.record().faceId(), record.faceId(), "每次观测应是独立记录");
        assertEquals(2, library.size(), "两条观测记录都应留存");
    }
}
