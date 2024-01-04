package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.admin.entity.SysUserGrid;
import com.chua.starter.admin.mapper.SysUserGridMapper;
import com.chua.starter.admin.service.SysUserGridService;
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
public class SysUserGridServiceImpl extends ServiceImpl<SysUserGridMapper, SysUserGrid> implements SysUserGridService, Constant {

    @Override
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'grid' + #sysUserGrid.userId")
    public boolean saveGrid(SysUserGrid sysUserGrid) {
        List<SysUserGrid> sysUserGrids = baseMapper.selectList(Wrappers.<SysUserGrid>lambdaQuery().eq(SysUserGrid::getUserId, sysUserGrid.getUserId()));
        if(sysUserGrids.isEmpty()) {
            return baseMapper.insert(sysUserGrid) == 1;
        }
        for (SysUserGrid userGrid : sysUserGrids) {
            BeanUtils.copyProperties(sysUserGrid, userGrid, "grid");
            baseMapper.updateById(userGrid);
        }
        return true;
    }

    @Override
    @Cacheable(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'grid' + #userId")
    public SysUserGrid getGridByUserId(String userId) {
        return baseMapper.selectOne(Wrappers.<SysUserGrid>lambdaQuery().eq(SysUserGrid::getUserId, userId));
    }
}
