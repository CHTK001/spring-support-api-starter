package com.chua.starter.learning.configuration;

import com.chua.pytorch.support.common.LearningConfig;
import com.chua.starter.learning.properties.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置
 *
 * @author CH
 */
@Configuration
@EnableConfigurationProperties({
        OcrProperties.class,
        LearningProperties.class,
        FaceDetectionProperties.class,
        CocoProperties.class,
        VehicleProperties.class,
        AnimalProperties.class,
        ImageCompareProperties.class,
        TextCompareProperties.class,
        MaskProperties.class,
        FaceRegProperties.class,
        ImageRegProperties.class,
        FaceFeatureProperties.class,
        ImageFeatureProperties.class,
        GanProperties.class,
        FaceCompareProperties.class
})
public class LearningConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LearningConfig learningConfig(LearningProperties learningProperties) {
        LearningConfig learningConfiguration = new LearningConfig();
        learningConfiguration.setModulePath(learningProperties.getModulePath());
        return learningConfiguration;
    }
}
