package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.SysUserQuery;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.pojo.user.SysUserPasswordUpdate;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.pojo.user.SysUserVO;
import com.chua.starter.system.pojo.user.UserLayout;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;

/**
 * 系统用户服务接口锛屽畾涔変簡涓庣郴缁熺敤鎴风浉鍏崇殑操作
 * 缁ф壙浜嗛€氱敤鐨勬湇鍔℃帴鍙Service锛屼负系统用户鎻愪緵澧炲垹鏌ユ敼绛夋搷浣?
 * @author Administrator
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 鏍规嵁用户查询鏉′欢鍜岃鑹查泦鍚堝垎椤垫煡璇㈢敤鎴蜂俊鎭?
     *
     * @param sysUserQuery 用户查询鏉′欢瀵硅薄锛屽寘鍚煡璇㈢殑鍚勭被参数
     * @param roles 用户角色闆嗗悎锛岀敤浜庣瓫閫夌敤鎴?
     * @return 分页查询结果锛屽寘鍚敤鎴蜂俊鎭垪琛ㄥ拰分页数据
     */
    IPage<SysUserVO> pageForUser(SysUserQuery sysUserQuery, Set<String> roles);
    /**
     * 删除鎸囧畾用户
     *
     * @param userId 用户ID锛岀敤浜庣‘瀹氳删除鐨勭敤鎴?
     * @return 操作结果瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧提示信息
     */
    ReturnResult<Boolean> delete(Long userId);

    /**
     * 重置鎸囧畾用户鐨勪笟涓讳俊鎭?
     *
     * @param userId 用户ID锛岀敤浜庣‘瀹氳重置鐨勭敤鎴?
     * @return 操作结果瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧提示信息
     */
    ReturnResult<SysUser> resetOwner(Long userId);

    /**
     * 重置鎸囧畾用户鐨勭櫥褰曞瘑鐮?
     *
     * @param userId 用户ID锛岀敤浜庣‘瀹氳重置密码鐨勭敤鎴?
     * @return 操作结果瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧提示信息
     */
    ReturnResult<String> reset(Long userId);

    /**
     * 更新用户信息锛屽寘鎷鑹茬瓑
     *
     * @param roles 用户鐨勬柊角色闆嗗悎
     * @param userId 用户ID锛岀敤浜庣‘瀹氳更新鐨勭敤鎴?
     * @param sysUser 更新鐨勭敤鎴蜂俊鎭璞?
     * @return 操作结果瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧提示信息
     */
    ReturnResult<SysUser> updateUser(Set<String> roles, Long userId, SysUserUpdate sysUser);

    /**
     * 保存鏂扮敤鎴蜂俊鎭?
     *
     * @param sysUserUpdate 鏂扮敤鎴蜂俊鎭璞★紝鍖呭惈用户鐨勫熀鏈俊鎭?
     * @param request HTTP请求瀵硅薄锛岀敤浜庤幏鍙栬姹傜浉鍏崇殑数据
     * @return 操作结果瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧提示信息
     */
    ReturnResult<SysUser> saveUser(SysUserUpdate sysUserUpdate, HttpServletRequest request);

    /**
     * 获取当前登录用户鐨勮彍鍗曞垪琛?
     *
     * @param roles 用户鐨勮鑹查泦鍚堬紝鐢ㄤ簬筛选夎彍鍗?
     * @return 菜单列表瀵硅薄锛屽寘鍚綋鍓嶇櫥褰曠敤鎴峰彲浠ヨ闂殑菜单信息
     */
    ReturnResult<List<SysMenuVO>> getUserMenu(Set<String> roles);


    /**
     * 更新鎸囧畾用户鐨勭櫥褰曞瘑鐮?
     *
     * @param userId 用户ID锛岀敤浜庣‘瀹氳更新鐨勭敤鎴?
     * @param sysUserPasswordUpdate 更新鐨勭敤鎴峰瘑鐮佷俊鎭璞?
     * @return 操作结果瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧提示信息
     */
    ReturnResult<SysUser> updateOwner(Set<String> roles, Long userId, SysUserPasswordUpdate sysUserPasswordUpdate);

    /**
     * 获取当前登录用户鐨勫竷灞€信息
     *
     * @param userId 用户ID锛?
     * @param roles  用户鐨勮鑹查泦鍚堬紝鐢ㄤ簬筛选夊竷灞€信息
     * @return 布局信息瀵硅薄锛屽寘鍚綋鍓嶇櫥褰曠敤鎴风殑布局信息
     */
    UserLayout getUserLayout(Long userId, Set<String> roles);

    /**
     * 保存用户布局信息
     *
     * @param userId 用户ID锛岀敤浜庣‘瀹氳保存布局鐨勭敤鎴?
     * @param userLayout 用户布局信息瀵硅薄锛屽寘鍚敤鎴风殑布局信息
     * @return 布局信息瀵硅薄锛屽寘鍚繚瀛樺悗鐨勫竷灞€信息
     */
    UserLayout saveUserLayout(Long userId, UserLayout userLayout);

}
