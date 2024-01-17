package com.chua.starter.learning.model;

import com.chua.common.support.feature.DetectionConfiguration;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.learning.properties.LearningProperties;
import lombok.Data;

/**
 * @author CH
 */
@Data
public class ConfigProperties {

    /**
     * spring的Bean实现方式
     */
    private String type;
    /**
     * spi实现方式
     */
    private String spi;
    /**
     * 引擎
     */
    private String engine;
    /**
     * 模型地址
     */
    private String modulePath;
    /**
     * 缓存目录
     */
    private String cachePath = "./cache";

    public ConfigProperties setSpi(String spi) {
        if(StringUtils.isEmpty(this.spi)) {
            this.spi = spi;
        }
        return this;
    }

    public ConfigProperties setType(String type) {
        if(StringUtils.isEmpty(this.type)) {
            this.type = type;
        }
        return this;
    }
    public String getType() {
        if(StringUtils.isEmpty(this.type)) {
           return "detector";
        }
        return this.type;
    }

    public DetectionConfiguration getConfig(LearningProperties learningProperties) {
        return DetectionConfiguration.builder()
                .cachePath(StringUtils.defaultString(this.getModulePath(), learningProperties.getModulePath()))
                .type(this.getSpi())
                .engine(this.getEngine())
                .build();
    }
    public DetectionConfiguration getConfig() {
        return DetectionConfiguration.builder()
                .cachePath(this.getModulePath())
                .type(this.getSpi())
                .engine(this.getEngine())
                .build();
    }
}
