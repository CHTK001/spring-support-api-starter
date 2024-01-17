package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.admin.entity.SysUserSetting;
import com.chua.starter.admin.mapper.SysUserGridMapper;
import com.chua.starter.admin.service.SysUserGridService;
import com.chua.starter.common.support.constant.Constant;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 *    
 * @author CH
 */     
@Service
public class SysUserGridServiceImpl extends ServiceImpl<SysUserGridMapper, SysUserSetting> implements SysUserGridService, Constant {

    @Override
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'grid' + #sysUserGrid.userId")
    public boolean saveGrid(SysUserSetting sysUserGrid) {
        List<SysUserSetting> sysUserGrids = baseMapper.selectList(Wrappers.<SysUserSetting>lambdaQuery().eq(SysUserSetting::getUserId, sysUserGrid.getUserId()));
        if(sysUserGrids.isEmpty()) {
            return baseMapper.insert(sysUserGrid) == 1;
        }
        for (SysUserSetting userGrid : sysUserGrids) {
            BeanUtils.copyProperties(sysUserGrid, userGrid, "grid");
            baseMapper.updateById(userGrid);
        }
        return true;
    }

    @Override
    @Cacheable(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'grid' + #userId")
    public SysUserSetting getGridByUserId(String userId) {
        return baseMapper.selectOne(Wrappers.<SysUserSetting>lambdaQuery().eq(SysUserSetting::getUserId, userId));
    }
}
