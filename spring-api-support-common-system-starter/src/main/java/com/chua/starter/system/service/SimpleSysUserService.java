package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.service.model.SysUser;
import jakarta.validation.constraints.NotNull;

/**
 * 鎺ュ彛 SimpleSysUserService 缁ф壙浜?IService锛屼笓闂ㄧ敤浜庡鐞?SysUser 绫荤殑绠€鍗曠敤鎴锋湇鍔?
 * @author CH
 * @since 2024/12/2
 */
public interface SimpleSysUserService extends IService<SysUser> {
    /**
     * 缁戝畾鐢ㄦ埛ID鍒扮壒瀹氭湇鍔℃垨鍔熻兘
     *
     * @param sysUserId 鐢ㄦ埛ID锛岃〃绀鸿缁戝畾鐨勭敤鎴?
     * @return 杩斿洖鎿嶄綔缁撴灉锛屽寘鍚竴涓竷灏斿€艰〃绀虹粦瀹氭槸鍚︽垚鍔?
     */
    ReturnResult<Boolean> bind(Integer sysUserId);

    /**
     * 瑙ｇ粦鐢ㄦ埛ID浠庣壒瀹氭湇鍔℃垨鍔熻兘
     *
     * @param sysUserId 鐢ㄦ埛ID锛岃〃绀鸿瑙ｇ粦鐨勭敤鎴?
     * @return 杩斿洖鎿嶄綔缁撴灉锛屽寘鍚竴涓竷灏斿€艰〃绀鸿В缁戞槸鍚︽垚鍔?
     */
    ReturnResult<Boolean> unbind(Integer sysUserId);

    /**
     * 鐢熸垚涓€涓繑鍥炵粨鏋滐紝鍏蜂綋缁撴瀯鍜屽唴瀹圭敱 ReturnBodyResult 瀹氫箟
     *
     * @return 杩斿洖涓€涓?ReturnBodyResult 瀵硅薄锛屽寘鍚敓鎴愮殑缁撴灉淇℃伅
     */
    ReturnResult<byte[]> generate(Integer sysUserId);

    /**
     * 楠岃瘉鐢ㄦ埛鐢熸垚鐨勯獙璇佺爜鏄惁姝ｇ‘
     *
     * @param code     鐢ㄦ埛鐢熸垚鐨勯獙璇佺爜
     * @param username 鐢ㄦ埛鍚嶏紝
     * @return 杩斿洖涓€涓竷灏斿€硷紝琛ㄧず楠岃瘉鏄惁鎴愬姛
     */
    ReturnResult<Boolean> verify(String code, @NotNull String username);

    /**
     * 鐢熸垚涓€涓敤浜庤闂壒瀹氭湇鍔＄殑 URI
     *
     * @param sysUserId 鐢ㄦ埛ID锛岃〃绀鸿鐢熸垚 URI 鐨勭敤鎴?
     * @return 杩斿洖涓€涓寘鍚敓鎴愮殑 URI 鐨勫瓧绗︿覆锛岀敤浜庤闂壒瀹氭湇鍔℃垨鍔熻兘
     */
    ReturnResult<String> generateUri(Integer sysUserId);
}
