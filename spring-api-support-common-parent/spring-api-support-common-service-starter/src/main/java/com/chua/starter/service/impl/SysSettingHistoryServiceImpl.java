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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * 系统配置变更历史服务实现类
 *
 * @since 2024/12/20
 * @author CH
 */
@Service
public class SysSettingHistoryServiceImpl extends ServiceImpl<SysSettingHistoryMapper, SysSettingHistory>
        implements SysSettingHistoryService {

    /**
     * 日志
    */
    private static final Logger log = LoggerFactory.getLogger(SysSettingHistoryServiceImpl.class);

    @Autowired
    /**
     * SYS设置映射器
    */
    private SysSettingMapper sysSettingMapper;

    @Autowired
    @Lazy
    /**
     * SYS设置服务
    */
    private SysSettingService sysSettingService;

    /**
     * 操作类型常量
     */
    private static final String OP_CREATE = "CREATE";
    /**
     * Op_更新
    */
    private static final String OP_UPDATE = "UPDATE";
    /**
     * Op_删除
    */
    private static final String OP_DELETE = "DELETE";
    /**
     * Op_导入
    */
    private static final String OP_IMPORT = "IMPORT";
    /**
     * Op_rollback
    */
    private static final String OP_ROLLBACK = "ROLLBACK";

    @Override
    /**
     * Record创建
    */
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
    /**
     * Record更新
    */
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
    /**
     * Record删除
    */
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
    /**
     * record导入
    */
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
    /**
     * record导入更新
    */
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
    /**
     * page历史
    */
    public IPage<SysSettingHistory> pageHistory(Query<SysSettingHistory> query, Integer settingId, String group) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysSettingHistory>lambdaQuery()
                .eq(settingId != null, SysSettingHistory::getSysSettingId, settingId)
                .eq(StringUtils.isNotBlank(group), SysSettingHistory::getSysSettingGroup, group)
                .orderByDesc(SysSettingHistory::getCreateTime)
        );
    }

    @Override
    /**
     * 列表bybatchno
    */
    public List<SysSettingHistory> listByBatchNo(String batchNo) {
        return baseMapper.selectList(Wrappers.<SysSettingHistory>lambdaQuery()
                .eq(SysSettingHistory::getSysSettingBatchNo, batchNo)
                .orderByDesc(SysSettingHistory::getCreateTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 回滚转为batch
     *
     * @param batchNo batchNo
     * @param operatorId 操作符id
     * @param operatorName 操作符名称
     * @return rollback转为batch的结果
     */
    public ReturnResult<Boolean> rollbackToBatch(String batchNo, String operatorId, String operatorName) {
        List<SysSettingHistory> histories = listByBatchNo(batchNo);
        if (histories.isEmpty()) {
            return ReturnResult.error("未找到该批次的变更记录");
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
    /**
     * 回滚转为历史
     *
     * @param historyId 历史id
     * @param operatorId 操作符id
     * @param operatorName 操作符名称
     * @return rollback转为历史的结果
     */
    public ReturnResult<Boolean> rollbackToHistory(Long historyId, String operatorId, String operatorName) {
        SysSettingHistory history = baseMapper.selectById(historyId);
        if (history == null) {
            return ReturnResult.error("历史记录不存在");
        }

        String rollbackBatchNo = UUID.randomUUID().toString().replace("-", "");
        rollbackSingleHistory(history, operatorId, operatorName, rollbackBatchNo);
        
        log.info("单条回滚完成: historyId={}, rollbackBatchNo={}", historyId, rollbackBatchNo);
        return ReturnResult.ok(true);
    }

    /**
     * 回滚单条历史记录
     * @param history 历史
     * @param operatorId 操作符id
     * @param operatorName 操作符名称
     * @param rollbackBatchNo rollbackBatchNo
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
     * @param setting setting
     * @param oldValue 旧值
     * @param operatorId 操作符id
     * @param operatorName 操作符名称
     * @param batchNo batchNo
     * @param remark 评论
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
