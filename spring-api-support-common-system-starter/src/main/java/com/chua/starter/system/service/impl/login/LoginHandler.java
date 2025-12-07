package com.chua.starter.system.service.impl.login;

import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.system.result.LoginResult;
import com.chua.starter.system.service.impl.SysLoginServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 鐧诲綍澶勭悊鍣?
 * @author CH
 * @since 2024/12/2
 */
public interface LoginHandler {


    /**
     * 鎵ц鐢ㄦ埛鐧诲綍鎿嶄綔
     *
     * @param sysLoginService 鐧诲綍鏈嶅姟瀹炵幇绫伙紝鐢ㄤ簬澶勭悊鐧诲綍閫昏緫
     * @param loginData       鐧诲綍鎵€闇€鐨勬暟鎹紝鍖呭惈鐢ㄦ埛鍑瘉绛変俊鎭?
     * @param request         HTTP璇锋眰瀵硅薄锛屽彲鐢ㄤ簬鑾峰彇璇锋眰鐩稿叧鏁版嵁
     * @param response        HTTP鍝嶅簲瀵硅薄锛屽彲鐢ㄤ簬璁剧疆鐧诲綍鍚庣殑鍝嶅簲鏁版嵁
     * @param fingerprint
     * @return 杩斿洖鐧诲綍缁撴灉锛屽寘鍚櫥褰曟槸鍚︽垚鍔熷強鐢ㄦ埛鐩稿叧淇℃伅
     */
    Result<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint);
}
