package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysCity;
import com.chua.starter.system.query.SysCityQuery;
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
     * 获取下级行政区划
     *
     * @return 获取下级行政区划
     */
    List<SysCity> childrenAll(@Param("query") SysCityQuery query);
}