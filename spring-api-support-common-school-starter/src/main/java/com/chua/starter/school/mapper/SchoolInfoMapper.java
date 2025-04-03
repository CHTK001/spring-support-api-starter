package com.chua.starter.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.school.dto.SchoolQueryDTO;
import com.chua.starter.school.entity.SchoolInfo;
import com.chua.starter.school.vo.SchoolInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 学校信息Mapper
 *
 * @author CH
 */
@Mapper
public interface SchoolInfoMapper extends BaseMapper<SchoolInfo> {

    /**
     * 分页查询学校信息列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 学校信息列表
     */
    IPage<SchoolInfoVO> selectSchoolInfoPage(Page<SchoolInfo> page, @Param("query") SchoolQueryDTO query);

    /**
     * 获取学校详情
     *
     * @param id 学校ID
     * @return 学校详情
     */
    SchoolInfoVO getSchoolDetailById(@Param("id") Long id);

    /**
     * 根据学校代码获取学校详情
     *
     * @param schoolCode 学校代码
     * @return 学校详情
     */
    SchoolInfoVO getSchoolDetailByCode(@Param("schoolCode") String schoolCode);
} 