package com.chua.starter.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.school.dto.SchoolSyncConfigDTO;
import com.chua.starter.school.entity.SchoolSyncConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 学校同步配置Mapper
 *
 * @author CH
 */
@Mapper
public interface SchoolSyncConfigMapper extends BaseMapper<SchoolSyncConfig> {

    /**
     * 分页查询同步配置
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 同步配置列表
     */
    IPage<SchoolSyncConfig> selectSyncConfigPage(Page<SchoolSyncConfig> page, @Param("query") SchoolSyncConfigDTO query);
}