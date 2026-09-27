package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysIpBlackwhite;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysPolicyConfigMapper;
import com.chua.starter.system.service.SysIpBlackwhiteService;
import com.chua.starter.system.support.IpPolicyCacheProjector;
import com.chua.starter.system.support.PolicyConfigConverter;
import com.chua.starter.system.util.IpCidrMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * IP 黑白名单服务实现
 *
 * <p>读写统一策略配置表 {@code sys_policy_config}（{@code type='IP'}），
 * 不再使用独立的 IP 名单表。IP 名单的领域逻辑（CIDR 归一、缓存投影、按 IP 判定放行）保持不变。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysIpBlackwhiteServiceImpl implements SysIpBlackwhiteService {

    /**
     * 统一策略配置映射器
     */
    private final SysPolicyConfigMapper sysPolicyConfigMapper;

    /**
     * IP 策略缓存投影器
     */
    private final IpPolicyCacheProjector ipPolicyCacheProjector;

    @Override
    public IPage<SysIpBlackwhite> page(Query<SysIpBlackwhite> query, SysIpBlackwhite entity) {
        // 分页参数取自 Query，实体类型需换成统一表行，故不能用 query.createPage()
        Page<SysPolicyConfig> page = new Page<>(query.getPage(), query.getPageSize());
        sysPolicyConfigMapper.selectPage(page,
                Wrappers.<SysPolicyConfig>lambdaQuery()
                        .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_IP)
                        .eq(null != entity.getSysIpType(), SysPolicyConfig::getListType,
                                PolicyConfigConverter.fromIpType(entity.getSysIpType()))
                        .eq(null != entity.getSysIpStatus(), SysPolicyConfig::getStatus,
                                PolicyConfigConverter.fromIpStatus(entity.getSysIpStatus()))
                        .like(StringUtils.isNotBlank(entity.getSysIpIp()), SysPolicyConfig::getBizKey, entity.getSysIpIp())
                        .orderByDesc(SysPolicyConfig::getCreateTime));
        Page<SysIpBlackwhite> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(PolicyConfigConverter.toIpList(page.getRecords()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysIpBlackwhite> saveIp(SysIpBlackwhite entity) {
        if (entity == null) {
            return ReturnResult.illegal("记录不能为空");
        }
        if (entity.getSysIpStatus() == null) {
            entity.setSysIpStatus(PolicyConfigConverter.IP_STATUS_ENABLED);
        }
        if (!isValidType(entity.getSysIpType()) || !isValidStatus(entity.getSysIpStatus())) {
            return ReturnResult.illegal("IP策略类型或状态错误");
        }
        try {
            entity.setSysIpIp(IpCidrMatcher.canonicalize(entity.getSysIpIp()));
        } catch (IllegalArgumentException e) {
            return ReturnResult.illegal(e.getMessage());
        }
        SysPolicyConfig row = PolicyConfigConverter.fromIp(entity);
        if (1 != sysPolicyConfigMapper.insert(row)) {
            return ReturnResult.error("保存失败");
        }
        entity.setSysIpId(row.getSysPolicyConfigId().intValue());
        if (ipPolicyCacheProjector != null) {
            ipPolicyCacheProjector.syncAfterCommit(row, null, false);
        }
        return ReturnResult.ok(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateIp(SysIpBlackwhite entity) {
        if (entity == null || entity.getSysIpId() == null) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig previous = getRow(entity.getSysIpId().longValue());
        if (previous == null) {
            return ReturnResult.illegal("记录不存在");
        }
        SysIpBlackwhite candidate = merge(previous, entity);
        if (!isValidType(candidate.getSysIpType()) || !isValidStatus(candidate.getSysIpStatus())) {
            return ReturnResult.illegal("IP策略类型或状态错误");
        }
        try {
            if (entity.getSysIpIp() != null) {
                entity.setSysIpIp(IpCidrMatcher.canonicalize(entity.getSysIpIp()));
            }
        } catch (IllegalArgumentException e) {
            return ReturnResult.illegal(e.getMessage());
        }
        SysPolicyConfig update = PolicyConfigConverter.fromIpForUpdate(entity);
        update.setType(SysPolicyConfig.TYPE_IP);
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
        if (ipPolicyCacheProjector != null) {
            ipPolicyCacheProjector.syncAfterCommit(getRow(entity.getSysIpId().longValue()), previous, false);
        }
        return ReturnResult.of(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteIp(Integer ipId) {
        if (ipId == null) {
            return ReturnResult.illegal("记录不存在");
        }
        SysPolicyConfig previous = getRow(ipId.longValue());
        if (previous == null) {
            return ReturnResult.illegal("记录不存在");
        }
        if (1 != sysPolicyConfigMapper.deleteById(ipId.longValue())) {
            return ReturnResult.of(false);
        }
        if (ipPolicyCacheProjector != null) {
            ipPolicyCacheProjector.syncAfterCommit(null, previous, true);
        }
        return ReturnResult.of(true);
    }

    @Override
    public boolean checkIp(String ip) {
        if (StringUtils.isBlank(ip) || ipPolicyCacheProjector == null) {
            return true;
        }
        String clientIp = ip.split(",")[0].trim();
        try {
            // 黑名单优先：命中即拒绝，白名单不能覆盖
            if (ipPolicyCacheProjector.matches(PolicyConfigConverter.IP_TYPE_BLACK, clientIp)) {
                return false;
            }
            // 白名单与「未列入任何名单」一样放行。
            // 白名单的语义是「跳过限流 / 跳过访问拦截」，不是准入名单；
            // 若这里改成 !hasWhitelist()，配一条白名单就会把其余所有 IP 全拒，
            // 且与 PolicyRateLimitFilter 的白名单语义、策略页文案都相矛盾。
            return true;
        } catch (Exception e) {
            log.warn("校验 IP 策略缓存失败，放行: ip={}", clientIp, e);
            return true;
        }
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
                .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_IP));
    }

    /**
     * 合并更新字段，未提供的字段沿用旧值。
     *
     * @param previous 旧行
     * @param update   更新内容
     * @return 合并后的传输对象
     */
    private SysIpBlackwhite merge(SysPolicyConfig previous, SysIpBlackwhite update) {
        SysIpBlackwhite candidate = new SysIpBlackwhite();
        candidate.setSysIpType(update.getSysIpType() == null
                ? PolicyConfigConverter.toIpType(previous.getListType()) : update.getSysIpType());
        candidate.setSysIpStatus(update.getSysIpStatus() == null
                ? PolicyConfigConverter.toIpStatus(previous.getStatus()) : update.getSysIpStatus());
        candidate.setSysIpIp(update.getSysIpIp() == null ? previous.getBizKey() : update.getSysIpIp());
        return candidate;
    }

    /**
     * 校验 IP 名单方向。
     *
     * @param type 类型
     * @return 是否合法
     */
    private boolean isValidType(Integer type) {
        return type != null && (type == PolicyConfigConverter.IP_TYPE_WHITE || type == PolicyConfigConverter.IP_TYPE_BLACK);
    }

    /**
     * 校验 IP 名单状态。
     *
     * @param status 状态
     * @return 是否合法
     */
    private boolean isValidStatus(Integer status) {
        return status != null && (status == PolicyConfigConverter.IP_STATUS_ENABLED || status == PolicyConfigConverter.IP_STATUS_DISABLED);
    }

    /**
     * 供启动重建使用的启用中的 IP 名单。
     *
     * @return 启用中的 IP 名单
     */
    public List<SysPolicyConfig> listEnabledRows() {
        return sysPolicyConfigMapper.selectList(Wrappers.<SysPolicyConfig>lambdaQuery()
                .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_IP)
                .eq(SysPolicyConfig::getStatus, SysPolicyConfig.STATUS_ENABLED));
    }
}
