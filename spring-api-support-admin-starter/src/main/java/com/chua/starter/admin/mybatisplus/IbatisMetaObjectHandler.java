package com.chua.starter.admin.mybatisplus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.user.UserResult;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * mybatis-plus 字段自动填充
 *
 * @author haoxr
 * @link https://mp.baomidou.com/guide/auto-fill-metainfo.html
 * @since 2022/10/14
 */
@Component
public class IbatisMetaObjectHandler implements MetaObjectHandler {

    @Resource
    private AuthClientProperties authProperties;

    /**
     * 新增填充创建时间
     *
     * @param metaObject metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        if(null != userLoginInfo) {
            this.strictInsertFill(metaObject, "createBy", () -> userLoginInfo.getExtValue("userId"), String.class);
            this.strictInsertFill(metaObject, "createName", () -> StringUtils.defaultString(
                    userLoginInfo.getExtValue("userRealName"), userLoginInfo.getUsername()
            ), String.class);
        }

    }

    /**
     * 更新填充更新时间
     *
     * @param metaObject metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        if(null != userLoginInfo) {
            this.strictUpdateFill(metaObject, "updateBy", () -> userLoginInfo.getExtValue("userId"), String.class);
            this.strictUpdateFill(metaObject, "updateName", () -> StringUtils.defaultString(
                    userLoginInfo.getExtValue("userRealName"), userLoginInfo.getUsername()
            ), String.class);
        }
    }

}
