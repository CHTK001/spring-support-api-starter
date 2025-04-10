package com.chua.starter.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.school.entity.SchoolMajor;
import com.chua.starter.school.mapper.SchoolMajorMapper;
import com.chua.starter.school.service.SchoolMajorService;
import com.chua.starter.school.vo.SchoolMajorVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学校专业Service实现类
 *
 * @author CH
 */
@Service
public class SchoolMajorServiceImpl extends ServiceImpl<SchoolMajorMapper, SchoolMajor> implements SchoolMajorService {

    @Override
    public List<SchoolMajorVO> getMajorsBySchoolId(Long schoolId) {
        LambdaQueryWrapper<SchoolMajor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SchoolMajor::getSchoolCode, schoolId);
        List<SchoolMajor> list = this.list(queryWrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public IPage<SchoolMajorVO> pageMajorsBySchoolId(Long schoolId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SchoolMajor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SchoolMajor::getSchoolCode, schoolId);

        Page<SchoolMajor> page = new Page<>(pageNum, pageSize);
        IPage<SchoolMajor> ipage = this.page(page, queryWrapper);

        // 转换为VO对象
        IPage<SchoolMajorVO> resultPage = new Page<>(ipage.getCurrent(), ipage.getSize(), ipage.getTotal());
        List<SchoolMajorVO> voList = ipage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public SchoolMajorVO getMajorDetail(Long id) {
        SchoolMajor major = this.getById(id);
        return major != null ? convertToVO(major) : null;
    }

    @Override
    public SchoolMajorVO getMajorDetailByCode(String schoolCode, String majorCode) {
        LambdaQueryWrapper<SchoolMajor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(schoolCode), SchoolMajor::getSchoolCode, schoolCode)
                .eq(StringUtils.hasText(majorCode), SchoolMajor::getSchoolMajorCode, majorCode);

        SchoolMajor major = this.getOne(queryWrapper);
        return major != null ? convertToVO(major) : null;
    }

    /**
     * 将实体转换为VO对象
     *
     * @param entity 实体对象
     * @return VO对象
     */
    private SchoolMajorVO convertToVO(SchoolMajor entity) {
        if (entity == null) {
            return null;
        }

        SchoolMajorVO vo = new SchoolMajorVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
} 