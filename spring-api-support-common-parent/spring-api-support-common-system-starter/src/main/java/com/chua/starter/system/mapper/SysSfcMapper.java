package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.entity.system.SysSfc;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author CH
 * @since 2024/10/24
 */
public interface SysSfcMapper extends BaseMapper<SysSfc> {
    /**
     * 根据条件分页查询SysSfc表中的数据
     * 此方法允许通过传入一个SysSfc对象作为条件参数，结合分页信息，来查询符合特定条件的SysSfc记录
     *
     * @param page 分页对象，包含分页参数当前页码和每页大小
     * @param sysSfc 包含查询条件的SysSfc对象
     * @param userId 用户ID，用于限定查询条件或记录查询用户的标识
     * @return 返回一个分页对象，包含查询结果和分页信息
     */
    IPage<SysSfc> selectPageForSfc(@Param("page") Page<SysSfc> page, @Param("condition") SysSfc sysSfc, @Param("userId") String userId);

    /**
     * 查询当前用户下的SysSfc数据
     * 此方法用于根据用户ID和SysSfc类别查询属于当前用户的所有SysSfc记录
     * 主要用于获取用户相关的SysSfc信息，以便进行用户级别的数据处理或展示
     *
     * @param sysSfcCategory SysSfc类别，用于限定查询的SysSfc类型
     * @param userId 用户ID，用于限定查询属于哪个用户的数据
     * @return 返回一个列表，包含所有符合查询条件的SysSfc对象
     */
    List<SysSfc> mine(@Param("sysSfcCategory") String sysSfcCategory, @Param("userId") String userId);
}
