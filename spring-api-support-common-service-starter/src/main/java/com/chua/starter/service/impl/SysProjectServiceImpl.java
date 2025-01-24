package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.SysProjectDefaultService;
import com.chua.starter.service.SysProjectService;
import com.chua.starter.service.mapper.SysProjectMapper;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.entity.SysProjectDefault;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 项目
 *
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class SysProjectServiceImpl extends ServiceImpl<SysProjectMapper, SysProject> implements SysProjectService {

    final TransactionTemplate transactionTemplate;
    final SysProjectDefaultService sysProjectDefaultService;
    @Override
    public IPage<SysProject> pageFor(Query<SysProject> query, SysProject sysProject, Set<String> roles) {
        return baseMapper.selectPageFor(query.createPage(), sysProject);
    }

    @Override
    public ReturnResult<Boolean> delete(Long sysProjectId) {
        SysProject sysProject = getById(sysProjectId);
        if (null == sysProject) {
            return Result.illegal("项目信息不存在");
        }
        return transactionTemplate.execute(it -> {
            saveOrUpdateDefault(sysProject);
            return ReturnResult.guess(baseMapper.updateById(sysProjectId) > 0);
        });
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysProject sysSecret) {
        sysSecret.setSysProjectCode(null);
        sysSecret.setSysProjectDelete(null);
        return transactionTemplate.execute(it -> {
            saveOrUpdateDefault(sysSecret);
            return ReturnResult.guess(baseMapper.updateById(sysSecret) > 0);
        });
    }

    private void saveOrUpdateDefault(SysProject sysProject) {
        String sysProjectFunctionDefaultLabel = sysProject.getSysProjectFunctionDefaultLabel();
        if (StringUtils.isNotBlank(sysProjectFunctionDefaultLabel)) {
            Set<String> strings = Splitter.on(",").omitEmptyStrings().trimResults().splitToSet(sysProjectFunctionDefaultLabel);
            sysProjectDefaultService.remove(Wrappers.<SysProjectDefault>lambdaQuery().eq(SysProjectDefault::getSysProjectId, sysProject.getSysProjectId()));
            List<SysProjectDefault> sysProjectDefaults = new ArrayList<>(strings.size());
            for (String string : strings) {
                SysProjectDefault sysProjectDefault = new SysProjectDefault();
                sysProjectDefault.setSysProjectDefaultType(string);
                sysProjectDefault.setSysProjectId(sysProject.getSysProjectId());

                sysProjectDefaults.add(sysProjectDefault);
            }
            sysProjectDefaultService.saveBatch(sysProjectDefaults);

        }
    }

    @Override
    public ReturnResult<SysProject> save(SysProject sysProject, Set<String> roles) {
        sysProject.setSysProjectDelete(0);
        if (null == sysProject.getSysProjectStatus()) {
            sysProject.setSysProjectStatus(1);
        }
        sysProject.setSysProjectCode(IdUtils.createUlid());
        return transactionTemplate.execute(it -> {
            baseMapper.insert(sysProject);
            saveOrUpdateDefault(sysProject);
            return ReturnResult.guess(sysProject);
        });
    }
}
