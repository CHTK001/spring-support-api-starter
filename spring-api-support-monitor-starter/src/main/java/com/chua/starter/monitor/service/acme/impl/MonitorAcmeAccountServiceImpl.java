package com.chua.starter.monitor.service.acme;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.mapper.MonitorAcmeAccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * ACME 账户服务实现
 *
 * @author CH
 * @since 4.0.0.42
 */
@Service
public class MonitorAcmeAccountServiceImpl
        extends ServiceImpl<MonitorAcmeAccountMapper, MonitorAcmeAccount>
        implements MonitorAcmeAccountService {

    @Override
    public List<MonitorAcmeAccount> listByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return Collections.emptyList();
        }
        return list(Wrappers.<MonitorAcmeAccount>lambdaQuery()
                .eq(MonitorAcmeAccount::getAcmeAccountEmail, email));
    }

    @Override
    public List<MonitorAcmeAccount> listByStatus(String status) {
        if (status == null || status.isEmpty()) {
            return Collections.emptyList();
        }
        return list(Wrappers.<MonitorAcmeAccount>lambdaQuery()
                .eq(MonitorAcmeAccount::getAcmeAccountStatus, status));
    }

    @Override
    public List<MonitorAcmeAccount> listByServer(String server) {
        if (server == null || server.isEmpty()) {
            return Collections.emptyList();
        }
        return list(Wrappers.<MonitorAcmeAccount>lambdaQuery()
                .eq(MonitorAcmeAccount::getAcmeAccountServer, server));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorAcmeAccount saveOrUpdateAccount(MonitorAcmeAccount account) {
        if (account == null) {
            return null;
        }
        if (account.getAcmeAccountId() == null) {
            account.setCreateTime(LocalDateTime.now());
            account.setUpdateTime(LocalDateTime.now());
            if (account.getAcmeAccountStatus() == null) {
                account.setAcmeAccountStatus("valid");
            }
            save(account);
            return account;
        }
        account.setUpdateTime(LocalDateTime.now());
        boolean ok = updateById(account);
        return ok ? account : null;
    }

    @Override
    public boolean updateStatus(Integer accountId, String newStatus) {
        if (accountId == null || newStatus == null) {
            return false;
        }
        MonitorAcmeAccount update = new MonitorAcmeAccount();
        update.setAcmeAccountId(accountId);
        update.setAcmeAccountStatus(newStatus);
        update.setUpdateTime(LocalDateTime.now());
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAccount(Integer accountId) {
        if (accountId == null) {
            return false;
        }
        return removeById(accountId);
    }

    @Override
    public long countAccounts() {
        return count();
    }
}
