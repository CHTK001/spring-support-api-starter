package com.chua.starter.school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.school.dto.SchoolStudentQueryDTO;
import com.chua.starter.school.entity.SchoolStudent;
import com.chua.starter.school.vo.SchoolStudentVO;

/**
 * 学校学生服务接口
 *
 * @author CH
 */
public interface SchoolStudentService extends IService<SchoolStudent> {

    /**
     * 保存学生信息
     *
     * @param schoolStudent 学生信息
     * @return 是否成功
     */
    ReturnResult<Boolean> saveSchoolStudent(SchoolStudent schoolStudent);

    /**
     * 更新学生信息
     *
     * @param schoolStudent 学生信息
     * @return 是否成功
     */
    ReturnResult<Boolean> updateSchoolStudent(SchoolStudent schoolStudent);

    /**
     * 分页查询学生列表
     *
     * @param query 查询条件
     * @return 学生列表
     */
    ReturnPageResult<SchoolStudentVO> pageSchoolStudent(SchoolStudentQueryDTO query);

    /**
     * 获取学生详情
     *
     * @param schoolStudentId 学生ID
     * @return 学生详情
     */
    ReturnResult<SchoolStudentVO> getSchoolStudentDetail(Long schoolStudentId);

    /**
     * 删除学生
     *
     * @param schoolStudentId 学生ID
     * @return 是否成功
     */
    ReturnResult<Boolean> deleteSchoolStudent(Long schoolStudentId);
} 