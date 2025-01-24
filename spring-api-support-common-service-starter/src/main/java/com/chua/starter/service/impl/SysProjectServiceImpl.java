package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.SysProjectService;
import com.chua.starter.service.mapper.SysProjectMapper;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.entity.SysProject;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 项目
 *
 * @author CH
 */
@Service
public class SysProjectServiceImpl extends ServiceImpl<SysProjectMapper, SysProject> implements SysProjectService {
    @Override
    public IPage<SysProject> pageFor(Query<SysProject> query, SysProject sysProject, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), query.mpjLambda()
                .selectAll(SysProject.class)
                .selectAs(SysDictItem::getSysDictItemName, SysProject::getSysProjectDictItemLabel)
                .leftJoin(SysDictItem.class, SysDictItem::getSysDictItemId, SysProject::getSysProjectDictItemId)
                .like(StringUtils.isNotBlank(sysProject.getSysProjectGroup()), SysProject::getSysProjectGroup, sysProject.getSysProjectGroup())
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long sysProjectId) {
        return ReturnResult.guess(baseMapper.deleteById(sysProjectId) > 0);
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysProject sysSecret) {
        sysSecret.setSysProjectCode(null);
        sysSecret.setSysProjectDelete(null);
        return ReturnResult.guess(baseMapper.updateById(sysSecret) > 0);
    }

    @Override
    public ReturnResult<SysProject> save(SysProject sysProject, Set<String> roles) {
        sysProject.setSysProjectDelete(0);
        if (null == sysProject.getSysProjectStatus()) {
            sysProject.setSysProjectStatus(1);
        }
        sysProject.setSysProjectCode(IdUtils.createUlid());
        baseMapper.insert(sysProject);
        return ReturnResult.guess(sysProject);
    }
}
