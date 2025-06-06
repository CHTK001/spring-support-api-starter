package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.protocol.channel.Channel;
import com.chua.common.support.protocol.session.ChannelSession;
import com.chua.socketio.support.MsgEvent;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.ssh.support.ssh.SshClient;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.monitor.entity.SoftServiceInstall;
import com.chua.starter.monitor.mapper.SoftServiceInstallMapper;
import com.chua.starter.monitor.service.SoftServiceInstallService;
import com.chua.starter.monitor.service.SoftServiceService;
import com.chua.starter.monitor.starter.service.TerminalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    @AutoInject
    private SocketSessionTemplate socketSessionTemplate;

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

        return ReturnResult.preconditioning(this.save(softServiceInstall)).toResult();
    }

    @Override
    public ReturnResult<Boolean> updateInstall(SoftServiceInstall softServiceInstall) {
        return ReturnResult.preconditioning(this.updateById(softServiceInstall)).toResult();
    }

    @Override
    public ReturnResult<Boolean> removeInstallById(Integer id) {
        return ReturnResult.preconditioning(this.removeById(id)).toResult();
    }

    @Override
    public SoftServiceInstall getInstallById(Integer id) {
        return this.getById(id);
    }

    @Override
    public List<SoftServiceInstall> listInstallBySshId(String sshId) {
        LambdaQueryWrapper<SoftServiceInstall> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoftServiceInstall::getSshId, sshId);
        return this.list(queryWrapper);
    }

    @Override
    public List<SoftServiceInstall> listInstallBySoftServiceId(Integer softServiceId) {
        LambdaQueryWrapper<SoftServiceInstall> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoftServiceInstall::getSoftServiceId, softServiceId);
        return this.list(queryWrapper);
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
            return ReturnResult.illegal("软件服务不存在");
        }

        // 检查安装命令是否存在
        if (softService.getSoftServiceInstall() == null || softService.getSoftServiceInstall().isEmpty()) {
            return ReturnResult.illegal("软件服务安装命令不存在");
        }

        // 检查是否已经存在安装记录
        SoftServiceInstall existInstall = this.getInstallBySshIdAndSoftServiceId(
                softServiceInstall.getSshId(), softServiceInstall.getSoftServiceId());

        // 如果存在安装记录且正在安装中，则返回错误
        if (existInstall != null && existInstall.getInstallStatus() == 1) {
            return ReturnResult.illegal("该软件已在安装中，请稍后再试");
        }

        // 如果不存在安装记录，则创建新的安装记录
        if (existInstall == null) {
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
            this.updateById(existInstall);
        }

        // 推送安装开始通知
        sendInstallProgress(existInstall.getInstallId(), "开始安装 " + softService.getSoftServiceName(), 0, 100, "INFO");

        // 异步执行安装
        final SoftServiceInstall finalExistInstall = existInstall;
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.getClient(finalExistInstall.getSshId());
                if (sshClient == null) {
                    updateInstallStatus(finalExistInstall.getInstallId(), 3, "无法连接到SSH服务器");
                    sendInstallProgress(finalExistInstall.getInstallId(), "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(finalExistInstall.getInstallId(), "SSH连接成功，准备执行安装命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(finalExistInstall.getInstallId().toString(), "install");

                // 设置监听器接收输出
                StringBuilder logBuilder = new StringBuilder();
                final int[] progressCounter = {0};
                channel.setListener(s -> {
                    logBuilder.append(s).append("\n");
                    // 更新日志
                    SoftServiceInstall updateInstall = new SoftServiceInstall();
                    updateInstall.setInstallId(finalExistInstall.getInstallId());
                    updateInstall.setInstallLog(logBuilder.toString());
                    this.updateById(updateInstall);

                    // 计算进度（简单模拟，每收到一条消息增加一点进度，最多到95%）
                    progressCounter[0] += 5;
                    int progress = Math.min(progressCounter[0], 95);

                    // 推送安装进度
                    sendInstallProgress(finalExistInstall.getInstallId(), s, progress, 100, "INFO");
                });

                try {
                    // 执行安装命令
                    channel.execute(softService.getSoftServiceInstall(), 300000); // 5分钟超时

                    // 更新安装状态为成功
                    updateInstallStatus(finalExistInstall.getInstallId(), 2, logBuilder.toString());

                    // 推送安装完成通知
                    sendInstallProgress(finalExistInstall.getInstallId(), "安装完成：" + softService.getSoftServiceName(), 100, 100, "SUCCESS");
                } catch (Exception e) {
                    log.error("安装软件失败: {}", e.getMessage(), e);
                    // 更新安装状态为失败
                    updateInstallStatus(finalExistInstall.getInstallId(), 3, logBuilder + "\n安装失败: " + e.getMessage());
                    
                    // 推送安装失败通知
                    sendInstallProgress(finalExistInstall.getInstallId(), "安装失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("安装软件失败: {}", e.getMessage(), e);
                // 更新安装状态为失败
                updateInstallStatus(finalExistInstall.getInstallId(), 3, "安装失败: " + e.getMessage());

                // 推送安装失败通知
                sendInstallProgress(finalExistInstall.getInstallId(), "安装失败：" + e.getMessage(), 100, 100, "ERROR");
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
            SshClient sshClient = terminalService.getClient(install.getSshId());
            if (sshClient == null) {
                return ReturnResult.illegal("无法连接到SSH服务器");
            }

            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            // 关闭通道
            clientSession.closeChannel(install.getInstallId().toString());

            // 更新安装状态为失败
            updateInstallStatus(installId, 3, install.getInstallLog() + "\n安装被用户手动停止");

            // 推送安装停止通知
            sendInstallProgress(installId, "安装被用户手动停止", 100, 100, "WARNING");

            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("停止安装失败: {}", e.getMessage(), e);
            return ReturnResult.illegal("停止安装失败: " + e.getMessage());
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
        if (softService.getSoftServiceInstall() == null || softService.getSoftServiceInstall().isEmpty()) {
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
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 如果正在安装中，则返回错误
        if (install.getInstallStatus() == 1) {
            return ReturnResult.illegal("该软件正在安装中，请先停止安装");
        }

        // 获取软件服务信息
        SoftService softService = softServiceService.getById(install.getSoftServiceId());
        if (softService == null) {
            return ReturnResult.illegal("软件服务不存在");
        }

        // 获取卸载命令
        String uninstallCommand = install.getInstallUninstallCommand();
        if (uninstallCommand == null || uninstallCommand.isEmpty()) {
            // 如果安装记录中没有卸载命令，则使用软件服务中的卸载命令
            uninstallCommand = softService.getSoftServiceUninstall();
            if (uninstallCommand == null || uninstallCommand.isEmpty()) {
                return ReturnResult.illegal("卸载命令不存在");
            }
        }

        // 推送卸载开始通知
        sendInstallProgress(installId, "开始卸载 " + softService.getSoftServiceName(), 0, 100, "INFO");

        // 异步执行卸载
        String finalUninstallCommand = uninstallCommand;
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.getClient(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(installId, "SSH连接成功，准备执行卸载命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_uninstall", "uninstall");

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
                    // 执行卸载命令
                    channel.execute(finalUninstallCommand, 300000); // 5分钟超时

                    // 更新安装状态为未安装
                    SoftServiceInstall updateInstall = new SoftServiceInstall();
                    updateInstall.setInstallId(installId);
                    updateInstall.setInstallStatus(0); // 未安装
                    updateInstall.setInstallRunStatus(0); // 未运行
                    this.updateById(updateInstall);

                    // 推送卸载完成通知
                    sendInstallProgress(installId, "卸载完成：" + softService.getSoftServiceName(), 100, 100, "SUCCESS");
                } catch (Exception e) {
                    log.error("卸载软件失败: {}", e.getMessage(), e);

                    // 推送卸载失败通知
                    sendInstallProgress(installId, "卸载失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("卸载软件失败: {}", e.getMessage(), e);

                // 推送卸载失败通知
                sendInstallProgress(installId, "卸载失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> startService(Integer installId) {
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查安装状态
        if (install.getInstallStatus() != 2) {
            return ReturnResult.illegal("该软件未安装成功，无法启动");
        }

        // 如果已经是运行中状态，则返回错误
        if (install.getInstallRunStatus() != null && install.getInstallRunStatus() == 1) {
            return ReturnResult.illegal("该软件已在运行中");
        }

        // 获取软件服务信息
        SoftService softService = softServiceService.getById(install.getSoftServiceId());
        if (softService == null) {
            return ReturnResult.illegal("软件服务不存在");
        }

        // 获取启动命令
        String startCommand = install.getInstallStartCommand();
        if (startCommand == null || startCommand.isEmpty()) {
            // 如果安装记录中没有启动命令，则使用软件服务中的启动命令
            startCommand = softService.getSoftServiceStart();
            if (startCommand == null || startCommand.isEmpty()) {
                return ReturnResult.illegal("启动命令不存在");
            }
        }

        // 推送启动开始通知
        sendInstallProgress(installId, "开始启动 " + softService.getSoftServiceName(), 0, 100, "INFO");

        // 异步执行启动
        final String finalStartCommand = startCommand;
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.getClient(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(installId, "SSH连接成功，准备执行启动命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_start", "start");

                // 设置监听器接收输出
                StringBuilder logBuilder = new StringBuilder();
                channel.setListener(s -> {
                    logBuilder.append(s).append("\n");

                    // 推送启动进度
                    sendInstallProgress(installId, s, 50, 100, "INFO");
                });

                try {
                    // 执行启动命令
                    channel.execute(finalStartCommand, 60000); // 1分钟超时

                    // 更新运行状态为运行中
                    SoftServiceInstall updateInstall = new SoftServiceInstall();
                    updateInstall.setInstallId(installId);
                    updateInstall.setInstallRunStatus(1); // 运行中
                    updateInstall.setInstallLastStartTime(System.currentTimeMillis());
                    this.updateById(updateInstall);

                    // 推送启动完成通知
                    sendInstallProgress(installId, "启动完成：" + softService.getSoftServiceName(), 100, 100, "SUCCESS");
                } catch (Exception e) {
                    log.error("启动服务失败: {}", e.getMessage(), e);

                    // 推送启动失败通知
                    sendInstallProgress(installId, "启动失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("启动服务失败: {}", e.getMessage(), e);

                // 推送启动失败通知
                sendInstallProgress(installId, "启动失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> stopService(Integer installId) {
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查安装状态
        if (install.getInstallStatus() != 2) {
            return ReturnResult.illegal("该软件未安装成功，无法停止");
        }

        // 如果已经是未运行状态，则返回错误
        if (install.getInstallRunStatus() == null || install.getInstallRunStatus() == 0) {
            return ReturnResult.illegal("该软件未在运行中");
        }

        // 获取软件服务信息
        SoftService softService = softServiceService.getById(install.getSoftServiceId());
        if (softService == null) {
            return ReturnResult.illegal("软件服务不存在");
        }

        // 获取停止命令
        String stopCommand = install.getInstallStopCommand();
        if (stopCommand == null || stopCommand.isEmpty()) {
            // 如果安装记录中没有停止命令，则使用软件服务中的停止命令
            stopCommand = softService.getSoftServiceStop();
            if (stopCommand == null || stopCommand.isEmpty()) {
                return ReturnResult.illegal("停止命令不存在");
            }
        }

        // 推送停止开始通知
        sendInstallProgress(installId, "开始停止 " + softService.getSoftServiceName(), 0, 100, "INFO");

        // 异步执行停止
        final String finalStopCommand = stopCommand;
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.getClient(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(installId, "SSH连接成功，准备执行停止命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_stop", "stop");

                // 设置监听器接收输出
                StringBuilder logBuilder = new StringBuilder();
                channel.setListener(s -> {
                    logBuilder.append(s).append("\n");

                    // 推送停止进度
                    sendInstallProgress(installId, s, 50, 100, "INFO");
                });

                try {
                    // 执行停止命令
                    channel.execute(finalStopCommand, 60000); // 1分钟超时

                    // 更新运行状态为未运行
                    SoftServiceInstall updateInstall = new SoftServiceInstall();
                    updateInstall.setInstallId(installId);
                    updateInstall.setInstallRunStatus(0); // 未运行
                    updateInstall.setInstallLastStopTime(System.currentTimeMillis());
                    this.updateById(updateInstall);

                    // 推送停止完成通知
                    sendInstallProgress(installId, "停止完成：" + softService.getSoftServiceName(), 100, 100, "SUCCESS");
                } catch (Exception e) {
                    log.error("停止服务失败: {}", e.getMessage(), e);

                    // 推送停止失败通知
                    sendInstallProgress(installId, "停止失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("停止服务失败: {}", e.getMessage(), e);

                // 推送停止失败通知
                sendInstallProgress(installId, "停止失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> restartService(Integer installId) {
        SoftServiceInstall install = this.getById(installId);
        if (install == null) {
            return ReturnResult.illegal("安装记录不存在");
        }

        // 检查安装状态
        if (install.getInstallStatus() != 2) {
            return ReturnResult.illegal("该软件未安装成功，无法重启");
        }

        // 获取软件服务信息
        SoftService softService = softServiceService.getById(install.getSoftServiceId());
        if (softService == null) {
            return ReturnResult.illegal("软件服务不存在");
        }

        // 获取重启命令
        String restartCommand = install.getInstallRestartCommand();
        if (restartCommand == null || restartCommand.isEmpty()) {
            // 如果安装记录中没有重启命令，则使用软件服务中的重启命令
            restartCommand = softService.getSoftServiceRestart();
            if (restartCommand == null || restartCommand.isEmpty()) {
                // 如果没有重启命令，则尝试停止后启动
                return stopThenStart(installId);
            }
        }

        // 推送重启开始通知
        sendInstallProgress(installId, "开始重启 " + softService.getSoftServiceName(), 0, 100, "INFO");

        // 异步执行重启
        final String finalRestartCommand = restartCommand;
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.getClient(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                sendInstallProgress(installId, "SSH连接成功，准备执行重启命令", 10, 100, "INFO");

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_restart", "restart");

                // 设置监听器接收输出
                StringBuilder logBuilder = new StringBuilder();
                channel.setListener(s -> {
                    logBuilder.append(s).append("\n");

                    // 推送重启进度
                    sendInstallProgress(installId, s, 50, 100, "INFO");
                });

                try {
                    // 执行重启命令
                    channel.execute(finalRestartCommand, 120000); // 2分钟超时

                    // 更新运行状态为运行中
                    SoftServiceInstall updateInstall = new SoftServiceInstall();
                    updateInstall.setInstallId(installId);
                    updateInstall.setInstallRunStatus(1); // 运行中
                    updateInstall.setInstallLastStartTime(System.currentTimeMillis());
                    this.updateById(updateInstall);

                    // 推送重启完成通知
                    sendInstallProgress(installId, "重启完成：" + softService.getSoftServiceName(), 100, 100, "SUCCESS");
                } catch (Exception e) {
                    log.error("重启服务失败: {}", e.getMessage(), e);

                    // 推送重启失败通知
                    sendInstallProgress(installId, "重启失败：" + e.getMessage(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("重启服务失败: {}", e.getMessage(), e);

                // 推送重启失败通知
                sendInstallProgress(installId, "重启失败：" + e.getMessage(), 100, 100, "ERROR");
            }
        });

        return ReturnResult.success(true);
    }

    /**
     * 先停止再启动服务
     *
     * @param installId 安装记录ID
     * @return 操作结果
     */
    private ReturnResult<Boolean> stopThenStart(Integer installId) {
        // 推送重启开始通知
        sendInstallProgress(installId, "开始重启服务（先停止再启动）", 0, 100, "INFO");

        // 异步执行停止后启动
        CompletableFuture.runAsync(() -> {
            try {
                // 先停止服务
                ReturnResult<Boolean> stopResult = stopService(installId);
                if (!stopResult.isOk()) {
                    sendInstallProgress(installId, "停止服务失败：" + stopResult.getMsg(), 100, 100, "ERROR");
                    return;
                }

                // 等待一段时间
                Thread.sleep(2000);

                // 再启动服务
                ReturnResult<Boolean> startResult = startService(installId);
                if (!startResult.isOk()) {
                    sendInstallProgress(installId, "启动服务失败：" + startResult.getMsg(), 100, 100, "ERROR");
                }
            } catch (Exception e) {
                log.error("重启服务失败: {}", e.getMessage(), e);
                sendInstallProgress(installId, "重启服务失败：" + e.getMessage(), 100, 100, "ERROR");
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
     */
    private void updateInstallStatus(Integer installId, Integer status, String log) {
        SoftServiceInstall updateInstall = new SoftServiceInstall();
        updateInstall.setInstallId(installId);
        updateInstall.setInstallStatus(status);
        updateInstall.setInstallLog(log);
        this.updateById(updateInstall);
    }

    /**
     * 发送安装进度消息
     *
     * @param installId 安装ID
     * @param message 消息内容
     * @param step 当前步骤
     * @param total 总步骤
     * @param type 消息类型：INFO, ERROR, SUCCESS, WARNING
     */
    private void sendInstallProgress(Integer installId, String message, int step, int total, String type) {
        if (socketSessionTemplate == null) {
            log.warn("socketSessionTemplate为空，无法发送安装进度通知");
            return;
        }

        try {
            // 创建消息事件
            MsgEvent msgEvent = MsgEvent.builder()
                    .name("install_progress")
                    .eventId(installId.toString())
                    .step(step)
                    .total(total)
                    .msg(message)
                    .type(type)
                    .module("soft-service-install")
                    .build();

            // 发送到指定的topic
            socketSessionTemplate.send("/topic/install/" + installId, msgEvent);

            // 同时发送到通用的安装进度频道
            socketSessionTemplate.send("/topic/install", msgEvent);

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
            SshClient sshClient = terminalService.getClient(install.getSshId());
            if (sshClient == null) {
                return ReturnResult.illegal("无法连接到SSH服务器");
            }

            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            Channel channel = clientSession.openChannel(installId + "_viewlog", "viewlog");

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
        sendInstallProgress(installId, "开始监控日志", 0, 100, "INFO");

        // 异步执行监控
        CompletableFuture.runAsync(() -> {
            try {
                // 获取SSH客户端
                SshClient sshClient = terminalService.getClient(install.getSshId());
                if (sshClient == null) {
                    sendInstallProgress(installId, "无法连接到SSH服务器", 100, 100, "ERROR");
                    return;
                }

                ChannelSession clientSession = (ChannelSession) sshClient.getSession();
                Channel channel = clientSession.openChannel(installId + "_monitorlog", "monitorlog");

                // 设置监听器接收输出
                channel.setListener(s -> {
                    // 推送日志内容
                    sendServiceLog(installId, s);
                });

                // 执行监控日志命令
                String command = "tail -f " + logPath;
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
            SshClient sshClient = terminalService.getClient(install.getSshId());
            if (sshClient == null) {
                return ReturnResult.illegal("无法连接到SSH服务器");
            }

            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            // 关闭通道
            clientSession.closeChannel(installId + "_monitorlog");

            // 推送监控停止通知
            sendInstallProgress(installId, "停止监控日志", 100, 100, "INFO");

            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("停止监控服务日志失败: {}", e.getMessage(), e);
            return ReturnResult.illegal("停止监控服务日志失败: " + e.getMessage());
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
                    .type("INFO")
                    .module("soft-service-log")
                    .build();

            // 发送到指定的topic
            socketSessionTemplate.send("/topic/service/log/" + installId, msgEvent);

        } catch (Exception e) {
            log.error("发送服务日志通知失败: {}", e.getMessage(), e);
        }
    }
} 