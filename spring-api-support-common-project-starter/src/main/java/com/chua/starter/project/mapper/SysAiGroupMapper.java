package com.chua.starter.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.project.SysAiGroup;
import lombok.NonNull;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI分组数据访问层
 *
 * @author CH
 * @since 2025/9/4 13:07
 */
@Mapper
public interface SysAiGroupMapper extends BaseMapper<SysAiGroup> {

    /**
     * 根据项目ID和用户ID查询AI分组列表
     *
     * @param sysProjectId 项目ID 例如：1001
     * @param sysUserId    用户ID 例如："user_001"
     * @return AI分组列表
     */
    List<SysAiGroup> group(
            @Param("sysProjectId") @NonNull Integer sysProjectId,
            @Param("sysUserId") String sysUserId);
}