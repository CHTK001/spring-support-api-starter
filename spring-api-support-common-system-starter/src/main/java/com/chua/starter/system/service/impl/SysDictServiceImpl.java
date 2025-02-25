package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.*;
import java.util.stream.Collectors;

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
        Page<SysDict> sysDictPage = baseMapper.selectPage(query.createPage(), Wrappers.<SysDict>lambdaQuery()
                .eq(SysDict::getSysDictPid, 0)
                .like(StringUtils.isNotBlank(sysDict.getSysDictCode()), SysDict::getSysDictCode, sysDict.getSysDictCode())
                .like(StringUtils.isNotBlank(sysDict.getSysDictName()), SysDict::getSysDictName, sysDict.getSysDictName())
                .like(StringUtils.isNotBlank(sysDict.getSysDictRemark()), SysDict::getSysDictRemark, sysDict.getSysDictRemark())
                .orderByDesc(SysDict::getCreateTime)
        );

        List<SysDict> records = sysDictPage.getRecords();
        List<Integer> ids = records.stream().map(SysDict::getSysDictId).toList();
        if (CollectionUtils.isEmpty(ids)) {
            return sysDictPage;
        }
        List<SysDict> sysDicts = baseMapper.selectList(Wrappers.<SysDict>lambdaQuery().in(SysDict::getSysDictPid, ids));
        Map<Integer, List<SysDict>> sysDictMap = sysDicts.stream().collect(Collectors.groupingBy(SysDict::getSysDictPid));
        for (SysDict record : records) {
            record.setChildren(Optional.ofNullable(sysDictMap.get(record.getSysDictId())).orElse(Collections.emptyList()));
        }
        return sysDictPage;
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

        if(!AuthConstant.hasAdmin(roles)) {
            sysDict.setSysDictInSystem(null);
        }

        Integer sysDictPid = sysDict.getSysDictPid();
        if(null != sysDictPid && sysDictPid != 0) {
            SysDict sysDict11 = baseMapper.selectById(sysDictPid);
            if(null == sysDict11) {
                return ReturnResult.fail("父级字典不存在");
            }

            if(null != sysDict11.getSysDictPid() && sysDict11.getSysDictPid() != 0) {
                return ReturnResult.fail("只允许挂载在一级节点上");
            }
        }

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
        sysDict.setSysDictStatus(1);
        SysDict sysRole1 = baseMapper.selectOne(Wrappers.<SysDict>lambdaQuery()
                .eq(SysDict::getSysDictCode, sysDict.getSysDictCode()));

        Integer sysDictPid = sysDict.getSysDictPid();
        if(null == sysDictPid || sysDictPid == 0) {
            sysDict.setSysDictPid(0);
        } else {
            SysDict sysDict1 = baseMapper.selectById(sysDictPid);
            if(null == sysDict1) {
                return ReturnResult.fail("父级字典不存在");
            }

            if(null != sysDict1.getSysDictPid() && sysDict1.getSysDictPid() != 0) {
                return ReturnResult.fail("只允许挂载在一级节点上");
            }
        }

        if(null != sysRole1) {
            return ReturnResult.fail("字典编码已存在");
        }
        if(baseMapper.insert(sysDict) > 0) {
            return ReturnResult.of(sysDict);
        }
        return ReturnResult.illegal("新增失败");
    }

}
