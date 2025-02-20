package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.starter.system.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {
    /**
     * 根据字典id查询字典项
     *
     * @param sysDictId 字典id
     * @param onlyChildren 是否只查询子项
     * @return 字典项
     */
    List<SysDictItem> selectListById(@Param("sysDictId")Integer sysDictId, @Param("onlyChildren")boolean onlyChildren);
    /**
     * 根据字典code查询字典项
     *
     * @param sysDictCode 字典code
     * @param onlyChildren 是否只查询子项
     * @return 字典项
     */
    List<SysDictItem> selectListByCode(@Param("sysDictCode")String sysDictCode, @Param("onlyChildren")boolean onlyChildren);

    /**
     * 分页查询字典项
     *
     * @param page 查询条件
     * @param sysDictId 字典id
     * @return 字典项
     */
    IPage<SysDictItem> selectPageForQuery(@Param("page")IPage<SysDictItem> page, @Param("sysDictId")Integer sysDictId);
}