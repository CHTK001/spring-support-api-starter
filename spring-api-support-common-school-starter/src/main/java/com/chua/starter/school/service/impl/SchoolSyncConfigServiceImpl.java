package com.chua.starter.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.school.entity.SchoolSyncConfig;
import com.chua.starter.school.mapper.SchoolSyncConfigMapper;
import com.chua.starter.school.service.SchoolSyncConfigService;
import com.chua.starter.school.service.SchoolSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * 学校同步配置Service实现类
 *
 * @author CH
 */
@Slf4j
@Service
public class SchoolSyncConfigServiceImpl extends ServiceImpl<SchoolSyncConfigMapper, SchoolSyncConfig> implements SchoolSyncConfigService {

    private final SchoolSyncService schoolSyncService;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public SchoolSyncConfigServiceImpl(SchoolSyncService schoolSyncService, TransactionTemplate transactionTemplate) {
        this.schoolSyncService = schoolSyncService;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * 分页查询同步配置
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 同步配置分页列表
     */
    @Override
    public IPage<SchoolSyncConfig> pageSyncConfig(Integer pageNum, Integer pageSize) {
        Page<SchoolSyncConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SchoolSyncConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SchoolSyncConfig::getConfigId);
        return baseMapper.selectPage(page, queryWrapper);
    }

    /**
     * 获取同步配置详情
     *
     * @param id 配置ID
     * @return 同步配置详情
     */
    @Override
    public SchoolSyncConfig getSyncConfigDetail(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 保存同步配置
     *
     * @param config 同步配置
     * @return 是否保存成功
     */
    @Override
    public boolean saveSyncConfig(SchoolSyncConfig config) {
        return transactionTemplate.execute(status -> {
            try {
                if (config.getConfigId() == null) {
                    // 新增配置
                    config.setSyncStatus(0); // 设置为未同步状态
                    config.setCreateTime(LocalDateTime.now());
                    config.setUpdateTime(LocalDateTime.now());
                    return baseMapper.insert(config) > 0;
                } else {
                    // 更新配置
                    SchoolSyncConfig existingConfig = baseMapper.selectById(config.getConfigId());
                    if (existingConfig == null) {
                        return false;
                    }
                    config.setUpdateTime(LocalDateTime.now());
                    return baseMapper.updateById(config) > 0;
                }
            } catch (Exception e) {
                log.error("保存同步配置异常: {}", e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        });
    }

    /**
     * 删除同步配置
     *
     * @param id 配置ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteSyncConfig(Long id) {
        return transactionTemplate.execute(status -> {
            try {
                return baseMapper.deleteById(id) > 0;
            } catch (Exception e) {
                log.error("删除同步配置异常: {}", e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        });
    }

    /**
     * 手动执行同步
     *
     * @param id 配置ID
     * @return 是否执行成功
     */
    @Override
    public boolean executeSync(Long id) {
        return transactionTemplate.execute(status -> {
            try {
                SchoolSyncConfig config = baseMapper.selectById(id);
                if (config == null) {
                    log.error("同步配置不存在，ID: {}", id);
                    return false;
                }

                // 更新状态为同步中
                config.setSyncStatus(1);
                config.setUpdateTime(LocalDateTime.now());
                baseMapper.updateById(config);

                // 从同步URL中提取学校ID
                String schoolId = extractSchoolIdFromUrl(config.getSyncUrl());
                if (schoolId == null) {
                    log.error("无法从URL中提取学校ID: {}", config.getSyncUrl());
                    updateSyncFailed(config);
                    return false;
                }

                // 执行同步
                boolean success = schoolSyncService.syncAllSchoolData(schoolId, config);

                // 更新同步状态
                config.setSyncStatus(success ? 2 : 3); // 成功=2，失败=3
                config.setLastSyncTime(LocalDateTime.now());
                config.setUpdateTime(LocalDateTime.now());
                baseMapper.updateById(config);

                return success;
            } catch (Exception e) {
                log.error("执行同步异常: {}", e.getMessage(), e);
                status.setRollbackOnly();
                return false;
            }
        });
    }

    /**
     * 手动同步指定学校
     *
     * @param schoolId 高考网学校ID
     * @return 是否同步成功
     */
    @Override
    public boolean syncSchool(String schoolId) {
        SchoolSyncConfig tempConfig = new SchoolSyncConfig();
        tempConfig.setConfigName("手动同步-" + schoolId);
        tempConfig.setSyncTarget("学校信息");
        tempConfig.setSyncUrl("https://www.gaokao.cn/school/" + schoolId);
        tempConfig.setSyncStatus(1); // 同步中

        // 默认使用高考网同步类型
        tempConfig.setSyncType("gaokao");

        try {
            return schoolSyncService.syncAllSchoolData(schoolId, tempConfig);
        } catch (Exception e) {
            log.error("手动同步学校异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从URL中提取学校ID
     *
     * @param url 高考网URL
     * @return 学校ID
     */
    private String extractSchoolIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        if (url.contains("/school/")) {
            String[] parts = url.split("/school/");
            if (parts.length > 1) {
                String idPart = parts[1];
                // 如果有其他后缀，需要去除
                if (idPart.contains("/")) {
                    return idPart.split("/")[0];
                }
                return idPart;
            }
        }
        return null;
    }

    /**
     * 更新同步失败状态
     *
     * @param config 配置
     */
    private void updateSyncFailed(SchoolSyncConfig config) {
        config.setSyncStatus(3); // 同步失败
        config.setLastSyncTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(config);
    }
} 