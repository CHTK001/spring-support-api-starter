package com.chua.starter.learning.properties;

import com.chua.starter.learning.model.ConfigProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 人脸特征值
 *
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "plugin.learning.image.feature")
public class ImageFeatureProperties extends ConfigProperties {
}
