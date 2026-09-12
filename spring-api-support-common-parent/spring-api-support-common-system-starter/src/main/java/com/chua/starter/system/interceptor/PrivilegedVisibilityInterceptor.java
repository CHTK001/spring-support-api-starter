package com.chua.starter.system.interceptor;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import java.util.function.Supplier;

/**
* 高权账号可见性拦截器
*
* @since 2025/5/1
* @author CH
 */
public class PrivilegedVisibilityInterceptor extends DataPermissionInterceptor {
    /**
    * 鉴权服务提供者
     */
    private final Supplier<AuthService> authServiceSupplier;

    /**
    * 构造拦截器，鉴权服务提供者为空。
    *
    * @param dataPermissionHandler 数据权限处理器
     */
    public PrivilegedVisibilityInterceptor(DataPermissionHandler dataPermissionHandler) {
        this(dataPermissionHandler, () -> null);
    }

    /**
    * 构造拦截器。
    *
    * @param dataPermissionHandler 数据权限处理器
    * @param authServiceSupplier    鉴权服务提供者
     */
    public PrivilegedVisibilityInterceptor(DataPermissionHandler dataPermissionHandler,
                                           Supplier<AuthService> authServiceSupplier) {
        super(dataPermissionHandler);
        this.authServiceSupplier = authServiceSupplier;
    }

    /**
    * 处理删除语句（当前不限制）。
    *
    * @param delete 删除语句
    * @param index  语句序号
    * @param sql    原始 SQL
    * @param obj    参数对象
     */
    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
    }

    /**
    * 处理更新语句（当前不限制）。
    *
    * @param update 更新语句
    * @param index  语句序号
    * @param sql    原始 SQL
    * @param obj    参数对象
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
    }

    /**
    * 处理查询语句，仅当存在当前登录用户时生效。
    *
    * @param select 查询语句
    * @param index  语句序号
    * @param sql    原始 SQL
    * @param obj    参数对象
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        if (!shouldApply()) {
            return;
        }
        super.processSelect(select, index, sql, obj);
    }

    /**
    * 判断是否需要应用数据权限。
    *
    * @return true 表示需要应用
     */
    private boolean shouldApply() {
        CurrentUser currentUser = getCurrentUser();
        return null != currentUser;
    }

    /**
    * 获取当前登录用户。
    *
    * @return 当前用户，无鉴权服务时为 空
     */
    private CurrentUser getCurrentUser() {
        if (authServiceSupplier == null) {
            return null;
        }
        AuthService authService = authServiceSupplier.get();
        return authService == null ? null : authService.getCurrentUser();
    }
}
