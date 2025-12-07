package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.system.pojo.menu.SysMenuSettingVO;
import com.chua.starter.system.pojo.menu.SysMenuVO;

import java.util.List;
import java.util.Set;

/**
 * 系统菜单服务接口锛屽畾涔変簡涓庣郴统计彍鍗曠浉鍏崇殑涓氬姟操作
 * 缁ф壙浜嗛€氱敤鐨勬湇鍔℃帴鍙Service<SysMenu>锛屾彁渚涗簡鏇村叿浣撶殑菜单操作鏂规硶
 *
 * @since 2024/8/7
 * @author CH
 */
public interface SysMenuService extends IService<SysMenu>{


    /**
     * 鏍规嵁鏉′欢查询系统菜单列表
     *
     * @param sysMenu 浣滀负查询鏉′欢鐨勭郴统计彍鍗曞璞★紝鍙互鎸囧畾部分灞炴€ц繘琛屾ā绯婃煡璇?
     * @param roles   当前用户鎷ユ湁鐨勮鑹查泦鍚堬紝鐢ㄤ簬权限杩囨护
     * @param  ignoreType 蹇界暐鐨勭被鍨?
     * @return 返回鍖归厤鐨勭郴统计彍鍗曞垪琛?
     */
    ReturnResult<List<SysMenuVO>> list(SysMenu sysMenu, Set<String> roles, int ignoreType);

    /**
     * 查询所有夌郴统计彍鍗曠殑缂撳瓨列表
     * 璇ユ柟娉曡繑鍥炵殑结果浼氳缂撳瓨锛屼娇鐢ㄤ簡@Cacheable娉ㄨВ杩涜缂撳瓨绠＄悊
     *
     * @return 返回所有夌郴统计彍鍗曠殑列表
     */
    List<SysMenu> listAll();

    /**
     * 分页查询系统菜单列表
     *
     * @param query 分页查询瀵硅薄锛岃缃簡分页鐨勫ぇ灏忓拰当前椤电爜
     * @param sysMenu 浣滀负查询鏉′欢鐨勭郴统计彍鍗曞璞★紝鍙互鎸囧畾部分灞炴€ц繘琛屾ā绯婃煡璇?
     * @param roles 当前用户鎷ユ湁鐨勮鑹查泦鍚堬紝鐢ㄤ簬权限杩囨护
     * @return 返回分页查询结果锛屽寘鍚簡分页信息鍜岃彍鍗曞垪琛?
     */
    IPage<SysMenu> pageForMenu(Query<SysMenu> query, SysMenu sysMenu, Set<String> roles);

    /**
     * 删除鎸囧畾鐨勭郴统计彍鍗?
     *
     * @param menuId 瑕佸垹闄ょ殑系统菜单鐨処D
     * @return 返回删除操作鐨勭粨鏋滐紝鍖呮嫭鏄惁成功鍜岀浉鍏虫秷鎭?
     */
    ReturnResult<Boolean> delete(Long menuId);

    /**
     * 更新系统菜单鐨勪俊鎭?
     *
     * @param roles 当前用户鎷ユ湁鐨勮鑹查泦鍚堬紝鐢ㄤ簬权限校验
     * @param sysMenu 瑕佹洿鏂扮殑系统菜单瀵硅薄锛屽寘鍚洿鏂板悗鐨勫睘鎬у€?
     * @return 返回更新操作鐨勭粨鏋滐紝鍖呮嫭鏄惁成功鍜岀浉鍏虫秷鎭?
     */
    ReturnResult<Boolean> update(Set<String> roles, SysMenu sysMenu);

    /**
     * 鏂板一个郴统计彍鍗?
     *
     * @param sysMenu 鏂板鐨勭郴统计彍鍗曞璞★紝鍖呭惈所有夊繀瑕佸睘鎬?
     * @param roles 当前用户鎷ユ湁鐨勮鑹查泦鍚堬紝鐢ㄤ簬权限校验
     * @return 返回鏂板操作鐨勭粨鏋滐紝鍖呮嫭鏄惁成功鍜岀浉鍏虫秷鎭?
     */
    ReturnResult<SysMenu> save(SysMenu sysMenu, Set<String> roles);

    /**
     * 鏍规嵁鏉′欢查询系统菜单列表
     *
     * @param sysMenu 浣滀负查询鏉′欢鐨勭郴统计彍鍗曞璞★紝鍙互鎸囧畾部分灞炴€ц繘琛屾ā绯婃煡璇?
     * @param roles 当前用户鎷ユ湁鐨勮鑹查泦鍚堬紝鐢ㄤ簬权限杩囨护
     * @return 返回鍖归厤鐨勭郴统计彍鍗曞垪琛?
     */
    ReturnResult<List<SysMenuSettingVO>> listMenu(SysMenu sysMenu, Set<String> roles);

    /**
     * 查询所有夌郴统计彍鍗曠殑缂撳瓨列表
     * 璇ユ柟娉曡繑鍥炵殑结果浼氳缂撳瓨锛屼娇鐢ㄤ簡@Cacheable娉ㄨВ杩涜缂撳瓨绠＄悊
     *
     * @return 返回所有夌郴统计彍鍗曠殑列表
     */
    ReturnResult<List<SysMenu>> all(SysMenu sysMenu, Set<String> roles);
}
