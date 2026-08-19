package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.entity.system.SysUserLayout;
import com.chua.starter.system.mapper.SysUserLayoutMapper;
import com.chua.starter.system.service.SysUserLayoutService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 用户布局偏好服务实现
 *
 * @since 4.0.0.42
 * @author CH
 */
@Service
public class SysUserLayoutServiceImpl extends ServiceImpl<SysUserLayoutMapper, SysUserLayout> implements SysUserLayoutService {

    @Override
    public List<SysUserLayout> listByUserId(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return list(Wrappers.<SysUserLayout>lambdaQuery()
                .eq(SysUserLayout::getSysLayoutUserId, userId));
    }

    @Override
    public SysUserLayout getByUserIdAndKey(Integer userId, String layoutKey) {
        if (userId == null || layoutKey == null) {
            return null;
        }
        return getOne(Wrappers.<SysUserLayout>lambdaQuery()
                .eq(SysUserLayout::getSysLayoutUserId, userId)
                .eq(SysUserLayout::getSysLayoutLayout, layoutKey)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserLayout saveOrUpdateLayout(SysUserLayout layout) {
        if (layout == null) {
            return null;
        }
        SysUserLayout existing = null;
        if (layout.getSysLayoutUserId() != null && layout.getSysLayoutLayout() != null) {
            existing = getByUserIdAndKey(layout.getSysLayoutUserId(), layout.getSysLayoutLayout());
        }
        if (existing == null) {
            layout.setCreateTime(LocalDateTime.now());
            layout.setUpdateTime(LocalDateTime.now());
            save(layout);
            return layout;
        }
        existing.setSysLayoutGrid(layout.getSysLayoutGrid());
        existing.setSysLayoutComponent(layout.getSysLayoutComponent());
        existing.setSysLayoutPreference(layout.getSysLayoutPreference());
        existing.setUpdateTime(LocalDateTime.now());
        boolean ok = updateById(existing);
        return ok ? existing : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByUserId(Integer userId) {
        if (userId == null) {
            return 0;
        }
        return baseMapper.delete(Wrappers.<SysUserLayout>lambdaQuery()
                .eq(SysUserLayout::getSysLayoutUserId, userId));
    }
}
