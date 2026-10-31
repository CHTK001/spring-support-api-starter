package com.chua.starter.system.login;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.oauth.client.support.user.UserInfoService;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.system.configuration.EduAuthDataSourceConfiguration;
import com.chua.starter.system.entity.EduStudentAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 学生登录服务：让 OAuth 服务端认识「学生」这种账号。
 *
 * <p><b>为什么放在这里</b>：OAuth 服务端通过
 * {@code LoginCheck#resolveUserInfoService} 按 authType 取 {@link UserInfoService}
 * 实现——先扫 Spring 容器的 {@code @Spi} 注解，再回退 SPI 文件。本类与
 * 既有的 WEB / TENANT / WECHAT 等 9 个实现并列，注册名 {@code STUDENT}。</p>
 *
 * <p><b>不能放在 oauth 模块，也不能放 eduai 模块</b>：
 * oauth 处于 api 层、eduai 处于 spring 层，api 层引用 spring 层是反向依赖；
 * 而 eduai 的类无法被 oauth 进程加载（两进程互不共享容器）。故本类只依赖
 * {@code edu_student} 的<b>表结构</b>这一稳定契约，不引用 eduai 的任何 Java 类型。</p>
 *
 * <p><b>令牌签发不在这里</b>：{@code LoginCheck#doLogin} 拿到本类返回的
 * {@link UserResume} 后统一走 {@code TokenResolver} 签发，因此本类只负责
 * 「验明身份 + 如实描述这个人」。</p>
 *
 * <p><b>跨库查询</b>：{@code edu_student} 在 eduai 库，用
 * {@link EduAuthDataSourceConfiguration} 提供的独立 JdbcTemplate 访问，
 * 不占用主库连接池、也不把该表纳入主库的 MyBatis 映射。</p>
 *
 * <p><b>条件装配</b>：本类只在安装了 {@code plugin.edu-auth.datasource}
 * 时才创建 Bean（与 {@link EduAuthDataSourceConfiguration}
 * 的 {@code @ConditionalOnProperty} 条件一致）。租户端 / 平台端 server 专度
 * 不配 {@code plugin.edu-auth}，故学生登录不创建 Bean——这反而保护了容器：
 * 学生登录强依赖 {@code @Qualifier("eduAuthJdbcTemplate")} 注入，
 * 如果创造 Bean 时容器里只有 Spring Boot 默认的 {@code jdbcTemplate}，
 * {@code UnsatisfiedDependencyException} 会发生（文档见
 * {@link EduAuthDataSourceConfiguration}）。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Slf4j
@Service
@Spi("STUDENT")
@ConditionalOnProperty(prefix = "plugin.edu-auth", name = "datasource.url", matchIfMissing = false)
public class StudentLoginService implements UserInfoService {

    /**
     * 在读状态：离校学生不允许登录
     */
    private static final int STATUS_ENROLLED = 1;

    /**
     * 按学号查询（显式列出列名而非 {@code SELECT *}：列集合是稳定契约，
     * 显式列出可让「上游加列」不影响本查询的映射结果）。
     */
    private static final String SQL_BY_STUDENT_NO = """
            SELECT student_id, student_no, student_name, student_class_id,
                   student_grade_level, student_grade, student_parent_phone,
                   student_password, student_pwd_salt, student_pwd_must_change,
                   student_status, student_owner_user_id
            FROM edu_student
            WHERE student_no = ?
            LIMIT 1
            """;

    /**
     * 按家长手机号查询（不加 LIMIT：手机号可带多个孩子，由调用方按条数决定放行与否）。
     */
    private static final String SQL_BY_PARENT_PHONE = """
            SELECT student_id, student_no, student_name, student_class_id,
                   student_grade_level, student_grade, student_parent_phone,
                   student_password, student_pwd_salt, student_pwd_must_change,
                   student_status, student_owner_user_id
            FROM edu_student
            WHERE student_parent_phone = ?
            """;

    /**
     * 学生档案行映射器（下划线列名自动转驼峰字段）。
     */
    private static final BeanPropertyRowMapper<EduStudentAuth> ROW_MAPPER =
            new BeanPropertyRowMapper<>(EduStudentAuth.class);

    /**
     * 学生档案查询模板
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 创建学生登录服务。
     *
     * @param eduAuthJdbcTemplate 学生档案专用的 JdbcTemplate（指向 eduai 库）
     */
    public StudentLoginService(
            @Qualifier(EduAuthDataSourceConfiguration.JDBC_TEMPLATE_NAME)
            JdbcTemplate eduAuthJdbcTemplate) {
        this.jdbcTemplate = eduAuthJdbcTemplate;
    }

    /**
     * 学生登录校验。
     *
     * <p>登录标识支持学号与家长手机号两种（与 eduai 侧口径一致）：
     * 学号优先，未命中才按手机号反查。同一手机号绑定多个孩子时拒绝登录，
     * 不替用户猜是哪个孩子。</p>
     *
     * @param principal  登录标识（学号或家长手机号）
     * @param credential 口令明文
     * @param address    客户端地址
     * @param ext        扩展参数（本场景未使用）
     * @return 用户信息；凭证不合法时返回带 {@code message} 的实例，由调用方转成失败结果
     */
    @Override
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        if (StringUtils.isBlank(principal)) {
            return denied("请输入学号或家长手机号");
        }
        if (StringUtils.isBlank(credential)) {
            return denied("请输入登录密码");
        }
        String account = principal.trim();

        List<EduStudentAuth> candidates = findByStudentNo(account);
        if (candidates.isEmpty()) {
            // 学号没命中才按家长手机号反查：一个号码可能带多个孩子
            candidates = findByParentPhone(account);
        }
        if (candidates.isEmpty()) {
            log.info("学生登录失败: 学号或家长手机号不存在 {}, IP: {}", account, address);
            return denied("学号或家长手机号不存在");
        }
        if (candidates.size() > 1) {
            // 不猜：同号下多个孩子时，任选其一都是错的学生
            log.info("学生登录失败: 手机号绑定多个学生 {}, IP: {}", account, address);
            return denied("该手机号绑定了多个学生，请改用学号登录");
        }

        EduStudentAuth student = candidates.get(0);
        if (student.getStudentStatus() == null || student.getStudentStatus() != STATUS_ENROLLED) {
            log.info("学生登录失败: 已离校 {}, IP: {}", account, address);
            return denied("该学生已离校，无法登录");
        }
        if (StringUtils.isBlank(student.getStudentPassword())) {
            // 老数据可能没存口令：初始口令阶段直接允许用「学号」登录
            if (!isInitialPasswordLogin(student, credential)) {
                log.info("学生登录失败: 未设置口令 {}, IP: {}", account, address);
                return denied("尚未设置登录密码，请联系班主任或管理员设置");
            }
        } else if (!passwordMatches(credential, student.getStudentPwdSalt(), student.getStudentPassword())
                && !isInitialPasswordLogin(student, credential)) {
            log.warn("学生登录失败: 口令不匹配 - 学号: {}, IP: {}", student.getStudentNo(), address);
            return denied("密码错误");
        }

        log.info("学生登录成功: {} (ID:{}), IP: {}", student.getStudentNo(), student.getStudentId(), address);
        return buildResume(student);
    }

    /**
     * 令牌升级（续期）时重建用户信息。
     *
     * <p>续期请求携带的是已签发的令牌，其中 {@code userId} 就是学号，
     * 据此重查档案可保证续期后学生信息（班级、学段等）仍是最新的，
     * 而不是照抄旧令牌里的快照。</p>
     *
     * @param userResult 令牌中解析出的用户信息
     * @return 重建后的用户信息；查不到档案时返回带 {@code message} 的实例
     */
    @Override
    public UserResume upgrade(UserResult userResult) {
        if (userResult == null || StringUtils.isBlank(userResult.getUserId())) {
            return denied("登录态已失效，请重新登录");
        }
        List<EduStudentAuth> found = findByStudentNo(userResult.getUserId().trim());
        if (found.isEmpty()) {
            log.warn("学生令牌续期失败: 档案不存在 - 学号: {}", userResult.getUserId());
            return denied("学生档案不存在");
        }
        EduStudentAuth student = found.get(0);
        if (student.getStudentStatus() == null || student.getStudentStatus() != STATUS_ENROLLED) {
            return denied("该学生已离校，无法续期");
        }
        return buildResume(student);
    }

    /**
     * 按学号查学生（不过滤状态，以便给出「已离校」的准确提示）。
     *
     * @param studentNo 学号
     * @return 命中的学生列表，最多一条
     */
    private List<EduStudentAuth> findByStudentNo(String studentNo) {
        return jdbcTemplate.query(SQL_BY_STUDENT_NO, ROW_MAPPER, studentNo);
    }

    /**
     * 按家长手机号查学生。
     *
     * @param phone 家长手机号
     * @return 命中的学生列表；可能多条
     */
    private List<EduStudentAuth> findByParentPhone(String phone) {
        return jdbcTemplate.query(SQL_BY_PARENT_PHONE, ROW_MAPPER, phone);
    }

    /**
     * 校验口令。
     *
     * <p>与 eduai 侧同算法（{@code hmacSha256(明文, 盐)}），否则两边签发的口令
     * 无法互认。比较用 {@link MessageDigest#isEqual}——定长比较不按字符提前返回，
     * 避免通过响应耗时逐位试探摘要。</p>
     *
     * @param raw    口令明文
     * @param salt   口令盐
     * @param stored 库中已存摘要
     * @return 匹配返回 true
     */
    private boolean passwordMatches(String raw, String salt, String stored) {
        if (StringUtils.isBlank(salt) || StringUtils.isBlank(stored)) {
            return false;
        }
        String actual = DigestUtils.hmacSha256(raw, salt);
        if (actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 判定是否属于「初始口令阶段用学号登录」。
     *
     * <p>建档默认口令 = 学号，但存量数据可能没存口令摘要、或摘要与学号不一致；
     * 只要学生**尚未自助改密**（{@code student_pwd_must_change = 1}），
     * 就允许用学号作为默认口令登录。学生一旦自行改密，标记置 0，默认口令立即失效。</p>
     *
     * @param student    学生档案
     * @param credential 本次提交的口令明文
     * @return 允许按默认口令放行时返回 true
     */
    private boolean isInitialPasswordLogin(EduStudentAuth student, String credential) {
        Integer mustChange = student.getStudentPwdMustChange();
        if (mustChange == null || mustChange != 1) {
            return false;
        }
        String no = student.getStudentNo();
        return StringUtils.isNotBlank(no) && no.equals(credential == null ? null : credential.trim());
    }

    /**
     * 组装用户信息。
     *
     * <p>{@code userId} 用学号：学生端其它接口按 {@code student_account = 登录态 userId}
     * 定位档案，与 eduai 侧口径一致。班级、学段等放 {@code ext}，
     * 因为 {@link UserResume} 没有对应字段。</p>
     *
     * @param student 学生档案
     * @return 用户信息
     */
    private UserResume buildResume(EduStudentAuth student) {
        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("studentId", student.getStudentId());
        ext.put("studentNo", student.getStudentNo());
        ext.put("studentName", student.getStudentName());
        ext.put("studentClassId", student.getStudentClassId());
        ext.put("studentGradeLevel", student.getStudentGradeLevel());
        ext.put("studentGrade", student.getStudentGrade());
        ext.put(AuthConstant.ACCOUNT_TYPE_KEY, "student");
        if (StringUtils.isNotBlank(student.getStudentOwnerUserId())) {
            ext.put("ownerUserId", student.getStudentOwnerUserId());
        }

        UserResume resume = new UserResume();
        resume.setUserId(student.getStudentNo());
        resume.setUsername(student.getStudentNo());
        resume.setNickName(StringUtils.isBlank(student.getStudentName())
                ? student.getStudentNo() : student.getStudentName());
        resume.setPhone(student.getStudentParentPhone());
        // 角色只给 STUDENT：不给任何管理侧角色，避免误挂到按角色声明的注解上
        resume.setRoles(Set.of(AuthConstant.STUDENT));
        resume.setPermission(Collections.emptySet());
        resume.setExt(ext);
        return resume;
    }

    /**
     * 构造拒绝结果。
     *
     * <p>用 {@code message} 而非抛异常：{@code LoginCheck#doLogin} 以
     * {@code message} 非空判定失败，并把它作为失败原因回给客户端；
     * 抛异常会被统一成「认证异常」而丢掉具体原因。</p>
     *
     * @param message 失败原因
     * @return 仅含 message 的用户信息
     */
    private UserResume denied(String message) {
        UserResume resume = new UserResume();
        resume.setMessage(message);
        return resume;
    }
}
