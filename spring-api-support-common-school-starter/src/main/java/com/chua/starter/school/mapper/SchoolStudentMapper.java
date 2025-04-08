package com.chua.starter.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.starter.school.dto.SchoolStudentQueryDTO;
import com.chua.starter.school.entity.SchoolStudent;
import com.chua.starter.school.vo.SchoolStudentVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校学生Mapper接口
 *
 * @author CH
 */
@Mapper
public interface SchoolStudentMapper extends BaseMapper<SchoolStudent> {

    /**
     * 分页查询学生列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 学生列表
     */
    IPage<SchoolStudentVO> pageSchoolStudent(IPage<SchoolStudent> page, SchoolStudentQueryDTO query);

    /**
     * 根据ID获取学生详情
     *
     * @param id 学生ID
     * @return 学生详情
     */
    SchoolStudentVO getStudentDetailById(Long id);

    /**
     * 根据学号获取学生详情
     *
     * @param studentNumber 学号
     * @return 学生详情
     */
    SchoolStudentVO getStudentDetailByNumber(String studentNumber);
} 