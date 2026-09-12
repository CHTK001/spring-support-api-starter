package com.chua.starter.service.configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.enums.Action;
import com.chua.common.support.utils.StringUtils;
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

import static com.chua.common.support.enums.Action.CREATE;
import static com.chua.common.support.enums.Action.UPDATE;

/**
* MyBatis-plus 字段自动填充
*
* @since 2024/10/14
* @author CH
 */
@Component
@ConditionalOnClass(name = {"com.baomidou.mybatisplus.core.handlers.MetaObjectHandler"})
public class IbatisMetaObjectHandler implements MetaObjectHandler {

    @Autowired(required = false)
    /** 认证属性 */
    private AuthClientProperties authProperties;

    /**
    * 新增填充创建时间
    *
    * @param metaObject meta对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.hasGetter("createTime")) {
            registerField(metaObject, "createTime", CREATE);
        }

        if (metaObject.hasGetter("createdAt")) {
            registerField(metaObject, "createdAt", CREATE);
        }

        UserResult userLoginInfo = getCurrentUser();
        if(null != userLoginInfo) {
            if (metaObject.hasGetter("createBy")) {
                registerField(metaObject, "createBy", option(userLoginInfo, "sysUserId", userLoginInfo.getUserId()), CREATE);
            }

            if (metaObject.hasGetter("createName")) {
                registerField(metaObject, "createName",  StringUtils.defaultString(option(userLoginInfo, "userRealName"), userLoginInfo.getUsername()), CREATE);
            }

            if (metaObject.hasGetter("operationUser")) {
                registerField(metaObject, "operationUser", StringUtils.defaultString(option(userLoginInfo, "userRealName"), userLoginInfo.getUsername()), CREATE);
            }

            if (metaObject.hasGetter("sysDeptId")) {
                registerField(metaObject, "sysDeptId", option(userLoginInfo, "sysDeptId"), CREATE);
            }

            if (metaObject.hasGetter("tenantId")) {
                registerField(metaObject, "tenantId", option(userLoginInfo, "tenantId"), CREATE);
            }
        }

    }

    /**
    * 更新填充更新时间
    *
    * @param metaObject meta对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasGetter("updateTime")) {
            registerField(metaObject, "updateTime", UPDATE);
        }

        if (metaObject.hasGetter("updatedAt")) {
            registerField(metaObject, "updatedAt", UPDATE);
        }

        UserResult userLoginInfo = getCurrentUser();
        if(null != userLoginInfo) {
            if (metaObject.hasGetter("updateBy")) {
                registerField(metaObject, "updateBy", option(userLoginInfo, "sysUserId", userLoginInfo.getUserId()), UPDATE);
            }
            if (metaObject.hasGetter("updateName")) {
                registerField(metaObject, "updateName",  StringUtils.defaultString(option(userLoginInfo, "userRealName"), userLoginInfo.getUsername()), UPDATE);
            }
            if (metaObject.hasGetter("operationUser")) {
                registerField(metaObject, "operationUser", StringUtils.defaultString(option(userLoginInfo, "userRealName"), userLoginInfo.getUsername()), UPDATE);
            }
        }
    }

    /**
    * 获取当前用户
    *
    * @return 获取当前用户的结果
     */
    private UserResult getCurrentUser() {
        if (authProperties != null && !authProperties.isEnable()) {
            return null;
        }
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        if (clientExecute.whiteList()) {
            return null;
        }
        return clientExecute.getSafeUserResult();
    }

    /**
    * 从用户扩展属性中取值（兼容旧版双参 获取期权 的语义）。
    *
    * @param userResult   用户信息
    * @param optionKey    扩展属性键
    * @param defaultValue 缺省值
    * @return 属性值（null 时回退缺省值）
     */
    private String option(UserResult userResult, String optionKey, String defaultValue) {
        Object value = userResult.getOption(optionKey);
        String text = StringUtils.toString(value);
        return StringUtils.defaultString(text, defaultValue);
    }

    /**
    * 从用户扩展属性中取值。
    *
    * @param userResult 用户信息
    * @param optionKey  扩展属性键
    * @return 属性值
     */
    private String option(UserResult userResult, String optionKey) {
        Object value = userResult.getOption(optionKey);
        return StringUtils.toString(value);
    }

    /**
    * 按字段类型注册自动填充（时间字段直接填充当前值）。
    *
    * @param metaObject 元对象
    * @param name       字段名
    * @param action     操作类型（新增/更新）
     */
    private void registerField(MetaObject metaObject, String name, Action action) {
        Class<?> type = metaObject.getGetterType(name);
//        Object value = metaObject.getValue(name);
//        if(null != value) {
 // 返回 ;
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
    /**
    * 按字段类型注册自动填充（自定义值，经类型转换后填充）。
    *
    * @param metaObject 元对象
    * @param name       字段名
    * @param value      待填充的值
    * @param action     操作类型（新增/更新）
     */
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
