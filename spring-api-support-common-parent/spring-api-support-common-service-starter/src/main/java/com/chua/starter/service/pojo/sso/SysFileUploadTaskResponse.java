package com.chua.starter.service.pojo.sso;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 任务创建响应
 * @since 2024/12/4
 * @author CH
 */
@Data
@Schema(description = "任务创建响应")
public class SysFileUploadTaskResponse {

    /**
      * 任务标识
     */
    @Schema(description = "任务ID")
    /** 任务标识 */
    private String taskId;

    /**
    * 任务数量
     */
    @Schema(description = "任务数量")
    /** 集合SYS任务单点登录数字 */
    private Integer setSysTaskSsoNumber;

    /**
    * 当前文件大小
     */
    @TableField(value = "sys_task_sso_chuck")
    @Schema(description="文件大小")
    /** SYS任务ssochuck */
    private Long sysTaskSsoChuck;

    /**
    * 文件名称
     */
    @TableField(value = "sys_task_sso_name")
    @Schema(description="文件名称/url")
    /** URL */
    private String url;
}
