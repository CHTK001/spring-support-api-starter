package com.chua.starter.school.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.school.dto.SchoolStudentQueryDTO;
import com.chua.starter.school.entity.SchoolStudent;
import com.chua.starter.school.mapper.SchoolStudentMapper;
import com.chua.starter.school.service.SchoolStudentService;
import com.chua.starter.school.vo.SchoolStudentVO;
import org.springframework.stereotype.Service;

/**
 * 学校学生服务实现类
 *
 * @author CH
 */
@Service
public class SchoolStudentServiceImpl extends ServiceImpl<SchoolStudentMapper, SchoolStudent> implements SchoolStudentService {

    /**
     * 分页查询学生列表
     *
     * @param query 查询条件
     * @return 学生列表
     */
    @Override
    public ReturnPageResult<SchoolStudentVO> pageSchoolStudent(SchoolStudentQueryDTO query) {
        IPage<SchoolStudentVO> schoolStudentPage = baseMapper.pageSchoolStudent(query.createFullPage(), query);
        return ReturnPageResultUtils.ok(schoolStudentPage);
    }

    /**
     * 获取学生详情
     *
     * @param schoolStudentId 学生ID
     * @return 学生详情
     */
    @Override
    public ReturnResult<SchoolStudentVO> getSchoolStudentDetail(Long schoolStudentId) {
        SchoolStudentVO schoolStudentVO = baseMapper.getStudentDetailById(schoolStudentId);
        return ReturnResult.ok(schoolStudentVO);
    }

    @Override
    public ReturnResult<Boolean> saveSchoolStudent(SchoolStudent schoolStudent) {
        schoolStudent.setSchoolStudentId(null);
        boolean result = save(schoolStudent);
        return result ? ReturnResult.ok(true) : ReturnResult.fail("保存学生信息失败");
    }

    @Override
    public ReturnResult<Boolean> updateSchoolStudent(SchoolStudent schoolStudent) {
        boolean result = updateById(schoolStudent);
        return result ? ReturnResult.ok(true) : ReturnResult.fail("更新学生信息失败");
    }

    @Override
    public ReturnResult<Boolean> deleteSchoolStudent(Long schoolStudentId) {
        return removeById(schoolStudentId) ? ReturnResult.ok(true) : ReturnResult.fail("删除学生信息失败");
    }
} 