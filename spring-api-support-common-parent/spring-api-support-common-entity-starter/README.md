# spring-api-support-common-entity-starter

## 📖 模块简介

**实体定义模块** - 统一定义所有业务实体类，按业务域分包管理，作为最基础的 Common 模块被所有其他模块依赖。

## 🎯 模块定位

- **基础层模块** - 最底层的模块，不依赖其他业务模块
- **职责** - 提供统一的实体定义和数据库映射
- **原则** - 按业务域分包，保持实体的独立性

## 📦 模块结构

```
spring-api-support-common-entity-starter/
├── entity/
│   ├── oauth/                           # OAuth认证域
│   │   ├── SysUser.java                 # 用户实体
│   │   ├── SysRole.java                 # 角色实体
│   │   ├── SysMenu.java                 # 菜单实体
│   │   ├── SysDept.java                 # 部门实体
│   │   ├── SysUserRole.java             # 用户角色关联
│   │   └── SysRoleMenu.java             # 角色菜单关联
│   │
│   ├── service/                         # 通用服务域
│   │   ├── SysDict.java                 # 字典实体
│   │   ├── SysFile.java                 # 文件实体
│   │   ├── SysMessage.java              # 消息实体
│   │   ├── SysSetting.java              # 设置实体
│   │   └── SysHoliday.java              # 节假日实体
│   │
│   ├── project/                         # 项目管理域
│   │   ├── ProjectAi.java               # AI项目实体
│   │   ├── ProjectEmail.java            # 邮件项目实体
│   │   ├── ProjectSms.java              # 短信项目实体
│   │   ├── ProjectDevice.java           # 设备项目实体
│   │   └── ProjectLog.java              # 项目日志实体
│   │
│   └── monitor/                         # 监控域
│       ├── MonitorApp.java              # 应用实体
│       ├── MonitorServer.java           # 服务器实体
│       ├── MonitorMetrics.java          # 指标实体
│       └── MonitorAlert.java            # 告警实体
│
└── enums/                               # 枚举定义
    ├── UserStatus.java                  # 用户状态枚举
    ├── RoleType.java                    # 角色类型枚举
    └── MenuType.java                    # 菜单类型枚举
```

## ✨ 核心特性

### 1. 统一实体定义

- ✅ 所有实体类统一管理
- ✅ 使用 MyBatis Plus 注解
- ✅ 支持自动填充和逻辑删除
- ✅ 完整的字段注释

### 2. 按业务域分包

- ✅ oauth - OAuth 认证相关实体
- ✅ service - 通用服务相关实体
- ✅ project - 项目管理相关实体
- ✅ monitor - 监控运维相关实体

### 3. 规范的命名

- ✅ 表名：sys_user、project_ai 等
- ✅ 字段名：sys_user_id、sys_user_username 等
- ✅ 类名：SysUser、ProjectAi 等

## 💡 使用方式

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-entity-starter</artifactId>
</dependency>
```

### 2. 使用实体

```java
import com.chua.starter.entity.oauth.SysUser;

@Service
public class UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    public SysUser getUserById(Long id) {
        return sysUserMapper.selectById(id);
    }
}
```

### 3. 实体示例

```java
/**
 * 用户实体
 *
 * @author CH
 * @since 2024/12/11
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    /**
     * 用户ID
     */
    @TableId(value = "sys_user_id", type = IdType.AUTO)
    private Long sysUserId;

    /**
     * 用户名
     */
    @TableField("sys_user_username")
    private String sysUserUsername;

    /**
     * 密码
     */
    @TableField("sys_user_password")
    private String sysUserPassword;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("sys_user_status")
    private Integer sysUserStatus;

    /**
     * 创建时间
     */
    @TableField(value = "sys_user_create_time", fill = FieldFill.INSERT)
    private LocalDateTime sysUserCreateTime;

    /**
     * 更新时间
     */
    @TableField(value = "sys_user_update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime sysUserUpdateTime;
}
```

## 🎨 设计规范

### 实体类规范

1. **使用 Lombok 简化代码**

```java
@Data
@TableName("sys_user")
public class SysUser {
    // 字段定义
}
```

2. **字段命名规范**

```java
// 表名：sys_user
// 字段名：sys_user_id, sys_user_username
// 实体字段：sysUserId, sysUserUsername
```

3. **必要的注解**

```java
@TableId(value = "sys_user_id", type = IdType.AUTO)  // 主键
@TableField("sys_user_username")                      // 字段映射
@TableLogic                                           // 逻辑删除
```

### 枚举类规范

```java
/**
 * 用户状态枚举
 *
 * @author CH
 * @since 2024/12/11
 */
@Getter
public enum UserStatus {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用");

    private final Integer code;
    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
```

## 🔗 依赖关系

```
所有业务模块
    ↓ 依赖
spring-api-support-common-entity-starter (实体定义)
    ↓ 依赖
MyBatis Plus (ORM框架)
Lombok (代码简化)
```

## 📝 开发规范

### 1. 新增实体

创建新实体时遵循以下步骤：

1. 确定业务域（oauth/service/project/monitor）
2. 创建实体类并添加注释
3. 使用 Lombok 注解
4. 添加 MyBatis Plus 注解
5. 定义字段并添加注释

### 2. 字段注释规范

```java
/**
 * 用户ID
 * <p>
 * 主键，自增
 * </p>
 */
@TableId(value = "sys_user_id", type = IdType.AUTO)
private Long sysUserId;
```

### 3. 时间字段自动填充

```java
/**
 * 创建时间
 */
@TableField(value = "sys_user_create_time", fill = FieldFill.INSERT)
private LocalDateTime sysUserCreateTime;

/**
 * 更新时间
 */
@TableField(value = "sys_user_update_time", fill = FieldFill.INSERT_UPDATE)
private LocalDateTime sysUserUpdateTime;
```

## 🔗 相关模块

- [spring-api-support-common-configuration-starter](../spring-api-support-common-configuration-starter) - 配置定义模块
- [spring-support-mybatis-starter](../../spring-support-parent-starter/spring-support-mybatis-starter) - MyBatis 支持模块

## 📄 许可证

本项目采用 Apache License 2.0 许可证。

---

**作者**: CH  
**版本**: 4.0.0  
**更新时间**: 2024/12/11
