package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.PageResult;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.unit.size.SizeUnit;
import com.chua.common.support.utils.StringUtils;
import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.docker.support.client.DockerPullImage;
import com.chua.docker.support.online.*;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.callback.StatusUpdatingWebSocketDockerPullCallback;
import com.chua.starter.monitor.entity.*;
import com.chua.starter.monitor.enums.OperationType;
import com.chua.starter.monitor.factory.DockerProtocolClientFactory;
import com.chua.starter.monitor.mapper.*;
import com.chua.starter.monitor.service.DockerCallbackService;
import com.chua.starter.monitor.service.SystemSoftService;
import com.chua.starter.monitor.service.SystemSoftVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统软件管理服务实现类
 *
 * @author CH
 * @since 2025/01/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSoftServiceImpl extends ServiceImpl<SystemSoftMapper, SystemSoft> implements SystemSoftService {

    private final SystemSoftMapper systemSoftMapper;
    private final SystemSoftRegistryMapper systemSoftRegistryMapper;
    private final SystemSoftVersionService systemSoftVersionService;
    private final SystemSoftImageMapper systemSoftImageMapper;
    private final SystemSoftRecordMapper systemSoftRecordMapper;
    private final DockerProtocolClientFactory dockerProtocolClientFactory;
    private final SocketSessionTemplate socketSessionTemplate;

    static final Map<Integer, DockerProtocolClient> PROTOCOL_CLIENT_MAP = new ConcurrentHashMap<>();
    @Autowired
    @Lazy
    private DockerCallbackService dockerCallbackService;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public ReturnResult<SystemSoft> getBySoftCode(String softCode) {
        log.debug("根据软件代码查询软件信息 - softCode: {}", softCode);

        try {
            if (!StringUtils.hasText(softCode)) {
                return ReturnResult.error("软件代码不能为空");
            }

            SystemSoft systemSoft = systemSoftMapper.selectBySoftCode(softCode);
            return ReturnResult.ok(systemSoft);

        } catch (Exception e) {
            log.error("根据软件代码查询软件信息异常 - softCode: {}", softCode, e);
            return ReturnResult.error("查询软件信息异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemSoft>> selectSoftList(String softCode, String softName, String category, Integer status) {
        log.debug("条件查询软件列表 - softCode: {}, softName: {}, category: {}, status: {}",
                softCode, softName, category, status);

        try {
            LambdaQueryWrapper<SystemSoft> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(StringUtils.hasText(softCode), SystemSoft::getSystemSoftCode, softCode)
                    .like(StringUtils.hasText(softName), SystemSoft::getSystemSoftName, softName)
                    .eq(StringUtils.hasText(category), SystemSoft::getSystemSoftCategory, category)
                    .eq(Objects.nonNull(status), SystemSoft::getSystemSoftStatus, status)
                    .orderByDesc(SystemSoft::getCreateTime);

            List<SystemSoft> list = systemSoftMapper.selectList(wrapper);
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("条件查询软件列表异常", e);
            return ReturnResult.error("查询软件列表异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SystemSoft> saveOrUpdateForSoft(SystemSoft systemSoft) {
        log.debug("保存或更新软件信息 - softCode: {}", systemSoft.getSystemSoftCode());

        try {
            // 数据验证
            ReturnResult<String> validateResult = validateSoftware(systemSoft);
            if (!validateResult.isOk()) {
                return ReturnResult.error(validateResult.getMsg());
            }

            // 设置默认值
            if (systemSoft.getSystemSoftStatus() == null) {
                systemSoft.setSystemSoftStatus(1);
            }
            if (systemSoft.getSystemSoftIsOfficial() == null) {
                systemSoft.setSystemSoftIsOfficial(0);
            }

            LocalDateTime now = LocalDateTime.now();
            if (systemSoft.getSystemSoftId() == null) {
                systemSoft.setCreateTime(now);
                systemSoftMapper.insert(systemSoft);
            } else {
                systemSoft.setUpdateTime(now);
                systemSoftMapper.updateById(systemSoft);
            }

            return ReturnResult.ok(systemSoft, "操作成功");

        } catch (Exception e) {
            log.error("保存或更新软件信息异常 - softCode: {}", systemSoft.getSystemSoftCode(), e);
            return ReturnResult.error("操作异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> removeById(Integer softId) {
        log.debug("删除软件 - softId: {}", softId);

        try {
            if (softId == null) {
                return ReturnResult.error("软件ID不能为空");
            }

            // 检查软件是否存在
            SystemSoft systemSoft = systemSoftMapper.selectById(softId);
            if (systemSoft == null) {
                return ReturnResult.error("软件不存在");
            }

            // 检查是否有关联的镜像或容器
            // TODO: 添加关联检查逻辑

            int result = systemSoftMapper.deleteById(softId);
            return ReturnResult.ok(result > 0, result > 0 ? "删除成功" : "删除失败");

        } catch (Exception e) {
            log.error("删除软件异常 - softId: {}", softId, e);
            return ReturnResult.error("删除异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> removeByIds(List<Integer> softIds) {
        log.debug("批量删除软件 - softIds: {}", softIds);

        try {
            if (softIds == null || softIds.isEmpty()) {
                return ReturnResult.error("软件ID列表不能为空");
            }

            int result = systemSoftMapper.deleteBatchIds(softIds);
            return ReturnResult.ok(result > 0, "批量删除完成，共删除" + result + "条记录");

        } catch (Exception e) {
            log.error("批量删除软件异常 - softIds: {}", softIds, e);
            return ReturnResult.error("批量删除异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> syncFromRegistry(Integer registryId) {
        log.info("从仓库同步软件信息 - registryId: {}", registryId);

        try {
            SystemSoftRegistry registry;
            if (registryId == null) {
                // 获取默认仓库
                registry = systemSoftRegistryMapper.selectDefaultRegistry();
                if (registry == null) {
                    return ReturnResult.error("未设置默认仓库");
                }
            } else {
                registry = systemSoftRegistryMapper.selectById(registryId);
                if (registry == null) {
                    return ReturnResult.error("仓库不存在");
                }
            }

            if (registry.getSystemSoftRegistryStatus() != 1) {
                return ReturnResult.error("仓库未启用");
            }
            if (registry.getSystemSoftRegistrySupportSync() == null || registry.getSystemSoftRegistrySupportSync() != 1) {
                return ReturnResult.error("该仓库不支持同步");
            }
            if (registry.getSystemSoftRegistryServerId() == null) {
                return ReturnResult.error("仓库未绑定服务器，无法使用Docker客户端同步");
            }

            // 使用 DockerProtocolClient 在绑定服务器上列举镜像，并同步到软件表
            int syncCount = doDockerClientListSync(registry);

            log.info("同步软件信息完成 - 同步数量: {}", syncCount);
            return ReturnResult.ok(syncCount, "同步成功，共同步" + syncCount + "个软件");

        } catch (Exception e) {
            log.error("从仓库同步软件信息异常 - registryId: {}", registryId, e);
            return ReturnResult.error("同步异常: " + e.getMessage());
        }
    }

    /**
     * 在线关键字搜索（聚合默认仓库，SPI）
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    页大小
     * @return 在线搜索到的软件列表
     */
    @Override
    public ReturnPageResult<SystemSoft> searchOnlineByKeyword(String keyword, Integer page, Integer size) {
        log.debug("在线关键字搜索开始 - keyword: {}, page: {}, size: {}", keyword, page, size);
        try {
            int p = (page == null || page < 1) ? 1 : page;
            int s = (size == null || size < 1) ? 10 : Math.min(size, 10000);
            String kw = StringUtils.defaultString(keyword, "");
            if (!StringUtils.hasText(kw)) {
                return ReturnPageResult.ok(PageResult.<SystemSoft>builder().data(java.util.Collections.emptyList()).total(0).pageNo(p).pageSize(s).build());
            }

            List<SystemSoftRegistry> registries = systemSoftRegistryMapper.selectDefaultRegistries();
            if (registries == null || registries.isEmpty()) {
                log.warn("未找到默认仓库，无法进行在线搜索");
                return ReturnPageResult.ok(PageResult.<SystemSoft>builder().data(java.util.Collections.emptyList()).total(0).pageNo(p).pageSize(s).build());
            }

            Map<String, SystemSoft> resultMap = new LinkedHashMap<>();
            Map<String, List<SystemSoftVersion>> resultVersionMap = new LinkedHashMap<>();
            long totalSum = 0L;
            for (SystemSoftRegistry registry : registries) {
                if (registry == null) {
                    continue;
                }
                var dockerRegistry = new DockerRegistry();
                dockerRegistry.setType(StringUtils.defaultString(registry.getSystemSoftRegistryType(), ""));
                dockerRegistry.setUrl(StringUtils.defaultString(registry.getSystemSoftRegistryUrl(), ""));
                dockerRegistry.setUsername(registry.getSystemSoftRegistryUsername());
                dockerRegistry.setPassword(registry.getSystemSoftRegistryPassword());

                DockerOnlineSearchProvider matched = getDockerOnlineSearchProvider(dockerRegistry);
                if (matched == null ) {
                    log.warn("未发现任何 DockerOnlineSearchProvider SPI 实现");
                    return ReturnPageResult.ok(PageResult.<SystemSoft>builder().data(List.of()).total(0).pageNo(p).pageSize(s).build());
                }

                DockerOnlineSearchTagProvider matchedTagProvider = getDockerOnlineSearchTagProvider(dockerRegistry);

                var pageResult = matched.search(kw.trim(), p, s, dockerRegistry);
                List<DockerSearchItem> items = pageResult != null && pageResult.getData() != null ? pageResult.getData().getData() : java.util.Collections.emptyList();
                long providerTotal = pageResult != null && pageResult.getData() != null ? pageResult.getData().getTotal() : 0L;
                totalSum += providerTotal;
                if (items == null || items.isEmpty()) {
                    continue;
                }

                for (DockerSearchItem it : items) {
                    if (it == null || !StringUtils.hasText(it.getImage())) {
                        continue;
                    }
                    String key = it.getImage();
                    if (resultMap.containsKey(key)) {
                        continue;
                    }
                    SystemSoft soft = new SystemSoft();
                    soft.setSystemSoftName(StringUtils.defaultString(it.getName(), it.getImage()));
                    soft.setSystemSoftCode(StringUtils.defaultString(it.getCode(), it.getImage().replace('/', '-')));
                    soft.setSystemSoftDockerImage(it.getImage());
                    soft.setSystemSoftDesc(it.getDesc());
                    soft.setSystemSoftIsOfficial(it.getOfficial() == null ? 0 : it.getOfficial());
                    soft.setSystemSoftRegistryId(registry.getSystemSoftRegistryId());
                    soft.setSystemSoftStarCount(it.getStarCount());
                    soft.setSystemSoftPullCount(it.getPullCount());
                    soft.setSystemSoftStatus(1);

                    if(null != matchedTagProvider) {
                        ReturnPageResult<DockerTagItem> dockerTagItemReturnPageResult = matchedTagProvider.listTags(soft.getSystemSoftIsOfficial() == 1 ? "library/" + soft.getSystemSoftDockerImage() : soft.getSystemSoftDockerImage(), 1, 10000, dockerRegistry);
                        resultVersionMap.put(key, createSystemSoftVersion(it.getImage(), dockerTagItemReturnPageResult));
                    }
                    resultMap.put(key, soft);
                }
            }

            List<SystemSoft> result = new ArrayList<>(resultMap.values());
            updateSoft(result, resultVersionMap);

            return ReturnPageResult.ok(PageResult.<SystemSoft>builder()
                    .data(result)
                    .total(totalSum)
                    .pageNo(p)
                    .pageSize(s)
                    .build());
        } catch (Exception e) {
            log.error("在线关键字搜索异常 - keyword: {}", keyword, e);
            return ReturnPageResult.error("在线搜索异常: " + e.getMessage());
        }
    }

    /**
     * 获取 DockerOnlineSearchTagProvider
     * @param dockerRegistry 配置
     * @return 结果
     */
    private DockerOnlineSearchTagProvider getDockerOnlineSearchTagProvider(DockerRegistry dockerRegistry) {
        var sp = ServiceProvider.of(DockerOnlineSearchTagProvider.class);
        List<DockerOnlineSearchTagProvider> providers = sp.collect();
        for (DockerOnlineSearchTagProvider provider : providers) {
            try {
                if (provider.supports(dockerRegistry)) {
                    return provider;
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    /**
     * 获取 DockerOnlineSearchProvider
     * @param dockerRegistry 配置
     * @return 结果
     */
    private DockerOnlineSearchProvider getDockerOnlineSearchProvider(DockerRegistry dockerRegistry) {
        var sp = ServiceProvider.of(DockerOnlineSearchProvider.class);
        List<DockerOnlineSearchProvider> providers = sp.collect();
        for (DockerOnlineSearchProvider provider : providers) {
            try {
                if (provider.supports(dockerRegistry)) {
                    return provider;
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    /**
     * 更新软件
     * @param result 软件
     * @param resultVersionMap 软件版本
     */
    private void updateSoft(List<SystemSoft> result, Map<String, List<SystemSoftVersion>> resultVersionMap) {
       Thread.ofVirtual().start(() -> {
           transactionTemplate.execute(it -> {
               Map<String, Integer> map = new HashMap<>(result.size());
               for (SystemSoft systemSoft : result) {
                   LambdaUpdateWrapper<SystemSoft> eq = Wrappers.<SystemSoft>lambdaUpdate()
                           .eq(SystemSoft::getSystemSoftDockerImage, systemSoft.getSystemSoftDockerImage())
                           .eq(SystemSoft::getSystemSoftRegistryId, systemSoft.getSystemSoftRegistryId());
                   SystemSoft one = this.getOne(eq);
                   if (null != one) {
                       systemSoft.setSystemSoftId(one.getSystemSoftId());
                       this.update(systemSoft, eq);
                   } else {
                       this.save(systemSoft);
                   }
                   map.put(systemSoft.getSystemSoftDockerImage(), systemSoft.getSystemSoftId());
               }

               updateSoftVersion(result, resultVersionMap, map);
               return null;
           });
       });
    }

    /**
     * 更新软件版本
     * @param result 软件
     * @param resultVersionMap 软件版本
     * @param map 软件id
     */
    private void updateSoftVersion(List<SystemSoft> result, Map<String, List<SystemSoftVersion>> resultVersionMap, Map<String, Integer> map) {
        for (Map.Entry<String, List<SystemSoftVersion>> entry : resultVersionMap.entrySet()) {
            Integer softId = map.get(entry.getKey());
            if (softId == null) {
                continue;
            }
            List<SystemSoftVersion> value = entry.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            value.forEach(it -> {
                it.setSystemSoftId(softId);
            });

            registerSoftVersion(softId, value);
        }
    }

    /**
     * 注册软件版本
     * @param softId 软件id
     * @param value 软件版本
     */
    private void registerSoftVersion(Integer softId, List<SystemSoftVersion> value) {
        systemSoftVersionService.remove(Wrappers.<SystemSoftVersion>lambdaUpdate().eq(SystemSoftVersion::getSystemSoftId, softId));
        systemSoftVersionService.saveBatch(value);
    }

    /**
     * 创建系统软件版本
     * @param image 镜像
     * @param dockerTagItemReturnPageResult 版本数据
     * @return 系统软件版本
     */
    private List<SystemSoftVersion> createSystemSoftVersion(String image, ReturnPageResult<DockerTagItem> dockerTagItemReturnPageResult) {
        PageResult<DockerTagItem> data = dockerTagItemReturnPageResult.getData();
        long total = data != null ? data.getTotal() : 0L;
        if (total <= 0) {
            return List.of();
        }
        List<DockerTagItem> dataList = data != null ? data.getData() : List.of();
        return dataList.stream().map(it -> {
            SystemSoftVersion version = new SystemSoftVersion();
            version.setSystemSoftVersionName(it.getTag());
            version.setSystemSoftVersionNumber(it.getTag());
            version.setSystemSoftVersionDescription(it.getTag());
            version.setSystemSoftVersionSize(SizeUnit.from(it.getSize()).toByteSize());
            version.setSystemSoftVersionIsLatest("latest".equals(it.getTag()) ? 1 : 0);
            version.setSystemSoftVersionReleaseTime(it.getLastPushTime());
            version.setSystemSoftVersionStatus(it.getActive() == null ? 1 : 0);
            version.setSysSoftVersionDigest(it.getDigest());
            return version;
        }).toList();
    }

    @Override
    public ReturnResult<Integer> importOnlineItems(List<SystemSoft> items) {
        return null;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SystemSoft> syncSingleSoftware(Integer registryId, String softwareName) {
        log.info("同步单个软件信息 - registryId: {}, softwareName: {}", registryId, softwareName);

        try {
            if (!StringUtils.hasText(softwareName)) {
                return ReturnResult.error("软件名称不能为空");
            }

            SystemSoftRegistry registry = systemSoftRegistryMapper.selectById(registryId);
            if (registry == null) {
                return ReturnResult.error("仓库不存在");
            }

            // TODO: 实现具体的单个软件同步逻辑
            // 这里暂时返回空结果

            return ReturnResult.ok(null, "同步成功");

        } catch (Exception e) {
            log.error("同步单个软件信息异常 - registryId: {}, softwareName: {}", registryId, softwareName, e);
            return ReturnResult.error("同步异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> installSoftware(Integer softId, List<Integer> serverIds, String imageTag) {
        log.info("安装软件 - softId: {}, serverIds: {}, imageTag: {}", softId, serverIds, imageTag);

        try {
            if (softId == null) {
                return ReturnResult.error("软件ID不能为空");
            }
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }
            if (!StringUtils.hasText(imageTag)) {
                imageTag = "latest";
            }

            SystemSoft systemSoft = systemSoftMapper.selectById(softId);
            if (systemSoft == null) {
                return ReturnResult.error("软件不存在");
            }

            if (!StringUtils.hasText(systemSoft.getSystemSoftDockerImage())) {
                return ReturnResult.error("软件未配置Docker镜像");
            }

            // 仅记录安装计划（兼容旧实现），不触发拉取
            for (Integer serverId : serverIds) {
                SystemSoftImage image = new SystemSoftImage();
                image.setSystemSoftId(softId);
                image.setSystemSoftImageServerId(serverId);
                image.setSystemSoftImageName(systemSoft.getSystemSoftDockerImage());
                image.setSystemSoftImageTag(imageTag);
                image.setCreateTime(LocalDateTime.now());
                image.setUpdateTime(LocalDateTime.now());
                systemSoftImageMapper.insert(image);
            }

            return ReturnResult.ok(true, "安装计划已记录");

        } catch (Exception e) {
            log.error("安装软件异常 - softId: {}, serverIds: {}, imageTag: {}", softId, serverIds, imageTag, e);
            return ReturnResult.error("安装异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> installSoftwareWithConfig(Integer softId, List<Integer> serverIds, java.util.Map<String, Object> installConfig) {
        log.info("安装软件(带配置) - softId: {}, serverIds: {}", softId, serverIds);
        try {
            if (softId == null) {
                return ReturnResult.error("软件ID不能为空");
            }
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }

            SystemSoft systemSoft = systemSoftMapper.selectById(softId);
            if (systemSoft == null) {
                return ReturnResult.error("软件不存在");
            }
            if (!StringUtils.hasText(systemSoft.getSystemSoftDockerImage())) {
                return ReturnResult.error("软件未配置Docker镜像");
            }

            String imageName = systemSoft.getSystemSoftDockerImage();
            String imageTag = String.valueOf(firstNonNull(installConfig != null ? installConfig.get("imageTag") : null, "latest"));
            String fullImage = StringUtils.hasText(imageTag) ? imageName + ":" + imageTag : imageName;

            // 生成一次性的 operationId
            String operationId = java.util.UUID.randomUUID().toString();

            // 针对每台服务器，创建镜像记录并触发异步拉取（进度通过 WebSocket 推送）
            for (Integer serverId : serverIds) {
                try {
                    // 创建镜像记录（状态: PULLING）
                    SystemSoftImage image = new SystemSoftImage();
                    image.setSystemSoftId(softId);
                    image.setSystemSoftImageServerId(serverId);
                    image.setSystemSoftImageName(imageName);
                    image.setSystemSoftImageTag(imageTag);
                    image.setSystemSoftImageFullName(fullImage);
                    image.setSystemSoftImageStatus(SystemSoftImageStatus.PULLING);
                    image.setCreateTime(LocalDateTime.now());
                    image.setUpdateTime(LocalDateTime.now());
                    systemSoftImageMapper.insert(image);

                    // 创建Docker客户端
                    DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
                    if (dockerClient == null) {
                        log.warn("无法创建Docker客户端, serverId: {}", serverId);
                        // 标记失败
                        image.setSystemSoftImageStatus(SystemSoftImageStatus.PULL_FAILED);
                        image.setUpdateTime(LocalDateTime.now());
                        systemSoftImageMapper.updateById(image);
                        continue;
                    }

                    // 组装拉取配置
                    DockerPullImage pullConfig = DockerPullImage.builder()
                            .imageName(imageName)
                            .tag(imageTag)
                            .build();

                    // 创建状态更新回调（会通过 WebSocket 推送）
                    var callback = dockerCallbackService.createStatusUpdatingWebSocketPullCallback(OperationType.PULL, fullImage, image.getSystemSoftImageId());

                    // 触发异步拉取
                    dockerCallbackService.pullImageWithCallback(dockerClient, pullConfig, callback);
                } catch (Exception exPerServer) {
                    log.warn("服务器安装触发失败, serverId: {}, err: {}", serverId, exPerServer.getMessage());
                }
            }

            return ReturnResult.ok(operationId);
        } catch (Exception e) {
            log.error("安装软件(带配置) 异常 - softId: {}", softId, e);
            return ReturnResult.error("安装异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getAvailableTags(Integer softId) {
        log.debug("获取软件可用标签 - softId: {}", softId);

        try {
            if (softId == null) {
                return ReturnResult.error("软件ID不能为空");
            }

            SystemSoft systemSoft = systemSoftMapper.selectById(softId);
            if (systemSoft == null) {
                return ReturnResult.error("软件不存在");
            }

            // TODO: 实现从仓库获取标签列表的逻辑
            // 这里暂时返回默认标签
            List<String> tags = List.of("latest", "stable");

            return ReturnResult.ok(tags);

        } catch (Exception e) {
            log.error("获取软件可用标签异常 - softId: {}", softId, e);
            return ReturnResult.error("获取标签异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkSoftCodeExists(String softCode, Integer excludeId) {
        log.debug("检查软件代码是否存在 - softCode: {}, excludeId: {}", softCode, excludeId);

        try {
            if (!StringUtils.hasText(softCode)) {
                return ReturnResult.error("软件代码不能为空");
            }

            Long count = systemSoftMapper.checkSoftCodeExists(softCode, excludeId);
            return ReturnResult.ok(count > 0);

        } catch (Exception e) {
            log.error("检查软件代码异常 - softCode: {}, excludeId: {}", softCode, excludeId, e);
            return ReturnResult.error("检查异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchUpdateStatus(List<Integer> softIds, Integer status) {
        log.debug("批量更新软件状态 - softIds: {}, status: {}", softIds, status);

        try {
            if (softIds == null || softIds.isEmpty()) {
                return ReturnResult.error("软件ID列表不能为空");
            }
            if (status == null) {
                return ReturnResult.error("状态不能为空");
            }

            Integer updateCount = systemSoftMapper.batchUpdateStatus(softIds, status);
            return ReturnResult.ok(updateCount > 0, "批量更新成功，共更新" + updateCount + "条记录");

        } catch (Exception e) {
            log.error("批量更新软件状态异常 - softIds: {}, status: {}", softIds, status, e);
            return ReturnResult.error("批量更新异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemSoft>> searchByKeyword(String keyword) {
        log.debug("根据关键字搜索软件 - keyword: {}", keyword);

        try {
            if (!StringUtils.hasText(keyword)) {
                return ReturnResult.error("关键字不能为空");
            }

            List<SystemSoft> list = systemSoftMapper.searchByKeyword(keyword);
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("根据关键字搜索软件异常 - keyword: {}", keyword, e);
            return ReturnResult.error("搜索异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemSoft>> getByCategory(String category) {
        log.debug("根据分类获取软件列表 - category: {}", category);

        try {
            List<SystemSoft> list = systemSoftMapper.selectByCategory(category);
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("根据分类获取软件列表异常 - category: {}", category, e);
            return ReturnResult.error("获取列表异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemSoft>> getPopularSofts(Integer limit) {
        log.debug("获取热门软件列表 - limit: {}", limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }

            List<SystemSoft> list = systemSoftMapper.selectPopularSofts(limit);
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("获取热门软件列表异常 - limit: {}", limit, e);
            return ReturnResult.error("获取列表异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemSoft>> getLatestSofts(Integer limit) {
        log.debug("获取最新软件列表 - limit: {}", limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }

            List<SystemSoft> list = systemSoftMapper.selectLatestSofts(limit);
            return ReturnResult.ok(list);

        } catch (Exception e) {
            log.error("获取最新软件列表异常 - limit: {}", limit, e);
            return ReturnResult.error("获取列表异常: " + e.getMessage());
        }
    }

    /**
     * 拉取Docker镜像（支持异步回调）
     * <p>
     * 该方法实现了拉取Docker镜像的功能，支持异步回调通知。整个过程包括：
     * 1. 创建Docker客户端连接
     * 2. 获取镜像信息并创建镜像记录
     * 3. 构造DockerPullImage配置对象
     * 4. 创建状态更新WebSocket回调处理器
     * 5. 执行异步镜像拉取操作
     * <p>
     * 该方法使用异步回调机制，在拉取过程中实时更新镜像状态并通过WebSocket推送进度信息。
     * 注意：由于是异步操作，方法返回时拉取可能尚未完成，状态更新在回调中处理。
     *
     * @param softId   软件ID，用于查询软件的Docker镜像信息
     * @param serverId 服务器ID，指定目标服务器
     * @param imageTag 镜像标签，默认为"latest"
     * @param params   拉取参数（保留用于扩展）
     * @return 拉取结果，包含镜像信息或错误信息
     * @see DockerProtocolClient Docker客户端
     * @see DockerPullImage 镜像拉取配置对象
     * @see StatusUpdatingWebSocketDockerPullCallback 状态更新WebSocket回调处理器
     */
    @Override
    public ReturnResult<SystemSoftImage> pullDockerImageWithCallback(Integer softId, Integer serverId, String imageTag, java.util.Map<String, Object> params) {
        log.info("拉取Docker镜像(带回调) - 软件ID: {}, 服务器ID: {}, 镜像标签: {}", softId, serverId, imageTag);

        try {
            // 获取Docker客户端
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 获取镜像信息
            String imageName = "nginx"; // 这里应该根据softId查询实际的镜像名称

            // 构造完整的镜像名称
            String fullImageName = imageName;
            if (StringUtils.hasText(imageTag)) {
                fullImageName = imageName + ":" + imageTag;
            }

            // 创建镜像记录
            SystemSoftImage image = new SystemSoftImage();
            image.setSystemSoftId(softId);
            image.setSystemSoftImageServerId(serverId);
            image.setSystemSoftImageName(imageName);
            image.setSystemSoftImageTag(imageTag);
            image.setSystemSoftImageFullName(fullImageName);
            image.setSystemSoftImageStatus(SystemSoftImageStatus.PULLING); // 拉取中
            image.setCreateTime(LocalDateTime.now());
            systemSoftImageMapper.insert(image);

            // 创建DockerPullImage配置对象
            DockerPullImage config = DockerPullImage.builder()
                    .imageName(imageName)
                    .tag(imageTag)
                    .build();

            // 创建状态更新WebSocket回调处理器
            var callback = dockerCallbackService.createStatusUpdatingWebSocketPullCallback(OperationType.PULL, fullImageName, image.getSystemSoftImageId());

            // 执行拉取操作（带回调）
            log.info("开始拉取镜像(带回调): {}:{}", imageName, imageTag);
            dockerCallbackService.pullImageWithCallback(dockerClient, config, callback);

            // 注意：由于是异步操作，我们不能立即更新状态为已拉取
            // 状态更新应该在回调中处理

            log.info("拉取镜像请求已发送 - 软件ID: {}, 服务器ID: {}, 镜像标签: {}", softId, serverId, imageTag);
            return ReturnResult.ok(image);
        } catch (Exception e) {
            // 更新镜像状态为拉取失败
            try {
                SystemSoftImage image = new SystemSoftImage();
                image.setSystemSoftImageStatus(SystemSoftImageStatus.PULL_FAILED);
                systemSoftImageMapper.updateById(image);
            } catch (Exception updateEx) {
                log.warn("更新镜像状态为拉取失败时出错: {}", updateEx.getMessage());
            }

            log.error("拉取镜像失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("拉取镜像失败: " + e.getMessage());
        }
    }

    /**
     * 使用 DockerProtocolClient 列举镜像并同步为 SystemSoft
     * <p>
     * 实现要点：
     * 1) 在绑定服务器上创建 Docker 客户端
     * 2) 反射适配 listImage/listImages 等可能的签名
     * 3) 提取仓库名（去除标签）并入库
     *
     * @param registry 仓库配置
     * @return 新增或更新数量
     * @author CH
     * @version 1.0.0
     * @since 2025/10/18
     */
    private int doDockerClientListSync(SystemSoftRegistry registry) {
        Integer serverId = registry.getSystemSoftRegistryServerId();
        if (serverId == null) {
            return 0;
        }
        int total = 0;
        try {
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.warn("无法创建Docker客户端 - serverId: {}", serverId);
                return 0;
            }

            List<?> items = tryInvokeListImages(dockerClient, registry);
            if (items == null || items.isEmpty()) {
                log.debug("未从 Docker 客户端获取到镜像列表");
                return 0;
            }

            for (Object it : items) {
                for (String baseImage : extractRepoNames(it)) {
                    if (!StringUtils.hasText(baseImage)) {
                        continue;
                    }
                    String softName = baseImage.contains("/") ? baseImage.substring(baseImage.lastIndexOf('/') + 1) : baseImage;
                    String softCode = baseImage.replace('/', '_');
                    LocalDateTime now = LocalDateTime.now();

                    SystemSoft existed = systemSoftMapper.selectByDockerImage(baseImage);
                    if (existed == null) {
                        SystemSoft soft = new SystemSoft();
                        soft.setSystemSoftName(softName);
                        soft.setSystemSoftCode(softCode);
                        soft.setSystemSoftDockerImage(baseImage);
                        soft.setSystemSoftRegistryId(registry.getSystemSoftRegistryId());
                        soft.setSystemSoftStatus(1);
                        soft.setSystemSoftIsOfficial(0);
                        soft.setCreateTime(now);
                        soft.setUpdateTime(now);
                        systemSoftMapper.insert(soft);
                        total++;
                    } else {
                        existed.setSystemSoftRegistryId(registry.getSystemSoftRegistryId());
                        existed.setUpdateTime(now);
                        if (!StringUtils.hasText(existed.getSystemSoftName())) {
                            existed.setSystemSoftName(softName);
                        }
                        if (!StringUtils.hasText(existed.getSystemSoftCode())) {
                            existed.setSystemSoftCode(softCode);
                        }
                        if (existed.getSystemSoftStatus() == null) {
                            existed.setSystemSoftStatus(1);
                        }
                        systemSoftMapper.updateById(existed);
                        total++;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("基于 Docker 客户端列举镜像失败: {}", e.getMessage());
            return 0;
        }
        return total;
    }

    /**
     * 反射调用 DockerProtocolClient 列举镜像，兼容不同签名
     */
    private List<?> tryInvokeListImages(DockerProtocolClient client, SystemSoftRegistry registry) {
        try {
            return client.listImages();
        } catch (Throwable ex) {
            log.warn("调用 DockerProtocolClient 列举镜像失败: {}", ex.getMessage());
        }
        return java.util.Collections.emptyList();
    }

    /**
     * 从返回项中提取去标签的仓库名集合
     */
    private List<String> extractRepoNames(Object item) {
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        if (item == null) {
            return java.util.Collections.emptyList();
        }
        try {
            if (item instanceof String) {
                set.add(stripTag((String) item));
            } else if (item instanceof java.util.Map) {
                java.util.Map<?, ?> mp = (java.util.Map<?, ?>) item;
                Object tags = firstNonNull(mp.get("repoTags"), mp.get("RepoTags"), mp.get("tags"));
                if (tags instanceof List) {
                    for (Object t : (List<?>) tags) {
                        if (t != null) {
                            set.add(stripTag(String.valueOf(t)));
                        }
                    }
                }
                Object name = firstNonNull(mp.get("name"), mp.get("repo_name"), mp.get("repoName"), mp.get("repository"));
                if (name != null) {
                    set.add(stripTag(String.valueOf(name)));
                }
            } else {
                try {
                    var m = item.getClass().getMethod("getRepoTags");
                    Object v = m.invoke(item);
                    if (v instanceof String[]) {
                        for (String s : (String[]) v) {
                            set.add(stripTag(s));
                        }
                    } else if (v instanceof java.util.List) {
                        for (Object s : (java.util.List<?>) v) {
                            if (s != null) {
                                set.add(stripTag(String.valueOf(s)));
                            }
                        }
                    }
                } catch (Exception ignore) {
                }
                try {
                    var m = item.getClass().getMethod("getName");
                    Object v = m.invoke(item);
                    if (v != null) {
                        set.add(stripTag(String.valueOf(v)));
                    }
                } catch (Exception ignore) {
                }
                try {
                    var m = item.getClass().getMethod("getRepository");
                    Object v = m.invoke(item);
                    if (v != null) {
                        set.add(stripTag(String.valueOf(v)));
                    }
                } catch (Exception ignore) {
                }
            }
        } catch (Throwable ignore) {
        }
        return new ArrayList<>(set);
    }

    private Object firstNonNull(Object... arr) {
        if (arr == null) return null;
        for (Object o : arr) {
            if (o != null) {
                return o;
            }
        }
        return null;
    }

    private String stripTag(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        int idx = name.lastIndexOf(':');
        if (idx > 0 && !name.contains("http://") && !name.contains("https://")) {
            return name.substring(0, idx);
        }
        return name;
    }

    /**
     * 验证软件信息
     *
     * @param systemSoft 软件信息
     * @return 验证结果
     */
    private ReturnResult<String> validateSoftware(SystemSoft systemSoft) {
        if (systemSoft == null) {
            return ReturnResult.error("软件信息不能为空");
        }

        if (!StringUtils.hasText(systemSoft.getSystemSoftCode())) {
            return ReturnResult.error("软件代码不能为空");
        }

        if (!StringUtils.hasText(systemSoft.getSystemSoftName())) {
            return ReturnResult.error("软件名称不能为空");
        }

        // 检查软件代码是否重复
        if (systemSoft.getSystemSoftId() == null) {
            // 新增时检查
            SystemSoft existing = systemSoftMapper.selectBySoftCode(systemSoft.getSystemSoftCode());
            if (existing != null) {
                return ReturnResult.error("软件代码已存在");
            }
        } else {
            // 更新时检查
            Long count = systemSoftMapper.checkSoftCodeExists(systemSoft.getSystemSoftCode(), systemSoft.getSystemSoftId());
            if (count > 0) {
                return ReturnResult.error("软件代码已被其他软件使用");
            }
        }

        return ReturnResult.ok("验证通过");
    }


    /**
     * 获取Docker客户端
     *
     * @param serverId 服务器ID
     * @return Docker客户端
     */
    public DockerProtocolClient getDockerClient(Integer serverId) {
        return dockerProtocolClientFactory.createClient(serverId);
    }
}