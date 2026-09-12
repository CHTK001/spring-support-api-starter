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
* @author CH
* @since 4.0.0
 */
@Component
@RequiredArgsConstructor
public class MusicSourceRegistry {

    /** 默认_源 */
    private static final String DEFAULT_SOURCE = "tx";
    /** 全部_源 */
    private static final String ALL_SOURCE = "all";
    /** Preferred_订单 */
    private static final List<String> PREFERRED_ORDER = Arrays.asList(
            "tx", "kw", "mg", "wy", "kg", "bd", "git", "demo"
    );
    /** Music来源可用性服务 */
    private final ObjectProvider<MusicSourceAvailabilityService> musicSourceAvailabilityService;

    /**
    * 列表源
    *
    * @return 列表源的结果
     */
    public List<MusicSourceOption> listSources() {
        List<MusicSourceOption> result = new ArrayList<>();
        for (MusicSourceProvider provider : listProviders()) {
            result.add(toSourceOption(provider, true));
        }
        result.sort(Comparator.comparing(MusicSourceOption::getCode));
        return result;
    }

    /**
    * 列表全部源
    *
    * @return 列表全部源的结果
     */
    public List<MusicSourceOption> listAllSources() {
        List<MusicSourceOption> result = new ArrayList<>();
        for (MusicSourceProvider provider : listAllProviders()) {
            result.add(toSourceOption(provider, isEnabled(provider.getSource().getCode())));
        }
        result.sort(Comparator.comparing(MusicSourceOption::getCode));
        return result;
    }

    /**
    * 列表提供者
    *
    * @return 列表提供者的结果
     */
    public List<MusicSourceProvider> listProviders() {
        return listAllProviders().stream()
                .filter(provider -> isEnabled(provider.getSource().getCode()))
                .toList();
    }

    /**
    * 列表全部提供者
    *
    * @return 列表全部提供者的结果
     */
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

    /**
    * 是否aggregate源
    *
    * @param source 源
    * @return 是否aggregate源的结果
     */
    public boolean isAggregateSource(String source) {
        return !StringUtils.hasText(source) || ALL_SOURCE.equalsIgnoreCase(source);
    }

    /**
    * 解析源
    *
    * @param source 源
    * @return resolve源的结果
     */
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

    /**
    * require提供者
    *
    * @param source 源
    * @return require提供者的结果
     */
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

    /**
    * 是否已启用
    *
    * @param sourceCode 源编码
    * @return 是否已启用的结果
     */
    private boolean isEnabled(String sourceCode) {
        MusicSourceAvailabilityService service = musicSourceAvailabilityService.getIfAvailable();
        return service == null || service.isEnabled(sourceCode);
    }

    /**
    * 转为源期权
    *
    * @param provider 提供者
    * @param enabled 已启用
    * @return 转为源期权的结果
     */
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

