package com.chua.starter.monitor.constant;

/**
 * 系统软件记录状态常量
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
public class SystemSoftRecordStatus {

    /**
     * 进行中
     */
    public static final Integer IN_PROGRESS = 1;

    /**
     * 成功
     */
    public static final Integer SUCCESS = 2;

    /**
     * 失败
     */
    public static final Integer FAILED = 3;

    /**
     * 已取消
     */
    public static final Integer CANCELLED = 4;

    /**
     * 等待中
     */
    public static final Integer WAITING = 0;

    /**
     * 私有构造函数，防止实例化
     */
    private SystemSoftRecordStatus() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 获取状态描述
     *
     * @param status 状态值
     * @return 状态描述
     */
    public static String getStatusDescription(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        switch (status) {
            case 0:
                return "等待中";
            case 1:
                return "进行中";
            case 2:
                return "成功";
            case 3:
                return "失败";
            case 4:
                return "已取消";
            default:
                return "未知状态";
        }
    }

    /**
     * 验证状态值是否有效
     *
     * @param status 状态值
     * @return 是否有效
     */
    public static boolean isValidStatus(Integer status) {
        return status != null && status >= 0 && status <= 4;
    }
}