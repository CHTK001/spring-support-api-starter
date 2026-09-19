package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysIpBlackwhite;
import com.chua.starter.system.mapper.SysIpBlackwhiteMapper;
import com.chua.starter.system.service.SysIpBlackwhiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * IP黑白名单服务实现
 *
 * @since 4.0.0.43
 * @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysIpBlackwhiteServiceImpl extends ServiceImpl<SysIpBlackwhiteMapper, SysIpBlackwhite>
        implements SysIpBlackwhiteService {

    /**
     * 类型: 白名单
    */
    private static final int TYPE_WHITE = 0;
    /**
     * 类型: 黑名单
    */
    private static final int TYPE_BLACK = 1;
    /**
     * 状态: 启用
    */
    private static final int STATUS_ENABLED = 0;

    @Override
    /**
     * 分页
    */
    public IPage<SysIpBlackwhite> page(Query<SysIpBlackwhite> query, SysIpBlackwhite entity) {
        return page(query.createPage(), Wrappers.<SysIpBlackwhite>lambdaQuery()
                .eq(null != entity.getSysIpType(), SysIpBlackwhite::getSysIpType, entity.getSysIpType())
                .eq(null != entity.getSysIpStatus(), SysIpBlackwhite::getSysIpStatus, entity.getSysIpStatus())
                .like(StringUtils.isNotBlank(entity.getSysIpIp()), SysIpBlackwhite::getSysIpIp, entity.getSysIpIp())
                .orderByDesc(SysIpBlackwhite::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 保存
    */
    public ReturnResult<SysIpBlackwhite> saveIp(SysIpBlackwhite entity) {
        if (null == entity.getSysIpStatus()) {
            entity.setSysIpStatus(STATUS_ENABLED);
        }
        save(entity);
        return ReturnResult.ok(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 更新
    */
    public ReturnResult<Boolean> updateIp(SysIpBlackwhite entity) {
        if (null == entity.getSysIpId()) {
            return ReturnResult.illegal("记录不存在");
        }
        return ReturnResult.of(updateById(entity));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 删除
    */
    public ReturnResult<Boolean> deleteIp(Integer ipId) {
        if (null == ipId) {
            return ReturnResult.illegal("记录不存在");
        }
        return ReturnResult.of(removeById(ipId));
    }

    @Override
    /**
     * 校验IP
    */
    public boolean checkIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return true;
        }
        String clientIp = ip.split(",")[0].trim();
        // 1. 黑名单命中即拒绝
        List<SysIpBlackwhite> blackList = list(Wrappers.<SysIpBlackwhite>lambdaQuery()
                .eq(SysIpBlackwhite::getSysIpType, TYPE_BLACK)
                .eq(SysIpBlackwhite::getSysIpStatus, STATUS_ENABLED));
        for (SysIpBlackwhite item : blackList) {
            if (matches(clientIp, item.getSysIpIp())) {
                return false;
            }
        }
        // 2. 存在白名单时, 不在白名单则拒绝
        List<SysIpBlackwhite> whiteList = list(Wrappers.<SysIpBlackwhite>lambdaQuery()
                .eq(SysIpBlackwhite::getSysIpType, TYPE_WHITE)
                .eq(SysIpBlackwhite::getSysIpStatus, STATUS_ENABLED));
        if (!whiteList.isEmpty()) {
            for (SysIpBlackwhite item : whiteList) {
                if (matches(clientIp, item.getSysIpIp())) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 判断 IP 是否匹配单个条目(支持精确 IP 或 IPv4 CIDR 段)
     *
     * @param clientIp 客户端 IP
     * @param entry    条目(IP 或 CIDR)
     * @return 是否匹配
     */
    private boolean matches(String clientIp, String entry) {
        if (StringUtils.isBlank(entry)) {
            return false;
        }
        String pattern = entry.trim();
        if (pattern.equals(clientIp)) {
            return true;
        }
        if (pattern.contains("/")) {
            return inCidr(clientIp, pattern);
        }
        return false;
    }

    /**
     * IPv4 CIDR 匹配
     *
     * @param ip   客户端 IP
     * @param cidr CIDR 段
     * @return 是否在段内
     */
    private boolean inCidr(String ip, String cidr) {
        try {
            int slash = cidr.indexOf('/');
            String network = cidr.substring(0, slash);
            int prefix = Integer.parseInt(cidr.substring(slash + 1));
            long ipLong = ipv4ToLong(ip);
            long netLong = ipv4ToLong(network);
            if (ipLong < 0 || netLong < 0) {
                return false;
            }
            long mask = prefix == 0 ? 0L : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
            return (ipLong & mask) == (netLong & mask);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * IPv4 转 long
     *
     * @param ip ip
     * @return long 值, 非法返回 -1
     */
    private long ipv4ToLong(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return -1;
            }
            long result = 0;
            for (String p : parts) {
                int seg = Integer.parseInt(p);
                if (seg < 0 || seg > 255) {
                    return -1;
                }
                result = (result << 8) | seg;
            }
            return result;
        } catch (Exception e) {
            return -1;
        }
    }
}
