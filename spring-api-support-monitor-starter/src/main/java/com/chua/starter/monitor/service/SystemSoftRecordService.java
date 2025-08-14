package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftRecord;

/**
 * 软件安装记录服务
 *
 * 作者: CH 时间: 2025/08/14
 */
public interface SystemSoftRecordService extends IService<SystemSoftRecord> {

    /**
     * 安装（多台服务器）
     * 
     * @param systemSoftId        软件ID 示例:1
     * @param systemSoftVersionId 版本ID 示例:10
     * @param serverIds           服务器ID数组 示例:[1,2,3]
     */
    ReturnResult<Boolean> install(Integer systemSoftId, Integer systemSoftVersionId, Integer[] serverIds);

    /**
     * 安装（带方式与参数）
     * 
     * @param systemSoftId        软件ID 示例:1
     * @param systemSoftVersionId 版本ID 示例:10
     * @param serverIds           服务器ID数组 示例:[1,2,3]
     * @param method              安装方式（DOCKER_CLI/COMPOSE/SWARM） 示例:DOCKER_CLI
     * @param params              安装参数(JSON文本) 示例:{"env":["TZ=Asia/Shanghai"]}
     */
    ReturnResult<Boolean> install(Integer systemSoftId, Integer systemSoftVersionId, Integer[] serverIds, String method,
            String params);

    /**
     * 卸载（多台服务器）
     * 
     * @param systemSoftId        软件ID 示例:1
     * @param systemSoftVersionId 版本ID 示例:10
     * @param serverIds           服务器ID数组 示例:[1,2]
     */
    ReturnResult<Boolean> uninstall(Integer systemSoftId, Integer systemSoftVersionId, Integer[] serverIds);
}
