package com.chua.starter.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.school.entity.SchoolHistoricalScore;
import com.chua.starter.school.vo.SchoolHistoricalScoreVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 历年分数Mapper
 *
 * @author CH
 */
@Mapper
public interface SchoolHistoricalScoreMapper extends BaseMapper<SchoolHistoricalScore> {

    /**
     * 获取学校的历年分数列表
     *
     * @param schoolId    学校ID
     * @param province    省份
     * @param subjectType 科目类型
     * @return 历年分数列表
     */
    List<SchoolHistoricalScoreVO> getScoresBySchoolId(
            @Param("schoolId") Long schoolId,
            @Param("province") String province,
            @Param("subjectType") String subjectType
    );

    /**
     * 获取专业的历年分数列表
     *
     * @param majorId     专业ID
     * @param province    省份
     * @param subjectType 科目类型
     * @return 历年分数列表
     */
    List<SchoolHistoricalScoreVO> getScoresByMajorId(
            @Param("majorId") Long majorId,
            @Param("province") String province,
            @Param("subjectType") String subjectType
    );

    /**
     * 分页获取历年分数
     *
     * @param page        分页参数
     * @param schoolId    学校ID
     * @param majorId     专业ID
     * @param province    省份
     * @param subjectType 科目类型
     * @param year        年份
     * @return 历年分数分页列表
     */
    IPage<SchoolHistoricalScoreVO> getScorePage(
            Page<SchoolHistoricalScore> page,
            @Param("schoolId") Long schoolId,
            @Param("majorId") Long majorId,
            @Param("province") String province,
            @Param("subjectType") String subjectType,
            @Param("year") Integer year
    );
} 