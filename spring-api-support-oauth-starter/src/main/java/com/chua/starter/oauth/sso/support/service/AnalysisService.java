package com.chua.starter.oauth.sso.support.service;

import com.chua.starter.oauth.sso.support.entity.AnalysisResult;

import java.util.List;

/**
 * OAuth认证分析服务接口
 * 
 * 提供OAuth认证数据分析功能，支持内部分析和AI分析
 * 
 * @author CH
 * @version 4.0.0.32
 */
public interface AnalysisService {

    /**
     * 执行分析
     * 
     * @param analysisType 分析类型（internal, ai）
     * @param days 分析天数
     * @return 分析结果
     */
    AnalysisResult performAnalysis(String analysisType, int days);

    /**
     * 获取支持的分析类型
     * 
     * @return 支持的分析类型列表
     */
    List<String> getSupportedAnalysisTypes();

    /**
     * 清除分析缓存
     */
    void clearCache();

    /**
     * 检查分析类型是否支持
     * 
     * @param analysisType 分析类型
     * @return 是否支持
     */
    boolean isAnalysisTypeSupported(String analysisType);

    /**
     * 获取分析配置
     * 
     * @return 分析配置信息
     */
    AnalysisConfig getAnalysisConfig();

    /**
     * 更新分析配置
     * 
     * @param config 新的分析配置
     */
    void updateAnalysisConfig(AnalysisConfig config);

    /**
     * 分析配置
     */
    interface AnalysisConfig {
        /**
         * 是否启用AI分析
         */
        boolean isAiAnalysisEnabled();

        /**
         * AI分析提供者
         */
        String getAiProvider();

        /**
         * 分析缓存时间（分钟）
         */
        int getCacheTimeMinutes();

        /**
         * 最大分析天数
         */
        int getMaxAnalysisDays();
    }
}
