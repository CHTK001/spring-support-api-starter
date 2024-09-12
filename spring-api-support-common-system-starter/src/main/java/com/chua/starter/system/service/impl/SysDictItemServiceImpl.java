package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysDict;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.mapper.SysDictItemMapper;
import com.chua.starter.system.mapper.SysDictMapper;
import com.chua.starter.system.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

    final SysDictMapper sysDictMapper;


    @Override
    public IPage<SysDictItem> pageForItem(Query<SysDictItem> query, Integer sysDictId, Set<String> roles) {
        return baseMapper.selectPageForQuery(query.createPage(), sysDictId);
    }

    @Override
    public ReturnResult<Boolean> delete(Long dictItemId) {
        SysDictItem sysDictItem = baseMapper.selectById(dictItemId);
        SysDict sysDict = sysDictMapper.selectById(sysDictItem.getSysDictId());
        sysDictItem.setSysDictCode(sysDict.getSysDictCode());
        boolean b = baseMapper.deleteById(dictItemId) > 0;
        sysDictItemService.deleteCacheId(sysDictItem);
        sysDictItemService.deleteCacheCode(sysDictItem);
        sysDictItemService.deleteCachePid(sysDictItem);
        sysDictItemService.deleteCachePcode(sysDictItem);
        return ReturnResult.guess(b);
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
        return ReturnResult.of(baseMapper.updateById(sysDictItem) > 0);
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
        baseMapper.insert(sysDictItem);
        sysDictItemService.deleteCacheId(sysDictItem);
        sysDictItemService.deleteCacheCode(sysDictItem);
        sysDictItemService.deleteCachePid(sysDictItem);
        sysDictItemService.deleteCachePcode(sysDictItem);
        return ReturnResult.of( sysDictItem);
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
        return baseMapper.selectListById(sysDictId, false);
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
}
