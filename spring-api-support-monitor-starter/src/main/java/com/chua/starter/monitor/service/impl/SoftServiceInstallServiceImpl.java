package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnCode;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.protocol.channel.Channel;
import com.chua.common.support.protocol.session.ChannelSession;
import com.chua.socketio.support.MsgEvent;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.ssh.support.ssh.CmdChannel;
import com.chua.ssh.support.ssh.SshChannelSession;
import com.chua.ssh.support.ssh.SshClient;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.monitor.entity.SoftServiceInstall;
import com.chua.starter.monitor.jpom.func.assets.model.MachineSshModel;
import com.chua.starter.monitor.jpom.func.assets.server.MachineSshServer;
import com.chua.starter.monitor.mapper.SoftServiceInstallMapper;
import com.chua.starter.monitor.pojo.SoftServiceStatusInfo;
import com.chua.starter.monitor.service.SoftServiceInstallService;
import com.chua.starter.monitor.service.SoftServiceService;
import com.chua.starter.monitor.starter.service.TerminalService;
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
            
            this.updateById(existInstall);
        }

        // 推送安装开始通知
        sendInstallProgress(existInstall, "开始安装 " + softService.getSoftServiceName(), 0, 100, "INFO");

        // 异步执行安装
        final SoftServiceInstall finalExistInstall = existInstall;
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.createClientBySshId(finalExistInstall.getSshId());
                if (sshClient == null) {
                    updateInstallStatus(finalExistInstall.getInstallId(), 3, "无法连接到SSH服务器");
                    sendInstallProgress(finalExistInstall, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(finalExistInstall, "SSH连接成功，准备执行安装命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(finalExistInstall.getInstallId().toString() + "_install", "shell");

                // 设置监听器接收输出
                StringBuilder logBuilder = new StringBuilder();
                final int[] progressCounter = {0};
                channel.setListener(s -> {
                    logBuilder.append(s).append("\n");
                    // 更新日志
//                    SoftServiceInstall updateInstall = new SoftServiceInstall();
//                    updateInstall.setInstallId(finalExistInstall.getInstallId());
//                    updateInstall.setInstallLog(logBuilder.toString());
//                    this.updateById(updateInstall);

                    // 计算进度（简单模拟，每收到一条消息增加一点进度，最多到95%）
                    progressCounter[0] += 5;
                    int progress = Math.min(progressCounter[0], 95);

                    // 推送安装进度
                    sendInstallProgress(finalExistInstall, s, progress, 100, "INFO");
                });

                try {
                    // 检查命令类型并执行安装命令
                    boolean isScript = finalExistInstall.getSoftServiceInstallCommandType() != null &&
                            finalExistInstall.getSoftServiceInstallCommandType() == 1;

                    if (isScript) {
                        sendInstallProgress(finalExistInstall, "检测到脚本安装方式，准备执行脚本", 15, 100, "INFO");
                    } else {
                        sendInstallProgress(finalExistInstall, "检测到命令行安装方式，准备执行命令", 15, 100, "INFO");
                    }
                    
                    // 执行安装命令
                    executeCommands(channel, finalExistInstall.getSoftServiceInstallCommand(), 300000); // 5分钟超时

                    // 更新安装状态为成功
                    SoftServiceInstall updatedInstall = updateInstallStatus(finalExistInstall.getInstallId(), 2, logBuilder.toString());

                    // 推送安装完成通知
                    sendInstallProgress(updatedInstall, "安装完成：" + finalExistInstall.getSoftServiceName(), 100, 100, "SUCCESS");
                } catch (Exception e) {
                    log.error("安装软件失败: {}", e.getMessage(), e);
                    // 更新安装状态为失败
                    SoftServiceInstall updatedInstall = updateInstallStatus(finalExistInstall.getInstallId(), 3, logBuilder + "\n安装失败: " + e.getMessage());
                    
                    // 推送安装失败通知
                    sendInstallProgress(updatedInstall, "安装失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("安装软件失败: {}", e.getMessage(), e);
                // 更新安装状态为失败
                SoftServiceInstall updatedInstall = updateInstallStatus(finalExistInstall.getInstallId(), 3, "安装失败: " + e.getMessage());

                // 推送安装失败通知
                sendInstallProgress(updatedInstall, "安装失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> stopInstall(Integer installId) {
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 如果已经不是安装中状态，则返回错误
        if (install.getInstallStatus() != 1) {
            return ReturnResult.illegal("该软件未在安装中，无需停止");
        }

        try {
            // 获取SSH客户端
            SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
            if (sshClient == null) {
                return ReturnResult.illegal("无法连接到SSH服务器");
            }

            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            // 关闭通道
            clientSession.closeChannel(install.getInstallId().toString());

            // 更新安装状态为失败
            updateInstallStatus(installId, 3, install.getInstallLogPath() + "\n安装被用户手动停止");

            // 推送安装停止通知
            sendInstallProgress(installId, "安装被用户手动停止", 100, 100, "WARNING");

            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("停止安装失败: {}", e.getMessage(), e);
            return ReturnResult.illegal("停止安装失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> startService(Integer installId) {
        // 获取安装记录
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            sendInstallProgress(installId, "安装记录不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查是否已安装
        if (install.getInstallStatus() != 2) {
            sendInstallProgress(installId, "软件未安装或安装失败，无法启动", 100, 100, "FAILURE");
            return ReturnResult.illegal("软件未安装或安装失败，无法启动");
        }

        // 检查是否已经在运行
        if (install.getInstallRunStatus() != null && install.getInstallRunStatus() == 1) {
            sendInstallProgress(installId, "服务已在运行中", 100, 100, "FAILURE");
            return ReturnResult.illegal("服务已在运行中");
        }

        // 检查启动命令是否存在
        if (install.getSoftServiceStartCommand() == null || install.getSoftServiceStartCommand().isEmpty()) {
            sendInstallProgress(installId, "启动命令不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("启动命令不存在");
        }

        // 发送启动开始通知
        sendInstallProgress(installId, "准备启动服务：" + install.getSoftServiceName(), 0, 100, "INFO");

        // 异步执行启动
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(installId, "SSH连接成功，准备执行启动命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_start", "shell");

                // 设置监听器接收输出
                StringBuilder logBuilder = new StringBuilder();
                final int[] progressCounter = {10}; // 从10%开始
                channel.setListener(s -> {
                    logBuilder.append(s).append("\n");

                    // 计算进度（简单模拟，每收到一条消息增加一点进度，最多到95%）
                    progressCounter[0] += 10;
                    int progress = Math.min(progressCounter[0], 95);
                    
                    // 推送启动进度
                    sendInstallProgress(installId, s, progress, 100, "INFO");
                });

                try {
                    // 检查命令类型并执行启动命令
                    boolean isScript = install.getSoftServiceStartCommandType() != null &&
                            install.getSoftServiceStartCommandType() == 1;

                    if (isScript) {
                        sendInstallProgress(installId, "检测到脚本启动方式，准备执行脚本", 15, 100, "INFO");
                    } else {
                        sendInstallProgress(installId, "检测到命令行启动方式，准备执行命令", 15, 100, "INFO");
                    }
                    
                    // 执行启动命令
                    String startCommand = install.getSoftServiceStartCommand();
                    sendInstallProgress(installId, "执行启动命令：" + (startCommand.length() > 50 ? startCommand.substring(0, 50) + "..." : startCommand), 20, 100, "INFO");
                    executeCommands(channel, startCommand, 60000); // 1分钟超时

                    // 更新运行状态为运行中
                    SoftServiceInstall updateInstall = new SoftServiceInstall();
                    updateInstall.setInstallId(installId);
                    updateInstall.setInstallRunStatus(1);
                    updateInstall.setInstallLastStartTime(System.currentTimeMillis());
                    this.updateById(updateInstall);
                    sendInstallProgress(installId, "更新服务状态为运行中", 98, 100, "INFO");

                    // 推送启动完成通知
                    sendInstallProgress(installId, "服务启动完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");

                    // 延迟检查服务状态
                    Thread.sleep(2000);
                    checkServiceStatus(installId);
                } catch (Exception e) {
                    log.error("启动服务失败: {}", e.getMessage(), e);
                    // 更新运行状态为未运行
                    SoftServiceInstall updateInstall = new SoftServiceInstall();
                    updateInstall.setInstallId(installId);
                    updateInstall.setInstallRunStatus(0);
                    this.updateById(updateInstall);
                    // 推送启动失败通知
                    sendInstallProgress(installId, "服务启动失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("启动服务失败: {}", e.getMessage(), e);
                // 更新运行状态为未运行
                SoftServiceInstall updateInstall = new SoftServiceInstall();
                updateInstall.setInstallId(installId);
                updateInstall.setInstallRunStatus(0);
                this.updateById(updateInstall);
                // 推送启动失败通知
                sendInstallProgress(installId, "服务启动失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> stopService(Integer installId) {
        // 获取安装记录
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            sendInstallProgress(installId, "安装记录不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查是否已安装
        if (install.getInstallStatus() != 2) {
            sendInstallProgress(installId, "软件未安装或安装失败，无法停止", 100, 100, "FAILURE");
            return ReturnResult.illegal("软件未安装或安装失败，无法停止");
        }

        // 检查是否已经停止
        if (install.getInstallRunStatus() == null || install.getInstallRunStatus() == 0) {
            sendInstallProgress(installId, "服务未运行", 100, 100, "FAILURE");
            return ReturnResult.illegal("服务未运行");
        }

        // 检查停止命令是否存在
        if (install.getSoftServiceStopCommand() == null || install.getSoftServiceStopCommand().isEmpty()) {
            sendInstallProgress(installId, "停止命令不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("停止命令不存在");
        }

        // 发送停止开始通知
        sendInstallProgress(installId, "准备停止服务：" + install.getSoftServiceName(), 0, 100, "INFO");

        // 异步执行停止
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(installId, "SSH连接成功，准备执行停止命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_stop", "shell");

                // 设置监听器接收输出
                StringBuilder logBuilder = new StringBuilder();
                final int[] progressCounter = {10}; // 从10%开始
                channel.setListener(s -> {
                    logBuilder.append(s).append("\n");

                    // 计算进度（简单模拟，每收到一条消息增加一点进度，最多到95%）
                    progressCounter[0] += 10;
                    int progress = Math.min(progressCounter[0], 95);
                    
                    // 推送停止进度
                    sendInstallProgress(installId, s, progress, 100, "INFO");
                });

                try {
                    // 检查命令类型并执行停止命令
                    boolean isScript = install.getSoftServiceStopCommandType() != null &&
                            install.getSoftServiceStopCommandType() == 1;

                    if (isScript) {
                        sendInstallProgress(installId, "检测到脚本停止方式，准备执行脚本", 15, 100, "INFO");
                    } else {
                        sendInstallProgress(installId, "检测到命令行停止方式，准备执行命令", 15, 100, "INFO");
                    }
                    
                    // 执行停止命令
                    String stopCommand = install.getSoftServiceStopCommand();
                    sendInstallProgress(installId, "执行停止命令：" + (stopCommand.length() > 50 ? stopCommand.substring(0, 50) + "..." : stopCommand), 20, 100, "INFO");
                    executeCommands(channel, stopCommand, 60000); // 1分钟超时

                    // 更新运行状态为未运行
                    SoftServiceInstall updateInstall = new SoftServiceInstall();
                    updateInstall.setInstallId(installId);
                    updateInstall.setInstallRunStatus(0);
                    updateInstall.setInstallLastStopTime(System.currentTimeMillis());
                    this.updateById(updateInstall);
                    sendInstallProgress(installId, "更新服务状态为未运行", 98, 100, "INFO");

                    // 推送停止完成通知
                    sendInstallProgress(installId, "服务停止完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");

                    // 延迟检查服务状态
                    Thread.sleep(2000);
                    checkServiceStatus(installId);
                } catch (Exception e) {
                    log.error("停止服务失败: {}", e.getMessage(), e);
                    // 更新运行状态保持不变
                    // 推送停止失败通知
                    sendInstallProgress(installId, "服务停止失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("停止服务失败: {}", e.getMessage(), e);
                // 推送停止失败通知
                sendInstallProgress(installId, "服务停止失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> restartService(Integer installId) {
        // 获取安装记录
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            sendInstallProgress(installId, "安装记录不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查是否已安装
        if (install.getInstallStatus() != 2) {
            sendInstallProgress(installId, "软件未安装或安装失败，无法重启", 100, 100, "FAILURE");
            return ReturnResult.illegal("软件未安装或安装失败，无法重启");
        }

        // 发送重启开始通知
        sendInstallProgress(installId, "准备重启服务：" + install.getSoftServiceName(), 0, 100, "INFO");

        // 如果有直接的重启命令，优先使用重启命令
        if (install.getSoftServiceRestartCommand() != null && !install.getSoftServiceRestartCommand().isEmpty()) {
            // 异步执行重启
            CompletableFuture.runAsync(() -> {
                try {
                    // 获取SSH客户端
                    SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
                    if (sshClient == null) {
                        sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
                        return;
                    }

                    sendInstallProgress(installId, "SSH连接成功，准备执行重启命令", 10, 100, "INFO");

                    ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                    Channel channel = clientSession.openChannel(installId + "_restart", "shell");

                    // 设置监听器接收输出
                    StringBuilder logBuilder = new StringBuilder();
                    final int[] progressCounter = {10}; // 从10%开始
                    channel.setListener(s -> {
                        logBuilder.append(s).append("\n");

                        // 计算进度（简单模拟，每收到一条消息增加一点进度，最多到95%）
                        progressCounter[0] += 10;
                        int progress = Math.min(progressCounter[0], 95);

                        // 推送重启进度
                        sendInstallProgress(installId, s, progress, 100, "INFO");
                    });

                    try {
                        // 检查命令类型并执行重启命令
                        boolean isScript = install.getSoftServiceRestartCommandType() != null &&
                                install.getSoftServiceRestartCommandType() == 1;

                        if (isScript) {
                            sendInstallProgress(installId, "检测到脚本重启方式，准备执行脚本", 15, 100, "INFO");
                        } else {
                            sendInstallProgress(installId, "检测到命令行重启方式，准备执行命令", 15, 100, "INFO");
                        }

                        // 执行重启命令
                        String restartCommand = install.getSoftServiceRestartCommand();
                        sendInstallProgress(installId, "执行重启命令：" + (restartCommand.length() > 50 ? restartCommand.substring(0, 50) + "..." : restartCommand), 20, 100, "INFO");
                        executeCommands(channel, restartCommand, 120000); // 2分钟超时

                        // 更新运行状态为运行中
                        SoftServiceInstall updateInstall = new SoftServiceInstall();
                        updateInstall.setInstallId(installId);
                        updateInstall.setInstallRunStatus(1);
                        updateInstall.setInstallLastStopTime(System.currentTimeMillis());
                        updateInstall.setInstallLastStartTime(System.currentTimeMillis());
                        this.updateById(updateInstall);
                        sendInstallProgress(installId, "更新服务状态为运行中", 98, 100, "INFO");

                        // 推送重启完成通知
                        sendInstallProgress(installId, "服务重启完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");

                        // 延迟检查服务状态
                        Thread.sleep(2000);
                        checkServiceStatus(installId);
                    } catch (Exception e) {
                        log.error("重启服务失败: {}", e.getMessage(), e);
                        // 更新运行状态为未运行
                        SoftServiceInstall updateInstall = new SoftServiceInstall();
                        updateInstall.setInstallId(installId);
                        updateInstall.setInstallRunStatus(0);
                        this.updateById(updateInstall);
                        // 推送重启失败通知
                        sendInstallProgress(installId, "服务重启失败：" + e.getMessage(), 100, 100, "ERROR");
                    }
                } catch (Exception e) {
                    log.error("重启服务失败: {}", e.getMessage(), e);
                    // 更新运行状态为未运行
                    SoftServiceInstall updateInstall = new SoftServiceInstall();
                    updateInstall.setInstallId(installId);
                    updateInstall.setInstallRunStatus(0);
                    this.updateById(updateInstall);
                    // 推送重启失败通知
                    sendInstallProgress(installId, "服务重启失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            });

            return ReturnResult.success(true);
        } else {
            // 如果没有直接的重启命令，则先停止再启动
            sendInstallProgress(installId, "未配置重启命令，将执行停止后启动操作", 5, 100, "INFO");
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
            sendInstallProgress(installId, "安装记录不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查停止和启动命令是否存在
        if (install.getSoftServiceStopCommand() == null || install.getSoftServiceStopCommand().isEmpty()) {
            sendInstallProgress(installId, "停止命令不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("停止命令不存在");
        }
        if (install.getSoftServiceStartCommand() == null || install.getSoftServiceStartCommand().isEmpty()) {
            sendInstallProgress(installId, "启动命令不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("启动命令不存在");
        }

        // 异步执行先停止再启动
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(installId, "SSH连接成功，准备执行停止命令", 10, 100, "INFO");

                // 1. 先执行停止命令
                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel stopChannel = clientSession.openChannel(installId + "_stop_then_start_stop", "shell");

                StringBuilder stopLogBuilder = new StringBuilder();
                stopChannel.setListener(s -> {
                    stopLogBuilder.append(s).append("\n");
                    // 推送停止进度
                    sendInstallProgress(installId, "停止服务中：" + s, 25, 100, "INFO");
                });

                try {
                    // 检查停止命令类型
                    boolean isStopScript = install.getSoftServiceStopCommandType() != null &&
                            install.getSoftServiceStopCommandType() == 1;

                    if (isStopScript) {
                        sendInstallProgress(installId, "检测到脚本停止方式，准备执行脚本", 15, 100, "INFO");
                    } else {
                        sendInstallProgress(installId, "检测到命令行停止方式，准备执行命令", 15, 100, "INFO");
                    }

                    // 执行停止命令
                    String stopCommand = install.getSoftServiceStopCommand();
                    sendInstallProgress(installId, "执行停止命令：" + (stopCommand.length() > 50 ? stopCommand.substring(0, 50) + "..." : stopCommand), 20, 100, "INFO");
                    executeCommands(stopChannel, stopCommand, 60000); // 1分钟超时

                    // 更新运行状态为未运行
                    SoftServiceInstall updateStopInstall = new SoftServiceInstall();
                    updateStopInstall.setInstallId(installId);
                    updateStopInstall.setInstallRunStatus(0);
                    updateStopInstall.setInstallLastStopTime(System.currentTimeMillis());
                    this.updateById(updateStopInstall);

                    // 推送停止完成通知
                    sendInstallProgress(installId, "服务停止完成，准备启动服务", 50, 100, "INFO");

                    // 等待一段时间再启动
                    sendInstallProgress(installId, "等待3秒后开始启动服务", 55, 100, "INFO");
                    Thread.sleep(3000);

                    // 2. 再执行启动命令
                    Channel startChannel = clientSession.openChannel(installId + "_stop_then_start_start", "shell");

                    StringBuilder startLogBuilder = new StringBuilder();
                    startChannel.setListener(s -> {
                        startLogBuilder.append(s).append("\n");
                        // 推送启动进度
                        sendInstallProgress(installId, "启动服务中：" + s, 75, 100, "INFO");
                    });

                    try {
                        // 检查启动命令类型
                        boolean isStartScript = install.getSoftServiceStartCommandType() != null &&
                                install.getSoftServiceStartCommandType() == 1;

                        if (isStartScript) {
                            sendInstallProgress(installId, "检测到脚本启动方式，准备执行脚本", 60, 100, "INFO");
                        } else {
                            sendInstallProgress(installId, "检测到命令行启动方式，准备执行命令", 60, 100, "INFO");
                        }

                        // 执行启动命令
                        String startCommand = install.getSoftServiceStartCommand();
                        sendInstallProgress(installId, "执行启动命令：" + (startCommand.length() > 50 ? startCommand.substring(0, 50) + "..." : startCommand), 65, 100, "INFO");
                        executeCommands(startChannel, startCommand, 60000); // 1分钟超时

                        // 更新运行状态为运行中
                        SoftServiceInstall updateStartInstall = new SoftServiceInstall();
                        updateStartInstall.setInstallId(installId);
                        updateStartInstall.setInstallRunStatus(1);
                        updateStartInstall.setInstallLastStartTime(System.currentTimeMillis());
                        this.updateById(updateStartInstall);
                        sendInstallProgress(installId, "更新服务状态为运行中", 98, 100, "INFO");

                        // 推送启动完成通知
                        sendInstallProgress(installId, "服务重启完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");

                        // 延迟检查服务状态
                        Thread.sleep(2000);
                        checkServiceStatus(installId);
                    } catch (Exception e) {
                        log.error("启动服务失败: {}", e.getMessage(), e);
                        // 更新运行状态为未运行
                        SoftServiceInstall updateStartInstall = new SoftServiceInstall();
                        updateStartInstall.setInstallId(installId);
                        updateStartInstall.setInstallRunStatus(0);
                        this.updateById(updateStartInstall);
                        // 推送启动失败通知
                        sendInstallProgress(installId, "服务启动失败：" + e.getMessage(), 100, 100, "ERROR");
                    }
                } catch (Exception e) {
                    log.error("停止服务失败: {}", e.getMessage(), e);
                    // 推送停止失败通知
                    sendInstallProgress(installId, "服务停止失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("重启服务失败: {}", e.getMessage(), e);
                // 推送重启失败通知
                sendInstallProgress(installId, "服务重启失败：" + e.getMessage(), 100, 100, "ERROR");
            }
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

        // 清除缓存，确保下次获取是最新数据
        clearInstallCache(installId);

        // 获取更新后的完整记录
        return this.getInstallById(installId);
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
            channel.execute(command, 30000); // 30秒超时

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

        // 获取日志目录
        String logPath = install.getInstallLogPath();
        if (logPath == null || logPath.isEmpty()) {
            return ReturnResult.illegal("日志路径未配置");
        }

        // 推送监控开始通知
        sendInstallProgress(installId, "开始监控日志: " + logPath, 0, 100, "INFO");

        // 异步执行监控
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
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
                String command = "tail -f " + logPath;
                sendInstallProgress(installId, "执行命令: " + command, 50, 100, "INFO");
                channel.execute(command, 0); // 无超时，持续执行
            } catch (Exception e) {
                log.error("监控服务日志失败: {}", e.getMessage(), e);
                sendInstallProgress(installId, "监控服务日志失败：" + e.getMessage(), 100, 100, "ERROR");
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
                    // 先尝试使用ps命令查找相关进程
                    String processName = softServiceInstall.getSoftServiceName().toLowerCase();
                    String checkProcessCmd = "ps -ef | grep -v grep | grep -i '" + processName + "' || echo 'NOT_RUNNING'";
                    String processResult = channel.execute(checkProcessCmd, 10000);

                    boolean isRunning = !processResult.contains("NOT_RUNNING");

                    // 如果使用进程名搜索没有找到，可以尝试使用安装路径查找
                    if (!isRunning && softServiceInstall.getInstallPath() != null && !softServiceInstall.getInstallPath().isEmpty()) {
                        checkProcessCmd = "ps -ef | grep -v grep | grep -i '" + softServiceInstall.getInstallPath() + "' || echo 'NOT_RUNNING'";
                        processResult = channel.execute(checkProcessCmd, 10000);
                        isRunning = !processResult.contains("NOT_RUNNING");
                    }

                    if (isRunning) {
                        statusBuilder.runStatus(1);
                        statusBuilder.runStatusDesc("运行中");

                        // 尝试提取PID和资源使用情况
                        try {
                            // 提取PID
                            String pidCmd = "ps -ef | grep -v grep | grep -i '" + processName + "' | awk '{print $2}' | head -1";
                            String pid = channel.execute(pidCmd, 5000).trim();

                            if (pid != null && !pid.isEmpty() && !pid.equals("NOT_RUNNING")) {
                                statusBuilder.pid(pid);

                                // 获取内存使用
                                String memCmd = "ps -o rss= -p " + pid + " | awk '{print $1/1024}'";
                                String memResult = channel.execute(memCmd, 5000).trim();
                                if (memResult != null && !memResult.isEmpty() && !memResult.contains("ERROR")) {
                                    try {
                                        double memoryUsage = Double.parseDouble(memResult);
                                        statusBuilder.memoryUsage(memoryUsage);
                                    } catch (NumberFormatException e) {
                                        log.debug("解析内存使用失败: {}", memResult);
                                    }
                                }

                                // 获取CPU使用
                                String cpuCmd = "ps -o %cpu= -p " + pid + " | awk '{print $1}'";
                                String cpuResult = channel.execute(cpuCmd, 5000).trim();
                                if (cpuResult != null && !cpuResult.isEmpty() && !cpuResult.contains("ERROR")) {
                                    try {
                                        double cpuUsage = Double.parseDouble(cpuResult);
                                        statusBuilder.cpuUsage(cpuUsage);
                                    } catch (NumberFormatException e) {
                                        log.debug("解析CPU使用失败: {}", cpuResult);
                                    }
                                }

                                // 获取运行时间
                                String etimeCmd = "ps -o etimes= -p " + pid + " | awk '{print $1}'";
                                String etimeResult = channel.execute(etimeCmd, 5000).trim();
                                if (etimeResult != null && !etimeResult.isEmpty() && !etimeResult.contains("ERROR")) {
                                    try {
                                        long runningTime = Long.parseLong(etimeResult);
                                        statusBuilder.runningTime(runningTime);
                                    } catch (NumberFormatException e) {
                                        log.debug("解析运行时间失败: {}", etimeResult);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.debug("获取进程详细信息失败: {}", e.getMessage());
                        }

                        statusBuilder.message("服务正在运行");
                    } else {
                        statusBuilder.runStatus(0);
                        statusBuilder.runStatusDesc("未运行");
                        statusBuilder.message("服务未运行");
                    }
                } catch (Exception e) {
                    log.error("检查服务运行状态失败: {}", e.getMessage(), e);
                    statusBuilder.runStatus(0);
                    statusBuilder.runStatusDesc("未知状态");
                    statusBuilder.message("检查服务运行状态失败: " + e.getMessage());
                }
            } else {
                // 如果未安装成功，则直接设置运行状态为未运行
                statusBuilder.runStatus(0);
                statusBuilder.runStatusDesc("未运行");
                statusBuilder.exists(false);

                if (softServiceInstall.getInstallStatus() == 1) {
                    statusBuilder.message("服务正在安装中");
                } else if (softServiceInstall.getInstallStatus() == 3) {
                    statusBuilder.message("服务安装失败");
                } else {
                    statusBuilder.message("服务未安装");
                }
            }

            // 构建状态信息对象
            SoftServiceStatusInfo statusInfo = statusBuilder.build();

            // 更新安装记录中的运行状态
            if (softServiceInstall.getInstallRunStatus() != statusInfo.getRunStatus()) {
                SoftServiceInstall updateInstall = new SoftServiceInstall();
                updateInstall.setInstallId(installId);
                updateInstall.setInstallRunStatus(statusInfo.getRunStatus());
                this.updateById(updateInstall);

                // 记录状态变更日志
                log.info("服务运行状态已变更: installId={}, oldStatus={}, newStatus={}",
                        installId, softServiceInstall.getInstallRunStatus(), statusInfo.getRunStatus());
            }

            // 推送状态信息
            sendServiceStatus(installId, statusInfo);

            return ReturnResult.success(statusInfo);
        } catch (Exception e) {
            log.error("检测服务状态失败: {}", e.getMessage(), e);
            return ReturnResult.failure(ReturnCode.REQUEST_PARAM_ERROR, "检测服务状态失败: " + e.getMessage());
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
     * @return 执行结果
     * @throws Exception 执行异常
     */
    private String executeCommands(Channel channel, String commands, int timeout) throws Exception {
        if (commands == null || commands.trim().isEmpty()) {
            return "";
        }

        // 检查是否是脚本（以#!/bin/bash开头）
        if (commands.trim().startsWith("#!/bin/bash")) {
            log.debug("检测到脚本命令，将整体执行");

            // 创建临时脚本文件
            String tempScriptName = "temp_script_" + System.currentTimeMillis() + ".sh";

            try {
                // 上传脚本到远程服务器
                log.debug("上传脚本到远程服务器: /tmp/{}", tempScriptName);
                channel.execute("cat > /tmp/" + tempScriptName + " << 'EOL'\n" + commands + "\nEOL", timeout);

                // 添加执行权限
                log.debug("添加执行权限: chmod +x /tmp/{}", tempScriptName);
                channel.execute("chmod +x /tmp/" + tempScriptName, timeout);

                // 执行脚本
                log.debug("执行脚本: /bin/bash /tmp/{}", tempScriptName);
                String result = channel.execute("/bin/bash /tmp/" + tempScriptName, timeout);

                // 删除临时脚本
                log.debug("删除临时脚本: /tmp/{}", tempScriptName);
                channel.execute("rm -f /tmp/" + tempScriptName, 5000);

                return result;
            } catch (Exception e) {
                log.error("执行脚本失败: {}", e.getMessage(), e);
                // 尝试清理临时文件
                try {
                    channel.execute("rm -f /tmp/" + tempScriptName, 5000);
                } catch (Exception ex) {
                    // 忽略清理失败的异常
                }
                throw e;
            }
        } else {
            // 非脚本，按行执行命令
            StringBuilder resultBuilder = new StringBuilder();
            String[] commandArray = commands.split("\\n");

            for (String command : commandArray) {
                if (command != null && !command.trim().isEmpty()) {
                    String trimmedCommand = command.trim();

                    // 跳过以#开头的注释行
                    if (trimmedCommand.startsWith("#")) {
                        log.debug("跳过注释行: {}", trimmedCommand);
                        continue;
                    }

                    log.debug("执行命令: {}", trimmedCommand);

                    String result = channel.execute(trimmedCommand, timeout);
                    resultBuilder.append(result);

                    // 添加换行符，以区分不同命令的输出
                    if (!result.endsWith("\n")) {
                        resultBuilder.append("\n");
                    }
                }
            }

            return resultBuilder.toString();
        }
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
                    SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
                    if (sshClient != null && sshClient.getSession() != null) {
                        SshChannelSession session = (SshChannelSession) sshClient.getSession();
                        CmdChannel openChannel = (CmdChannel) session.openChannel("exec");
                        String result = openChannel.execute(command, 10000);
                        boolean isRunning = false;

                        // 判断服务是否运行中
                        if (StringUtils.isEmpty(successFlag)) {
                            isRunning = com.chua.common.support.utils.StringUtils.isNotBlank(result) && !result.contains("not found") && !result.contains("No such file");
                        } else {
                            isRunning = result != null && result.contains(successFlag);
                        }

                        // 更新运行状态
                        SoftServiceInstall statusUpdate = new SoftServiceInstall();
                        statusUpdate.setInstallId(installId);
                        statusUpdate.setInstallRunStatus(isRunning ? 1 : 0);
                        statusUpdate.setInstallLastCheckTime(System.currentTimeMillis());

                        this.updateById(statusUpdate);

                        // 发送状态检查通知
                        sendInstallProgress(
                                install,
                                "服务状态检查: " + (isRunning ? "服务正在运行" : "服务未运行"),
                                100,
                                100,
                                isRunning ? "SUCCESS" : "WARNING"
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
            sendInstallProgress(installId, "安装记录不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查是否已安装
        if (install.getInstallStatus() != 2) {
            sendInstallProgress(installId, "软件未安装或安装失败，无法卸载", 100, 100, "FAILURE");
            return ReturnResult.illegal("软件未安装或安装失败，无法卸载");
        }

        // 检查卸载命令是否存在
        if (install.getSoftServiceUninstallCommand() == null || install.getSoftServiceUninstallCommand().isEmpty()) {
            sendInstallProgress(installId, "卸载命令不存在", 100, 100, "FAILURE");
            return ReturnResult.illegal("卸载命令不存在");
        }

        // 发送卸载开始通知
        sendInstallProgress(installId, "准备卸载软件：" + install.getSoftServiceName(), 0, 100, "INFO");

        // 异步执行卸载
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.createClientBySshId(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(installId, "SSH连接成功，准备执行卸载命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_uninstall", "shell");

                // 设置监听器接收输出
                StringBuilder logBuilder = new StringBuilder();
                final int[] progressCounter = {0};
                channel.setListener(s -> {
                    logBuilder.append(s).append("\n");

                    // 计算进度（简单模拟，每收到一条消息增加一点进度，最多到95%）
                    progressCounter[0] += 5;
                    int progress = Math.min(progressCounter[0], 95);

                    // 推送卸载进度
                    sendInstallProgress(installId, s, progress, 100, "INFO");
                });

                try {
                    // 检查命令类型并执行卸载命令
                    boolean isScript = install.getSoftServiceUninstallCommandType() != null &&
                            install.getSoftServiceUninstallCommandType() == 1;

                    if (isScript) {
                        sendInstallProgress(installId, "检测到脚本卸载方式，准备执行脚本", 15, 100, "INFO");
                    } else {
                        sendInstallProgress(installId, "检测到命令行卸载方式，准备执行命令", 15, 100, "INFO");
                    }

                    // 执行卸载命令
                    String uninstallCommand = install.getSoftServiceUninstallCommand();
                    sendInstallProgress(installId, "执行卸载命令：" + (uninstallCommand.length() > 50 ? uninstallCommand.substring(0, 50) + "..." : uninstallCommand), 20, 100, "INFO");
                    executeCommands(channel, uninstallCommand, 300000); // 5分钟超时

                    // 更新安装状态为未安装
                    updateInstallStatus(installId, 0, logBuilder.toString());
                    sendInstallProgress(installId, "更新安装状态为未安装", 98, 100, "INFO");

                    // 推送卸载完成通知
                    sendInstallProgress(installId, "卸载完成：" + install.getSoftServiceName(), 100, 100, "SUCCESS");
                } catch (Exception e) {
                    log.error("卸载软件失败: {}", e.getMessage(), e);
                    // 更新安装状态为失败
                    updateInstallStatus(installId, 3, logBuilder + "\n卸载失败: " + e.getMessage());
                    // 推送卸载失败通知
                    sendInstallProgress(installId, "卸载失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("卸载软件失败: {}", e.getMessage(), e);
                // 更新安装状态为失败
                updateInstallStatus(installId, 3, "卸载失败: " + e.getMessage());
                // 推送卸载失败通知
                sendInstallProgress(installId, "卸载失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }
} 