package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.entity.system.SysDeptPrincipalRel;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.mapper.SysDeptMapper;
import com.chua.starter.system.mapper.SysDeptPrincipalRelMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.service.SysDeptPrincipalRelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* 部门负责人关系服务实现
*
* @since 2026/4/5
* @author CH
 */
@Service
@RequiredArgsConstructor
public class SysDeptPrincipalRelServiceImpl extends ServiceImpl<SysDeptPrincipalRelMapper, SysDeptPrincipalRel>
        implements SysDeptPrincipalRelService {

    /** SYS用户映射器 */
    private final SysUserMapper sysUserMapper;
    /** sysdeptmapper */
    private final SysDeptMapper sysDeptMapper;

    @Override
    /** 同步deptprincipals */
    public void syncDeptPrincipals(Integer deptId, Collection<Integer> userIds) {
        if (deptId == null) {
            return;
        }

        remove(Wrappers.<SysDeptPrincipalRel>lambdaQuery().eq(SysDeptPrincipalRel::getSysDeptId, deptId));

        List<Integer> normalizedUserIds = normalizeIds(userIds);
        if (!normalizedUserIds.isEmpty()) {
            saveBatch(normalizedUserIds.stream().map(userId -> {
                SysDeptPrincipalRel item = new SysDeptPrincipalRel();
                item.setSysDeptId(deptId);
                item.setSysUserId(userId);
                return item;
            }).collect(Collectors.toList()));
        }

        SysDept update = new SysDept();
        update.setSysDeptId(deptId);
        update.setSysDeptPrincipal(String.join("、", resolvePrincipalNames(normalizedUserIds)));
        sysDeptMapper.updateById(update);
    }

    @Override
    /** 列表manageddeptscopes */
    public List<SysDept> listManagedDeptScopes(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectManagedDeptScopesByUserId(userId);
    }

    @Override
    /** 列表manageddept标识 */
    public List<Integer> listManagedDeptIds(Integer userId) {
        return listManagedDeptScopes(userId).stream()
                .map(SysDept::getSysDeptId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    /** 列表manageddept树标识 */
    public List<String> listManagedDeptTreeIds(Integer userId) {
        return listManagedDeptScopes(userId).stream()
                .map(SysDept::getSysDeptTreeId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    /** 列表manageddept树标识 */
    public List<String> listManagedDeptTreeIds(Collection<Integer> deptIds) {
        if (CollectionUtils.isEmpty(deptIds)) {
            return Collections.emptyList();
        }
        return sysDeptMapper.selectBatchIds(deptIds).stream()
                .map(SysDept::getSysDeptTreeId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    /** fillprincipal信息 */
    public void fillPrincipalInfo(List<? extends SysDept> departments) {
        if (CollectionUtils.isEmpty(departments)) {
            return;
        }

        Map<Integer, List<Integer>> principalUserIdMap = mapPrincipalUserIds(
                departments.stream()
                        .map(SysDept::getSysDeptId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));
        Set<Integer> userIds = principalUserIdMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Integer, String> principalNameMap = resolvePrincipalNameMap(userIds);

        for (SysDept department : departments) {
            List<Integer> principalUserIds = principalUserIdMap.getOrDefault(department.getSysDeptId(), Collections.emptyList());
            List<String> principalNames = principalUserIds.stream()
                    .map(principalNameMap::get)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());
            department.setPrincipalUserIds(principalUserIds);
            department.setPrincipalUserNames(principalNames);
            if (StringUtils.isEmpty(department.getSysDeptPrincipal()) && !principalNames.isEmpty()) {
                department.setSysDeptPrincipal(String.join("、", principalNames));
            }
        }
    }

    @Override
    /** 映射principal用户标识 */
    public Map<Integer, List<Integer>> mapPrincipalUserIds(Collection<Integer> deptIds) {
        if (CollectionUtils.isEmpty(deptIds)) {
            return Collections.emptyMap();
        }

        return baseMapper.selectList(
                        Wrappers.<SysDeptPrincipalRel>lambdaQuery()
                                .in(SysDeptPrincipalRel::getSysDeptId, deptIds))
                .stream()
                .filter(item -> item.getSysDeptId() != null && item.getSysUserId() != null)
                .collect(Collectors.groupingBy(
                        SysDeptPrincipalRel::getSysDeptId,
                        LinkedHashMap::new,
                        Collectors.mapping(SysDeptPrincipalRel::getSysUserId, Collectors.toList())));
    }

    /**
    * normalize标识
    *
    * @param userIds 用户标识
    * @return normalize标识的结果
     */
    private List<Integer> normalizeIds(Collection<Integer> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
    * 解析principal名称
    *
    * @param userIds 用户标识
    * @return resolveprincipal名称的结果
     */
    private List<String> resolvePrincipalNames(Collection<Integer> userIds) {
        return resolvePrincipalNameMap(userIds).values().stream().toList();
    }

    /**
    * 解析principal名称映射
    *
    * @param userIds 用户标识
    * @return resolveprincipal名称映射的结果
     */
    private Map<Integer, String> resolvePrincipalNameMap(Collection<Integer> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        return sysUserMapper.selectBatchIds(userIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        SysUser::getSysUserId,
                        user -> StringUtils.isNotEmpty(user.getSysUserNickname())
                                ? user.getSysUserNickname()
                                : user.getSysUserUsername(),
                        (left, right) -> left,
                        LinkedHashMap::new));
    }
}
