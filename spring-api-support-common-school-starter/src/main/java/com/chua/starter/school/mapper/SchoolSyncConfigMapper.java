package com.chua.starter.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.school.entity.SchoolSyncConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校同步配置Mapper
 *
 * @author CH
 */
@Mapper
public interface SchoolSyncConfigMapper extends BaseMapper<SchoolSyncConfig> {
} 