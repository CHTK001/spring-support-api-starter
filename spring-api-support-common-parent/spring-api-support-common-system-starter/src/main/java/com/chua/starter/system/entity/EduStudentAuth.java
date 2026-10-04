package com.chua.starter.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 学生档案（学生登录专用只读视图）。
 *
 * <p><b>为什么这个实体在 system 模块而不是 eduai 模块</b>：学生登录由 OAuth 服务端
 * 的 {@code UserInfoService} 实现完成，而 OAuth 服务端处于 api 层，
 * eduai 处于 spring 层；api 层引用 spring 层会造成反向依赖（本仓明令禁止）。
 * 因此这里只保留「登录校验所需的最小字段」，以<b>表结构</b>为契约而非以类为契约。</p>
 *
 * <p><b>不加 MyBatis-Plus 注解的原因</b>：{@code edu_student} 位于 eduai 库，
 * 与本服务主库不同。跨库查询用独立的 {@code JdbcTemplate}（见
 * {@code EduAuthDataSourceConfiguration}），而非把该表纳入主库的 MyBatis 映射——
 * 后者会让本服务的主库上下文里出现一张它并不拥有的表。
 * 字段名与列名按下划线转驼峰约定对应，由 {@code BeanPropertyRowMapper} 完成映射。</p>
 *
 * <p><b>只读</b>：登录流程只查不改。</p>
 *
 * <p><b>字段来源</b>：{@code eduai} 模块的 {@code V1.2__init_all_eduai.sql}，
 * 该校验脚本是全仓学生表的唯一事实来源。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Data
public class EduStudentAuth {

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号（学生端登录账号，令牌里的 userId 与它对齐）
     */
    private String studentNo;

    /**
     * 姓名
     */
    private String studentName;

    /**
     * 班级ID
     */
    private Long studentClassId;

    /**
     * 学段：PRIMARY / JUNIOR / SENIOR
     */
    private String studentGradeLevel;

    /**
     * 年级
     */
    private String studentGrade;

    /**
     * 家长手机号（与学号二选一的登录标识；同号可带多个孩子，故非唯一）
     */
    private String studentParentPhone;

    /**
     * 登录口令摘要 {@code hmacSha256(明文, 盐)}，非明文
     */
    @JsonIgnore
    private String studentPassword;

    /**
     * 口令盐（随机十六进制，与 {@link #studentPassword} 一一对应）
     */
    @JsonIgnore
    private String studentPwdSalt;

    /**
     * 是否仍在使用「初始口令」：1=是（教师建档/重置后学生尚未自行改密）。
     *
     * <p>初始口令阶段允许用「学号」作为默认口令登录（建档默认口令=学号）；
     * 学生自助改密后该标记置 0，默认口令立即失效。</p>
     */
    private Integer studentPwdMustChange;

    /**
     * 状态：0-离校 1-在读
     */
    private Integer studentStatus;

    /**
     * 归属用户ID（学生端数据隔离依据，登录时需带进令牌）
     */
    private String studentOwnerUserId;
}
