package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.system.pojo.user.log.SysUserLogQuery;

import java.util.Set;

/**
 * 绯荤粺鐢ㄦ埛鏃ュ織鏈嶅姟鎺ュ彛
 * 璇ユ帴鍙ｅ畾涔変簡涓庣郴缁熺敤鎴锋棩蹇楃浉鍏崇殑鎿嶄綔锛屽寘鎷煡璇㈢敤鎴锋棩蹇楀垎椤垫暟鎹拰鍒犻櫎鎸囧畾鐨勬棩蹇?
 * @author CH
 * @since 2024/8/8
 */
public interface SysUserLogService extends IService<SysUserLog> {

    /**
     * 鏍规嵁鏌ヨ鏉′欢鍒嗛〉鑾峰彇鐢ㄦ埛鏃ュ織
     * 姝ゆ柟娉曞厑璁告牴鎹笉鍚岀殑鏌ヨ鏉′欢鍜岃鑹茬瓫閫夛紝杩斿洖鐢ㄦ埛鏃ュ織鐨勫垎椤垫暟鎹?
     *
     * @param query 鏌ヨ瀵硅薄锛岀敤浜庡垎椤靛拰鎺掑簭璁剧疆
     * @param sysUserLogQuery 鏌ヨ鏉′欢瀵硅薄锛岀敤浜庣瓫閫夋棩蹇?
     * @param roles 鐢ㄦ埛瑙掕壊闆嗗悎锛岀敤浜庢牴鎹鑹茬瓫閫夋棩蹇?
     * @return 杩斿洖鍖呭惈鐢ㄦ埛鏃ュ織鐨勫垎椤垫暟鎹璞?
     */
    IPage<SysUserLog> pageForUserLog(Query<SysUserLog> query, SysUserLogQuery sysUserLogQuery, Set<String> roles);

    /**
     * 鍒犻櫎鎸囧畾鐨勭敤鎴锋棩蹇?
     * 姝ゆ柟娉曢€氳繃鏃ュ織鐨処D杩涜鍒犻櫎鎿嶄綔锛岃繑鍥炰竴涓〃绀烘搷浣滅粨鏋滅殑瀵硅薄
     *
     * @param userLogId 鐢ㄦ埛鏃ュ織鐨処D
     * @return 杩斿洖涓€涓寘鍚搷浣滅粨鏋滅殑瀵硅薄锛屽璞′腑鐨勬暟鎹瓧娈佃〃绀烘搷浣滄槸鍚︽垚鍔?
     */
    ReturnResult<Boolean> delete(Long userLogId);

    /**
     * 鑾峰彇褰撳墠鐢ㄦ埛鐨勬棩蹇?
     * 姝ゆ柟娉曟牴鎹敤鎴风殑瑙掕壊鍜屾煡璇㈡潯浠惰幏鍙栧綋鍓嶇敤鎴风殑鏃ュ織锛岃繑鍥炰竴涓寘鍚棩蹇楃殑鍒嗛〉鏁版嵁瀵硅薄
     *
     * @param query      鏌ヨ瀵硅薄锛岀敤浜庡垎椤靛拰鎺掑簭璁剧疆
     * @param sysUserLogQuery 鏌ヨ鏉′欢瀵硅薄锛岀敤浜庣瓫閫夋棩蹇?
     * @param sysUserId  璐﹀彿ID
     * @param roles      鐢ㄦ埛瑙掕壊闆嗗悎锛岀敤浜庢牴鎹鑹茬瓫閫夋棩蹇?
     * @return 杩斿洖鍖呭惈褰撳墠鐢ㄦ埛鏃ュ織鐨勫垎椤垫暟鎹璞?
     */
    IPage<SysUserLog> mineLogs(Query<SysUserLog> query, SysUserLogQuery sysUserLogQuery, Integer sysUserId, Set<String> roles);
}
