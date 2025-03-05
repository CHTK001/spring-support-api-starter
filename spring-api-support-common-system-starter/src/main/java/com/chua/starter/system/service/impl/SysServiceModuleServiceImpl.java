package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysServiceModule;
import com.chua.starter.system.mapper.SysServiceModuleMapper;
import com.chua.starter.system.service.SysServiceModuleService;
import com.github.yulichang.toolkit.MPJWrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务模块
 *
 * @author CH
 */
@Service
public class SysServiceModuleServiceImpl extends ServiceImpl<SysServiceModuleMapper, SysServiceModule> implements SysServiceModuleService {

    @Override
    public IPage<SysServiceModule> pageFor(Query<SysServiceModule> query, SysServiceModule sysServiceModule) {
        return baseMapper.selectPage(query.createPage(),
                Wrappers.<SysServiceModule>lambdaQuery()
                        .likeRight(StringUtils.isNotBlank(sysServiceModule.getSysServiceModuleName()), SysServiceModule::getSysServiceModuleName, sysServiceModule.getSysServiceModuleName())
                        .likeRight(StringUtils.isNotBlank(sysServiceModule.getSysServiceModuleCode()), SysServiceModule::getSysServiceModuleCode, sysServiceModule.getSysServiceModuleCode())
                        .orderByAsc(SysServiceModule::getSysServiceModuleSort)
        );
    }

    @Override
    public ReturnResult<SysServiceModule> saveFor(SysServiceModule sysServiceModule) {
        if (exists(Wrappers.<SysServiceModule>lambdaQuery()
                .eq(SysServiceModule::getSysServiceModuleCode, sysServiceModule.getSysServiceModuleCode()))) {
            return ReturnResult.error("服务模块编码已存在");
        }
        sysServiceModule.setSysServiceModuleDelete(0);
        sysServiceModule.setSysServiceModuleStatus(0);
        save(sysServiceModule);
        return ReturnResult.success(sysServiceModule);
    }

    @Override
    public ReturnResult<Boolean> updateFor(SysServiceModule sysServiceModule) {
        if (exists(Wrappers.<SysServiceModule>lambdaQuery().eq(SysServiceModule::getSysServiceModuleCode, sysServiceModule.getSysServiceModuleCode())
                .ne(SysServiceModule::getSysServiceModuleId, sysServiceModule.getSysServiceModuleId()))) {
            return ReturnResult.error("服务模块编码已存在");
        }
        sysServiceModule.setSysServiceModuleDelete(null);
        return ReturnResult.preconditioning(updateById(sysServiceModule)).toResult();
    }

    @Override
    public ReturnResult<Boolean> deleteFor(Long SysServiceModuleId) {
        return ReturnResult.preconditioning(removeById(SysServiceModuleId)).toResult();
    }

    @Override
    public ReturnResult<List<SysServiceModule>> listFor(SysServiceModule sysServiceModule) {
        return ReturnResult.ok(baseMapper.selectList(MPJWrappers.<SysServiceModule>lambdaJoin()
                .selectAll(SysServiceModule.class)
                .likeRight(StringUtils.isNotBlank(sysServiceModule.getSysServiceModuleName()), SysServiceModule::getSysServiceModuleName, sysServiceModule.getSysServiceModuleName())
                .likeRight(StringUtils.isNotBlank(sysServiceModule.getSysServiceModuleCode()), SysServiceModule::getSysServiceModuleCode, sysServiceModule.getSysServiceModuleCode())
                .orderByAsc(SysServiceModule::getSysServiceModuleSort)
        ));
    }
}
