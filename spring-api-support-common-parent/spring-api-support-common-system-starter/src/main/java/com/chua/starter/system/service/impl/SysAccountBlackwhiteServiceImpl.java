package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysAccountBlackwhite;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.system.mapper.SysPolicyConfigMapper;
import com.chua.starter.system.service.SysAccountBlackwhiteService;
import com.chua.starter.system.support.PolicyCacheService;
import com.chua.starter.system.support.PolicyConfigConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账号黑白名单服务实现
 *
 * <p>读写统一策略配置表 {@code sys_firewall_config}（{@code type='ACCOUNT'}），
 * 不再使用独立的账号名单表。账号名单的取值方向与统一表一致，转换无需翻转。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysAccountBlackwhiteServiceImpl implements SysAccountBlackwhiteService {

    /**
     * 类型: 黑名单
     */
    private static final int TYPE_BLACK = SysPolicyConfig.LIST_BLACK;
    /**
     * 类型: 白名单
     */
    private static final int TYPE_WHITE = SysPolicyConfig.LIST_WHITE;
    /**
     * 启用
     */
    private static final int STATUS_ENABLED = SysPolicyConfig.STATUS_ENABLED;

    /**
     * 统一策略配置映射器
     */
    private final SysPolicyConfigMapper sysPolicyConfigMapper;

    /**
     * 策略运行时缓存
     */
    private final PolicyCacheService policyCacheService;

    @Override
    public IPage<SysAccountBlackwhite> page(Query<SysAccountBlackwhite> query, SysAccountBlackwhite entity) {
        // 分页参数取自 Query，实体类型需换成统一表行，故不能用 query.createPage()
        Page<SysPolicyConfig> page = new Page<>(query.getPage(), query.getPageSize());
        sysPolicyConfigMapper.selectPage(page,
                Wrappers.<SysPolicyConfig>lambdaQuery()
                        .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_ACCOUNT)
                        .eq(null != entity.getType(), SysPolicyConfig::getListType, entity.getType())
                        .eq(null != entity.getStatus(), SysPolicyConfig::getStatus, entity.getStatus())
                        .like(StringUtils.isNotBlank(entity.getAccount()), SysPolicyConfig::getBizKey, entity.getAccount())
                        .orderByDesc(SysPolicyConfig::getCreateTime));
        Page<SysAccountBlackwhite> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(PolicyConfigConverter.toAccountList(page.getRecords()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysAccountBlackwhite> saveAccount(SysAccountBlackwhite entity) {
        if (null == entity) {
            return ReturnResult.illegal("记录不能为空");
        }
        if (null == entity.getStatus()) {
            entity.setStatus(STATUS_ENABLED);
        }
        SysPolicyConfig row = PolicyConfigConverter.fromAccount(entity);
        if (1 != sysPolicyConfigMapper.insert(row)) {
            return ReturnResult.error("保存失败");
        }
        entity.setSysAccountBwId(row.getSysPolicyConfigId());
        syncToCache(row, null);
        return ReturnResult.ok(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateAccount(SysAccountBlackwhite entity) {
        if (null == entity || null == entity.getSysAccountBwId()) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig previous = getRow(entity.getSysAccountBwId());
        if (null == previous) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig update = PolicyConfigConverter.fromAccountForUpdate(entity);
        update.setType(SysPolicyConfig.TYPE_ACCOUNT);
        update.setDimension("");
        if (null == update.getListType()) {
            update.setListType(previous.getListType());
        }
        if (null == update.getStatus()) {
            update.setStatus(previous.getStatus());
        }
        if (null == update.getBizKey()) {
            update.setBizKey(previous.getBizKey());
        }
        if (1 != sysPolicyConfigMapper.updateById(update)) {
            return ReturnResult.of(false);
        }
        syncToCache(getRow(entity.getSysAccountBwId()), previous);
        return ReturnResult.of(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteAccount(Long id) {
        if (null == id) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig previous = getRow(id);
        if (null == previous) {
            return ReturnResult.illegal("记录不存在");
        }
        if (1 != sysPolicyConfigMapper.deleteById(id)) {
            return ReturnResult.of(false);
        }
        evictCache(previous);
        return ReturnResult.of(true);
    }

    /**
     * 按主键读取统一表行。
     *
     * @param id 主键
     * @return 统一表行
     */
    private SysPolicyConfig getRow(Long id) {
        return sysPolicyConfigMapper.selectOne(Wrappers.<SysPolicyConfig>lambdaQuery()
                .eq(SysPolicyConfig::getSysPolicyConfigId, id)
                .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_ACCOUNT));
    }

    /**
     * 同步账号黑白名单到策略缓存
     *
     * @param cur 新记录
     * @param old 旧记录(可能为 null)
     */
    private void syncToCache(SysPolicyConfig cur, SysPolicyConfig old) {
        try {
            if (null != old) {
                evictCache(old);
            }
            if (null == cur.getStatus() || cur.getStatus() == STATUS_ENABLED) {
                policyCacheService.putAccount(cur.getListType(), cur.getBizKey());
            }
        } catch (Exception e) {
            log.warn("同步账号黑白名单到策略缓存失败: {}", cur, e);
        }
    }

    /**
     * 清理策略缓存中的账号黑白名单
     *
     * @param row 记录
     */
    private void evictCache(SysPolicyConfig row) {
        try {
            policyCacheService.evictAccount(TYPE_BLACK, row.getBizKey());
            policyCacheService.evictAccount(TYPE_WHITE, row.getBizKey());
        } catch (Exception e) {
            log.warn("清理账号黑白名单策略缓存失败: {}", row, e);
        }
    }
}
