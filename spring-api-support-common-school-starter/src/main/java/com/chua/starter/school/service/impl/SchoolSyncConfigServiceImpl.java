package com.chua.starter.school.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.school.dto.SchoolSyncConfigDTO;
import com.chua.starter.school.entity.SchoolSyncConfig;
import com.chua.starter.school.mapper.SchoolSyncConfigMapper;
import com.chua.starter.school.provider.SchoolSyncProvider;
import com.chua.starter.school.service.SchoolSyncConfigService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 学校同步配置Service实现类
 *
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class SchoolSyncConfigServiceImpl extends ServiceImpl<SchoolSyncConfigMapper, SchoolSyncConfig> implements SchoolSyncConfigService {

    private static final Logger log = LoggerFactory.getLogger(SchoolSyncConfigServiceImpl.class);
    private final SchoolSyncConfigMapper syncConfigMapper;

    public static SchoolSyncProvider getProvider(String schoolSyncConfigSyncType) {
        SchoolSyncProvider schoolSyncProvider = ServiceProvider.of(SchoolSyncProvider.class).getNewExtension(schoolSyncConfigSyncType);
        if (null == schoolSyncProvider) {
            log.warn("未找到对应的同步提供者: {}", schoolSyncConfigSyncType);
            return null;
        }

        log.info("找到对应的同步提供者: {} => {}", schoolSyncConfigSyncType, schoolSyncProvider.getClass().getName());
        return schoolSyncProvider;
    }

    /**
     * 分页查询同步配置
     *
     * @param query 查询条件
     * @return 同步配置列表
     */
    @Override
    public ReturnPageResult<SchoolSyncConfig> pageSyncConfig(SchoolSyncConfigDTO query) {
        IPage<SchoolSyncConfig> configPage = syncConfigMapper.selectSyncConfigPage(query.createFullPage(), query);
        return ReturnPageResultUtils.ok(configPage);
    }

    /**
     * 保存同步配置
     *
     * @param config 同步配置
     * @return 是否保存成功
     */
    @Override
    public ReturnResult<Boolean> saveSyncConfig(SchoolSyncConfig config) {
        if (config == null) {
            return ReturnResult.fail("同步配置不能为空");
        }
        config.setSchoolSyncConfigId(null);
        boolean result = save(config);
        return result ? ReturnResult.ok(true) : ReturnResult.fail("保存同步配置失败");
    }

    /**
     * 更新同步配置
     *
     * @param config 同步配置
     * @return 是否更新成功
     */
    @Override
    public ReturnResult<Boolean> updateSyncConfig(SchoolSyncConfig config) {
        if (config == null) {
            return ReturnResult.fail("同步配置不能为空");
        }
        boolean result = updateById(config);
        return result ? ReturnResult.ok(true) : ReturnResult.fail("更新同步配置失败");
    }

    /**
     * 删除同步配置
     *
     * @param id 配置ID
     * @return 是否删除成功
     */
    @Override
    public ReturnResult<Boolean> deleteSyncConfig(Long id) {
        if (id == null) {
            return ReturnResult.fail("配置ID不能为空");
        }
        boolean result = removeById(id);
        return result ? ReturnResult.ok(true) : ReturnResult.fail("删除同步配置失败");
    }

    @Override
    public boolean executeSync(Long schoolSyncConfigId) {
        try {
            // 获取同步配置
            SchoolSyncConfig config = getById(schoolSyncConfigId);
            if (config == null) {
                log.error("同步配置不存在: {}", schoolSyncConfigId);
                return false;
            }

            // 检查同步状态
            if (null != config.getSchoolSyncConfigSyncStatus() && 1 == config.getSchoolSyncConfigSyncStatus()) {
                log.error("同步配置状态不正确，当前状态: {}", config.getSchoolSyncConfigSyncStatus());
                return false;
            }

            // 获取同步提供者
            SchoolSyncProvider provider = getProvider(config.getSchoolSyncConfigType());
            if (provider == null) {
                log.error("未找到对应的同步提供者: {}", config.getSchoolSyncConfigSyncType());
                return false;
            }

            // 更新同步状态为进行中
            config.setSchoolSyncConfigSyncStatus(1);
            config.setUpdateTime(LocalDateTime.now());
            updateById(config);

            // 执行同步
            boolean result = provider.syncAllSchoolData(config);

            // 更新同步状态
            config.setSchoolSyncConfigSyncStatus(result ? 2 : 3);
            config.setUpdateTime(LocalDateTime.now());
            updateById(config);

            return result;
        } catch (Exception e) {
            log.error("执行同步异常: {}", e.getMessage(), e);
            return false;
        }
    }

}