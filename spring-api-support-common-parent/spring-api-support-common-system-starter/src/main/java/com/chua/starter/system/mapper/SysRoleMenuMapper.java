package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysRoleMenu;
import com.chua.starter.system.pojo.menu.RoleMenuBinding;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色-菜单关系 Mapper。
 *
 * @since 2024/8/7
 * @author CH
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 一次性取回「全部角色-菜单绑定」并带出角色码。
     *
     * <p>为什么需要它：菜单按用户过滤时要同时判断两件事 —— ①这份菜单是否绑给了我的角色、
     * ②这份菜单有没有被任何角色绑定过（没绑过视为公共菜单）。拆成两条 SQL 就是多一次
     * 全表扫描，这里用一条联表查询同时满足。</p>
     *
     * <p>只取两列（菜单 id + 角色码），不必加载实体其它字段。角色被删但关联残留时
     * {@code roleCode} 为 {@code null}，调用方需容忍。</p>
     *
     * @return 绑定行列表
     */
    List<RoleMenuBinding> selectRoleMenuBindings();
}
