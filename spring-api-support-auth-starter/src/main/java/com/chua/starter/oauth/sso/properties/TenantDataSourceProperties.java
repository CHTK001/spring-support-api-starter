package com.chua.starter.oauth.sso.properties;


import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static com.chua.starter.oauth.sso.properties.TenantDataSourceProperties.PRE;

/**
 * 租户数据源属性配置类
 * 用于读取配置文件中以 "spring.tenant" 为前缀的租户相关数据源设置
 */
@Data
@ConfigurationProperties(prefix = PRE, ignoreInvalidFields = true)
public class TenantDataSourceProperties {

    /**
     * 配置前缀常量，用于绑定配置项
     */
    public static final String PRE = "spring.tenant";

    /**
     * 租户数据源配置列表，每个租户对应一个数据源配置
     */
    public List<DataSourceProperties> properties;

    /**
     * 是否启用租户功能
     */
    private boolean enable;
}
