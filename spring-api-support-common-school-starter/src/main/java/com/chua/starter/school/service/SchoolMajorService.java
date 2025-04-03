package com.chua.starter.school.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.school.entity.SchoolMajor;
import com.chua.starter.school.vo.SchoolMajorVO;

import java.util.List;

/**
 * 学校专业Service
 *
 * @author CH
 */
public interface SchoolMajorService extends IService<SchoolMajor> {

    /**
     * 获取学校专业列表
     *
     * @param schoolId 学校ID
     * @return 专业列表
     */
    List<SchoolMajorVO> getMajorsBySchoolId(Long schoolId);

    /**
     * 分页获取学校专业列表
     *
     * @param schoolId 学校ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 专业分页列表
     */
    IPage<SchoolMajorVO> pageMajorsBySchoolId(Long schoolId, Integer pageNum, Integer pageSize);

    /**
     * 获取专业详情
     *
     * @param id 专业ID
     * @return 专业详情
     */
    SchoolMajorVO getMajorDetail(Long id);

    /**
     * 根据学校代码和专业代码获取专业详情
     *
     * @param schoolCode 学校代码
     * @param majorCode  专业代码
     * @return 专业详情
     */
    SchoolMajorVO getMajorDetailByCode(String schoolCode, String majorCode);
} 