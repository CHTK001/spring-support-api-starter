package com.chua.spider.support.configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器。
 *
 * <p>为 {@code createTime}/{@code updateTime} 标注了
 * {@code @TableField(fill = FieldFill.INSERT / INSERT_UPDATE)} 的实体自动填充当前时间。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Component
public class SpiderMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入填充字段名
     */
    private static final String CREATE_TIME = "createTime";

    /**
     * 更新填充字段名
     */
    private static final String UPDATE_TIME = "updateTime";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);
        strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
