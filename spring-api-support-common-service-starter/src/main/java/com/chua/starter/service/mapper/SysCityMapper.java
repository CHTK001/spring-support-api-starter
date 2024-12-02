package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boren.biz.department.dto.CascadeListRes;
import com.boren.biz.department.dto.CascadeSecondList;
import com.boren.biz.department.dto.CascadeThirdList;
import com.boren.biz.entity.area.City;
import com.chua.starter.service.query.SysCityQuery;
import com.chua.starter.system.entity.SysCity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author yzj
 * @since 2022-11-03
 */
@Mapper
public interface SysCityMapper extends BaseMapper<SysCity> {


    /**
     * 行政区划级联查询
     *
     * @return 行政区划级联查询
     */
    List<SysCity> cascadeList(@Param("query") SysCityQuery query);

    /**
     * 根据省份查询行政区划级联
     * @param query 查询参数
     * @return 根据省份查询行政区划级联
     */
    List<SysCity> queryCityForProvince(SysCityQuery query);
}