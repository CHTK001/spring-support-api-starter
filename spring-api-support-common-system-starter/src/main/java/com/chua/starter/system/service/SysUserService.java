package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.query.SysUserQuery;
import com.chua.starter.system.model.SysUser;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.pojo.user.SysUserPasswordUpdate;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.pojo.user.SysUserVO;
import com.chua.starter.system.pojo.user.UserLayout;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;

/**
 * 绯荤粺鐢ㄦ埛鏈嶅姟鎺ュ彛锛屽畾涔変簡涓庣郴缁熺敤鎴风浉鍏崇殑鎿嶄綔
 * 缁ф壙浜嗛€氱敤鐨勬湇鍔℃帴鍙Service锛屼负绯荤粺鐢ㄦ埛鎻愪緵澧炲垹鏌ユ敼绛夋搷浣?
 * @author Administrator
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 鏍规嵁鐢ㄦ埛鏌ヨ鏉′欢鍜岃鑹查泦鍚堝垎椤垫煡璇㈢敤鎴蜂俊鎭?
     *
     * @param sysUserQuery 鐢ㄦ埛鏌ヨ鏉′欢瀵硅薄锛屽寘鍚煡璇㈢殑鍚勭被鍙傛暟
     * @param roles 鐢ㄦ埛瑙掕壊闆嗗悎锛岀敤浜庣瓫閫夌敤鎴?
     * @return 鍒嗛〉鏌ヨ缁撴灉锛屽寘鍚敤鎴蜂俊鎭垪琛ㄥ拰鍒嗛〉鏁版嵁
     */
    IPage<SysUserVO> pageForUser(SysUserQuery sysUserQuery, Set<String> roles);
    /**
     * 鍒犻櫎鎸囧畾鐢ㄦ埛
     *
     * @param userId 鐢ㄦ埛ID锛岀敤浜庣‘瀹氳鍒犻櫎鐨勭敤鎴?
     * @return 鎿嶄綔缁撴灉瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧鎻愮ず淇℃伅
     */
    ReturnResult<Boolean> delete(Long userId);

    /**
     * 閲嶇疆鎸囧畾鐢ㄦ埛鐨勪笟涓讳俊鎭?
     *
     * @param userId 鐢ㄦ埛ID锛岀敤浜庣‘瀹氳閲嶇疆鐨勭敤鎴?
     * @return 鎿嶄綔缁撴灉瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧鎻愮ず淇℃伅
     */
    ReturnResult<SysUser> resetOwner(Long userId);

    /**
     * 閲嶇疆鎸囧畾鐢ㄦ埛鐨勭櫥褰曞瘑鐮?
     *
     * @param userId 鐢ㄦ埛ID锛岀敤浜庣‘瀹氳閲嶇疆瀵嗙爜鐨勭敤鎴?
     * @return 鎿嶄綔缁撴灉瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧鎻愮ず淇℃伅
     */
    ReturnResult<String> reset(Long userId);

    /**
     * 鏇存柊鐢ㄦ埛淇℃伅锛屽寘鎷鑹茬瓑
     *
     * @param roles 鐢ㄦ埛鐨勬柊瑙掕壊闆嗗悎
     * @param userId 鐢ㄦ埛ID锛岀敤浜庣‘瀹氳鏇存柊鐨勭敤鎴?
     * @param sysUser 鏇存柊鐨勭敤鎴蜂俊鎭璞?
     * @return 鎿嶄綔缁撴灉瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧鎻愮ず淇℃伅
     */
    ReturnResult<SysUser> updateUser(Set<String> roles, Long userId, SysUserUpdate sysUser);

    /**
     * 淇濆瓨鏂扮敤鎴蜂俊鎭?
     *
     * @param sysUserUpdate 鏂扮敤鎴蜂俊鎭璞★紝鍖呭惈鐢ㄦ埛鐨勫熀鏈俊鎭?
     * @param request HTTP璇锋眰瀵硅薄锛岀敤浜庤幏鍙栬姹傜浉鍏崇殑鏁版嵁
     * @return 鎿嶄綔缁撴灉瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧鎻愮ず淇℃伅
     */
    ReturnResult<SysUser> saveUser(SysUserUpdate sysUserUpdate, HttpServletRequest request);

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛鐨勮彍鍗曞垪琛?
     *
     * @param roles 鐢ㄦ埛鐨勮鑹查泦鍚堬紝鐢ㄤ簬绛涢€夎彍鍗?
     * @return 鑿滃崟鍒楄〃瀵硅薄锛屽寘鍚綋鍓嶇櫥褰曠敤鎴峰彲浠ヨ闂殑鑿滃崟淇℃伅
     */
    ReturnResult<List<SysMenuVO>> getUserMenu(Set<String> roles);


    /**
     * 鏇存柊鎸囧畾鐢ㄦ埛鐨勭櫥褰曞瘑鐮?
     *
     * @param userId 鐢ㄦ埛ID锛岀敤浜庣‘瀹氳鏇存柊鐨勭敤鎴?
     * @param sysUserPasswordUpdate 鏇存柊鐨勭敤鎴峰瘑鐮佷俊鎭璞?
     * @return 鎿嶄綔缁撴灉瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧鎻愮ず淇℃伅
     */
    ReturnResult<SysUser> updateOwner(Set<String> roles, Long userId, SysUserPasswordUpdate sysUserPasswordUpdate);

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛鐨勫竷灞€淇℃伅
     *
     * @param userId 鐢ㄦ埛ID锛?
     * @param roles  鐢ㄦ埛鐨勮鑹查泦鍚堬紝鐢ㄤ簬绛涢€夊竷灞€淇℃伅
     * @return 甯冨眬淇℃伅瀵硅薄锛屽寘鍚綋鍓嶇櫥褰曠敤鎴风殑甯冨眬淇℃伅
     */
    UserLayout getUserLayout(Long userId, Set<String> roles);

    /**
     * 淇濆瓨鐢ㄦ埛甯冨眬淇℃伅
     *
     * @param userId 鐢ㄦ埛ID锛岀敤浜庣‘瀹氳淇濆瓨甯冨眬鐨勭敤鎴?
     * @param userLayout 鐢ㄦ埛甯冨眬淇℃伅瀵硅薄锛屽寘鍚敤鎴风殑甯冨眬淇℃伅
     * @return 甯冨眬淇℃伅瀵硅薄锛屽寘鍚繚瀛樺悗鐨勫竷灞€淇℃伅
     */
    UserLayout saveUserLayout(Long userId, UserLayout userLayout);

}
