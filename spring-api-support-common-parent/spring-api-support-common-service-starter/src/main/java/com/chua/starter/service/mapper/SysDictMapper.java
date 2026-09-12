package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.service.SysDict;
import org.apache.ibatis.annotations.Mapper;

/**
* 系统字典表数据访问层接口
* 
* @since 2024/8/7
* @author CH
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
}