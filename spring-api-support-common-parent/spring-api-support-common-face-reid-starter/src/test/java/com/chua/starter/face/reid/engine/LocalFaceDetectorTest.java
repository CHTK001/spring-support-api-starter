package com.chua.starter.face.reid.engine;

import com.chua.starter.face.reid.properties.FaceReidProperties;
import com.chua.starter.face.reid.spi.FaceDetection;
import com.chua.starter.face.reid.spi.FaceDetector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LocalFaceDetector} 与图像处理链路的测试。
 *
 * @author CH
 * @since 4.0.0.43
 */
class LocalFaceDetectorTest {

    /**
     * 构造默认引擎。
     *
     * @return 引擎
     */
    private LocalFaceDetector detector() {
        return new LocalFaceDetector(FaceReidProperties.DetectorOptions.defaults(),
                20_000_000L, 20);
    }

    /**
     * 合成人脸应被检出，且特征维度固定、已归一化。
     */
    @Test
    @DisplayName("检出人脸且特征 L2 归一化")
    void shouldDetectSyntheticFace() {
        FaceDetector.DetectionResult result = detector().detect(SyntheticImages.render("alice", true), "alice.png");

        assertTrue(result.ok(), "合成人脸应检测成功，实际: " + result.reason());
        assertFalse(result.faces().isEmpty(), "应至少检出一张人脸");

        FaceDetection face = result.faces().get(0);
        assertEquals(LocalFaceDescriptor.DIMENSION, face.dimension(), "维度应与引擎声明一致");
        double norm = 0;
        for (float value : face.embedding()) {
            norm += (double) value * value;
        }
        assertEquals(1.0, Math.sqrt(norm), 1e-4, "特征应已 L2 归一化");
        assertTrue(face.box().quality() > 0, "检出框应有正质量");
    }

    /**
     * 同一外观两次提取应得到几乎相同的特征（Re-ID 可重现的前提）。
     */
    @Test
    @DisplayName("同图两次提取特征一致")
    void shouldBeDeterministic() {
        byte[] image = SyntheticImages.render("bob", true);
        List<FaceDetection> first = detector().detect(image, "bob.png").faces();
        List<FaceDetection> second = detector().detect(image, "bob.png").faces();

        assertEquals(first.size(), second.size());
        double similarity = com.chua.starter.face.reid.reid.SimilarityMetric.COSINE
                .score(first.get(0).embedding(), second.get(0).embedding());
        assertEquals(1.0, similarity, 1e-6, "同图同框特征应完全一致");
    }

    /**
     * 纯背景图不应检出人脸。
     */
    @Test
    @DisplayName("纯背景图不误检")
    void shouldNotDetectOnBlankBackground() {
        FaceDetector.DetectionResult result = detector().detect(SyntheticImages.render("none", false), "blank.png");
        assertTrue(result.ok());
        assertTrue(result.faces().isEmpty(), "纯灰背景不应检出人脸");
    }

    /**
     * 坏输入返回可读原因而不是抛异常。
     */
    @Test
    @DisplayName("坏输入安全降级")
    void shouldRejectInvalidInput() {
        LocalFaceDetector detector = detector();

        FaceDetector.DetectionResult empty = detector.detect(new byte[0], "x.png");
        assertFalse(empty.ok());
        assertNotNull(empty.reason());

        FaceDetector.DetectionResult garbage = detector.detect(new byte[]{1, 2, 3, 4}, "x.png");
        assertFalse(garbage.ok());
        assertTrue(garbage.faces().isEmpty());

        FaceDetector.DetectionResult badSuffix = detector.detect(SyntheticImages.render("c", true), "c.gif");
        assertFalse(badSuffix.ok());
        assertTrue(badSuffix.reason().contains("不支持"), "应提示格式不支持: " + badSuffix.reason());
    }

    /**
     * 灰度开关生效。
     */
    @Test
    @DisplayName("灰度图受 allow-gray-input 控制")
    void shouldRespectGrayscaleFlag() {
        FaceReidProperties.DetectorOptions allow = FaceReidProperties.DetectorOptions.defaults();
        LocalFaceDetector lenient = new LocalFaceDetector(allow, 20_000_000L, 20);
        byte[] gray = SyntheticImages.grayscale("dave");
        assertTrue(lenient.detect(gray, "dave.png").ok(), "允许灰度时应能解码");

        FaceReidProperties.DetectorOptions base = FaceReidProperties.DetectorOptions.defaults();
        FaceReidProperties.DetectorOptions strict = new FaceReidProperties.DetectorOptions(
                base.minFaceSize(), base.scoreThreshold(), base.nmsThreshold(), base.minQuality(),
                base.blurThreshold(), base.normalizeBrightness(), base.flipAugment(),
                false, base.allowedImageSuffix());
        LocalFaceDetector denied = new LocalFaceDetector(strict, 20_000_000L, 20);
        FaceDetector.DetectionResult result = denied.detect(gray, "dave.png");
        assertFalse(result.ok(), "禁止灰度时应拒绝");
    }

    /**
     * 超大图按比例缩放。
     */
    @Test
    @DisplayName("超过像素上限时缩小")
    void shouldDownscaleOversizedImage() {
        ImageDecoder decoder = new ImageDecoder(FaceReidProperties.DetectorOptions.defaults(), 10_000L);
        ImageDecoder.Decoded decoded = decoder.decode(SyntheticImages.render("erin", true), "erin.png");
        assertNotNull(decoded.image());
        assertTrue(decoded.scaled(), "应触发缩放");
        assertTrue((long) decoded.image().width() * decoded.image().height() <= 10_000L);
    }

    /**
     * 引擎标识与 SPI 匹配。
     */
    @Test
    @DisplayName("引擎名匹配 SPI")
    void shouldMatchEngineName() {
        LocalFaceDetector detector = detector();
        assertTrue(detector.supports("local"));
        assertTrue(detector.supports("LOCAL"));
        assertFalse(detector.supports("arcface"));
        assertTrue(detector.ready());
        assertEquals(LocalFaceDescriptor.DIMENSION, detector.dimension());
    }
}
