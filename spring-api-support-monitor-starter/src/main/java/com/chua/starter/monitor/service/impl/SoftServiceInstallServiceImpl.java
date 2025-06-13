package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnCode;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.protocol.channel.Channel;
import com.chua.common.support.protocol.session.ChannelSession;
import com.chua.socketio.support.MsgEvent;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.ssh.support.ssh.SshClient;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.monitor.entity.SoftServiceInstall;
import com.chua.starter.monitor.entity.SoftServiceLog;
import com.chua.starter.monitor.jpom.func.assets.model.MachineSshModel;
import com.chua.starter.monitor.jpom.func.assets.server.MachineSshServer;
import com.chua.starter.monitor.mapper.SoftServiceInstallMapper;
import com.chua.starter.monitor.pojo.SoftServiceStatusInfo;
import com.chua.starter.monitor.service.SoftServiceInstallService;
import com.chua.starter.monitor.service.SoftServiceLogService;
import com.chua.starter.monitor.service.SoftServiceService;
import com.chua.starter.monitor.starter.service.TerminalService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 软件服务安装记录服务实现类
 *
 * @author CH
 * @since 2024/7/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SoftServiceInstallServiceImpl extends ServiceImpl<SoftServiceInstallMapper, SoftServiceInstall> implements SoftServiceInstallService {

    private final SoftServiceService softServiceService;
    private final TerminalService terminalService;
    private final MachineSshServer machineSshServer;
    // 添加日志文件基础路径
    private static final String LOG_DIR_PATH = "/soft/install/";
    /**
     * Redis缓存key前缀
     */
    private static final String CACHE_KEY_PREFIX = "soft_service_install:";
    /**
     * 缓存过期时间（秒）
     */
    private static final long CACHE_EXPIRE_TIME = 30;
    private final SocketSessionTemplate socketSessionTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SoftServiceLogService softServiceLogService;

    @Override
    public ReturnResult<Boolean> addInstall(SoftServiceInstall softServiceInstall) {
        // 设置初始状态
        if (softServiceInstall.getInstallStatus() == null) {
            softServiceInstall.setInstallStatus(0); // 未安装
        }
        if (softServiceInstall.getInstallStatus() == null) {
            softServiceInstall.setInstallStatus(0); // 未运行
        }
        softServiceInstall.setInstallTime(System.currentTimeMillis());

        // 获取软件服务信息，缓存命令数据
        try {
            SoftService softService = softServiceService.getById(softServiceInstall.getSoftServiceId());
            if (softService != null) {
                // 缓存SoftService命令数据
                softServiceInstall.setSoftServiceName(softService.getSoftServiceName());
                softServiceInstall.setSoftServiceInstallCommand(softService.getSoftServiceInstallCommand());
                softServiceInstall.setSoftServiceInstallCommandType(softService.getSoftServiceInstallCommandType());
                softServiceInstall.setSoftServiceUninstallCommand(softService.getSoftServiceUninstallCommand());
                softServiceInstall.setSoftServiceUninstallCommandType(softService.getSoftServiceUninstallCommandType());
                softServiceInstall.setSoftServiceStartCommand(softService.getSoftServiceStartCommand());
                softServiceInstall.setSoftServiceStartCommandType(softService.getSoftServiceStartCommandType());
                softServiceInstall.setSoftServiceStopCommand(softService.getSoftServiceStopCommand());
                softServiceInstall.setSoftServiceStopCommandType(softService.getSoftServiceStopCommandType());
                softServiceInstall.setSoftServiceRestartCommand(softService.getSoftServiceRestartCommand());
                softServiceInstall.setSoftServiceRestartCommandType(softService.getSoftServiceRestartCommandType());
                softServiceInstall.setSoftServiceStatusCheckCommand(softService.getSoftServiceStatusCheckCommand());
                softServiceInstall.setSoftServiceStatusCheckCommandType(softService.getSoftServiceStatusCheckCommandType());
                softServiceInstall.setSoftServiceStatusCheckSuccessFlag(softService.getSoftServiceStatusCheckSuccessFlag());

                // 同步日志路径
                if (StringUtils.hasText(softService.getSoftServiceLogPath())) {
                    softServiceInstall.setServiceLogPath(softService.getSoftServiceLogPath());
                }
            }
        } catch (Exception e) {
            log.error("获取软件服务信息失败: {}", e.getMessage(), e);
        }

        boolean success = this.save(softServiceInstall);

        // 如果保存成功且ID已生成，添加到缓存
        if (success && softServiceInstall.getInstallId() != null) {
            String cacheKey = CACHE_KEY_PREFIX + softServiceInstall.getInstallId();
            redisTemplate.opsForValue().set(cacheKey, softServiceInstall, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        }

        return ReturnResult.preconditioning(success).toResult();
    }

    @Override
    public ReturnResult<Boolean> updateInstall(SoftServiceInstall softServiceInstall) {
        // 获取软件服务信息，缓存命令数据
        try {
            SoftService softService = softServiceService.getById(softServiceInstall.getSoftServiceId());
            if (softService != null) {
                // 缓存SoftService命令数据
                softServiceInstall.setSoftServiceName(softService.getSoftServiceName());
                softServiceInstall.setSoftServiceInstallCommand(softService.getSoftServiceInstallCommand());
                softServiceInstall.setSoftServiceInstallCommandType(softService.getSoftServiceInstallCommandType());
                softServiceInstall.setSoftServiceUninstallCommand(softService.getSoftServiceUninstallCommand());
                softServiceInstall.setSoftServiceUninstallCommandType(softService.getSoftServiceUninstallCommandType());
                softServiceInstall.setSoftServiceStartCommand(softService.getSoftServiceStartCommand());
                softServiceInstall.setSoftServiceStartCommandType(softService.getSoftServiceStartCommandType());
                softServiceInstall.setSoftServiceStopCommand(softService.getSoftServiceStopCommand());
                softServiceInstall.setSoftServiceStopCommandType(softService.getSoftServiceStopCommandType());
                softServiceInstall.setSoftServiceRestartCommand(softService.getSoftServiceRestartCommand());
                softServiceInstall.setSoftServiceRestartCommandType(softService.getSoftServiceRestartCommandType());
                softServiceInstall.setSoftServiceStatusCheckCommand(softService.getSoftServiceStatusCheckCommand());
                softServiceInstall.setSoftServiceStatusCheckCommandType(softService.getSoftServiceStatusCheckCommandType());
                softServiceInstall.setSoftServiceStatusCheckSuccessFlag(softService.getSoftServiceStatusCheckSuccessFlag());
            }
        } catch (Exception e) {
            log.error("获取软件服务信息失败: {}", e.getMessage(), e);
        }

        boolean success = this.updateById(softServiceInstall);

        // 更新成功后清除缓存
        if (success && softServiceInstall.getInstallId() != null) {
            clearInstallCache(softServiceInstall.getInstallId());
        }

        return ReturnResult.preconditioning(success).toResult();
    }

    @Override
    public ReturnResult<Boolean> removeInstallById(Integer id) {
        boolean success = this.removeById(id);

        // 删除成功后清除缓存
        if (success) {
            clearInstallCache(id);
        }

        return ReturnResult.preconditioning(success).toResult();
    }

    @Override
    public SoftServiceInstall getInstallById(Integer installId) {
        String cacheKey = CACHE_KEY_PREFIX + installId;

        // 先从Redis缓存获取
        SoftServiceInstall cachedInstall = (SoftServiceInstall) redisTemplate.opsForValue().get(cacheKey);

        // 如果缓存存在，直接返回
        if (cachedInstall != null) {
            return cachedInstall;
        }

        // 缓存不存在，从数据库获取
        SoftServiceInstall install = super.getById(installId);

        // 更新缓存
        if (install != null) {
            // 获取软件服务信息，缓存命令数据
            try {
                SoftService softService = softServiceService.getById(install.getSoftServiceId());
                if (softService != null) {
                    install.setSoftServiceName(softService.getSoftServiceName());
                    // 缓存SoftService命令数据
                    install.setSoftServiceInstallCommand(softService.getSoftServiceInstallCommand());
                    install.setSoftServiceInstallCommandType(softService.getSoftServiceInstallCommandType());
                    install.setSoftServiceUninstallCommand(softService.getSoftServiceUninstallCommand());
                    install.setSoftServiceUninstallCommandType(softService.getSoftServiceUninstallCommandType());
                    install.setSoftServiceStartCommand(softService.getSoftServiceStartCommand());
                    install.setSoftServiceStartCommandType(softService.getSoftServiceStartCommandType());
                    install.setSoftServiceStopCommand(softService.getSoftServiceStopCommand());
                    install.setSoftServiceStopCommandType(softService.getSoftServiceStopCommandType());
                    install.setSoftServiceRestartCommand(softService.getSoftServiceRestartCommand());
                    install.setSoftServiceRestartCommandType(softService.getSoftServiceRestartCommandType());
                    install.setSoftServiceStatusCheckCommand(softService.getSoftServiceStatusCheckCommand());
                    install.setSoftServiceStatusCheckCommandType(softService.getSoftServiceStatusCheckCommandType());
                    install.setSoftServiceStatusCheckSuccessFlag(softService.getSoftServiceStatusCheckSuccessFlag());

                    // 新增：添加命令成功和失败标识
                    install.setSoftServiceStatusCheckFailureFlag(softService.getSoftServiceStatusCheckFailureFlag());
                    install.setSoftServiceInstallSuccessFlag(softService.getSoftServiceInstallSuccessFlag());
                    install.setSoftServiceInstallFailureFlag(softService.getSoftServiceInstallFailureFlag());
                    install.setSoftServiceUninstallSuccessFlag(softService.getSoftServiceUninstallSuccessFlag());
                    install.setSoftServiceUninstallFailureFlag(softService.getSoftServiceUninstallFailureFlag());
                    install.setSoftServiceStartSuccessFlag(softService.getSoftServiceStartSuccessFlag());
                    install.setSoftServiceStartFailureFlag(softService.getSoftServiceStartFailureFlag());
                    install.setSoftServiceStopSuccessFlag(softService.getSoftServiceStopSuccessFlag());
                    install.setSoftServiceStopFailureFlag(softService.getSoftServiceStopFailureFlag());
                    install.setSoftServiceRestartSuccessFlag(softService.getSoftServiceRestartSuccessFlag());
                    install.setSoftServiceRestartFailureFlag(softService.getSoftServiceRestartFailureFlag());

                    // 同步日志路径
                    if (StringUtils.hasText(softService.getSoftServiceLogPath())) {
                        install.setServiceLogPath(softService.getSoftServiceLogPath());
                    }
                }

                // 获取SSH服务器信息
                MachineSshModel sshModel = machineSshServer.getByKey(install.getSshId());
                if (sshModel != null) {
                    install.setSshHost(sshModel.getHost());
                    install.setSshPort(sshModel.getPort());
                    install.setSshName(sshModel.getName());
                }
            } catch (Exception e) {
                log.error("填充安装记录信息失败: {}", e.getMessage(), e);
            }
            
            redisTemplate.opsForValue().set(cacheKey, install, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        }

        return install;
    }

    @Override
    public List<SoftServiceInstall> listInstallBySshId(String sshId) {
        LambdaQueryWrapper<SoftServiceInstall> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoftServiceInstall::getSshId, sshId);
        List<SoftServiceInstall> installList = this.list(queryWrapper);

        // 如果没有安装记录，直接返回空列表
        if (installList == null || installList.isEmpty()) {
            return installList;
        }

        try {
            // 获取SSH服务器信息
            MachineSshModel sshModel = machineSshServer.getByKey(sshId);

            // 收集所有的软件服务ID
            List<Integer> softServiceIds = installList.stream()
                    .map(SoftServiceInstall::getSoftServiceId)
                    .distinct()
                    .toList();

            // 使用in查询批量获取软件服务信息，避免循环查询数据库
            Map<Integer, SoftService> softServiceMap = new HashMap<>();
            if (!softServiceIds.isEmpty()) {
                List<SoftService> softServiceList = softServiceService.listByIds(softServiceIds);
                if (softServiceList != null) {
                    softServiceMap = softServiceList.stream()
                            .collect(Collectors.toMap(SoftService::getSoftServiceId, service -> service));
                }
            }

            // 填充信息到安装记录中
            for (SoftServiceInstall install : installList) {
                // 填充SSH信息
                if (sshModel != null) {
                    install.setSshHost(sshModel.getHost());
                    install.setSshPort(sshModel.getPort());
                    install.setSshName(sshModel.getName());
                }

                // 填充软件服务名称和命令数据
                SoftService softService = softServiceMap.get(install.getSoftServiceId());
                if (softService != null) {
                    install.setSoftServiceName(softService.getSoftServiceName());
                    // 缓存SoftService命令数据
                    install.setSoftServiceInstallCommand(softService.getSoftServiceInstallCommand());
                    install.setSoftServiceInstallCommandType(softService.getSoftServiceInstallCommandType());
                    install.setSoftServiceUninstallCommand(softService.getSoftServiceUninstallCommand());
                    install.setSoftServiceUninstallCommandType(softService.getSoftServiceUninstallCommandType());
                    install.setSoftServiceStartCommand(softService.getSoftServiceStartCommand());
                    install.setSoftServiceStartCommandType(softService.getSoftServiceStartCommandType());
                    install.setSoftServiceStopCommand(softService.getSoftServiceStopCommand());
                    install.setSoftServiceStopCommandType(softService.getSoftServiceStopCommandType());
                    install.setSoftServiceRestartCommand(softService.getSoftServiceRestartCommand());
                    install.setSoftServiceRestartCommandType(softService.getSoftServiceRestartCommandType());
                    install.setSoftServiceStatusCheckCommand(softService.getSoftServiceStatusCheckCommand());
                    install.setSoftServiceStatusCheckCommandType(softService.getSoftServiceStatusCheckCommandType());
                    install.setSoftServiceStatusCheckSuccessFlag(softService.getSoftServiceStatusCheckSuccessFlag());

                    // 新增：添加命令成功和失败标识
                    install.setSoftServiceStatusCheckFailureFlag(softService.getSoftServiceStatusCheckFailureFlag());
                    install.setSoftServiceInstallSuccessFlag(softService.getSoftServiceInstallSuccessFlag());
                    install.setSoftServiceInstallFailureFlag(softService.getSoftServiceInstallFailureFlag());
                    install.setSoftServiceUninstallSuccessFlag(softService.getSoftServiceUninstallSuccessFlag());
                    install.setSoftServiceUninstallFailureFlag(softService.getSoftServiceUninstallFailureFlag());
                    install.setSoftServiceStartSuccessFlag(softService.getSoftServiceStartSuccessFlag());
                    install.setSoftServiceStartFailureFlag(softService.getSoftServiceStartFailureFlag());
                    install.setSoftServiceStopSuccessFlag(softService.getSoftServiceStopSuccessFlag());
                    install.setSoftServiceStopFailureFlag(softService.getSoftServiceStopFailureFlag());
                    install.setSoftServiceRestartSuccessFlag(softService.getSoftServiceRestartSuccessFlag());
                    install.setSoftServiceRestartFailureFlag(softService.getSoftServiceRestartFailureFlag());
                } else {
                    install.setSoftServiceName("未知软件");
                }
            }
        } catch (Exception e) {
            log.error("填充安装记录信息失败: {}", e.getMessage(), e);
        }

        return installList;
    }

    @Override
    public List<SoftServiceInstall> listInstallBySoftServiceId(Integer softServiceId) {
        LambdaQueryWrapper<SoftServiceInstall> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoftServiceInstall::getSoftServiceId, softServiceId);
        List<SoftServiceInstall> installList = this.list(queryWrapper);

        // 如果没有安装记录，直接返回空列表
        if (installList == null || installList.isEmpty()) {
            return installList;
        }

        // 获取软件服务信息
        SoftService softService = softServiceService.getById(softServiceId);
        String softServiceName = softService != null ? softService.getSoftServiceName() : "未知软件";

        // 收集所有的SSH ID
        List<String> sshIds = installList.stream()
                .map(SoftServiceInstall::getSshId)
                .distinct()
                .toList();

        try {
            // 使用批量查询获取所有SSH服务器信息
            Map<String, MachineSshModel> sshModelMap = new HashMap<>();
            if (!sshIds.isEmpty()) {
                // 由于MachineSshServer没有提供listByIds方法，这里逐个查询
                for (String id : sshIds) {
                    MachineSshModel sshModel = machineSshServer.getByKey(id);
                    if (sshModel != null) {
                        sshModelMap.put(sshModel.getId(), sshModel);
                    }
                }
            }

            // 填充SSH信息到安装记录中
            for (SoftServiceInstall install : installList) {
                MachineSshModel sshModel = sshModelMap.get(install.getSshId());
                if (sshModel != null) {
                    // 使用TableField(exist = false)添加的额外字段
                    install.setSshHost(sshModel.getHost());
                    install.setSshPort(sshModel.getPort());
                    install.setSshName(sshModel.getName());
                }

                // 添加软件服务名称到每一个安装记录中
                install.setSoftServiceName(softServiceName);

                // 缓存SoftService命令数据
                if (softService != null) {
                    install.setSoftServiceInstallCommand(softService.getSoftServiceInstallCommand());
                    install.setSoftServiceInstallCommandType(softService.getSoftServiceInstallCommandType());
                    install.setSoftServiceUninstallCommand(softService.getSoftServiceUninstallCommand());
                    install.setSoftServiceUninstallCommandType(softService.getSoftServiceUninstallCommandType());
                    install.setSoftServiceStartCommand(softService.getSoftServiceStartCommand());
                    install.setSoftServiceStartCommandType(softService.getSoftServiceStartCommandType());
                    install.setSoftServiceStopCommand(softService.getSoftServiceStopCommand());
                    install.setSoftServiceStopCommandType(softService.getSoftServiceStopCommandType());
                    install.setSoftServiceRestartCommand(softService.getSoftServiceRestartCommand());
                    install.setSoftServiceRestartCommandType(softService.getSoftServiceRestartCommandType());
                    install.setSoftServiceStatusCheckCommand(softService.getSoftServiceStatusCheckCommand());
                    install.setSoftServiceStatusCheckCommandType(softService.getSoftServiceStatusCheckCommandType());
                    install.setSoftServiceStatusCheckSuccessFlag(softService.getSoftServiceStatusCheckSuccessFlag());

                    // 新增：添加命令成功和失败标识
                    install.setSoftServiceStatusCheckFailureFlag(softService.getSoftServiceStatusCheckFailureFlag());
                    install.setSoftServiceInstallSuccessFlag(softService.getSoftServiceInstallSuccessFlag());
                    install.setSoftServiceInstallFailureFlag(softService.getSoftServiceInstallFailureFlag());
                    install.setSoftServiceUninstallSuccessFlag(softService.getSoftServiceUninstallSuccessFlag());
                    install.setSoftServiceUninstallFailureFlag(softService.getSoftServiceUninstallFailureFlag());
                    install.setSoftServiceStartSuccessFlag(softService.getSoftServiceStartSuccessFlag());
                    install.setSoftServiceStartFailureFlag(softService.getSoftServiceStartFailureFlag());
                    install.setSoftServiceStopSuccessFlag(softService.getSoftServiceStopSuccessFlag());
                    install.setSoftServiceStopFailureFlag(softService.getSoftServiceStopFailureFlag());
                    install.setSoftServiceRestartSuccessFlag(softService.getSoftServiceRestartSuccessFlag());
                    install.setSoftServiceRestartFailureFlag(softService.getSoftServiceRestartFailureFlag());
                }
            }
        } catch (Exception e) {
            log.error("填充SSH信息失败: {}", e.getMessage(), e);
        }

        return installList;
    }

    @Override
    public SoftServiceInstall getInstallBySshIdAndSoftServiceId(String sshId, Integer softServiceId) {
        LambdaQueryWrapper<SoftServiceInstall> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoftServiceInstall::getSshId, sshId)
                .eq(SoftServiceInstall::getSoftServiceId, softServiceId);
        return this.getOne(queryWrapper);
    }

    @Override
    public ReturnResult<Boolean> startInstall(SoftServiceInstall softServiceInstall) {
        // 检查软件服务是否存在
        SoftService softService = softServiceService.getById(softServiceInstall.getSoftServiceId());
        if (softService == null) {
            sendInstallProgress(softServiceInstall, "软件服务不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("软件服务不存在");
        }

        // 检查安装命令是否存在
        if (softService.getSoftServiceInstallCommand() == null || softService.getSoftServiceInstallCommand().isEmpty()) {
            sendInstallProgress(softServiceInstall, "软件服务安装命令不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("软件服务安装命令不存在");
        }

        // 检查是否已经存在安装记录
        SoftServiceInstall existInstall = this.getInstallBySshIdAndSoftServiceId(
                softServiceInstall.getSshId(), softServiceInstall.getSoftServiceId());

        if (existInstall != null && existInstall.getInstallStatus() == 2) {
            sendInstallProgress(existInstall, "该软件已安装成功", 100, 100, "SUCCESS");
            return ReturnResult.success();
        }

        // 如果存在安装记录且正在安装中，则返回错误
        if (existInstall != null && existInstall.getInstallStatus() == 1) {
            sendInstallProgress(existInstall, "该软件已在安装中，请稍后再试", 100, 100, "FAILURE");
            return ReturnResult.illegal("该软件已在安装中，请稍后再试");
        }

        // 增加软件服务的安装次数
        softServiceService.incrementInstallCount(softServiceInstall.getSoftServiceId());

        // 如果不存在安装记录，则创建新的安装记录
        if (existInstall == null) {
            // 将软件服务的命令数据缓存到安装记录中
            softServiceInstall.setSoftServiceName(softService.getSoftServiceName());
            softServiceInstall.setSoftServiceInstallCommand(softService.getSoftServiceInstallCommand());
            softServiceInstall.setSoftServiceInstallCommandType(softService.getSoftServiceInstallCommandType());
            softServiceInstall.setSoftServiceUninstallCommand(softService.getSoftServiceUninstallCommand());
            softServiceInstall.setSoftServiceUninstallCommandType(softService.getSoftServiceUninstallCommandType());
            softServiceInstall.setSoftServiceStartCommand(softService.getSoftServiceStartCommand());
            softServiceInstall.setSoftServiceStartCommandType(softService.getSoftServiceStartCommandType());
            softServiceInstall.setSoftServiceStopCommand(softService.getSoftServiceStopCommand());
            softServiceInstall.setSoftServiceStopCommandType(softService.getSoftServiceStopCommandType());
            softServiceInstall.setSoftServiceRestartCommand(softService.getSoftServiceRestartCommand());
            softServiceInstall.setSoftServiceRestartCommandType(softService.getSoftServiceRestartCommandType());
            softServiceInstall.setSoftServiceStatusCheckCommand(softService.getSoftServiceStatusCheckCommand());
            softServiceInstall.setSoftServiceStatusCheckCommandType(softService.getSoftServiceStatusCheckCommandType());
            softServiceInstall.setSoftServiceStatusCheckSuccessFlag(softService.getSoftServiceStatusCheckSuccessFlag());

            // 添加命令成功和失败标识
            softServiceInstall.setSoftServiceStatusCheckFailureFlag(softService.getSoftServiceStatusCheckFailureFlag());
            softServiceInstall.setSoftServiceInstallSuccessFlag(softService.getSoftServiceInstallSuccessFlag());
            softServiceInstall.setSoftServiceInstallFailureFlag(softService.getSoftServiceInstallFailureFlag());
            softServiceInstall.setSoftServiceUninstallSuccessFlag(softService.getSoftServiceUninstallSuccessFlag());
            softServiceInstall.setSoftServiceUninstallFailureFlag(softService.getSoftServiceUninstallFailureFlag());
            softServiceInstall.setSoftServiceStartSuccessFlag(softService.getSoftServiceStartSuccessFlag());
            softServiceInstall.setSoftServiceStartFailureFlag(softService.getSoftServiceStartFailureFlag());
            softServiceInstall.setSoftServiceStopSuccessFlag(softService.getSoftServiceStopSuccessFlag());
            softServiceInstall.setSoftServiceStopFailureFlag(softService.getSoftServiceStopFailureFlag());
            softServiceInstall.setSoftServiceRestartSuccessFlag(softService.getSoftServiceRestartSuccessFlag());
            softServiceInstall.setSoftServiceRestartFailureFlag(softService.getSoftServiceRestartFailureFlag());

            // 保存自定义安装路径和端口
            if (StringUtils.hasText(softService.getSoftServiceInstallPath())) {
                softServiceInstall.setInstallPathCustom(softService.getSoftServiceInstallPath());
            }
            if (StringUtils.hasText(softService.getSoftServiceInstallPort())) {
                softServiceInstall.setInstallPort(softService.getSoftServiceInstallPort());
            }
            if (StringUtils.hasText(softService.getSoftServiceDefaultPort())) {
                softServiceInstall.setInstallDefaultPort(softService.getSoftServiceDefaultPort());
            }
            
            softServiceInstall.setInstallStatus(1); // 安装中
            softServiceInstall.setInstallTime(System.currentTimeMillis());
            softServiceInstall.setInstallVersion(softService.getSoftServiceVersion());
            this.save(softServiceInstall);
            existInstall = softServiceInstall;
        } else {
            // 更新安装状态
            existInstall.setInstallStatus(1); // 安装中
            existInstall.setInstallTime(System.currentTimeMillis());
            existInstall.setInstallVersion(softService.getSoftServiceVersion());

            // 更新软件服务的命令数据缓存
            existInstall.setSoftServiceName(softService.getSoftServiceName());
            existInstall.setSoftServiceInstallCommand(softService.getSoftServiceInstallCommand());
            existInstall.setSoftServiceInstallCommandType(softService.getSoftServiceInstallCommandType());
            existInstall.setSoftServiceUninstallCommand(softService.getSoftServiceUninstallCommand());
            existInstall.setSoftServiceUninstallCommandType(softService.getSoftServiceUninstallCommandType());
            existInstall.setSoftServiceStartCommand(softService.getSoftServiceStartCommand());
            existInstall.setSoftServiceStartCommandType(softService.getSoftServiceStartCommandType());
            existInstall.setSoftServiceStopCommand(softService.getSoftServiceStopCommand());
            existInstall.setSoftServiceStopCommandType(softService.getSoftServiceStopCommandType());
            existInstall.setSoftServiceRestartCommand(softService.getSoftServiceRestartCommand());
            existInstall.setSoftServiceRestartCommandType(softService.getSoftServiceRestartCommandType());
            existInstall.setSoftServiceStatusCheckCommand(softService.getSoftServiceStatusCheckCommand());
            existInstall.setSoftServiceStatusCheckCommandType(softService.getSoftServiceStatusCheckCommandType());
            existInstall.setSoftServiceStatusCheckSuccessFlag(softService.getSoftServiceStatusCheckSuccessFlag());

            // 添加命令成功和失败标识
            existInstall.setSoftServiceStatusCheckFailureFlag(softService.getSoftServiceStatusCheckFailureFlag());
            existInstall.setSoftServiceInstallSuccessFlag(softService.getSoftServiceInstallSuccessFlag());
            existInstall.setSoftServiceInstallFailureFlag(softService.getSoftServiceInstallFailureFlag());
            existInstall.setSoftServiceUninstallSuccessFlag(softService.getSoftServiceUninstallSuccessFlag());
            existInstall.setSoftServiceUninstallFailureFlag(softService.getSoftServiceUninstallFailureFlag());
            existInstall.setSoftServiceStartSuccessFlag(softService.getSoftServiceStartSuccessFlag());
            existInstall.setSoftServiceStartFailureFlag(softService.getSoftServiceStartFailureFlag());
            existInstall.setSoftServiceStopSuccessFlag(softService.getSoftServiceStopSuccessFlag());
            existInstall.setSoftServiceStopFailureFlag(softService.getSoftServiceStopFailureFlag());
            existInstall.setSoftServiceRestartSuccessFlag(softService.getSoftServiceRestartSuccessFlag());
            existInstall.setSoftServiceRestartFailureFlag(softService.getSoftServiceRestartFailureFlag());

            // 保存自定义安装路径和端口
            if (StringUtils.hasText(softService.getSoftServiceInstallPath())) {
                existInstall.setInstallPathCustom(softService.getSoftServiceInstallPath());
            }
            if (StringUtils.hasText(softService.getSoftServiceInstallPort())) {
                existInstall.setInstallPort(softService.getSoftServiceInstallPort());
            }
            if (StringUtils.hasText(softService.getSoftServiceDefaultPort())) {
                existInstall.setInstallDefaultPort(softService.getSoftServiceDefaultPort());
            }
            
            this.updateById(existInstall);
        }

        // 推送安装开始通知
        sendInstallProgress(existInstall, "开始安装 " + softService.getSoftServiceName(), 0, 100, "INFO");

        // 记录安装开始日志
        saveOperationLog(existInstall, "install", "开始安装 " + softService.getSoftServiceName(), 0);

        // 异步执行安装
        final SoftServiceInstall finalExistInstall = existInstall;
        CompletableFuture.runAsync(() -> {
            executeCommandWithHandling(
                    finalExistInstall,
                    finalExistInstall.getSoftServiceInstallCommandType(),
                    finalExistInstall.getSoftServiceInstallCommand(),
                    finalExistInstall.getSoftServiceInstallSuccessFlag(),
                    finalExistInstall.getSoftServiceInstallFailureFlag(),
                    "安装",
                    5000, // 5s超时
                    result -> {
                        if (result.isFailure()) {
                            // 匹配到失败标识
                            updateInstallStatus(finalExistInstall.getInstallId(), 3, result.getLogBuilder().toString() + "\n安装失败: 匹配到失败标识");
                            sendInstallProgress(finalExistInstall, "安装失败：匹配到失败标识", 100, 100, "ERROR");
                        } else {
                            // 匹配到成功标识或无法判断，默认为成功
                            SoftServiceInstall updatedInstall = updateInstallStatus(finalExistInstall.getInstallId(), 2, result.getLogBuilder().toString());

                            if (result.isSuccess()) {
                                sendInstallProgress(updatedInstall, "安装成功：匹配到成功标识", 100, 100, "SUCCESS");
                            } else {
                                sendInstallProgress(updatedInstall, "安装完成：" + finalExistInstall.getSoftServiceName(), 100, 100, "SUCCESS");
                            }
                        }
                    }
            );
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> stopInstall(Integer installId) {
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查是否已安装
        Integer installStatus = install.getInstallStatus();
        if (installStatus == 3 || installStatus == 0) {
            return ReturnResult.illegal("软件未安装或安装失败，无需卸载");
        }

        // 检查卸载命令是否存在
        if (install.getSoftServiceUninstallCommand() == null || install.getSoftServiceUninstallCommand().isEmpty()) {
            return ReturnResult.illegal("卸载命令不存在");
        }

        // 发送卸载开始通知
        sendInstallProgress(install, "准备卸载软件：" + install.getSoftServiceName(), 0, 100, "INFO");

        // 记录卸载开始日志
        saveOperationLog(install, "uninstall", "准备卸载软件：" + install.getSoftServiceName(), 0);

        // 异步执行卸载
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(install, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(install, "SSH连接成功，准备执行卸载命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_uninstall", "shell");

                // 设置监听器接收输出
                StringBuilder logBuilder = new StringBuilder();
                final int[] progressCounter = {0};
                final boolean[] foundSuccess = {false};
                final boolean[] foundFailure = {false};
                channel.setListener(s -> {
                    logBuilder.append(s).append("\n");

                    // 检查输出是否包含成功或失败标识
                    if (StringUtils.hasText(install.getSoftServiceUninstallFailureFlag())) {
                        String[] failureFlags = install.getSoftServiceUninstallFailureFlag().split(",");
                        for (String flag : failureFlags) {
                            flag = flag.trim();
                            if (StringUtils.hasText(flag)) {
                                // 支持通配符 * 匹配
                                if (flag.contains("*")) {
                                    String pattern = flag.replace("*", ".*");
                                    if (s.matches("(?s).*" + pattern + ".*")) {
                                        log.info("卸载失败，匹配到失败标识: {}", flag);
                                        foundFailure[0] = true;
                                    }
                                } else if (s.contains(flag)) {
                                    log.info("卸载失败，匹配到失败标识: {}", flag);
                                    foundFailure[0] = true;
                                }
                            }
                        }
                    }

                    // 检查是否包含成功标识（只有在没有匹配到失败标识的情况下才检查）
                    if (!foundFailure[0] && StringUtils.hasText(install.getSoftServiceUninstallSuccessFlag())) {
                        String[] successFlags = install.getSoftServiceUninstallSuccessFlag().split(",");
                        for (String flag : successFlags) {
                            flag = flag.trim();
                            if (StringUtils.hasText(flag)) {
                                // 支持通配符 * 匹配
                                if (flag.contains("*")) {
                                    String pattern = flag.replace("*", ".*");
                                    if (s.matches("(?s).*" + pattern + ".*")) {
                                        log.info("卸载成功，匹配到成功标识: {}", flag);
                                        foundSuccess[0] = true;
                                    }
                                } else if (s.contains(flag)) {
                                    log.info("卸载成功，匹配到成功标识: {}", flag);
                                    foundSuccess[0] = true;
                                }
                            }
                        }
                    }

                    // 计算进度（简单模拟，每收到一条消息增加一点进度，最多到95%）
                    progressCounter[0] += 5;
                    int progress = Math.min(progressCounter[0], 95);

                    // 推送卸载进度
                    sendInstallProgress(install, s, progress, 100, "INFO");
                });

                try {
                    // 检查命令类型并执行卸载命令
                    boolean isScript = install.getSoftServiceUninstallCommandType() != null &&
                            install.getSoftServiceUninstallCommandType() == 1;

                    if (isScript) {
                        sendInstallProgress(install, "检测到脚本卸载方式，准备执行脚本", 15, 100, "INFO");
                    } else {
                        sendInstallProgress(install, "检测到命令行卸载方式，准备执行命令", 15, 100, "INFO");
                    }

                    // 执行卸载命令
                    String uninstallCommand = install.getSoftServiceUninstallCommand();
                    sendInstallProgress(install, "执行卸载命令：" + (uninstallCommand.length() > 50 ? uninstallCommand.substring(0, 50) + "..." : uninstallCommand), 20, 100, "INFO");
                    executeCommands(channel, uninstallCommand, 10000, install);

                    // 等待一段时间，确保所有输出都被处理
                    Thread.sleep(2000);

                    if (foundFailure[0]) {
                        // 匹配到失败标识
                        updateInstallStatus(installId, 3, logBuilder + "\n卸载失败: 匹配到失败标识");
                        sendInstallProgress(install, "卸载失败：匹配到失败标识", 100, 100, "ERROR");
                    } else {
                        // 匹配到成功标识或无法判断，默认为成功
                        // 更新安装状态为未安装
                        updateInstallStatus(installId, 0, logBuilder.toString());
                        sendInstallProgress(install, "更新安装状态为未安装", 98, 100, "INFO");

                        // 推送卸载完成通知
                        if (foundSuccess[0]) {
                            sendInstallProgress(install, "卸载成功：匹配到成功标识", 100, 100, "SUCCESS");
                        } else {
                            sendInstallProgress(install, "卸载完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");
                        }
                    }
                } catch (Exception e) {
                    log.error("卸载软件失败: {}", e.getMessage(), e);
                    // 更新安装状态为失败
                    updateInstallStatus(installId, 3, logBuilder + "\n卸载失败: " + e.getMessage());
                    // 推送卸载失败通知
                    sendInstallProgress(install, "卸载失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("卸载软件失败: {}", e.getMessage(), e);
                // 更新安装状态为失败
                updateInstallStatus(installId, 3, "卸载失败: " + e.getMessage());
                // 推送卸载失败通知
                sendInstallProgress(install, "卸载失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> startService(Integer installId) {
        // 获取安装记录
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查是否已安装成功，只有安装成功的服务才能启动
        if (install.getInstallStatus() != 2) {
            return ReturnResult.illegal("软件未安装成功，无法启动");
        }

        // 检查是否已经在运行，如果已经在运行则不需要再启动
        if (install.getInstallRunStatus() != null && install.getInstallRunStatus() == 1) {
            return ReturnResult.illegal("服务已在运行中");
        }

        // 检查启动命令是否存在
        if (install.getSoftServiceStartCommand() == null || install.getSoftServiceStartCommand().isEmpty()) {
            return ReturnResult.illegal("启动命令不存在");
        }

        // 发送启动开始通知
        sendInstallProgress(install, "准备启动服务：" + install.getSoftServiceName(), 0, 100, "INFO");

        // 记录启动开始日志
        saveOperationLog(install, "start", "准备启动服务：" + install.getSoftServiceName(), 0);

        // 异步执行启动
        CompletableFuture.runAsync(() -> {
            executeCommandWithHandling(
                    install,
                    install.getSoftServiceStartCommandType(),
                    install.getSoftServiceStartCommand(),
                    install.getSoftServiceStartSuccessFlag(),
                    install.getSoftServiceStartFailureFlag(),
                    "启动",
                    60000, // 1分钟超时
                    result -> {
                        if (result.isFailure()) {
                            // 匹配到失败标识
                            SoftServiceInstall updateInstall = new SoftServiceInstall();
                            updateInstall.setInstallId(installId);
                            updateInstall.setInstallRunStatus(0);
                            this.updateById(updateInstall);
                            sendInstallProgress(install, "启动失败：匹配到失败标识", 100, 100, "ERROR");
                    } else {
                            // 匹配到成功标识或无法判断，默认为成功
                            // 更新运行状态为运行中
                            SoftServiceInstall updateInstall = new SoftServiceInstall();
                            updateInstall.setInstallId(installId);
                            updateInstall.setInstallRunStatus(1);
                            updateInstall.setInstallLastStartTime(System.currentTimeMillis());
                            this.updateById(updateInstall);
                            sendInstallProgress(install, "更新服务状态为运行中", 98, 100, "INFO");

                            // 推送启动完成通知
                            if (result.isSuccess()) {
                                sendInstallProgress(install, "服务启动成功：匹配到成功标识", 100, 100, "SUCCESS");
                            } else {
                                sendInstallProgress(install, "服务启动完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");
                            }
                        }
                    }
            );
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> stopService(Integer installId) {
        // 获取安装记录
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查是否已安装成功，只有安装成功的服务才能停止
        if (install.getInstallStatus() != 2) {
            return ReturnResult.illegal("软件未安装成功，无法停止");
        }

        // 检查是否已经停止，如果已经停止则不需要再停止
        if (install.getInstallRunStatus() == null || install.getInstallRunStatus() == 0) {
            return ReturnResult.illegal("服务未运行");
        }

        // 检查停止命令是否存在
        if (install.getSoftServiceStopCommand() == null || install.getSoftServiceStopCommand().isEmpty()) {
            return ReturnResult.illegal("停止命令不存在");
        }

        // 发送停止开始通知
        sendInstallProgress(install, "准备停止服务：" + install.getSoftServiceName(), 0, 100, "INFO");

        // 记录停止开始日志
        saveOperationLog(install, "stop", "准备停止服务：" + install.getSoftServiceName(), 0);

        // 异步执行停止
        CompletableFuture.runAsync(() -> {
            executeCommandWithHandling(
                    install,
                    install.getSoftServiceStopCommandType(),
                    install.getSoftServiceStopCommand(),
                    install.getSoftServiceStopSuccessFlag(),
                    install.getSoftServiceStopFailureFlag(),
                    "停止",
                    60000, // 1分钟超时
                    result -> {
                        if (result.isFailure()) {
                            // 匹配到失败标识
                            sendInstallProgress(install, "停止失败：匹配到失败标识", 100, 100, "ERROR");
                    } else {
                            // 匹配到成功标识或无法判断，默认为成功
                            // 更新运行状态为未运行
                            SoftServiceInstall updateInstall = new SoftServiceInstall();
                            updateInstall.setInstallId(installId);
                            updateInstall.setInstallRunStatus(0);
                            updateInstall.setInstallLastStopTime(System.currentTimeMillis());
                            this.updateById(updateInstall);
                            sendInstallProgress(install, "更新服务状态为未运行", 98, 100, "INFO");

                            // 推送停止完成通知
                            if (result.isSuccess()) {
                                sendInstallProgress(install, "服务停止成功：匹配到成功标识", 100, 100, "SUCCESS");
                            } else {
                                sendInstallProgress(install, "服务停止完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");
                            }
                        }

                    // 延迟检查服务状态
                        try {
                            Thread.sleep(2000);
                            checkServiceStatus(installId);
                        } catch (Exception e) {
                            log.error("检查服务状态失败: {}", e.getMessage(), e);
                        }
                    }
            );
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> restartService(Integer installId) {
        // 获取安装记录
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查是否已安装
        if (install.getInstallStatus() != 2) {
            return ReturnResult.illegal("软件未安装或安装失败，无法重启");
        }

        // 发送重启开始通知
        sendInstallProgress(install, "准备重启服务：" + install.getSoftServiceName(), 0, 100, "INFO");

        // 记录重启开始日志
        saveOperationLog(install, "restart", "准备重启服务：" + install.getSoftServiceName(), 0);

        // 如果有直接的重启命令，优先使用重启命令
        if (install.getSoftServiceRestartCommand() != null && !install.getSoftServiceRestartCommand().isEmpty()) {
            // 异步执行重启
            CompletableFuture.runAsync(() -> {
                executeCommandWithHandling(
                        install,
                        install.getSoftServiceRestartCommandType(),
                        install.getSoftServiceRestartCommand(),
                        install.getSoftServiceRestartSuccessFlag(),
                        install.getSoftServiceRestartFailureFlag(),
                        "重启",
                        120000, // 2分钟超时
                        result -> {
                            if (result.isFailure()) {
                                // 匹配到失败标识
                                SoftServiceInstall updateInstall = new SoftServiceInstall();
                                updateInstall.setInstallId(installId);
                                updateInstall.setInstallRunStatus(0);
                                this.updateById(updateInstall);
                                sendInstallProgress(install, "重启失败：匹配到失败标识", 100, 100, "ERROR");
                        } else {
                                // 匹配到成功标识或无法判断，默认为成功
                                // 更新运行状态为运行中
                                SoftServiceInstall updateInstall = new SoftServiceInstall();
                                updateInstall.setInstallId(installId);
                                updateInstall.setInstallRunStatus(1);
                                updateInstall.setInstallLastStopTime(System.currentTimeMillis());
                                updateInstall.setInstallLastStartTime(System.currentTimeMillis());
                                this.updateById(updateInstall);
                                sendInstallProgress(install, "更新服务状态为运行中", 98, 100, "INFO");

                                // 推送重启完成通知
                                if (result.isSuccess()) {
                                    sendInstallProgress(install, "服务重启成功：匹配到成功标识", 100, 100, "SUCCESS");
                                } else {
                                    sendInstallProgress(install, "服务重启完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");
                                }
                            }

                        // 延迟检查服务状态
                            try {
                                Thread.sleep(2000);
                                checkServiceStatus(installId);
                            } catch (Exception e) {
                                log.error("检查服务状态失败: {}", e.getMessage(), e);
                            }
                        }
                );
            });

            return ReturnResult.success(true);
        } else {
            // 如果没有直接的重启命令，则先停止再启动
            sendInstallProgress(install, "未配置重启命令，将执行停止后启动操作", 5, 100, "INFO");
            return stopThenStart(installId);
        }
    }

    /**
     * 先停止再启动服务
     *
     * @param installId 安装记录ID
     * @return 操作结果
     */
    private ReturnResult<Boolean> stopThenStart(Integer installId) {
        // 获取安装记录
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查停止和启动命令是否存在
        if (install.getSoftServiceStopCommand() == null || install.getSoftServiceStopCommand().isEmpty()) {
            return ReturnResult.illegal("停止命令不存在");
        }
        if (install.getSoftServiceStartCommand() == null || install.getSoftServiceStartCommand().isEmpty()) {
            return ReturnResult.illegal("启动命令不存在");
        }

        // 异步执行先停止再启动
        CompletableFuture.runAsync(() -> {
            // 1. 先执行停止命令
            executeCommandWithHandling(
                    install,
                    install.getSoftServiceStopCommandType(),
                    install.getSoftServiceStopCommand(),
                    install.getSoftServiceStopSuccessFlag(),
                    install.getSoftServiceStopFailureFlag(),
                    "停止",
                    60000, // 1分钟超时
                    stopResult -> {
                        if (stopResult.isFailure()) {
                            // 停止失败，不继续执行启动
                            sendInstallProgress(install, "停止失败：匹配到失败标识，中止重启过程", 100, 100, "ERROR");
                            return;
                    }

                        // 更新运行状态为未运行
                    SoftServiceInstall updateStopInstall = new SoftServiceInstall();
                    updateStopInstall.setInstallId(installId);
                    updateStopInstall.setInstallRunStatus(0);
                    updateStopInstall.setInstallLastStopTime(System.currentTimeMillis());
                    this.updateById(updateStopInstall);

                    // 推送停止完成通知
                        if (stopResult.isSuccess()) {
                            sendInstallProgress(install, "服务停止成功：匹配到成功标识", 50, 100, "SUCCESS");
                        } else {
                            sendInstallProgress(install, "服务停止完成：" + install.getSoftServiceName(), 50, 100, "SUCCESS");
                        }

                        // 推送停止完成通知
                        sendInstallProgress(install, "服务停止完成，准备启动服务", 50, 100, "INFO");

                    // 等待一段时间再启动
                        try {
                            sendInstallProgress(install, "等待3秒后开始启动服务", 55, 100, "INFO");
                            Thread.sleep(3000);

                            // 2. 再执行启动命令
                            executeCommandWithHandling(
                                    install,
                                    install.getSoftServiceStartCommandType(),
                                    install.getSoftServiceStartCommand(),
                                    install.getSoftServiceStartSuccessFlag(),
                                    install.getSoftServiceStartFailureFlag(),
                                    "启动",
                                    60000, // 1分钟超时
                                    startResult -> {
                                        if (startResult.isFailure()) {
                                            // 匹配到失败标识
                                            SoftServiceInstall updateStartInstall = new SoftServiceInstall();
                                            updateStartInstall.setInstallId(installId);
                                            updateStartInstall.setInstallRunStatus(0);
                                            this.updateById(updateStartInstall);
                                            sendInstallProgress(install, "启动失败：匹配到失败标识", 100, 100, "ERROR");
                                        } else {
                                            // 匹配到成功标识或无法判断，默认为成功
                                            // 更新运行状态为运行中
                                            SoftServiceInstall updateStartInstall = new SoftServiceInstall();
                                            updateStartInstall.setInstallId(installId);
                                            updateStartInstall.setInstallRunStatus(1);
                                            updateStartInstall.setInstallLastStartTime(System.currentTimeMillis());
                                            this.updateById(updateStartInstall);
                                            sendInstallProgress(install, "更新服务状态为运行中", 98, 100, "INFO");

                                            // 推送启动完成通知
                                            if (startResult.isSuccess()) {
                                                sendInstallProgress(install, "服务启动成功：匹配到成功标识", 100, 100, "SUCCESS");
                                            } else {
                                                sendInstallProgress(install, "服务启动完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");
                                            }
                                        }

                                        // 延迟检查服务状态
                                        try {
                                            Thread.sleep(2000);
                                            checkServiceStatus(installId);
                                        } catch (Exception e) {
                                            log.error("检查服务状态失败: {}", e.getMessage(), e);
                                        }
                                    }
                            );
                        } catch (Exception e) {
                            log.error("等待启动过程中发生错误: {}", e.getMessage(), e);
                            sendInstallProgress(install, "重启过程中发生错误: " + e.getMessage(), 100, 100, "ERROR");
                        }
                    }
            );
        });

        return ReturnResult.success(true);
    }

    /**
     * 更新安装状态
     *
     * @param installId 安装记录ID
     * @param status 状态：0-未安装，1-安装中，2-安装成功，3-安装失败
     * @param log 日志
     * @return 更新后的安装记录
     */
    private SoftServiceInstall updateInstallStatus(Integer installId, Integer status, String log) {
        SoftServiceInstall updateInstall = new SoftServiceInstall();
        updateInstall.setInstallId(installId);
        updateInstall.setInstallStatus(status);
//        updateInstall.setInstallLog(log);
        this.updateById(updateInstall);

        // 记录日志到日志文件
        if (log != null && !log.isEmpty()) {
            // 获取完整安装记录
            SoftServiceInstall fullInstall = this.getById(installId);

            // 确定操作类型
            String operationType = "install";
            if (fullInstall != null) {
                saveOperationLog(fullInstall, operationType, log, status == 2 ? 1 : (status == 3 ? 2 : 0));
            }
        }

        // 清除缓存，确保下次获取是最新数据
        clearInstallCache(installId);

        // 获取更新后的完整记录
        return this.getInstallById(installId);
    }

    /**
     * 保存操作日志
     *
     * @param install       安装记录
     * @param operationType 操作类型
     * @param content       日志内容
     * @param status        状态：0-进行中，1-成功，2-失败
     */
    private void saveOperationLog(SoftServiceInstall install, String operationType, String content, Integer status) {
        try {
            // 创建日志记录
            SoftServiceLog serviceLog = new SoftServiceLog();
            serviceLog.setSoftServiceLogInstallId(install.getInstallId());
            serviceLog.setSoftServiceLogServiceId(install.getSoftServiceId());
            serviceLog.setSoftServiceLogSshId(install.getSshId());
            serviceLog.setSoftServiceLogOperationType(operationType);
            serviceLog.setSoftServiceLogContent(content);
            serviceLog.setSoftServiceLogStatus(status);
            serviceLog.setSoftServiceLogTime(System.currentTimeMillis());

            // 设置日志类型
            switch (operationType) {
                case "install":
                    serviceLog.setSoftServiceLogType(1);
                    break;
                case "uninstall":
                    serviceLog.setSoftServiceLogType(2);
                    break;
                case "start":
                    serviceLog.setSoftServiceLogType(3);
                    break;
                case "stop":
                    serviceLog.setSoftServiceLogType(4);
                    break;
                case "restart":
                    serviceLog.setSoftServiceLogType(5);
                    break;
                case "status":
                    serviceLog.setSoftServiceLogType(6);
                    break;
                default:
                    serviceLog.setSoftServiceLogType(0);
            }

            // 保存日志
            softServiceLogService.saveLog(serviceLog);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 追加操作日志
     *
     * @param installId     安装记录ID
     * @param operationType 操作类型
     * @param content       日志内容
     */
    private void appendOperationLog(Integer installId, String operationType, String content) {
        try {
            // 获取安装记录
            SoftServiceInstall install = this.getById(installId);
            if (install == null) {
                log.warn("安装记录不存在，无法记录日志: installId={}", installId);
                return;
            }

            // 追加日志内容
            softServiceLogService.appendLogContent(installId, operationType, content);
        } catch (Exception e) {
            log.error("追加操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送安装进度消息
     *
     * @param installId 安装ID
     * @param message 消息内容
     * @param step 当前步骤
     * @param total 总步骤
     * @param type 消息类型：INFO, ERROR, SUCCESS, WARNING, FAILURE
     */
    private void sendInstallProgress(Integer installId, String message, int step, int total, String type) {
        if (socketSessionTemplate == null) {
            log.warn("socketSessionTemplate为空，无法发送安装进度通知");
            return;
        }

        try {
            // 获取安装记录，用于获取sshId
            SoftServiceInstall install = this.getById(installId);
            if (install == null) {
                log.warn("安装记录不存在，无法发送安装进度通知: installId={}", installId);
                return;
            }

            // 调用重载方法
            sendInstallProgress(install, message, step, total, type);
        } catch (Exception e) {
            log.error("发送安装进度通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送安装进度消息（使用SoftServiceInstall参数）
     *
     * @param install SoftServiceInstall对象
     * @param message 消息内容
     * @param step    当前步骤
     * @param total   总步骤
     * @param type    消息类型：INFO, ERROR, SUCCESS, WARNING, FAILURE
     */
    public void sendInstallProgress(SoftServiceInstall install, String message, int step, int total, String type) {
        if (socketSessionTemplate == null) {
            log.warn("socketSessionTemplate为空，无法发送安装进度通知");
            return;
        }

        try {
            Integer installId = install.getInstallId();
            String sshId = install.getSshId();
            
            // 创建消息事件
            MsgEvent msgEvent = MsgEvent.builder()
                    .name("install_progress")
                    .eventId(installId.toString())
                    .step(step)
                    .total(total)
                    .msg(message)
                    .type(type)
                    .module("soft-service-install")
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 统一发送到/topic/install/soft
            socketSessionTemplate.send("/topic/install/soft", msgEvent);

            // 记录日志
            log.info("发送安装进度通知: installId={}, sshId={}, message={}, step={}/{}, type={}",
                    installId, sshId, message, step, total, type);

            // 记录操作日志
            // 根据消息类型确定操作类型
            String operationType;
            if (message.contains("安装")) {
                operationType = "install";
            } else if (message.contains("卸载")) {
                operationType = "uninstall";
            } else if (message.contains("启动")) {
                operationType = "start";
            } else if (message.contains("停止")) {
                operationType = "stop";
            } else if (message.contains("重启")) {
                operationType = "restart";
            } else if (message.contains("状态")) {
                operationType = "status";
            } else {
                // 默认使用install
                operationType = "install";
            }

            // 确定日志状态
            Integer status;
            if ("ERROR".equals(type) || "FAILURE".equals(type)) {
                status = 2; // 失败
            } else if ("SUCCESS".equals(type)) {
                status = 1; // 成功
            } else {
                status = 0; // 进行中
            }

            // 追加日志内容
            appendOperationLog(installId, operationType, message);

            // 如果是最终状态（成功或失败），更新日志状态
            if (step >= total) {
                // 创建完整日志对象
                SoftServiceLog serviceLog = new SoftServiceLog();
                serviceLog.setSoftServiceLogInstallId(installId);
                serviceLog.setSoftServiceLogServiceId(install.getSoftServiceId());
                serviceLog.setSoftServiceLogSshId(sshId);
                serviceLog.setSoftServiceLogOperationType(operationType);
                serviceLog.setSoftServiceLogStatus(status);
                serviceLog.setSoftServiceLogEndTime(System.currentTimeMillis());

                // 更新日志状态
                softServiceLogService.updateLog(serviceLog);
            }

        } catch (Exception e) {
            log.error("发送安装进度通知失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public ReturnResult<String> viewServiceLog(Integer installId, Integer lines) {
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查安装状态
        if (install.getInstallStatus() != 2) {
            return ReturnResult.illegal("该软件未安装成功，无法查看日志");
        }

        // 获取日志目录
        String logPath = install.getInstallLogPath();
        if (logPath == null || logPath.isEmpty()) {
            return ReturnResult.illegal("日志路径未配置");
        }

        try {
            // 获取SSH客户端
            SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
            if (sshClient == null) {
                return ReturnResult.illegal("无法连接到SSH服务器");
            }

            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            Channel channel = clientSession.openChannel(installId + "_viewlog", "cmd");

            // 设置监听器接收输出
            StringBuilder logBuilder = new StringBuilder();
            channel.setListener(logBuilder::append);

            // 执行查看日志命令
            String command = "tail -n " + lines + " " + logPath;
            channel.execute(command, 10000); // 30秒超时

            return ReturnResult.success(logBuilder.toString());
        } catch (Exception e) {
            log.error("查看服务日志失败: {}", e.getMessage(), e);
            return ReturnResult.illegal("查看服务日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> startMonitorServiceLog(Integer installId) {
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查安装状态
        if (install.getInstallStatus() != 2) {
            return ReturnResult.illegal("该软件未安装成功，无法监控日志");
        }

        // 获取服务日志路径
        String logPath = install.getServiceLogPath();
        if (logPath == null || logPath.isEmpty()) {
            // 尝试从软件服务配置获取日志路径
            try {
                SoftService softService = softServiceService.getById(install.getSoftServiceId());
                if (softService != null && StringUtils.hasText(softService.getSoftServiceLogPath())) {
                    logPath = softService.getSoftServiceLogPath();
                    // 更新安装记录中的服务日志路径
                    install.setServiceLogPath(logPath);
                    this.updateById(install);
                } else {
                    return ReturnResult.illegal("服务日志路径未配置");
                }
            } catch (Exception e) {
                log.error("获取软件服务日志路径失败: {}", e.getMessage(), e);
                return ReturnResult.illegal("获取软件服务日志路径失败: " + e.getMessage());
            }
        }

        // 推送监控开始通知
        sendInstallProgress(install, "开始监控服务日志: " + logPath, 0, 100, "INFO");

        // 异步执行监控
        String finalLogPath = logPath;
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(install, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_monitorlog", "shell");

                // 设置监听器接收输出
                channel.setListener(s -> {
                    // 推送日志内容
                    sendServiceLog(installId, s);
                });

                // 执行监控日志命令
                String command = "tail -f " + finalLogPath;
                sendInstallProgress(install, "执行命令: " + command, 50, 100, "INFO");
                channel.execute(command, 0); // 无超时，持续执行
            } catch (Exception e) {
                log.error("监控服务日志失败: {}", e.getMessage(), e);
                sendInstallProgress(install, "监控服务日志失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> stopMonitorServiceLog(Integer installId) {
        try {
            // 获取安装记录
            SoftServiceInstall install = this.getById(installId);
            if (install == null) {
                return ReturnResult.illegal("安装记录不存在");
            }

            // 获取SSH客户端
            SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
            if (sshClient == null) {
                return ReturnResult.illegal("无法连接到SSH服务器");
            }

            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            // 关闭通道
            clientSession.closeChannel(installId + "_monitorlog");

            // 推送监控停止通知
            sendInstallProgress(installId, "已停止监控日志", 100, 100, "INFO");

            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("停止监控服务日志失败: {}", e.getMessage(), e);
            return ReturnResult.illegal("停止监控服务日志失败: " + e.getMessage());
        }
    }

    /**
     * 检测服务状态
     *
     * @param installId 安装记录ID
     * @return 操作结果，包含服务状态信息
     */
    @Override
    public ReturnResult<SoftServiceStatusInfo> checkServiceStatus(Integer installId) {
        // 获取安装记录
        SoftServiceInstall softServiceInstall = this.getById(installId);
        if (softServiceInstall == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        try {
            // 获取SSH客户端
            SshClient sshClient = terminalService.createClientBySshId(softServiceInstall.getSshId());
            if (sshClient == null) {
                return ReturnResult.illegal("无法连接到SSH服务器");
            }

            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            Channel channel = clientSession.openChannel(installId.toString() + "_status", "shell");

            SoftServiceStatusInfo.SoftServiceStatusInfoBuilder statusBuilder = SoftServiceStatusInfo.builder()
                    .installId(installId)
                    .softServiceId(softServiceInstall.getSoftServiceId())
                    .softServiceName(softServiceInstall.getSoftServiceName())
                    .installStatus(softServiceInstall.getInstallStatus())
                    .runStatus(softServiceInstall.getInstallRunStatus())
                    .installPath(softServiceInstall.getInstallPath())
                    .checkTime(System.currentTimeMillis());

            // 设置安装状态描述
            switch (softServiceInstall.getInstallStatus()) {
                case 0:
                    statusBuilder.installStatusDesc("未安装");
                    break;
                case 1:
                    statusBuilder.installStatusDesc("安装中");
                    break;
                case 2:
                    statusBuilder.installStatusDesc("已安装");
                    break;
                case 3:
                    statusBuilder.installStatusDesc("安装失败");
                    break;
                default:
                    statusBuilder.installStatusDesc("未知状态");
            }

            // 如果已经安装成功，则检测服务是否存在和运行状态
            if (softServiceInstall.getInstallStatus() == 2) {
                // 1. 检查服务是否存在
                if (softServiceInstall.getInstallPath() != null && !softServiceInstall.getInstallPath().isEmpty()) {
                    try {
                        String checkDirCmd = "ls -la " + softServiceInstall.getInstallPath() + " 2>/dev/null || echo 'NOT_EXISTS'";
                        String dirResult = channel.execute(checkDirCmd, 10000);
                        boolean exists = !dirResult.contains("NOT_EXISTS");
                        statusBuilder.exists(exists);

                        if (!exists) {
                            statusBuilder.message("安装目录不存在，可能已被删除");
                            statusBuilder.runStatus(0);
                            statusBuilder.runStatusDesc("未运行");

                            // 推送状态信息
                            SoftServiceStatusInfo statusInfo = statusBuilder.build();
                            sendServiceStatus(installId, statusInfo);

                            return ReturnResult.success(statusInfo);
                        }
                    } catch (Exception e) {
                        log.error("检查安装目录是否存在失败: {}", e.getMessage(), e);
                        statusBuilder.exists(false);
                        statusBuilder.message("检查安装目录是否存在失败: " + e.getMessage());
                    }
                }

                // 2. 检查服务运行状态
                try {
                    // 如果有配置状态检查命令，则直接使用命令判断服务状态
                    if (StringUtils.hasText(softServiceInstall.getSoftServiceStatusCheckCommand())) {
                        try {
                            String checkCmd = softServiceInstall.getSoftServiceStatusCheckCommand();
                            final boolean[] foundSuccess = {false};
                            final boolean[] foundFailure = {false};
                            StringBuilder statusOutputBuilder = new StringBuilder();

                            // 创建新的通道，避免之前的输出影响判断
                            Channel statusChannel = clientSession.openChannel(installId + "_status_check", "shell");
                            statusChannel.setListener(s -> {
                                statusOutputBuilder.append(s).append("\n");

                                // 检查输出是否包含成功或失败标识
                                if (StringUtils.hasText(softServiceInstall.getSoftServiceStatusCheckFailureFlag())) {
                                    String[] failureFlags = softServiceInstall.getSoftServiceStatusCheckFailureFlag().split(",");
                                    for (String flag : failureFlags) {
                                        flag = flag.trim();
                                        if (StringUtils.hasText(flag)) {
                                            // 支持通配符 * 匹配
                                            if (flag.contains("*")) {
                                                String pattern = flag.replace("*", ".*");
                                                if (s.matches("(?s).*" + pattern + ".*")) {
                                                    log.info("状态检查失败，匹配到失败标识: {}", flag);
                                                    foundFailure[0] = true;
                                                }
                                            } else if (s.contains(flag)) {
                                                log.info("状态检查失败，匹配到失败标识: {}", flag);
                                                foundFailure[0] = true;
                                            }
                                        }
                                    }
                                }

                                // 检查是否包含成功标识（只有在没有匹配到失败标识的情况下才检查）
                                if (!foundFailure[0] && StringUtils.hasText(softServiceInstall.getSoftServiceStatusCheckSuccessFlag())) {
                                    String[] successFlags = softServiceInstall.getSoftServiceStatusCheckSuccessFlag().split(",");
                                    for (String flag : successFlags) {
                                        flag = flag.trim();
                                        if (StringUtils.hasText(flag)) {
                                            // 支持通配符 * 匹配
                                            if (flag.contains("*")) {
                                                String pattern = flag.replace("*", ".*");
                                                if (s.matches("(?s).*" + pattern + ".*")) {
                                                    log.info("状态检查成功，匹配到成功标识: {}", flag);
                                                    foundSuccess[0] = true;
                                                }
                                            } else if (s.contains(flag)) {
                                                log.info("状态检查成功，匹配到成功标识: {}", flag);
                                                foundSuccess[0] = true;
                                            }
                                        }
                                    }
                                }
                            });

                            // 执行状态检查命令
                            executeCommands(statusChannel, checkCmd, 10000, softServiceInstall);

                            // 等待一段时间，确保所有输出都被处理
                            Thread.sleep(1000);

                            if (foundFailure[0]) {
                                // 匹配到失败标识，服务未运行
                                statusBuilder.runStatus(0);
                                statusBuilder.runStatusDesc("未运行");
                                statusBuilder.message("服务未运行：匹配到失败标识");
                            } else if (foundSuccess[0]) {
                                // 匹配到成功标识，服务正在运行
                                statusBuilder.runStatus(1);
                                statusBuilder.runStatusDesc("运行中");
                                statusBuilder.message("服务正在运行：匹配到成功标识");
                            } else {
                                // 未匹配到任何标识，默认为运行中
                                statusBuilder.runStatus(1);
                                statusBuilder.runStatusDesc("运行中");
                                statusBuilder.message("服务状态未知：未匹配到任何标识，默认为运行中");
                                log.warn("服务[{}]状态检查未匹配到任何标识，命令输出: {}", softServiceInstall.getSoftServiceName(), statusOutputBuilder);
                            }
                        } catch (Exception e) {
                            log.error("执行状态检查命令失败: {}", e.getMessage(), e);
                            statusBuilder.runStatus(0);
                            statusBuilder.runStatusDesc("未知状态");
                            statusBuilder.message("执行状态检查命令失败: " + e.getMessage());
                        }
                    } else {
                        // 没有配置状态检查命令，默认为运行中
                        statusBuilder.runStatus(1);
                        statusBuilder.runStatusDesc("运行中");
                        statusBuilder.message("未配置状态检查命令，默认为运行中");
                    }

                } catch (Exception e) {
                    log.error("检查服务运行状态失败: {}", e.getMessage(), e);
                    statusBuilder.runStatus(0);
                    statusBuilder.runStatusDesc("未知状态");
                    statusBuilder.message("检查服务运行状态失败: " + e.getMessage());
                }
            } else {
                // 未安装成功，不检查运行状态
                statusBuilder.runStatus(0);
                statusBuilder.runStatusDesc("未运行");
                statusBuilder.message("软件未安装成功，无法检查运行状态");
            }

            // 更新安装记录中的运行状态
            softServiceInstall.setInstallRunStatus(statusBuilder.build().getRunStatus());
            softServiceInstall.setInstallLastCheckTime(System.currentTimeMillis());
            this.updateById(softServiceInstall);

            // 推送状态信息
            SoftServiceStatusInfo statusInfo = statusBuilder.build();
            sendServiceStatus(installId, statusInfo);

            return ReturnResult.success(statusInfo);
        } catch (Exception e) {
            log.error("检查服务状态失败: {}", e.getMessage(), e);
            return ReturnResult.failure(ReturnCode.REQUEST_PARAM_ERROR, "检查服务状态失败: " + e.getMessage());
        }
    }

    /**
     * 发送服务日志消息
     *
     * @param installId 安装ID
     * @param message   消息内容
     */
    private void sendServiceLog(Integer installId, String message) {
        if (socketSessionTemplate == null) {
            log.warn("socketSessionTemplate为空，无法发送服务日志通知");
            return;
        }

        try {
            // 创建消息事件
            MsgEvent msgEvent = MsgEvent.builder()
                    .name("service_log")
                    .eventId(installId.toString())
                    .msg(message)
                    .type("LOG")
                    .module("soft-service-log")
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 统一发送到/topic/install/soft
            socketSessionTemplate.send("/topic/install/soft", msgEvent);

            // 记录调试日志
            log.debug("发送服务日志通知: installId={}, message={}", installId, message);

            // 追加日志内容到运行日志
            appendOperationLog(installId, "runtime", message);

        } catch (Exception e) {
            log.error("发送服务日志通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送服务状态消息
     *
     * @param installId  安装ID
     * @param statusInfo 状态信息
     */
    private void sendServiceStatus(Integer installId, SoftServiceStatusInfo statusInfo) {
        if (socketSessionTemplate == null) {
            log.warn("socketSessionTemplate为空，无法发送服务状态通知");
            return;
        }

        try {
            // 创建消息事件
            MsgEvent msgEvent = MsgEvent.builder()
                    .name("service_status")
                    .eventId(installId.toString())
                    .data(statusInfo)
                    .type("STATUS")
                    .module("soft-service-status")
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 统一发送到/topic/install/soft
            socketSessionTemplate.send("/topic/install/soft", msgEvent);

            // 记录日志
            log.info("发送服务状态通知: installId={}, status={}, runStatus={}",
                    installId, statusInfo.getInstallStatus(), statusInfo.getRunStatus());

        } catch (Exception e) {
            log.error("发送服务状态通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 执行多条命令
     *
     * @param channel  SSH通道
     * @param commands 命令字符串，多条命令以\n分隔
     * @param timeout  超时时间（毫秒）
     * @throws Exception 执行异常
     */
    private void executeCommands(Channel channel, String commands, int timeout, SoftServiceInstall install) throws Exception {
        if (commands == null || commands.trim().isEmpty()) {
            return;
        }

        // 替换命令中的变量
        commands = replaceCommandVariables(commands, install);
        
        // 检查是否是脚本（以#!/bin/bash开头）
        if (commands.trim().startsWith("#!/bin/bash")) {
            log.debug("检测到脚本命令，将整体执行");

            // 创建临时脚本文件
            String tempScriptName = "temp_script_" + System.currentTimeMillis() + ".sh";

            try {
                // 在脚本末尾添加自删除命令
                String scriptWithCleanup = commands + "\n\n# 脚本执行完成后自动删除自身\nrm -f /tmp/" + tempScriptName + "\n";

                // 上传脚本(改进heredoc语法)
                log.debug("上传脚本到远程服务器: /tmp/{}", tempScriptName);
                channel.execute("cat > /tmp/" + tempScriptName + " << EOL\n" + scriptWithCleanup + "\nEOL\n", 500);

                // 添加执行权限(确保前一个命令完成)
                Thread.sleep(500); // 添加短暂延迟
                log.debug("添加执行权限: chmod +x /tmp/{}", tempScriptName);
                channel.execute("chmod +x /tmp/" + tempScriptName + "\n", 500);

                // 直接执行脚本，不使用nohup，不重定向输出
                log.debug("执行脚本: /bin/bash /tmp/{}", tempScriptName);
                channel.execute("/bin/bash /tmp/" + tempScriptName + "\n", timeout);

            } catch (Exception e) {
                // 确保即使出错也尝试删除临时文件
                try {
                    channel.execute("rm -f /tmp/" + tempScriptName + " 2>/dev/null\n", 500);
                } catch (Exception ignore) {}
                throw e;
            }
        } else {
            // 非脚本，按行执行命令
            String[] commandArray = commands.split("\\n");

            for (String command : commandArray) {
                if (command != null && !command.trim().isEmpty()) {
                    String trimmedCommand = command.trim();

                    if (StringUtils.isEmpty(trimmedCommand)) {
                        continue;
                    }
                    // 跳过以#开头的注释行
                    if (trimmedCommand.startsWith("#")) {
                        log.debug("跳过注释行: {}", trimmedCommand);
                        continue;
                    }

                    log.debug("执行命令: {}", trimmedCommand);
                    channel.execute(com.chua.common.support.utils.StringUtils.endWithAppend(trimmedCommand, "\n"), timeout);
                }
            }
        }
    }

    /**
     * 替换命令中的变量
     *
     * @param command 原始命令
     * @param install 安装记录
     * @return 替换后的命令
     */
    private String replaceCommandVariables(String command, SoftServiceInstall install) {
        if (command == null || command.isEmpty()) {
            return command;
        }

        // 替换安装路径变量 $DIR
        if (StringUtils.hasText(install.getInstallPathCustom())) {
            command = command.replace("$DIR", install.getInstallPathCustom());
        } else if (StringUtils.hasText(install.getInstallPath())) {
            command = command.replace("$DIR", install.getInstallPath());
        }

        // 替换安装端口变量 $PORT
        if (StringUtils.hasText(install.getInstallPort())) {
            command = command.replace("$PORT", install.getInstallPort());
        }

        // 替换默认端口变量 $DEFAULT_PORT
        if (StringUtils.hasText(install.getInstallDefaultPort())) {
            command = command.replace("$DEFAULT_PORT", install.getInstallDefaultPort());
        }

        // 替换配置目录变量 $CONFIG_DIR
        if (StringUtils.hasText(install.getInstallConfigPath())) {
            command = command.replace("$CONFIG_DIR", install.getInstallConfigPath());
        } else if (StringUtils.hasText(install.getInstallPathCustom())) {
            command = command.replace("$CONFIG_DIR", install.getInstallPathCustom() + "/config");
        } else if (StringUtils.hasText(install.getInstallPath())) {
            command = command.replace("$CONFIG_DIR", install.getInstallPath() + "/config");
        }

        // 替换日志目录变量 $LOG_DIR
        if (StringUtils.hasText(install.getServiceLogPath())) {
            command = command.replace("$LOG_DIR", install.getServiceLogPath());
        } else if (StringUtils.hasText(install.getInstallPathCustom())) {
            command = command.replace("$LOG_DIR", install.getInstallPathCustom() + "/logs");
        } else if (StringUtils.hasText(install.getInstallPath())) {
            command = command.replace("$LOG_DIR", install.getInstallPath() + "/logs");
        }

        // 替换版本变量 $VERSION
        if (StringUtils.hasText(install.getInstallVersion())) {
            command = command.replace("$VERSION", install.getInstallVersion());
        }

        // 替换时间戳变量 $TIMESTAMP
        command = command.replace("$TIMESTAMP", String.valueOf(System.currentTimeMillis()));

        // 替换日期变量 $DATE
        command = command.replace("$DATE", java.time.LocalDate.now().toString());

        return command;
    }

    /**
     * 批量检测服务状态
     *
     * @param installIds 安装记录ID列表
     * @return 操作结果，包含服务状态信息列表
     */
    @Override
    public ReturnResult<List<SoftServiceStatusInfo>> batchCheckServiceStatus(List<Integer> installIds) {
        if (installIds == null || installIds.isEmpty()) {
            return ReturnResult.illegal("安装记录ID列表不能为空");
        }

        log.info("批量检测服务状态，共{}个服务", installIds.size());

        List<SoftServiceStatusInfo> statusList = installIds.stream()
                .map(installId -> {
                    try {
                        ReturnResult<SoftServiceStatusInfo> result = this.checkServiceStatus(installId);
                        if (result.isOk()) {
                            return result.getData();
                        } else {
                            log.warn("检测服务状态失败: installId={}, message={}", installId, result.getMsg());
                            // 对于失败的服务，创建一个包含错误信息的状态对象
                            SoftServiceStatusInfo errorStatus = SoftServiceStatusInfo.builder()
                                    .installId(installId)
                                    .message(result.getMsg())
                                    .checkTime(System.currentTimeMillis())
                                    .runStatus(0)
                                    .runStatusDesc("未知状态")
                                    .installStatus(0)
                                    .installStatusDesc("未知状态")
                                    .exists(false)
                                    .build();

                            // 对失败的服务状态检测也发送通知
                            sendServiceStatus(installId, errorStatus);

                            return errorStatus;
                        }
                    } catch (Exception e) {
                        log.error("检测服务状态异常: installId={}, error={}", installId, e.getMessage(), e);
                        // 对于发生异常的服务，创建一个包含错误信息的状态对象
                        SoftServiceStatusInfo errorStatus = SoftServiceStatusInfo.builder()
                                .installId(installId)
                                .message("检测服务状态异常: " + e.getMessage())
                                .checkTime(System.currentTimeMillis())
                                .runStatus(0)
                                .runStatusDesc("未知状态")
                                .installStatus(0)
                                .installStatusDesc("未知状态")
                                .exists(false)
                                .build();

                        // 对异常的服务状态检测也发送通知
                        sendServiceStatus(installId, errorStatus);

                        return errorStatus;
                    }
                })
                .collect(Collectors.toList());

        log.info("批量检测服务状态完成，共{}个服务", statusList.size());
        return ReturnResult.success(statusList);
    }

    /**
     * 清除指定安装记录的缓存
     *
     * @param installId 安装记录ID
     */
    private void clearInstallCache(Integer installId) {
        String cacheKey = CACHE_KEY_PREFIX + installId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 更新服务状态检查命令
     *
     * @param installId   安装记录ID
     * @param command     状态检查命令
     * @param commandType 命令类型（0:单行命令，1:脚本）
     * @param successFlag 成功标识
     * @return 结果
     */
    @Override
    public ReturnResult<Boolean> updateStatusCheckCommand(Integer installId, String command, Integer commandType, String successFlag) {
        if (installId == null) {
            return ReturnResult.illegal("安装记录ID不能为空");
        }

        if (!StringUtils.hasText(command)) {
            return ReturnResult.illegal("状态检查命令不能为空");
        }

        try {
            // 查询安装记录是否存在
            SoftServiceInstall install = this.getById(installId);
            if (install == null) {
                return ReturnResult.illegal("安装记录不存在");
            }

            // 更新状态检查命令
            SoftServiceInstall updateInstall = new SoftServiceInstall();
            updateInstall.setInstallId(installId);
            updateInstall.setSoftServiceStatusCheckCommand(command);
            updateInstall.setSoftServiceStatusCheckCommandType(commandType);
            updateInstall.setSoftServiceStatusCheckSuccessFlag(successFlag);

            boolean success = this.updateById(updateInstall);

            if (success) {
                // 清除缓存
                clearInstallCache(installId);

                // 立即执行一次状态检查
                try {
                    // 直接调用checkServiceStatus方法执行状态检查
                    ReturnResult<SoftServiceStatusInfo> statusResult = this.checkServiceStatus(installId);

                    if (statusResult.isOk()) {
                        SoftServiceStatusInfo statusInfo = statusResult.getData();
                        // 发送状态检查通知
                        sendInstallProgress(
                                install,
                                "服务状态检查: " + (statusInfo.getRunStatus() == 1 ? "服务正在运行" : "服务未运行"),
                                100,
                                100,
                                statusInfo.getRunStatus() == 1 ? "SUCCESS" : "WARNING"
                        );
                    } else {
                        // 状态检查失败
                        sendInstallProgress(
                                install,
                                "服务状态检查失败: " + statusResult.getMsg(),
                                100,
                                100,
                                "WARNING"
                        );
                    }
                } catch (Exception e) {
                    log.error("执行状态检查命令失败: {}", e.getMessage(), e);
                    // 状态检查失败不影响命令更新结果
                }

                return ReturnResult.ok(null, "更新状态检查命令成功");
            } else {
                return ReturnResult.illegal("更新状态检查命令失败");
            }
        } catch (Exception e) {
            log.error("更新状态检查命令异常: {}", e.getMessage(), e);
            return ReturnResult.illegal("更新状态检查命令异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> batchInstall(Integer softServiceId, List<String> sshIds) {
        // 检查软件服务是否存在
        SoftService softService = softServiceService.getById(softServiceId);
        if (softService == null) {
            return ReturnResult.illegal("软件服务不存在");
        }

        // 检查安装命令是否存在
        if (softService.getSoftServiceInstallCommand() == null || softService.getSoftServiceInstallCommand().isEmpty()) {
            return ReturnResult.illegal("软件服务安装命令不存在");
        }

        // 批量安装
        for (String sshId : sshIds) {
            // 创建安装记录
            SoftServiceInstall softServiceInstall = new SoftServiceInstall();
            softServiceInstall.setSshId(sshId);
            softServiceInstall.setSoftServiceId(softServiceId);

            // 异步执行安装
            CompletableFuture.runAsync(() -> {
                try {
                    startInstall(softServiceInstall);
                } catch (Exception e) {
                    log.error("批量安装软件失败: {}", e.getMessage(), e);
                }
            });
        }

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> uninstall(Integer installId) {
        // 获取安装记录
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查是否已安装
        Integer installStatus = install.getInstallStatus();
        if (installStatus == 3 || installStatus == 0) {
            return ReturnResult.illegal("软件未安装或安装失败，无需卸载");
        }

        // 检查卸载命令是否存在
        if (install.getSoftServiceUninstallCommand() == null || install.getSoftServiceUninstallCommand().isEmpty()) {
            return ReturnResult.illegal("卸载命令不存在");
        }

        // 发送卸载开始通知
        sendInstallProgress(install, "准备卸载软件：" + install.getSoftServiceName(), 0, 100, "INFO");

        // 记录卸载开始日志
        saveOperationLog(install, "uninstall", "准备卸载软件：" + install.getSoftServiceName(), 0);

        // 异步执行卸载
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(install, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(install, "SSH连接成功，准备执行卸载命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_uninstall", "shell");

                // 设置监听器接收输出
                StringBuilder logBuilder = new StringBuilder();
                final int[] progressCounter = {0};
                final boolean[] foundSuccess = {false};
                final boolean[] foundFailure = {false};
                channel.setListener(s -> {
                    logBuilder.append(s).append("\n");

                    // 检查输出是否包含成功或失败标识
                    if (StringUtils.hasText(install.getSoftServiceUninstallFailureFlag())) {
                        String[] failureFlags = install.getSoftServiceUninstallFailureFlag().split(",");
                        for (String flag : failureFlags) {
                            flag = flag.trim();
                            if (StringUtils.hasText(flag)) {
                                // 支持通配符 * 匹配
                                if (flag.contains("*")) {
                                    String pattern = flag.replace("*", ".*");
                                    if (s.matches("(?s).*" + pattern + ".*")) {
                                        log.info("卸载失败，匹配到失败标识: {}", flag);
                                        foundFailure[0] = true;
                                    }
                                } else if (s.contains(flag)) {
                                    log.info("卸载失败，匹配到失败标识: {}", flag);
                                    foundFailure[0] = true;
                                }
                            }
                        }
                    }

                    // 检查是否包含成功标识（只有在没有匹配到失败标识的情况下才检查）
                    if (!foundFailure[0] && StringUtils.hasText(install.getSoftServiceUninstallSuccessFlag())) {
                        String[] successFlags = install.getSoftServiceUninstallSuccessFlag().split(",");
                        for (String flag : successFlags) {
                            flag = flag.trim();
                            if (StringUtils.hasText(flag)) {
                                // 支持通配符 * 匹配
                                if (flag.contains("*")) {
                                    String pattern = flag.replace("*", ".*");
                                    if (s.matches("(?s).*" + pattern + ".*")) {
                                        log.info("卸载成功，匹配到成功标识: {}", flag);
                                        foundSuccess[0] = true;
                                    }
                                } else if (s.contains(flag)) {
                                    log.info("卸载成功，匹配到成功标识: {}", flag);
                                    foundSuccess[0] = true;
                                }
                            }
                        }
                    }

                    // 计算进度（简单模拟，每收到一条消息增加一点进度，最多到95%）
                    progressCounter[0] += 5;
                    int progress = Math.min(progressCounter[0], 95);

                    // 推送卸载进度
                    sendInstallProgress(install, s, progress, 100, "INFO");
                });

                try {
                    // 检查命令类型并执行卸载命令
                    boolean isScript = install.getSoftServiceUninstallCommandType() != null &&
                            install.getSoftServiceUninstallCommandType() == 1;

                    if (isScript) {
                        sendInstallProgress(install, "检测到脚本卸载方式，准备执行脚本", 15, 100, "INFO");
                    } else {
                        sendInstallProgress(install, "检测到命令行卸载方式，准备执行命令", 15, 100, "INFO");
                    }

                    // 执行卸载命令
                    String uninstallCommand = install.getSoftServiceUninstallCommand();
                    sendInstallProgress(install, "执行卸载命令：" + (uninstallCommand.length() > 50 ? uninstallCommand.substring(0, 50) + "..." : uninstallCommand), 20, 100, "INFO");
                    executeCommands(channel, uninstallCommand, 10000, install);

                    // 等待一段时间，确保所有输出都被处理
                    Thread.sleep(2000);

                    if (foundFailure[0]) {
                        // 匹配到失败标识
                        updateInstallStatus(installId, 3, logBuilder + "\n卸载失败: 匹配到失败标识");
                        sendInstallProgress(install, "卸载失败：匹配到失败标识", 100, 100, "ERROR");
                    } else {
                        // 匹配到成功标识或无法判断，默认为成功
                        // 更新安装状态为未安装
                        updateInstallStatus(installId, 0, logBuilder.toString());
                        sendInstallProgress(install, "更新安装状态为未安装", 98, 100, "INFO");

                        // 推送卸载完成通知
                        if (foundSuccess[0]) {
                            sendInstallProgress(install, "卸载成功：匹配到成功标识", 100, 100, "SUCCESS");
                        } else {
                            sendInstallProgress(install, "卸载完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");
                        }
                    }
                } catch (Exception e) {
                    log.error("卸载软件失败: {}", e.getMessage(), e);
                    // 更新安装状态为失败
                    updateInstallStatus(installId, 3, logBuilder + "\n卸载失败: " + e.getMessage());
                    // 推送卸载失败通知
                    sendInstallProgress(install, "卸载失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("卸载软件失败: {}", e.getMessage(), e);
                // 更新安装状态为失败
                updateInstallStatus(installId, 3, "卸载失败: " + e.getMessage());
                // 推送卸载失败通知
                sendInstallProgress(install, "卸载失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }

    /**
     * 检查输出内容是否包含成功或失败标识
     *
     * @param output        命令输出内容
     * @param logBuilder     日志构建器
     * @param successFlag   成功标识，多个用逗号分隔
     * @param failureFlag   失败标识，多个用逗号分隔
     * @param foundSuccess  成功标识标记数组
     * @param foundFailure  失败标识标记数组
     * @param resultHandler 结果处理器
     */
    private void checkOutputForFlags(String output,
                                     StringBuilder logBuilder,
                                     String successFlag,
                                     String failureFlag,
                                     boolean[] foundSuccess,
                                     boolean[] foundFailure,
                                     Consumer<CommandResult> resultHandler) {
        // 检查输出是否包含失败标识
        if (StringUtils.hasText(failureFlag)) {
            String[] failureFlags = failureFlag.split(",");
            for (String flag : failureFlags) {
                flag = flag.trim();
                if (StringUtils.hasText(flag)) {
                    // 支持通配符 * 匹配
                    if (flag.contains("*")) {
                        String pattern = flag.replace("*", ".*");
                        if (output.matches("(?s).*" + pattern + ".*")) {
                            log.info("命令执行失败，匹配到失败标识: {}", flag);
                            foundFailure[0] = true;
                            resultHandler.accept(new CommandResult(false, true, logBuilder));
                        }
                    } else if (output.contains(flag)) {
                        log.info("命令执行失败，匹配到失败标识: {}", flag);
                        // 处理命令执行结果
                        foundFailure[0] = true;
                        resultHandler.accept(new CommandResult(false, true, logBuilder));
                    }
                }
            }
        }

        // 检查是否包含成功标识（只有在没有匹配到失败标识的情况下才检查）
        if (!foundFailure[0] && StringUtils.hasText(successFlag)) {
            String[] successFlags = successFlag.split(",");
            for (String flag : successFlags) {
                flag = flag.trim();
                if (StringUtils.hasText(flag)) {
                    // 支持通配符 * 匹配
                    if (flag.contains("*")) {
                        String pattern = flag.replace("*", ".*");
                        if (output.matches("(?s).*" + pattern + ".*")) {
                            log.info("命令执行成功，匹配到成功标识: {}", flag);
                            resultHandler.accept(new CommandResult(true, false, logBuilder));
                        }
                    } else if (output.contains(flag)) {
                        log.info("命令执行成功，匹配到成功标识: {}", flag);
                        resultHandler.accept(new CommandResult(true, false, logBuilder));
                    }
                }
            }
        }
    }

    /**
     * 创建命令输出监听器
     *
     * @param install         安装记录
     * @param logBuilder      日志构建器
     * @param progressCounter 进度计数器
     * @param foundSuccess    成功标识标记数组
     * @param foundFailure    失败标识标记数组
     * @param successFlag     成功标识
     * @param failureFlag     失败标识
     * @param operationType   操作类型(安装/启动/停止/重启/卸载)
     * @param resultHandler
     * @return 命令输出监听器
     */
    private com.chua.common.support.protocol.channel.ChannelListener createCommandOutputListener(
            SoftServiceInstall install,
            StringBuilder logBuilder,
            int[] progressCounter,
            boolean[] foundSuccess,
            boolean[] foundFailure,
            String successFlag,
            String failureFlag,
            String operationType,
            Consumer<CommandResult> resultHandler) {

        return s -> {
            // 将新输出添加到缓冲区
            logBuilder.append(s);

            // 检查输出是否包含成功或失败标识
            checkOutputForFlags(s, logBuilder, successFlag, failureFlag, foundSuccess, foundFailure, resultHandler);

            // 计算进度（根据不同阶段使用不同增量）
            int currentProgress = progressCounter[0];
            if (currentProgress < 50) {
                // 1-50阶段，每次+5
                progressCounter[0] += 5;
            } else if (currentProgress < 70) {
                // 50-70阶段，每次+3
                progressCounter[0] += 3;
            } else if (currentProgress < 90) {
                // 70-90阶段，每次+1
                progressCounter[0] += 1;
            } else {
                // 90往上，每次+0.5（使用整数时，我们乘以10再除以10来表示0.5）
                progressCounter[0] += (progressCounter[0] >= 95) ? 0 : 1;
            }

            // 确保进度不超过95%（留5%给最后的完成步骤）
            int progress = Math.min(progressCounter[0], 95);

            // 推送进度
            sendInstallProgress(install, s, progress, 100, "INFO");
        };
    }

    /**
     * 执行命令并处理结果
     *
     * @param install       安装记录
     * @param commandType   命令类型
     * @param command       命令内容
     * @param successFlag   成功标识
     * @param failureFlag   失败标识
     * @param operationType 操作类型(install/start/stop/restart/uninstall)
     * @param timeout       超时时间
     * @param resultHandler 结果处理器
     */
    private void executeCommandWithHandling(
            SoftServiceInstall install,
            Integer commandType,
            String command,
            String successFlag,
            String failureFlag,
            String operationType,
            int timeout,
            Consumer<CommandResult> resultHandler) {

        try {
            // 获取SSH客户端
            SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
            if (sshClient == null) {
                sendInstallProgress(install, "无法连接到SSH服务器", 100, 100, "ERROR");
                return;
            }

            sendInstallProgress(install, "SSH连接成功，准备执行" + operationType + "命令", 10, 100, "INFO");

            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            Channel channel = clientSession.openChannel(install.getInstallId() + "_" + operationType, "shell");

            // 设置监听器接收输出
            StringBuilder logBuilder = new StringBuilder();
            final int[] progressCounter = {10}; // 从10%开始
            final boolean[] foundSuccess = {false};
            final boolean[] foundFailure = {false};

            // 记录命令执行到日志
            String logOperationType;
            switch (operationType) {
                case "安装":
                    logOperationType = "install";
                    break;
                case "卸载":
                    logOperationType = "uninstall";
                    break;
                case "启动":
                    logOperationType = "start";
                    break;
                case "停止":
                    logOperationType = "stop";
                    break;
                case "重启":
                    logOperationType = "restart";
                    break;
                default:
                    logOperationType = operationType.toLowerCase();
            }

            // 记录命令到日志
            appendOperationLog(install.getInstallId(), logOperationType, "执行命令: " + command);

            // 创建并设置命令输出监听器
            channel.setListener(createCommandOutputListener(
                    install, logBuilder, progressCounter, foundSuccess, foundFailure,
                    successFlag, failureFlag, operationType, resultHandler));

            try {
                // 检查命令类型并执行命令
                boolean isScript = commandType != null && commandType == 1;

                if (isScript) {
                    sendInstallProgress(install, "检测到脚本" + operationType + "方式，准备执行脚本", 15, 100, "INFO");
                } else {
                    sendInstallProgress(install, "检测到命令行" + operationType + "方式，准备执行命令", 15, 100, "INFO");
                }

                // 执行命令
                sendInstallProgress(install, "执行" + operationType + "命令：" + (command.length() > 50 ? command.substring(0, 50) + "..." : command), 20, 100, "INFO");
                executeCommands(channel, command, timeout, install);

            } catch (Exception e) {
                log.error(operationType + "执行失败: {}", e.getMessage(), e);
                sendInstallProgress(install, operationType + "失败：" + e.getMessage(), 100, 100, "ERROR");
                // 记录错误到日志
                appendOperationLog(install.getInstallId(), logOperationType, operationType + "执行失败: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error(operationType + "执行失败: {}", e.getMessage(), e);
            sendInstallProgress(install, operationType + "失败：" + e.getMessage(), 100, 100, "ERROR");
        }
    }

    @Override
    public ReturnResult<List<String>> listServiceLogFiles(Integer installId) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        return softServiceLogService.getLogFilesByInstallId(installId);
    }

    @Override
    public ReturnResult<String> getServiceOperationLog(Integer installId, String operationType) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        if (!StringUtils.hasText(operationType)) {
            return ReturnResult.illegal("操作类型不能为空");
        }

        return softServiceLogService.getLogContent(installId, operationType);
    }

    @Override
    public ReturnResult<byte[]> downloadServiceOperationLog(Integer installId, String operationType) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        if (!StringUtils.hasText(operationType)) {
            return ReturnResult.illegal("操作类型不能为空");
        }

        return softServiceLogService.downloadLogFile(installId, operationType);
    }

    @Override
    public ReturnResult<String> getServiceConfig(Integer installId) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        // 获取安装记录
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查安装状态
        if (install.getInstallStatus() != 2) {
            return ReturnResult.illegal("该软件未安装成功，无法获取配置");
        }

        // 获取配置文件目录
        String configPath = install.getInstallConfigPath();
        if (configPath == null || configPath.isEmpty()) {
            // 如果配置文件目录为空，尝试使用默认目录
            configPath = install.getInstallPath() + "/config/application.conf";
            log.info("配置文件目录为空，使用默认目录: {}", configPath);
        }

        try {
            // 获取SSH客户端
            SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
            if (sshClient == null) {
                return ReturnResult.illegal("无法连接到SSH服务器");
            }

            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            Channel channel = clientSession.openChannel(installId + "_getconfig", "cmd");

            // 设置监听器接收输出
            StringBuilder configBuilder = new StringBuilder();
            channel.setListener(configBuilder::append);

            // 执行查看配置文件命令
            String command = "cat " + configPath;
            log.info("执行获取配置命令: {}", command);
            channel.execute(command, 10000); // 30秒超时

            String configContent = configBuilder.toString().trim();

            // 检查是否成功读取到配置
            if (configContent.isEmpty() || configContent.contains("No such file or directory")) {
                log.warn("配置文件不存在: {}", configPath);
                return ReturnResult.success("");
            }

            return ReturnResult.success(configContent);
        } catch (Exception e) {
            log.error("获取服务配置失败: {}", e.getMessage(), e);
            return ReturnResult.illegal("获取服务配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> updateServiceConfig(Integer installId, String config) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        if (config == null) {
            return ReturnResult.illegal("配置内容不能为空");
        }

        // 获取安装记录
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查安装状态
        if (install.getInstallStatus() != 2) {
            return ReturnResult.illegal("该软件未安装成功，无法更新配置");
        }

        // 获取配置文件目录
        String configPath = install.getInstallConfigPath();
        if (configPath == null || configPath.isEmpty()) {
            // 如果配置文件目录为空，尝试使用默认目录
            configPath = install.getInstallPath() + "/config/application.conf";
            log.info("配置文件目录为空，使用默认目录: {}", configPath);
        }

        try {
            // 获取SSH客户端
            SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
            if (sshClient == null) {
                return ReturnResult.illegal("无法连接到SSH服务器");
            }

            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            Channel channel = clientSession.openChannel(installId + "_updateconfig", "cmd");

            // 替换配置文件中的变量
            config = replaceCommandVariables(config, install);

            // 备份原配置文件
            String backupCommand = "cp " + configPath + " " + configPath + ".bak." + System.currentTimeMillis();
            log.info("备份配置文件: {}", backupCommand);
            channel.execute(backupCommand, 5000);

            // 写入新配置文件
            String tempFile = "/tmp/config_" + installId + "_" + System.currentTimeMillis() + ".tmp";
            log.info("创建临时配置文件: {}", tempFile);

            // 使用heredoc写入临时文件
            String writeCommand = "cat > " + tempFile + " << 'EOL'\n" +
                    config + "\nEOL\n";

            log.info("写入临时配置文件");
            channel.execute(writeCommand, 5000);

            // 移动临时文件到目标位置
            String moveCommand = "mv " + tempFile + " " + configPath;
            log.info("移动临时配置文件到目标位置: {}", moveCommand);
            channel.execute(moveCommand, 5000);

            // 更新配置记录
            SoftServiceInstall updateInstall = new SoftServiceInstall();
            updateInstall.setInstallId(installId);
            updateInstall.setServiceConfig(config);
            this.updateById(updateInstall);

            // 记录操作日志
            appendOperationLog(installId, "config", "更新配置文件: " + configPath);

            // 发送通知
            sendInstallProgress(install, "配置文件更新成功", 100, 100, "SUCCESS");

            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("更新服务配置失败: {}", e.getMessage(), e);
            return ReturnResult.illegal("更新服务配置失败: " + e.getMessage());
        }
    }

    /**
     * 命令执行结果
     */
    @Data
    @AllArgsConstructor
    private static class CommandResult {
        private boolean success;
        private boolean failure;
        private StringBuilder logBuilder;
    }

    @Override
    public boolean updateById(SoftServiceInstall entity) {
        // 先清除缓存
        String cacheKey = CACHE_KEY_PREFIX + entity.getInstallId();
        redisTemplate.delete(cacheKey);

        // 如果是更新命令相关字段，确保这些字段被正确保存到数据库
        if (entity.getSoftServiceInstallCommand() != null ||
                entity.getSoftServiceUninstallCommand() != null ||
                entity.getSoftServiceStartCommand() != null ||
                entity.getSoftServiceStopCommand() != null ||
                entity.getSoftServiceRestartCommand() != null ||
                entity.getSoftServiceStatusCheckCommand() != null) {

            log.info("更新安装记录[ID:{}]的命令数据", entity.getInstallId());
        }

        // 执行更新
        return super.updateById(entity);
    }
}