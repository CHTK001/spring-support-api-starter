package com.chua.starter.learning.properties;

import com.chua.starter.learning.model.ConfigProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ocr配置
 *
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "plugin.learning.compare.face")
public class FaceCompareProperties extends ConfigProperties {
}
