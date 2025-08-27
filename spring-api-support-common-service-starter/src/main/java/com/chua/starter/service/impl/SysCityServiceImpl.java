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
 *
 * @author CH
 * @since 2024/12/2
 */
@Service
@Slf4j
public class SysCityServiceImpl extends ServiceImpl<SysCityMapper, SysCity> implements SysCityService {

    /**
     * 查询城市信息
     * <p>
     * 本方法通过接收SysCityQuery对象作为查询条件，返回符合条件的城市列表
     * 主要用于展示城市信息的场景，如城市列表展示、城市搜索等
     *
     * @param query 查询条件对象，包含需要查询的城市的相关条件
     *              例如：parentCode=CN110000, cityName=北京, returnType=TREE
     * @return 返回一个Result对象，其中包含查询结果的城市列表。如果查询没有结果，列表将为空
     */
    @Override
    public Result<List<SysCity>> queryCity(SysCityQuery query) {
        List<SysCity> sysCities = baseMapper.cascadeList(query);
        if ("LIST".equals(query.getReturnType())) {
            return Result.success(sysCities);
        }

        TreeNode<SysCity> treeNode = TreeNode.transfer(sysCities, sysCity -> {
            TreeNode<SysCity> node = new TreeNode<>();
            node.setId(sysCity.getSysCityCode());
            node.setLabel(sysCity.getSysCityName());
            node.setPid(sysCity.getSysCityParentCode());
            node.setExt(sysCity);
            return node;
        });

        return Result.success(treeNode.toList());
    }

    /**
     * 获取下级以及子集行政区划
     * <p>
     * 本方法通过接收SysCityQuery对象作为查询条件，返回符合条件的城市列表
     * 主要用于获取下级行政区划信息的场景，如获取某个城市的下级行政区划等
     *
     * @param query 查询条件对象，包含需要查询的城市的相关条件
     *              例如：parentCode=CN110000, cityName=北京, returnType=TREE
     * @return 返回一个Result对象，其中包含查询结果的城市列表。如果查询没有结果，列表将为空
     */
    @Override
    public Result<List<SysCity>> childrenAll(SysCityQuery query) {
        List<SysCity> sysCities = baseMapper.childrenAll(query);
        if ("LIST".equals(query.getReturnType())) {
            return Result.success(sysCities);
        }

        TreeNode<SysCity> treeNode = TreeNode.transfer(sysCities, sysCity -> {
            TreeNode<SysCity> node = new TreeNode<>();
            node.setId(sysCity.getSysCityCode());
            node.setLabel(sysCity.getSysCityName());
            node.setPid(sysCity.getSysCityParentCode());
            node.setExt(sysCity);
            return node;
        });

        return Result.success(treeNode.toList());
    }
}
