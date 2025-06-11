package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnCode;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.monitor.entity.SoftServiceInstall;
import com.chua.starter.monitor.mapper.SoftServiceMapper;
import com.chua.starter.monitor.service.SoftServiceService;
import com.chua.starter.mybatis.entity.Query;
import com.github.yulichang.toolkit.MPJWrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 软件服务服务实现类
 *
 * @author CH
 */
@Service
@Slf4j
public class SoftServiceServiceImpl extends ServiceImpl<SoftServiceMapper, SoftService> implements SoftServiceService {

    @Override
    public IPage<SoftService> pageFor(Query<SoftService> query, SoftService softService) {
        if (null == softService.getSshId()) {
            return this.page(query.createFullPage(), MPJWrappers.lambdaJoin(SoftService.class)
                    .selectAll(SoftService.class)
                    .like(StringUtils.isNotBlank(softService.getSoftServiceCategory()), SoftService::getSoftServiceCategory, softService.getSoftServiceCategory())
                    .like(StringUtils.isNotBlank(softService.getSoftServiceName()), SoftService::getSoftServiceName, softService.getSoftServiceName())
                    .like(StringUtils.isNotBlank(softService.getSoftServiceOs()), SoftService::getSoftServiceOs, softService.getSoftServiceOs())
            );
        }
        return this.page(query.createFullPage(),
                query.mpjLambda()
                        .selectAll(SoftService.class)
                        .leftJoin(SoftServiceInstall.class, SoftServiceInstall::getSoftServiceId, SoftService::getSoftServiceId)
                        .like(StringUtils.isNotBlank(softService.getSoftServiceCategory()), SoftService::getSoftServiceCategory, softService.getSoftServiceCategory())
                        .like(StringUtils.isNotBlank(softService.getSoftServiceName()), SoftService::getSoftServiceName, softService.getSoftServiceName())
                        .like(StringUtils.isNotBlank(softService.getSoftServiceOs()), SoftService::getSoftServiceOs, softService.getSoftServiceOs())
                        .eq(SoftServiceInstall::getSshId, softService.getSshId())
        );
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
}
