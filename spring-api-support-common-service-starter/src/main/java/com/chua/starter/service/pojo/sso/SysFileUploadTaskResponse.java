package com.chua.starter.service.pojo.sso;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 任务创建响应
 * @author CH
 * @since 2024/12/4
 */
@Data
@Schema(description = "任务创建响应")
public class SysFileUploadTaskResponse {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private String taskId;

    /**
     * 任务数量
     */
    @Schema(description = "任务数量")
    private Integer setSysTaskSsoNumber;

    /**
     * 当前文件大小
     */
    @TableField(value = "sys_task_sso_chuck")
    @Schema(description="文件大小")
    private Long sysTaskSsoChuck;

    /**
     * 文件名称
     */
    @TableField(value = "sys_task_sso_name")
    @Schema(description="文件名称/url")
    private String url;
}
