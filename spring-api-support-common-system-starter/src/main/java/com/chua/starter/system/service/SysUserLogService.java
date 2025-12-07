package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.system.pojo.user.log.SysUserLogQuery;

import java.util.Set;

/**
 * 系统用户日志服务接口
 * 璇ユ帴鍙ｅ畾涔変簡涓庣郴缁熺敤鎴锋棩蹇楃浉鍏崇殑操作锛屽寘鎷煡璇㈢敤鎴锋棩蹇楀垎椤垫暟鎹拰删除鎸囧畾鐨勬棩蹇?
 * @author CH
 * @since 2024/8/8
 */
public interface SysUserLogService extends IService<SysUserLog> {

    /**
     * 鏍规嵁查询鏉′欢分页获取用户日志
     * 姝ゆ柟娉曞厑璁告牴鎹笉鍚岀殑查询鏉′欢鍜岃鑹茬瓫閫夛紝返回用户日志鐨勫垎椤垫暟鎹?
     *
     * @param query 查询瀵硅薄锛岀敤浜庡垎椤靛拰排序设置
     * @param sysUserLogQuery 查询鏉′欢瀵硅薄锛岀敤浜庣瓫閫夋棩蹇?
     * @param roles 用户角色闆嗗悎锛岀敤浜庢牴鎹鑹茬瓫閫夋棩蹇?
     * @return 返回鍖呭惈用户日志鐨勫垎椤垫暟鎹璞?
     */
    IPage<SysUserLog> pageForUserLog(Query<SysUserLog> query, SysUserLogQuery sysUserLogQuery, Set<String> roles);

    /**
     * 删除鎸囧畾鐨勭敤鎴锋棩蹇?
     * 姝ゆ柟娉曢€氳繃日志鐨処D杩涜删除操作锛岃繑鍥炰竴涓〃绀烘搷浣滅粨鏋滅殑瀵硅薄
     *
     * @param userLogId 用户日志鐨処D
     * @return 返回一个寘鍚搷浣滅粨鏋滅殑瀵硅薄锛屽璞′腑鐨勬暟鎹瓧娈佃〃绀烘搷浣滄槸鍚︽垚鍔?
     */
    ReturnResult<Boolean> delete(Long userLogId);

    /**
     * 获取当前用户鐨勬棩蹇?
     * 姝ゆ柟娉曟牴鎹敤鎴风殑角色鍜屾煡璇㈡潯浠惰幏鍙栧綋鍓嶇敤鎴风殑日志锛岃繑鍥炰竴涓寘鍚棩蹇楃殑分页数据瀵硅薄
     *
     * @param query      查询瀵硅薄锛岀敤浜庡垎椤靛拰排序设置
     * @param sysUserLogQuery 查询鏉′欢瀵硅薄锛岀敤浜庣瓫閫夋棩蹇?
     * @param sysUserId  账号ID
     * @param roles      用户角色闆嗗悎锛岀敤浜庢牴鎹鑹茬瓫閫夋棩蹇?
     * @return 返回鍖呭惈当前用户日志鐨勫垎椤垫暟鎹璞?
     */
    IPage<SysUserLog> mineLogs(Query<SysUserLog> query, SysUserLogQuery sysUserLogQuery, Integer sysUserId, Set<String> roles);
}
