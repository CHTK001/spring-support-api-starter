package com.chua.starter.oauth.client.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.json.Json;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.infomation.AuthenticationInformation;
import com.chua.starter.oauth.client.support.infomation.Information;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * gRPC协议客户端实现
 * <p>
 * 基于gRPC协议的OAuth认证客户端实现，提供高性能的二进制通信。
 * gRPC协议优势：
 * 1. 基于HTTP/2，支持多路复用和流控制
 * 2. Protocol Buffers序列化，性能优异
 * 3. 强类型接口定义，类型安全
 * 4. 支持流式通信和双向通信
 * 5. 内置负载均衡和服务发现
 * </p>
 *
 * @author CH
 */
@Extension("grpc")
@Slf4j
public class GrpcProtocol extends AbstractProtocol {

    private volatile ManagedChannel channel;
    private volatile OAuthServiceGrpc.OAuthServiceBlockingStub blockingStub;

    public GrpcProtocol(AuthClientProperties authClientProperties) {
        super(authClientProperties);
    }

    /**
     * 初始化gRPC连接
     */
    private void initializeChannel() {
        if (channel == null || channel.isShutdown()) {
            synchronized (this) {
                if (channel == null || channel.isShutdown()) {
                    String serverUrl = selectUrl();
                    if (serverUrl == null) {
                        throw new IllegalStateException("无法获取gRPC服务器地址");
                    }

                    // 解析服务器地址和端口
                    URI uri = URI.create(serverUrl.startsWith("grpc://") ? serverUrl : "grpc://" + serverUrl);
                    String host = uri.getHost();
                    int port = uri.getPort() != -1 ? uri.getPort() : 9090; // 默认gRPC端口

                    // 创建gRPC通道
                    channel = ManagedChannelBuilder.forAddress(host, port)
                            .usePlaintext() // 在生产环境中应该使用TLS
                            .keepAliveTime(30, TimeUnit.SECONDS)
                            .keepAliveTimeout(5, TimeUnit.SECONDS)
                            .keepAliveWithoutCalls(true)
                            .maxInboundMessageSize(4 * 1024 * 1024) // 4MB
                            .build();

                    blockingStub = OAuthServiceGrpc.newBlockingStub(channel);

                    log.info("gRPC OAuth客户端连接已建立: {}:{}", host, port);
                }
            }
        }
    }

    @Override
    protected AuthenticationInformation approve(Cookie cookie, String token, String subProtocol) {
        try {
            initializeChannel();

            // 构建gRPC认证请求
            AuthenticateRequest.Builder requestBuilder = AuthenticateRequest.newBuilder()
                    .setToken(StringUtils.defaultString(token, ""))
                    .setSubProtocol(StringUtils.defaultString(subProtocol, "DEFAULT"))
                    .setAccessKey(authClientProperties.getKey().getAccessKey())
                    .setSecretKey(authClientProperties.getKey().getSecretKey())
                    .setTimestamp(System.currentTimeMillis())
                    .setUuid(IdUtils.simpleUuid())
                    .setEncode(isEncode())
                    .setAddress(RequestUtils.getIpAddress())
                    .setAppName(SpringBeanUtils.getEnvironment().resolvePlaceholders("${spring.application.name:}"));

            if (cookie != null) {
                requestBuilder.setCookie(cookie.getValue());
            }

            // 生成签名
            String sign = generateSign(requestBuilder);
            requestBuilder.setSign(sign);

            // 发送gRPC请求
            AuthenticateResponse response = blockingStub.authenticate(requestBuilder.build());

            // 处理响应
            return processAuthenticateResponse(response);

        } catch (StatusRuntimeException e) {
            log.error("gRPC认证请求失败: {}", e.getStatus());
            return AuthenticationInformation.authServerError();
        } catch (Exception e) {
            log.error("gRPC认证过程异常", e);
            return AuthenticationInformation.authServerError();
        }
    }

    @Override
    protected AuthenticationInformation upgradeInformation(Cookie cookie, String token, UpgradeType upgradeType,
            String refreshToken) {
        try {
            initializeChannel();

            // 构建gRPC升级请求
            UpgradeRequest.Builder requestBuilder = UpgradeRequest.newBuilder()
                    .setToken(StringUtils.defaultString(token, ""))
                    .setUpgradeType(convertUpgradeType(upgradeType))
                    .setRefreshToken(StringUtils.defaultString(refreshToken, ""))
                    .setAccessKey(authClientProperties.getKey().getAccessKey())
                    .setSecretKey(authClientProperties.getKey().getSecretKey())
                    .setTimestamp(System.currentTimeMillis())
                    .setUuid(IdUtils.simpleUuid())
                    .setEncode(isEncode());

            if (cookie != null) {
                requestBuilder.setCookie(cookie.getValue());
            }

            // 生成签名
            String sign = generateUpgradeSign(requestBuilder);
            requestBuilder.setSign(sign);

            // 发送gRPC请求
            UpgradeResponse response = blockingStub.upgrade(requestBuilder.build());

            // 处理响应
            return processUpgradeResponse(response);

        } catch (StatusRuntimeException e) {
            log.error("gRPC令牌升级请求失败: {}", e.getStatus());
            return AuthenticationInformation.authServerError();
        } catch (Exception e) {
            log.error("gRPC令牌升级过程异常", e);
            return AuthenticationInformation.authServerError();
        }
    }

    @Override
    public LoginAuthResult getAccessToken(String username, String password, AuthType authType,
            Map<String, Object> ext) {
        try {
            initializeChannel();

            // 构建gRPC登录请求
            LoginRequest.Builder requestBuilder = LoginRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .setAuthType(convertAuthType(authType))
                    .setAccessKey(authClientProperties.getKey().getAccessKey())
                    .setSecretKey(authClientProperties.getKey().getSecretKey())
                    .setTimestamp(System.currentTimeMillis())
                    .setUuid(IdUtils.simpleUuid())
                    .setEncode(isEncode())
                    .setAddress(RequestUtils.getIpAddress())
                    .setAppName(SpringBeanUtils.getEnvironment().resolvePlaceholders("${spring.application.name:}"));

            // 添加扩展参数
            if (ext != null) {
                ext.forEach((key, value) -> {
                    if (value != null) {
                        requestBuilder.putExt(key, value.toString());
                    }
                });
            }

            // 生成签名
            String sign = generateLoginSign(requestBuilder);
            requestBuilder.setSign(sign);

            // 发送gRPC请求
            LoginResponse response = blockingStub.login(requestBuilder.build());

            // 处理响应
            return processLoginResponse(response);

        } catch (StatusRuntimeException e) {
            log.error("gRPC登录请求失败: {}", e.getStatus());
            return new LoginAuthResult(500, "gRPC登录请求失败: " + e.getStatus().getDescription());
        } catch (Exception e) {
            log.error("gRPC登录过程异常", e);
            return new LoginAuthResult(500, "gRPC登录过程异常: " + e.getMessage());
        }
    }

    @Override
    public LoginAuthResult logout(String uid, LogoutType logoutType, UserResult userResult) {
        try {
            initializeChannel();

            // 构建gRPC登出请求
            LogoutRequest.Builder requestBuilder = LogoutRequest.newBuilder()
                    .setUid(uid)
                    .setLogoutType(convertLogoutType(logoutType))
                    .setAccessKey(authClientProperties.getKey().getAccessKey())
                    .setSecretKey(authClientProperties.getKey().getSecretKey())
                    .setTimestamp(System.currentTimeMillis())
                    .setUuid(IdUtils.simpleUuid());

            // 添加用户信息
            if (userResult != null) {
                requestBuilder.setUserResult(convertUserResult(userResult));
            }

            // 生成签名
            String sign = generateLogoutSign(requestBuilder);
            requestBuilder.setSign(sign);

            // 发送gRPC请求
            LogoutResponse response = blockingStub.logout(requestBuilder.build());

            // 处理响应
            return processLogoutResponse(response);

        } catch (StatusRuntimeException e) {
            log.error("gRPC登出请求失败: {}", e.getStatus());
            return new LoginAuthResult(500, "gRPC登出请求失败: " + e.getStatus().getDescription());
        } catch (Exception e) {
            log.error("gRPC登出过程异常", e);
            return new LoginAuthResult(500, "gRPC登出过程异常: " + e.getMessage());
        }
    }

    /**
     * 生成认证请求签名
     */
    private String generateSign(AuthenticateRequest.Builder requestBuilder) {
        // 构建签名数据Map，与HTTP协议保持一致
        Map<String, Object> signData = new HashMap<>();
        signData.put("x-oauth-cookie", requestBuilder.getCookie());
        signData.put("x-oauth-token", requestBuilder.getToken());
        signData.put("x-oauth-access-key", requestBuilder.getAccessKey());
        signData.put("x-oauth-secret-key", requestBuilder.getSecretKey());
        signData.put("x-oauth-sub-protocol", requestBuilder.getSubProtocol());
        signData.put("x-oauth-timestamp", requestBuilder.getTimestamp());
        signData.put("x-oauth-uuid", requestBuilder.getUuid());
        signData.put("x-oauth-param-address", requestBuilder.getAddress());
        signData.put("x-oauth-param-app-name", requestBuilder.getAppName());

        // 使用gRPC客户端安全工具类生成签名
        return com.chua.starter.oauth.client.support.security.GrpcClientSecurityUtils.generateSignFromMap(signData);
    }

    /**
     * 生成升级请求签名
     */
    private String generateUpgradeSign(UpgradeRequest.Builder requestBuilder) {
        // 构建签名数据Map，与HTTP协议保持一致
        Map<String, Object> signData = new HashMap<>();
        signData.put("x-oauth-cookie", requestBuilder.getCookie());
        signData.put("x-oauth-token", requestBuilder.getToken());
        signData.put("x-oauth-access-key", requestBuilder.getAccessKey());
        signData.put("x-oauth-secret-key", requestBuilder.getSecretKey());
        signData.put("x-oauth-upgrade-type", requestBuilder.getUpgradeType().name());
        signData.put("x-oauth-refresh-token", requestBuilder.getRefreshToken());
        signData.put("x-oauth-timestamp", requestBuilder.getTimestamp());
        signData.put("x-oauth-uuid", requestBuilder.getUuid());

        // 使用gRPC客户端安全工具类生成签名
        return com.chua.starter.oauth.client.support.security.GrpcClientSecurityUtils.generateSignFromMap(signData);
    }

    /**
     * 生成登录请求签名
     */
    private String generateLoginSign(LoginRequest.Builder requestBuilder) {
        // 构建签名数据Map，与HTTP协议保持一致
        Map<String, Object> signData = new HashMap<>();
        signData.put("x-oauth-username", requestBuilder.getUsername());
        signData.put("x-oauth-password", requestBuilder.getPassword());
        signData.put("x-oauth-access-key", requestBuilder.getAccessKey());
        signData.put("x-oauth-secret-key", requestBuilder.getSecretKey());
        signData.put("x-oauth-auth-type", requestBuilder.getAuthType().name());
        signData.put("x-oauth-timestamp", requestBuilder.getTimestamp());
        signData.put("x-oauth-uuid", requestBuilder.getUuid());
        signData.put("x-oauth-param-address", requestBuilder.getAddress());
        signData.put("x-oauth-param-app-name", requestBuilder.getAppName());
        signData.put("x-oauth-ext", requestBuilder.getExtMap());

        // 使用gRPC客户端安全工具类生成签名
        return com.chua.starter.oauth.client.support.security.GrpcClientSecurityUtils.generateSignFromMap(signData);
    }

    /**
     * 生成登出请求签名
     */
    private String generateLogoutSign(LogoutRequest.Builder requestBuilder) {
        // 构建签名数据Map，与HTTP协议保持一致
        Map<String, Object> signData = new HashMap<>();
        signData.put("x-oauth-uid", requestBuilder.getUid());
        signData.put("x-oauth-access-key", requestBuilder.getAccessKey());
        signData.put("x-oauth-secret-key", requestBuilder.getSecretKey());
        signData.put("x-oauth-logout-type", requestBuilder.getLogoutType().name());
        signData.put("x-oauth-timestamp", requestBuilder.getTimestamp());
        signData.put("x-oauth-uuid", requestBuilder.getUuid());

        // 使用gRPC客户端安全工具类生成签名
        return com.chua.starter.oauth.client.support.security.GrpcClientSecurityUtils.generateSignFromMap(signData);
    }

    /**
     * 转换认证类型
     */
    private com.chua.starter.oauth.grpc.AuthType convertAuthType(AuthType authType) {
        if (authType == null) {
            return com.chua.starter.oauth.grpc.AuthType.AUTH_TYPE_AUTO;
        }

        return switch (authType) {
            case AUTO -> com.chua.starter.oauth.grpc.AuthType.AUTH_TYPE_AUTO;
            case STATIC -> com.chua.starter.oauth.grpc.AuthType.AUTH_TYPE_STATIC;
            default -> com.chua.starter.oauth.grpc.AuthType.AUTH_TYPE_AUTO;
        };
    }

    /**
     * 转换升级类型
     */
    private com.chua.starter.oauth.grpc.UpgradeType convertUpgradeType(UpgradeType upgradeType) {
        if (upgradeType == null) {
            return com.chua.starter.oauth.grpc.UpgradeType.UPGRADE_TYPE_REFRESH;
        }

        return switch (upgradeType) {
            case REFRESH -> com.chua.starter.oauth.grpc.UpgradeType.UPGRADE_TYPE_REFRESH;
            case RENEW -> com.chua.starter.oauth.grpc.UpgradeType.UPGRADE_TYPE_RENEW;
            case EXTEND -> com.chua.starter.oauth.grpc.UpgradeType.UPGRADE_TYPE_EXTEND;
            default -> com.chua.starter.oauth.grpc.UpgradeType.UPGRADE_TYPE_REFRESH;
        };
    }

    /**
     * 转换登出类型
     */
    private com.chua.starter.oauth.grpc.LogoutType convertLogoutType(LogoutType logoutType) {
        if (logoutType == null) {
            return com.chua.starter.oauth.grpc.LogoutType.LOGOUT_TYPE_NORMAL;
        }

        return switch (logoutType) {
            case NORMAL -> com.chua.starter.oauth.grpc.LogoutType.LOGOUT_TYPE_NORMAL;
            case FORCE -> com.chua.starter.oauth.grpc.LogoutType.LOGOUT_TYPE_FORCE;
            case TIMEOUT -> com.chua.starter.oauth.grpc.LogoutType.LOGOUT_TYPE_TIMEOUT;
            case KICK -> com.chua.starter.oauth.grpc.LogoutType.LOGOUT_TYPE_KICK;
            default -> com.chua.starter.oauth.grpc.LogoutType.LOGOUT_TYPE_NORMAL;
        };
    }

    /**
     * 转换用户结果对象
     */
    private com.chua.starter.oauth.grpc.UserResult convertUserResult(UserResult userResult) {
        if (userResult == null) {
            return com.chua.starter.oauth.grpc.UserResult.getDefaultInstance();
        }

        UserResume.Builder userResumeBuilder = UserResume.newBuilder()
                .setUnionId(StringUtils.defaultString(userResult.getUnionId(), ""))
                .setOpenId(StringUtils.defaultString(userResult.getOpenId(), ""))
                .setUid(StringUtils.defaultString(userResult.getUid(), ""))
                .setUserId(StringUtils.defaultString(userResult.getUserId(), ""))
                .setLoginType(StringUtils.defaultString(userResult.getLoginType(), ""))
                .setTenantId(StringUtils.defaultString(userResult.getTenantId(), ""))
                .setAddress(StringUtils.defaultString(userResult.getAddress(), ""))
                .setDeptId(StringUtils.defaultString(userResult.getDeptId(), ""))
                .setUsername(StringUtils.defaultString(userResult.getUsername(), ""))
                .setLoginCnt(userResult.getLoginCnt())
                .setNickName(StringUtils.defaultString(userResult.getNickName(), ""))
                .setExpireTime(userResult.getExpireTime() != null ? userResult.getExpireTime() : 0L);

        // 添加角色信息
        if (userResult.getRoles() != null) {
            userResult.getRoles().forEach(userResumeBuilder::addRoles);
        }

        // 添加权限信息
        if (userResult.getPermission() != null) {
            userResult.getPermission().forEach(userResumeBuilder::addPermissions);
        }

        return com.chua.starter.oauth.grpc.UserResult.newBuilder()
                .setUserResume(userResumeBuilder.build())
                .setToken(StringUtils.defaultString(userResult.getToken(), ""))
                .setRefreshToken(StringUtils.defaultString(userResult.getRefreshToken(), ""))
                .setRefreshExpireTime(
                        userResult.getRefreshExpireTime() != null ? userResult.getRefreshExpireTime() : 0L)
                .build();
    }

    /**
     * 处理认证响应
     */
    private AuthenticationInformation processAuthenticateResponse(AuthenticateResponse response) {
        if (response.getCode() != 200) {
            log.warn("gRPC认证失败: code={}, message={}", response.getCode(), response.getMessage());
            return new AuthenticationInformation(Information.AUTHENTICATION_FAILURE, null);
        }

        if (!response.hasUserResult()) {
            return new AuthenticationInformation(Information.AUTHENTICATION_SERVER_EXCEPTION, null);
        }

        UserResult userResult = convertGrpcUserResult(response.getUserResult());
        registerToRequest(userResult);

        return new AuthenticationInformation(Information.OK, userResult);
    }

    /**
     * 处理升级响应
     */
    private AuthenticationInformation processUpgradeResponse(UpgradeResponse response) {
        if (response.getCode() != 200) {
            log.warn("gRPC令牌升级失败: code={}, message={}", response.getCode(), response.getMessage());
            return new AuthenticationInformation(Information.AUTHENTICATION_FAILURE, null);
        }

        if (!response.hasUserResume()) {
            return new AuthenticationInformation(Information.AUTHENTICATION_SERVER_EXCEPTION, null);
        }

        UserResume userResume = convertGrpcUserResume(response.getUserResume());
        registerToRequest(userResume);

        AuthenticationInformation authInfo = new AuthenticationInformation(Information.OK, userResume);
        authInfo.setToken(response.getToken());
        authInfo.setRefreshToken(response.getRefreshToken());

        return authInfo;
    }

    /**
     * 处理登录响应
     */
    private LoginAuthResult processLoginResponse(LoginResponse response) {
        if (response.getCode() != 200) {
            return new LoginAuthResult(response.getCode(), response.getMessage());
        }

        if (!response.hasUserResume()) {
            return new LoginAuthResult(500, "用户信息缺失");
        }

        UserResume userResume = convertGrpcUserResume(response.getUserResume());
        registerToRequest(userResume);

        LoginAuthResult result = new LoginAuthResult(response.getCode(), response.getMessage());
        result.setToken(response.getToken());
        result.setRefreshToken(response.getRefreshToken());
        result.setUserResume(userResume);

        return result;
    }

    /**
     * 处理登出响应
     */
    private LoginAuthResult processLogoutResponse(LogoutResponse response) {
        return new LoginAuthResult(response.getCode(), response.getMessage());
    }

    /**
     * 转换gRPC用户结果为本地用户结果
     */
    private UserResult convertGrpcUserResult(com.chua.starter.oauth.grpc.UserResult grpcUserResult) {
        UserResult userResult = new UserResult();

        if (grpcUserResult.hasUserResume()) {
            UserResume userResume = convertGrpcUserResume(grpcUserResult.getUserResume());
            // 复制UserResume的属性到UserResult
            userResult.setUnionId(userResume.getUnionId());
            userResult.setOpenId(userResume.getOpenId());
            userResult.setUid(userResume.getUid());
            userResult.setUserId(userResume.getUserId());
            userResult.setLoginType(userResume.getLoginType());
            userResult.setTenantId(userResume.getTenantId());
            userResult.setAddress(userResume.getAddress());
            userResult.setDeptId(userResume.getDeptId());
            userResult.setUsername(userResume.getUsername());
            userResult.setLoginCnt(userResume.getLoginCnt());
            userResult.setNickName(userResume.getNickName());
            userResult.setExpireTime(userResume.getExpireTime());
            userResult.setRoles(userResume.getRoles());
            userResult.setPermission(userResume.getPermission());
        }

        userResult.setToken(grpcUserResult.getToken());
        userResult.setRefreshToken(grpcUserResult.getRefreshToken());
        userResult.setRefreshExpireTime(grpcUserResult.getRefreshExpireTime());

        return userResult;
    }

    /**
     * 转换gRPC用户摘要为本地用户摘要
     */
    private UserResume convertGrpcUserResume(com.chua.starter.oauth.grpc.UserResume grpcUserResume) {
        UserResume userResume = new UserResume();
        userResume.setUnionId(grpcUserResume.getUnionId());
        userResume.setOpenId(grpcUserResume.getOpenId());
        userResume.setUid(grpcUserResume.getUid());
        userResume.setUserId(grpcUserResume.getUserId());
        userResume.setLoginType(grpcUserResume.getLoginType());
        userResume.setTenantId(grpcUserResume.getTenantId());
        userResume.setAddress(grpcUserResume.getAddress());
        userResume.setDeptId(grpcUserResume.getDeptId());
        userResume.setUsername(grpcUserResume.getUsername());
        userResume.setLoginCnt(grpcUserResume.getLoginCnt());
        userResume.setNickName(grpcUserResume.getNickName());
        userResume.setExpireTime(grpcUserResume.getExpireTime());

        // 转换角色和权限列表
        userResume.setRoles(grpcUserResume.getRolesList().stream().collect(java.util.stream.Collectors.toSet()));
        userResume.setPermission(
                grpcUserResume.getPermissionsList().stream().collect(java.util.stream.Collectors.toSet()));

        return userResume;
    }

    /**
     * 关闭gRPC连接
     */
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("gRPC OAuth客户端连接已关闭");
            } catch (InterruptedException e) {
                log.warn("关闭gRPC连接时被中断", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
