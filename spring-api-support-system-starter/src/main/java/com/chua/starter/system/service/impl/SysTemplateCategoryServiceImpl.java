package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.function.Joiner;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.treenode.TreeNode;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.TreeUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysTemplate;
import com.chua.starter.system.entity.SysTemplateCategory;
import com.chua.starter.system.mapper.SysTemplateCategoryMapper;
import com.chua.starter.system.mapper.SysTemplateMapper;
import com.chua.starter.system.pojo.category.SysTemplateCategoryTree;
import com.chua.starter.system.service.SysTemplateCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;

/**
 *
 * @since 2024/9/5
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysTemplateCategoryServiceImpl extends ServiceImpl<SysTemplateCategoryMapper, SysTemplateCategory> implements SysTemplateCategoryService{

    final TransactionTemplate transactionTemplate;
    final SysTemplateMapper sysTemplateMapper;

    @Override
    public IPage<SysTemplateCategory> page(Query<SysTemplateCategory> query, SysTemplateCategory sysTemplateGroup, Set<String> roles) {
        return baseMapper.selectPageByRoles(query.createPage(), sysTemplateGroup, AuthConstant.hasAdmin(roles));
    }

    @Override
    public List<SysTemplateCategory> list(Query<SysTemplateCategory> query, SysTemplateCategory sysTemplateCategory, Set<String> roles) {
        return baseMapper.selectListByRoles(query.createPage(), sysTemplateCategory, AuthConstant.hasAdmin(roles));
    }

    @Override
    public ReturnResult<Boolean> delete(Long sysTemplateId) {
        if(null == sysTemplateId) {
            return ReturnResult.fail("模板ID不能为空");
        }
        return ReturnResult.guess(transactionTemplate.execute(stats -> {
            int deleted = baseMapper.deleteById(sysTemplateId);
            sysTemplateMapper.delete(Wrappers.<SysTemplate>lambdaQuery().eq(SysTemplate::getSysTemplateGroupId, sysTemplateId));
            return deleted > 0;
        }));
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysTemplateCategory sysTemplateGroup) {
        String sysTemplateGroupCode = sysTemplateGroup.getSysTemplateCategoryCode();
        if(StringUtils.isNotBlank(sysTemplateGroupCode)) {
            SysTemplateCategory templateGroup1 = baseMapper.selectOne(Wrappers.<SysTemplateCategory>lambdaQuery()
                    .eq(SysTemplateCategory::getSysTemplateCategoryCode, sysTemplateGroupCode)
                    .ne(SysTemplateCategory::getSysTemplateCategoryId, sysTemplateGroup.getSysTemplateCategoryId()));
            if(null != templateGroup1) {
                return ReturnResult.fail("编码已存在");
            }
        }

        Integer sysTemplateCategoryPid = sysTemplateGroup.getSysTemplateCategoryPid();
        String treeId = "";
        if(null == sysTemplateCategoryPid || sysTemplateCategoryPid == 0) {
            sysTemplateGroup.setSysTemplateCategoryPid(0);
            treeId = Joiner.on(",").join( 0, sysTemplateGroup.getSysTemplateCategoryId());
        } else {
            SysTemplateCategory sysTemplateCategory = baseMapper.selectById(sysTemplateCategoryPid);
            if(null == sysTemplateCategory) {
                return ReturnResult.error("父级菜单不存在");
            }
            treeId = Joiner.on(",").join( sysTemplateCategory.getSysTemplateCategoryTreeId(), sysTemplateGroup.getSysTemplateCategoryId());
        }
        sysTemplateGroup.setSysTemplateCategoryTreeId(treeId);


        sysTemplateGroup.setSysTemplateCategoryDelete(null);
        return ReturnResult.of(baseMapper.updateById(sysTemplateGroup) > 0);
    }

    @Override
    public ReturnResult<SysTemplateCategory> save(SysTemplateCategory sysTemplateGroup, Set<String> roles) {
        SysTemplateCategory templateGroup = baseMapper.selectOne(Wrappers.<SysTemplateCategory>lambdaQuery()
                .eq(SysTemplateCategory::getSysTemplateCategoryCode, sysTemplateGroup.getSysTemplateCategoryCode()));
        if(null != templateGroup) {
            return ReturnResult.fail("字典编码已存在");
        }

        Integer sysTemplateCategoryPid = sysTemplateGroup.getSysTemplateCategoryPid();
        String treeId = "";
        if(null == sysTemplateCategoryPid || sysTemplateCategoryPid == 0) {
            sysTemplateGroup.setSysTemplateCategoryPid(0);
            treeId = Joiner.on(",").join( 0, sysTemplateGroup.getSysTemplateCategoryId());
        } else {
            SysTemplateCategory sysTemplateCategory = baseMapper.selectById(sysTemplateCategoryPid);
            if(null == sysTemplateCategory) {
                return ReturnResult.error("父级菜单不存在");
            }
            treeId = Joiner.on(",").join( sysTemplateCategory.getSysTemplateCategoryTreeId(), sysTemplateGroup.getSysTemplateCategoryId());
        }
        sysTemplateGroup.setSysTemplateCategoryTreeId(treeId);

        sysTemplateGroup.setSysTemplateCategoryDelete(0);
        if(baseMapper.insert(sysTemplateGroup) > 0) {
            return ReturnResult.of(sysTemplateGroup);
        }
        return ReturnResult.illegal("新增失败");
    }

    @Override
    public ReturnResult<Boolean> rollback(Set<String> roles, SysTemplateCategory sysTemplateGroup) {
        sysTemplateGroup.setSysTemplateCategoryDelete(0);
        return ReturnResult.of(baseMapper.updateById(sysTemplateGroup) > 0);
    }

    @Override
    public ReturnResult<List<SysTemplateCategoryTree>> tree(Query<SysTemplateCategory> query, SysTemplateCategory sysTemplateCategory, Set<String> roles) {
        List<SysTemplateCategory> sysTemplateCategories = baseMapper.selectListByRoles(query.createPage(), sysTemplateCategory, AuthConstant.hasAdmin(roles));
        return ReturnResult.ok(TreeUtils.toTree(sysTemplateCategories, item -> {
            SysTemplateCategoryTree sysTemplateCategoryTree = new SysTemplateCategoryTree();
            BeanUtils.copyProperties(item, sysTemplateCategoryTree);

            TreeNode<SysTemplateCategoryTree> treeNode = new TreeNode<>();
            treeNode.setId(sysTemplateCategoryTree.getSysTemplateCategoryId().toString());
            treeNode.setLabel(sysTemplateCategoryTree.getSysTemplateCategoryName());
            treeNode.setValue(sysTemplateCategoryTree.getSysTemplateCategoryName());
            treeNode.setPid(sysTemplateCategoryTree.getSysTemplateCategoryPid().toString());
            treeNode.setExt(sysTemplateCategoryTree);
            treeNode.setChildrenConsumer(sysTemplateCategoryTree::setChildren);

            return treeNode;
        }));
    }
}
