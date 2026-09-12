package com.chua.starter.geo.model;

/**
* 下载任务状态枚举。
*
* @author CH
* @since 1.0.0
 */
public enum TaskStatus {
    /** 待处理 */
    PENDING,
    /** 下载中 */
    DOWNLOADING,
    /** 已暂停 */
    PAUSED,
    /** 成功率过低，等待用户决策 */
    PENDING_DECISION,
    /** 拼接中 */
    MERGING,
    /** 处理中 */
    PROCESSING,
    /** 导出中 */
    EXPORTING,
    /** 已完成 */
    COMPLETED,
    /** 部分失败但已自动导出 */
    COMPLETED_WITH_GAPS,
    /** 失败 */
    FAILED,
    /** 已取消 */
    CANCELLED
}
