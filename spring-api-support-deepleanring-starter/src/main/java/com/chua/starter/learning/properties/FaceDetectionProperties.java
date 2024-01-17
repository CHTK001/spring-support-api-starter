package com.chua.starter.learning.properties;

import com.chua.starter.learning.model.ConfigProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ocr配置
 *
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "plugin.learning.face.detection")
public class FaceDetectionProperties extends ConfigProperties {
}
