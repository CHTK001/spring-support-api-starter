package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.SystemSoftRecord;
import com.chua.starter.monitor.mapper.SystemSoftRecordMapper;
import com.chua.starter.monitor.service.docker.SystemSoftRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 系统软件操作记录服务实现类
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSoftRecordServiceImpl extends ServiceImpl<SystemSoftRecordMapper, SystemSoftRecord> implements SystemSoftRecordService {

    private final SystemSoftRecordMapper systemSoftRecordMapper;

    @Override
    public IPage<SystemSoftRecord> selectRecordPage(Page<SystemSoftRecord> page,
                                                    Integer softId,
                                                    Integer serverId,
                                                    String operationType,
                                                    Integer operationStatus,
                                                    String operationUser) {
        log.debug("分页查询操作记录列表 - 页码: {}, 每页大小: {}, 软件ID: {}, 服务器ID: {}, 操作类型: {}, 操作状态: {}, 操作用户: {}",
                page.getCurrent(), page.getSize(), softId, serverId, operationType, operationStatus, operationUser);

        // 构建查询条件
        LambdaQueryWrapper<SystemSoftRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(softId != null, SystemSoftRecord::getSystemSoftId, softId)
                .eq(serverId != null, SystemSoftRecord::getSystemServerId, serverId)
                .eq(StringUtils.hasText(operationType), SystemSoftRecord::getSystemSoftRecordOperationType, operationType)
                .eq(operationStatus != null, SystemSoftRecord::getSystemSoftRecordStatus, operationStatus)
                .eq(StringUtils.hasText(operationUser), SystemSoftRecord::getSystemSoftRecordUser, operationUser)
                .orderByDesc(SystemSoftRecord::getSystemSoftRecordTime);

        IPage<SystemSoftRecord> result = this.page(page, queryWrapper);
        log.debug("分页查询操作记录列表完成 - 总记录数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());

        return result;
    }

    @Override
    public List<SystemSoftRecord> selectBySoftId(Integer softId) {
        log.debug("根据软件ID查询操作记录列表 - 软件ID: {}", softId);
        List<SystemSoftRecord> records = systemSoftRecordMapper.selectBySoftId(softId);
        log.debug("根据软件ID查询操作记录列表完成 - 记录数: {}", records.size());
        return records;
    }

    @Override
    public List<SystemSoftRecord> selectByServerId(Integer serverId) {
        log.debug("根据服务器ID查询操作记录列表 - 服务器ID: {}", serverId);
        List<SystemSoftRecord> records = systemSoftRecordMapper.selectByServerId(serverId);
        log.debug("根据服务器ID查询操作记录列表完成 - 记录数: {}", records.size());
        return records;
    }

    @Override
    public List<SystemSoftRecord> selectByOperationType(String operationType) {
        log.debug("根据操作类型查询操作记录列表 - 操作类型: {}", operationType);
        List<SystemSoftRecord> records = systemSoftRecordMapper.selectByOperationType(operationType);
        log.debug("根据操作类型查询操作记录列表完成 - 记录数: {}", records.size());
        return records;
    }

    @Override
    public List<SystemSoftRecord> selectByOperationStatus(Integer operationStatus) {
        log.debug("根据操作状态查询操作记录列表 - 操作状态: {}", operationStatus);
        List<SystemSoftRecord> records = systemSoftRecordMapper.selectByOperationStatus(String.valueOf(operationStatus));
        log.debug("根据操作状态查询操作记录列表完成 - 记录数: {}", records.size());
        return records;
    }

    @Override
    public List<SystemSoftRecord> selectByOperationUser(String operationUser) {
        log.debug("根据操作用户查询操作记录列表 - 操作用户: {}", operationUser);
        List<SystemSoftRecord> records = systemSoftRecordMapper.selectByOperationUser(operationUser);
        log.debug("根据操作用户查询操作记录列表完成 - 记录数: {}", records.size());
        return records;
    }

    @Override
    public List<SystemSoftRecord> selectByContainerId(String containerId) {
        log.debug("根据容器ID查询操作记录列表 - 容器ID: {}", containerId);

        // 使用LambdaQueryWrapper查询
        LambdaQueryWrapper<SystemSoftRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemSoftRecord::getSystemSoftRecordContainerId, containerId)
                .orderByDesc(SystemSoftRecord::getSystemSoftRecordTime);

        List<SystemSoftRecord> records = this.list(queryWrapper);
        log.debug("根据容器ID查询操作记录列表完成 - 记录数: {}", records.size());
        return records;
    }

    @Override
    public List<SystemSoftRecord> selectFailedRecords() {
        log.debug("获取失败的操作记录列表");
        List<SystemSoftRecord> records = systemSoftRecordMapper.selectFailedRecords();
        log.debug("获取失败的操作记录列表完成 - 记录数: {}", records.size());
        return records;
    }

    @Override
    public List<SystemSoftRecord> selectSuccessRecords() {
        log.debug("获取成功的操作记录列表");
        List<SystemSoftRecord> records = systemSoftRecordMapper.selectSuccessRecords();
        log.debug("获取成功的操作记录列表完成 - 记录数: {}", records.size());
        return records;
    }

    @Override
    public List<SystemSoftRecord> selectRunningRecords() {
        log.debug("获取进行中的操作记录列表");
        List<SystemSoftRecord> records = systemSoftRecordMapper.selectRunningRecords();
        log.debug("获取进行中的操作记录列表完成 - 记录数: {}", records.size());
        return records;
    }

    @Override
    public List<SystemSoftRecord> searchByKeyword(String keyword) {
        log.debug("根据关键字搜索操作记录 - 关键字: {}", keyword);
        List<SystemSoftRecord> records = systemSoftRecordMapper.searchByKeyword(keyword);
        log.debug("根据关键字搜索操作记录完成 - 记录数: {}", records.size());
        return records;
    }

    @Override
    public SystemSoftRecord selectLatestBySoftId(Integer softId) {
        log.debug("根据软件ID获取最新的操作记录 - 软件ID: {}", softId);
        SystemSoftRecord record = systemSoftRecordMapper.selectLatestBySoftId(softId);
        log.debug("根据软件ID获取最新的操作记录完成 - 记录ID: {}", record != null ? record.getSystemSoftRecordId() : null);
        return record;
    }

    @Override
    public SystemSoftRecord selectLatestByServerId(Integer serverId) {
        log.debug("根据服务器ID获取最新的操作记录 - 服务器ID: {}", serverId);
        SystemSoftRecord record = systemSoftRecordMapper.selectLatestByServerId(serverId);
        log.debug("根据服务器ID获取最新的操作记录完成 - 记录ID: {}", record != null ? record.getSystemSoftRecordId() : null);
        return record;
    }

    @Override
    public Long countBySoftId(Integer softId) {
        log.debug("根据软件ID统计操作记录数量 - 软件ID: {}", softId);
        Long count = systemSoftRecordMapper.countBySoftId(softId);
        log.debug("根据软件ID统计操作记录数量完成 - 数量: {}", count);
        return count;
    }

    @Override
    public Long countByServerId(Integer serverId) {
        log.debug("根据服务器ID统计操作记录数量 - 服务器ID: {}", serverId);
        Long count = systemSoftRecordMapper.countByServerId(serverId);
        log.debug("根据服务器ID统计操作记录数量完成 - 数量: {}", count);
        return count;
    }

    @Override
    public Long countByOperationType(String operationType) {
        log.debug("根据操作类型统计操作记录数量 - 操作类型: {}", operationType);
        Long count = systemSoftRecordMapper.countByOperationType(operationType);
        log.debug("根据操作类型统计操作记录数量完成 - 数量: {}", count);
        return count;
    }

    @Override
    public Long countByOperationStatus(Integer operationStatus) {
        log.debug("根据操作状态统计操作记录数量 - 操作状态: {}", operationStatus);
        Long count = systemSoftRecordMapper.countByOperationStatus(String.valueOf(operationStatus));
        log.debug("根据操作状态统计操作记录数量完成 - 数量: {}", count);
        return count;
    }

    @Override
    public Long countByOperationUser(String operationUser) {
        log.debug("根据操作用户统计操作记录数量 - 操作用户: {}", operationUser);
        Long count = systemSoftRecordMapper.countByOperationUser(operationUser);
        log.debug("根据操作用户统计操作记录数量完成 - 数量: {}", count);
        return count;
    }
}