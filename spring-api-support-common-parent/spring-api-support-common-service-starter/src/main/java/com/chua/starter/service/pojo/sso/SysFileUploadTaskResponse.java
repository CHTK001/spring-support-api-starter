package com.chua.starter.service.pojo.sso;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 任务创建响应
 * @since 2024/12/4
 * @author CH
 */
@Schema(description = "任务创建响应")
public record SysFileUploadTaskResponse(
        /**
         * 任务标识
         */
        @Schema(description = "任务ID")
        /**
         * 任务标识
         */
        String taskId,
        /**
         * 任务数量
         */
        @Schema(description = "任务数量")
        /**
         * 集合SYS任务单点登录数字
         */
        Integer setSysTaskSsoNumber,
        /**
         * 当前文件大小
         */
        @Schema(description = "文件大小")
        /**
         * SYS任务ssochuck
         */
        Long sysTaskSsoChuck,
        /**
         * 文件名称
         */
        @Schema(description = "文件名称/url")
        /**
         * URL
         */
        String url
) {
}
