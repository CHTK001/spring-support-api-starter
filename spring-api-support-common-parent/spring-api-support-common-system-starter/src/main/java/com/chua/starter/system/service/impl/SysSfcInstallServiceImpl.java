package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.entity.system.SysSfcInstall;
import com.chua.starter.system.mapper.SysSfcInstallMapper;
import com.chua.starter.system.service.SysSfcInstallService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * SFC 安装记录服务实现
 *
 * @author CH
 * @since 4.0.0.42
 */
@Service
public class SysSfcInstallServiceImpl extends ServiceImpl<SysSfcInstallMapper, SysSfcInstall> implements SysSfcInstallService {

    @Override
    public List<SysSfcInstall> listByUserId(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<SysSfcInstall>lambdaQuery()
                .eq(SysSfcInstall::getUserId, userId));
    }

    @Override
    public List<SysSfcInstall> listBySfcId(Integer sfcId) {
        if (sfcId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<SysSfcInstall>lambdaQuery()
                .eq(SysSfcInstall::getSysSfcId, sfcId));
    }

    @Override
    public SysSfcInstall getInstall(Integer userId, Integer sfcId) {
        if (userId == null || sfcId == null) {
            return null;
        }
        return getOne(Wrappers.<SysSfcInstall>lambdaQuery()
                .eq(SysSfcInstall::getUserId, userId)
                .eq(SysSfcInstall::getSysSfcId, sfcId)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysSfcInstall recordInstall(SysSfcInstall install) {
        if (install == null || install.getUserId() == null || install.getSysSfcId() == null) {
            return null;
        }
        SysSfcInstall existing = getInstall(install.getUserId(), install.getSysSfcId());
        if (existing != null) {
            existing.setUpdateTime(LocalDateTime.now());
            boolean ok = updateById(existing);
            return ok ? existing : null;
        }
        install.setCreateTime(LocalDateTime.now());
        install.setUpdateTime(LocalDateTime.now());
        boolean ok = save(install);
        return ok ? install : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uninstall(Integer userId, Integer sfcId) {
        if (userId == null || sfcId == null) {
            return false;
        }
        return remove(Wrappers.<SysSfcInstall>lambdaQuery()
                .eq(SysSfcInstall::getUserId, userId)
                .eq(SysSfcInstall::getSysSfcId, sfcId));
    }
}
