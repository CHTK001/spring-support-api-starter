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
 *
 * @since 2026/08/03
 * @author CH
 */
@Schema(description = "运维项目管理")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_devops_project")
public class SysDevopsProject extends SysBase {

    /**
     * 主键
     */
    @TableId(value = "project_id", type = IdType.AUTO)
    @Schema(description = "主键")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    /** projectid */
    private Long projectId;

    /**
    * 项目名称
     */
    @TableField(value = "project_name")
    @Schema(description = "项目名称")
    @NotBlank(message = "项目名称不能为空", groups = {AddGroup.class})
    /** Project名称 */
    private String projectName;

    /**
    * 项目描述
     */
    @TableField(value = "project_description")
    @Schema(description = "项目描述")
    /** Project描述 */
    private String projectDescription;

    /**
    * 项目图标URL
     */
    @TableField(value = "project_icon")
    @Schema(description = "项目图标URL")
    /** Projecticon */
    private String projectIcon;

    /**
    * 服务器OS类型
     */
    @TableField(value = "server_os")
    @Schema(description = "服务器OS类型：linux/windows/macos/unknown")
    /** 服务器OS */
    private String serverOs;

    /**
    * 排序
     */
    @TableField(value = "sort_order")
    @Schema(description = "排序")
    /** 排序排序 */
    private Integer sortOrder;

    /**
    * 状态
     */
    @TableField(value = "status")
    @Schema(description = "状态：1启用 0禁用")
    /** 状态 */
    private Integer status;

    // ── 服务器连接信息（AES加密存储） ──

    /**
     * 连接类型
     */
    @TableField(value = "server_type")
    @Schema(description = "连接类型：SSH/WINRM/LOCAL")
    /** 服务器类型 */
    private String serverType;

    /**
    * 主机地址（加密）
     */
    @TableField(value = "server_host")
    @Schema(description = "主机地址（加密）")
    /** 服务器主机 */
    private String serverHost;

    /**
    * 端口号
     */
    @TableField(value = "server_port")
    @Schema(description = "端口号")
    /** 服务器端口 */
    private Integer serverPort;

    /**
    * 用户名（加密）
     */
    @TableField(value = "server_username")
    @Schema(description = "用户名（加密）")
    /** 服务器用户名 */
    private String serverUsername;

    /**
    * 密码（加密）
     */
    @TableField(value = "server_password")
    @Schema(description = "密码（加密）")
    /** 服务器密码 */
    private String serverPassword;

    /**
    * SSH私钥（加密）
     */
    @TableField(value = "server_private_key")
    @Schema(description = "SSH私钥（加密）")
    /** 服务器私有密钥 */
    private String serverPrivateKey;

    /**
    * 连接超时（秒）
     */
    @TableField(value = "server_connect_timeout")
    @Schema(description = "连接超时（秒）")
    /** 服务器连接超时 */
    private Integer serverConnectTimeout;

    /**
    * 服务器信息是否激活
     */
    @TableField(value = "server_enabled")
    @Schema(description = "服务器信息是否激活：1是 0否")
    /** 服务器是否启用 */
    private Integer serverEnabled;

    /**
    * 最近激活时间
     */
    @TableField(value = "server_activated_at")
    @Schema(description = "最近激活时间")
    /** 服务器activatedat */
    private java.time.LocalDateTime serverActivatedAt;

    // ── 路径配置 ──

    /**
     * 日志路径
     */
    @TableField(value = "log_path")
    @Schema(description = "日志路径")
    /** 日志路径 */
    private String logPath;

    /**
    * 配置文件路径
     */
    @TableField(value = "config_path")
    @Schema(description = "配置文件路径")
    /** 配置路径 */
    private String configPath;

    /**
    * 依赖目录路径
     */
    @TableField(value = "lib_path")
    @Schema(description = "依赖目录路径")
    /** LIB路径 */
    private String libPath;

    /**
    * 项目根目录
     */
    @TableField(value = "root_path")
    @Schema(description = "项目根目录")
    /** 根级路径 */
    private String rootPath;

    // ── 操作脚本 ──

    /**
     * 启动脚本
     */
    @TableField(value = "start_script")
    @Schema(description = "启动脚本")
    /** 开始script */
    private String startScript;

    /**
    * 停止脚本
     */
    @TableField(value = "stop_script")
    @Schema(description = "停止脚本")
    /** Stopscript */
    private String stopScript;

    /**
    * 重启脚本
     */
    @TableField(value = "restart_script")
    @Schema(description = "重启脚本")
    /** Restartscript */
    private String restartScript;

    // ── 运行时状态（不存库，动态查询） ──

    /**
     * 运行时状态
     */
    @TableField(exist = false)
    @Schema(description = "运行时状态：running/stopped/unknown")
    /** Runtime状态 */
    private String runtimeStatus;

    /**
    * 关联密钥数量
     */
    @TableField(exist = false)
    @Schema(description = "关联密钥数量")
    /** Secret数量 */
    private Integer secretCount;
}