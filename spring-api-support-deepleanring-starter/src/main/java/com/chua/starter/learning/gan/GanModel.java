package com.chua.starter.learning.gan;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiOption;
import com.chua.common.support.constant.PredictResult;
import com.chua.common.support.engine.Engine;
import com.chua.common.support.feature.detector.Detector;
import com.chua.common.support.utils.BufferedImageUtils;
import com.chua.pytorch.support.biggan.BigGAN128;
import com.chua.starter.learning.model.Model;
import com.chua.starter.learning.properties.GanProperties;
import com.chua.starter.learning.properties.LearningProperties;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图像生成
 * @author CH
 */
@Api(tags = "图像生成")
@RestController
@RequestMapping("/v1/gan")
@Slf4j
@Spi("图像生成")
@SpiOption(value = "图像生成")
public class GanModel implements Model {

    Map<Integer, BigGAN128> detector = new HashMap<>(3);

    @Resource
    private GanProperties ganProperties;
    @Resource
    private LearningProperties learningProperties;

    private Engine engine;

    @Operation(summary = "分类")
    @GetMapping
    public ResponseEntity<byte[]> reg(int size, int keyword) {
        if(null == engine) {
            engine = Engine.auto(ganProperties.getConfig(learningProperties));
        }
        Detector bigGAN128 = engine.get("gan" + size, Detector.class);
        List<PredictResult> detect = bigGAN128.predict(keyword);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BufferedImage bufferedImage = (BufferedImage) detect.get(0).getNdArray();
            BufferedImageUtils.writeToStream(bufferedImage, baos, "jpeg");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(baos.toByteArray());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new byte[0]);
        }
    }


}
