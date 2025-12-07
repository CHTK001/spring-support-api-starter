package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.service.pojo.user.RefreshToken;
import com.chua.starter.service.pojo.user.ThirdPartyUnbindRequest;
import com.chua.starter.service.result.LoginResult;
import com.chua.starter.service.result.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 鐧诲綍鎺ュ彛
 * @author CH
 * @since 2024/9/12
 */
public interface SysLoginService {

    /**
     * 鐢ㄦ埛鐧诲綍鏂规硶锛屾牴鎹櫥褰曟暟鎹繘琛岀敤鎴疯韩浠介獙璇?
     *
     * @param loginData   鐧诲綍鏁版嵁瀵硅薄锛屽寘鍚敤鎴峰悕銆佸瘑鐮佺瓑淇℃伅
     * @param request     HTTP璇锋眰瀵硅薄锛岀敤浜庤幏鍙栬姹傜浉鍏崇殑鏁版嵁
     * @param response    HTTP鍝嶅簲瀵硅薄锛岀敤浜庤缃搷搴旀暟鎹?
     * @param fingerprint
     * @return 缁撴灉瀵硅薄锛屽寘鍚櫥褰曠粨鏋滃拰鍙兘鐨勯檮鍔犱俊鎭?
     */
    Result<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint);

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛鐨勪俊鎭?
     *
     * @return 鐢ㄦ埛淇℃伅瀵硅薄锛屽寘鍚敤鎴风殑鍩烘湰淇℃伅
     */
    UserInfoVO getUserLoginInfo();
    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛鐨勪俊鎭?
     *
     * @param token 鐢ㄦ埛鐨勪护鐗岋紝鐢ㄤ簬纭畾褰撳墠鐧诲綍鐢ㄦ埛
     * @return 鐢ㄦ埛淇℃伅瀵硅薄锛屽寘鍚敤鎴风殑鍩烘湰淇℃伅
     */
    UserInfoVO getUserLoginInfo(String token);
    /**
     * 鍒锋柊鐢ㄦ埛浠ょ墝
     *
     * @param refreshToken 鍒锋柊浠ょ墝瀵硅薄锛屽寘鍚棫鐨勪护鐗屼俊鎭?
     * @param request HTTP璇锋眰瀵硅薄锛岀敤浜庤幏鍙栬姹傜浉鍏崇殑鏁版嵁
     * @param response HTTP鍝嶅簲瀵硅薄锛岀敤浜庤缃搷搴旀暟鎹?
     * @return 缁撴灉瀵硅薄锛屽寘鍚埛鏂扮粨鏋滃拰鍙兘鐨勯檮鍔犱俊鎭?
     */
    Result<LoginResult> refresh(RefreshToken refreshToken, HttpServletRequest request, HttpServletResponse response);

    /**
     * 涓夋柟鐧诲綍
     *
     * @param loginData 鐧诲綍鏁版嵁瀵硅薄锛屽寘鍚笁鏂圭櫥褰曚俊鎭?
     * @param request HTTP璇锋眰瀵硅薄锛岀敤浜庤幏鍙栬姹傜浉鍏崇殑鏁版嵁
     * @param response HTTP鍝嶅簲瀵硅薄锛岀敤浜庤缃搷搴旀暟鎹?
     * @return 缁撴灉瀵硅薄锛屽寘鍚笁鏂圭櫥褰曠粨鏋滃拰鍙兘鐨勯檮鍔犱俊鎭?
     */
    ReturnResult<String> thirdLoginCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response);

    /**
     * 涓夋柟缁戝畾
     *
     * @param loginData 鐧诲綍鏁版嵁瀵硅薄锛屽寘鍚笁鏂圭櫥褰曚俊鎭?
     * @param request HTTP璇锋眰瀵硅薄锛岀敤浜庤幏鍙栬姹傜浉鍏崇殑鏁版嵁
     * @param response HTTP鍝嶅簲瀵硅薄锛岀敤浜庤缃搷搴旀暟鎹?
     * @return 缁撴灉瀵硅薄锛屽寘鍚笁鏂圭粦瀹氱粨鏋滃拰鍙兘鐨勯檮鍔犱俊鎭?
     */
    ReturnResult<String> thirdBindCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response);

    /**
     * 鑾峰彇涓夋柟缁戝畾淇℃伅
     *
     * @param userId 鐢ㄦ埛ID锛岀敤浜庣‘瀹氳鑾峰彇缁戝畾淇℃伅鐨勭敤鎴?
     * @return 缁戝畾淇℃伅鍒楄〃锛屽寘鍚綋鍓嶇敤鎴风粦瀹氱殑涓夋柟淇℃伅
     */
    ReturnResult<List<String>> thirdBindInfo(String userId);

    /**
     * 涓夋柟瑙ｇ粦
     *
     * @param userId 鐢ㄦ埛ID锛岀敤浜庣‘瀹氳瑙ｇ粦鐨勭敤鎴?
     * @param thirdPartyUnbindRequest 瑙ｇ粦璇锋眰瀵硅薄锛屽寘鍚瑙ｇ粦鐨勪笁鏂逛俊鎭?
     * @return 鎿嶄綔缁撴灉瀵硅薄锛屽寘鍚搷浣滄槸鍚︽垚鍔熷拰鐩稿叧鎻愮ず淇℃伅
     */
    ReturnResult<Boolean> thirdUnbind(String userId, ThirdPartyUnbindRequest thirdPartyUnbindRequest);
}
