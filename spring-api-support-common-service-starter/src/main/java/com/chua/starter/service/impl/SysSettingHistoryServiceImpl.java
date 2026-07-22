package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.service.SysSettingHistory;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.SysSettingHistoryService;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.mapper.SysSettingHistoryMapper;
import com.chua.starter.service.mapper.SysSettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 系统配置变更历史服务实现类
 *
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@Service
public class SysSettingHistoryServiceImpl extends ServiceImpl<SysSettingHistoryMapper, SysSettingHistory> 
        implements SysSettingHistoryService {

    @Autowired
    private SysSettingMapper sysSettingMapper;

    @Autowired
    @Lazy
    private SysSettingService sysSettingService;

    /**
     * 操作类型常量
     */
    private static final String OP_CREATE = "CREATE";
    private static final String OP_UPDATE = "UPDATE";
    private static final String OP_DELETE = "DELETE";
    private static final String OP_IMPORT = "IMPORT";
    private static final String OP_ROLLBACK = "ROLLBACK";

    @Override
    public void recordCreate(SysSetting sysSetting, String operatorId, String operatorName, String batchNo) {
        SysSettingHistory history = new SysSettingHistory();
        history.setSysSettingId(sysSetting.getSysSettingId());
        history.setSysSettingName(sysSetting.getSysSettingName());
        history.setSysSettingGroup(sysSetting.getSysSettingGroup());
        history.setSysSettingOldValue(null);
        history.setSysSettingNewValue(sysSetting.getSysSettingValue());
        history.setSysSettingOperation(OP_CREATE);
        history.setSysSettingOperatorId(operatorId);
        history.setSysSettingOperatorName(operatorName);
        history.setSysSettingBatchNo(batchNo);
        history.setSysSettingRemark("新增配置");
        baseMapper.insert(history);
        log.info("记录配置新增历史: settingId={}, name={}", sysSetting.getSysSettingId(), sysSetting.getSysSettingName());
    }

    @Override
    public void recordUpdate(SysSetting oldSetting, SysSetting newSetting, String operatorId, String operatorName, String batchNo) {
        // 只有值变化时才记录
        if (StringUtils.equals(oldSetting.getSysSettingValue(), newSetting.getSysSettingValue())) {
            return;
        }
        
        SysSettingHistory history = new SysSettingHistory();
        history.setSysSettingId(oldSetting.getSysSettingId());
        history.setSysSettingName(oldSetting.getSysSettingName());
        history.setSysSettingGroup(oldSetting.getSysSettingGroup());
        history.setSysSettingOldValue(oldSetting.getSysSettingValue());
        history.setSysSettingNewValue(newSetting.getSysSettingValue());
        history.setSysSettingOperation(OP_UPDATE);
        history.setSysSettingOperatorId(operatorId);
        history.setSysSettingOperatorName(operatorName);
        history.setSysSettingBatchNo(batchNo);
        history.setSysSettingRemark("更新配置");
        baseMapper.insert(history);
        log.info("记录配置更新历史: settingId={}, name={}, oldValue={}, newValue={}", 
                oldSetting.getSysSettingId(), oldSetting.getSysSettingName(), 
                oldSetting.getSysSettingValue(), newSetting.getSysSettingValue());
    }

    @Override
    public void recordDelete(SysSetting sysSetting, String operatorId, String operatorName, String batchNo) {
        SysSettingHistory history = new SysSettingHistory();
        history.setSysSettingId(sysSetting.getSysSettingId());
        history.setSysSettingName(sysSetting.getSysSettingName());
        history.setSysSettingGroup(sysSetting.getSysSettingGroup());
        history.setSysSettingOldValue(sysSetting.getSysSettingValue());
        history.setSysSettingNewValue(null);
        history.setSysSettingOperation(OP_DELETE);
        history.setSysSettingOperatorId(operatorId);
        history.setSysSettingOperatorName(operatorName);
        history.setSysSettingBatchNo(batchNo);
        history.setSysSettingRemark("删除配置");
        baseMapper.insert(history);
        log.info("记录配置删除历史: settingId={}, name={}", sysSetting.getSysSettingId(), sysSetting.getSysSettingName());
    }

    @Override
    public void recordImport(List<SysSetting> settings, String operatorId, String operatorName, String batchNo) {
        for (SysSetting setting : settings) {
            SysSettingHistory history = new SysSettingHistory();
            history.setSysSettingId(setting.getSysSettingId());
            history.setSysSettingName(setting.getSysSettingName());
            history.setSysSettingGroup(setting.getSysSettingGroup());
            history.setSysSettingOldValue(null);
            history.setSysSettingNewValue(setting.getSysSettingValue());
            history.setSysSettingOperation(OP_IMPORT);
            history.setSysSettingOperatorId(operatorId);
            history.setSysSettingOperatorName(operatorName);
            history.setSysSettingBatchNo(batchNo);
            history.setSysSettingRemark("导入新增配置");
            baseMapper.insert(history);
        }
        log.info("记录配置导入历史: batchNo={}, count={}", batchNo, settings.size());
    }

    @Override
    public void recordImportUpdate(SysSetting oldSetting, SysSetting newSetting, String operatorId, String operatorName, String batchNo) {
        SysSettingHistory history = new SysSettingHistory();
        history.setSysSettingId(newSetting.getSysSettingId());
        history.setSysSettingName(newSetting.getSysSettingName());
        history.setSysSettingGroup(newSetting.getSysSettingGroup());
        history.setSysSettingOldValue(oldSetting.getSysSettingValue());
        history.setSysSettingNewValue(newSetting.getSysSettingValue());
        history.setSysSettingOperation(OP_IMPORT);
        history.setSysSettingOperatorId(operatorId);
        history.setSysSettingOperatorName(operatorName);
        history.setSysSettingBatchNo(batchNo);
        history.setSysSettingRemark("导入覆盖配置");
        baseMapper.insert(history);
        log.info("记录配置导入覆盖历史: settingId={}, name={}, oldValue={}, newValue={}",
                newSetting.getSysSettingId(), newSetting.getSysSettingName(),
                oldSetting.getSysSettingValue(), newSetting.getSysSettingValue());
    }

    @Override
    public IPage<SysSettingHistory> pageHistory(Query<SysSettingHistory> query, Integer settingId, String group) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysSettingHistory>lambdaQuery()
                .eq(settingId != null, SysSettingHistory::getSysSettingId, settingId)
                .eq(StringUtils.isNotBlank(group), SysSettingHistory::getSysSettingGroup, group)
                .orderByDesc(SysSettingHistory::getCreateTime)
        );
    }

    @Override
    public List<SysSettingHistory> listByBatchNo(String batchNo) {
        return baseMapper.selectList(Wrappers.<SysSettingHistory>lambdaQuery()
                .eq(SysSettingHistory::getSysSettingBatchNo, batchNo)
                .orderByDesc(SysSettingHistory::getCreateTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> rollbackToBatch(String batchNo, String operatorId, String operatorName) {
        List<SysSettingHistory> histories = listByBatchNo(batchNo);
        if (histories.isEmpty()) {
            return ReturnResult.fail("未找到该批次的变更记录");
        }

        String rollbackBatchNo = UUID.randomUUID().toString().replace("-", "");
        
        for (SysSettingHistory history : histories) {
            rollbackSingleHistory(history, operatorId, operatorName, rollbackBatchNo);
        }
        
        log.info("批量回滚完成: originalBatchNo={}, rollbackBatchNo={}, count={}", batchNo, rollbackBatchNo, histories.size());
        return ReturnResult.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> rollbackToHistory(Long historyId, String operatorId, String operatorName) {
        SysSettingHistory history = baseMapper.selectById(historyId);
        if (history == null) {
            return ReturnResult.fail("历史记录不存在");
        }

        String rollbackBatchNo = UUID.randomUUID().toString().replace("-", "");
        rollbackSingleHistory(history, operatorId, operatorName, rollbackBatchNo);
        
        log.info("单条回滚完成: historyId={}, rollbackBatchNo={}", historyId, rollbackBatchNo);
        return ReturnResult.ok(true);
    }

    /**
     * 回滚单条历史记录
     */
    private void rollbackSingleHistory(SysSettingHistory history, String operatorId, String operatorName, String rollbackBatchNo) {
        String operation = history.getSysSettingOperation();
        Integer settingId = history.getSysSettingId();
        
        switch (operation) {
            case OP_CREATE, OP_IMPORT -> {
                // 新增/导入的回滚：删除配置
                SysSetting setting = sysSettingMapper.selectById(settingId);
                if (setting != null) {
                    sysSettingMapper.deleteById(settingId);
                    sysSettingService.clearCache(setting);
                    recordRollback(setting, null, operatorId, operatorName, rollbackBatchNo, "回滚新增操作");
                }
            }
            case OP_UPDATE -> {
                // 更新的回滚：恢复旧值
                SysSetting setting = sysSettingMapper.selectById(settingId);
                if (setting != null) {
                    String currentValue = setting.getSysSettingValue();
                    setting.setSysSettingValue(history.getSysSettingOldValue());
                    sysSettingMapper.updateById(setting);
                    sysSettingService.clearCache(setting);
                    recordRollback(setting, currentValue, operatorId, operatorName, rollbackBatchNo, "回滚更新操作");
                }
            }
            case OP_DELETE -> {
                // 删除的回滚：恢复配置
                SysSetting newSetting = new SysSetting();
                newSetting.setSysSettingName(history.getSysSettingName());
                newSetting.setSysSettingGroup(history.getSysSettingGroup());
                newSetting.setSysSettingValue(history.getSysSettingOldValue());
                newSetting.setSysSettingStatus(1);
                sysSettingMapper.insert(newSetting);
                sysSettingService.clearCache(newSetting);
                recordRollback(newSetting, null, operatorId, operatorName, rollbackBatchNo, "回滚删除操作");
            }
            default -> log.warn("不支持回滚的操作类型: {}", operation);
        }
    }

    /**
     * 记录回滚历史
     */
    private void recordRollback(SysSetting setting, String oldValue, String operatorId, String operatorName, String batchNo, String remark) {
        SysSettingHistory history = new SysSettingHistory();
        history.setSysSettingId(setting.getSysSettingId());
        history.setSysSettingName(setting.getSysSettingName());
        history.setSysSettingGroup(setting.getSysSettingGroup());
        history.setSysSettingOldValue(oldValue);
        history.setSysSettingNewValue(setting.getSysSettingValue());
        history.setSysSettingOperation(OP_ROLLBACK);
        history.setSysSettingOperatorId(operatorId);
        history.setSysSettingOperatorName(operatorName);
        history.setSysSettingBatchNo(batchNo);
        history.setSysSettingRemark(remark);
        baseMapper.insert(history);
    }
}
