package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.WaybackVersion;
import com.chua.starter.geo.service.WaybackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Esri Wayback 历史影像服务实现，返回示例历史版本数据。
 *
 * @author CH
 * @since 1.0.0
 */
@Service
@Slf4j
public class WaybackServiceImpl implements WaybackService {

    /**
     * 获取所有历史影像版本列表。
     *
     * @return 版本列表
     */
    @Override
    public List<WaybackVersion> getVersions() {
        List<WaybackVersion> versions = new ArrayList<>();
        // TODO: 接入 Esri Wayback API 获取真实历史版本
        LocalDateTime baseDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        for (int i = 0; i < 5; i++) {
            WaybackVersion v = new WaybackVersion();
            v.setId("ver_" + i);
            v.setDateStr(baseDate.plusMonths(i).toLocalDate().toString());
            v.setTimestamp(baseDate.plusMonths(i));
            v.setDescription("2024-0" + (i + 1) + "月卫星影像");
            versions.add(v);
        }
        return versions;
    }
}
