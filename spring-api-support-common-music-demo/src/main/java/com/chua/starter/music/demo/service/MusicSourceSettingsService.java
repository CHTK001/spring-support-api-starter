package com.chua.starter.music.demo.service;

import com.chua.starter.music.model.MusicSourceOption;
import com.chua.starter.music.service.MusicSourceAvailabilityService;
import com.chua.starter.music.service.MusicSourceRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicSourceSettingsService implements MusicSourceAvailabilityService {

    private final ObjectMapper objectMapper;
    private final MusicSourceRegistry musicSourceRegistry;
    private final Map<String, Boolean> sourceStates = new ConcurrentHashMap<>();

    @Value("${music.admin.settings-file:.runtime/music-source-settings.json}")
    private String settingsFile;

    @PostConstruct
    public void init() {
        load();
    }

    public List<MusicSourceOption> listAllSources() {
        return musicSourceRegistry.listAllSources();
    }

    public MusicSourceOption updateSourceState(String sourceCode, boolean enabled) {
        boolean exists = musicSourceRegistry.listAllProviders().stream()
                .anyMatch(provider -> sourceCode.equals(provider.getSource().getCode()));
        if (!exists) {
            throw new IllegalArgumentException("未找到音源: " + sourceCode);
        }
        sourceStates.put(sourceCode, enabled);
        save();
        return listAllSources().stream()
                .filter(item -> sourceCode.equals(item.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到音源: " + sourceCode));
    }

    @Override
    public boolean isEnabled(String sourceCode) {
        return sourceStates.getOrDefault(sourceCode, true);
    }

    private void load() {
        Path path = Path.of(settingsFile);
        if (!Files.exists(path)) {
            return;
        }
        try {
            Map<String, Boolean> loaded = objectMapper.readValue(path.toFile(), new TypeReference<>() {
            });
            loaded.forEach((key, value) -> {
                if (StringUtils.hasText(key) && value != null) {
                    sourceStates.put(key, value);
                }
            });
        } catch (IOException ex) {
            log.warn("Load music source settings failed, file={}, message={}", settingsFile, ex.getMessage());
        }
    }

    private void save() {
        Path path = Path.of(settingsFile);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Map<String, Boolean> ordered = new LinkedHashMap<>();
            for (MusicSourceOption source : musicSourceRegistry.listAllSources()) {
                ordered.put(source.getCode(), isEnabled(source.getCode()));
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), ordered);
        } catch (IOException ex) {
            throw new IllegalStateException("保存音源配置失败: " + settingsFile, ex);
        }
    }
}
