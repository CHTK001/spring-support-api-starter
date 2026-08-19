package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.entity.system.SysUserThirdParty;
import com.chua.starter.system.mapper.SysUserThirdPartyMapper;
import com.chua.starter.system.service.SysUserThirdPartyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 用户第三方账号绑定服务实现
 *
 * @since 4.0.0.42
 * @author CH
 */
@Service
public class SysUserThirdPartyServiceImpl extends ServiceImpl<SysUserThirdPartyMapper, SysUserThirdParty> implements SysUserThirdPartyService {

    @Override
    /** ListByUserId */
    public List<SysUserThirdParty> listByUserId(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<SysUserThirdParty>lambdaQuery()
                .eq(SysUserThirdParty::getSysUserId, userId));
    }

    @Override
    /** ListByUserIdAndType */
    public List<SysUserThirdParty> listByUserIdAndType(Integer userId, String type) {
        if (userId == null || type == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<SysUserThirdParty>lambdaQuery()
                .eq(SysUserThirdParty::getSysUserId, userId)
                .eq(SysUserThirdParty::getSysUserThirdPartyType, type));
    }

    @Override
    /** 获取ByThirdPartyUid */
    public SysUserThirdParty getByThirdPartyUid(String type, String uid) {
        if (type == null || uid == null) {
            return null;
        }
        return getOne(Wrappers.<SysUserThirdParty>lambdaQuery()
                .eq(SysUserThirdParty::getSysUserThirdPartyType, type)
                .eq(SysUserThirdParty::getSysUserThirdPartyUid, uid)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /** 绑定 */
    public SysUserThirdParty bind(SysUserThirdParty record) {
        if (record == null || record.getSysUserThirdPartyType() == null
                || record.getSysUserThirdPartyUid() == null) {
            return null;
        }
        SysUserThirdParty existing = getByThirdPartyUid(
                record.getSysUserThirdPartyType(), record.getSysUserThirdPartyUid());
        if (existing != null) {
            existing.setSysUserId(record.getSysUserId());
            existing.setSysUserThirdPartyName(record.getSysUserThirdPartyName());
            if (record.getSysUserThirdPartyValue() != null) {
                existing.setSysUserThirdPartyValue(record.getSysUserThirdPartyValue());
            }
            existing.setUpdateTime(LocalDateTime.now());
            boolean ok = updateById(existing);
            return ok ? existing : null;
        }
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        boolean ok = save(record);
        return ok ? record : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /** 解绑 */
    public boolean unbind(Integer id) {
        if (id == null) {
            return false;
        }
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /** 解绑All */
    public int unbindAll(Integer userId) {
        if (userId == null) {
            return 0;
        }
        return baseMapper.delete(Wrappers.<SysUserThirdParty>lambdaQuery()
                .eq(SysUserThirdParty::getSysUserId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /** 更新Value */
    public boolean updateValue(Integer id, String newValue) {
        if (id == null) {
            return false;
        }
        SysUserThirdParty update = new SysUserThirdParty();
        update.setSysUserThirdPartyId(id);
        update.setSysUserThirdPartyValue(newValue);
        update.setUpdateTime(LocalDateTime.now());
        return updateById(update);
    }
}
