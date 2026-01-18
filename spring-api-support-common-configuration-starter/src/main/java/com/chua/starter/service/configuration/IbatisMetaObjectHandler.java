package com.chua.starter.service.configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.chua.common.support.core.constant.Action;
import com.chua.common.support.base.converter.Converter;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.user.UserResult;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.function.Supplier;

import static com.chua.common.support.core.constant.Action.CREATE;
import static com.chua.common.support.core.constant.Action.UPDATE;

/**
 * mybatis-plus 字段自动填充
 *
 * @author haoxr
 * @link https://mp.baomidou.com/guide/auto-fill-metainfo.html
 * @since 2022/10/14
 */
@Component
@ConditionalOnClass(name = {"com.baomidou.mybatisplus.core.handlers.MetaObjectHandler"})
public class IbatisMetaObjectHandler implements MetaObjectHandler {

    @Autowired(required = false)
    private AuthClientProperties authProperties;

    /**
     * 新增填充创建时间
     *
     * @param metaObject metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.hasGetter("createTime")) {
            registerField(metaObject, "createTime", CREATE);
        }


        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        if(clientExecute.whiteList()) {
            return;
        }
        UserResult userLoginInfo = clientExecute.getUserResult();
        if(null != userLoginInfo) {
            if (metaObject.hasGetter("createBy")) {
                registerField(metaObject, "createBy", userLoginInfo.getOption("sysUserId", userLoginInfo.getUserId()), CREATE);
            }

            if (metaObject.hasGetter("createName")) {
                registerField(metaObject, "createName",  StringUtils.defaultString(userLoginInfo.getOption("userRealName"), userLoginInfo.getUsername()), CREATE);
            }

            if (metaObject.hasGetter("operationUser")) {
                registerField(metaObject, "operationUser", StringUtils.defaultString(userLoginInfo.getOption("userRealName"), userLoginInfo.getUsername()), CREATE);
            }

            if (metaObject.hasGetter("sysDeptId")) {
                registerField(metaObject, "sysDeptId", userLoginInfo.getOption("sysDeptId"), CREATE);
            }

            if (metaObject.hasGetter("tenantId")) {
                registerField(metaObject, "tenantId", userLoginInfo.getOption("tenantId"), CREATE);
            }
        }

    }

    /**
     * 更新填充更新时间
     *
     * @param metaObject metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasGetter("updateTime")) {
            registerField(metaObject, "updateTime", UPDATE);
        }

        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        if(clientExecute.whiteList()) {
            return;
        }
        UserResult userLoginInfo = clientExecute.getUserResult();
        if(null != userLoginInfo) {
            if (metaObject.hasGetter("updateBy")) {
                registerField(metaObject, "updateBy", userLoginInfo.getOption("sysUserId", userLoginInfo.getUserId()), UPDATE);
            }
            if (metaObject.hasGetter("updateName")) {
                registerField(metaObject, "updateName",  StringUtils.defaultString(userLoginInfo.getOption("userRealName"), userLoginInfo.getUsername()), UPDATE);
            }
            if (metaObject.hasGetter("operationUser")) {
                registerField(metaObject, "operationUser", StringUtils.defaultString(userLoginInfo.getOption("userRealName"), userLoginInfo.getUsername()), UPDATE);
            }
        }
    }

    private void registerField(MetaObject metaObject, String name, Action action) {
        Class<?> type = metaObject.getGetterType(name);
//        Object value = metaObject.getValue(name);
//        if(null != value) {
//            return ;
//        }
        if (action == UPDATE) {
            metaObject.setValue(name, null);
        }
        if(LocalDateTime.class.isAssignableFrom(type)) {
            if(action == CREATE) {
                this.strictInsertFill(metaObject, name, LocalDateTime::now, LocalDateTime.class);
            } else if(action == UPDATE) {
                this.strictUpdateFill(metaObject, name, LocalDateTime::now, LocalDateTime.class);
            }
            return;
        }

        if(LocalDate.class.isAssignableFrom(type)) {
            if(action == CREATE) {
                this.strictInsertFill(metaObject, name, LocalDate::now, LocalDate.class);
            } else if(action == UPDATE) {
                this.strictUpdateFill(metaObject, name, LocalDate::now, LocalDate.class);
            }
            return;
        }

        if(LocalTime.class.isAssignableFrom(type)) {
            if(action == CREATE) {
                this.strictInsertFill(metaObject, name, LocalTime::now, LocalTime.class);
            } else if(action == UPDATE) {
                this.strictUpdateFill(metaObject, name, LocalTime::now, LocalTime.class);
            }
            return;
        }

        if(Date.class.isAssignableFrom(type)) {
            if(action == CREATE) {
                this.strictInsertFill(metaObject, name, Date::new, Date.class);
            } else if(action == UPDATE) {
                this.strictUpdateFill(metaObject, name, Date::new, Date.class);
            }
        }

    }
    @SuppressWarnings("unchecked")
    private void registerField(MetaObject metaObject, String name, Object value, Action action) {
        Class<?> type = metaObject.getGetterType(name);
        if(action == CREATE) {
            this.strictInsertFill(metaObject, name, (Supplier) () -> Converter.convertIfNecessary(value, type), type);
            return;
        }
        metaObject.setValue(name, null);
        this.strictUpdateFill(metaObject, name, (Supplier) () -> Converter.convertIfNecessary(value, type), type);
    }

}
