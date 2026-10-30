package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.system.service.impl.SysMenuServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 菜单新增必填守卫的回归测试（P0：admin 登录后侧栏一个菜单都不显示）。
 *
 * <h3>这个 P0 是怎么发生的</h3>
 * <p>权限矩阵验收时用空体 {@code {}} 探 {@code POST /v2/menu/save}，请求竟然成功了 ——
 * 因为 {@code SysMenu} 实体上的 {@code @Validated(AddGroup.class)} 分组里<b>没有任何必填约束</b>，
 * 空体直接 INSERT 出一条标题/路径/组件/名称全为 NULL 的菜单行。这类行 {@code parentId=0}
 * 且被判定为叶子菜单，前端侧边栏建树时抛异常，<b>整棵菜单树不渲染</b>：
 * 实测 26 条进、0 条出，表现为最高权限账号 admin 侧栏全空（普通用户因接口返回空、
 * 回落到静态路由反而正常）。</p>
 *
 * <h3>为什么不在实体上加 @NotBlank</h3>
 * <p>仓库里有三个同名 {@code AddGroup}：{@code com.chua.starter.system.validator.group}、
 * {@code com.chua.common.support.utils.group}、{@code com.chua.common.support.base.validator.group}。
 * 实体在 entity-starter，引的是 utils 层那个；控制器 {@code SysMenuController} 用的是 system 层那个。
 * <b>两者是不同的类</b>，实体上的 {@code groups} 约束与 {@code @Validated} 永远匹配不上、静默失效。
 * 因此修法是服务层显式守卫，本测试钉的就是这道守卫。</p>
 *
 * <h3>为什么依赖能传 null</h3>
 * <p>守卫全部在 {@code baseMapper.insert(sysMenu)} <b>之前</b> return，
 * 所以 {@code new SysMenuServiceImpl(null, null, null, null)} 就足以覆盖全部拒绝路径 ——
 * 依赖为 null 恰好保证了「一旦守卫失效就会立刻 NPE」，不会静默通过。
 * 反过来说：如果哪天有人把守卫挪到 insert 之后，本类会立刻变红，这正是想要的。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("菜单新增必填守卫（P0 回归网）")
class SysMenuServiceImplTest {

    /**
     * 构造一个依赖全为 null 的 service。
     *
     * @return service
     */
    private SysMenuServiceImpl service() {
        // 构造器新增了 MenuVisibilityEvaluator；守卫仍在 insert 之前返回，
        // 依赖传 null 可继续保证「守卫失效即 NPE」
        return new SysMenuServiceImpl(null, null, null, null);
    }

    /**
     * 构造一份最小合法菜单。
     *
     * @return 菜单
     */
    private SysMenu validMenu() {
        SysMenu menu = new SysMenu();
        menu.setSysMenuTitle("测试菜单");
        menu.setSysMenuType(0);
        menu.setSysMenuSort(1);
        return menu;
    }

    /**
     * 核心用例：空体必须被拒绝。
     *
     * <p>这一条在修复前是<b>返回成功</b>的 —— 它就是 P0 的直接成因。</p>
     */
    @Test
    @DisplayName("空体被拒绝-不再产生全NULL菜单行")
    void 空体被拒绝() {
        ReturnResult<SysMenu> result = service().save(new SysMenu(), Set.of("ADMIN"));

        assertFalse(result.isSuccess(), "空体不得被接受 —— 修复前它会 INSERT 出全 NULL 菜单行，"
                + "该行使前端建树抛异常、admin 侧栏全空（P0）");
        assertTrue(result.getMsg().contains("菜单标题不能为空"), "实际: " + result.getMsg());
    }

    /**
     * 只有标题时，类型缺失应被指出。
     */
    @Test
    @DisplayName("仅标题-提示缺少菜单类型")
    void 仅标题被拒绝() {
        SysMenu menu = new SysMenu();
        menu.setSysMenuTitle("只有标题");

        ReturnResult<SysMenu> result = service().save(menu, Set.of("ADMIN"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("菜单类型不能为空"), "实际: " + result.getMsg());
    }

    /**
     * 标题+类型时，排序缺失应被指出。
     */
    @Test
    @DisplayName("标题加类型-提示缺少菜单排序")
    void 缺排序被拒绝() {
        SysMenu menu = new SysMenu();
        menu.setSysMenuTitle("标题与类型");
        menu.setSysMenuType(0);

        ReturnResult<SysMenu> result = service().save(menu, Set.of("ADMIN"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("菜单排序不能为空"), "实际: " + result.getMsg());
    }

    /**
     * 空白标题也算缺失。
     *
     * <p>只判 null 的话，{@code "   "} 能过，随后入库一条标题为空白的菜单，
     * 侧边栏同样渲染不出来。</p>
     */
    @Test
    @DisplayName("空白标题同样被拒绝")
    void 空白标题被拒绝() {
        SysMenu menu = validMenu();
        menu.setSysMenuTitle("   ");

        ReturnResult<SysMenu> result = service().save(menu, Set.of("ADMIN"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("菜单标题不能为空"), "实际: " + result.getMsg());
    }

    /**
     * 菜单实体为 null 时应被拒绝。
     */
    @Test
    @DisplayName("null菜单被拒绝")
    void null菜单被拒绝() {
        ReturnResult<SysMenu> result = service().save(null, Set.of("ADMIN"));

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("菜单不存在"), "实际: " + result.getMsg());
    }

    /**
     * 按钮类菜单（type=3）允许 path/name 为空 —— 这条不能被守卫误伤。
     *
     * <p>现有 35 条菜单中 type=3 的有 12 条，它们的 path 与 name <b>全部为空</b>。
     * 如果有人日后「顺手」把 path/name 也加进必填，按钮菜单就再也创建不出来。
     * 这里用一个反向断言把它钉住：必填项只能是 title/type/sort。</p>
     */
    @Test
    @DisplayName("按钮类菜单-允许path与name为空")
    void 按钮类菜单不被误伤() {
        SysMenu menu = new SysMenu();
        menu.setSysMenuTitle("某个按钮");
        menu.setSysMenuType(3);
        menu.setSysMenuSort(1);
        // path / name 故意留空

        // 三项必填齐了，守卫应当放行 —— 于是会走到 baseMapper.insert(null) 并抛 NPE。
        // 断言 NPE 是在断言「守卫没有误伤」：若守卫误伤，返回的会是 ReturnResult 而不是异常。
        boolean guardPassed = false;
        try {
            service().save(menu, Set.of("ADMIN"));
        } catch (NullPointerException expected) {
            // baseMapper 为 null，走到 insert 才炸 = 守卫已放行 = 符合预期
            guardPassed = true;
        }

        assertTrue(guardPassed,
                "path/name 不该是必填：按钮类菜单(type=3)本来就没有它们，"
                        + "现有 12 条按钮菜单的 path/name 全为空，加必填会打断按钮菜单创建");
    }

    /**
     * 校验顺序固定为 标题 → 类型 → 排序。
     *
     * <p>顺序很重要：一次请求只应回一个最靠前的错误，否则用户会连环补字段。
     * 同时也防止有人把三处 if 的顺序调换后，提示语对不上。</p>
     */
    @Test
    @DisplayName("校验顺序-标题优先于类型与排序")
    void 校验顺序固定() {
        SysMenu menu = new SysMenu();   // 三项全缺

        ReturnResult<SysMenu> result = service().save(menu, Set.of("ADMIN"));

        assertEquals("菜单标题不能为空", result.getMsg(),
                "三项全缺时应先报标题，一次只提示一个最靠前的错误");
    }
}