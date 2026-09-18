package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.DownloadHistoryRecord;
import com.chua.starter.geo.service.DownloadHistoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 下载历史记录服务实现，使用 JSON 文件持久化。
 * 文件路径默认 ~/.geo-downloader/历史.json，可通过配置覆盖。
 *
 * @author CH
 * @since 1.0.0
 */
@Service
@Slf4j
public class DownloadHistoryServiceImpl implements DownloadHistoryService {

    private static final String DEFAULT_HISTORY_FILE = "history.json"; // 默认历史文件

    /** JSON 序列化器 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 历史记录文件路径 */
    private Path historyFile;

    /**
     * 构造方法，支持通过配置覆盖历史文件路径。
     *
     * @param historyDir 历史数据存储目录（可选，默认 ~/.geo-downloader）
     * @param @Value("${geo.history.dir:}" @Value("${geo.history.dir:}"
     */
    public DownloadHistoryServiceImpl(@Value("${geo.history.dir:}") String historyDir) {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        if (historyDir != null && !historyDir.isBlank()) {
            this.historyFile = Path.of(historyDir, DEFAULT_HISTORY_FILE);
        } else {
            this.historyFile = Path.of(System.getProperty("user.home"), ".geo-downloader", DEFAULT_HISTORY_FILE);
        }
    }

    /**
     * 应用启动时初始化文件。
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(historyFile.getParent());
            if (!Files.exists(historyFile)) {
                Files.writeString(historyFile, "[]");
            }
            log.info("历史文件路径: {}", historyFile.toAbsolutePath());
        } catch (IOException e) {
            log.warn("初始化历史文件失败: {}", e.getMessage());
        }
    }

    /**
     * 保存下载成功记录。
     *
     * @param record 下载结果记录
     */
    @Override
    public void save(DownloadHistoryRecord record) {
        record.setCreatedAt(LocalDateTime.now());
        record.setStatus("completed");
        List<DownloadHistoryRecord> records = readAll();
        records.add(0, record);
        writeAll(records);
        log.info("[历史] 保存成功记录: {} tiles={}", record.getName(), record.getTileCount());
    }

    /**
     * 保存下载失败记录。
     *
     * @param record 失败结果记录
     */
    @Override
    public void saveFailed(DownloadHistoryRecord record) {
        record.setCreatedAt(LocalDateTime.now());
        record.setStatus("failed");
        List<DownloadHistoryRecord> records = readAll();
        records.add(0, record);
        writeAll(records);
        log.info("[历史] 保存失败记录: {} error={}", record.getName(), record.getFilePath());
    }

    /**
     * 获取所有历史记录（按创建时间倒序）。
     *
     * @return 历史记录列表
     */
    @Override
    public List<DownloadHistoryRecord> listAll() {
        return readAll();
    }

    /**
     * 分页查询历史记录。
     *
     * @param page     页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult listPage(int page, int pageSize) {
        List<DownloadHistoryRecord> all = readAll();
        int from = Math.min((page - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<DownloadHistoryRecord> pageRecords = all.subList(from, to);
        return new PageResult(pageRecords, all.size());
    }

    /**
     * 删除指定记录。
     *
     * @param id 记录 标识
     * @return 是否删除成功
     */
    @Override
    public boolean delete(String id) {
        List<DownloadHistoryRecord> records = readAll();
        boolean removed = records.removeIf(r -> id.equals(r.getId()));
        if (removed) {
            writeAll(records);
        }
        return removed;
    }

    /**
     * 清空所有历史记录。
     */
    @Override
    public void clear() {
        writeAll(Collections.emptyList());
        log.info("[历史] 历史记录已清空");
    }

    /**
     * 读取所有历史记录。
     *
     * @return 历史记录列表
     */
    private List<DownloadHistoryRecord> readAll() {
        try {
            if (!Files.exists(historyFile)) {
                return Collections.emptyList();
            }
            String content = Files.readString(historyFile);
            if (content.isBlank()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(content, new TypeReference<>() {});
        } catch (IOException e) {
            log.warn("读取历史记录失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 写入所有历史记录。
     *
     * @param records 历史记录列表
     */
    private void writeAll(List<DownloadHistoryRecord> records) {
        try {
            Files.writeString(historyFile, objectMapper.writeValueAsString(records));
        } catch (IOException e) {
            log.warn("写入历史记录失败: {}", e.getMessage());
        }
    }
}
