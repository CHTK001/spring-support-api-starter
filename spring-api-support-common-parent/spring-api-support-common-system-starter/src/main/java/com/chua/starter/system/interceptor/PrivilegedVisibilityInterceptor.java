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
 * @author CH
 */
public class PrivilegedVisibilityInterceptor extends DataPermissionInterceptor {
    private final Supplier<AuthService> authServiceSupplier;

    public PrivilegedVisibilityInterceptor(DataPermissionHandler dataPermissionHandler) {
        this(dataPermissionHandler, () -> null);
    }

    public PrivilegedVisibilityInterceptor(DataPermissionHandler dataPermissionHandler,
                                           Supplier<AuthService> authServiceSupplier) {
        super(dataPermissionHandler);
        this.authServiceSupplier = authServiceSupplier;
    }

    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
    }

    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
    }

    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        if (!shouldApply()) {
            return;
        }
        super.processSelect(select, index, sql, obj);
    }

    private boolean shouldApply() {
        CurrentUser currentUser = getCurrentUser();
        return null != currentUser;
    }

    private CurrentUser getCurrentUser() {
        if (authServiceSupplier == null) {
            return null;
        }
        AuthService authService = authServiceSupplier.get();
        return authService == null ? null : authService.getCurrentUser();
    }
}
