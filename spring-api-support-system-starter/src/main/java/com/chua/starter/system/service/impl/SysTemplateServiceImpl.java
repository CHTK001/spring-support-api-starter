package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.entity.SysTemplate;
import com.chua.starter.system.entity.SysTemplateCategory;
import com.chua.starter.system.mapper.SysTemplateCategoryMapper;
import com.chua.starter.system.mapper.SysTemplateMapper;
import com.chua.starter.system.service.SysTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 *
 * @since 2024/9/2
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysTemplateServiceImpl extends ServiceImpl<SysTemplateMapper, SysTemplate> implements SysTemplateService{

    final TransactionTemplate transactionTemplate;

    final SysTemplateCategoryMapper sysTemplateCategoryMapper;

    @Autowired
    private SysTemplateService sysTemplateService;

    @Override
    public IPage<SysTemplate> page(Query<SysTemplate> query, SysTemplate sysTemplate, Set<String> roles) {
        return baseMapper.selectPageForRoles(query.createPage(), sysTemplate, AuthConstant.hasAdmin(roles));
    }

    @Override
    public ReturnResult<Boolean> delete(Long sysTemplateId) {
        SysTemplate sysTemplate = baseMapper.selectById(sysTemplateId);
        SysTemplateCategory templateGroup = sysTemplateCategoryMapper.selectById(sysTemplate.getSysTemplateCategoryId());
        sysTemplate.setSysTemplateGroupCode(templateGroup.getSysTemplateCategoryCode());
        boolean b = baseMapper.deleteById(sysTemplateId) > 0;
        sysTemplateService.deleteCacheId(templateGroup);
        sysTemplateService.deleteCacheCode(templateGroup);
        return ReturnResult.guess(b);
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysTemplate sysTemplate) {
        String sysTemplateCode = sysTemplate.getSysTemplateCode();
        if(StringUtils.isNotBlank(sysTemplateCode)) {
            SysTemplate sysDictItem1 = baseMapper.selectOne(Wrappers.<SysTemplate>lambdaQuery()
                    .eq(SysTemplate::getSysTemplateCode, sysTemplateCode)
                    .ne(SysTemplate::getSysTemplateId, sysTemplate.getSysTemplateId()));
            if(null != sysDictItem1) {
                return ReturnResult.fail("字典编码已存在");
            }
        }
        sysTemplate.setSysTemplateDelete(null);
        SysTemplateCategory templateGroup = sysTemplateCategoryMapper.selectById(sysTemplate.getSysTemplateCategoryId());
        sysTemplate.setSysTemplateGroupCode(templateGroup.getSysTemplateCategoryCode());
        sysTemplateService.deleteCacheId(templateGroup);
        sysTemplateService.deleteCacheCode(templateGroup);
        return ReturnResult.of(baseMapper.updateById(sysTemplate) > 0);
    }

    @Override
    public ReturnResult<SysTemplate> save(SysTemplate sysTemplate, Set<String> roles) {
        SysTemplate sysTemplate1 = baseMapper.selectOne(Wrappers.<SysTemplate>lambdaQuery()
                .eq(SysTemplate::getSysTemplateCode, sysTemplate.getSysTemplateCode()));
        if(null != sysTemplate1) {
            return ReturnResult.fail("字典编码已存在");
        }

        if(null == sysTemplate.getSysTemplateStatus()) {
            sysTemplate.setSysTemplateStatus(1);
        }

        if(AuthConstant.hasAdmin(roles)) {
            sysTemplate.setSysTemplateApprove(2);
        }

        sysTemplate.setSysTemplateDelete(0);
        SysTemplateCategory templateGroup = sysTemplateCategoryMapper.selectById(sysTemplate.getSysTemplateCategoryId());
        sysTemplate.setSysTemplateGroupCode(templateGroup.getSysTemplateCategoryCode());
        baseMapper.insert(sysTemplate);
        sysTemplateService.deleteCacheId(templateGroup);
        sysTemplateService.deleteCacheCode(templateGroup);
        return ReturnResult.of( sysTemplate);
    }
    @Override
    @ApiCacheKey("'sys:category:id:' + #sysTemplateCategory.sysTemplateCategoryId")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customKeyGenerator")
    public void deleteCacheId(SysTemplateCategory sysTemplateCategory) {

    }

    @Override
    @ApiCacheKey("'sys:category:code:' + #sysTemplateCategory.sysTemplateCategoryCode")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customKeyGenerator")
    public void deleteCacheCode(SysTemplateCategory sysTemplateCategory) {

    }

    @Override
    public ReturnResult<Boolean> rollback(Set<String> roles, SysTemplate sysTemplate) {
        sysTemplate.setSysTemplateDelete(0);
        return ReturnResult.of(baseMapper.updateById(sysTemplate) > 0);
    }

    @Override
    public List<SysTemplate> listForItemCode(Query<SysDictItem> query, String sysTemplateCategoryCode, Set<String> roles) {
        return baseMapper.listForItemCode(sysTemplateCategoryCode);
    }

    @Override
    public List<SysTemplate> listForItem(Query<SysDictItem> query, Integer sysTemplateCategoryId, Set<String> roles) {
        return baseMapper.listForItem(sysTemplateCategoryId);
    }

}
