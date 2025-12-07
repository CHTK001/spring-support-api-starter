package com.chua.starter.system.service.impl.login;

import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.system.result.LoginResult;
import com.chua.starter.system.service.impl.SysLoginServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录处理鍣?
 * @author CH
 * @since 2024/12/2
 */
public interface LoginHandler {


    /**
     * 鎵ц用户登录操作
     *
     * @param sysLoginService 登录服务瀹炵幇绫伙紝鐢ㄤ簬处理登录閫昏緫
     * @param loginData       登录鎵€闇€鐨勬暟鎹紝鍖呭惈用户鍑瘉绛変俊鎭?
     * @param request         HTTP请求瀵硅薄锛屽彲鐢ㄤ簬获取请求鐩稿叧数据
     * @param response        HTTP响应瀵硅薄锛屽彲鐢ㄤ簬设置登录鍚庣殑响应数据
     * @param fingerprint
     * @return 返回登录结果锛屽寘鍚櫥褰曟槸鍚︽垚鍔熷強用户鐩稿叧信息
     */
    Result<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint);
}
