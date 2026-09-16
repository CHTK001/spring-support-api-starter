package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.service.SysDictItemService;
import com.chua.starter.service.mapper.SysDictItemMapper;
import com.chua.starter.service.mapper.SysDictItemPropertyMapper;
import com.chua.starter.service.mapper.SysDictMapper;
import org.springframework.context.annotation.Lazy;
import com.chua.starter.entity.service.SysDict;
import com.chua.starter.entity.service.SysDictItem;
import com.chua.starter.entity.service.SysDictItemProperty;
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
* 系统字典项服务实现类
*
* @since 2024/8/7
* @author CH
 */
@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

    @Autowired
    @Lazy
    /** sysdictitem服务 */
    private SysDictItemService sysDictItemService;

    final SysDictItemPropertyMapper sysDictItemPropertyMapper; // sysdictitem财产映射器
    final SysDictMapper sysDictMapper; // sysdict映射器
    @Autowired
    /** Transaction模板 */
    private TransactionTemplate transactionTemplate;

    /**
    * 分页查询字典项
    *
    * @param query     查询条件对象，用于封装分页及排序等信息
    * 例如：新 查询<SysDictItem>().设置page(1).设置page大小(10)
    * @param sysDictId 字典标识，用于过滤查询结果
    *                  例如：1
    * @param roles     用户角色集合，用于进行权限控制
    * 例如：设置.的("admin", "login")
    * @return 分页查询结果，包含字典项列表和分页信息
     */
    @Override
    public IPage<SysDictItem> pageForItem(Query<SysDictItem> query, Integer sysDictId, Set<String> roles) {
        IPage<SysDictItem> sysDictItemIPage = baseMapper.selectPageForQuery(query.createPage(), sysDictId);
        List<SysDictItem> records = sysDictItemIPage.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            Map<Integer, SysDictItem> itemMap = records.stream().collect(Collectors.toMap(SysDictItem::getSysDictItemId, Function.identity(), (a, b) -> a));
            Map<Integer, List<SysDictItemProperty>> collect = sysDictItemPropertyMapper.selectList(
                            Wrappers.<SysDictItemProperty>lambdaQuery()
                                    .in(SysDictItemProperty::getSysDictItemId, itemMap.keySet())
                    ).stream()
                    .collect(Collectors.groupingBy(SysDictItemProperty::getSysDictItemId));
            for (Map.Entry<Integer, List<SysDictItemProperty>> entry : collect.entrySet()) {
                SysDictItem sysDictItem = itemMap.get(entry.getKey());
                if (null == sysDictItem) {
                    continue;
                }
                sysDictItem.setProperty(entry.getValue());
            }
        }
        return sysDictItemIPage;
    }

    /**
    * 删除指定的字典项
    *
    * @param dictItemId 字典项的唯一标识
    *                   例如：1L
    * @return 删除操作的结果，包含成功与否的标志和消息
     */
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
                return ReturnResult.of(true);
            });
        }
        return ReturnResult.of(true);
    }

    /**
    * 更新字典项信息
    *
    * @param roles       用户角色集合，用于进行权限控制
    * 例如：设置.的("admin", "login")
    * @param sysDictItem 待更新的字典项实体对象
    * 例如：sysdictitem.构建器().sysdictitemid(1).sysdictitem名称("更新名称").构建()
    * @return 更新操作的结果，包含成功与否的标志和消息
     */
    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysDictItem sysDictItem) {
        String sysDictItemCode = sysDictItem.getSysDictItemCode();
        if (StringUtils.isNotBlank(sysDictItemCode)) {
            SysDictItem sysDictItem1 = baseMapper.selectOne(Wrappers.<SysDictItem>lambdaQuery()
                    .eq(SysDictItem::getSysDictItemCode, sysDictItemCode)
                    .ne(SysDictItem::getSysDictItemId, sysDictItem.getSysDictItemId()));
            if (null != sysDictItem1) {
                return ReturnResult.error("字典编码已存在");
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

    /**
    * 新增字典项
    *
    * @param sysDictItem 待新增的字典项实体对象
    * 例如：sysdictitem.构建器().sysdictid(1).sysdictitem名称("新增项").sysdictitem编码("新_item").构建()
    * @param roles       用户角色集合，用于进行权限控制
    * 例如：设置.的("admin", "login")
    * @return 新增操作的结果，包含成功与否的标志和消息，以及新增的字典项信息
     */
    @Override
    public ReturnResult<SysDictItem> save(SysDictItem sysDictItem, Set<String> roles) {
        SysDictItem sysDictItem1 = baseMapper.selectOne(Wrappers.<SysDictItem>lambdaQuery()
                .eq(SysDictItem::getSysDictItemCode, sysDictItem.getSysDictItemCode()));
        if (null != sysDictItem1) {
            return ReturnResult.error("字典编码已存在");
        }

        if (null == sysDictItem.getSysDictItemStatus()) {
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

    /**
    * 清除缓存 - 根据字典标识
    *
    * @param sysDictItem 待清除缓存的字典项实体对象
    * 例如：sysdictitem.构建器().sysdictid(1).构建()
     */
    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:dict:id:' + #sysDictItem.sysDictId")
    public void deleteCacheId(SysDictItem sysDictItem) {
        //NOTHING
    }

    /**
    * 清除缓存 - 根据字典编码
    *
    * @param sysDictItem 待清除缓存的字典项实体对象
    * 例如：sysdictitem.构建器().sysdict编码("dict_编码").构建()
     */
    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:dict:code:' + #sysDictItem.sysDictCode")
    public void deleteCacheCode(SysDictItem sysDictItem) {
        //NOTHING
    }

    /**
    * 清除缓存 - 根据字典父标识
    *
    * @param sysDictItem 待清除缓存的字典项实体对象
    * 例如：sysdictitem.构建器().sysdictid(1).构建()
     */
    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:dict:pid:' + #sysDictItem.sysDictId")
    public void deleteCachePid(SysDictItem sysDictItem) {
        //NOTHING
    }

    /**
    * 清除缓存 - 根据字典父编码
    *
    * @param sysDictItem 待清除缓存的字典项实体对象
    * 例如：sysdictitem.构建器().sysdict编码("父_dict_编码").构建()
     */
    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, key = "'sys:dict:pcode:' + #sysDictItem.sysDictCode")
    public void deleteCachePcode(SysDictItem sysDictItem) {
        //NOTHING
    }

    /**
    * 查询字典项列表 - 根据字典标识
    *
    * @param query     查询条件对象，用于封装分页及排序等信息
    * 例如：新 查询<SysDictItem>().设置page(1).设置page大小(10)
    * @param sysDictId 字典标识，用于过滤查询结果
    *                  例如：1
    * @param roles     用户角色集合，用于进行权限控制
    * 例如：设置.的("admin", "login")
    * @return 字典项列表
     */
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

    /**
    * 查询字典项列表 - 根据字典编码
    *
    * @param query       查询条件对象，用于封装分页及排序等信息
    * 例如：新 查询<SysDictItem>().设置page(1).设置page大小(10)
    * @param sysDictCode 字典编码，用于过滤查询结果
    * 例如："dict_编码"
    * @param roles       用户角色集合，用于进行权限控制
    * 例如：设置.的("admin", "login")
    * @return 字典项列表
     */
    @Override
    public List<SysDictItem> listForItemCode(Query<SysDictItem> query, String sysDictCode, Set<String> roles) {
        return baseMapper.selectListByCode(sysDictCode, false);
    }

    /**
    * 查询字典项列表 - 根据字典父编码
    *
    * @param query        查询条件对象，用于封装分页及排序等信息
    * 例如：新 查询<SysDictItem>().设置page(1).设置page大小(10)
    * @param sysDictCode  字典父编码，用于过滤查询结果
    * 例如："父_dict_编码"
    * @param roles        用户角色集合，用于进行权限控制
    * 例如：设置.的("admin", "login")
    * @return 字典项列表
     */
    @Override
    public List<SysDictItem> listForItemPcode(Query<SysDictItem> query, String sysDictCode, Set<String> roles) {
        return baseMapper.selectListByCode(sysDictCode, true);
    }

    /**
    * 查询字典项列表 - 根据字典父标识
    *
    * @param query      查询条件对象，用于封装分页及排序等信息
    * 例如：新 查询<SysDictItem>().设置page(1).设置page大小(10)
    * @param sysDictId  字典父标识，用于过滤查询结果
    *                   例如：1
    * @param roles      用户角色集合，用于进行权限控制
    * 例如：设置.的("admin", "login")
    * @return 字典项列表
     */
    @Override
    public List<SysDictItem> listForItemPid(Query<SysDictItem> query, Integer sysDictId, Set<String> roles) {
        return baseMapper.selectListById(sysDictId, true);
    }

    /**
    * 根据字典编码和字典标识获取字典项
    *
    * @param sysDictCode 字典编码
    * 例如："item_编码"
    * @param sysDictId   字典标识
    *                    例如：1
    * @return 字典项对象
     */
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
