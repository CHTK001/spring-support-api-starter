package com.chua.starter.school.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
    public IPage<SchoolInfoVO> pageSchoolInfo(SchoolQueryDTO query) {
        Page<SchoolInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectSchoolInfoPage(page, query);
    }

    /**
     * 获取学校详情
     *
     * @param id 学校ID
     * @return 学校详情
     */
    @Override
    public SchoolInfoVO getSchoolDetail(Long id) {
        return baseMapper.getSchoolDetailById(id);
    }

    /**
     * 根据学校代码获取学校详情
     *
     * @param schoolCode 学校代码
     * @return 学校详情
     */
    @Override
    public SchoolInfoVO getSchoolDetailByCode(String schoolCode) {
        return baseMapper.getSchoolDetailByCode(schoolCode);
    }
} 