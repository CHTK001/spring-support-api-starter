package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运维项目管理（含服务器连接信息）
 */
@Schema(description = "运维项目管理")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_devops_project")
public class SysDevopsProject extends SysBase {

    @TableId(value = "project_id", type = IdType.AUTO)
    @Schema(description = "主键")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long projectId;

    @TableField(value = "project_name")
    @Schema(description = "项目名称")
    @NotBlank(message = "项目名称不能为空", groups = {AddGroup.class})
    private String projectName;

    @TableField(value = "project_description")
    @Schema(description = "项目描述")
    private String projectDescription;

    @TableField(value = "project_icon")
    @Schema(description = "项目图标URL")
    private String projectIcon;

    @TableField(value = "server_os")
    @Schema(description = "服务器OS类型：linux/windows/macos/unknown")
    private String serverOs;

    @TableField(value = "sort_order")
    @Schema(description = "排序")
    private Integer sortOrder;

    @TableField(value = "status")
    @Schema(description = "状态：1启用 0禁用")
    private Integer status;

    // ── 服务器连接信息（AES加密存储） ──

    @TableField(value = "server_type")
    @Schema(description = "连接类型：SSH/WINRM/LOCAL")
    private String serverType;

    @TableField(value = "server_host")
    @Schema(description = "主机地址（加密）")
    private String serverHost;

    @TableField(value = "server_port")
    @Schema(description = "端口号")
    private Integer serverPort;

    @TableField(value = "server_username")
    @Schema(description = "用户名（加密）")
    private String serverUsername;

    @TableField(value = "server_password")
    @Schema(description = "密码（加密）")
    private String serverPassword;

    @TableField(value = "server_private_key")
    @Schema(description = "SSH私钥（加密）")
    private String serverPrivateKey;

    @TableField(value = "server_connect_timeout")
    @Schema(description = "连接超时（秒）")
    private Integer serverConnectTimeout;

    @TableField(value = "server_enabled")
    @Schema(description = "服务器信息是否激活：1是 0否")
    private Integer serverEnabled;

    @TableField(value = "server_activated_at")
    @Schema(description = "最近激活时间")
    private java.time.LocalDateTime serverActivatedAt;

    // ── 路径配置 ──

    @TableField(value = "log_path")
    @Schema(description = "日志路径")
    private String logPath;

    @TableField(value = "config_path")
    @Schema(description = "配置文件路径")
    private String configPath;

    @TableField(value = "lib_path")
    @Schema(description = "依赖目录路径")
    private String libPath;

    @TableField(value = "root_path")
    @Schema(description = "项目根目录")
    private String rootPath;

    // ── 操作脚本 ──

    @TableField(value = "start_script")
    @Schema(description = "启动脚本")
    private String startScript;

    @TableField(value = "stop_script")
    @Schema(description = "停止脚本")
    private String stopScript;

    @TableField(value = "restart_script")
    @Schema(description = "重启脚本")
    private String restartScript;

    // ── 运行时状态（不存库，动态查询） ──

    @TableField(exist = false)
    @Schema(description = "运行时状态：running/stopped/unknown")
    private String runtimeStatus;

    @TableField(exist = false)
    @Schema(description = "关联密钥数量")
    private Integer secretCount;
}