package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.system.entity.SysDict;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.entity.SysDictItemProperty;
import com.chua.starter.system.mapper.SysDictItemMapper;
import com.chua.starter.system.mapper.SysDictItemPropertyMapper;
import com.chua.starter.system.mapper.SysDictMapper;
import com.chua.starter.system.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService{

    @Autowired
    private SysDictItemService sysDictItemService;


    final SysDictItemPropertyMapper sysDictItemPropertyMapper;
    final SysDictMapper sysDictMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;


    @Override
    public IPage<SysDictItem> pageForItem(Query<SysDictItem> query, Integer sysDictId, Set<String> roles) {
        IPage<SysDictItem> sysDictItemIPage = baseMapper.selectPageForQuery(query.createPage(), sysDictId);
        return ReturnPageResultUtils.preconditioning(sysDictItemIPage)
                .mapping(SysDictItem::getSysDictItemId, (ids, it) -> {
                    Map<Integer, List<SysDictItemProperty>> collect = sysDictItemPropertyMapper.selectList(
                                    Wrappers.<SysDictItemProperty>lambdaQuery()
                                            .in(SysDictItemProperty::getSysDictItemId, ids)
                            ).stream()
                            .collect(Collectors.groupingBy(SysDictItemProperty::getSysDictItemId));
                    for (Map.Entry<Integer, List<SysDictItemProperty>> entry : collect.entrySet()) {
                        SysDictItem sysDictItem = it.get(entry.getKey());
                        if (null == sysDictItem) {
                            continue;
                        }
                        sysDictItem.setProperty(entry.getValue());
                    }
                }).returnPage();
    }

    @Override
    public ReturnResult<Boolean> delete(Long dictItemId) {
        SysDictItem sysDictItem = baseMapper.selectById(dictItemId);
        if (null != sysDictItem) {
            SysDict sysDict = sysDictMapper.selectById(sysDictItem.getSysDictId());
            sysDictItem.setSysDictCode(sysDict.getSysDictCode());
            transactionTemplate.execute(status -> {
                baseMapper.deleteById(dictItemId);
                sysDictItemPropertyMapper.delete(Wrappers.<SysDictItemProperty>lambdaQuery()
                        .eq(SysDictItemProperty::getSysDictItemId, sysDictItem.getSysDictItemId()));
                sysDictItemService.deleteCacheId(sysDictItem);
                sysDictItemService.deleteCacheCode(sysDictItem);
                sysDictItemService.deleteCachePid(sysDictItem);
                sysDictItemService.deleteCachePcode(sysDictItem);
                return ReturnResult.guess(true);
            });
        }
        return ReturnResult.guess(true);
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysDictItem sysDictItem) {
        String sysDictItemCode = sysDictItem.getSysDictItemCode();
        if(StringUtils.isNotBlank(sysDictItemCode)) {
            SysDictItem sysDictItem1 = baseMapper.selectOne(Wrappers.<SysDictItem>lambdaQuery()
                    .eq(SysDictItem::getSysDictItemCode, sysDictItemCode)
                    .ne(SysDictItem::getSysDictId, sysDictItem.getSysDictId()));
            if(null != sysDictItem1) {
                return ReturnResult.fail("字典编码已存在");
            }
        }


        SysDict sysDict = sysDictMapper.selectById(sysDictItem.getSysDictId());
        sysDictItem.setSysDictCode(sysDict.getSysDictCode());
        sysDictItemService.deleteCacheId(sysDictItem);
        sysDictItemService.deleteCacheCode(sysDictItem);
        sysDictItemService.deleteCachePid(sysDictItem);
        sysDictItemService.deleteCachePcode(sysDictItem);
        return transactionTemplate.execute(status -> {
            return ReturnResult.of(baseMapper.updateById(sysDictItem) > 0);
        });
    }

    @Override
    public ReturnResult<SysDictItem> save(SysDictItem sysDictItem, Set<String> roles) {
        SysDictItem sysDictItem1 = baseMapper.selectOne(Wrappers.<SysDictItem>lambdaQuery()
                .eq(SysDictItem::getSysDictItemCode, sysDictItem.getSysDictItemCode()));
        if(null != sysDictItem1) {
            return ReturnResult.fail("字典编码已存在");
        }

        if(null == sysDictItem.getSysDictItemStatus()) {
            sysDictItem.setSysDictItemStatus(1);
        }

        SysDict sysDict = sysDictMapper.selectById(sysDictItem.getSysDictId());
        sysDictItem.setSysDictCode(sysDict.getSysDictCode());
        sysDictItem.setSysDictItemDelete(0);
        return transactionTemplate.execute(status -> {
            baseMapper.insert(sysDictItem);
            sysDictItemService.deleteCacheId(sysDictItem);
            sysDictItemService.deleteCacheCode(sysDictItem);
            sysDictItemService.deleteCachePid(sysDictItem);
            sysDictItemService.deleteCachePcode(sysDictItem);
            return ReturnResult.of(sysDictItem);
        });
    }
    @Override
    @ApiCacheKey("'sys:dict:id:' + #sysDictItem.sysDictId")
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public void deleteCacheId(SysDictItem sysDictItem) {
        //NOTHING
    }
    @Override
    @ApiCacheKey("'sys:dict:code:' + #sysDictItem.sysDictCode")
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public void deleteCacheCode(SysDictItem sysDictItem) {
        //NOTHING
    }

    @Override
    @ApiCacheKey("'sys:dict:pid:' + #sysDictItem.sysDictId")
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public void deleteCachePid(SysDictItem sysDictItem) {

    }

    @Override
    @ApiCacheKey("'sys:dict:pcode:' + #sysDictItem.sysDictCode")
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public void deleteCachePcode(SysDictItem sysDictItem) {

    }

    @Override
    public List<SysDictItem> listForItemId(Query<SysDictItem> query, Integer sysDictId, Set<String> roles) {
        List<SysDictItem> sysDictItems = baseMapper.selectListById(sysDictId, false);
        if (CollectionUtils.isEmpty(sysDictItems)) {
            return Collections.emptyList();
        }

        Map<Integer, SysDictItem> it = sysDictItems.stream().collect(Collectors.toMap(SysDictItem::getSysDictItemId, Function.identity()));
        Map<Integer, List<SysDictItemProperty>> collect = sysDictItemPropertyMapper.selectList(
                        Wrappers.<SysDictItemProperty>lambdaQuery()
                                .in(SysDictItemProperty::getSysDictItemId, it.keySet())
                ).stream()
                .collect(Collectors.groupingBy(SysDictItemProperty::getSysDictItemId));
        for (Map.Entry<Integer, List<SysDictItemProperty>> entry : collect.entrySet()) {
            SysDictItem sysDictItem = it.get(entry.getKey());
            if (null == sysDictItem) {
                continue;
            }
            sysDictItem.setProperty(entry.getValue());
        }
        return sysDictItems;
    }

    @Override
    public List<SysDictItem> listForItemCode(Query<SysDictItem> query, String sysDictCode, Set<String> roles) {
        return baseMapper.selectListByCode(sysDictCode, false);

    }

    @Override
    public List<SysDictItem> listForItemPcode(Query<SysDictItem> query, String sysDictCode, Set<String> roles) {
        return baseMapper.selectListByCode(sysDictCode, true);

    }

    @Override
    public List<SysDictItem> listForItemPid(Query<SysDictItem> query, Integer sysDictId, Set<String> roles) {
        return baseMapper.selectListById(sysDictId, true);

    }

    @Override
    public SysDictItem getTypeIdByName(String sysDictCode, int sysDictId) {
        return baseMapper
                .selectOne(
                        Wrappers.<SysDictItem>lambdaQuery()
                                .eq(SysDictItem::getSysDictItemCode, sysDictCode)
                                .eq(SysDictItem::getSysDictId, sysDictId)
                );
    }
}
