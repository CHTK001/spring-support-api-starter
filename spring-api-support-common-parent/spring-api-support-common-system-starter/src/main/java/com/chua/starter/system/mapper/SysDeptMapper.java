package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysDept;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @since 2024/8/7
 * @author CH
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
    List<Map<String, Object>> selectMemberCounts();
}
