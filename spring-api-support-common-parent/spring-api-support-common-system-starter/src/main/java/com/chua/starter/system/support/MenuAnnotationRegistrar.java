package com.chua.starter.system.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.system.annotations.Menu;
import com.chua.starter.system.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 注解菜单注册器
 *
 * <p>扫描 Spring 容器与 Spring MVC 控制器上的 {@link Menu} 注解，按 {@code sysMenuName}
 * 幂等 upsert 进 {@code sys_menu}。注解只提供「默认值」：已存在的菜单只补空字段，
 * 不覆盖管理员改过的标题、排序、角色与开关，避免每次重启把线上配置冲掉。</p>
 *
 * <p>类级与方法级注解都生效；同一 {@code name} 两侧都有时方法级优先（声明更具体）。
 * 带 {@code parent} 的菜单分两轮处理，保证父菜单先拿到主键。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class MenuAnnotationRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 菜单映射器
     */
    private final SysMenuMapper sysMenuMapper;

    /**
     * Spring 容器，用于枚举 Bean 上的类级注解与按名取 MVC 映射
     */
    private final ApplicationContext applicationContext;

    /**
     * 顶级菜单 pid
     */
    private static final int ROOT_PID = 0;

    /**
     * Spring MVC 映射的 bean 名
     *
     * <p>按名字取而不是按类型注入：上下文里同时存在 {@code requestMappingHandlerMapping} 与
     * Actuator 的 {@code controllerEndpointHandlerMapping} 两个同类型 Bean，按类型注入必然
     * 抛 {@code NoUniqueBeanDefinitionException}；后者只含端点映射，扫描它没有意义。</p>
     *
     * <p>也不在构造期注入：{@code RequestMappingHandlerMapping} 由 WebMvcConfigurer 体系创建，
     * 构造期强制注入容易和自定义映射 Bean 形成装配环（框架侧 {@code ApiFeatureConfiguration}
     * 就为此被迫加 {@code @Lazy}）。这里改为 {@code ApplicationReadyEvent} 之后再取。</p>
     */
    private static final String HANDLER_MAPPING_BEAN = "requestMappingHandlerMapping";

    /**
     * 条件类型默认值
     */
    private static final String DEFAULT_SHOW_TYPE = "ALWAYS";

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            int created = registerAll();
            log.info("注解菜单注册完成: 新增 {} 条", created);
        } catch (Exception e) {
            // 注册失败不阻断启动，菜单仍可用数据库里已有的数据
            log.error("注解菜单注册失败，保留数据库中已有菜单", e);
        }
    }

    /**
     * 扫描并注册全部注解菜单。
     *
     * @return 新增条数
     */
    public int registerAll() {
        Map<String, Menu> declared = collectDeclarations();

        Map<String, Integer> idByName = new LinkedHashMap<>();
        for (SysMenu existing : sysMenuMapper.selectList(
                Wrappers.<SysMenu>lambdaQuery().select(SysMenu::getSysMenuId, SysMenu::getSysMenuName))) {
            if (null != existing.getSysMenuName()) {
                idByName.put(existing.getSysMenuName(), existing.getSysMenuId());
            }
        }

        int created = 0;
        // 两轮：先落无父菜单拿到 id，再落有父菜单，保证父子顺序
        for (int round = 0; round < 2; round++) {
            boolean wantParent = round == 1;
            for (Map.Entry<String, Menu> entry : declared.entrySet()) {
                boolean hasParent = StringUtils.isNotBlank(entry.getValue().parent());
                if (hasParent != wantParent) {
                    continue;
                }
                Integer parentId = hasParent
                        ? idByName.getOrDefault(entry.getValue().parent().trim(), ROOT_PID)
                        : ROOT_PID;
                UpsertOutcome outcome = upsert(entry.getKey(), entry.getValue(), parentId);
                if (outcome.created()) {
                    created++;
                }
                if (null != outcome.menuId()) {
                    idByName.put(entry.getKey(), outcome.menuId());
                }
            }
        }
        return created;
    }

    /**
     * 收集类级与方法级注解声明，方法级覆盖类级。
     *
     * @return 菜单名 → 注解
     */
    private Map<String, Menu> collectDeclarations() {
        Map<String, Menu> declared = new LinkedHashMap<>();
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (Exception e) {
                continue;
            }
            if (null == bean) {
                continue;
            }
            Menu anno = AnnotatedElementUtils.findMergedAnnotation(AopUtils.getTargetClass(bean), Menu.class);
            if (null != anno) {
                declared.put(anno.name().trim(), anno);
            }
        }
        if (!applicationContext.containsBean(HANDLER_MAPPING_BEAN)) {
            log.warn("未找到 {} Bean，跳过方法级 @Menu 注解扫描", HANDLER_MAPPING_BEAN);
            return declared;
        }
        RequestMappingHandlerMapping mapping =
                applicationContext.getBean(HANDLER_MAPPING_BEAN, RequestMappingHandlerMapping.class);
        for (HandlerMethod handler : mapping.getHandlerMethods().values()) {
            Menu anno = AnnotatedElementUtils.findMergedAnnotation(handler.getMethod(), Menu.class);
            if (null == anno) {
                anno = AnnotatedElementUtils.findMergedAnnotation(handler.getBeanType(), Menu.class);
            }
            if (null != anno) {
                declared.put(anno.name().trim(), anno);
            }
        }
        return declared;
    }

    /**
     * upsert 单条注解菜单：存在则只补空字段并落库，不存在则插入。
     *
     * @param name     菜单名（幂等键）
     * @param anno     注解
     * @param parentId 父菜单 id
     * @return 处理结果
     */
    private UpsertOutcome upsert(String name, Menu anno, Integer parentId) {
        SysMenu existing = sysMenuMapper.selectOne(Wrappers.<SysMenu>lambdaQuery()
                .eq(SysMenu::getSysMenuName, name)
                .last("limit 1"));
        if (null == existing) {
            SysMenu menu = new SysMenu();
            menu.setSysMenuName(name);
            menu.setSysMenuPid(parentId);
            fillDefaults(menu, anno);
            if (sysMenuMapper.insert(menu) > 0) {
                return new UpsertOutcome(true, menu.getSysMenuId());
            }
            return new UpsertOutcome(false, null);
        }
        fillDefaults(existing, anno);
        sysMenuMapper.updateById(existing);
        return new UpsertOutcome(false, existing.getSysMenuId());
    }

    /**
     * 只补空字段，保留管理员改过的值。
     *
     * @param menu 菜单
     * @param anno 注解
     */
    private void fillDefaults(SysMenu menu, Menu anno) {
        if (StringUtils.isBlank(menu.getSysMenuTitle())) {
            menu.setSysMenuTitle(anno.title());
        }
        if (StringUtils.isBlank(menu.getSysMenuPath())) {
            menu.setSysMenuPath(anno.path());
        }
        if (StringUtils.isBlank(menu.getSysMenuComponent())) {
            menu.setSysMenuComponent(anno.component());
        }
        if (StringUtils.isBlank(menu.getSysMenuIcon())) {
            menu.setSysMenuIcon(anno.icon());
        }
        if (StringUtils.isBlank(menu.getSysMenuPerm())) {
            menu.setSysMenuPerm(anno.perm());
        }
        if (null == menu.getSysMenuPid()) {
            menu.setSysMenuPid(ROOT_PID);
        }
        if (null == menu.getSysMenuSort()) {
            menu.setSysMenuSort(anno.sort());
        }
        // 条件字段同理：未配置或仍是默认值时才写入注解声明
        if (StringUtils.isBlank(menu.getSysMenuShowType())
                || DEFAULT_SHOW_TYPE.equalsIgnoreCase(menu.getSysMenuShowType().trim())) {
            menu.setSysMenuShowType(StringUtils.isBlank(anno.showType()) ? DEFAULT_SHOW_TYPE : anno.showType().trim());
        }
        if (StringUtils.isBlank(menu.getSysMenuShowValue())) {
            menu.setSysMenuShowValue(anno.showValue());
        }
    }

    /**
     * upsert 结果
     *
     * @param created 是否新增
     * @param menuId  菜单主键
     */
    private record UpsertOutcome(boolean created, Integer menuId) {
    }
}
