# spring-api-support-common-system-starter

## 📖 模块简介

**系统服务模块** - 提供用户、角色、菜单、部门等基础系统服务，作为 Common 模块被其他应用模块依赖。

## 🎯 模块定位

- **Common 模块** - 被非 Common 模块依赖的基础服务
- **职责** - 提供系统管理相关的服务和接口
- **不包含** - 应用启动类和独立运行能力

## ✨ 核心功能

### 👥 用户服务

- ✅ 用户信息管理
- ✅ 用户权限查询
- ✅ 用户角色关联
- ✅ 用户部门关联
- ✅ 用户状态管理

### 🔐 角色服务

- ✅ 角色信息管理
- ✅ 角色权限分配
- ✅ 角色菜单关联
- ✅ 角色用户关联

### 📋 菜单服务

- ✅ 菜单树形结构
- ✅ 菜单权限控制
- ✅ 动态路由生成
- ✅ 按钮权限管理

### 🏢 部门服务

- ✅ 部门树形结构
- ✅ 部门层级管理
- ✅ 部门用户关联

## 📦 模块结构

```
spring-api-support-common-system-starter/
├── service/
│   ├── SysUserService.java                # 用户服务接口
│   ├── SysRoleService.java                # 角色服务接口
│   ├── SysMenuService.java                # 菜单服务接口
│   ├── SysDeptService.java                # 部门服务接口
│   └── impl/                              # 服务实现
│       ├── SysUserServiceImpl.java
│       ├── SysRoleServiceImpl.java
│       ├── SysMenuServiceImpl.java
│       └── SysDeptServiceImpl.java
├── mapper/
│   ├── SysUserMapper.java                 # 用户Mapper
│   ├── SysRoleMapper.java                 # 角色Mapper
│   ├── SysMenuMapper.java                 # 菜单Mapper
│   └── SysDeptMapper.java                 # 部门Mapper
└── pojo/
    ├── SysUserVO.java                     # 用户视图对象
    ├── SysRoleVO.java                     # 角色视图对象
    ├── SysMenuVO.java                     # 菜单视图对象
    └── SysDeptVO.java                     # 部门视图对象
```

## 🔗 依赖关系

```
应用模块 (system-starter, monitor-starter等)
    ↓ 依赖
spring-api-support-common-system-starter (系统服务模块)
    ↓ 依赖
spring-api-support-common-oauth-starter (OAuth认证模块)
    ↓ 依赖
spring-api-support-common-entity-starter (实体定义)
spring-api-support-common-configuration-starter (配置定义)
```

## 💡 使用方式

### 1. 添加依赖

在应用模块的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-system-starter</artifactId>
</dependency>
```

### 2. 注入服务

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/{id}")
    public ReturnResult<SysUserVO> getUser(@PathVariable Long id) {
        return sysUserService.getUserById(id);
    }
}
```

### 3. 使用示例

#### 查询用户信息

```java
// 根据ID查询用户
ReturnResult<SysUserVO> result = sysUserService.getUserById(1L);

// 分页查询用户
Page<SysUserVO> page = sysUserService.getUserPage(pageNum, pageSize, query);

// 根据用户名查询
SysUserVO user = sysUserService.getUserByUsername("admin");
```

#### 角色权限管理

```java
// 查询用户角色
List<SysRoleVO> roles = sysRoleService.getUserRoles(userId);

// 分配角色权限
sysRoleService.assignPerms(roleId, menuIds);

// 查询角色菜单
List<SysMenuVO> menus = sysMenuService.getRoleMenus(roleId);
```

#### 菜单树查询

```java
// 获取完整菜单树
List<SysMenuVO> menuTree = sysMenuService.getMenuTree();

// 获取用户菜单树（权限过滤）
List<SysMenuVO> userMenus = sysMenuService.getUserMenuTree(userId);
```

## 🎨 设计原则

### 1. 职责单一

- ✅ 只提供系统管理相关服务
- ✅ 不包含业务逻辑
- ✅ 不包含应用启动类

### 2. 依赖清晰

- ✅ 依赖 common-oauth-starter 获取认证信息
- ✅ 依赖 common-entity-starter 使用实体定义
- ✅ 不依赖具体应用模块

### 3. 易于扩展

- ✅ 接口与实现分离
- ✅ 支持自定义实现
- ✅ 灵活的配置选项

## 🔒 权限控制

### 数据权限

系统支持多种数据权限范围：

- **全部数据** - 查看所有数据
- **本部门及以下** - 查看本部门及下级部门数据
- **本部门** - 仅查看本部门数据
- **仅本人** - 仅查看自己的数据
- **自定义** - 自定义数据范围

### 使用示例

```java
@DataScope(deptAlias = "d", userAlias = "u")
public List<SysUser> getUserList(SysUserQuery query) {
    // 自动根据用户权限过滤数据
    return sysUserMapper.selectList(query);
}
```

## 📝 开发规范

### 服务层规范

```java
/**
 * 用户服务接口
 *
 * @author CH
 * @since 2024/12/11
 */
public interface SysUserService {

    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    ReturnResult<SysUserVO> getUserById(Long userId);
}
```

### Mapper 层规范

```java
/**
 * 用户Mapper
 *
 * @author CH
 * @since 2024/12/11
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectByUsername(@Param("username") String username);
}
```

## 🔗 相关模块

- [spring-api-support-common-oauth-starter](../spring-api-support-common-oauth-starter) - OAuth 认证模块
- [spring-api-support-common-entity-starter](../spring-api-support-common-entity-starter) - 实体定义模块
- [spring-api-support-system-starter](../spring-api-support-system-starter) - 系统应用模块

## 📄 许可证

本项目采用 Apache License 2.0 许可证。

---

**作者**: CH  
**版本**: 4.0.0  
**更新时间**: 2024/12/11
