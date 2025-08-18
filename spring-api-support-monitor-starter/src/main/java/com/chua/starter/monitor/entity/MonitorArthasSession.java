package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Arthas Session 记录表
 * 用于管理异步执行的session生命周期
 */
@Data
@TableName("monitor_arthas_session")
public class MonitorArthasSession {

    // 状态常量
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_INTERRUPTED = "INTERRUPTED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    // 命令类型常量
    public static final String TYPE_TRACE = "trace";
    public static final String TYPE_THREAD = "thread";
    public static final String TYPE_JVM = "jvm";
    public static final String TYPE_HEAP = "heap";
    public static final String TYPE_OTHER = "other";
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 会话ID（Arthas生成的唯一标识）
     */
    @TableField("session_id")
    private String sessionId;
    /**
     * 消费者ID（用于拉取结果）
     */
    @TableField("consumer_id")
    private String consumerId;
    /**
     * 任务ID（用于中断任务）
     */
    @TableField("job_id")
    private Integer jobId;
    /**
     * 节点ID
     */
    @TableField("node_id")
    private String nodeId;
    /**
     * Arthas HTTP API地址
     */
    @TableField("http_url")
    private String httpUrl;
    /**
     * 执行的命令
     */
    @TableField("command")
    private String command;
    /**
     * 命令类型（如：trace、thread、jvm等）
     */
    @TableField("command_type")
    private String commandType;
    /**
     * 会话状态：RUNNING-运行中, COMPLETED-已完成, INTERRUPTED-已中断, EXPIRED-已过期
     */
    @TableField("status")
    private String status;
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
    /**
     * 过期时间（超过此时间自动清理）
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;
}
