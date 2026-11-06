package com.chua.starter.tenant.server.provision;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 一次租户创建任务的运行快照。
 *
 * <p>任务由后台虚拟线程推进，前端随时可能来取快照（套接字断线兜底）或请求取消，
 * 因此所有写操作都加锁，读操作走 {@link #toMap()} 生成不可变快照——直接把字段暴露出去，
 * 前端序列化时正好撞上后台线程改值，会读到「步骤已完成、百分比还是上一步」这种自相矛盾的状态。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
public class TenantProvisionTask {

    /**
     * 任务标识
     */
    private final String taskId;

    /**
     * 租户标识，保存记录后才有值
     */
    private String tenantId;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 租户登录账号
     */
    private String tenantUsername;

    /**
     * 任务状态
     */
    private Status status = Status.RUNNING;

    /**
     * 进度百分比（0-99，完成后置 100）
     */
    private int percentage;

    /**
     * 当前步骤标识
     */
    private String currentKey;

    /**
     * 当前步骤标题
     */
    private String currentTitle;

    /**
     * 当前步骤说明
     */
    private String currentDescription;

    /**
     * 步骤列表
     */
    private final List<Step> steps = new ArrayList<>();

    /**
     * 明细日志（最多保留 200 条）
     */
    private final List<String> details = new ArrayList<>();

    /**
     * 结束时的汇总信息
     */
    private String message;

    /**
     * 创建时间
     */
    private final long createTime = System.currentTimeMillis();

    /**
     * 结束时间；未结束为 null
     */
    private Long finishTime;

    /**
     * 是否已请求取消
     */
    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);

    /**
     * 创建任务。
     *
     * @param taskId 任务标识
     */
    public TenantProvisionTask(String taskId) {
        this.taskId = taskId;
    }

    /**
     * 追加一条明细。
     *
     * @param detail 明细文本，空值忽略
     */
    public synchronized void appendDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            return;
        }
        details.add(detail);
        while (details.size() > 200) {
            details.remove(0);
        }
    }

    /**
     * 请求取消任务。
     *
     * @return 首次请求返回 true；重复请求返回 false
     */
    public boolean requestCancel() {
        return cancelRequested.compareAndSet(false, true);
    }

    /**
     * 是否已请求取消。
     *
     * @return 已请求返回 true
     */
    public boolean isCancelRequested() {
        return cancelRequested.get();
    }

    /**
     * 按步骤序号推进百分比。
     *
     * <p>上限钉在 99：最后一步的 100 只由成功收尾写入，否则「还差最后一步」也会显示 100%，
     * 用户会以为已经完成。</p>
     *
     * @param index 当前步骤序号（0 起）
     * @param total 步骤总数
     */
    public synchronized void updatePercentage(int index, int total) {
        if (total <= 0) {
            return;
        }
        int next = (int) Math.round((double) index * 100.0 / (double) total);
        this.percentage = Math.max(0, Math.min(99, next));
    }

    /**
     * 生成快照。
     *
     * @return 可直接序列化的快照
     */
    public synchronized Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>(16);
        map.put("taskId", taskId);
        map.put("tenantId", tenantId);
        map.put("tenantName", tenantName);
        map.put("tenantUsername", tenantUsername);
        map.put("status", status == null ? Status.RUNNING.name() : status.name());
        map.put("percentage", percentage);
        map.put("currentKey", currentKey);
        map.put("currentTitle", currentTitle);
        map.put("currentDescription", currentDescription);
        map.put("message", message);
        map.put("cancellable", !isFinished() && !cancelRequested.get());
        map.put("finished", isFinished());
        map.put("createTime", createTime);
        map.put("finishTime", finishTime);
        List<Map<String, Object>> stepMaps = new ArrayList<>(steps.size());
        for (Step step : steps) {
            stepMaps.add(step.toMap());
        }
        map.put("steps", stepMaps);
        map.put("details", new ArrayList<>(details));
        return map;
    }

    /**
     * 任务是否已结束。
     *
     * @return 成功 / 失败 / 取消返回 true
     */
    public boolean isFinished() {
        return status != null && status != Status.RUNNING;
    }

    /**
     * 获取任务标识。
     *
     * @return 任务标识
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 获取租户标识。
     *
     * @return 租户标识
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 设置租户标识。
     *
     * @param tenantId 租户标识
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 获取租户名称。
     *
     * @return 租户名称
     */
    public String getTenantName() {
        return tenantName;
    }

    /**
     * 设置租户名称。
     *
     * @param tenantName 租户名称
     */
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    /**
     * 获取租户账号。
     *
     * @return 租户账号
     */
    public String getTenantUsername() {
        return tenantUsername;
    }

    /**
     * 设置租户账号。
     *
     * @param tenantUsername 租户账号
     */
    public void setTenantUsername(String tenantUsername) {
        this.tenantUsername = tenantUsername;
    }

    /**
     * 获取任务状态。
     *
     * @return 任务状态
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 设置任务状态。
     *
     * @param status 任务状态
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * 获取进度百分比。
     *
     * @return 百分比
     */
    public int getPercentage() {
        return percentage;
    }

    /**
     * 设置进度百分比。
     *
     * @param percentage 百分比
     */
    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    /**
     * 获取当前步骤标识。
     *
     * @return 步骤标识
     */
    public String getCurrentKey() {
        return currentKey;
    }

    /**
     * 设置当前步骤标识。
     *
     * @param currentKey 步骤标识
     */
    public void setCurrentKey(String currentKey) {
        this.currentKey = currentKey;
    }

    /**
     * 获取当前步骤标题。
     *
     * @return 步骤标题
     */
    public String getCurrentTitle() {
        return currentTitle;
    }

    /**
     * 设置当前步骤标题。
     *
     * @param currentTitle 步骤标题
     */
    public void setCurrentTitle(String currentTitle) {
        this.currentTitle = currentTitle;
    }

    /**
     * 获取当前步骤说明。
     *
     * @return 步骤说明
     */
    public String getCurrentDescription() {
        return currentDescription;
    }

    /**
     * 设置当前步骤说明。
     *
     * @param currentDescription 步骤说明
     */
    public void setCurrentDescription(String currentDescription) {
        this.currentDescription = currentDescription;
    }

    /**
     * 获取步骤列表。
     *
     * @return 步骤列表
     */
    public List<Step> getSteps() {
        return steps;
    }

    /**
     * 获取明细列表。
     *
     * @return 明细列表
     */
    public List<String> getDetails() {
        return details;
    }

    /**
     * 获取汇总信息。
     *
     * @return 汇总信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置汇总信息。
     *
     * @param message 汇总信息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间戳
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * 获取结束时间。
     *
     * @return 结束时间戳；未结束为 null
     */
    public Long getFinishTime() {
        return finishTime;
    }

    /**
     * 设置结束时间。
     *
     * @param finishTime 结束时间戳
     */
    public void setFinishTime(Long finishTime) {
        this.finishTime = finishTime;
    }

    /**
     * 获取取消标记。
     *
     * @return 取消标记
     */
    public AtomicBoolean getCancelRequested() {
        return cancelRequested;
    }

    /**
     * 任务状态。
     */
    public enum Status {
        /**
         * 进行中
         */
        RUNNING,
        /**
         * 成功
         */
        SUCCESS,
        /**
         * 失败
         */
        FAILED,
        /**
         * 已取消
         */
        CANCELLED
    }

    /**
     * 单个步骤。
     */
    public static class Step {

        /**
         * 步骤标识
         */
        private final String key;

        /**
         * 步骤标题
         */
        private final String title;

        /**
         * 步骤说明
         */
        private final String description;

        /**
         * 步骤状态
         */
        private StepStatus status = StepStatus.WAIT;

        /**
         * 结果明细
         */
        private String detail;

        /**
         * 开始时间
         */
        private Long startTime;

        /**
         * 结束时间
         */
        private Long endTime;

        /**
         * 创建步骤。
         *
         * @param key         步骤标识
         * @param title       步骤标题
         * @param description 步骤说明
         */
        public Step(String key, String title, String description) {
            this.key = key;
            this.title = title;
            this.description = description;
        }

        /**
         * 生成快照。
         *
         * @return 步骤快照
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>(8);
            map.put("key", key);
            map.put("title", title);
            map.put("description", description);
            map.put("status", status == null ? StepStatus.WAIT.name() : status.name());
            map.put("statusLabel", label(status));
            map.put("detail", detail);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            return map;
        }

        /**
         * 状态中文标签。
         *
         * @param status 状态，允许为空
         * @return 中文标签
         */
        private static String label(StepStatus status) {
            if (status == null) {
                return "等待";
            }
            return switch (status) {
                case RUNNING -> "进行中";
                case SUCCESS -> "已完成";
                case SKIPPED -> "已跳过";
                case ERROR -> "失败";
                case CANCELLED -> "已取消";
                default -> "等待";
            };
        }

        /**
         * 获取步骤标识。
         *
         * @return 步骤标识
         */
        public String getKey() {
            return key;
        }

        /**
         * 获取步骤标题。
         *
         * @return 步骤标题
         */
        public String getTitle() {
            return title;
        }

        /**
         * 获取步骤说明。
         *
         * @return 步骤说明
         */
        public String getDescription() {
            return description;
        }

        /**
         * 获取步骤状态。
         *
         * @return 步骤状态
         */
        public StepStatus getStatus() {
            return status;
        }

        /**
         * 设置步骤状态。
         *
         * @param status 步骤状态
         */
        public void setStatus(StepStatus status) {
            this.status = status;
        }

        /**
         * 获取结果明细。
         *
         * @return 结果明细
         */
        public String getDetail() {
            return detail;
        }

        /**
         * 设置结果明细。
         *
         * @param detail 结果明细
         */
        public void setDetail(String detail) {
            this.detail = detail;
        }

        /**
         * 获取开始时间。
         *
         * @return 开始时间戳
         */
        public Long getStartTime() {
            return startTime;
        }

        /**
         * 设置开始时间。
         *
         * @param startTime 开始时间戳
         */
        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        /**
         * 获取结束时间。
         *
         * @return 结束时间戳
         */
        public Long getEndTime() {
            return endTime;
        }

        /**
         * 设置结束时间。
         *
         * @param endTime 结束时间戳
         */
        public void setEndTime(Long endTime) {
            this.endTime = endTime;
        }
    }

    /**
     * 步骤状态。
     */
    public enum StepStatus {
        /**
         * 未开始
         */
        WAIT,
        /**
         * 进行中
         */
        RUNNING,
        /**
         * 已完成
         */
        SUCCESS,
        /**
         * 已跳过
         */
        SKIPPED,
        /**
         * 失败
         */
        ERROR,
        /**
         * 已取消
         */
        CANCELLED
    }
}
