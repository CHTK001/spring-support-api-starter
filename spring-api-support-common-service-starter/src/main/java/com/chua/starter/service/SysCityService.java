package com.chua.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.service.query.SysCityQuery;
import com.chua.starter.system.entity.SysCity;

import java.util.List;

/**
 * 区域规划
 * @author CH
 * @since 2024/12/2
 */
public interface SysCityService extends IService<SysCity> {

    /**
     * 查询城市信息
     *
     * 本方法通过接收SysCityQuery对象作为查询条件，返回符合条件的城市列表
     * 主要用于展示城市信息的场景，如城市列表展示、城市搜索等
     *
     * @param query 查询条件对象，包含需要查询的城市的相关条件
     * @return 返回一个Result对象，其中包含查询结果的城市列表如果查询没有结果，列表将为空
     */
    Result<List<SysCity>> queryCity(SysCityQuery query);

    /**
     * 获取下级以及子集行政区划
     *
     * 本方法通过接收SysCityQuery对象作为查询条件，返回符合条件的城市列表
     * 主要用于获取下级行政区划信息的场景，如获取某个城市的下级行政区划等
     *
     * @param query 查询条件对象，包含需要查询的城市的相关条件
     * @return 返回一个Result对象，其中包含查询结果的城市列表如果查询没有结果，列表将为空
     */
    Result<List<SysCity>> childrenAll(SysCityQuery query);
}
