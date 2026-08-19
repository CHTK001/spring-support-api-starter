/**
 * 定时任务启动器。
 *
 * <p>平台基于 job-starter 新架构以 SCHEDULER 角色运行, 通过
 * {@link JobAutoConfiguration} 自动装配 Leader 扫描 + 长链 Sync,
 * 业务执行器侧配置 job-starter 的 EXECUTOR 角色接入。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties({SchedulerJobPlatformProperties.class, ApiProperties.class})
@MapperScan({"com.chua.starter.**.mapper", JobAutoConfiguration.MAPPER_PACKAGE})
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.system",
        "com.chua.starter.tenant",
        "com.chua.starter.scheduler",
        "com.chua.starter.service.impl",
        "com.chua.starter.service.task",
        "com.chua.starter.service.configuration"
})
/**
 * public class SchedulerApplication
 *
 * @author CH
 * @since 4.0.0.42
 */
public class SchedulerApplication