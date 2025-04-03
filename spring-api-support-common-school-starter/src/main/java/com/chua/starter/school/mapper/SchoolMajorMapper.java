package com.chua.starter.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.school.entity.SchoolMajor;
import com.chua.starter.school.vo.SchoolMajorVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学校专业Mapper
 *
 * @author CH
 */
@Mapper
public interface SchoolMajorMapper extends BaseMapper<SchoolMajor> {

    /**
     * 获取学校专业列表
     *
     * @param schoolId 学校ID
     * @return 专业列表
     */
    List<SchoolMajorVO> getMajorsBySchoolId(@Param("schoolId") Long schoolId);

    /**
     * 分页获取学校专业列表
     *
     * @param page     分页参数
     * @param schoolId 学校ID
     * @return 专业分页列表
     */
    IPage<SchoolMajorVO> getMajorPageBySchoolId(Page<SchoolMajor> page, @Param("schoolId") Long schoolId);

    /**
     * 获取专业详情
     *
     * @param id 专业ID
     * @return 专业详情
     */
    SchoolMajorVO getMajorDetailById(@Param("id") Long id);

    /**
     * 根据学校代码和专业代码获取专业详情
     *
     * @param schoolCode 学校代码
     * @param majorCode  专业代码
     * @return 专业详情
     */
    SchoolMajorVO getMajorDetailByCode(@Param("schoolCode") String schoolCode, @Param("majorCode") String majorCode);
} 