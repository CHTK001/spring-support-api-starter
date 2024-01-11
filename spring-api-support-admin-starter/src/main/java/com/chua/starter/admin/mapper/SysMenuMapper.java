package com.chua.starter.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.admin.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    /**
     * 带有角色的菜单
     *
     * @return 带有角色的菜单
     */
    List<SysMenu> roleMenu();
}