package com.chua.starter.monitor.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.ssh.support.ssh.CmdChannel;
import com.chua.ssh.support.ssh.SshChannelSession;
import com.chua.sshj.support.ssh.SshjClient;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.monitor.entity.SoftServiceInstall;
import com.chua.starter.monitor.jpom.func.assets.model.MachineSshModel;
import com.chua.starter.monitor.jpom.func.assets.server.MachineSshServer;
import com.chua.starter.monitor.mapper.SoftServiceInstallMapper;
import com.chua.starter.monitor.mapper.SoftServiceMapper;
import com.chua.starter.monitor.service.impl.SoftServiceInstallServiceImpl;
import com.chua.starter.monitor.starter.service.TerminalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SSH状态检查任务
 * 定期检测所有SSH服务器是否在线，并更新状态到数据库
 * 同时检查已安装服务的运行状态
 *
 * @author CH
 * @since 2024/7/19
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SshStatusCheckTask {

    private final MachineSshServer machineSshServer;
    private final TerminalService terminalService;
    private final SoftServiceInstallMapper softServiceInstallMapper;
    private final SoftServiceMapper softServiceMapper;
    private final SoftServiceInstallServiceImpl softServiceInstallService;

    /**
     * 缓存SSH连接状态，避免频繁更新数据库
     * key: sshId, value: 上次状态(true:在线, false:离线)
     */
    private final Map<String, Boolean> sshStatusCache = new ConcurrentHashMap<>();

    /**
     * 缓存服务运行状态，避免频繁更新数据库
     * key: installId, value: 上次状态(true:运行中, false:未运行)
     */
    private final Map<Integer, Boolean> serviceStatusCache = new ConcurrentHashMap<>();

    /**
     * 每分钟执行一次SSH状态检测
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void checkSshStatus() {
        log.info("开始执行SSH状态检测任务");

        try {
            // 获取所有SSH服务器
            List<MachineSshModel> sshList = machineSshServer.list();
            if (sshList == null || sshList.isEmpty()) {
                log.info("没有找到SSH服务器配置");
                return;
            }

            log.info("共检测到{}个SSH服务器", sshList.size());

            // 记录当前所有SSH的ID
            Map<String, MachineSshModel> currentSshMap = new HashMap<>();

            // 检测每个SSH服务器状态
            for (MachineSshModel ssh : sshList) {
                String sshId = ssh.getId();
                currentSshMap.put(sshId, ssh);

                // 检测单个SSH状态
                checkSingleSsh(ssh);
            }

            // 清理缓存中不存在的SSH
            sshStatusCache.keySet().removeIf(key -> !currentSshMap.containsKey(key));

        } catch (Exception e) {
            log.error("执行SSH状态检测任务异常", e);
        }
    }

    /**
     * 检测单个SSH服务器状态
     *
     * @param ssh SSH服务器配置
     */
    private void checkSingleSsh(MachineSshModel ssh) {
        String sshId = ssh.getId();

        try {
            // 尝试连接SSH
            SshjClient sshClient = terminalService.createClientBySshId(sshId);
            boolean isOnline = (sshClient != null && sshClient.getSession() != null);

            // 获取上次状态
            Boolean lastStatus = sshStatusCache.get(sshId);

            // 如果状态发生变化或者是首次检测，则更新数据库
            if (lastStatus == null || lastStatus != isOnline) {
                updateSshStatus(ssh, isOnline);

                // 更新缓存
                sshStatusCache.put(sshId, isOnline);

                log.info("SSH服务器[{}]({})状态变更为: {}",
                        ssh.getName(), sshId, isOnline ? "在线" : "离线");

                // 如果SSH从离线变为在线，检查该服务器上的所有服务状态
                if (isOnline && (lastStatus != null && !lastStatus)) {
                    checkServicesOnSsh(sshId, sshClient);
                }
            }

        } catch (Exception e) {
            log.error("检测SSH服务器[{}]({})状态异常: {}",
                    ssh.getName(), sshId, e.getMessage());

            // 发生异常，认为SSH离线
            if (Boolean.TRUE.equals(sshStatusCache.get(sshId))) {
                updateSshStatus(ssh, false);
                sshStatusCache.put(sshId, false);
            }
        }
    }

    /**
     * 更新SSH状态到数据库
     *
     * @param ssh      SSH服务器配置
     * @param isOnline 是否在线
     */
    private void updateSshStatus(MachineSshModel ssh, boolean isOnline) {
        try {
            // 更新SSH状态
            ssh.setStatus(isOnline ? 1 : 0);
            machineSshServer.updateById(ssh);

            log.info("已更新SSH服务器[{}]({})状态为: {}",
                    ssh.getName(), ssh.getId(), isOnline ? "在线" : "离线");

        } catch (Exception e) {
            log.error("更新SSH服务器[{}]({})状态失败: {}",
                    ssh.getName(), ssh.getId(), e.getMessage());
        }
    }

    /**
     * 检查指定SSH服务器上的所有服务状态
     *
     * @param sshId     SSH服务器ID
     * @param sshClient SSH客户端
     */
    private void checkServicesOnSsh(String sshId, SshjClient sshClient) {
        try {
            // 查询该SSH服务器上已安装的所有服务
            List<SoftServiceInstall> installList = softServiceInstallMapper.selectBySshId(sshId);

            if (installList == null || installList.isEmpty()) {
                log.info("SSH服务器[{}]上没有已安装的服务", sshId);
                return;
            }

            // 获取所有相关的软件服务ID
            List<Integer> softServiceIds = installList.stream()
                    .map(SoftServiceInstall::getSoftServiceId)
                    .collect(Collectors.toList());

            // 查询所有相关的软件服务
            List<SoftService> softServiceList = softServiceMapper.selectList(
                    Wrappers.lambdaQuery(SoftService.class)
                            .in(SoftService::getSoftServiceId, softServiceIds)
                            .isNotNull(SoftService::getSoftServiceStatusCheckCommand)
            );

            if (softServiceList == null || softServiceList.isEmpty()) {
                log.info("没有找到配置了状态检查命令的软件服务信息");
                return;
            }

            // 创建软件服务ID到软件服务的映射
            Map<Integer, SoftService> softServiceMap = softServiceList.stream()
                    .collect(Collectors.toMap(SoftService::getSoftServiceId, service -> service));

            log.info("SSH服务器[{}]上共有{}个已安装服务，开始检查服务状态",
                    sshId, installList.size());

            // 检查每个服务的状态
            for (SoftServiceInstall install : installList) {
                SoftService softService = softServiceMap.get(install.getSoftServiceId());
                if (softService != null) {
                    checkServiceStatus(install, softService, sshClient);
                }
            }

        } catch (Exception e) {
            log.error("检查SSH服务器[{}]上的服务状态异常: {}", sshId, e.getMessage());
        }
    }

    /**
     * 检查服务状态
     *
     * @param install     服务安装记录
     * @param softService 软件服务
     * @param sshClient   SSH客户端
     */
    private void checkServiceStatus(SoftServiceInstall install, SoftService softService, SshjClient sshClient) {
        // 调用ServiceStatusCheckTask中的逻辑检查服务状态
        // 这里简化实现，实际项目中可以考虑将ServiceStatusCheckTask中的核心逻辑抽取出来复用

        Integer installId = install.getInstallId();
        String checkCommand = install.getSoftServiceStatusCheckCommand();

        if (checkCommand == null || checkCommand.trim().isEmpty()) {
            log.debug("服务[{}]未配置状态检查命令，标记为未知状态", installId);
            // 更新为未知状态
            updateServiceStatusUnknown(install);
            return;
        }

        try {
            // 执行状态检查命令
            SshChannelSession session = (SshChannelSession) sshClient.getSession();
            CmdChannel openChannel = (CmdChannel) session.openChannel("exec");
            String result = openChannel.execute(checkCommand, 10000);

            // 判断服务是否运行中
            boolean isRunning = isServiceRunning(result, install.getSoftServiceStatusCheckSuccessFlag());

            // 获取上次状态
            Boolean lastStatus = serviceStatusCache.get(installId);

            // 如果状态发生变化或者是首次检测，则更新数据库
            if (lastStatus == null || lastStatus != isRunning) {
                updateServiceStatus(install, isRunning);

                // 更新缓存
                serviceStatusCache.put(installId, isRunning);

                log.info("服务[{}]状态变更为: {}", installId, isRunning ? "运行中" : "未运行");
            }
        } catch (Exception e) {
            log.error("检查服务[{}]状态异常: {}", installId, e.getMessage());
        }
    }

    /**
     * 判断服务是否运行中
     *
     * @param result      命令执行结果
     * @param successFlag 成功标识
     * @return 是否运行中
     */
    private boolean isServiceRunning(String result, String successFlag) {
        // 如果没有配置成功标识，则根据结果是否为空判断
        if (successFlag == null || successFlag.trim().isEmpty()) {
            return result != null && !result.trim().isEmpty()
                    && !result.contains("not found") && !result.contains("No such file");
        }

        // 检查结果中是否包含成功标识
        return result != null && result.contains(successFlag);
    }

    /**
     * 更新服务运行状态
     *
     * @param install   服务安装记录
     * @param isRunning 是否运行中
     */
    private void updateServiceStatus(SoftServiceInstall install, boolean isRunning) {
        try {
            // 更新服务运行状态
            SoftServiceInstall updateInstall = new SoftServiceInstall();
            updateInstall.setInstallId(install.getInstallId());
            updateInstall.setInstallRunStatus(isRunning ? 1 : 0);

            // 如果是启动状态，更新最后启动时间；如果是停止状态，更新最后停止时间
            if (isRunning) {
                updateInstall.setInstallLastStartTime(System.currentTimeMillis());
            } else {
                updateInstall.setInstallLastStopTime(System.currentTimeMillis());
            }

            // 更新最后检查时间
            updateInstall.setInstallLastCheckTime(System.currentTimeMillis());

            // 更新到数据库
            softServiceInstallMapper.updateById(updateInstall);

            // 发送状态变更通知
            softServiceInstallService.sendInstallProgress(
                    install,
                    "服务状态检查: " + (isRunning ? "服务正在运行" : "服务未运行"),
                    100,
                    100,
                    isRunning ? "SUCCESS" : "WARNING"
            );

        } catch (Exception e) {
            log.error("更新服务[{}]运行状态失败: {}",
                    install.getInstallId(), e.getMessage());
        }
    }

    /**
     * 更新服务为未知状态
     *
     * @param install 服务安装记录
     */
    private void updateServiceStatusUnknown(SoftServiceInstall install) {
        try {
            // 更新服务为未知状态（使用特殊状态码2表示未知状态）
            SoftServiceInstall updateInstall = new SoftServiceInstall();
            updateInstall.setInstallId(install.getInstallId());
            updateInstall.setInstallRunStatus(2); // 2表示未知状态
            updateInstall.setInstallLastCheckTime(System.currentTimeMillis());

            // 更新到数据库
            softServiceInstallMapper.updateById(updateInstall);

            // 更新缓存（使用null表示未知状态）
            serviceStatusCache.remove(install.getInstallId());

            // 发送状态变更通知
            softServiceInstallService.sendInstallProgress(
                    install,
                    "服务状态检查: 未配置检查命令，状态未知",
                    100,
                    100,
                    "WARNING"
            );

            log.info("已更新服务[{}]运行状态为: 未知状态", install.getInstallId());
        } catch (Exception e) {
            log.error("更新服务[{}]为未知状态失败: {}", install.getInstallId(), e.getMessage());
        }
    }
} 