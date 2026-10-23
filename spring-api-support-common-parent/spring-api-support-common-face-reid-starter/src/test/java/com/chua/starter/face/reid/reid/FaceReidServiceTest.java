package com.chua.starter.face.reid.reid;

import com.chua.starter.face.reid.engine.LocalFaceDetector;
import com.chua.starter.face.reid.model.TrackSummary;
import com.chua.starter.face.reid.properties.FaceReidConfig;
import com.chua.starter.face.reid.properties.FaceReidProperties;
import com.chua.starter.face.reid.spi.FaceDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FaceReidService} 端到端重识别测试。
 *
 * <p>不走 mock：用合成人脸图片跑完整链路
 * （解码 → 检测 → 特征 → 入库 → 轨迹归并 → 检索），验证「同一个人走过两个摄像头
 * 仍被认作同一个人」这一 Re-ID 的核心断言。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
class FaceReidServiceTest {

    /**
     * 被测服务。
     */
    private FaceReidService service;

    /**
     * 本地引擎的判定阈值。
     *
     * <p>内置引擎是手工特征，同人相似度接近 1、不同人在 0.97~0.99，
     * 因此必须用 0.995 这类高阈值（深度模型才用 0.55~0.60）。
     * 这个数字本身就是「阈值需按引擎标定」的证据。</p>
     */
    private static final double LOCAL_THRESHOLD = 0.995;

    /**
     * 构造完整链路。
     */
    @BeforeEach
    void setUp() {
        FaceReidProperties.DetectorOptions options = FaceReidProperties.DetectorOptions.defaults();
        FaceDetector detector = new LocalFaceDetector(options, 20_000_000L, 20);
        FaceReidConfig config = new FaceReidConfig(true, "local", LocalFaceDetector.DIMENSION,
                1000, SimilarityMetric.COSINE, LOCAL_THRESHOLD, 10, false, 3600, 5, 0, 20);
        service = new FaceReidService(detector, new FaceFeatureLibrary(config), "cam-default", 20);
    }

    /**
     * 生成人脸图片。
     *
     * @param seed 外观种子
     * @return PNG 字节
     */
    private static byte[] face(String seed) {
        return render(seed, 0);
    }

    /**
     * 渲染带若干偏移的合成人脸。
     *
     * @param seed   外观种子
     * @param shiftX 整体水平偏移（模拟不同机位）
     * @return PNG
     */
    private static byte[] render(String seed, int shiftX) {
        int size = 240;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(new Color(232, 234, 238));
            g.fillRect(0, 0, size, size);
            int hash = seed == null ? 0 : seed.hashCode();
            int skinShift = Math.abs(hash % 30);
            g.setColor(new Color(200 + skinShift / 2, 150 + skinShift / 3, 120 + skinShift / 4));
            int faceSize = 150;
            int faceX = 45 + shiftX;
            int faceY = 45;
            g.fillOval(faceX, faceY, faceSize, faceSize);
            g.setColor(new Color(40, 30, 25));
            g.fillOval(faceX, faceY - 10, faceSize, 60);
            int eyeShift = Math.abs(hash % 14) - 7;
            g.setColor(Color.WHITE);
            g.fillOval(faceX + 36 + eyeShift, faceY + 62, 26, 16);
            g.fillOval(faceX + 90 - eyeShift, faceY + 62, 26, 16);
            g.setColor(new Color(30, 30, 30));
            int pupilShift = Math.abs(hash % 6) - 3;
            g.fillOval(faceX + 44 + eyeShift + pupilShift, faceY + 66, 10, 10);
            g.fillOval(faceX + 98 - eyeShift + pupilShift, faceY + 66, 10, 10);
            g.setColor(new Color(180, 130, 105));
            g.fillOval(faceX + 66, faceY + 86, 18, 14);
            g.setColor(new Color(150, 70, 70));
            g.fillOval(faceX + 50, faceY + 112, 50 + Math.abs(hash % 10), 14);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            g.dispose();
        }
    }

    /**
     * 同一个人出现在两个摄像头：只应产生一条轨迹，且标记为跨镜。
     */
    @Test
    @DisplayName("跨摄像头同人去重为一条轨迹")
    void shouldRecognizeSamePersonAcrossCameras() {
        FaceReidService.RegisterResult first = service.register("cam-A", "frame-1", face("alice"));
        assertTrue(first.ok(), "注册应成功: " + first.reason());
        assertFalse(first.faces().isEmpty(), "应检出人脸");

        FaceReidService.RegisterResult second = service.register("cam-B", "frame-2", face("alice"));
        assertTrue(second.ok());

        assertEquals(1, service.library().trackCount(), "同一个人应只有一条轨迹");

        List<TrackSummary> tracks = service.tracks(null, false, 10);
        assertEquals(1, tracks.size());
        TrackSummary track = tracks.get(0);
        assertTrue(track.crossCamera(), "应识别为跨摄像头出现");
        assertEquals(2, track.cameraIds().size(), "应记录两个摄像头");
        assertTrue(track.cameraIds().containsAll(List.of("cam-A", "cam-B")));
    }

    /**
     * 不同的人应是不同的轨迹。
     */
    @Test
    @DisplayName("不同人产生不同轨迹")
    void shouldSeparateDifferentPeople() {
        service.register("cam-A", "f1", face("alice"));
        service.register("cam-A", "f2", face("zoe-with-different-features"));

        assertTrue(service.library().trackCount() >= 2, "不同人应至少两条轨迹");
    }

    /**
     * 以图搜人：用同一个人的图查询，应认出已有轨迹。
     */
    @Test
    @DisplayName("以图搜人命中已入库轨迹")
    void shouldIdentifyEnrolledPerson() {
        FaceReidService.RegisterResult registered = service.register("cam-A", "frame-1", face("alice"));
        assertTrue(registered.ok());
        String enrolledTrack = registered.faces().get(0).trackId();

        FaceReidService.IdentifyResult identified = service.identify(face("alice"), 5, false);
        assertTrue(identified.ok(), "识别应成功: " + identified.reason());
        assertFalse(identified.faces().isEmpty(), "应检出查询人脸");

        FaceReidService.FaceIdentification result = identified.faces().get(0);
        assertNotNull(result.best(), "应有候选");
        assertTrue(result.identified(), "同一张脸应被认出");
        assertEquals(enrolledTrack, result.best().track().trackId(), "应命中之前入库的轨迹");
        assertTrue(result.best().score() > 0.99, "同图相似度应接近 1，实际: " + result.best().score());
    }

    /**
     * 未入库的人查询时应返回未识别（而不是报错）。
     */
    @Test
    @DisplayName("未入库的人不误认")
    void shouldNotFalselyIdentifyStranger() {
        service.register("cam-A", "f1", face("alice"));

        FaceReidService.IdentifyResult identified = service.identify(face("completely-different"), 5, true);
        assertTrue(identified.ok());
        for (FaceReidService.FaceIdentification face : identified.faces()) {
            assertFalse(face.identified(), "陌生人不应被认出");
        }
    }

    /**
     * 纯背景图不产生记录。
     */
    @Test
    @DisplayName("无人脸图片安全处理")
    void shouldHandleImageWithoutFace() {
        BufferedImage blank = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = blank.createGraphics();
        try {
            g.setColor(new Color(250, 250, 250));
            g.fillRect(0, 0, 200, 200);
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(blank, "png", out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        FaceReidService.RegisterResult result = service.register("cam-A", "blank", out.toByteArray());
        assertTrue(result.ok(), "不应报错");
        assertTrue(result.faces().isEmpty(), "不应检出人脸");
        assertEquals(0, service.library().size(), "不应入库");
    }

    /**
     * 坏图片返回失败原因。
     */
    @Test
    @DisplayName("坏图片返回可读原因")
    void shouldReportFailureForBadImage() {
        FaceReidService.RegisterResult result = service.register("cam-A", "bad", new byte[]{9, 9, 9});
        assertFalse(result.ok());
        assertNotNull(result.reason());
    }

    /**
     * 引擎不可用时给出明确原因而非崩溃。
     */
    @Test
    @DisplayName("引擎不可用时降级")
    void shouldDegradeWhenEngineUnavailable() {
        FaceReidService degraded = new FaceReidService(new FaceDetector.Unavailable("测试降级"),
                new FaceFeatureLibrary(FaceReidConfig.of(new FaceReidProperties())), "cam", 20);

        assertFalse(degraded.available());
        FaceReidService.RegisterResult result = degraded.register("cam-A", "x", face("alice"));
        assertFalse(result.ok());
        assertEquals("测试降级", result.reason());

        FaceReidService.IdentifyResult identified = degraded.identify(face("alice"), 5, false);
        assertFalse(identified.ok());
    }

    /**
     * 统计信息可用。
     */
    @Test
    @DisplayName("统计信息反映运行态")
    void shouldExposeStats() {
        service.register("cam-A", "f1", face("alice"));
        var stats = service.stats();
        assertEquals(true, stats.get("available"));
        assertEquals("local", stats.get("engine"));
        assertEquals(1, stats.get("tracks"));
        assertTrue((Integer) stats.get("faces") >= 1);
    }

    /**
     * 清空后回到空库。
     */
    @Test
    @DisplayName("清空特征库")
    void shouldClearLibrary() {
        service.register("cam-A", "f1", face("alice"));
        assertTrue(service.library().size() > 0);
        assertTrue(service.clear() > 0);
        assertEquals(0, service.library().size());
        assertEquals(0, service.library().trackCount());
    }

    /**
     * sourceId 为空时自动生成，且不影响同帧去重语义。
     */
    @Test
    @DisplayName("来源 ID 自动生成")
    void shouldGenerateSourceId() {
        FaceReidService.RegisterResult result = service.register("cam-A", null, face("alice"));
        assertTrue(result.ok());
        assertNotNull(result.sourceId());
        assertFalse(result.sourceId().isBlank());
    }
}
