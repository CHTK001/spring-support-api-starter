package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysAccount;
import com.chua.starter.system.mapper.SysAccountMapper;
import com.chua.starter.system.service.SysAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 第三方账号服务实现
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Service
public class SysAccountServiceImpl extends ServiceImpl<SysAccountMapper, SysAccount> implements SysAccountService {

    @Override
    public SysAccount getByOpenid(String accountType, String openid) {
        if (accountType == null || openid == null) {
            return null;
        }
        return getOne(Wrappers.<SysAccount>lambdaQuery()
                .eq(SysAccount::getSysAccountType, accountType)
                .eq(SysAccount::getSysAccountOpenid, openid)
                .last("LIMIT 1"));
    }

    @Override
    public List<SysAccount> listByUnionid(String accountType, String unionid) {
        if (accountType == null || unionid == null) {
            return List.of();
        }
        return list(Wrappers.<SysAccount>lambdaQuery()
                .eq(SysAccount::getSysAccountType, accountType)
                .eq(SysAccount::getSysAccountUnionid, unionid));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysAccount> bindAccount(SysAccount account) {
        if (account == null || account.getSysAccountOpenid() == null || account.getSysAccountType() == null) {
            return ReturnResult.error("账号信息不完整");
        }
        SysAccount existing = getByOpenid(account.getSysAccountType(), account.getSysAccountOpenid());
        if (existing != null) {
            existing.setSysAccountUserId(account.getSysAccountUserId());
            existing.setSysAccountName(account.getSysAccountName());
            existing.setSysAccountAvatar(account.getSysAccountAvatar());
            existing.setSysAccountPhone(account.getSysAccountPhone());
            existing.setSysAccountEmail(account.getSysAccountEmail());
            existing.setSysAccountForbidden(false);
            existing.setUpdateTime(LocalDateTime.now());
            boolean ok = updateById(existing);
            return ok ? ReturnResult.ok(existing) : ReturnResult.error("更新绑定失败");
        }
        if (account.getSysAccountLoginCount() == null) {
            account.setSysAccountLoginCount(0);
        }
        if (account.getSysAccountForbidden() == null) {
            account.setSysAccountForbidden(false);
        }
        boolean ok = save(account);
        return ok ? ReturnResult.ok(account) : ReturnResult.error("绑定失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> unbindAccount(Integer accountId) {
        if (accountId == null) {
            return ReturnResult.error("accountId 不能为空");
        }
        boolean ok = removeById(accountId);
        return ok ? ReturnResult.ok(true) : ReturnResult.error("解绑失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unbindByUserId(Integer userId) {
        if (userId == null) {
            return 0;
        }
        return baseMapper.delete(Wrappers.<SysAccount>lambdaQuery()
                .eq(SysAccount::getSysAccountUserId, userId));
    }

    @Override
    public List<SysAccount> listByUserId(Integer userId) {
        if (userId == null) {
            return List.of();
        }
        return list(Wrappers.<SysAccount>lambdaQuery()
                .eq(SysAccount::getSysAccountUserId, userId));
    }

    @Override
    public boolean updateLoginInfo(Integer accountId, String ip, String ipAddress) {
        if (accountId == null) {
            return false;
        }
        SysAccount account = getById(accountId);
        if (account == null) {
            return false;
        }
        account.setSysAccountLastIp(ip);
        account.setSysAccountLastIpAddress(ipAddress);
        account.setSysAccountLoginCount((account.getSysAccountLoginCount() == null ? 0 : account.getSysAccountLoginCount()) + 1);
        account.setUpdateTime(LocalDateTime.now());
        return updateById(account);
    }

    @Override
    public boolean setForbidden(Integer accountId, boolean forbidden) {
        if (accountId == null) {
            return false;
        }
        SysAccount account = new SysAccount();
        account.setSysAccountId(accountId);
        account.setSysAccountForbidden(forbidden);
        account.setUpdateTime(LocalDateTime.now());
        return updateById(account);
    }
}
