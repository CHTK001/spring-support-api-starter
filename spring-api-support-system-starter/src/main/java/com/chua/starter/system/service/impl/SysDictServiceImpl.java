package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysDict;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.mapper.SysDictItemMapper;
import com.chua.starter.system.mapper.SysDictMapper;
import com.chua.starter.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService{

    final TransactionTemplate transactionTemplate;
    final SysDictItemMapper sysDictItemMapper;

    @Override
    public IPage<SysDict> pageForDict(Query<SysDict> query, SysDict sysDict, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysDict>lambdaQuery()
                .like(StringUtils.isNotBlank(sysDict.getSysDictCode()), SysDict::getSysDictCode, sysDict.getSysDictCode())
                .like(StringUtils.isNotBlank(sysDict.getSysDictName()), SysDict::getSysDictName, sysDict.getSysDictName())
                .like(StringUtils.isNotBlank(sysDict.getSysDictRemark()), SysDict::getSysDictRemark, sysDict.getSysDictRemark())
                .orderByDesc(SysDict::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long dictId) {
        if(null == dictId) {
            return ReturnResult.fail("字典ID不能为空");
        }

        SysDict sysDict = baseMapper.selectById(dictId);
        if(null == sysDict) {
            return ReturnResult.fail("字典不存在, 请刷新后重试!");
        }

        Integer sysRoleInSystem = sysDict.getSysDictInSystem();

        if(null != sysRoleInSystem && sysRoleInSystem == 1) {
            return ReturnResult.fail("内置字典不允许删除");
        }

        return ReturnResult.guess(transactionTemplate.execute(stats -> {
            int deleted = baseMapper.deleteById(dictId);
            sysDictItemMapper.delete(Wrappers.<SysDictItem>lambdaQuery().eq(SysDictItem::getSysDictId, dictId));
            return deleted > 0;
        }));
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysDict sysDict) {
        SysDict sysDict1 = baseMapper.selectById(sysDict.getSysDictId());
        Integer sysRoleInSystem = sysDict1.getSysDictInSystem();
        if(null != sysRoleInSystem && sysRoleInSystem == 1 && !AuthConstant.hasAdmin(roles)) {
            return ReturnResult.fail("内置字典不允许修改");
        }
        sysDict.setSysDictInSystem(null);
        String sysRoleCode = sysDict.getSysDictCode();
        if(StringUtils.isNotBlank(sysRoleCode)) {
            SysDict sysRole11 = baseMapper.selectOne(Wrappers.<SysDict>lambdaQuery()
                    .eq(SysDict::getSysDictCode, sysRoleCode)
                    .ne(SysDict::getSysDictId, sysDict.getSysDictId()));
            if(null != sysRole11) {
                return ReturnResult.fail("字典编码已存在");
            }
        }

        return ReturnResult.of(baseMapper.updateById(sysDict) > 0);
    }

    @Override
    public ReturnResult<SysDict> save(SysDict sysDict, Set<String> roles) {
        sysDict.setSysDictInSystem(0);
        SysDict sysRole1 = baseMapper.selectOne(Wrappers.<SysDict>lambdaQuery()
                .eq(SysDict::getSysDictCode, sysDict.getSysDictCode()));
        if(null != sysRole1) {
            return ReturnResult.fail("字典编码已存在");
        }
        if(baseMapper.insert(sysDict) > 0) {
            return ReturnResult.of(sysDict);
        }
        return ReturnResult.illegal("新增失败");
    }

}
