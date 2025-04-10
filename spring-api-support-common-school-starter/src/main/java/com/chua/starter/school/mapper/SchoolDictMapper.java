package com.chua.starter.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.school.entity.SchoolDict;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SchoolDictMapper extends BaseMapper<SchoolDict> {
}