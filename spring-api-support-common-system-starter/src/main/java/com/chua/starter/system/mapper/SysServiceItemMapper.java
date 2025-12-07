package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.service.entity.SysServiceModuleItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysServiceItemMapper extends BaseMapper<SysServiceModuleItem> {
}