package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysCity;
import com.chua.starter.service.SysCityService;
import com.chua.starter.service.mapper.SysCityMapper;
import com.chua.starter.service.query.SysCityQuery;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 区域规划
 *
 * @since 2024/12/2
 * @author CH
 */
@Service
public class SysCityServiceImpl extends ServiceImpl<SysCityMapper, SysCity> implements SysCityService {

    @Override
    /**
     * 查询City
    */
    public ReturnResult<List<SysCity>> queryCity(SysCityQuery query) {
        List<SysCity> sysCities = baseMapper.cascadeList(query);
        if ("LIST".equals(query.getReturnType())) {
            return ReturnResult.ok(sysCities);
        }
        return ReturnResult.ok(buildTree(sysCities));
    }

    @Override
    /**
     * children全部
    */
    public ReturnResult<List<SysCity>> childrenAll(SysCityQuery query) {
        List<SysCity> sysCities = baseMapper.childrenAll(query);
        if ("LIST".equals(query.getReturnType())) {
            return ReturnResult.ok(sysCities);
        }
        return ReturnResult.ok(buildTree(sysCities));
    }

    /**
     * 构建树
     *
     * @param all 全部
     * @return 构建树的结果
     */
    private List<SysCity> buildTree(List<SysCity> all) {
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, SysCity> map = new LinkedHashMap<>();
        List<SysCity> roots = new ArrayList<>();
        for (SysCity city : all) {
            city.setChildren(new ArrayList<>());
            map.put(city.getSysCityCode(), city);
        }
        for (SysCity city : all) {
            SysCity parent = map.get(city.getSysCityParentCode());
            if (parent != null) {
                parent.getChildren().add(city);
            } else {
                roots.add(city);
            }
        }
        return roots;
    }
}