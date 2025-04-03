package com.chua.starter.school.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.school.dto.SchoolQueryDTO;
import com.chua.starter.school.entity.SchoolInfo;
import com.chua.starter.school.vo.SchoolInfoVO;

/**
 * 学校信息Service
 *
 * @author CH
 */
public interface SchoolInfoService extends IService<SchoolInfo> {

    /**
     * 分页查询学校信息
     *
     * @param query 查询条件
     * @return 学校信息分页列表
     */
    IPage<SchoolInfoVO> pageSchoolInfo(SchoolQueryDTO query);

    /**
     * 获取学校详情
     *
     * @param id 学校ID
     * @return 学校详情
     */
    SchoolInfoVO getSchoolDetail(Long id);

    /**
     * 根据学校代码获取学校详情
     *
     * @param schoolCode 学校代码
     * @return 学校详情
     */
    SchoolInfoVO getSchoolDetailByCode(String schoolCode);
} 