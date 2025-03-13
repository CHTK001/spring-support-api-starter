package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysAiVincentStyle;
import com.chua.starter.system.mapper.SysAiVincentStyleMapper;
import com.chua.starter.system.service.SysAiVincentStyleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysAiVincentStyleServiceImpl extends ServiceImpl<SysAiVincentStyleMapper, SysAiVincentStyle> implements SysAiVincentStyleService {

    @Override
    public ReturnResult<Boolean> deleteForModuleStyle(Integer sysAiVincentStyleId) {
        return ReturnResult.ok(baseMapper.deleteById(sysAiVincentStyleId) > 0);
    }

    @Override
    public ReturnResult<SysAiVincentStyle> saveForModuleStyle(SysAiVincentStyle sysAiVincentStyle) {
        save(sysAiVincentStyle);
        return ReturnResult.ok(sysAiVincentStyle);
    }

    @Override
    public ReturnResult<Boolean> updateForModuleStyle(SysAiVincentStyle sysAiVincentStyle) {
        return ReturnResult.ok(updateById(sysAiVincentStyle));
    }

    @Override
    public ReturnResult<List<SysAiVincentStyle>> listForModuleStyle(SysAiVincentStyle sysAiVincentStyle) {
        return ReturnResult.ok(baseMapper.selectList(Wrappers.lambdaQuery(SysAiVincentStyle.class)
                .eq(SysAiVincentStyle::getSysAiModuleId, sysAiVincentStyle.getSysAiModuleId())
        ));
    }
}
