// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.service.SysSettingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态调度注册中心。
 *
 * <p>Spring 原生 {@code @Scheduled} 的 cron 在启动后不可更改，Actuator 的
 * {@code scheduledtasks} 端点也只读。本注册中心为系统内的可调度任务提供运行期
 * 修改 cron、启停与查看下一次执行时间的能力，配置持久化到 {@code sys_setting}：
 * <ul>
 *     <li>{@code schedule.task.<key>.cron}    任务 cron 表达式</li>
 *     <li>{@code schedule.task.<key>.enabled} 任务是否启用（1/0）</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0.51
 */
@Slf4j
@Component
public class DynamicScheduleRegistry {

    /**
     * 设置分组：定时任务
     */
    private static final String SETTING_GROUP = "schedule";

    /**
     * cron 配置 code 前缀
     */
    private static final String CODE_CRON_PREFIX = "schedule.task.";

    /**
     * 启用配置 code 前缀
     */
    private static final String CODE_ENABLED_PREFIX = "schedule.task.";

    /**
     * cron 配置 code 后缀
     */
    private static final String CODE_CRON_SUFFIX = ".cron";

    /**
     * 启用配置 code 后缀
     */
    private static final String CODE_ENABLED_SUFFIX = ".enabled";

    /**
     * 时间格式
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 已注册任务：key -> 任务
     */
    private final Map<String, ManagedTask> tasks = new ConcurrentHashMap<>();

    /**
     * 调度线程池
     */
    private ThreadPoolTaskScheduler scheduler;

    /**
     * 系统设置服务（可能不存在，降级为纯内存）
     */
    @Autowired(required = false)
    private SysSettingService sysSettingService;

    /**
     * 初始化调度线程池
     */
    @PostConstruct
    public void init() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("dynamic-schedule-");
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        scheduler.initialize();
    }

    /**
     * 销毁调度线程池
     */
    @PreDestroy
    public void destroy() {
        if (null != scheduler) {
            scheduler.shutdown();
        }
    }

    /**
     * 注册一个可动态调度的任务（cron / 启停取持久化配置，缺省用默认值）。
     *
     * @param key            任务唯一标识
     * @param label          任务名称（用于展示）
     * @param defaultCron    默认 cron 表达式
     * @param defaultEnabled 默认是否启用
     * @param runnable       任务体
     */
    public void register(String key, String label, String defaultCron, boolean defaultEnabled, Runnable runnable) {
        ManagedTask task = new ManagedTask(key, label, runnable);
        task.cron = readCron(key, defaultCron);
        task.enabled = readEnabled(key, defaultEnabled);
        tasks.put(key, task);
        apply(task);
        log.info("[动态调度] 注册任务 {} ({}) cron={} enabled={}", label, key, task.cron, task.enabled);
    }

    /**
     * 修改任务 cron 并立即生效。
     *
     * @param key  任务标识
     * @param cron 新的 cron 表达式
     * @return 是否成功
     */
    public boolean updateCron(String key, String cron) {
        ManagedTask task = tasks.get(key);
        if (null == task) {
            return false;
        }
        if (null == cron || cron.isBlank() || !CronExpression.isValidExpression(cron)) {
            return false;
        }
        task.cron = cron.trim();
        writeSetting(CODE_CRON_PREFIX + key + CODE_CRON_SUFFIX, task.cron);
        apply(task);
        log.info("[动态调度] 更新任务 {} cron={}", key, task.cron);
        return true;
    }

    /**
     * 启用 / 停用任务。
     *
     * @param key     任务标识
     * @param enabled 是否启用
     * @return 是否成功
     */
    public boolean setEnabled(String key, boolean enabled) {
        ManagedTask task = tasks.get(key);
        if (null == task) {
            return false;
        }
        task.enabled = enabled;
        writeSetting(CODE_ENABLED_PREFIX + key + CODE_ENABLED_SUFFIX, enabled ? "1" : "0");
        apply(task);
        log.info("[动态调度] {} 任务 {}", enabled ? "启用" : "停用", key);
        return true;
    }

    /**
     * 立即执行一次任务（不影响既有调度）。
     *
     * @param key 任务标识
     * @return 是否成功
     */
    public boolean runOnce(String key) {
        ManagedTask task = tasks.get(key);
        if (null == task) {
            return false;
        }
        try {
            task.runnable.run();
            task.lastStatus = "SUCCESS";
        } catch (Exception e) {
            task.lastStatus = "FAILED: " + e.getMessage();
            log.warn("[动态调度] 手动执行任务 {} 失败: {}", key, e.getMessage(), e);
        }
        task.lastTime = LocalDateTime.now().format(TIME_FORMATTER);
        return true;
    }

    /**
     * 任务列表快照。
     *
     * @return 任务元数据列表
     */
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> rows = new ArrayList<>(tasks.size());
        for (ManagedTask task : tasks.values()) {
            Map<String, Object> row = new LinkedHashMap<>(10);
            row.put("key", task.key);
            row.put("label", task.label);
            row.put("cron", task.cron);
            row.put("enabled", task.enabled);
            row.put("nextTime", task.nextTime);
            row.put("lastTime", task.lastTime);
            row.put("lastStatus", task.lastStatus);
            rows.add(row);
        }
        rows.sort((a, b) -> String.valueOf(a.get("key")).compareTo(String.valueOf(b.get("key"))));
        return rows;
    }

    /**
     * 任务是否存在。
     *
     * @param key 任务标识
     * @return 是否存在
     */
    public boolean contains(String key) {
        return tasks.containsKey(key);
    }

    /**
     * 应用任务的启用状态与 cron
     *
     * @param task 任务
     */
    private void apply(ManagedTask task) {
        task.cancel();
        task.nextTime = "";
        if (!task.enabled) {
            return;
        }
        CronExpression expression = CronExpression.parse(task.cron);
        task.future = scheduler.schedule(new DelegatingRunnable(task), new CronTrigger(task.cron));
        LocalDateTime next = expression.next(LocalDateTime.now());
        task.nextTime = null == next ? "" : next.format(TIME_FORMATTER);
    }

    /**
     * 读取持久化 cron
     *
     * @param key           任务标识
     * @param defaultCron   默认值
     * @return cron 表达式
     */
    private String readCron(String key, String defaultCron) {
        String value = readSetting(CODE_CRON_PREFIX + key + CODE_CRON_SUFFIX);
        if (null == value || value.isBlank() || !CronExpression.isValidExpression(value)) {
            return defaultCron;
        }
        return value.trim();
    }

    /**
     * 读取持久化启用状态
     *
     * @param key            任务标识
     * @param defaultEnabled 默认值
     * @return 是否启用
     */
    private boolean readEnabled(String key, boolean defaultEnabled) {
        String value = readSetting(CODE_ENABLED_PREFIX + key + CODE_ENABLED_SUFFIX);
        if (null == value || value.isBlank()) {
            return defaultEnabled;
        }
        return "1".equals(value.trim()) || Boolean.parseBoolean(value.trim());
    }

    /**
     * 读取设置项
     *
     * @param code 设置 code
     * @return 设置值
     */
    private String readSetting(String code) {
        if (null == sysSettingService) {
            return null;
        }
        try {
            SysSetting setting = sysSettingService.getOne(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingCode, code).last("limit 1"), false);
            return null == setting ? null : setting.getSysSettingValue();
        } catch (Exception e) {
            log.debug("[动态调度] 读取设置 {} 失败: {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * 写入设置项（不存在则新增）
     *
     * @param code 设置 code
     * @param value 设置值
     */
    private void writeSetting(String code, String value) {
        if (null == sysSettingService) {
            return;
        }
        try {
            boolean updated = sysSettingService.update(Wrappers.<SysSetting>lambdaUpdate()
                    .set(SysSetting::getSysSettingValue, value)
                    .eq(SysSetting::getSysSettingCode, code));
            if (updated) {
                return;
            }
            SysSetting setting = new SysSetting();
            setting.setSysSettingCode(code);
            setting.setSysSettingName("定时任务:" + code);
            setting.setSysSettingValue(value);
            setting.setSysSettingDefaultValue(value);
            setting.setSysSettingGroup(SETTING_GROUP);
            setting.setSysSettingValueType("TEXT");
            setting.setSysSettingStatus(1);
            setting.setSysSettingInSystem(1);
            sysSettingService.save(setting);
        } catch (Exception e) {
            log.debug("[动态调度] 写入设置 {} 失败: {}", code, e.getMessage());
        }
    }

    /**
     * 可动态调度的任务
     */
    private final class ManagedTask {
        /**
         * 任务标识
         */
        final String key;
        /**
         * 任务名称
         */
        final String label;
        /**
         * 任务体
         */
        final Runnable runnable;
        /**
         * cron 表达式
         */
        volatile String cron;
        /**
         * 是否启用
         */
        volatile boolean enabled;
        /**
         * 调度句柄
         */
        volatile ScheduledFuture<?> future;
        /**
         * 下一次执行时间
         */
        volatile String nextTime = "";
        /**
         * 上一次执行时间
         */
        volatile String lastTime = "";
        /**
         * 上一次执行结果
         */
        volatile String lastStatus = "";

        ManagedTask(String key, String label, Runnable runnable) {
            this.key = key;
            this.label = label;
            this.runnable = runnable;
        }

        /**
         * 取消当前调度
         */
        void cancel() {
            ScheduledFuture<?> f = future;
            if (null != f) {
                f.cancel(false);
                future = null;
            }
        }
    }

    /**
     * 包装任务体，记录执行时间与结果并刷新下一次执行时间
     */
    private final class DelegatingRunnable implements Runnable {
        /**
         * 目标任务
         */
        private final ManagedTask task;

        DelegatingRunnable(ManagedTask task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.runnable.run();
                task.lastStatus = "SUCCESS";
            } catch (Exception e) {
                task.lastStatus = "FAILED: " + e.getMessage();
                log.warn("[动态调度] 任务 {} 执行失败: {}", task.key, e.getMessage(), e);
            }
            task.lastTime = LocalDateTime.now().format(TIME_FORMATTER);
            if (task.enabled) {
                LocalDateTime next = CronExpression.parse(task.cron).next(LocalDateTime.now());
                task.nextTime = null == next ? "" : next.format(TIME_FORMATTER);
            }
        }
    }
}