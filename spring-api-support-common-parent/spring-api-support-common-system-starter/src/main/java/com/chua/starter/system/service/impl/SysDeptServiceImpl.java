package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.google.common.base.Joiner;
import com.chua.common.support.function.SafeConsumer;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.BeanUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.system.util.TreeNode;
import com.chua.starter.system.util.TreeUtils;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.system.mapper.SysDeptMapper;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.pojo.dept.SysDeptVO;
import com.chua.starter.system.service.SysDeptPrincipalRelService;
import com.chua.starter.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 * <p>提供部门的增删改查及树形结构转换功能</p>
 *
 * @since 2024/8/7
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    /**
     * sysdeptprincipalrel服务
    */
    private final SysDeptPrincipalRelService sysDeptPrincipalRelService;

    @Override
    /**
     * 列表
    */
    public List<SysDeptVO> list(SysDept sysDept, Set<String> roles) {
        List<SysDept> sysDepts = baseMapper.selectList(
                Wrappers.<SysDept>lambdaQuery()
                        .orderByAsc(SysDept::getSysDeptSort)
                        .orderByDesc(SysDept::getCreateTime)
        );
        sysDeptPrincipalRelService.fillPrincipalInfo(sysDepts);
        Map<Integer, Integer> memberCountMap = baseMapper.selectMemberCounts().stream()
                .filter(item -> null != item.get("sysDeptId"))
                .collect(Collectors.toMap(
                        item -> ((Number) item.get("sysDeptId")).intValue(),
                        item -> ((Number) item.get("memberCount")).intValue(),
                        (left, right) -> right
                ));
        sysDepts.forEach(item -> item.setMemberCount(memberCountMap.getOrDefault(item.getSysDeptId(), 0)));

        List<SysDeptVO> tree = TreeUtils.toTree(sysDepts, sysDept1 -> {
            SysDeptVO sysDeptVO = BeanUtils.copyProperties(sysDept1, SysDeptVO.class);
            TreeNode<SysDeptVO> treeNode = new TreeNode<>();
            treeNode.setId(String.valueOf(sysDeptVO.getSysDeptId()));
            treeNode.setPid(String.valueOf(sysDeptVO.getSysDeptPid()));
            treeNode.setExt(sysDeptVO);
            treeNode.setChildrenConsumer(new SafeConsumer<List<SysDeptVO>>() {
                @Override
                /**
                 * safeaccept
                */
                public void safeAccept(List<SysDeptVO> sysDeptVOS) throws Throwable {
                    sysDeptVO.setChildren(sysDeptVOS);
                }
            });
            return treeNode;
        });
        if (StringUtils.isEmpty(sysDept.getSysDeptName())) {
            return tree;
        }
        return filterTreeByName(tree, sysDept.getSysDeptName());
    }

    @Override
    /**
     * 删除
    */
    public ReturnResult<Boolean> delete(Long deptId) {
        return ReturnResult.of(baseMapper.deleteById(deptId) > 0);
    }

    @Override
    /**
     * 更新
    */
    public ReturnResult<Boolean> update(Set<String> roles, SysDept sysDept) {
        if (null != sysDept.getSysDeptPid() && sysDept.getSysDeptPid() != SystemConstant.ROOT_NODE_ID) {
            SysDept parentSysDept = baseMapper.selectById(sysDept.getSysDeptPid());
            if(null == parentSysDept) {
                return ReturnResult.illegal("上级部门不存在");
            }
            if (StringUtils.transArrayContains(parentSysDept.getSysDeptTreeId(), String.valueOf(sysDept.getSysDeptId()))) {
                return ReturnResult.error("涓嶈兘选择鑷繁/子级浣滀负父级菜单");
            }

            String menuTreeId = Joiner.on(",").join(parentSysDept.getSysDeptTreeId(), sysDept.getSysDeptId());
            sysDept.setSysDeptTreeId(menuTreeId);
        }

        // 唯一性校验: 机构编码全局唯一, 同级部门名称唯一 (排除自身)
        if (StringUtils.isNotBlank(sysDept.getSysDeptCode())
                && count(Wrappers.<SysDept>lambdaQuery()
                .eq(SysDept::getSysDeptCode, sysDept.getSysDeptCode())
                .ne(SysDept::getSysDeptId, sysDept.getSysDeptId())) > 0) {
            return ReturnResult.illegal("机构编码已存在");
        }
        Integer deptPid = resolvePidForUniqueness(sysDept);
        String deptName = sysDept.getSysDeptName();
        if (StringUtils.isNotBlank(deptName)
                && count(Wrappers.<SysDept>lambdaQuery()
                .eq(SysDept::getSysDeptPid, deptPid)
                .eq(SysDept::getSysDeptName, deptName)
                .ne(SysDept::getSysDeptId, sysDept.getSysDeptId())) > 0) {
            return ReturnResult.illegal("同级部门名称已存在");
        }

        boolean updated = baseMapper.updateById(sysDept) > 0;
        if (updated) {
            sysDeptPrincipalRelService.syncDeptPrincipals(sysDept.getSysDeptId(), sysDept.getPrincipalUserIds());
        }
        return ReturnResult.of(updated);
    }

    /**
     * 解析唯一性校验使用的父级部门ID:
     * 更新请求未携带 sysDeptPid 时, 回退到数据库中的当前父级, 避免误判同名。
     *
     * @param sysDept 部门信息
     * @return 父级部门ID (根节点返回 ROOT_NODE_ID)
     */
    private Integer resolvePidForUniqueness(SysDept sysDept) {
        if (null != sysDept.getSysDeptPid()) {
            return sysDept.getSysDeptPid();
        }
        SysDept current = baseMapper.selectById(sysDept.getSysDeptId());
        return (null != current && null != current.getSysDeptPid())
                ? current.getSysDeptPid() : SystemConstant.ROOT_NODE_ID;
    }

    @Override
    /**
     * 保存
    */
    public ReturnResult<SysDept> save(SysDept sysDept, Set<String> roles) {
        String menuTreeId = "";
        String parentTreeId = "";
        if (null == sysDept.getSysDeptPid() || sysDept.getSysDeptPid() == SystemConstant.ROOT_NODE_ID) {
            sysDept.setSysDeptPid(SystemConstant.ROOT_NODE_ID);
            parentTreeId = String.valueOf(SystemConstant.ROOT_NODE_ID);
        } else {
            SysDept parentSysDept = baseMapper.selectById(sysDept.getSysDeptPid());
            if(null == parentSysDept) {
                return ReturnResult.illegal("上级部门不存在");
            }

            if (StringUtils.transArrayContains(parentSysDept.getSysDeptTreeId(), String.valueOf(sysDept.getSysDeptId()))) {
                return ReturnResult.error("涓嶈兘选择鑷繁/子级浣滀负父级菜单");
            }

            parentTreeId = parentSysDept.getSysDeptTreeId();
        }

        // 唯一性校验: 机构编码全局唯一, 同级部门名称唯一
        if (StringUtils.isNotBlank(sysDept.getSysDeptCode())
                && count(Wrappers.<SysDept>lambdaQuery()
                .eq(SysDept::getSysDeptCode, sysDept.getSysDeptCode())) > 0) {
            return ReturnResult.illegal("机构编码已存在");
        }
        String deptName = sysDept.getSysDeptName();
        if (StringUtils.isNotBlank(deptName)
                && count(Wrappers.<SysDept>lambdaQuery()
                .eq(SysDept::getSysDeptPid, sysDept.getSysDeptPid())
                .eq(SysDept::getSysDeptName, deptName)) > 0) {
            return ReturnResult.illegal("同级部门名称已存在");
        }

        baseMapper.insert(sysDept);
        sysDept.setSysDeptTreeId(Joiner.on(",").join(parentTreeId, sysDept.getSysDeptId()));
        baseMapper.updateById(sysDept);
        sysDeptPrincipalRelService.syncDeptPrincipals(sysDept.getSysDeptId(), sysDept.getPrincipalUserIds());
        return ReturnResult.of(sysDept);
    }

    /**
     * 过滤树by名称
     *
     * @param source 源
     * @param keyword keyword
     * @return 过滤器树by名称的结果
     */
    private List<SysDeptVO> filterTreeByName(List<SysDeptVO> source, String keyword) {
        if (null == source || source.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysDeptVO> result = new ArrayList<>();
        for (SysDeptVO item : source) {
            SysDeptVO filtered = filterTreeNode(item, keyword);
            if (null != filtered) {
                result.add(filtered);
            }
        }
        return result;
    }

    /**
     * 过滤树节点
     *
     * @param node 节点
     * @param keyword keyword
     * @return 过滤器树节点的结果
     */
    private SysDeptVO filterTreeNode(SysDeptVO node, String keyword) {
        List<SysDeptVO> matchedChildren = filterTreeByName(node.getChildren(), keyword);
        boolean selfMatched = startsWithIgnoreCase(node.getSysDeptName(), keyword);
        if (!selfMatched && matchedChildren.isEmpty()) {
            return null;
        }
        SysDeptVO copy = BeanUtils.copyProperties(node, SysDeptVO.class);
        copy.setChildren(matchedChildren);
        return copy;
    }

    /**
     * 启动withignore大小写
     *
     * @param value 值
     * @param prefix 前缀
     * @return 启动withignore大小写的结果
     */
    private boolean startsWithIgnoreCase(String value, String prefix) {
        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(prefix)) {
            return false;
        }
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
