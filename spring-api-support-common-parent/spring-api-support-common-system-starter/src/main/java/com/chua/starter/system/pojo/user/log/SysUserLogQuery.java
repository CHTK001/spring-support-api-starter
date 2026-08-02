package com.chua.starter.system.pojo.user.log;

import com.chua.starter.system.pojo.DateQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户日志查询
 *
 * @author CH
 * @since 2024/8/23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户日志查询")
public class SysUserLogQuery extends DateQuery {

    /**
     * 账号
     */
    @Schema(description = "账号")
    @Size(max = 255, message = "账号最大长度需小于255")
    private String sysLogUsername;

    /**
     * 请求模块
     */
    @Schema(description = "请求模块")
    private String sysLogFrom;

    /**
     * 请求地址
     */
    @Schema(description = "请求地址")
    private String sysLogAddress;

    /**
     * 请求IP
     */
    @Schema(description = "请求IP")
    private String sysLogIp;

    /**
     * 请求状态: 0:失败 1:成功
     */
    @Schema(description = "请求状态: 0:失败 1:成功")
    private Integer sysLogStatus;
}
