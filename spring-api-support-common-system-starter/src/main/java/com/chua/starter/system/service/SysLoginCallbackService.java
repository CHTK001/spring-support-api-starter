package com.chua.starter.system.service;

import com.chua.starter.system.model.SysUser;

/**
 * 鐧诲綍鍥炶皟
 * @author CH
 * @since 2024/9/12
 */
public interface SysLoginCallbackService {
    /**
     * 鏍规嵁鐢ㄦ埛id鑾峰彇鐢ㄦ埛淇℃伅
     * @param userId 鐢ㄦ埛id
     * @return 鐢ㄦ埛淇℃伅
     */
    SysUser getById(String userId);

    /**
     * 鏇存柊鐢ㄦ埛淇℃伅
     * @param sysUser 鐢ㄦ埛淇℃伅
     */
    void updateById(SysUser sysUser);
}
