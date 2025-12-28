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
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.constant.MonitorSocketTopics;
import com.chua.starter.monitor.callback.StatusUpdatingWebSocketDockerPullCallback;
import com.chua.starter.monitor.entity.*;
import com.chua.starter.monitor.enums.OperationType;
import com.chua.starter.monitor.factory.DockerProtocolClientFactory;
import com.chua.starter.monitor.mapper.*;
import com.chua.starter.monitor.service.docker.DockerCallbackService;
import com.chua.starter.monitor.service.docker.SystemSoftService;
import com.chua.starter.monitor.service.docker.SystemSoftVersionService;
import com.github.dockerjava.api.model.Image;
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
import java.util.stream.Collectors;

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
    private final SystemSoftContainerMapper systemSoftContainerMapper;
    private final DockerProtocolClientFactory dockerProtocolClientFactory;
    private final SocketSessionTemplate socketSessionTemplate;
    private final MonitorSysGenServerMapper monitorSysGenServerMapper;

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
            Long imageCount = systemSoftImageMapper.selectCount(
                    Wrappers.<SystemSoftImage>lambdaQuery()
                            .eq(SystemSoftImage::getSystemSoftId, softId)
            );

            if (imageCount != null && imageCount > 0) {
                return ReturnResult.error(String.format("该软件还有 %d 个关联的镜像，请先删除镜像", imageCount));
            }

            Long containerCount = systemSoftContainerMapper.selectCount(
                    Wrappers.<SystemSoftContainer>lambdaQuery()
                            .eq(SystemSoftContainer::getSystemSoftId, softId)
            );

            if (containerCount != null && containerCount > 0) {
                return ReturnResult.error(String.format("该软件还有 %d 个关联的容器，请先删除容器", containerCount));
            }

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

            return ReturnResult.ok(0);
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
                if (matched == null) {
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

                    if (null != matchedTagProvider) {
                        ReturnPageResult<DockerTagItem> dockerTagItemReturnPageResult = matchedTagProvider.listTags(soft.getSystemSoftIsOfficial() == 1 ? "library/" + soft.getSystemSoftDockerImage() : soft.getSystemSoftDockerImage(), 1, 10000, dockerRegistry);
                        resultVersionMap.put(key, createSystemSoftVersion(it.getImage(), dockerTagItemReturnPageResult));
                    }
                    resultMap.put(key, soft);
                }
            }

            List<SystemSoft> result = new ArrayList<>(resultMap.values());

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
     *
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
     *
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
     *
     * @param result           软件
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
     *
     * @param result           软件
     * @param resultVersionMap 软件版本
     * @param map              软件id
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
     *
     * @param softId 软件id
     * @param value  软件版本
     */
    private void registerSoftVersion(Integer softId, List<SystemSoftVersion> value) {
        systemSoftVersionService.remove(Wrappers.<SystemSoftVersion>lambdaUpdate().eq(SystemSoftVersion::getSystemSoftId, softId));
        systemSoftVersionService.saveBatch(value);
    }

    /**
     * 创建系统软件版本
     *
     * @param image                         镜像
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
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> importOnlineItems(List<SystemSoft> items) {
        log.info("批量导入在线检索的软件 - 数量: {}", items == null ? 0 : items.size());

        if (items == null || items.isEmpty()) {
            return ReturnResult.error("导入列表为空");
        }

        int savedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (SystemSoft item : items) {
            try {
                // 验证必要字段
                if (!StringUtils.hasText(item.getSystemSoftCode())) {
                    log.warn("软件代码为空，跳过导入 - name: {}", item.getSystemSoftName());
                    skippedCount++;
                    continue;
                }

                if (!StringUtils.hasText(item.getSystemSoftName())) {
                    log.warn("软件名称为空，跳过导入 - code: {}", item.getSystemSoftCode());
                    skippedCount++;
                    continue;
                }

                // 检查是否已存在（根据代码）
                LambdaQueryWrapper<SystemSoft> queryWrapper = Wrappers.lambdaQuery(SystemSoft.class)
                        .eq(SystemSoft::getSystemSoftCode, item.getSystemSoftCode());
                SystemSoft existingSoft = systemSoftMapper.selectOne(queryWrapper);

                if (existingSoft == null) {
                    // 不存在，插入新记录
                    item.setCreateTime(now);
                    item.setUpdateTime(now);

                    // 设置默认值
                    if (item.getSystemSoftStatus() == null) {
                        item.setSystemSoftStatus(1);
                    }
                    if (item.getSystemSoftIsOfficial() == null) {
                        item.setSystemSoftIsOfficial(0);
                    }

                    systemSoftMapper.insert(item);
                    savedCount++;
                    log.debug("新增软件 - code: {}, name: {}", item.getSystemSoftCode(), item.getSystemSoftName());
                } else {
                    // 已存在，更新信息（保留原有的ID和创建时间）
                    existingSoft.setSystemSoftName(item.getSystemSoftName());
                    existingSoft.setSystemSoftDesc(item.getSystemSoftDesc());
                    existingSoft.setSystemSoftIcon(item.getSystemSoftIcon());
                    existingSoft.setSystemSoftDockerImage(item.getSystemSoftDockerImage());

                    // 更新统计信息
                    if (item.getSystemSoftStarCount() != null) {
                        existingSoft.setSystemSoftStarCount(item.getSystemSoftStarCount());
                    }
                    if (item.getSystemSoftPullCount() != null) {
                        existingSoft.setSystemSoftPullCount(item.getSystemSoftPullCount());
                    }

                    existingSoft.setUpdateTime(now);
                    systemSoftMapper.updateById(existingSoft);
                    updatedCount++;
                    log.debug("更新软件 - code: {}, name: {}", item.getSystemSoftCode(), item.getSystemSoftName());
                }
            } catch (Exception e) {
                log.error("导入软件失败 - code: {}, name: {}, error: {}",
                        item.getSystemSoftCode(), item.getSystemSoftName(), e.getMessage(), e);
                skippedCount++;
            }
        }

        log.info("批量导入完成 - 新增: {}, 更新: {}, 跳过: {}, 总计: {}",
                savedCount, updatedCount, skippedCount, items.size());

        return ReturnResult.ok(savedCount + updatedCount,
                String.format("成功导入 %d 个软件（新增 %d，更新 %d，跳过 %d）",
                        savedCount + updatedCount, savedCount, updatedCount, skippedCount));
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

            if (registry.getSystemSoftRegistryStatus() != 1) {
                return ReturnResult.error("仓库未启用");
            }

            // 使用在线搜索功能获取软件信息
            var dockerRegistry = new DockerRegistry();
            dockerRegistry.setType(StringUtils.defaultString(registry.getSystemSoftRegistryType(), ""));
            dockerRegistry.setUrl(StringUtils.defaultString(registry.getSystemSoftRegistryUrl(), ""));
            dockerRegistry.setUsername(registry.getSystemSoftRegistryUsername());
            dockerRegistry.setPassword(registry.getSystemSoftRegistryPassword());

            DockerOnlineSearchProvider searchProvider = getDockerOnlineSearchProvider(dockerRegistry);
            if (searchProvider == null) {
                return ReturnResult.error("未找到适合的搜索提供者");
            }

            // 搜索软件
            var pageResult = searchProvider.search(softwareName, 1, 10, dockerRegistry);
            if (pageResult == null || pageResult.getData() == null || pageResult.getData().getData() == null) {
                return ReturnResult.error("未找到该软件");
            }

            List<DockerSearchItem> items = pageResult.getData().getData();
            if (items.isEmpty()) {
                return ReturnResult.error("未找到该软件");
            }

            // 取第一个匹配的软件
            DockerSearchItem item = items.get(0);

            // 查找是否已存在
            SystemSoft existingSoft = systemSoftMapper.selectOne(
                    Wrappers.<SystemSoft>lambdaQuery()
                            .eq(SystemSoft::getSystemSoftDockerImage, item.getImage())
                            .eq(SystemSoft::getSystemSoftRegistryId, registryId)
            );

            SystemSoft soft;
            if (existingSoft != null) {
                // 更新现有软件
                existingSoft.setSystemSoftName(StringUtils.defaultString(item.getName(), item.getImage()));
                existingSoft.setSystemSoftDesc(item.getDesc());
                existingSoft.setSystemSoftIsOfficial(item.getOfficial() == null ? 0 : item.getOfficial());
                existingSoft.setSystemSoftStarCount(item.getStarCount());
                existingSoft.setSystemSoftPullCount(item.getPullCount());
                existingSoft.setUpdateTime(LocalDateTime.now());
                systemSoftMapper.updateById(existingSoft);
                soft = existingSoft;
                log.info("更新软件信息成功 - softId: {}, softName: {}", soft.getSystemSoftId(), soft.getSystemSoftName());
            } else {
                // 创建新软件
                soft = new SystemSoft();
                soft.setSystemSoftName(StringUtils.defaultString(item.getName(), item.getImage()));
                soft.setSystemSoftCode(StringUtils.defaultString(item.getCode(), item.getImage().replace('/', '-')));
                soft.setSystemSoftDockerImage(item.getImage());
                soft.setSystemSoftDesc(item.getDesc());
                soft.setSystemSoftIsOfficial(item.getOfficial() == null ? 0 : item.getOfficial());
                soft.setSystemSoftRegistryId(registry.getSystemSoftRegistryId());
                soft.setSystemSoftStarCount(item.getStarCount());
                soft.setSystemSoftPullCount(item.getPullCount());
                soft.setSystemSoftStatus(1);
                soft.setCreateTime(LocalDateTime.now());
                systemSoftMapper.insert(soft);
                log.info("创建软件信息成功 - softId: {}, softName: {}", soft.getSystemSoftId(), soft.getSystemSoftName());
            }

            // 同步版本信息
            DockerOnlineSearchTagProvider tagProvider = getDockerOnlineSearchTagProvider(dockerRegistry);
            if (tagProvider != null) {
                try {
                    String imageName = soft.getSystemSoftIsOfficial() == 1
                            ? "library/" + soft.getSystemSoftDockerImage()
                            : soft.getSystemSoftDockerImage();
                    ReturnPageResult<DockerTagItem> tagsResult = tagProvider.listTags(imageName, 1, 100, dockerRegistry);
                    if (tagsResult != null && tagsResult.getData() != null) {
                        List<SystemSoftVersion> versions = createSystemSoftVersion(item.getImage(), tagsResult);
                        if (!versions.isEmpty()) {
                            versions.forEach(v -> v.setSystemSoftId(soft.getSystemSoftId()));
                            // 删除旧版本
                            systemSoftVersionService.remove(
                                    Wrappers.<SystemSoftVersion>lambdaQuery()
                                            .eq(SystemSoftVersion::getSystemSoftId, soft.getSystemSoftId())
                            );
                            // 保存新版本
                            systemSoftVersionService.saveBatch(versions);
                            log.info("同步软件版本成功 - softId: {}, versionCount: {}", soft.getSystemSoftId(), versions.size());
                        }
                    }
                } catch (Exception e) {
                    log.warn("同步软件版本失败 - softId: {}, error: {}", soft.getSystemSoftId(), e.getMessage());
                }
            }

            return ReturnResult.ok(soft, "同步成功");

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

            List<String> tags = new ArrayList<>();

            // 1. 首先从数据库中获取已保存的版本标签
            List<SystemSoftVersion> versions = systemSoftVersionService.list(
                    Wrappers.<SystemSoftVersion>lambdaQuery()
                            .eq(SystemSoftVersion::getSystemSoftId, softId)
                            .eq(SystemSoftVersion::getSystemSoftVersionStatus, 1)
                            .orderByDesc(SystemSoftVersion::getSystemSoftVersionIsLatest)
                            .orderByDesc(SystemSoftVersion::getSystemSoftVersionReleaseTime)
            );

            if (versions != null && !versions.isEmpty()) {
                tags.addAll(versions.stream()
                        .map(SystemSoftVersion::getSystemSoftVersionNumber)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .collect(Collectors.toList())
                );
            }

            // 2. 如果配置了仓库，尝试从仓库在线获取最新标签（异步）
            if (systemSoft.getSystemSoftRegistryId() != null && systemSoft.getSystemSoftDockerImage() != null) {
                try {
                    SystemSoftRegistry registry = systemSoftRegistryMapper.selectById(systemSoft.getSystemSoftRegistryId());
                    if (registry != null && registry.getSystemSoftRegistryStatus() == 1) {
                        var dockerRegistry = new DockerRegistry();
                        dockerRegistry.setType(StringUtils.defaultString(registry.getSystemSoftRegistryType(), ""));
                        dockerRegistry.setUrl(StringUtils.defaultString(registry.getSystemSoftRegistryUrl(), ""));
                        dockerRegistry.setUsername(registry.getSystemSoftRegistryUsername());
                        dockerRegistry.setPassword(registry.getSystemSoftRegistryPassword());

                        DockerOnlineSearchTagProvider tagProvider = getDockerOnlineSearchTagProvider(dockerRegistry);
                        if (tagProvider != null) {
                            String imageName = systemSoft.getSystemSoftIsOfficial() == 1
                                    ? "library/" + systemSoft.getSystemSoftDockerImage()
                                    : systemSoft.getSystemSoftDockerImage();

                            ReturnPageResult<DockerTagItem> tagsResult = tagProvider.listTags(imageName, 1, 100, dockerRegistry);
                            if (tagsResult != null && tagsResult.getData() != null && tagsResult.getData().getData() != null) {
                                List<String> onlineTags = tagsResult.getData().getData().stream()
                                        .map(DockerTagItem::getTag)
                                        .filter(StringUtils::hasText)
                                        .collect(Collectors.toList());

                                // 合并在线标签，去重
                                Set<String> tagSet = new LinkedHashSet<>(tags);
                                tagSet.addAll(onlineTags);
                                tags = new ArrayList<>(tagSet);

                                log.debug("从仓库获取到 {} 个在线标签", onlineTags.size());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("从仓库获取在线标签失败 - softId: {}, error: {}", softId, e.getMessage());
                    // 忽略在线获取失败的错误，使用数据库中的标签
                }
            }

            // 3. 如果没有任何标签，返回默认标签
            if (tags.isEmpty()) {
                tags.add("latest");
                log.debug("未找到任何标签，返回默认标签: latest");
            }

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
     * 反射调用 DockerProtocolClient 列举镜像，兼容不同签名
     */
    private List<?> tryInvokeListImages(DockerProtocolClient client, SystemSoftRegistry registry) {
        try {
            // 直接调用 listImages() 方法
            return client.listImages();
        } catch (Throwable ex) {
            log.warn("调用 DockerProtocolClient.listImages() 失败: {}, 尝试反射调用", ex.getMessage());
            try {
                // 尝试通过反射调用 listImage() 方法（单数形式，兼容旧版本）
                return (List<?>) client.getClass().getMethod("listImage").invoke(client);
            } catch (Throwable ex2) {
                log.warn("通过反射调用 DockerProtocolClient.listImage() 也失败: {}", ex2.getMessage());
            }
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

    @Override
    public ReturnResult<Map<String, Object>> syncImagesFromServers(List<Integer> serverIds) {
        log.debug("从服务器同步Docker镜像 - serverIds: {}", serverIds);

        try {
            // 生成operationId
            String operationId = "sync-images-" + System.currentTimeMillis();

            // 结果统计
            int totalImages = 0;
            int successCount = 0;
            int errorCount = 0;

            // 遍历每台服务器
            for (Integer serverId : serverIds) {
                try {
                    log.debug("开始同步服务器镜像 - serverId: {}", serverId);

                    // 获取服务器信息
                    MonitorSysGenServer server = monitorSysGenServerMapper.selectById(serverId);
                    if (server == null) {
                        log.warn("服务器不存在 - serverId: {}", serverId);
                        errorCount++;
                        continue;
                    }

                    // 推送开始同步通知
                    pushSyncProgress(operationId, serverId, server.getMonitorSysGenServerName(),
                            0, "开始同步镜像列表...");

                    // 创建Docker客户端
                    DockerProtocolClient dockerClient = getDockerClient(serverId);
                    if (dockerClient == null) {
                        log.warn("无法创建Docker客户端 - serverId: {}", serverId);
                        pushSyncProgress(operationId, serverId, server.getMonitorSysGenServerName(),
                                0, "无法连接到Docker服务");
                        errorCount++;
                        continue;
                    }

                    // 获取镜像列表
                    List<Image> images = dockerClient.listImages();
                    if (images == null || images.isEmpty()) {
                        log.debug("服务器没有镜像 - serverId: {}", serverId);
                        pushSyncProgress(operationId, serverId, server.getMonitorSysGenServerName(),
                                100, "该服务器没有镜像");
                        continue;
                    }

                    totalImages += images.size();
                    log.debug("获取到 {} 个镜像 - serverId: {}", images.size(), serverId);

                    // 保存镜像信息
                    int imageIndex = 0;
                    for (Image image : images) {
                        try {
                            imageIndex++;
                            int progress = (int) ((double) imageIndex / images.size() * 100);

                            // 推送同步进度
                            pushSyncProgress(operationId, serverId, server.getMonitorSysGenServerName(),
                                    progress, String.format("同步镜像 %d/%d", imageIndex, images.size()));

                            // 解析镜像名称和标签
                            String imageName = image.getRepoTags() != null && image.getRepoTags().length != 0
                                    ? image.getRepoTags()[0] : image.getId();

                            String[] parts = imageName.split(":");
                            String name = parts.length > 0 ? parts[0] : imageName;
                            String tag = parts.length > 1 ? parts[1] : "latest";

                            // 查找或创建软件记录
                            SystemSoft soft = findOrCreateSoftByImageName(name);

                            // 检查镜像是否已存在
                            LambdaQueryWrapper<SystemSoftImage> wrapper = Wrappers.lambdaQuery();
                            wrapper.eq(SystemSoftImage::getSystemSoftId, soft.getSystemSoftId())
                                    .eq(SystemSoftImage::getSystemSoftImageServerId, serverId)
                                    .eq(SystemSoftImage::getSystemSoftImageImageId, image.getId());
                            SystemSoftImage existing = systemSoftImageMapper.selectOne(wrapper);

                            if (existing != null) {
                                // 更新现有记录
                                existing.setSystemSoftImageTag(tag);
                                existing.setSystemSoftImageSize(image.getSize());
                                existing.setSystemSoftImageStatus(SystemSoftImageStatus.AVAILABLE);
                                existing.setUpdateTime(LocalDateTime.now());
                                systemSoftImageMapper.updateById(existing);
                            } else {
                                // 创建新记录
                                SystemSoftImage newImage = new SystemSoftImage();
                                newImage.setSystemSoftId(soft.getSystemSoftId());
                                newImage.setSystemSoftImageServerId(serverId);
                                newImage.setSystemSoftImageServerName(server.getMonitorSysGenServerName());
                                newImage.setSystemSoftImageImageId(image.getId());
                                newImage.setSystemSoftImageName(name);
                                newImage.setSystemSoftImageTag(tag);
                                newImage.setSystemSoftImageFullName(imageName);
                                newImage.setSystemSoftImageSize(image.getSize());
                                newImage.setSystemSoftImageStatus(SystemSoftImageStatus.AVAILABLE);
                                newImage.setCreateTime(LocalDateTime.now());
                                systemSoftImageMapper.insert(newImage);
                            }

                            successCount++;

                        } catch (Exception e) {
                            log.error("保存镜像信息失败 - image: {}", image.getId(), e);
                            errorCount++;
                        }
                    }

                    // 推送完成通知
                    pushSyncProgress(operationId, serverId, server.getMonitorSysGenServerName(),
                            100, String.format("同步完成，共 %d 个镜像", images.size()));

                } catch (Exception e) {
                    log.error("同步服务器镜像失败 - serverId: {}", serverId, e);
                    errorCount++;
                }
            }

            // 推送总体完成通知
            pushSyncComplete(operationId, totalImages, successCount, errorCount);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("operationId", operationId);
            result.put("syncCount", successCount);
            result.put("totalImages", totalImages);
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);

            log.debug("同步镜像完成 - operationId: {}, totalImages: {}, successCount: {}, errorCount: {}",
                    operationId, totalImages, successCount, errorCount);

            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("同步镜像异常", e);
            return ReturnResult.error("同步镜像异常: " + e.getMessage());
        }
    }

    /**
     * 根据镜像名称查找或创建软件
     */
    private SystemSoft findOrCreateSoftByImageName(String imageName) {
        // 清理镜像名称
        String cleanName = imageName.replaceAll("^.*/", ""); // 移除仓库前缀
        String softCode = cleanName.replaceAll("[^a-zA-Z0-9-]", "-"); // 生成软件代码

        // 查找已存在的软件
        SystemSoft existing = systemSoftMapper.selectBySoftCode(softCode);
        if (existing != null) {
            return existing;
        }

        // 创建新软件
        SystemSoft newSoft = new SystemSoft();
        newSoft.setSystemSoftName(cleanName);
        newSoft.setSystemSoftCode(softCode);
        newSoft.setSystemSoftDockerImage(imageName);
        newSoft.setSystemSoftCategory("Docker");
        newSoft.setSystemSoftStatus(1);
        newSoft.setSystemSoftIsOfficial(0);
        newSoft.setSystemSoftDesc("从服务器同步的Docker镜像");
        newSoft.setCreateTime(LocalDateTime.now());

        systemSoftMapper.insert(newSoft);
        log.debug("创建新软件 - softCode: {}, imageName: {}", softCode, imageName);

        return newSoft;
    }

    /**
     * 推送同步进度
     */
    private void pushSyncProgress(String operationId, Integer serverId, String serverName,
                                  int progress, String message) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operationId", operationId);
            data.put("type", "software_sync_progress");
            data.put("serverId", serverId);
            data.put("serverName", serverName);
            data.put("progress", progress);
            data.put("message", message);
            data.put("timestamp", System.currentTimeMillis());

            socketSessionTemplate.send(MonitorSocketTopics.Monitor.Software.SYNC_PROGRESS, data);
        } catch (Exception e) {
            log.error("推送同步进度失败", e);
        }
    }

    /**
     * 推送同步完成
     */
    private void pushSyncComplete(String operationId, int totalImages, int successCount, int errorCount) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operationId", operationId);
            data.put("type", "operation_complete");
            data.put("title", "镜像同步");
            data.put("message", String.format("同步完成，共 %d 个镜像，成功 %d 个，失败 %d 个",
                    totalImages, successCount, errorCount));
            data.put("totalImages", totalImages);
            data.put("successCount", successCount);
            data.put("errorCount", errorCount);
            data.put("timestamp", System.currentTimeMillis());

            socketSessionTemplate.send(MonitorSocketTopics.Monitor.Operation.COMPLETE, data);
        } catch (Exception e) {
            log.error("推送同步完成失败", e);
        }
    }
}
