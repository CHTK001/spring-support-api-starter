package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.pojo.menu.SysMenuSettingVO;
import com.chua.starter.system.pojo.menu.SysMenuVO;

import java.util.List;
import java.util.Set;

/**
 * 绯荤粺鑿滃崟鏈嶅姟鎺ュ彛锛屽畾涔変簡涓庣郴缁熻彍鍗曠浉鍏崇殑涓氬姟鎿嶄綔
 * 缁ф壙浜嗛€氱敤鐨勬湇鍔℃帴鍙Service<SysMenu>锛屾彁渚涗簡鏇村叿浣撶殑鑿滃崟鎿嶄綔鏂规硶
 *
 * @since 2024/8/7
 * @author CH
 */
public interface SysMenuService extends IService<SysMenu>{


    /**
     * 鏍规嵁鏉′欢鏌ヨ绯荤粺鑿滃崟鍒楄〃
     *
     * @param sysMenu 浣滀负鏌ヨ鏉′欢鐨勭郴缁熻彍鍗曞璞★紝鍙互鎸囧畾閮ㄥ垎灞炴€ц繘琛屾ā绯婃煡璇?
     * @param roles   褰撳墠鐢ㄦ埛鎷ユ湁鐨勮鑹查泦鍚堬紝鐢ㄤ簬鏉冮檺杩囨护
     * @param  ignoreType 蹇界暐鐨勭被鍨?
     * @return 杩斿洖鍖归厤鐨勭郴缁熻彍鍗曞垪琛?
     */
    ReturnResult<List<SysMenuVO>> list(SysMenu sysMenu, Set<String> roles, int ignoreType);

    /**
     * 鏌ヨ鎵€鏈夌郴缁熻彍鍗曠殑缂撳瓨鍒楄〃
     * 璇ユ柟娉曡繑鍥炵殑缁撴灉浼氳缂撳瓨锛屼娇鐢ㄤ簡@Cacheable娉ㄨВ杩涜缂撳瓨绠＄悊
     *
     * @return 杩斿洖鎵€鏈夌郴缁熻彍鍗曠殑鍒楄〃
     */
    List<SysMenu> listAll();

    /**
     * 鍒嗛〉鏌ヨ绯荤粺鑿滃崟鍒楄〃
     *
     * @param query 鍒嗛〉鏌ヨ瀵硅薄锛岃缃簡鍒嗛〉鐨勫ぇ灏忓拰褰撳墠椤电爜
     * @param sysMenu 浣滀负鏌ヨ鏉′欢鐨勭郴缁熻彍鍗曞璞★紝鍙互鎸囧畾閮ㄥ垎灞炴€ц繘琛屾ā绯婃煡璇?
     * @param roles 褰撳墠鐢ㄦ埛鎷ユ湁鐨勮鑹查泦鍚堬紝鐢ㄤ簬鏉冮檺杩囨护
     * @return 杩斿洖鍒嗛〉鏌ヨ缁撴灉锛屽寘鍚簡鍒嗛〉淇℃伅鍜岃彍鍗曞垪琛?
     */
    IPage<SysMenu> pageForMenu(Query<SysMenu> query, SysMenu sysMenu, Set<String> roles);

    /**
     * 鍒犻櫎鎸囧畾鐨勭郴缁熻彍鍗?
     *
     * @param menuId 瑕佸垹闄ょ殑绯荤粺鑿滃崟鐨処D
     * @return 杩斿洖鍒犻櫎鎿嶄綔鐨勭粨鏋滐紝鍖呮嫭鏄惁鎴愬姛鍜岀浉鍏虫秷鎭?
     */
    ReturnResult<Boolean> delete(Long menuId);

    /**
     * 鏇存柊绯荤粺鑿滃崟鐨勪俊鎭?
     *
     * @param roles 褰撳墠鐢ㄦ埛鎷ユ湁鐨勮鑹查泦鍚堬紝鐢ㄤ簬鏉冮檺鏍￠獙
     * @param sysMenu 瑕佹洿鏂扮殑绯荤粺鑿滃崟瀵硅薄锛屽寘鍚洿鏂板悗鐨勫睘鎬у€?
     * @return 杩斿洖鏇存柊鎿嶄綔鐨勭粨鏋滐紝鍖呮嫭鏄惁鎴愬姛鍜岀浉鍏虫秷鎭?
     */
    ReturnResult<Boolean> update(Set<String> roles, SysMenu sysMenu);

    /**
     * 鏂板涓€涓郴缁熻彍鍗?
     *
     * @param sysMenu 鏂板鐨勭郴缁熻彍鍗曞璞★紝鍖呭惈鎵€鏈夊繀瑕佸睘鎬?
     * @param roles 褰撳墠鐢ㄦ埛鎷ユ湁鐨勮鑹查泦鍚堬紝鐢ㄤ簬鏉冮檺鏍￠獙
     * @return 杩斿洖鏂板鎿嶄綔鐨勭粨鏋滐紝鍖呮嫭鏄惁鎴愬姛鍜岀浉鍏虫秷鎭?
     */
    ReturnResult<SysMenu> save(SysMenu sysMenu, Set<String> roles);

    /**
     * 鏍规嵁鏉′欢鏌ヨ绯荤粺鑿滃崟鍒楄〃
     *
     * @param sysMenu 浣滀负鏌ヨ鏉′欢鐨勭郴缁熻彍鍗曞璞★紝鍙互鎸囧畾閮ㄥ垎灞炴€ц繘琛屾ā绯婃煡璇?
     * @param roles 褰撳墠鐢ㄦ埛鎷ユ湁鐨勮鑹查泦鍚堬紝鐢ㄤ簬鏉冮檺杩囨护
     * @return 杩斿洖鍖归厤鐨勭郴缁熻彍鍗曞垪琛?
     */
    ReturnResult<List<SysMenuSettingVO>> listMenu(SysMenu sysMenu, Set<String> roles);

    /**
     * 鏌ヨ鎵€鏈夌郴缁熻彍鍗曠殑缂撳瓨鍒楄〃
     * 璇ユ柟娉曡繑鍥炵殑缁撴灉浼氳缂撳瓨锛屼娇鐢ㄤ簡@Cacheable娉ㄨВ杩涜缂撳瓨绠＄悊
     *
     * @return 杩斿洖鎵€鏈夌郴缁熻彍鍗曠殑鍒楄〃
     */
    ReturnResult<List<SysMenu>> all(SysMenu sysMenu, Set<String> roles);
}
