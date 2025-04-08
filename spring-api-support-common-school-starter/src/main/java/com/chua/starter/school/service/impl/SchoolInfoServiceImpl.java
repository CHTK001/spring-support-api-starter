package com.chua.starter.school.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.school.dto.SchoolQueryDTO;
import com.chua.starter.school.entity.SchoolInfo;
import com.chua.starter.school.mapper.SchoolInfoMapper;
import com.chua.starter.school.service.SchoolInfoService;
import com.chua.starter.school.vo.SchoolInfoVO;
import org.springframework.stereotype.Service;

/**
 * 学校信息Service实现类
 *
 * @author CH
 */
@Service
public class SchoolInfoServiceImpl extends ServiceImpl<SchoolInfoMapper, SchoolInfo> implements SchoolInfoService {

    /**
     * 分页查询学校信息
     *
     * @param query 查询条件
     * @return 学校信息分页列表
     */
    @Override
    public ReturnPageResult<SchoolInfoVO> pageSchoolInfo(SchoolQueryDTO query) {
        IPage<SchoolInfoVO> schoolInfoPage = baseMapper.selectSchoolInfoPage(query.createFullPage(), query);
        return ReturnPageResultUtils.ok(schoolInfoPage);
    }

    /**
     * 获取学校详情
     *
     * @param id 学校ID
     * @return 学校详情
     */
    @Override
    public ReturnResult<SchoolInfoVO> getSchoolDetail(Long id) {
        SchoolInfoVO schoolInfoVO = baseMapper.getSchoolDetailById(id);
        return ReturnResult.ok(schoolInfoVO);
    }

    @Override
    public ReturnResult<Boolean> saveSchSchoolInfo(SchoolInfo schoolInfo) {
        schoolInfo.setSchoolId(null);
        boolean result = save(schoolInfo);
        return result ? ReturnResult.ok(true) : ReturnResult.fail("保存学校信息失败");
    }

    @Override
    public ReturnResult<Boolean> updateSchoolInfo(SchoolInfo schoolInfo) {
        boolean result = updateById(schoolInfo);
        return result ? ReturnResult.ok(true) : ReturnResult.fail("更新学校信息失败");
    }

    @Override
    public ReturnResult<Boolean> deleteSchool(Long schoolId) {
        return removeById(schoolId) ? ReturnResult.ok(true) : ReturnResult.fail("删除学校信息失败");
    }
} 