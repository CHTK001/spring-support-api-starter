package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.model.SysRole;
import com.chua.starter.system.pojo.role.RoleMenuUpdate;

import java.util.Set;

/**
 * 绯荤粺瑙掕壊
 * @author CH
 * @since 2024/8/7
 */
public interface SysRoleService extends IService<SysRole> {
    /**
     * 鍒嗛〉鏌ヨ瑙掕壊淇℃伅
     *
     * @param query 鏌ヨ鏉′欢灏佽瀵硅薄
     * @param sysRole 瑙掕壊瀹炰綋瀵硅薄锛岀敤浜庝紶閫掕鑹叉暟鎹?
     * @param roles 瑙掕壊闆嗗悎锛岀敤浜庤繃婊よ鑹?
     * @return 鍒嗛〉缁撴灉锛屽寘鍚鑹蹭俊鎭?
     */
    IPage<SysRole> pageForRole(Query<SysRole> query, SysRole sysRole, Set<String> roles);

    /**
     * 鍒犻櫎瑙掕壊
     *
     * @param roleId 瑙掕壊ID锛岀敤浜庣‘瀹氳鍒犻櫎鐨勮鑹?
     * @return 鍒犻櫎缁撴灉锛屽寘鍚搷浣滄槸鍚︽垚鍔熺殑鏍囧織
     */
    ReturnResult<Boolean> delete(Long roleId);

    /**
     * 鏇存柊瑙掕壊淇℃伅
     *
     * @param roles 瑙掕壊闆嗗悎锛岀敤浜庢壒閲忔搷浣滆鑹?
     * @param sysRole 寰呮洿鏂扮殑瑙掕壊淇℃伅
     * @return 鏇存柊缁撴灉锛屽寘鍚搷浣滄槸鍚︽垚鍔熺殑鏍囧織
     */
    ReturnResult<Boolean> updateRole(Set<String> roles, SysRole sysRole);

    /**
     * 淇濆瓨瑙掕壊淇℃伅
     *
     * @param sysRole 寰呬繚瀛樼殑瑙掕壊淇℃伅
     * @return 淇濆瓨缁撴灉锛屽寘鍚搷浣滄槸鍚︽垚鍔熺殑鏍囧織浠ュ強淇濆瓨鐨勮鑹蹭俊鎭?
     */
    ReturnResult<SysRole> saveRole(SysRole sysRole);

    /**
     * 鏇存柊瑙掕壊鑿滃崟
     *
     * @param roles 瑙掕壊闆嗗悎锛岀敤浜庢壒閲忔搷浣滆鑹茶彍鍗?
     * @param roleMenuUpdate 瑙掕壊鑿滃崟鏇存柊瀵硅薄锛屽寘鍚鑹茶彍鍗曠殑鍙樻洿淇℃伅
     * @return 鏇存柊缁撴灉锛屽寘鍚搷浣滄槸鍚︽垚鍔熺殑鏍囧織
     */
    ReturnResult<Boolean> updateMenu(Set<String> roles, RoleMenuUpdate roleMenuUpdate);

    /**
     * 鑾峰彇瑙掕壊鑿滃崟
     *
     * @param roleMenuUpdate 瑙掕壊鑿滃崟鏇存柊瀵硅薄锛屽寘鍚鑹茶彍鍗曠殑鍙樻洿淇℃伅
     * @return 鏇存柊缁撴灉锛屽寘鍚搷浣滄槸鍚︽垚鍔熺殑鏍囧織
     */
    ReturnResult<Set<Integer>> getMenu(RoleMenuUpdate roleMenuUpdate);
}
