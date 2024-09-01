package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysDict;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.mapper.SysDictItemMapper;
import com.chua.starter.system.service.SysDictItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
@Service
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService{

    @Autowired
    private SysDictItemService sysDictItemService;


    @Override
    public IPage<SysDictItem> pageForItem(Query<SysDictItem> query, Integer sysDictId, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysDictItem>lambdaQuery()
                .eq(SysDictItem::getSysDictId, sysDictId)
                .orderByAsc(SysDictItem::getSysDictItemSort)
                .orderByDesc(SysDictItem::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long dictItemId) {
        SysDictItem sysDictItem = baseMapper.selectById(dictItemId);
        boolean b = baseMapper.deleteById(dictItemId) > 0;
        sysDictItemService.deleteCache(sysDictItem);
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
        sysDictItemService.deleteCache(sysDictItem);
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
        sysDictItem.setSysDictItemDelete(0);
        baseMapper.insert(sysDictItem);
        sysDictItemService.deleteCache(sysDictItem);
        return ReturnResult.of( sysDictItem);
    }
    @Override
    @Caching(evict = {
            @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'cache:sys:dict:id:' + #sysDictItem.sysDictId"),
            @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'cache:sys:dict:item:id:' + #sysDictItem.sysDictItemId"),
            @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'cache:sys:dict:item:code:' + #sysDictItem.sysDictItemCode")
    })
    public void deleteCache(SysDictItem sysDictItem) {
        //NOTHING
    }

    @Override
    public List<SysDictItem> listForItem(Query<SysDictItem> query, Integer dictItemId, Set<String> roles) {
        QueryWrapper<SysDictItem> wrapper = query.wrapper();
        return baseMapper.selectList(wrapper.lambda().eq(SysDictItem::getSysDictItemStatus, 1)
                .eq(SysDictItem::getSysDictId, dictItemId)
        );
    }

    @Override
    public List<SysDictItem> listForItemCode(Query<SysDictItem> query, Integer sysDictCode, Set<String> roles) {
        return baseMapper.selectList(query.mpjLambda()
                .innerJoin(SysDict.class, SysDict::getSysDictId, SysDictItem::getSysDictId)
                .eq(SysDictItem::getSysDictItemStatus, 1)
                .eq(SysDict::getSysDictCode, sysDictCode)
        );
    }
}
