package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.SysProjectDefaultService;
import com.chua.starter.service.SysProjectService;
import com.chua.starter.service.mapper.SysProjectMapper;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.entity.SysProjectDefault;
import com.chua.starter.system.pojo.project.ProjectDefaultFunctionV1Request;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

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
    @ApiCacheKey("'sys:project:' + #sysProjectId")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public SysProject getOne(Integer sysProjectId) {
        SysProject sysProject = new SysProject();
        sysProject.setSysProjectId(sysProjectId);
        IPage<SysProject> sysProjectIPage = pageFor(new Query<SysProject>().setPageSize(1), sysProject, Set.of());
        return CollectionUtils.findFirst(sysProjectIPage.getRecords());
    }

    @Override
    public SysProject getDefaultByType(String typeName) {
        IPage<SysProject> defaultByType = baseMapper.getDefaultByType(new Page<>(1, 1), typeName);
        return CollectionUtils.findFirst(defaultByType.getRecords());
    }

    @Override
    @ApiCacheKey("'sys:project:' + #sysProjectId")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public ReturnResult<Boolean> delete(Long sysProjectId) {
        SysProject sysProject = getById(sysProjectId);
        if (null == sysProject) {
            return Result.illegal("项目信息不存在");
        }
        return transactionTemplate.execute(it -> {
            saveOrUpdateDefault(sysProject);
            return ReturnResult.guess(baseMapper.deleteById(sysProjectId) > 0);
        });
    }

    @Override
    @ApiCacheKey("'sys:project:' + #sysProject.sysProjectId")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public ReturnResult<Boolean> update(Set<String> roles, SysProject sysProject) {
        sysProject.setSysProjectCode(null);
        sysProject.setSysProjectDelete(null);
        return transactionTemplate.execute(it -> {
            saveOrUpdateDefault(sysProject);
            return ReturnResult.guess(baseMapper.updateById(sysProject) > 0);
        });
    }

    @Override
    public ReturnResult<SysProject> updateDefault(ProjectDefaultFunctionV1Request request) {
        return transactionTemplate.execute(it -> {
            sysProjectDefaultService.remove(Wrappers.<SysProjectDefault>lambdaQuery().eq(SysProjectDefault::getSysProjectDefaultType, request.getSysProjectDefaultType()));
            Integer sysSetDefault = request.getSysSetDefault();
            if(null == sysSetDefault || sysSetDefault == 0) {
                return ReturnResult.ok(getDefault(request.getSysProjectId()));
            }
            SysProjectDefault sysProjectDefault = new SysProjectDefault();
            sysProjectDefault.setSysProjectDefaultType(request.getSysProjectDefaultType() + "");
            sysProjectDefault.setSysProjectId(request.getSysProjectId());
            sysProjectDefaultService.save(sysProjectDefault);
            return ReturnResult.guess(getDefault(request.getSysProjectId()));
        });
    }

    /**
     * 获取默认项目
     *
     * @param sysProjectId 项目id
     * @return 项目
     */
    protected SysProject getDefault(Integer sysProjectId) {
        SysProject sysProject = new SysProject();
        sysProject.setSysProjectId(sysProjectId);
        Query<SysProject> query = new Query<>();
        query.setPageSize(1);
        IPage<SysProject> sysProjectIPage = pageFor(query, sysProject, Set.of());
        return CollectionUtils.findFirst(sysProjectIPage.getRecords());
    }

    private void saveOrUpdateDefault(SysProject sysProject) {
        String sysProjectFunctionDefaultIds = sysProject.getSysProjectFunctionDefaultIds();
        if (StringUtils.isNotBlank(sysProjectFunctionDefaultIds)) {
            Set<String> strings = Splitter.on(",").omitEmptyStrings().trimResults().splitToSet(sysProjectFunctionDefaultIds);
            sysProjectDefaultService.remove(Wrappers.<SysProjectDefault>lambdaQuery().eq(SysProjectDefault::getSysProjectId, sysProject.getSysProjectId()));
            sysProjectDefaultService.remove(Wrappers.<SysProjectDefault>lambdaQuery()
                    .in(SysProjectDefault::getSysProjectDefaultType, strings));
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
