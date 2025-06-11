package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnCode;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.ServerInfo;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.monitor.entity.SoftServiceInstall;
import com.chua.starter.monitor.entity.SoftServiceVersion;
import com.chua.starter.monitor.jpom.func.assets.model.MachineSshModel;
import com.chua.starter.monitor.jpom.func.assets.server.MachineSshServer;
import com.chua.starter.monitor.mapper.SoftServiceInstallMapper;
import com.chua.starter.monitor.mapper.SoftServiceMapper;
import com.chua.starter.monitor.service.SoftServiceService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.github.yulichang.toolkit.MPJWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 软件服务服务实现类
 *
 * @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SoftServiceServiceImpl extends ServiceImpl<SoftServiceMapper, SoftService> implements SoftServiceService {

    private final SoftServiceInstallMapper softServiceInstallMapper;
    private final MachineSshServer machineSshServer;

    @Override
    public IPage<SoftService> pageFor(Query<SoftService> query, SoftService softService) {
        IPage<SoftService> page;
        
        if (null == softService.getSshId()) {
            page = this.page(query.createFullPage(), MPJWrappers.lambdaJoin(SoftService.class)
                    .selectAll(SoftService.class)
                    .like(StringUtils.isNotBlank(softService.getSoftServiceCategory()), SoftService::getSoftServiceCategory, softService.getSoftServiceCategory())
                    .like(StringUtils.isNotBlank(softService.getSoftServiceName()), SoftService::getSoftServiceName, softService.getSoftServiceName())
                    .like(StringUtils.isNotBlank(softService.getSoftServiceOs()), SoftService::getSoftServiceOs, softService.getSoftServiceOs())
                    .orderByDesc(query.isNewest(), SoftService::getCreateTime)
                    .orderByDesc(query.isPopular(), SoftService::getInstallCount)
            );
        } else {
            page = this.page(query.createFullPage(),
                    query.mpjLambda()
                            .selectAll(SoftService.class)
                            .leftJoin(SoftServiceInstall.class, SoftServiceInstall::getSoftServiceId, SoftService::getSoftServiceId)
                            .like(StringUtils.isNotBlank(softService.getSoftServiceCategory()), SoftService::getSoftServiceCategory, softService.getSoftServiceCategory())
                            .like(StringUtils.isNotBlank(softService.getSoftServiceName()), SoftService::getSoftServiceName, softService.getSoftServiceName())
                            .like(StringUtils.isNotBlank(softService.getSoftServiceOs()), SoftService::getSoftServiceOs, softService.getSoftServiceOs())
                            .eq(SoftServiceInstall::getSshId, softService.getSshId())
                            .orderByDesc(query.isNewest(), SoftService::getCreateTime)
                            .orderByDesc(query.isPopular(), SoftService::getInstallCount)
            );
        }

        // 填充已安装服务器信息
        fillInstalledServersInfo(page.getRecords());

        // 按软件名称分组并处理版本信息
        List<SoftService> groupedServices = groupServicesByName(page.getRecords());

        // 替换原始记录列表
        page.setRecords(groupedServices);

        return page;
    }

    /**
     * 按软件名称分组并处理版本信息
     *
     * @param originalServices 原始软件服务列表
     * @return 分组后的软件服务列表
     */
    @Override
    public List<SoftService> groupServicesByName(List<SoftService> originalServices) {
        if (originalServices == null || originalServices.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 按软件名称分组
            Map<String, List<SoftService>> serviceGroups = originalServices.stream()
                    .collect(Collectors.groupingBy(SoftService::getSoftServiceName));

            List<SoftService> result = new ArrayList<>();

            // 处理每个分组
            for (Map.Entry<String, List<SoftService>> entry : serviceGroups.entrySet()) {
                List<SoftService> services = entry.getValue();

                if (services.size() == 1) {
                    // 只有一个版本，直接添加到结果中
                    SoftService service = services.get(0);
                    service.setIsGroupHeader(true);
                    service.setVersions(Collections.singletonList(
                            SoftServiceVersion.builder()
                                    .softServiceId(service.getSoftServiceId())
                                    .version(service.getSoftServiceVersion())
                                    .downloadUrl(service.getSoftServiceDownloadUrl())
                                    .isCurrent(true)
                                    .isInstallable(StringUtils.isNotBlank(service.getSoftServiceInstallCommand()))
                                    .installCommand(service.getSoftServiceInstallCommand())
                                    .releaseTime(service.getCreateTime())
                                    .build()
                    ));
                    result.add(service);
                } else {
                    // 多个版本，创建分组头
                    services.sort(Comparator.comparing(SoftService::getCreateTime).reversed());
                    SoftService groupHeader = services.get(0);
                    groupHeader.setIsGroupHeader(true);

                    // 创建版本列表
                    List<SoftServiceVersion> versions = new ArrayList<>();
                    for (SoftService service : services) {
                        versions.add(
                                SoftServiceVersion.builder()
                                        .softServiceId(service.getSoftServiceId())
                                        .version(service.getSoftServiceVersion())
                                        .downloadUrl(service.getSoftServiceDownloadUrl())
                                        .releaseTime(service.getCreateTime())
                                        .versionDesc(service.getSoftServiceRemark())
                                        .isCurrent(service.equals(groupHeader))
                                        .isInstallable(StringUtils.isNotBlank(service.getSoftServiceInstallCommand()))
                                        .installCommand(service.getSoftServiceInstallCommand())
                                        .build()
                        );
                    }

                    groupHeader.setVersions(versions);
                    result.add(groupHeader);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("按软件名称分组处理失败: {}", e.getMessage(), e);
            return originalServices;
        }
    }

    /**
     * 填充已安装服务器信息
     *
     * @param softServices 软件服务列表
     */
    private void fillInstalledServersInfo(List<SoftService> softServices) {
        if (softServices == null || softServices.isEmpty()) {
            return;
        }

        try {
            // 获取所有软件服务ID
            List<Integer> softServiceIds = softServices.stream()
                    .map(SoftService::getSoftServiceId)
                    .toList();

            // 批量查询安装记录
            List<SoftServiceInstall> allInstalls = new ArrayList<>(softServiceInstallMapper.selectList(
                    Wrappers.lambdaQuery(SoftServiceInstall.class)
                            .in(SoftServiceInstall::getSoftServiceId, softServiceIds)
            ));

            // 收集所有的SSH ID
            Set<String> sshIds = allInstalls.stream()
                    .map(SoftServiceInstall::getSshId)
                    .collect(Collectors.toSet());

            // 查询所有服务器信息
            Map<String, MachineSshModel> sshModelMap = new HashMap<>();
            for (String sshId : sshIds) {
                try {
                    MachineSshModel sshModel = machineSshServer.getByKey(sshId);
                    if (sshModel != null) {
                        sshModelMap.put(sshId, sshModel);
                    }
                } catch (Exception e) {
                    log.warn("获取SSH服务器信息失败: {}, {}", sshId, e.getMessage());
                }
            }

            // 按软件服务ID分组
            Map<Integer, List<SoftServiceInstall>> installMap = allInstalls.stream()
                    .collect(Collectors.groupingBy(SoftServiceInstall::getSoftServiceId));

            // 填充已安装服务器信息
            for (SoftService softService : softServices) {
                List<SoftServiceInstall> installs = installMap.get(softService.getSoftServiceId());
                if (installs != null && !installs.isEmpty()) {
                    List<ServerInfo> installedServers = new ArrayList<>();

                    for (SoftServiceInstall install : installs) {
                        String sshId = install.getSshId();

                        // 使用Builder模式创建ServerInfo对象
                        ServerInfo serverInfo = ServerInfo.builder()
                                .sshId(sshId)
                                .installId(install.getInstallId())
                                .installStatus(install.getInstallStatus())
                                .installStatusDesc(getInstallStatusDesc(install.getInstallStatus()))
                                .runStatus(install.getInstallRunStatus())
                                .runStatusDesc(getRunStatusDesc(install.getInstallRunStatus()))
                                .installTime(install.getInstallTime())
                                .installPath(install.getInstallPath())
                                .installVersion(install.getInstallVersion())
                                .build();

                        // 添加服务器名称和主机信息
                        MachineSshModel sshModel = sshModelMap.get(sshId);
                        if (sshModel != null) {
                            serverInfo.setServerName(sshModel.getName());
                            serverInfo.setServerHost(sshModel.getHost());
                            serverInfo.setHostName(sshModel.getHostName());
                            serverInfo.setOsName(sshModel.getOsName());
                        }

                        installedServers.add(serverInfo);
                    }

                    softService.setInstalledServers(installedServers);
                }
            }
        } catch (Exception e) {
            log.error("填充已安装服务器信息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取安装状态描述
     *
     * @param status 安装状态码
     * @return 安装状态描述
     */
    private String getInstallStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }

        switch (status) {
            case 0:
                return "未安装";
            case 1:
                return "安装中";
            case 2:
                return "已安装";
            case 3:
                return "安装失败";
            default:
                return "未知";
        }
    }

    /**
     * 获取运行状态描述
     *
     * @param status 运行状态码
     * @return 运行状态描述
     */
    private String getRunStatusDesc(Integer status) {
        if (status == null) {
            return "未运行";
        }

        switch (status) {
            case 0:
                return "未运行";
            case 1:
                return "运行中";
            default:
                return "未知";
        }
    }

    /**
     * 收藏服务
     *
     * @param softServiceId 软件服务ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    @Override
    public ReturnResult<Boolean> favoriteService(Integer softServiceId) {
        // 获取软件服务
        SoftService softService = this.getById(softServiceId);
        if (softService == null) {
            return ReturnResult.illegal("软件服务不存在");
        }

        try {
            // 更新收藏次数
            SoftService updateService = new SoftService();
            updateService.setSoftServiceId(softServiceId);

            // 如果收藏次数为空，则设置为1，否则加1
            Integer favoriteCount = softService.getFavoriteCount();
            if (favoriteCount == null) {
                favoriteCount = 1;
            } else {
                favoriteCount += 1;
            }
            updateService.setFavoriteCount(favoriteCount);

            boolean result = this.updateById(updateService);
            return ReturnResult.preconditioning(result).toResult();
        } catch (Exception e) {
            log.error("收藏服务失败: {}", e.getMessage(), e);
            return ReturnResult.failure(ReturnCode.REQUEST_PARAM_ERROR, "收藏服务失败: " + e.getMessage());
        }
    }

    /**
     * 增加安装次数
     *
     * @param softServiceId 软件服务ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    @Override
    public ReturnResult<Boolean> incrementInstallCount(Integer softServiceId) {
        // 获取软件服务
        SoftService softService = this.getById(softServiceId);
        if (softService == null) {
            return ReturnResult.illegal("软件服务不存在");
        }

        try {
            // 更新安装次数
            SoftService updateService = new SoftService();
            updateService.setSoftServiceId(softServiceId);

            // 如果安装次数为空，则设置为1，否则加1
            Integer installCount = softService.getInstallCount();
            if (installCount == null) {
                installCount = 1;
            } else {
                installCount += 1;
            }
            updateService.setInstallCount(installCount);

            boolean result = this.updateById(updateService);
            return ReturnResult.preconditioning(result).toResult();
        } catch (Exception e) {
            log.error("增加安装次数失败: {}", e.getMessage(), e);
            return ReturnResult.failure(ReturnCode.REQUEST_PARAM_ERROR, "增加安装次数失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有支持的操作系统
     *
     * @return 操作系统列表
     */
    @Override
    public List<String> listAllOs() {
        try {
            // 查询所有不同的操作系统
            List<SoftService> list = this.lambdaQuery()
                    .select(SoftService::getSoftServiceOs)
                    .groupBy(SoftService::getSoftServiceOs)
                    .list();

            // 过滤掉空值并返回结果
            return list.stream()
                    .map(SoftService::getSoftServiceOs)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取操作系统列表失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 在保存或更新前自动检测并设置命令类型
     *
     * @param softService 软件服务
     */
    private void detectAndSetCommandTypes(SoftService softService) {
        // 检测安装命令类型
        if (StringUtils.isNotBlank(softService.getSoftServiceInstallCommand())) {
            softService.setSoftServiceInstallCommandType(
                    isScriptCommand(softService.getSoftServiceInstallCommand()) ? 1 : 0
            );
        }

        // 检测卸载命令类型
        if (StringUtils.isNotBlank(softService.getSoftServiceUninstallCommand())) {
            softService.setSoftServiceUninstallCommandType(
                    isScriptCommand(softService.getSoftServiceUninstallCommand()) ? 1 : 0
            );
        }

        // 检测启动命令类型
        if (StringUtils.isNotBlank(softService.getSoftServiceStartCommand())) {
            softService.setSoftServiceStartCommandType(
                    isScriptCommand(softService.getSoftServiceStartCommand()) ? 1 : 0
            );
        }

        // 检测停止命令类型
        if (StringUtils.isNotBlank(softService.getSoftServiceStopCommand())) {
            softService.setSoftServiceStopCommandType(
                    isScriptCommand(softService.getSoftServiceStopCommand()) ? 1 : 0
            );
        }

        // 检测重启命令类型
        if (StringUtils.isNotBlank(softService.getSoftServiceRestartCommand())) {
            softService.setSoftServiceRestartCommandType(
                    isScriptCommand(softService.getSoftServiceRestartCommand()) ? 1 : 0
            );
        }
    }

    /**
     * 判断命令是否为脚本命令
     *
     * @param command 命令内容
     * @return 是否为脚本命令
     */
    private boolean isScriptCommand(String command) {
        return command != null && command.trim().startsWith("#!/bin/bash");
    }

    @Override
    public boolean save(SoftService entity) {
        // 在保存前自动检测并设置命令类型
        detectAndSetCommandTypes(entity);
        return super.save(entity);
    }

    @Override
    public boolean updateById(SoftService entity) {
        // 在更新前自动检测并设置命令类型
        detectAndSetCommandTypes(entity);
        return super.updateById(entity);
    }

    /**
     * 判断软件服务是否可以安装
     * 根据安装脚本是否存在来决定
     *
     * @param softService 软件服务
     * @return 是否可以安装
     */
    @Override
    public boolean isInstallable(SoftService softService) {
        if (softService == null) {
            return false;
        }

        return StringUtils.isNotBlank(softService.getSoftServiceInstallCommand());
    }

    /**
     * 清除命令数据以减少数据包大小
     *
     * @param services 软件服务列表
     */
    @Override
    public void cleanCommandData(List<SoftService> services) {
        if (services == null || services.isEmpty()) {
            return;
        }

        for (SoftService service : services) {
            // 清除命令数据
            service.setSoftServiceInstallCommand(null);
            service.setSoftServiceUninstallCommand(null);
            service.setSoftServiceStartCommand(null);
            service.setSoftServiceStopCommand(null);
            service.setSoftServiceRestartCommand(null);
            service.setSoftServiceStatusCheckCommand(null);
        }
    }

    /**
     * 分页查询并清除命令数据
     *
     * @param query       查询条件
     * @param softService 软件服务
     * @return 分页结果，已清除命令数据
     */
    @Override
    public ReturnPageResult<SoftService> pageForWithoutCommands(Query<SoftService> query, SoftService softService) {
        // 先执行分页查询
        IPage<SoftService> page = this.pageFor(query, softService);

        // 清除命令数据
        if (page != null && page.getRecords() != null) {
            cleanCommandData(page.getRecords());
        }

        // 转换为ReturnPageResult并返回
        return ReturnPageResultUtils.ok(page);
    }

    /**
     * 检测命令类型
     *
     * @param command 命令内容
     * @return 命令类型信息，包含commandType、commandTypeName和isScript
     */
    @Override
    public ReturnResult<Map<String, Object>> detectCommandType(String command) {
        // 检查命令是否以#!/bin/bash开头
        boolean isScript = command != null && command.trim().startsWith("#!/bin/bash");

        // 返回命令类型信息
        Map<String, Object> result = Map.of(
                "commandType", isScript ? 1 : 0,
                "commandTypeName", isScript ? "脚本" : "单行命令",
                "isScript", isScript
        );

        return ReturnResult.success(result);
    }
}
