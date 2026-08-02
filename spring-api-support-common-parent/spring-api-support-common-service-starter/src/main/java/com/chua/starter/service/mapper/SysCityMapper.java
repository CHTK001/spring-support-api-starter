package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.service.query.SysCityQuery;
import com.chua.starter.entity.service.SysCity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 城市数据访问层接口
 *
 * @author CH
 * @since 2022-11-03
 */
@Mapper
public interface SysCityMapper extends BaseMapper<SysCity> {


    /**
     * 行政区划级联查询
     *
     * @param query 查询条件对象，包含父级代码、城市名称等查询参数
     *              示例: {parentCode: "110000", cityName: "北京", returnType: "TREE"}
     * @return 符合条件的行政区划列表，根据returnType返回树形或列表结构
     */
    List<SysCity> cascadeList(@Param("query") SysCityQuery query);

    /**
     * 获取下级行政区划
     *
     * @param query 查询条件对象，主要使用parentCode查询下级行政区划
     *              示例: {parentCode: "110000"}
     * @return 下级行政区划列表
     */
    List<SysCity> childrenAll(@Param("query") SysCityQuery query);
}