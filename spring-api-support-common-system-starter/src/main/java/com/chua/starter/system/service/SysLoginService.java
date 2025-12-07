package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.system.pojo.user.RefreshToken;
import com.chua.starter.system.pojo.user.ThirdPartyUnbindRequest;
import com.chua.starter.system.result.LoginResult;
import com.chua.starter.system.result.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 登录接口
 * @author CH
 * @since 2024/9/12
 */
public interface SysLoginService {

    /**
     * 用户登录鏂规硶锛屾牴鎹櫥褰曟暟鎹繘琛岀敤鎴疯韩浠介獙璇?
     *
     * @param loginData   登录数据瀵硅薄锛屽寘鍚敤鎴峰悕銆佸瘑鐮佺瓑信息
     * @param request     HTTP请求瀵硅薄锛岀敤浜庤幏鍙栬姹傜浉鍏崇殑数据
     * @param response    HTTP响应瀵硅薄锛岀敤浜庤缃搷搴旀暟鎹?
     * @param fingerprint
     * @return 结果瀵硅薄锛屽寘鍚櫥褰曠粨鏋滃拰鍙兘鐨勯檮鍔犱俊鎭?
     */
    Result<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint);

    /**
     * 获取当前登录用户鐨勪俊鎭?
     *
     * @return 用户信息瀵硅薄锛屽寘鍚敤鎴风殑鍩烘湰信息
     */
    UserInfoVO getUserLoginInfo();
    /**
     * 获取当前登录用户鐨勪俊鎭?
     *
     * @param token 用户鐨勪护鐗岋紝鐢ㄤ簬确认畾当前登录用户
     * @return 用户信息瀵硅薄锛屽寘鍚敤鎴风殑鍩烘湰信息
     */
    UserInfoVO getUserLoginInfo(String token);
    /**
     * 刷新用户浠ょ墝
     *
     * @param refreshToken 刷新浠ょ墝瀵硅薄锛屽寘鍚棫鐨勪护鐗屼俊鎭?
     * @param request HTTP请求瀵硅薄锛岀敤浜庤幏鍙栬姹傜浉鍏崇殑数据
     * @param response HTTP响应瀵硅薄锛岀敤浜庤缃搷搴旀暟鎹?
     * @return 结果瀵硅薄锛屽寘鍚埛鏂扮粨鏋滃拰鍙兘鐨勯檮鍔犱俊鎭?
     */
    Result<LoginResult> refresh(RefreshToken refreshToken, HttpServletRequest request, HttpServletResponse response);

    /**
     * 涓夋柟登录
     *
     * @param loginData 登录数据瀵硅薄锛屽寘鍚笁鏂圭櫥褰曚俊鎭?
     * @param request HTTP请求瀵硅薄锛岀敤浜庤幏鍙栬姹傜浉鍏崇殑数据
     * @param response HTTP响应瀵硅薄锛岀敤浜庤缃搷搴旀暟鎹?
     * @return 结果瀵硅薄锛屽寘鍚笁鏂圭櫥褰曠粨鏋滃拰鍙兘鐨勯檮鍔犱俊鎭?
     */
    ReturnResult<String> thirdLoginCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response);

    /**
     * 涓夋柟绑定
     *
     * @param loginData 登录数据瀵硅薄锛屽寘鍚笁鏂圭櫥褰曚俊鎭?
     * @param request HTTP请求瀵硅薄锛岀敤浜庤幏鍙栬姹傜浉鍏崇殑数据
     * @param response HTTP响应瀵硅薄锛岀敤浜庤缃搷搴旀暟鎹?
     * @return 结果瀵硅薄锛屽寘鍚笁鏂圭粦瀹氱粨鏋滃拰鍙兘鐨勯檮鍔犱俊鎭?
     */
    ReturnResult<String> thirdBindCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取涓夋柟绑定信息
     *
     * @param userId 用户ID锛岀敤浜庣‘瀹氳获取绑定信息鐨勭敤鎴?
     * @return 绑定信息列表锛屽寘鍚綋鍓嶇敤鎴风粦瀹氱殑涓夋柟信息
     */
    ReturnResult<List<String>> thirdBindInfo(String userId);

    /**
     * 涓夋柟解绑
     *
     * @param userId 用户ID锛岀敤浜庣‘瀹氳解绑鐨勭敤鎴?
     * @param thirdPartyUnbindRequest 解绑请求瀵硅薄锛屽寘鍚解绑鐨勪笁鏂逛俊鎭?
     * @return 操作结果瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧提示信息
     */
    ReturnResult<Boolean> thirdUnbind(String userId, ThirdPartyUnbindRequest thirdPartyUnbindRequest);
}
