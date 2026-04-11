package com.chua.starter.project.service;

import com.chua.common.support.ai.result.PredictResult;
import com.chua.common.support.ai.result.PredictResultObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.pojo.ImageRequest;

import java.io.File;

/**
 * CapSpot 模板点击定位服务。
 *
 * @author CH
 * @since 2026/4/7 10:00
 */
public interface CapSpotService {

    /**
     * 在目标图中定位模板图中的多个点击区域。
     *
     * @param templateFile 模板图
     * @param targetFile   目标图
     * @param request      请求参数
     * @return 返回完整命中列表，而不是仅返回 top1
     */
    ReturnResult<PredictResultObject<PredictResult>> detect(File templateFile, File targetFile, ImageRequest request);
}
