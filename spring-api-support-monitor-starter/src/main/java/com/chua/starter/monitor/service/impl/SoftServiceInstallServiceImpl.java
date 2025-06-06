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
        if (softServiceInstall.getRunStatus() == null) {
            softServiceInstall.setRunStatus(0); // 未运行
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
            softServiceInstall.setRunStatus(0); // 未运行
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
} 