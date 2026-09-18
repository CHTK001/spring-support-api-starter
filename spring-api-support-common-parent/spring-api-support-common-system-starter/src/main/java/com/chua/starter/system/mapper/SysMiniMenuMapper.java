package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysMiniMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小程序菜单 映射器
 *
 * @since 2026/08/03
 * @author CH
 */
@Mapper
public interface SysMiniMenuMapper extends BaseMapper<SysMiniMenu> {
}
