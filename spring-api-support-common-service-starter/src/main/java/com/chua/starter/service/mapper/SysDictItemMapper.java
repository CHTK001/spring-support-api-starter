package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.starter.service.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典项子项 Mapper 接口
 *
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {
    /**
     * 根据字典id查询字典项
     *
     * @param sysDictId    字典id，例如：1001
     * @param onlyChildren 是否只查询子项，true 表示只查子项，false 表示包含父级和子项
     * @return 字典项列表
     */
    List<SysDictItem> selectListById(@Param("sysDictId") Integer sysDictId, @Param("onlyChildren") boolean onlyChildren);

    /**
     * 根据字典code查询字典项
     *
     * @param sysDictCode  字典code，例如："USER_STATUS"
     * @param onlyChildren 是否只查询子项，true 表示只查子项，false 表示包含父级和子项
     * @return 字典项列表
     */
    List<SysDictItem> selectListByCode(@Param("sysDictCode") String sysDictCode, @Param("onlyChildren") boolean onlyChildren);

    /**
     * 分页查询字典项
     *
     * @param page       分页对象，用于封装分页参数，例如：new Page<>(1, 10)
     * @param sysDictId  字典id，例如：1001
     * @return 分页结果，包含字典项列表和分页信息
     */
    IPage<SysDictItem> selectPageForQuery(@Param("page") IPage<SysDictItem> page, @Param("sysDictId") Integer sysDictId);
}