package com.chua.starter.learning.properties;

import com.chua.starter.learning.model.ConfigProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 人脸识别
 *
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "plugin.learning.face.reg")
public class FaceRegProperties extends ConfigProperties {
    /**
     * 人脸底库位置
     */
    private String library = "face";
    /**
     * 单张图片人脸超过一个时候取top1
     */
    private boolean overing = true;
    /**
     * 返回数据的阈值
     */
    private float threshold = 0.5f;
}
