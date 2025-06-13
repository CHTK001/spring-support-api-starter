package com.chua.starter.monitor.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.protocol.channel.Channel;
import com.chua.common.support.protocol.session.ChannelSession;
import com.chua.sshj.support.ssh.SshjClient;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.monitor.entity.SoftServiceInstall;
import com.chua.starter.monitor.mapper.SoftServiceInstallMapper;
import com.chua.starter.monitor.mapper.SoftServiceMapper;
import com.chua.starter.monitor.service.impl.SoftServiceInstallServiceImpl;
import com.chua.starter.monitor.starter.service.TerminalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 服务状态检查任务
 * 定期检查所有已安装服务的运行状态
 *
 * @author CH
 * @since 2024/7/19
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceStatusCheckTask {

    private final SoftServiceInstallMapper softServiceInstallMapper;
    private final SoftServiceMapper softServiceMapper;
    private final TerminalService terminalService;
    private final SoftServiceInstallServiceImpl softServiceInstallService;

    /**
     * 缓存服务运行状态，避免频繁更新数据库
     * key: installId, value: 上次状态(true:运行中, false:未运行)
     */
    private final ConcurrentHashMap<Integer, Boolean> serviceStatusCache = new ConcurrentHashMap<>();

    /**
     * 每2分钟执行一次服务状态检查
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void checkServiceStatus() {
        log.info("开始执行服务状态检查任务");

        try {
            // 查询所有已安装成功的服务
            List<SoftServiceInstall> installList = softServiceInstallMapper.selectList(
                    Wrappers.lambdaQuery(SoftServiceInstall.class)
                            .eq(SoftServiceInstall::getInstallStatus, 2) // 安装成功
            );

            if (installList == null || installList.isEmpty()) {
                log.info("没有找到需要检查状态的服务");
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
                log.info("没有找到配置了状态检查命令的服务");
                return;
            }

            // 创建软件服务ID到软件服务的映射
            Map<Integer, SoftService> softServiceMap = softServiceList.stream()
                    .collect(Collectors.toMap(SoftService::getSoftServiceId, service -> service));

            // 过滤出需要检查的安装记录
            List<SoftServiceInstall> checkList = installList.stream()
                    .filter(install -> softServiceMap.containsKey(install.getSoftServiceId()))
                    .collect(Collectors.toList());

            log.info("共检测到{}个需要检查状态的服务", checkList.size());

            // 遍历检查每个服务
            for (SoftServiceInstall install : checkList) {
                SoftService softService = softServiceMap.get(install.getSoftServiceId());
                checkSingleServiceStatus(install, softService);
            }
        } catch (Exception e) {
            log.error("执行服务状态检查任务异常", e);
        }
    }

    /**
     * 检查单个服务状态
     *
     * @param install     服务安装记录
     * @param softService 软件服务
     */
    private void checkSingleServiceStatus(SoftServiceInstall install, SoftService softService) {
        Integer installId = install.getInstallId();
        String sshId = install.getSshId();
        String checkCommand = install.getSoftServiceStatusCheckCommand();
        String successFlag = install.getSoftServiceStatusCheckSuccessFlag();

        if (StringUtils.isEmpty(checkCommand)) {
            log.debug("服务[{}]未配置状态检查命令，标记为未知状态", installId);
            // 更新为未知状态
            updateServiceStatusUnknown(install);
            return;
        }

        try {
            // 获取SSH客户端
            SshjClient sshClient = terminalService.createClientBySshId(sshId);
            if (sshClient == null) {
                log.warn("服务[{}]所在服务器SSH连接失败，无法检查服务状态", installId);
                return;
            }

            // 执行状态检查命令
            ChannelSession clientSession = (ChannelSession) sshClient.getSession();
            Channel channel = clientSession.openChannel(installId + "_status_check", "shell");

            // 执行命令并获取结果
            String result = executeStatusCheckCommand(channel, install);

            // 判断服务是否运行中
            boolean isRunning = isServiceRunning(result, successFlag);

            // 获取上次状态
            Boolean lastStatus = serviceStatusCache.get(installId);

            // 如果状态发生变化或者是首次检测，则更新数据库
            if (lastStatus == null || lastStatus != isRunning) {
                updateServiceStatus(install, isRunning);
                // 更新缓存
                serviceStatusCache.put(installId, isRunning);

                log.info("服务[{}]状态变更为: {}", installId, isRunning ? "运行中" : "未运行");
            }

            // 更新最后检查时间
            SoftServiceInstall updateInstall = new SoftServiceInstall();
            updateInstall.setInstallId(installId);
            updateInstall.setInstallLastCheckTime(System.currentTimeMillis());
            softServiceInstallMapper.updateById(updateInstall);

        } catch (Exception e) {
            log.error("检查服务[{}]状态异常: {}", installId, e.getMessage());
        }
    }

    /**
     * 执行状态检查命令
     *
     * @param channel SSH通道
     * @param install 服务安装记录
     * @return 执行结果
     */
    private String executeStatusCheckCommand(Channel channel, SoftServiceInstall install) throws Exception {
        String command = install.getSoftServiceStatusCheckCommand();
        Integer commandType = install.getSoftServiceStatusCheckCommandType();

        // 根据命令类型执行不同的命令
        if (commandType != null && commandType == 1) {
            // 脚本命令
            return executeScriptCommand(channel, command);
        } else {
            // 单行命令
            return channel.execute(command, 10000); // 10秒超时
        }
    }

    /**
     * 执行脚本命令
     *
     * @param channel SSH通道
     * @param script  脚本内容
     * @return 执行结果
     */
    private String executeScriptCommand(Channel channel, String script) throws Exception {
        // 创建临时脚本文件
        String tempScriptName = "temp_check_script_" + System.currentTimeMillis() + ".sh";

        try {
            // 上传脚本到远程服务器
            channel.execute("cat > /tmp/" + tempScriptName + " << 'EOL'\n" + script + "\nEOL", 5000);

            // 添加执行权限
            channel.execute("chmod +x /tmp/" + tempScriptName, 5000);

            // 执行脚本
            String result = channel.execute("/bin/bash /tmp/" + tempScriptName, 10000);

            // 删除临时脚本
            channel.execute("rm -f /tmp/" + tempScriptName, 5000);

            return result;
        } catch (Exception e) {
            // 尝试清理临时文件
            try {
                channel.execute("rm -f /tmp/" + tempScriptName, 5000);
            } catch (Exception ex) {
                // 忽略清理失败的异常
            }
            throw e;
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
        if (StringUtils.isEmpty(successFlag)) {
            return StringUtils.isNotEmpty(result) && !result.contains("not found") && !result.contains("No such file");
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

            log.info("已更新服务[{}]运行状态为: {}",
                    install.getInstallId(),
                    isRunning ? "运行中" : "未运行");
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