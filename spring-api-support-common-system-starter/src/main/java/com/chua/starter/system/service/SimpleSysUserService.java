package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysUser;
import jakarta.validation.constraints.NotNull;

/**
 * 接口 SimpleSysUserService 缁ф壙浜?IService锛屼笓闂ㄧ敤浜庡鐞?SysUser 绫荤殑绠€鍗曠敤鎴锋湇鍔?
 * @author CH
 * @since 2024/12/2
 */
public interface SimpleSysUserService extends IService<SysUser> {
    /**
     * 绑定用户ID鍒扮壒瀹氭湇鍔℃垨功能
     *
     * @param sysUserId 用户ID锛岃〃绀鸿绑定鐨勭敤鎴?
     * @return 返回操作结果锛屽寘鍚竴涓竷灏斿€艰〃绀虹粦瀹氭槸鍚︽垚鍔?
     */
    ReturnResult<Boolean> bind(Integer sysUserId);

    /**
     * 解绑用户ID浠庣壒瀹氭湇鍔℃垨功能
     *
     * @param sysUserId 用户ID锛岃〃绀鸿解绑鐨勭敤鎴?
     * @return 返回操作结果锛屽寘鍚竴涓竷灏斿€艰〃绀鸿В缁戞槸鍚︽垚鍔?
     */
    ReturnResult<Boolean> unbind(Integer sysUserId);

    /**
     * 鐢熸垚一个繑鍥炵粨鏋滐紝鍏蜂綋缁撴瀯鍜屽唴瀹圭敱 ReturnBodyResult 瀹氫箟
     *
     * @return 返回一个?ReturnBodyResult 瀵硅薄锛屽寘鍚敓鎴愮殑结果信息
     */
    ReturnResult<byte[]> generate(Integer sysUserId);

    /**
     * 验证用户鐢熸垚鐨勯獙璇佺爜鏄惁姝ｇ‘
     *
     * @param code     用户鐢熸垚鐨勯獙璇佺爜
     * @param username 用户鍚嶏紝
     * @return 返回一个竷灏斿€硷紝琛ㄧず验证鏄惁成功
     */
    ReturnResult<Boolean> verify(String code, @NotNull String username);

    /**
     * 鐢熸垚一个敤浜庤闂壒瀹氭湇鍔＄殑 URI
     *
     * @param sysUserId 用户ID锛岃〃绀鸿鐢熸垚 URI 鐨勭敤鎴?
     * @return 返回一个寘鍚敓鎴愮殑 URI 鐨勫瓧绗︿覆锛岀敤浜庤闂壒瀹氭湇鍔℃垨功能
     */
    ReturnResult<String> generateUri(Integer sysUserId);
}
