package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysTemplate;
import com.chua.starter.system.entity.SysTemplateGroup;
import com.chua.starter.system.mapper.SysTemplateGroupMapper;
import com.chua.starter.system.mapper.SysTemplateMapper;
import com.chua.starter.system.service.SysTemplateGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;

/**
 *
 * @since 2024/9/2
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysTemplateGroupServiceImpl extends ServiceImpl<SysTemplateGroupMapper, SysTemplateGroup> implements SysTemplateGroupService{

    final TransactionTemplate transactionTemplate;
    final SysTemplateMapper sysTemplateMapper;

    @Override
    public IPage<SysTemplateGroup> page(Query<SysTemplateGroup> query, SysTemplateGroup sysTemplateGroup, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), query.lambda()
                .like(StringUtils.isNotBlank(sysTemplateGroup.getSysTemplateGroupName()), SysTemplateGroup::getSysTemplateGroupName, sysTemplateGroup.getSysTemplateGroupName())
                .eq(StringUtils.isNotBlank(sysTemplateGroup.getSysTemplateGroupCode()), SysTemplateGroup::getSysTemplateGroupCode, sysTemplateGroup.getSysTemplateGroupCode())
                .orderByDesc(SysTemplateGroup::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long sysTemplateId) {
        if(null == sysTemplateId) {
            return ReturnResult.fail("模板ID不能为空");
        }
        return ReturnResult.guess(transactionTemplate.execute(stats -> {
            int deleted = baseMapper.deleteById(sysTemplateId);
            sysTemplateMapper.delete(Wrappers.<SysTemplate>lambdaQuery().eq(SysTemplate::getSysTemplateGroupId, sysTemplateId));
            return deleted > 0;
        }));
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysTemplateGroup sysTemplateGroup) {
        String sysTemplateGroupCode = sysTemplateGroup.getSysTemplateGroupCode();
        if(StringUtils.isNotBlank(sysTemplateGroupCode)) {
            SysTemplateGroup templateGroup1 = baseMapper.selectOne(Wrappers.<SysTemplateGroup>lambdaQuery()
                    .eq(SysTemplateGroup::getSysTemplateGroupCode, sysTemplateGroupCode)
                    .ne(SysTemplateGroup::getSysTemplateGroupId, sysTemplateGroup.getSysTemplateGroupId()));
            if(null != templateGroup1) {
                return ReturnResult.fail("编码已存在");
            }
        }

        return ReturnResult.of(baseMapper.updateById(sysTemplateGroup) > 0);
    }

    @Override
    public ReturnResult<SysTemplateGroup> save(SysTemplateGroup sysTemplateGroup, Set<String> roles) {
        SysTemplateGroup templateGroup = baseMapper.selectOne(Wrappers.<SysTemplateGroup>lambdaQuery()
                .eq(SysTemplateGroup::getSysTemplateGroupCode, sysTemplateGroup.getSysTemplateGroupCode()));
        if(null != templateGroup) {
            return ReturnResult.fail("字典编码已存在");
        }
        if(baseMapper.insert(sysTemplateGroup) > 0) {
            return ReturnResult.of(sysTemplateGroup);
        }
        return ReturnResult.illegal("新增失败");
    }
}
