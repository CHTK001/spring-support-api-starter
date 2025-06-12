package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.AuthInformation;
import com.chua.starter.oauth.sso.support.parser.Authorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.provider.LoginProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.chua.common.support.lang.code.ReturnCode.OK;


/**
 * http
 *
 * @author CH
 */
@Controller
@Extension("http")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequestMapping("${plugin.auth.server.context-path:}")
@RequiredArgsConstructor
@Tag(name = "认证服务", description = "提供HTTP协议的认证服务接口")
public class HttpProtocol implements Protocol, InitializingBean {

    final AuthServerProperties authServerProperties;

    final LoginProvider loginProvider;

    final LoginCheck loginCheck;

    final ApplicationContext applicationContext;

    @Value("${plugin.auth.server.context-path:}")
    private String contextPath;

    /**
     * 鉴权
     *
     * @return 鉴权
     */
    @PostMapping("/oauth")
    @ResponseBody
    @Operation(
            summary = "用户鉴权",
            description = "验证用户权限，确认用户身份和访问权限",
            responses = {
                    @ApiResponse(responseCode = "200", description = "鉴权成功"),
                    @ApiResponse(responseCode = "401", description = "鉴权失败")
            }
    )
    public ReturnResult<String> oauth(
            @Parameter(description = "认证请求信息", required = true) @RequestBody AuthRequest request1,
            HttpServletRequest request,
            HttpServletResponse response) {
        String data = request1.getData();
        AuthInformation authInformation = new AuthInformation(request1.isEncipher(), data, request, authServerProperties, applicationContext, loginCheck);
        Authorization authorization = authInformation.resolve();
        String address = RequestUtils.getIpAddress(request);

        if (!authorization.hasKey()) {
            loginProvider.logout(request, response);
            return ReturnResult.noAuth();
        }

        if (!authorization.hasTokenOrCookie()) {
            loginProvider.logout(request, response);
            return ReturnResult.noAuth();
        }

        ReturnResult<String> authentication = authorization.authentication(request1.isEncipher());
        if (!OK.getCode().equals(authentication.getCode())) {
            loginProvider.logout(request, response);
            return ReturnResult.noAuth();
        }

        return authentication;
    }

    /**
     * 执行升级操作的方法。
     *
     * @param data     包含升级相关数据的字符串，具体格式和内容根据业务逻辑而定。
     * @param request  HttpServletRequest对象，用于获取客户端请求的相关信息。
     * @param response HttpServletResponse对象，用于向客户端发送响应。
     * @return ReturnResult<String> 包含升级操作结果的返回对象，其中可能包含成功与否、错误信息等。
     */
    @PostMapping("/upgrade")
    @ResponseBody
    @Operation(
            summary = "令牌升级",
            description = "升级用户令牌，刷新令牌有效期或权限",
            responses = {
                    @ApiResponse(responseCode = "200", description = "升级成功"),
                    @ApiResponse(responseCode = "401", description = "升级失败，无效的令牌")
            }
    )
    public ReturnResult<String> upgrade(
            @Parameter(description = "包含升级相关的加密数据", required = true) @RequestParam("data") String data,
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthInformation authInformation = new AuthInformation(true, data, request, authServerProperties, applicationContext, loginCheck);
        Authorization authorization = authInformation.resolve();
        String address = RequestUtils.getIpAddress(request);

        if (!authorization.hasRefreshToken()) {
            loginProvider.logout(request, response);
            return ReturnResult.noAuth();
        }

        if (!authorization.hasKey()) {
            loginProvider.logout(request, response);
            return ReturnResult.noAuth();
        }

        if (!authorization.hasTokenOrCookie()) {
            loginProvider.logout(request, response);
            return ReturnResult.noAuth();
        }

        ReturnResult<String> authentication = authorization.upgrade(address, authServerProperties.getCookieName());
        if (!OK.getCode().equals(authentication.getCode())) {
            loginProvider.logout(request, response);
            return ReturnResult.noAuth();
        }


        return authentication;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
