package com.chua.starter.video.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.provider.ResourceProvider;
import com.chua.starter.video.service.ResourceSearchManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源检索管理服务实现
 *
 * @author CH
 */
@Service
@Slf4j
public class ResourceSearchManagerImpl implements ResourceSearchManager {

    @Autowired
    private List<ResourceProvider> resourceProviders;

    @Override
    public ReturnResult<List<VideoInfo>> searchFromAllResources(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return ReturnResult.error("关键词不能为空");
        }

        List<VideoInfo> result = new ArrayList<>();
        List<ResourceProvider> availableProviders = getAvailableProviders();

        if (availableProviders.isEmpty()) {
            return ReturnResult.error("没有可用的资源提供者");
        }

        // 按优先级排序后依次检索
        for (ResourceProvider provider : availableProviders) {
            try {
                ReturnResult<List<VideoInfo>> searchResult = provider.searchResource(keyword);
                if (searchResult.isOk() && searchResult.getData() != null && !searchResult.getData().isEmpty()) {
                    result.addAll(searchResult.getData());
                    // 找到结果后可以选择是否继续检索其他资源
                    // 如果需要合并所有资源结果，则不break
                    // 如果只需要第一个有结果的资源，则可以break
                    // break;
                }
            } catch (Exception e) {
                log.error("从资源 [{}] 检索视频时发生错误", provider.getProviderName(), e);
            }
        }

        if (result.isEmpty()) {
            return ReturnResult.error("未找到相关视频资源");
        }

        return ReturnResult.success(result);
    }

    @Override
    public ReturnResult<List<VideoInfo>> searchFromResource(String keyword, String providerName) {
        if (!StringUtils.hasText(keyword)) {
            return ReturnResult.error("关键词不能为空");
        }

        if (!StringUtils.hasText(providerName)) {
            return ReturnResult.error("资源提供者名称不能为空");
        }

        ResourceProvider targetProvider = resourceProviders.stream()
                .filter(provider -> providerName.equals(provider.getProviderName()) && provider.isAvailable())
                .findFirst()
                .orElse(null);

        if (targetProvider == null) {
            return ReturnResult.error("指定的资源提供者不存在或不可用");
        }

        try {
            return targetProvider.searchResource(keyword);
        } catch (Exception e) {
            log.error("从资源 [{}] 检索视频时发生错误", providerName, e);
            return ReturnResult.error("检索过程中发生错误");
        }
    }

    @Override
    public List<String> getAvailableResources() {
        return getAvailableProviders().stream()
                .map(ResourceProvider::getProviderName)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有可用的资源提供者，并按优先级排序
     *
     * @return 可用的资源提供者列表
     */
    private List<ResourceProvider> getAvailableProviders() {
        return resourceProviders.stream()
                .filter(ResourceProvider::isAvailable)
                .sorted(Comparator.comparingInt(ResourceProvider::getPriority))
                .collect(Collectors.toList());
    }
} 