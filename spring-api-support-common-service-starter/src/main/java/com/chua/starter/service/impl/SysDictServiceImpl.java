package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.SysDictService;
import com.chua.starter.service.mapper.SysDictItemMapper;
import com.chua.starter.service.mapper.SysDictMapper;
import com.chua.starter.service.entity.SysDict;
import com.chua.starter.service.entity.SysDictItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统字典服务实现类
 *
 * @author CH
 * @since 2024/8/7
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    /**
     * 事务模板，用于执行事务操作
     */
    final TransactionTemplate transactionTemplate;

    /**
     * 字典项Mapper，用于操作字典项数据
     */
    final SysDictItemMapper sysDictItemMapper;

    /**
     * 分页查询系统字典信息
     * 根据查询条件和用户角色进行筛选，只查询一级字典及其子级
     *
     * @param query   分页查询对象，包含分页信息和排序规则
     *                示例: {
     *                "page": 1,
     *                "pageSize": 10,
     *                "order": "createTime desc"
     *                }
     * @param sysDict 系统字典查询条件对象
     *                示例: {
     *                "sysDictCode": "USER_TYPE",
     *                "sysDictName": "用户类型",
     *                "sysDictRemark": "用户类型字典"
     *                }
     * @param roles   用户角色集合，用于权限控制
     *                示例: ["ADMIN", "USER"]
     * @return 分页查询结果，包含查询到的系统字典信息及子级
     * 示例: {
     * "records": [
     * {
     * "sysDictId": 1,
     * "sysDictPid": 0,
     * "sysDictName": "用户类型",
     * "sysDictCode": "USER_TYPE",
     * "children": [
     * {
     * "sysDictId": 2,
     * "sysDictPid": 1,
     * "sysDictName": "普通用户",
     * "sysDictCode": "NORMAL_USER"
     * }
     * ]
     * }
     * ],
     * "total": 1
     * }
     */
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

    /**
     * 删除指定的系统字典
     * 同时删除该字典下的所有字典项
     *
     * @param dictId 字典ID，唯一标识要删除的字典
     *               示例: 1
     * @return 删除操作的结果，包含成功与否的标志
     * 示例: {
     * "code": 200,
     * "data": true,
     * "message": "success"
     * }
     */
    @Override
    public ReturnResult<Boolean> delete(Long dictId) {
        if (null == dictId) {
            return ReturnResult.fail("字典ID不能为空");
        }

        SysDict sysDict = baseMapper.selectById(dictId);
        if (null == sysDict) {
            return ReturnResult.fail("字典不存在, 请刷新后重试!");
        }

        Integer sysRoleInSystem = sysDict.getSysDictInSystem();

        if (null != sysRoleInSystem && sysRoleInSystem == 1) {
            return ReturnResult.fail("内置字典不允许删除");
        }

        return ReturnResult.guess(transactionTemplate.execute(stats -> {
            int deleted = baseMapper.deleteById(dictId);
            sysDictItemMapper.delete(Wrappers.<SysDictItem>lambdaQuery().eq(SysDictItem::getSysDictId, dictId));
            return deleted > 0;
        }));
    }

    /**
     * 更新系统字典信息
     * 管理员可以修改所有字典，非管理员不能修改内置字典且不能修改是否内置字段
     *
     * @param roles   用户角色集合，用于权限控制
     *                示例: ["ADMIN"]
     * @param sysDict 待更新的系统字典对象
     *                示例: {
     *                "sysDictId": 1,
     *                "sysDictName": "更新后的用户类型",
     *                "sysDictCode": "USER_TYPE",
     *                "sysDictPid": 0
     *                }
     * @return 更新操作的结果，包含成功与否的标志
     * 示例: {
     * "code": 200,
     * "data": true,
     * "message": "success"
     * }
     */
    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysDict sysDict) {
        SysDict sysDict1 = baseMapper.selectById(sysDict.getSysDictId());
        Integer sysRoleInSystem = sysDict1.getSysDictInSystem();
        if (null != sysRoleInSystem && sysRoleInSystem == 1 && !AuthConstant.hasAdmin(roles)) {
            return ReturnResult.fail("内置字典不允许修改");
        }

        if (!AuthConstant.hasAdmin(roles)) {
            sysDict.setSysDictInSystem(null);
        }

        Integer sysDictPid = sysDict.getSysDictPid();
        if (null != sysDictPid && sysDictPid != 0) {
            SysDict sysDict11 = baseMapper.selectById(sysDictPid);
            if (null == sysDict11) {
                return ReturnResult.fail("父级字典不存在");
            }

            if (null != sysDict11.getSysDictPid() && sysDict11.getSysDictPid() != 0) {
                return ReturnResult.fail("只允许挂载在一级节点上");
            }
        }

        String sysRoleCode = sysDict.getSysDictCode();
        if (StringUtils.isNotBlank(sysRoleCode)) {
            SysDict sysRole11 = baseMapper.selectOne(Wrappers.<SysDict>lambdaQuery()
                    .eq(SysDict::getSysDictCode, sysRoleCode)
                    .ne(SysDict::getSysDictId, sysDict.getSysDictId()));
            if (null != sysRole11) {
                return ReturnResult.fail("字典编码已存在");
            }
        }

        return ReturnResult.of(baseMapper.updateById(sysDict) > 0);
    }

    /**
     * 保存新的系统字典信息
     * 新增的字典默认为非内置状态且状态为显示
     *
     * @param sysDict 待保存的系统字典对象
     *                示例: {
     *                "sysDictName": "新字典",
     *                "sysDictCode": "NEW_DICT",
     *                "sysDictPid": 0,
     *                "sysDictRemark": "这是一个新字典"
     *                }
     * @param roles   用户角色集合，用于权限控制（当前版本未使用）
     *                示例: ["ADMIN"]
     * @return 保存操作的结果，包含成功与否的标志以及保存的字典信息
     *         示例: {
     *         "code": 200,
     *         "data": {
     *         "sysDictId": 3,
     *         "sysDictName": "新字典",
     *         "sysDictCode": "NEW_DICT",
     *         "sysDictPid": 0,
     *         "sysDictStatus": 1,
     *         "sysDictInSystem": 0
     *         },
     *         "message": "success"
     *         }
     */
    @Override
    public ReturnResult<SysDict> save(SysDict sysDict, Set<String> roles) {
        sysDict.setSysDictInSystem(0);
        sysDict.setSysDictStatus(1);
        SysDict sysRole1 = baseMapper.selectOne(Wrappers.<SysDict>lambdaQuery()
                .eq(SysDict::getSysDictCode, sysDict.getSysDictCode()));

        Integer sysDictPid = sysDict.getSysDictPid();
        if (null == sysDictPid || sysDictPid == 0) {
            sysDict.setSysDictPid(0);
        } else {
            SysDict sysDict1 = baseMapper.selectById(sysDictPid);
            if (null == sysDict1) {
                return ReturnResult.fail("父级字典不存在");
            }

            if (null != sysDict1.getSysDictPid() && sysDict1.getSysDictPid() != 0) {
                return ReturnResult.fail("只允许挂载在一级节点上");
            }
        }

        if (null != sysRole1) {
            return ReturnResult.fail("字典编码已存在");
        }
        if (baseMapper.insert(sysDict) > 0) {
            return ReturnResult.of(sysDict);
        }
        return ReturnResult.illegal("新增失败");
    }

}
