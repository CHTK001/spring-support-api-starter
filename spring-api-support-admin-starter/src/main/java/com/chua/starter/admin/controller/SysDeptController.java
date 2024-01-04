package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.treenode.CustomTreeNode;
import com.chua.starter.common.support.logger.Logger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.admin.entity.SysDept;
import com.chua.starter.admin.node.SysDeptNode;
import com.chua.starter.admin.service.SysDeptService;
import com.chua.starter.admin.utils.ServicePageResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.chua.starter.common.support.constant.Constant.SYSTEM;

/**
 * 机构
 * @author CH
 */
@Api(tags = "机构接口")
@RestController
@RequestMapping("/v2/dept")
@RequiredArgsConstructor
public class SysDeptController {


    private final SysDeptService sysDeptService;
    /**
     * 机构树
     * @return 机构树
     */
    @Operation(summary = "机构树")
    @GetMapping("tree")
    @Cacheable(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dept-tree'")
    public ReturnResult<List<SysDeptNode>> tree() {
        CustomTreeNode<SysDeptNode, Long> customTreeNode = new CustomTreeNode<>(SysDeptNode::getDeptId, SysDeptNode::getDeptParentId, 0L);
        List<SysDept> list = sysDeptService.list();
        for (SysDept sysDept : list) {
            SysDeptNode node = new SysDeptNode();
            BeanUtils.copyProperties(sysDept, node);

            customTreeNode.add(node);
        }
        return Result.ok(customTreeNode.transferAll());
    }
    /**
     * 机构列表
     * @return 机构列表
     */
    @Operation(summary = "机构列表")
    @GetMapping("list")
    @Cacheable(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dept'")
    public ReturnResult<List<SysDept>> list() {
        return Result.ok(sysDeptService.list());
    }
    /**
     * 机构分页列表
     * @return 机构分页列表
     */
    @Operation(summary = "机构分页列表")
    @GetMapping("page")
    public ReturnPageResult<SysDept> page(
            String keyword,
            @ApiParam("页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @ApiParam("数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
            ) {
        return ServicePageResultUtils.ok(sysDeptService.page(new Page<>(page, pageSize),
                Wrappers.<SysDept>lambdaQuery()
                        .like(StringUtils.isNotBlank(keyword), SysDept::getDeptName, keyword)
                        .or().like(StringUtils.isNotBlank(keyword), SysDept::getDeptMarker, keyword)
                ));
    }
    /**
     * 新增组织机构
     * @return 新增组织机构
     */
    @Operation(summary = "新增组织机构")
    @PostMapping("save")
    @Caching(evict = {
            @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dept'"),
            @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dept-tree'"),
    })
    public ReturnResult<SysDept> save(@RequestBody SysDept sysDept) {
        if(StringUtils.isBlank(sysDept.getDeptTreePath())) {
            return Result.illegal("DeptTree不能为空, 且应该为当前节点的所有父节点ID");
        }

        sysDeptService.save(sysDept);
        return Result.ok(sysDept);
    }
    /**
     * 更新组织机构
     * @return 更新组织机构
     */
    @Operation(summary = "更新组织机构")
    @PutMapping("update")
    @Caching(evict = {
            @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dept'"),
            @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dept-tree'"),
    })
    public ReturnResult<SysDept> update(@RequestBody SysDept sysDept) {
        if(null == sysDept.getDeptId()) {
            return Result.illegal("组织编码不能为空");
        }

        if(StringUtils.isBlank(sysDept.getDeptTreePath())) {
            return Result.illegal("DeptTree不能为空, 且应该为当前节点的所有父节点ID");
        }

        Long deptParentId = sysDept.getDeptParentId();
        if(deptParentId != null) {
            if(sysDept.getDeptId().equals(deptParentId)) {
                return Result.illegal("上级部门不能是本身");
            }
            List<String> strings = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(sysDept.getDeptTreePath());
            int selfIndex = strings.indexOf(sysDept.getDeptId() + "");
            int parentIndex = strings.indexOf(deptParentId + "");
            if(selfIndex > -1 && selfIndex <= parentIndex) {
                return Result.illegal("上级部门不能是本身或者它的子部门");
            }
        }

        sysDeptService.updateById(sysDept);
        return Result.ok(sysDept);
    }
    /**
     * 删除组织机构
     * @return 删除组织机构
     */
    @Operation(summary = "删除组织机构")
    @DeleteMapping("delete")
    @Logger
    @Caching(evict = {
            @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dept'"),
            @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dept-tree'"),
    })
    public ReturnResult<Long> delete(Long deptId) {
        if(null == deptId) {
            return Result.illegal("组织编码不能为空");
        }

        sysDeptService.removeById(deptId);
        return Result.ok(deptId);
    }
    /**
     * 批量删除组织机构
     * @return 批量删除组织机构
     */
    @Operation(summary = "批量删除组织机构")
    @DeleteMapping("batchDelete")
    @Caching(evict = {
            @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dept'"),
            @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'dept-tree'"),
    })
    public ReturnResult<Void> batchDelete(String sysDeptId) {
        if(StringUtils.isBlank(sysDeptId)) {
            return Result.illegal("组织编码不能为空");
        }

        sysDeptService.removeByIds(Splitter.on(',').omitEmptyStrings().trimResults().splitToSet(sysDeptId));
        return Result.ok();
    }
}
