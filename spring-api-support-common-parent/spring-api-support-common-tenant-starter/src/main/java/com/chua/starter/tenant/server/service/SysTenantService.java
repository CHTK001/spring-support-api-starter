package com.chua.starter.tenant.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.entity.SysTenantStats;
import com.chua.starter.tenant.server.pojo.SysTenantBindServiceV1Request;

import java.util.List;

/**
 * 租户服务接口
 *
 * @since 2024/12/06
 * @author CH
 */
public interface SysTenantService extends IService<SysTenant> {

    /**
     * 分页查询租户
     *
     * @param query     查询对象
     * @param sysTenant 租户条件
     * @return 分页结果
     */
    IPage<SysTenant> pageForSysTenant(Query<SysTenant> query, SysTenant sysTenant);

    /**
     * 查询租户列表
     *
     * @param sysTenant 租户条件
     * @return 租户列表
     */
    ReturnResult<List<SysTenant>> listForSysTenant(SysTenant sysTenant);

    /**
     * 保存租户
     *
     * @param sysTenant 租户对象
     * @return 保存结果
     */
    ReturnResult<SysTenant> saveForSysTenant(SysTenant sysTenant);

    /**
     * 只保存租户记录（校验 + 口令加密落库 + 编码兼容），不建库、不建表、不推菜单。
     *
     * <p>给「创建进度」分步编排用：进度弹窗要逐步显示保存记录 / 建库 / 建表 /
     * 同步管理员 / 推送菜单，合并成一次调用就说不清进行到哪一步。</p>
     *
     * @param sysTenant 租户对象
     * @return 保存结果（含生成的租户标识）
     */
    ReturnResult<SysTenant> saveRecordForSysTenant(SysTenant sysTenant);

    /**
     * 更新租户
     *
     * @param sysTenant 租户对象
     * @return 更新结果
     */
    ReturnResult<Boolean> updateForSysTenant(SysTenant sysTenant);

    /**
     * 删除租户（逻辑删除，保留独立数据库与库账号）
     *
     * @param sysTenantId 租户标识
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteForSysTenant(String sysTenantId);

    /**
     * 物理删除租户独立数据库（不可逆，需输入租户编码确认）
     *
     * <p>与逻辑删除分开：只有这个入口会执行 {@code DROP DATABASE}。
     * 成功后清空租户记录上的独立库登记，租户本身保留。</p>
     *
     * @param sysTenantId 租户标识
     * @param confirmCode 租户编码，必须与记录一致
     * @return 删除结果
     */
    ReturnResult<Boolean> dropTenantDatabase(String sysTenantId, String confirmCode);

    /**
     * 根据标识获取租户详情
     *
     * @param sysTenantId 租户标识
     * @return 租户详情
     */
    ReturnResult<SysTenant> getForSysTenant(String sysTenantId);

    /**
     * 绑定租户服务
     *
     * @param request 绑定请求
     * @return 绑定结果
     */
    ReturnResult<Boolean> bindServiceForSysTenant(SysTenantBindServiceV1Request request);

    /**
     * 更新租户状态
     *
     * @param sysTenantId 租户标识
     * @param status      状态值
     * @return 更新结果
     */
    ReturnResult<Boolean> updateStatusForSysTenant(String sysTenantId, Integer status);

    /**
     * 获取租户统计数据
     *
     * @return 统计数据（总数、启用数、禁用数、订阅服务数）
     */
    ReturnResult<SysTenantStats> statsForSysTenant();

    /**
     * 重置租户密码并推送租户端
     *
     * @param sysTenantId     租户标识
     * @param sysTenantPassword 新密码（明文，同步到租户端登录账号）
     * @return 重置结果
     */
    ReturnResult<Boolean> resetPasswordForSysTenant(String sysTenantId, String sysTenantPassword);

    /**
     * 重置租户菜单并推送到租户端
     * <p>
     * 按租户当前订阅的服务重新计算菜单并全量推送给租户端，
     * 保证租户端菜单始终与平台分配保持一致（租户端不可自行维护菜单数据，只能被分配）。
     * </p>
     *
     * @param sysTenantId 租户标识
     */
    void resetMenuForSysTenant(String sysTenantId);

    /**
     * 初始化（或补齐）租户独立库的业务表。
     *
     * <p>给两类场景用：建租户时脚本执行失败、以及存量租户库需要补跑新增的迁移脚本。
     * 走的是与平台主库同一批 {@code db/init} 脚本，租户库的 {@code sys_database_version}
     * 记录已执行版本，因此重复调用是安全的。</p>
     *
     * @param sysTenantId 租户标识
     * @return 初始化结果
     */
    ReturnResult<String> initTenantDatabase(String sysTenantId);

    /**
     * 把平台租户账号同步成租户库管理员（建租户时自动做，这里给存量租户补做）。
     *
     * <p>租户库里能登录的账号 {@code sys_user} 由本动作维护：没有就建、有就覆盖口令并重新启用，
     * 因此重复调用安全。</p>
     *
     * @param sysTenantId 租户标识
     * @return 同步结果说明
     */
    ReturnResult<String> syncTenantDatabaseAdmin(String sysTenantId);
}
