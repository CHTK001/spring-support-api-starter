package com.chua.starter.service.configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.chua.common.support.constant.Action;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.user.UserResult;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.function.Supplier;

import static com.chua.common.support.constant.Action.CREATE;
import static com.chua.common.support.constant.Action.UPDATE;

/**
 * mybatis-plus 字段自动填充
 *
 * @author haoxr
 * @link https://mp.baomidou.com/guide/auto-fill-metainfo.html
 * @since 2022/10/14
 */
@Component
@RequiredArgsConstructor
public class IbatisMetaObjectHandler implements MetaObjectHandler {

    private final AuthClientProperties authProperties;

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


        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        if(null != userLoginInfo) {
            if (metaObject.hasGetter("createBy")) {
                registerField(metaObject, "createBy",  userLoginInfo.getExtValue("sysUserId"), CREATE);
            }
            if (metaObject.hasGetter("createName")) {
                registerField(metaObject, "createName",  StringUtils.defaultString(userLoginInfo.getExtValue("userRealName"), userLoginInfo.getUsername()), CREATE);
            }

            if (metaObject.hasGetter("sysDeptId")) {
                registerField(metaObject, "sysDeptId", userLoginInfo.getExtValue("sysDeptId"), CREATE);
            }

            if (metaObject.hasGetter("tenantId")) {
                registerField(metaObject, "tenantId", userLoginInfo.getExtValue("tenantId"), CREATE);
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

        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        if(null != userLoginInfo) {
            if (metaObject.hasGetter("updateBy")) {
                registerField(metaObject, "updateBy",  userLoginInfo.getExtValue("sysUserId"), UPDATE);
            }
            if (metaObject.hasGetter("updateName")) {
                registerField(metaObject, "updateName",  StringUtils.defaultString(userLoginInfo.getExtValue("userRealName"), userLoginInfo.getUsername()), UPDATE);
            }
        }
    }

    private void registerField(MetaObject metaObject, String name, Action action) {
        Class<?> type = metaObject.getGetterType(name);
        Object value = metaObject.getValue(name);
        if(null != value) {
            return ;
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
        this.strictUpdateFill(metaObject, name, (Supplier) () -> Converter.convertIfNecessary(value, type), type);
    }

}
