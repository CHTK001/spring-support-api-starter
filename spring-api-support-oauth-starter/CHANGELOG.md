# 更新日志

## [4.0.0] - 2024-12-02

### 🚀 新增功能

#### gRPC 协议服务端支持

- **新增 gRPC 服务实现** - 完整的 OAuth 认证 gRPC 服务端实现
- **GrpcProtocol 服务类** - 实现 OAuthServiceGrpc.OAuthServiceImplBase
- **Protocol Buffers 定义** - 与客户端一致的 protobuf 服务定义
- **gRPC 安全机制** - 服务端签名验证和加密支持
- **Spring Boot 集成** - 使用@GrpcService 注解自动注册服务

#### 协议架构增强

- **统一协议接口** - 服务端 Protocol 接口统一实现
- **多协议并存** - 支持 HTTP、gRPC、TCP、UDP 协议同时运行
- **协议自动发现** - 通过 SPI 机制自动发现和注册协议实现
- **性能优化** - gRPC 协议的高性能二进制通信

#### 安全机制强化

- **GrpcAuthInformation** - gRPC 协议的认证信息处理
- **签名验证** - 完整的请求签名验证机制
- **防重放攻击** - 时间戳和 UUID 验证
- **访问控制** - AK/SK 密钥验证

### 🔧 配置增强

#### 新增配置项

```yaml
plugin:
  oauth:
    server:
      # 支持的协议列表
      protocols: [http, grpc, tcp, udp]

      # gRPC服务器端口
      grpc-port: 9090

      # gRPC TLS配置
      grpc-enable-tls: false
      grpc-cert-chain-file: "/path/to/cert.pem"
      grpc-private-key-file: "/path/to/key.pem"

      # gRPC消息大小限制
      grpc-max-inbound-message-size: 4194304 # 4MB
```

#### 协议配置更新

- **协议列表扩展** - 新增 grpc 协议支持
- **端口配置** - 独立的 gRPC 服务端口配置
- **TLS 支持** - gRPC TLS 加密配置选项
- **性能调优** - 消息大小和连接参数配置

### 📦 依赖更新

#### 新增依赖

- `io.grpc:grpc-netty-shaded:1.58.0` - gRPC 网络传输
- `io.grpc:grpc-protobuf:1.58.0` - Protocol Buffers 支持
- `io.grpc:grpc-stub:1.58.0` - gRPC 服务端存根
- `com.google.protobuf:protobuf-java:3.24.4` - Protocol Buffers Java 库
- `net.devh:grpc-server-spring-boot-starter:2.15.0.RELEASE` - Spring Boot gRPC 服务端集成

#### 构建工具更新

- `protobuf-maven-plugin:0.6.1` - Protocol Buffers 编译插件
- `os-maven-plugin:1.7.1` - 操作系统检测插件

### 🏗️ 架构改进

#### gRPC 服务架构

```
OAuthServiceGrpc.OAuthServiceImplBase
    ↓
GrpcProtocol (实现Protocol接口)
    ↓
各种AuthInformation处理类
    ↓
具体的Authorization实现
```

#### 服务方法实现

- **login()** - 用户登录认证
- **authenticate()** - OAuth 令牌验证
- **upgrade()** - 令牌升级和刷新
- **logout()** - 用户登出

#### 认证信息处理

- **GrpcAuthInformation** - gRPC 协议适配器
- **GrpcRequest/GrpcResponse** - 模拟 HTTP 请求响应对象
- **统一认证流程** - 与现有 HTTP 协议保持一致的认证流程

### 🔒 安全增强

#### gRPC 安全工具类

- **GrpcSecurityUtils** - 服务端安全工具类
- **签名生成和验证** - MD5 和 SHA-256 哈希算法
- **加密解密** - AES 对称加密支持
- **安全参数验证** - 完整的安全参数检查

#### 安全机制

- **请求验证** - 完整的 gRPC 请求验证流程
- **签名校验** - 基于请求参数的签名验证
- **时间戳检查** - 防止重放攻击
- **密钥管理** - AK/SK 访问密钥管理

### 🔄 协议兼容性

#### 多协议支持

- **HTTP 协议** - 保持现有 HTTP 协议完全兼容
- **gRPC 协议** - 新增高性能 gRPC 协议支持
- **TCP 协议** - 二进制 TCP 协议支持
- **UDP 协议** - 无连接 UDP 协议支持

#### 协议切换

- **配置驱动** - 通过配置文件控制启用的协议
- **动态注册** - SPI 机制自动发现协议实现
- **并行服务** - 多个协议可以同时提供服务

### 📚 文档更新

#### 代码文档

- **完整的 JavaDoc** - 所有新增类和方法的详细文档
- **中文注释** - 符合项目规范的中文代码注释
- **架构说明** - 详细的 gRPC 服务架构说明

#### 配置文档

- **协议配置** - 详细的 gRPC 协议配置说明
- **安全配置** - gRPC 安全机制配置指南
- **性能调优** - gRPC 性能优化建议

### 🚀 性能优化

#### gRPC 协议优势

- **HTTP/2 基础** - 多路复用和头部压缩
- **二进制协议** - Protocol Buffers 高效序列化
- **流式通信** - 支持双向流式通信（预留）
- **连接复用** - 长连接和多路复用

#### 服务端优化

- **异步处理** - 非阻塞的异步请求处理
- **连接管理** - 高效的连接池管理
- **内存优化** - 优化的内存使用和垃圾回收
- **并发处理** - 高并发请求处理能力

### 🧪 测试支持

#### 测试覆盖

- **单元测试** - gRPC 服务方法的单元测试
- **集成测试** - 完整的 gRPC 认证流程测试
- **性能测试** - gRPC vs HTTP 性能对比
- **安全测试** - 安全机制的验证测试

#### 测试工具

- **gRPC 客户端测试** - 专用的 gRPC 测试客户端
- **协议兼容性测试** - 多协议兼容性验证
- **负载测试** - 高并发场景下的性能测试

### 🔧 运维支持

#### 监控和日志

- **详细日志** - 完整的 gRPC 请求和响应日志
- **性能指标** - gRPC 协议的性能监控指标
- **错误处理** - 完善的错误处理和异常日志
- **调试支持** - 便于调试的详细信息输出

#### 部署支持

- **容器化** - Docker 容器化部署支持
- **服务发现** - 与现有服务发现机制集成
- **负载均衡** - 支持 gRPC 负载均衡
- **健康检查** - gRPC 服务健康检查

### 📋 已知问题

#### 当前限制

- **TLS 支持** - 当前版本使用明文传输，生产环境需要配置 TLS
- **流式通信** - 当前实现为一元 RPC，流式通信功能待后续版本
- **服务发现** - 当前使用静态配置，动态服务发现待后续版本

#### 后续计划

- **TLS 加密** - 完整的 TLS/SSL 支持
- **流式 API** - 支持服务端流和双向流
- **服务网格** - 集成 Istio 等服务网格
- **监控集成** - 集成 Prometheus 等监控系统

### 🔄 迁移指南

#### 从 HTTP 到 gRPC

1. **更新配置** - 在 protocols 列表中添加 grpc
2. **配置端口** - 设置 grpc-port 配置项
3. **客户端更新** - 客户端配置 protocol 为 grpc
4. **测试验证** - 验证 gRPC 协议功能正常

#### 混合部署

- **渐进迁移** - 可以逐步将客户端从 HTTP 迁移到 gRPC
- **协议并存** - HTTP 和 gRPC 协议可以同时提供服务
- **负载分担** - 不同客户端可以使用不同协议

---

## [4.0.0-beta] - 2024-11-XX

### 🔧 优化改进

- 优化认证性能
- 完善 TCP 和 UDP 协议服务端实现
- 增强令牌管理机制

### 🐛 问题修复

- 修复并发认证问题
- 修复令牌过期处理问题

---

_更多历史版本信息请查看 Git 提交记录_
