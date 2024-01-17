package com.chua.starter.learning.service;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiDefault;
import com.chua.common.support.constant.PredictResult;
import com.chua.common.support.engine.Engine;
import com.chua.common.support.feature.detector.Detector;
import com.chua.common.support.lang.loader.BaseLazyLoader;
import com.chua.common.support.lang.loader.Loader;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.learning.model.ConfigProperties;
import com.chua.starter.learning.properties.LearningProperties;

import java.util.List;

/**
 * 引擎实现
 * @author CH
 */
@Spi("detector")
@SpiDefault
public class SpiEngineService implements EngineService{
    protected final ConfigProperties configProperties;
    final Loader<Detector> lazyLoader = new BaseLazyLoader<Detector>() {
        @Override
        public Detector init() {
            return Engine.auto(configProperties.getConfig())
                    .get(configProperties.getSpi(), Detector.class);
        }
    };

    public SpiEngineService(ConfigProperties configProperties , LearningProperties learningProperties) {
        if(StringUtils.isEmpty(configProperties.getModulePath())) {
            configProperties.setModulePath(learningProperties.getModulePath());
        }

        this.configProperties = configProperties;
    }


    @Override
    public List<PredictResult> recognize(Object... input) {
        return lazyLoader.get().predict(input);
    }
}
