package com.chua.starter.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.school.entity.SchoolAdmissionPlan;
import com.chua.starter.school.vo.SchoolAdmissionPlanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 招生计划Mapper
 *
 * @author CH
 */
@Mapper
public interface SchoolAdmissionPlanMapper extends BaseMapper<SchoolAdmissionPlan> {

    /**
     * 获取学校的招生计划列表
     *
     * @param schoolId 学校ID
     * @param year     年份
     * @return 招生计划列表
     */
    List<SchoolAdmissionPlanVO> getAdmissionPlansBySchoolId(@Param("schoolId") Long schoolId, @Param("year") Integer year);

    /**
     * 获取专业的招生计划列表
     *
     * @param majorId 专业ID
     * @param year    年份
     * @return 招生计划列表
     */
    List<SchoolAdmissionPlanVO> getAdmissionPlansByMajorId(@Param("majorId") Long majorId, @Param("year") Integer year);

    /**
     * 分页获取招生计划
     *
     * @param page     分页参数
     * @param schoolId 学校ID
     * @param majorId  专业ID
     * @param year     年份
     * @param province 省份
     * @return 招生计划分页列表
     */
    IPage<SchoolAdmissionPlanVO> getAdmissionPlanPage(
            Page<SchoolAdmissionPlan> page,
            @Param("schoolId") Long schoolId,
            @Param("majorId") Long majorId,
            @Param("year") Integer year,
            @Param("province") String province
    );
} 