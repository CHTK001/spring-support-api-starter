package com.chua.starter.monitor.starter.constant;

/**
 * 服务器连接状态常量
 *
 * @author CH
 * @since 2024/12/20
 */
public class ServerConnectionStatus {

    /**
     * 离线
     */
    public static final int OFFLINE = 0;

    /**
     * 在线
     */
    public static final int ONLINE = 1;

    /**
     * 连接中
     */
    public static final int CONNECTING = 2;

    /**
     * 连接失败
     */
    public static final int FAILED = 3;

    /**
     * 获取状态描述
     *
     * @param status 状态码
     * @return 状态描述
     */
    public static String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case OFFLINE:
                return "离线";
            case ONLINE:
                return "在线";
            case CONNECTING:
                return "连接中";
            case FAILED:
                return "连接失败";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否在线
     *
     * @param status 状态码
     * @return 是否在线
     */
    public static boolean isOnline(Integer status) {
        return status != null && status == ONLINE;
    }

    /**
     * 判断是否离线
     *
     * @param status 状态码
     * @return 是否离线
     */
    public static boolean isOffline(Integer status) {
        return status == null || status == OFFLINE;
    }

    /**
     * 判断是否连接中
     *
     * @param status 状态码
     * @return 是否连接中
     */
    public static boolean isConnecting(Integer status) {
        return status != null && status == CONNECTING;
    }

    /**
     * 判断是否连接失败
     *
     * @param status 状态码
     * @return 是否连接失败
     */
    public static boolean isFailed(Integer status) {
        return status != null && status == FAILED;
    }
}
