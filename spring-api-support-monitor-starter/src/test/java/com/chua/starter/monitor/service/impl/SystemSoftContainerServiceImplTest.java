package com.chua.starter.monitor.service.impl;

import com.chua.starter.monitor.entity.SystemSoftContainerStats;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemSoftContainerServiceImplTest {

    @Test
    void extractIncrementalLogsShouldReturnNewSuffix() {
        String previousLogs = "line1\nline2\n";
        String latestLogs = "line1\nline2\nline3\n";

        String delta = SystemSoftContainerServiceImpl.extractIncrementalLogs(previousLogs, latestLogs);

        assertThat(delta).isEqualTo("line3\n");
    }

    @Test
    void extractIncrementalLogsShouldHandleShiftedWindow() {
        String previousLogs = "line1\nline2\n";
        String latestLogs = "line2\nline3\n";

        String delta = SystemSoftContainerServiceImpl.extractIncrementalLogs(previousLogs, latestLogs);

        assertThat(delta).isEqualTo("line3\n");
    }

    @Test
    void extractIncrementalLogsShouldReturnEmptyWhenUnchanged() {
        String logs = "same";

        String delta = SystemSoftContainerServiceImpl.extractIncrementalLogs(logs, logs);

        assertThat(delta).isEmpty();
    }

    @Test
    void normalizeStatsSnapshotShouldTrimLongTextFields() {
        String oversized = "x".repeat(400);
        SystemSoftContainerStats stats = new SystemSoftContainerStats();
        stats.setSystemSoftContainerStatsContainerId(oversized);
        stats.setSystemSoftContainerStatsContainerName(oversized);
        stats.setSystemSoftContainerStatsStatus(oversized);
        stats.setSystemSoftContainerStatsStartedAt(oversized);
        stats.setSystemSoftContainerStatsImage(oversized);
        stats.setSystemSoftContainerStatsCommand(oversized);
        stats.setSystemSoftContainerStatsCreated(oversized);
        stats.setSystemSoftContainerStatsPorts(oversized);
        stats.setSystemSoftContainerStatsLabels(oversized);
        stats.setSystemSoftContainerStatsStatusDetail(oversized);

        SystemSoftContainerServiceImpl.normalizeStatsSnapshot(stats);

        assertThat(stats.getSystemSoftContainerStatsContainerId()).hasSize(255);
        assertThat(stats.getSystemSoftContainerStatsContainerName()).hasSize(255);
        assertThat(stats.getSystemSoftContainerStatsStatus()).hasSize(255);
        assertThat(stats.getSystemSoftContainerStatsStartedAt()).hasSize(255);
        assertThat(stats.getSystemSoftContainerStatsImage()).hasSize(255);
        assertThat(stats.getSystemSoftContainerStatsCommand()).hasSize(255);
        assertThat(stats.getSystemSoftContainerStatsCreated()).hasSize(255);
        assertThat(stats.getSystemSoftContainerStatsPorts()).hasSize(255);
        assertThat(stats.getSystemSoftContainerStatsLabels()).hasSize(255);
        assertThat(stats.getSystemSoftContainerStatsStatusDetail()).hasSize(255);
    }
}
