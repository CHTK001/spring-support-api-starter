package com.chua.starter.music.service;

import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.starter.music.model.MusicSourceOption;
import com.chua.starter.music.spi.MusicSourceProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 音源 SPI 注册中心。
 */
@Component
public class MusicSourceRegistry {

    private static final String DEFAULT_SOURCE = "demo";

    public List<MusicSourceOption> listSources() {
        ServiceProvider<MusicSourceProvider> provider = ServiceProvider.of(MusicSourceProvider.class);
        Set<String> extensions = provider.getExtensions();
        List<MusicSourceOption> result = new ArrayList<>();
        for (String extension : extensions) {
            MusicSourceProvider instance = provider.getNewExtension(extension);
            if (instance == null) {
                continue;
            }
            result.add(instance.getSource());
        }
        result.sort(Comparator.comparing(MusicSourceOption::getCode));
        return result;
    }

    public String resolveSource(String source) {
        if (StringUtils.hasText(source)) {
            return source;
        }
        return DEFAULT_SOURCE;
    }

    public MusicSourceProvider requireProvider(String source) {
        String actualSource = resolveSource(source);
        MusicSourceProvider provider = ServiceProvider.of(MusicSourceProvider.class).getNewExtension(actualSource);
        if (provider == null) {
            throw new IllegalArgumentException("未找到音源实现: " + actualSource);
        }
        return provider;
    }
}
