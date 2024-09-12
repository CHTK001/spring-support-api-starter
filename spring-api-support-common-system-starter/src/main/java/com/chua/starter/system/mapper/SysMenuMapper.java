package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}