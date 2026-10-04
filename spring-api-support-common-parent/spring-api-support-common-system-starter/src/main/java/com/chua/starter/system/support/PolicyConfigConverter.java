package com.chua.starter.system.support;

import com.chua.starter.entity.system.SysAccountBlackwhite;
import com.chua.starter.entity.system.SysIpBlackwhite;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.entity.system.SysRateLimitRule;
import com.chua.starter.mybatis.pojo.SysBase;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一策略配置行与各类型传输对象之间的转换器。
 *
 * <p>{@code sys_firewall_config} 用统一的列承载所有策略类型，接口层仍按类型返回各自的结构。
 * 本转换器是两者之间唯一的映射点，也是各历史取值方向归一的唯一位置。</p>
 *
 * <p><b>取值方向归一表</b>（务必与迁移 SQL 保持一致）：</p>
 * <table border="1">
 *   <caption>取值方向归一</caption>
 *   <tr><th>类型</th><th>旧接口字段</th><th>旧取值</th><th>统一表列</th><th>统一取值</th></tr>
 *   <tr><td>IP</td><td>sysIpType</td><td>0白/1黑</td><td>listType</td><td>1黑/2白</td></tr>
 *   <tr><td>IP</td><td>sysIpStatus</td><td>0启用/1禁用</td><td>status</td><td>1启用/0禁用</td></tr>
 *   <tr><td>ACCOUNT</td><td>type</td><td>1黑/2白</td><td>listType</td><td>1黑/2白（同向）</td></tr>
 *   <tr><td>ACCOUNT</td><td>status</td><td>1启用/0禁用</td><td>status</td><td>1启用/0禁用（同向）</td></tr>
 *   <tr><td>RATE_LIMIT</td><td>enabled</td><td>1启用/0禁用</td><td>status</td><td>1启用/0禁用（同向）</td></tr>
 * </table>
 *
 * @since 4.0.0.50
 * @author CH
 */
public final class PolicyConfigConverter {

    /**
     * IP 旧口径：类型 0 白名单
     */
    public static final int IP_TYPE_WHITE = 0;
    /**
     * IP 旧口径：类型 1 黑名单
     */
    public static final int IP_TYPE_BLACK = 1;
    /**
     * IP 旧口径：状态 0 启用
     */
    public static final int IP_STATUS_ENABLED = 0;
    /**
     * IP 旧口径：状态 1 禁用
     */
    public static final int IP_STATUS_DISABLED = 1;

    private PolicyConfigConverter() {
    }

    // ==================== IP ====================

    /**
     * 统一表行转 IP 传输对象。
     *
     * @param row 统一表行
     * @return IP 传输对象
     */
    public static SysIpBlackwhite toIp(SysPolicyConfig row) {
        if (null == row) {
            return null;
        }
        SysIpBlackwhite dto = new SysIpBlackwhite();
        dto.setSysIpId(null == row.getSysPolicyConfigId() ? null : row.getSysPolicyConfigId().intValue());
        dto.setSysIpType(toIpType(row.getListType()));
        dto.setSysIpIp(row.getBizKey());
        dto.setSysIpStatus(toIpStatus(row.getStatus()));
        dto.setSysIpRemark(row.getRemark());
        copyAudit(row, dto);
        return dto;
    }

    /**
     * IP 传输对象转统一表行（用于新增）。
     *
     * @param dto IP 传输对象
     * @return 统一表行
     */
    public static SysPolicyConfig fromIp(SysIpBlackwhite dto) {
        if (null == dto) {
            return null;
        }
        SysPolicyConfig row = new SysPolicyConfig();
        row.setType(SysPolicyConfig.TYPE_IP);
        row.setDimension("");
        row.setListType(fromIpType(dto.getSysIpType()));
        row.setBizKey(dto.getSysIpIp());
        row.setStatus(fromIpStatus(dto.getSysIpStatus()));
        row.setSort(0);
        row.setRemark(dto.getSysIpRemark());
        return row;
    }

    /**
     * IP 传输对象转统一表行（用于更新，保留主键）。
     *
     * @param dto IP 传输对象
     * @return 统一表行
     */
    public static SysPolicyConfig fromIpForUpdate(SysIpBlackwhite dto) {
        SysPolicyConfig row = fromIp(dto);
        if (null != row && null != dto.getSysIpId()) {
            row.setSysPolicyConfigId(dto.getSysIpId().longValue());
        }
        return row;
    }

    /**
     * 批量转换。
     *
     * @param rows 统一表行
     * @return IP 传输对象列表
     */
    public static List<SysIpBlackwhite> toIpList(List<SysPolicyConfig> rows) {
        List<SysIpBlackwhite> result = new ArrayList<>();
        if (null == rows) {
            return result;
        }
        for (SysPolicyConfig row : rows) {
            result.add(toIp(row));
        }
        return result;
    }

    // ==================== 限流 ====================

    /**
     * 统一表行转限流传输对象。
     *
     * @param row 统一表行
     * @return 限流传输对象
     */
    public static SysRateLimitRule toRateLimit(SysPolicyConfig row) {
        if (null == row) {
            return null;
        }
        SysRateLimitRule dto = new SysRateLimitRule();
        dto.setSysRateLimitRuleId(row.getSysPolicyConfigId());
        dto.setDimension(row.getDimension());
        dto.setTarget(row.getBizKey());
        dto.setQps(row.getQps());
        dto.setBurstSize(row.getBurstSize());
        dto.setEnabled(row.getStatus());
        dto.setRemark(row.getRemark());
        copyAudit(row, dto);
        return dto;
    }

    /**
     * 限流传输对象转统一表行（用于新增）。
     *
     * @param dto 限流传输对象
     * @return 统一表行
     */
    public static SysPolicyConfig fromRateLimit(SysRateLimitRule dto) {
        if (null == dto) {
            return null;
        }
        SysPolicyConfig row = new SysPolicyConfig();
        row.setType(SysPolicyConfig.TYPE_RATE_LIMIT);
        row.setDimension(null == dto.getDimension() ? "" : dto.getDimension());
        row.setListType(SysPolicyConfig.LIST_NONE);
        row.setBizKey(dto.getTarget());
        row.setStatus(dto.getEnabled());
        row.setQps(dto.getQps());
        row.setBurstSize(dto.getBurstSize());
        row.setSort(0);
        row.setRemark(dto.getRemark());
        return row;
    }

    /**
     * 限流传输对象转统一表行（用于更新，保留主键）。
     *
     * @param dto 限流传输对象
     * @return 统一表行
     */
    public static SysPolicyConfig fromRateLimitForUpdate(SysRateLimitRule dto) {
        SysPolicyConfig row = fromRateLimit(dto);
        if (null != row && null != dto.getSysRateLimitRuleId()) {
            row.setSysPolicyConfigId(dto.getSysRateLimitRuleId());
        }
        return row;
    }

    /**
     * 批量转换。
     *
     * @param rows 统一表行
     * @return 限流传输对象列表
     */
    public static List<SysRateLimitRule> toRateLimitList(List<SysPolicyConfig> rows) {
        List<SysRateLimitRule> result = new ArrayList<>();
        if (null == rows) {
            return result;
        }
        for (SysPolicyConfig row : rows) {
            result.add(toRateLimit(row));
        }
        return result;
    }

    // ==================== 账号 ====================

    /**
     * 统一表行转账号传输对象。
     *
     * @param row 统一表行
     * @return 账号传输对象
     */
    public static SysAccountBlackwhite toAccount(SysPolicyConfig row) {
        if (null == row) {
            return null;
        }
        SysAccountBlackwhite dto = new SysAccountBlackwhite();
        dto.setSysAccountBwId(row.getSysPolicyConfigId());
        dto.setAccount(row.getBizKey());
        dto.setType(row.getListType());
        dto.setStatus(row.getStatus());
        dto.setRemark(row.getRemark());
        copyAudit(row, dto);
        return dto;
    }

    /**
     * 账号传输对象转统一表行（用于新增）。
     *
     * @param dto 账号传输对象
     * @return 统一表行
     */
    public static SysPolicyConfig fromAccount(SysAccountBlackwhite dto) {
        if (null == dto) {
            return null;
        }
        SysPolicyConfig row = new SysPolicyConfig();
        row.setType(SysPolicyConfig.TYPE_ACCOUNT);
        row.setDimension("");
        row.setListType(dto.getType());
        row.setBizKey(dto.getAccount());
        row.setStatus(dto.getStatus());
        row.setSort(0);
        row.setRemark(dto.getRemark());
        return row;
    }

    /**
     * 账号传输对象转统一表行（用于更新，保留主键）。
     *
     * @param dto 账号传输对象
     * @return 统一表行
     */
    public static SysPolicyConfig fromAccountForUpdate(SysAccountBlackwhite dto) {
        SysPolicyConfig row = fromAccount(dto);
        if (null != row && null != dto.getSysAccountBwId()) {
            row.setSysPolicyConfigId(dto.getSysAccountBwId());
        }
        return row;
    }

    /**
     * 批量转换。
     *
     * @param rows 统一表行
     * @return 账号传输对象列表
     */
    public static List<SysAccountBlackwhite> toAccountList(List<SysPolicyConfig> rows) {
        List<SysAccountBlackwhite> result = new ArrayList<>();
        if (null == rows) {
            return result;
        }
        for (SysPolicyConfig row : rows) {
            result.add(toAccount(row));
        }
        return result;
    }

    // ==================== 内部工具 ====================

    /**
     * 统一表 listType(1黑/2白) 转 IP 旧口径(0白/1黑)。
     *
     * @param listType 名单方向
     * @return IP 旧口径类型
     */
    public static int toIpType(Integer listType) {
        return null == listType || listType == SysPolicyConfig.LIST_BLACK ? IP_TYPE_BLACK : IP_TYPE_WHITE;
    }

    /**
     * IP 旧口径(0白/1黑) 转统一表 listType(1黑/2白)。
     *
     * @param ipType IP 旧口径类型
     * @return 名单方向
     */
    public static int fromIpType(Integer ipType) {
        return null == ipType || ipType == IP_TYPE_BLACK ? SysPolicyConfig.LIST_BLACK : SysPolicyConfig.LIST_WHITE;
    }

    /**
     * 统一表 status(1启用/0禁用) 转 IP 旧口径(0启用/1禁用)。
     *
     * @param status 统一状态
     * @return IP 旧口径状态
     */
    public static int toIpStatus(Integer status) {
        return null == status || status == SysPolicyConfig.STATUS_ENABLED ? IP_STATUS_ENABLED : IP_STATUS_DISABLED;
    }

    /**
     * IP 旧口径(0启用/1禁用) 转统一表 status(1启用/0禁用)。
     *
     * <p><b>null 必须原样返回 null</b>，不能塌缩成「禁用」。更新场景用
     * 「null = 本次不修改该字段」表达部分更新；若在这里把 null 变成禁用，
     * 调用方的 {@code if (null == status) 沿用旧值} 就成了死代码
     * （返回原始 int 时永远非 null），省略状态字段的更新会静默把名单停用。
     * 新增场景的默认值由 {@code saveIp} 在转换前补齐。</p>
     *
     * @param ipStatus IP 旧口径状态，null 表示未提供
     * @return 统一状态；未提供时返回 null
     */
    public static Integer fromIpStatus(Integer ipStatus) {
        if (null == ipStatus) {
            return null;
        }
        return ipStatus == IP_STATUS_DISABLED ? SysPolicyConfig.STATUS_DISABLED : SysPolicyConfig.STATUS_ENABLED;
    }

    /**
     * 复制审计字段。
     *
     * @param from 源行
     * @param to   目标对象
     */
    private static void copyAudit(SysPolicyConfig from, SysBase to) {
        to.setCreateName(from.getCreateName());
        to.setCreateBy(from.getCreateBy());
        to.setCreateTime(from.getCreateTime());
        to.setUpdateName(from.getUpdateName());
        to.setUpdateBy(from.getUpdateBy());
        to.setUpdateTime(from.getUpdateTime());
    }
}
