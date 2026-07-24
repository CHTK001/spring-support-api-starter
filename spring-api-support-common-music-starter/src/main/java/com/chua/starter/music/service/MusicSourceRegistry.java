package com.chua.starter.music.service;

import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.data.support.music.model.MusicSourceOption;
import com.chua.data.support.music.spi.MusicSourceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 音源 SPI 注册中心。
 */
@Component
@RequiredArgsConstructor
public class MusicSourceRegistry {

    private static final String DEFAULT_SOURCE = "tx";
    private static final String ALL_SOURCE = "all";
    private static final List<String> PREFERRED_ORDER = Arrays.asList(
            "tx", "kw", "mg", "wy", "kg", "bd", "git", "demo"
    );
    private final ObjectProvider<MusicSourceAvailabilityService> musicSourceAvailabilityService;

    public List<MusicSourceOption> listSources() {
        List<MusicSourceOption> result = new ArrayList<>();
        for (MusicSourceProvider provider : listProviders()) {
            result.add(toSourceOption(provider, true));
        }
        result.sort(Comparator.comparing(MusicSourceOption::getCode));
        return result;
    }

    public List<MusicSourceOption> listAllSources() {
        List<MusicSourceOption> result = new ArrayList<>();
        for (MusicSourceProvider provider : listAllProviders()) {
            result.add(toSourceOption(provider, isEnabled(provider.getSource().getCode())));
        }
        result.sort(Comparator.comparing(MusicSourceOption::getCode));
        return result;
    }

    public List<MusicSourceProvider> listProviders() {
        return listAllProviders().stream()
                .filter(provider -> isEnabled(provider.getSource().getCode()))
                .toList();
    }

    public List<MusicSourceProvider> listAllProviders() {
        ServiceProvider<MusicSourceProvider> provider = ServiceProvider.of(MusicSourceProvider.class);
        Set<String> extensions = provider.getExtensions();
        List<MusicSourceProvider> result = new ArrayList<>();
        for (String extension : extensions) {
            MusicSourceProvider instance = provider.getNewExtension(extension);
            if (instance == null) {
                continue;
            }
            result.add(instance);
        }
        result.sort(Comparator.comparing(item -> item.getSource().getCode()));
        return result;
    }

    public boolean isAggregateSource(String source) {
        return !StringUtils.hasText(source) || ALL_SOURCE.equalsIgnoreCase(source);
    }

    public String resolveSource(String source) {
        if (StringUtils.hasText(source)) {
            return source;
        }
        List<String> codes = listProviders().stream()
                .map(MusicSourceProvider::getSource)
                .map(MusicSourceOption::getCode)
                .toList();
        for (String code : PREFERRED_ORDER) {
            if (codes.contains(code)) {
                return code;
            }
        }
        return DEFAULT_SOURCE;
    }

    public MusicSourceProvider requireProvider(String source) {
        String actualSource = resolveSource(source);
        MusicSourceProvider provider = listAllProviders().stream()
                .filter(item -> actualSource.equals(item.getSource().getCode()))
                .findFirst()
                .orElse(null);
        if (provider == null) {
            throw new IllegalArgumentException("未找到音源实现: " + actualSource);
        }
        if (!isEnabled(actualSource)) {
            throw new IllegalStateException("音源已停用: " + actualSource);
        }
        return provider;
    }

    private boolean isEnabled(String sourceCode) {
        MusicSourceAvailabilityService service = musicSourceAvailabilityService.getIfAvailable();
        return service == null || service.isEnabled(sourceCode);
    }

    private MusicSourceOption toSourceOption(MusicSourceProvider provider, boolean enabled) {
        MusicSourceOption source = provider.getSource();
        return MusicSourceOption.builder()
                .code(source.getCode())
                .name(source.getName())
                .description(source.getDescription())
                .enabled(enabled)
                .build();
    }
}
