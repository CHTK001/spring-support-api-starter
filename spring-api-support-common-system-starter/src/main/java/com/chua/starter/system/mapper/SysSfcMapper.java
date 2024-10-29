package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.system.entity.SysSfc;
import org.apache.ibatis.annotations.Param;

/**
 * @author CH
 * @since 2024/10/24
 */
public interface SysSfcMapper extends BaseMapper<SysSfc> {
    /**
     * 分页查询
     *
     * @param page   page
     * @param sysSfc sysSfc
     * @param userId
     * @return page
     */
    IPage<SysSfc> selectPageForSfc(@Param("page") Page<SysSfc> page, @Param("condition") SysSfc sysSfc, @Param("userId") String userId);
}