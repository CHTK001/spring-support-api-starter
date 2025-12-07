package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.function.Joiner;
import com.chua.common.support.function.SafeConsumer;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.treenode.TreeNode;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.TreeUtils;
import com.chua.starter.entity.system.SysDept;
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
                        .likeRight(StringUtils.isNotEmpty(sysDept.getSysDeptName()), SysDept::getSysDeptName, sysDept.getSysDeptName())
                        .orderByAsc(SysDept::getSysDeptSort)
                        .orderByDesc(SysDept::getCreateTime)
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
        if (null != sysDept.getSysDeptPid() && sysDept.getSysDeptPid() != 0) {
            SysDept parentSysDept = baseMapper.selectById(sysDept.getSysDeptPid());
            if(null == parentSysDept) {
                return ReturnResult.illegal("涓婄骇閮ㄩ棬涓嶅瓨鍦");
            }
            if (StringUtils.transArrayContains(parentSysDept.getSysDeptTreeId(), String.valueOf(sysDept.getSysDeptId()))) {
                return ReturnResult.error("涓嶈兘閫夋嫨鑷繁/瀛愮骇浣滀负鐖剁骇鑿滃崟");
            }

            String menuTreeId = Joiner.on(",").join(parentSysDept.getSysDeptTreeId(), sysDept.getSysDeptId());
            sysDept.setSysDeptTreeId(menuTreeId);
        }
        return ReturnResult.guess(baseMapper.updateById(sysDept) > 0);
    }

    @Override
    public ReturnResult<SysDept> save(SysDept sysDept, Set<String> roles) {
        String menuTreeId = "";
        String parentTreeId = "";
        if (null == sysDept.getSysDeptPid() || sysDept.getSysDeptPid() == 0) {
            sysDept.setSysDeptPid(0);
            parentTreeId = "0";
        } else {
            SysDept parentSysDept = baseMapper.selectById(sysDept.getSysDeptPid());
            if(null == parentSysDept) {
                return ReturnResult.illegal("涓婄骇閮ㄩ棬涓嶅瓨鍦");
            }

            if (StringUtils.transArrayContains(parentSysDept.getSysDeptTreeId(), String.valueOf(sysDept.getSysDeptId()))) {
                return ReturnResult.error("涓嶈兘閫夋嫨鑷繁/瀛愮骇浣滀负鐖剁骇鑿滃崟");
            }

            parentTreeId = parentSysDept.getSysDeptTreeId();
        }
        baseMapper.insert(sysDept);
        sysDept.setSysDeptTreeId(Joiner.on(",").join(parentTreeId, sysDept.getSysDeptId()));
        baseMapper.updateById(sysDept);
        return ReturnResult.guess(sysDept);
    }
}
