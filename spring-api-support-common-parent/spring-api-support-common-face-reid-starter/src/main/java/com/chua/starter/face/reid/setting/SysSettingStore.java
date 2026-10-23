package com.chua.starter.face.reid.setting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于通用配置表 {@code sys_setting} 的人脸重识别配置存储（弱耦合适配）。
 *
 * <h3>设计取舍</h3>
 * <p>仓库中 {@code video} 模块通过直接依赖
 * {@code spring-api-support-common-service-starter} 读写 {@code SysSettingService}。
 * 本模块<b>不</b>照搬这一做法：那会把 MyBatis-Plus、Redis、GeoIP 等整串数据层依赖
 * 强加给「只想用人脸重识别」的宿主，而配置持久化只是一个可选增强——没有数据库时
 * 回退 {@code application.yml} 即可，识别链路完全不受影响。</p>
 *
 * <p>因此本类是一层<b>可失败即降级</b>的适配：</p>
 * <ul>
 *   <li>无任何对配置服务/实体的编译期引用，类路径上没有它们也能编译、启动；</li>
 *   <li>运行期由 {@link ObjectProvider} 按类型名取宿主容器中的配置服务，有则用；</li>
 *   <li>任意一次调用异常都退化为「不可用」，不影响识别主流程。</li>
 * </ul>
 *
 * <p>需要完整的配置管理（分组、变更历史、导入导出）时，用 {@code /v2/setting}
 * 管理端维护 {@code faceReid} 分组即可，本适配会自动读到。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Slf4j
public class SysSettingStore implements FaceReidSettingStore {

    /**
     * 配置服务全限定名。
     */
    public static final String SERVICE_CLASS = "com.chua.starter.service.SysSettingService";

    /**
     * 配置实体全限定名。
     */
    public static final String ENTITY_CLASS = "com.chua.starter.entity.service.SysSetting";

    /**
     * 状态：启用。
     */
    private static final int STATUS_ENABLED = 1;

    /**
     * 标记：系统内置。
     */
    private static final int IN_SYSTEM = 1;

    /**
     * 配置服务提供者。
     */
    private final ObjectProvider<?> provider;

    /**
     * 构造存储。
     *
     * @param provider 配置服务提供者（按类型名查找）
     */
    public SysSettingStore(ObjectProvider<?> provider) {
        this.provider = provider;
    }

    /**
     * 类路径是否具备配置服务与实体。
     *
     * @return 具备返回 true
     */
    public static boolean supported() {
        ClassLoader loader = SysSettingStore.class.getClassLoader();
        return load(SERVICE_CLASS, loader) != null && load(ENTITY_CLASS, loader) != null;
    }

    /**
     * 安全加载类。
     *
     * @param name   类名
     * @param loader 类加载器
     * @return 类型，不存在返回 null
     */
    private static Class<?> load(String name, ClassLoader loader) {
        try {
            return Class.forName(name, false, loader);
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public boolean available() {
        return service() != null;
    }

    /**
     * 取出配置服务。
     *
     * @return 服务，不可用返回 null
     */
    private Object service() {
        if (provider == null) {
            return null;
        }
        try {
            Class<?> type = load(SERVICE_CLASS, Thread.currentThread().getContextClassLoader());
            Object bean = provider.getIfAvailable();
            return type != null && type.isInstance(bean) ? bean : null;
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public List<Row> list(String group) {
        Object service = service();
        if (service == null) {
            return List.of();
        }
        try {
            Object wrapper = wrapper(service, group, null);
            Object rows = method(service, "list", 1).invoke(service, wrapper);
            List<Row> result = new ArrayList<>();
            if (rows instanceof Iterable<?> iterable) {
                for (Object entity : iterable) {
                    String name = String.valueOf(getter(entity, "getSysSettingName"));
                    Object value = getter(entity, "getSysSettingValue");
                    Object sort = getter(entity, "getSysSettingSort");
                    result.add(new Row(name, value == null ? null : String.valueOf(value),
                            sort instanceof Number number ? number.intValue() : 0));
                }
            }
            return result;
        } catch (Throwable e) {
            log.debug("[FaceReid] 读取 sys_setting 失败，降级使用 application 配置: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void upsert(String group, String name, String value, String valueType, String remark, int sort) {
        Object service = service();
        if (service == null) {
            return;
        }
        try {
            Object existing = method(service, "getOne", 2).invoke(service, wrapper(service, group, name), false);
            if (existing == null) {
                method(service, "save", 1).invoke(service, entity(service, group, name, value, valueType, remark, sort));
                return;
            }
            if (!java.util.Objects.equals(getter(existing, "getSysSettingValue"), value)) {
                existing.getClass().getMethod("setSysSettingValue", String.class).invoke(existing, value);
                method(service, "updateById", 1).invoke(service, existing);
            }
        } catch (Throwable e) {
            log.debug("[FaceReid] 写入 sys_setting 失败: {}", e.getMessage());
        }
    }

    @Override
    public int deleteByPrefix(String group, String namePrefix) {
        Object service = service();
        if (service == null) {
            return 0;
        }
        try {
            Object removed = method(service, "remove", 1).invoke(service, wrapper(service, group, null, namePrefix));
            return Boolean.TRUE.equals(removed) ? 1 : 0;
        } catch (Throwable e) {
            log.debug("[FaceReid] 删除 sys_setting 失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 查找服务上的指定方法。
     *
     * @param service 服务
     * @param name    方法名
     * @param arity   参数个数
     * @return 方法
     * @throws NoSuchMethodException 不存在
     */
    private Method method(Object service, String name, int arity) throws NoSuchMethodException {
        for (Method candidate : service.getClass().getMethods()) {
            if (candidate.getName().equals(name) && candidate.getParameterCount() == arity) {
                return candidate;
            }
        }
        throw new NoSuchMethodException(name + "/" + arity);
    }

    /**
     * 读取 getter。
     *
     * @param entity 实体
     * @param name   getter 名
     * @return 值
     * @throws Exception 反射失败
     */
    private Object getter(Object entity, String name) throws Exception {
        return entity.getClass().getMethod(name).invoke(entity);
    }

    /**
     * 构造查询条件。
     *
     * @param service    服务
     * @param group      分组
     * @param name       名称（null 表示查整组）
     * @param namePrefix 名称前缀（null 表示不用）
     * @return 条件
     * @throws Exception 反射失败
     */
    private Object wrapper(Object service, String group, String name, String... namePrefix) throws Exception {
        ClassLoader loader = service.getClass().getClassLoader();
        Class<?> entity = Class.forName(ENTITY_CLASS, false, loader);
        Class<?> wrappers = Class.forName("com.baomidou.mybatisplus.core.toolkit.Wrappers", false, loader);
        Object query = wrappers.getMethod("lambdaQuery", Class.class).invoke(null, entity);
        Class<?> chain = Class.forName("com.baomidou.mybatisplus.core.conditions.AbstractWrapper", false, loader);
        Class<?> sfunc = Class.forName("com.baomidou.mybatisplus.core.toolkit.support.SFunction", false, loader);
        Method eq = chain.getMethod("eq", sfunc, Object.class);
        eq.invoke(query, column(loader, entity, sfunc, "getSysSettingGroup"), group);
        if (name != null) {
            eq.invoke(query, column(loader, entity, sfunc, "getSysSettingName"), name);
        } else if (namePrefix != null && namePrefix.length > 0 && namePrefix[0] != null) {
            chain.getMethod("likeRight", sfunc, Object.class)
                    .invoke(query, column(loader, entity, sfunc, "getSysSettingName"), namePrefix[0]);
        } else {
            eq.invoke(query, column(loader, entity, sfunc, "getSysSettingStatus"), STATUS_ENABLED);
        }
        return query;
    }

    /**
     * 构造 SFunction 列引用。
     *
     * <p>MyBatis-Plus 需要从方法引用里解析属性名；此处用动态代理让
     * {@code toString} 返回 getter 名，供其工具类反射解析。</p>
     *
     * @param loader 类加载器
     * @param entity 实体类型
     * @param sfunc  SFunction 类型
     * @param getter getter 名
     * @return 代理
     */
    private Object column(ClassLoader loader, Class<?> entity, Class<?> sfunc, String getter) {
        return java.lang.reflect.Proxy.newProxyInstance(loader, new Class<?>[]{sfunc},
                (proxy, invoked, args) -> switch (invoked.getName()) {
                    case "hashCode" -> getter.hashCode();
                    case "equals" -> proxy == args[0];
                    case "toString" -> getter;
                    default -> null;
                });
    }

    /**
     * 新建配置实体。
     *
     * @param service   服务
     * @param group     分组
     * @param name      名称
     * @param value     值
     * @param valueType 类型
     * @param remark    备注
     * @param sort      排序
     * @return 实体
     * @throws Exception 反射失败
     */
    private Object entity(Object service, String group, String name, String value,
                          String valueType, String remark, int sort) throws Exception {
        Class<?> type = Class.forName(ENTITY_CLASS, false, service.getClass().getClassLoader());
        Object entity = type.getDeclaredConstructor().newInstance();
        setter(type, entity, "setSysSettingGroup", String.class, group);
        setter(type, entity, "setSysSettingName", String.class, name);
        setter(type, entity, "setSysSettingValue", String.class, value);
        setter(type, entity, "setSysSettingValueType", String.class, valueType);
        setter(type, entity, "setSysSettingRemark", String.class, remark);
        setter(type, entity, "setSysSettingSort", Integer.class, sort);
        setter(type, entity, "setSysSettingStatus", Integer.class, STATUS_ENABLED);
        setter(type, entity, "setSysSettingInSystem", Integer.class, IN_SYSTEM);
        setter(type, entity, "setSysSettingCollect", Integer.class, 0);
        return entity;
    }

    /**
     * 安全调用 setter（字段缺失或类型不符时跳过）。
     *
     * @param type   实体类型
     * @param target 目标
     * @param name   方法名
     * @param param  参数类型
     * @param value  值
     */
    private void setter(Class<?> type, Object target, String name, Class<?> param, Object value) {
        try {
            type.getMethod(name, param).invoke(target, value);
        } catch (Exception ignored) {
            // 实体版本差异时忽略缺失字段
        }
    }
}
