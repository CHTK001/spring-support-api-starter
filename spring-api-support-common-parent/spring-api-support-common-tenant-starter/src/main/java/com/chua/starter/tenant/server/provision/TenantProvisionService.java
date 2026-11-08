package com.chua.starter.tenant.server.provision;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.utils.IdUtils;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.service.SysTenantService;
import com.chua.starter.tenant.server.support.TenantDataSourceResolver;
import com.chua.starter.tenant.server.support.TenantDatasourceInfo;
import com.chua.starter.tenant.server.support.TenantDatabaseAdminSynchronizer;
import com.chua.starter.tenant.server.support.TenantDatabaseInitializer;
import com.chua.starter.tenant.server.support.TenantDatabaseProvisioner;
import com.chua.starter.tenant.server.support.TenantPasswordCipher;
import com.chua.starter.tenant.server.sync.TenantServerSyncPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 租户创建进度服务。
 *
 * <p>创建一个独立库租户要做七件事：校验参数、落平台记录、建库、建数据库账号、初始化业务表、
 * 同步租户库管理员、计算菜单。任何一步都可能耗时数十秒，同步等一个 HTTP 响应会超时，
 * 所以这里把流程放到虚拟线程里跑，每次状态变化推一帧给前端，接口本身只返回任务号。</p>
 *
 * <h2>几个刻意的取舍</h2>
 *
 * <ul>
 *   <li><b>取消只在步骤边界生效</b>：建库、建账号这类 DDL 无法安全中断，硬杀线程会留下
 *       半成品库或半成品授权。请求取消后置一个标记，当前步骤跑完再停，并在结果里说明
 *       「已写入的库/账号/记录需到数据库处理」。</li>
 *   <li><b>共享库租户不是故障</b>：没有登记库名/地址的租户本来就用平台主库，建库与建账号
 *       两步记为「已跳过」，任务照样成功。</li>
 *   <li><b>套接字不可用不影响创建</b>：推送失败只记 WARN，前端还能用
 *       {@code /v2/tenant/provision/status} 轮询同一份快照。</li>
 * </ul>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProvisionService {

    /**
     * 套接字事件名
     */
    public static final String SOCKET_EVENT = "message";

    /**
     * 进度百分比：100 表示全部完成
     */
    private static final int PERCENTAGE_COMPLETE = 100;

    /**
     * 套接字所属模块；前端按它过滤消息
     */
    public static final String SOCKET_MODULE = "system";

    /**
     * 进度主题
     */
    public static final String SOCKET_TOPIC = "tenant:provision:progress";

    /**
     * 已结束任务的保留时长（毫秒）：30 分钟。前端刷新页面后还能看到刚才的结果。
     */
    private static final long TASK_TTL_MILLIS = 30L * 60 * 1000;

    /**
     * 内存中最多保留的任务数；超出时先淘汰最早结束的
     */
    private static final int MAX_TASKS = 200;

    /**
     * 租户服务
     */
    private final SysTenantService sysTenantService;

    /**
     * 数据库 DDL 执行器
     */
    private final TenantDatabaseProvisioner tenantDatabaseProvisioner;

    /**
     * 租户库业务表初始化器
     */
    private final TenantDatabaseInitializer tenantDatabaseInitializer;

    /**
     * 租户库管理员同步器
     */
    private final TenantDatabaseAdminSynchronizer tenantDatabaseAdminSynchronizer;

    /**
     * 租户口令加解密
     */
    private final TenantPasswordCipher tenantPasswordCipher;

    /**
     * 租户配置
     */
    private final TenantProperties tenantProperties;

    /**
     * 租户库连接信息解析器（自填字段 / 数据源台账 / 平台自身实例）
     */
    private final TenantDataSourceResolver tenantDataSourceResolver;

    /**
     * 租户端同步发布器；未接入同步服务时为空
     */
    @Autowired(required = false)
    private TenantServerSyncPublisher syncPublisher;

    /**
     * 套接字会话模板；未接入套接字时为空，此时只在状态接口里看进度
     */
    @Autowired(required = false)
    private SocketSessionTemplate socketSessionTemplate;

    /**
     * 任务表
     */
    private final Map<String, TenantProvisionTask> tasks = new ConcurrentHashMap<>();

    /**
     * 创建进度能力说明。
     *
     * <p>前端据此决定是否订阅套接字、是否渲染「取消创建」。</p>
     *
     * @return 能力说明
     */
    public Map<String, Object> capabilities() {
        Map<String, Object> map = new LinkedHashMap<>(8);
        map.put("socket", socketSessionTemplate != null);
        map.put("socketProtocol", socketSessionTemplate == null ? null : "socketio");
        map.put("socketModule", SOCKET_MODULE);
        map.put("socketTopic", SOCKET_TOPIC);
        map.put("cancellable", true);
        map.put("statusApi", "/v2/tenant/provision/status");
        map.put("cancelApi", "/v2/tenant/provision/cancel");
        map.put("steps", stepDefinitions());
        return map;
    }

    /**
     * 启动创建任务。
     *
     * @param sysTenant 租户信息
     * @return 任务快照，含 {@code taskId}
     */
    public Map<String, Object> start(SysTenant sysTenant) {
        cleanExpired();
        TenantProvisionTask task = new TenantProvisionTask(IdUtils.createSimpleUuid());
        task.setTenantName(sysTenant.getSysTenantName());
        task.setTenantUsername(sysTenant.getSysTenantUsername());
        for (Map<String, Object> definition : stepDefinitions()) {
            task.getSteps().add(new TenantProvisionTask.Step(
                    String.valueOf(definition.get("key")),
                    String.valueOf(definition.get("title")),
                    String.valueOf(definition.get("description"))));
        }
        task.setCurrentTitle("准备开始");
        task.setCurrentDescription("即将为租户「" + safe(sysTenant.getSysTenantName()) + "」创建记录与独立数据库");
        task.appendDetail("开始创建租户「" + safe(sysTenant.getSysTenantName()) + "」，登录账号 "
                + safe(sysTenant.getSysTenantUsername()));
        tasks.put(task.getTaskId(), task);
        publish(task);
        // 虚拟线程：建库、建表、建账号都是阻塞 IO，不能占用 Tomcat 请求线程
        Thread.startVirtualThread(() -> execute(task, sysTenant));
        Map<String, Object> result = new LinkedHashMap<>(4);
        result.put("taskId", task.getTaskId());
        result.put("status", task.getStatus().name());
        result.put("steps", task.toMap().get("steps"));
        return result;
    }

    /**
     * 请求取消任务。
     *
     * @param taskId 任务标识
     * @return 是否受理；任务不存在或已结束返回 false
     */
    public boolean cancel(String taskId) {
        TenantProvisionTask task = tasks.get(taskId);
        if (task == null || task.isFinished()) {
            return false;
        }
        boolean accepted = task.requestCancel();
        if (accepted) {
            task.appendDetail("已收到取消请求，将在当前步骤结束后停止");
            publish(task);
        }
        log.info("[租户创建进度] 收到取消请求: taskId={}", taskId);
        return accepted;
    }

    /**
     * 查询任务快照。
     *
     * @param taskId 任务标识
     * @return 快照；任务不存在或已过期返回 null
     */
    public Map<String, Object> statusOf(String taskId) {
        TenantProvisionTask task = tasks.get(taskId);
        return task == null ? null : task.toMap();
    }

    /**
     * 依次执行创建步骤。
     *
     * <p>步骤失败即终止：后面的步骤依赖前一步的结果（没建库谈不上建账号），继续跑只会
     * 产生一串误导性的错误。</p>
     *
     * @param task      任务
     * @param sysTenant 租户信息
     */
    private void execute(TenantProvisionTask task, SysTenant sysTenant) {
        List<TenantProvisionTask.Step> steps = task.getSteps();
        int total = steps.size();
        int index = 0;
        SysTenant saved;
        try {
            if (!begin(task, index, total)) {
                cancelled(task, index);
                return;
            }
            applyDefaultDbHost(sysTenant);
            String error = validateRequest(sysTenant);
            if (error != null) {
                fail(task, index, error);
                return;
            }
            done(task, index, "租户名称、账号、密码等必填信息校验通过");

            index = 1;
            if (!begin(task, index, total)) {
                cancelled(task, index);
                return;
            }
            ReturnResult<SysTenant> savedResult = sysTenantService.saveRecordForSysTenant(sysTenant);
            if (!savedResult.isOk()) {
                fail(task, index, "保存租户记录失败：" + savedResult.getMsg());
                return;
            }
            saved = savedResult.getData();
            task.setTenantId(saved.getSysTenantId());
            done(task, index, "租户记录已写入，标识 " + saved.getSysTenantId());

            String sqlPassword = tenantPasswordCipher.decrypt(saved.getSysTenantSqlPassword());
            String adminPassword = tenantPasswordCipher.decrypt(saved.getSysTenantPassword());
            // 引用型租户（台账 / 平台自身实例）的实例信息由解析器给出；自填型沿用旧字段口径
            TenantDatasourceInfo dbInfo = tenantDataSourceResolver.resolve(saved);
            boolean separateDb = dbInfo != null;

            index = 2;
            if (!begin(task, index, total)) {
                cancelled(task, index);
                return;
            }
            if (!separateDb) {
                skip(task, index, "未登记独立库（与平台共用主库），无需建库");
            } else if (!tenantDatabaseProvisioner.isAvailable()) {
                skip(task, index, "自动建库未启用（datasource-routing-auto-provision=false），跳过建库；"
                        + "请先自动创建，或通过平台库配置管理。");
            } else {
                if (!tenantDatabaseProvisioner.isSafeIdentifier(saved.getSysTenantDbname())
                        || !tenantDatabaseProvisioner.isSafeIdentifier(saved.getSysTenantSqlUsername())) {
                    fail(task, index, "库名或数据库账号含非法字符（只允许字母、数字、下划线）");
                    return;
                }
                tenantDatabaseProvisioner.createDatabase(saved.getSysTenantDbname());
                done(task, index, "已创建数据库 " + saved.getSysTenantDbname());
            }

            index = 3;
            if (!begin(task, index, total)) {
                cancelled(task, index);
                return;
            }
            if (!separateDb || !tenantDatabaseProvisioner.isAvailable()) {
                skip(task, index, separateDb
                        ? "自动建库未启用（数据库账号权限不足或关闭为租户建账号）"
                        : "沿用租户自带数据库账号");
            } else {
                tenantDatabaseProvisioner.createAccount(saved, saved.getSysTenantDbname(), sqlPassword);
                tenantDatabaseProvisioner.verify(saved, sqlPassword);
                done(task, index, "已为 " + saved.getSysTenantSqlUsername() + " 授权 " + saved.getSysTenantDbname());
            }

            index = 4;
            if (!begin(task, index, total)) {
                cancelled(task, index);
                return;
            }
            TenantDatabaseInitializer.InitResult initResult = tenantDatabaseInitializer.initialize(saved, sqlPassword);
            if (initResult.isSuccess()) {
                done(task, index, "业务表初始化完成：" + initResult.message());
            } else if (initResult.status() == TenantDatabaseInitializer.InitResult.Status.SKIPPED) {
                skip(task, index, "已跳过：" + initResult.message());
            } else {
                fail(task, index, "业务表初始化失败：" + initResult.message());
                return;
            }

            index = 5;
            if (!begin(task, index, total)) {
                cancelled(task, index);
                return;
            }
            TenantDatabaseAdminSynchronizer.Result adminResult = tenantDatabaseAdminSynchronizer.sync(saved, adminPassword);
            if (adminResult.isSynced()) {
                done(task, index, "管理员账号已同步：" + adminResult.message());
            } else if (adminResult.status() == TenantDatabaseAdminSynchronizer.Result.Status.SKIPPED) {
                skip(task, index, adminResult.message());
            } else {
                fail(task, index, "管理员账号同步失败：" + adminResult.message());
                return;
            }

            index = 6;
            if (!begin(task, index, total)) {
                cancelled(task, index);
                return;
            }
            if (syncPublisher == null) {
                skip(task, index, "未接入租户端同步（未启用同步服务），租户端上线后可调用「重置菜单」补偿");
            } else {
                syncPublisher.publishTenantUpdate(saved);
                sysTenantService.resetMenuForSysTenant(saved.getSysTenantId());
                done(task, index, "已推送租户信息与按订阅计算的菜单");
            }

            index = 7;
            begin(task, index, total);
            done(task, index, "租户创建完成");
            synchronized (task) {
                task.setStatus(TenantProvisionTask.Status.SUCCESS);
                task.setPercentage(PERCENTAGE_COMPLETE);
                task.setCurrentKey("done");
                task.setCurrentTitle("创建完成");
                task.setCurrentDescription("租户「" + safe(saved.getSysTenantName()) + "」已就绪");
                task.setMessage("租户「" + safe(saved.getSysTenantName()) + "」（" + safe(saved.getSysTenantUsername())
                        + "）创建成功，数据范围：" + (separateDb
                        ? "独立库 " + safe(saved.getSysTenantDbname())
                        : "与平台共享主库"));
                task.setFinishTime(System.currentTimeMillis());
                task.appendDetail(task.getMessage());
            }
            publish(task);
            log.info("[租户创建进度] 创建完成: taskId={}, tenantId={}", task.getTaskId(), saved.getSysTenantId());
        } catch (Exception e) {
            log.error("[租户创建进度] 创建异常: taskId={}", task.getTaskId(), e);
            fail(task, Math.min(index, total - 1), "创建异常：" + e.getMessage());
        }
    }

    /**
     * 进入某一步骤。
     *
     * @param task  任务
     * @param index 步骤序号
     * @param total 步骤总数
     * @return 继续执行返回 true；已请求取消返回 false
     */
    private boolean begin(TenantProvisionTask task, int index, int total) {
        if (task.isCancelRequested()) {
            return false;
        }
        TenantProvisionTask.Step step = task.getSteps().get(index);
        synchronized (task) {
            step.setStatus(TenantProvisionTask.StepStatus.RUNNING);
            step.setStartTime(System.currentTimeMillis());
            task.setStatus(TenantProvisionTask.Status.RUNNING);
            task.setCurrentKey(step.getKey());
            task.setCurrentTitle(step.getTitle());
            task.setCurrentDescription(step.getDescription());
            task.updatePercentage(index, total);
            task.appendDetail("开始：" + step.getTitle() + " —— " + step.getDescription());
        }
        publish(task);
        return true;
    }

    /**
     * 标记步骤完成。
     *
     * @param task   任务
     * @param index  步骤序号
     * @param detail 结果说明
     */
    private void done(TenantProvisionTask task, int index, String detail) {
        TenantProvisionTask.Step step = task.getSteps().get(index);
        synchronized (task) {
            step.setStatus(TenantProvisionTask.StepStatus.SUCCESS);
            step.setDetail(detail);
            step.setEndTime(System.currentTimeMillis());
            task.appendDetail(step.getTitle() + "：" + detail);
        }
        publish(task);
    }

    /**
     * 标记步骤跳过。
     *
     * @param task   任务
     * @param index  步骤序号
     * @param reason 跳过原因
     */
    private void skip(TenantProvisionTask task, int index, String reason) {
        TenantProvisionTask.Step step = task.getSteps().get(index);
        synchronized (task) {
            step.setStatus(TenantProvisionTask.StepStatus.SKIPPED);
            step.setDetail(reason);
            step.setEndTime(System.currentTimeMillis());
            task.appendDetail(step.getTitle() + "：已跳过 —— " + reason);
        }
        publish(task);
    }

    /**
     * 标记步骤失败并结束任务。
     *
     * @param task   任务
     * @param index  步骤序号
     * @param reason 失败原因
     */
    private void fail(TenantProvisionTask task, int index, String reason) {
        TenantProvisionTask.Step step = task.getSteps().get(index);
        synchronized (task) {
            step.setStatus(TenantProvisionTask.StepStatus.ERROR);
            step.setDetail(reason);
            step.setEndTime(System.currentTimeMillis());
            task.setStatus(TenantProvisionTask.Status.FAILED);
            task.setCurrentKey(step.getKey());
            task.setCurrentTitle("创建失败");
            task.setCurrentDescription(reason);
            task.setMessage(reason);
            task.setFinishTime(System.currentTimeMillis());
            task.appendDetail(step.getTitle() + "：失败 —— " + reason);
        }
        publish(task);
        log.warn("[租户创建进度] 创建失败: taskId={}, reason={}", task.getTaskId(), reason);
    }

    /**
     * 标记任务已取消。
     *
     * @param task  任务
     * @param index 取消时所在的步骤序号
     */
    private void cancelled(TenantProvisionTask task, int index) {
        synchronized (task) {
            for (int i = index; i < task.getSteps().size(); i++) {
                task.getSteps().get(i).setStatus(TenantProvisionTask.StepStatus.CANCELLED);
            }
            task.setStatus(TenantProvisionTask.Status.CANCELLED);
            task.setCurrentTitle("已取消");
            task.setCurrentDescription("用户取消，未执行的步骤已停止");
            task.setMessage("租户创建已取消。已完成的步骤保留（已写入的库/账号/记录需到数据库处理）。");
            task.setFinishTime(System.currentTimeMillis());
            task.appendDetail(task.getMessage());
        }
        publish(task);
        log.info("[租户创建进度] 任务已取消: taskId={}", task.getTaskId());
    }

    /**
     * 未填库地址时补成平台管理连接的地址。
     *
     * <p>前端只填库名、账号、口令，三者齐全即视为「要独立库」；地址由后端按
     * {@code datasource-routing-admin-url} 推导，避免让用户填一个平台侧的内部地址。</p>
     *
     * @param sysTenant 租户信息
     */
    private void applyDefaultDbHost(SysTenant sysTenant) {
        if (sysTenant == null || !hasText(sysTenant.getSysTenantDbname())) {
            return;
        }
        Long datasourceId = sysTenant.getSysTenantDatasourceId();
        if (datasourceId != null && datasourceId != 0L) {
            // 引用型：实例地址与账号口令由解析器按台账 / 平台自身实例给出，
            // 不回填自填字段，避免在租户记录里留一份与实例现状脱节的旧地址
            return;
        }
        if (!hasText(sysTenant.getSysTenantSqlUsername())
                || !hasText(sysTenant.getSysTenantSqlPassword())) {
            return;
        }
        if (hasText(sysTenant.getSysTenantHost()) || hasText(sysTenant.getSysTenantIp())) {
            return;
        }
        sysTenant.setSysTenantHost(resolveAdminHost());
    }

    /**
     * 从管理连接地址里取出主机名。
     *
     * @return 主机名；配置缺失时返回 {@code 127.0.0.1}
     */
    private String resolveAdminHost() {
        String url = tenantProperties == null || tenantProperties.getServer() == null
                ? null
                : tenantProperties.getServer().getDatasourceRoutingAdminUrl();
        if (hasText(url)) {
            int start = url.indexOf("//");
            String rest = start < 0 ? url : url.substring(start + 2);
            int slash = rest.indexOf('/');
            String hostPort = slash < 0 ? rest : rest.substring(0, slash);
            int colon = hostPort.indexOf(':');
            String host = colon < 0 ? hostPort : hostPort.substring(0, colon);
            if (hasText(host)) {
                return host.trim();
            }
        }
        return "127.0.0.1";
    }

    /**
     * 校验创建参数。
     *
     * @param sysTenant 租户信息
     * @return 校验错误；通过返回 null
     */
    private String validateRequest(SysTenant sysTenant) {
        if (sysTenant == null) {
            return "租户参数为空";
        }
        if (!hasText(sysTenant.getSysTenantName())) {
            return "租户名称不能为空";
        }
        if (!hasText(sysTenant.getSysTenantUsername())) {
            return "租户账号不能为空";
        }
        if (!hasText(sysTenant.getSysTenantPassword())) {
            return "租户密码不能为空";
        }
        boolean anySql = hasText(sysTenant.getSysTenantDbname())
                || hasText(sysTenant.getSysTenantSqlUsername())
                || hasText(sysTenant.getSysTenantSqlPassword());
        boolean allSql = hasText(sysTenant.getSysTenantDbname())
                && hasText(sysTenant.getSysTenantSqlUsername())
                && hasText(sysTenant.getSysTenantSqlPassword());
        // 引用型（台账 / 平台自身实例）：实例地址与账号口令都由实例提供，
        // 租户只需给库名，不该再要求填账号口令（否则等于让用户重复录入一遍实例凭据）
        Long datasourceId = sysTenant.getSysTenantDatasourceId();
        boolean sharedInstance = datasourceId != null && datasourceId != 0L;
        if (sharedInstance && !hasText(sysTenant.getSysTenantDbname()) && anySql) {
            return "选择了共享数据库实例：只需填写该租户要用的库名";
        }
        if (anySql && !allSql && !sharedInstance) {
            return "独立库信息不完整：库名、数据库账号、数据库密码必须同时填写（留空表示与平台共用主库）";
        }
        return null;
    }

    /**
     * 推送一帧进度。
     *
     * @param task 任务
     */
    private void publish(TenantProvisionTask task) {
        if (socketSessionTemplate == null) {
            log.debug("[租户创建进度] 未接入套接字，跳过推送: taskId={}", task.getTaskId());
            return;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>(task.toMap());
            // module / event 放到顶层：前端按模块过滤消息，放在 data 里会收不到
            payload.put("module", SOCKET_MODULE);
            payload.put("event", SOCKET_TOPIC);
            payload.put("timestamp", System.currentTimeMillis());
            socketSessionTemplate.broadcast(SOCKET_EVENT, Json.toJSONString(payload));
        } catch (Exception e) {
            log.warn("[租户创建进度] 套接字推送失败: taskId={}, {}", task.getTaskId(), e.getMessage());
        }
    }

    /**
     * 步骤定义。
     *
     * @return 步骤定义列表
     */
    private List<Map<String, Object>> stepDefinitions() {
        List<Map<String, Object>> steps = new ArrayList<>(8);
        steps.add(step("validate", "校验租户参数",
                "检查租户名称、账号、密码；独立库字段要么全填、要么全空（全空=与平台共用主库）"));
        steps.add(step("save", "保存租户记录",
                "写入 sys_tenant，密码列加密保存（同步下发时再还原）"));
        steps.add(step("database", "创建独立数据库",
                "对登记的空库地址执行 CREATE DATABASE IF NOT EXISTS（utf8mb4），已存在时复用"));
        steps.add(step("account", "创建数据库账号并授权",
                "CREATE USER / ALTER USER / GRANT ALL ON 该库，并用一次真实连接校验"));
        steps.add(step("schema", "初始化租户库业务表",
                "执行与平台同一批 db/init 脚本，版本记录在租户库 sys_database_version，可重复执行"));
        steps.add(step("admin", "同步租户库管理员",
                "把平台租户账号写进租户库 sys_user 并绑定超级管理员角色，租户用同一账号登录自己的库"));
        steps.add(step("menu", "计算租户信息与菜单",
                "按租户订阅服务计算菜单，并推送给已上线的租户端"));
        steps.add(step("done", "完成", "汇总本次创建结果"));
        return steps;
    }

    /**
     * 构造一个步骤定义。
     *
     * @param key         步骤标识
     * @param title       步骤标题
     * @param description 步骤说明
     * @return 步骤定义
     */
    private Map<String, Object> step(String key, String title, String description) {
        Map<String, Object> map = new LinkedHashMap<>(4);
        map.put("key", key);
        map.put("title", title);
        map.put("description", description);
        return map;
    }

    /**
     * 清理过期与超量的任务。
     */
    private void cleanExpired() {
        long deadline = System.currentTimeMillis() - TASK_TTL_MILLIS;
        tasks.entrySet().removeIf(entry -> entry.getValue().getFinishTime() != null
                && entry.getValue().getFinishTime() < deadline);
        if (tasks.size() <= MAX_TASKS) {
            return;
        }
        List<String> finished = new ArrayList<>();
        for (Map.Entry<String, TenantProvisionTask> entry : tasks.entrySet()) {
            if (entry.getValue().getFinishTime() == null) {
                continue;
            }
            finished.add(entry.getKey());
        }
        finished.sort(Comparator.comparing(key -> tasks.get(key).getFinishTime()));
        Iterator<String> iterator = finished.iterator();
        while (tasks.size() > MAX_TASKS && iterator.hasNext()) {
            tasks.remove(iterator.next());
        }
    }

    /**
     * 是否包含文本。
     *
     * @param value 待判断值
     * @return 非空白返回 true
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 空值转空串，避免前端显示 {@code null}。
     *
     * @param value 原始值
     * @return 非空字符串
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }
}
