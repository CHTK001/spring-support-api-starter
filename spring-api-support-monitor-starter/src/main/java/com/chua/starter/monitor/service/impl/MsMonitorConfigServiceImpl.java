package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.monitor.entity.MsMonitorConfig;
import com.chua.starter.monitor.mapper.MsMonitorConfigMapper;
import com.chua.starter.monitor.service.MsMonitorConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MsMonitorConfigServiceImpl extends ServiceImpl<MsMonitorConfigMapper, MsMonitorConfig>
        implements MsMonitorConfigService {

    @Override
    public Map<String, String> getAllConfigMap() {
        var list = list();
        if (CollectionUtils.isEmpty(list)) {
            return Map.of();
        }
        return list.stream().collect(Collectors.toMap(
                MsMonitorConfig::getConfigKey,
                MsMonitorConfig::getConfigValue,
                (v1, v2) -> v2));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(java.util.List<MsMonitorConfig> configs) {
        super.saveBatch(configs);
    }
}
