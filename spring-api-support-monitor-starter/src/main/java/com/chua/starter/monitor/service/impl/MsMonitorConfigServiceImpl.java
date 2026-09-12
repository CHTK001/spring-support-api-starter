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

/**
* 全局配置服务实现，提供配置的批量保存与键值对读取能力。
*
* @since 2026-08-11
* @author CH
 */
@Slf4j
@Service
public class MsMonitorConfigServiceImpl extends ServiceImpl<MsMonitorConfigMapper, MsMonitorConfig>
        implements MsMonitorConfigService {

    /**
    * 读取全部配置项，返回键值对映射；相同 键 时后面的值覆盖前面的值。
    *
    * @return 配置键值对 映射
     */
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

    /**
    * 批量保存或更新配置项（事务保障）。
    *
    * @param configs 配置列表
    * @return 是否保存成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(java.util.Collection<MsMonitorConfig> configs) {
        return super.saveBatch(configs);
    }
}
