package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.treenode.TreeNode;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.service.SysCityService;
import com.chua.starter.service.mapper.SysCityMapper;
import com.chua.starter.service.query.SysCityQuery;
import com.chua.starter.system.entity.SysCity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 区域规划
 * @author CH
 * @since 2024/8/7
 */
@Service
@Slf4j
public class SysCityServiceImpl extends ServiceImpl<SysCityMapper, SysCity> implements SysCityService {

    @Override
    public Result<List<SysCity>> queryCity(SysCityQuery query) {
        List<SysCity> sysCities = baseMapper.cascadeList(query);
        if("LIST".equals(query.getReturnType())) {
           return Result.success(sysCities);
        }

        TreeNode<SysCity> treeNode = TreeNode.transfer(sysCities, sysCity -> {
            TreeNode<SysCity> node = new TreeNode<>();
            node.setId(sysCity.getSysCityCode());
            node.setLabel(sysCity.getSysCityName());
            node.setPid("0");
            node.setExt(sysCity);
            return node;
        });

        return Result.success(treeNode.toList());

    }

    @Override
    public Result<List<SysCity>> childrenAll(SysCityQuery query) {
        List<SysCity> sysCities = baseMapper.childrenAll(query);
        if("LIST".equals(query.getReturnType())) {
            return Result.success(sysCities);
        }

        TreeNode<SysCity> treeNode = TreeNode.transfer(sysCities, sysCity -> {
            TreeNode<SysCity> node = new TreeNode<>();
            node.setId(sysCity.getSysCityCode());
            node.setLabel(sysCity.getSysCityName());
            node.setPid("0");
            node.setExt(sysCity);
            return node;
        });

        return Result.success(treeNode.toList());
    }
}
