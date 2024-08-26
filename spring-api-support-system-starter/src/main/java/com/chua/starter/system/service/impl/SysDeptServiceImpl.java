package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.function.Joiner;
import com.chua.common.support.function.SafeConsumer;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.treenode.TreeNode;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.TreeUtils;
import com.chua.starter.system.entity.SysDept;
import com.chua.starter.system.mapper.SysDeptMapper;
import com.chua.starter.system.pojo.dept.SysDeptVO;
import com.chua.starter.system.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService{

    @Override
    public List<SysDeptVO> list(SysDept sysDept, Set<String> roles) {
        List<SysDept> sysDepts = baseMapper.selectList(
                Wrappers.<SysDept>lambdaQuery()
                        .like(ObjectUtils.isNotEmpty(sysDept.getSysDeptName()), SysDept::getSysDeptName, sysDept.getSysDeptName())
        );

        return TreeUtils.toTree(sysDepts, sysDept1 -> {
            SysDeptVO sysDeptVO = BeanUtils.copyProperties(sysDept1, SysDeptVO.class);
            TreeNode<SysDeptVO> treeNode = new TreeNode<>();
            treeNode.setId(String.valueOf(sysDeptVO.getSysDeptId()));
            treeNode.setPid(String.valueOf(sysDeptVO.getSysDeptPid()));
            treeNode.setExt(sysDeptVO);
            treeNode.setChildrenConsumer(new SafeConsumer<List<SysDeptVO>>() {
                @Override
                public void safeAccept(List<SysDeptVO> sysDeptVOS) throws Throwable {
                    sysDeptVO.setChildren(sysDeptVOS);
                }
            });
            return treeNode;
        });
    }

    @Override
    public ReturnResult<Boolean> delete(Long deptId) {
        return ReturnResult.guess(baseMapper.deleteById(deptId) > 0);
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysDept sysDept) {
        String menuTreeId = "";
        if(null == sysDept.getSysDeptPid()) {
            sysDept.setSysDeptPid(0);
            menuTreeId = Joiner.on(",").join( 0, sysDept.getSysDeptId());
        } else {
            SysDept parentSysDept = baseMapper.selectById(sysDept.getSysDeptPid());
            if(null == parentSysDept) {
                return ReturnResult.illegal("上级部门不存在");
            }
            if(StringUtils.transArrayContains(parentSysDept.getSysDeptTreeId(), String.valueOf(parentSysDept.getSysDeptId()))) {
                return ReturnResult.error("不能选择自己作为父级菜单");
            }

            menuTreeId = Joiner.on(",").join( parentSysDept.getSysDeptTreeId(), sysDept.getSysDeptId());
        }
        sysDept.setSysDeptTreeId(menuTreeId);
        return ReturnResult.guess(baseMapper.updateById(sysDept) > 0);
    }

    @Override
    public ReturnResult<SysDept> save(SysDept sysDept, Set<String> roles) {
        String menuTreeId = "";
        if(null == sysDept.getSysDeptPid()) {
            sysDept.setSysDeptPid(0);
            menuTreeId = Joiner.on(",").join( 0, sysDept.getSysDeptId());
        } else {
            SysDept parentSysDept = baseMapper.selectById(sysDept.getSysDeptPid());
            if(null == parentSysDept) {
                return ReturnResult.illegal("上级部门不存在");
            }

            if(StringUtils.transArrayContains(parentSysDept.getSysDeptTreeId(), String.valueOf(parentSysDept.getSysDeptId()))) {
                return ReturnResult.error("不能选择自己作为父级菜单");
            }

            menuTreeId = Joiner.on(",").join( parentSysDept.getSysDeptTreeId(), sysDept.getSysDeptId());
        }
        sysDept.setSysDeptTreeId(menuTreeId);
        baseMapper.insert(sysDept);
        return ReturnResult.guess(sysDept);
    }
}
