package com.chua.starter.oauth.sso.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.geo.GeoCity;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.sso.mapper.SysUserLogMapper;
import com.chua.starter.oauth.sso.service.IptablesService;
import com.chua.starter.oauth.sso.service.SysUserLogService;
import com.chua.starter.system.entity.SysUserLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 用户日志服务实现类
 * 使用 Java 21 虚拟线程实现异步日志保存功能
 *
 * @author CH
 */
@Slf4j
@Service
public class SysUserLogServiceImpl extends ServiceImpl<SysUserLogMapper, SysUserLog> implements SysUserLogService {

    /**
     * 虚拟线程执行器
     * 使用 Java 21 的虚拟线程技术，提供轻量级的并发处理能力
     */
    private final Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * IP地址处理服务
     * 用于IP地址验证、地理位置解析、安全检查等
     */
    @Autowired
    private IptablesService iptablesService;

    /**
     * 登录日志类型常量
     */
    private static final String LOGIN_LOG_TYPE = "LOGIN";

    /**
     * 登出日志类型常量
     */
    private static final String LOGOUT_LOG_TYPE = "LOGOUT";

    @Override
    public void asyncSaveUserLog(SysUserLog userLog) {
        if (userLog == null) {
            log.warn("用户日志对象为空，跳过保存");
            return;
        }

        // 只处理登录和登出类型的日志
        String logFrom = userLog.getSysLogFrom();
        if (!LOGIN_LOG_TYPE.equals(logFrom) && !LOGOUT_LOG_TYPE.equals(logFrom)) {
            log.debug("跳过非登录/登出类型的日志: logFrom={}", logFrom);
            return;
        }

        // 使用虚拟线程异步处理用户日志保存
        virtualThreadExecutor.execute(() -> {
            try {
                log.debug("开始异步保存用户{}日志: username={}, loginType={}, ip={}",
                         logFrom, userLog.getSysLogUsername(), userLog.getSysLogLoginType(), userLog.getSysLogIp());

                // 处理IP地址信息
                processIpInformation(userLog);

                // 设置默认值
                if (userLog.getSysLogUrl() == null) {
                    userLog.setSysLogUrl("/" + logFrom.toLowerCase());
                }

                // 登出日志默认状态为成功
                if (LOGOUT_LOG_TYPE.equals(logFrom) && userLog.getSysLogStatus() == null) {
                    userLog.setSysLogStatus(1);
                }

                // 设置创建时间和日志编码
                if (userLog.getCreateTime() == null) {
                    userLog.setCreateTime(LocalDateTime.now());
                }

                // 保存到数据库
                save(userLog);

                log.info("用户{}日志保存成功: username={}, loginType={}, status={}, cost={}ms, address={}",
                        logFrom, userLog.getSysLogUsername(), userLog.getSysLogLoginType(),
                        userLog.getSysLogStatus(), userLog.getSysLogCost(), userLog.getSysLogAddress());

            } catch (Exception e) {
                log.error("异步保存用户{}日志失败: username={}, loginType={}, error={}",
                         logFrom, userLog.getSysLogUsername(), userLog.getSysLogLoginType(), e.getMessage(), e);
            }
        });
    }

    /**
     * 处理IP地址相关信息
     * 包括IP验证、地理位置解析、安全检查等
     *
     * @param userLog 用户日志对象
     */
    private void processIpInformation(SysUserLog userLog) {
        String ipAddress = userLog.getSysLogIp();
        if (ipAddress == null || iptablesService == null) {
            log.debug("IP地址为空或IptablesService未注入，跳过IP处理");
            return;
        }

        try {
            // 使用IptablesService处理IP地址
            ReturnResult<GeoCity> ipResult = iptablesService.transferAddress(ipAddress);

            if (ipResult.isOk()) {
                // 补充地理位置信息
                GeoCity geoCity = ipResult.getData();
                if (geoCity != null) {
                    // 使用现有的 sysLogAddress 字段存储地理位置信息
                    userLog.setSysLogAddress(StringUtils.arrayToString(
                            geoCity.getCountry(), geoCity.getProvince(), geoCity.getCity()
                    ));
                    // 使用现有的 sysLogIsp 字段存储ISP信息
                    userLog.setSysLogIsp(geoCity.getIsp());
                }

                log.debug("IP地址处理完成: ip={}, location={}",
                         ipAddress, geoCity != null ? geoCity.getCity() : "未知");
            } else {
                log.warn("IP地址处理失败: ip={}, error={}", ipAddress, ipResult.getMsg());
                userLog.setSysLogAddress("IP地址无效");
            }

        } catch (Exception e) {
            log.error("处理IP地址信息时发生异常: ip={}, error={}", ipAddress, e.getMessage(), e);
            // IP处理失败不应影响日志保存的主流程
            userLog.setSysLogAddress("IP处理异常");
        }
    }


    /**
     * 应用关闭时的清理方法
     * 确保虚拟线程执行器正确关闭
     */
    @jakarta.annotation.PreDestroy
    public void shutdown() {
        try {
            if (virtualThreadExecutor instanceof AutoCloseable) {
                ((AutoCloseable) virtualThreadExecutor).close();
                log.info("虚拟线程执行器已关闭");
            }
        } catch (Exception e) {
            log.warn("关闭虚拟线程执行器时发生异常: {}", e.getMessage());
        }
    }
}
