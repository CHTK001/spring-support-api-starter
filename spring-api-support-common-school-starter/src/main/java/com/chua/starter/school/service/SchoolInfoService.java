package com.chua.starter.school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
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
    ReturnPageResult<SchoolInfoVO> pageSchoolInfo(SchoolQueryDTO query);

    /**
     * 获取学校详情
     *
     * @param id 学校ID
     * @return 学校详情
     */
    ReturnResult<SchoolInfoVO> getSchoolDetail(Long id);

    /**
     * 保存学校信息
     *
     * @param schoolInfo 学校信息
     * @return 是否保存成功
     */
    ReturnResult<Boolean> saveSchSchoolInfo(SchoolInfo schoolInfo);

    /**
     * 更新学校信息
     *
     * @param schoolInfo 学校信息
     * @return 是否更新成功
     */
    ReturnResult<Boolean> updateSchoolInfo(SchoolInfo schoolInfo);

    /**
     * 删除学校信息
     *
     * @param schoolId 学校ID
     * @return 是否删除成功
     */
    ReturnResult<Boolean> deleteSchool(Long schoolId);
}