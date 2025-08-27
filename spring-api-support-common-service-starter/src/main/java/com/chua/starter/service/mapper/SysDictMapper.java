package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统字典表数据访问层接口
 * 
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
}