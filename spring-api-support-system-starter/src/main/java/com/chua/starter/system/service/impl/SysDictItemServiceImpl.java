package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.mapper.SysDictItemMapper;
import com.chua.starter.system.service.SysDictItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.chua.starter.system.constant.SystemConstant.CACHE_WITH_REDIS;

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
    public IPage<SysDictItem> listForItem(Query<SysDictItem> query, Integer sysDictId, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysDictItem>lambdaQuery()
                .eq(SysDictItem::getSysDictId, sysDictId)
                .orderByAsc(SysDictItem::getSysDictItemSort)
                .orderByDesc(SysDictItem::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long dictItemId) {
        return ReturnResult.guess(baseMapper.deleteById(dictItemId) > 0);
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

        return ReturnResult.of(baseMapper.updateById(sysDictItem) > 0);
    }

    @Override
    public ReturnResult<SysDictItem> save(SysDictItem sysDictItem, Set<String> roles) {
        SysDictItem sysDictItem1 = baseMapper.selectOne(Wrappers.<SysDictItem>lambdaQuery()
                .eq(SysDictItem::getSysDictItemCode, sysDictItem.getSysDictItemCode()));
        if(null != sysDictItem1) {
            return ReturnResult.fail("字典编码已存在");
        }
        baseMapper.insert(sysDictItem);
        sysDictItemService.deleteCache(sysDictItem);
        return ReturnResult.of( sysDictItem);
    }
    @Override
    @CacheEvict(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'sys:dict:item:id:' + #sysDictItem.sysDictId")
    public void deleteCache(SysDictItem sysDictItem) {
        //NOTHING
    }
}
