# spring-support-api-starter

## Project Introduction

**Spring Boot 3 Enterprise API Service Scaffold** - Based on Spring Boot 3.x and Java 21, providing a complete enterprise-level backend service solution.

### Core Features

- ✅ **Modular design** - Maven multi-module, clear responsibilities
- ✅ **OAuth 2.0 authentication** - Unified single sign-on system
- ✅ **Project management** - Unified AI, email, SMS, and device project management
- ✅ **System management** - RBAC permissions, user, role, and menu management
- ✅ **General services** - Dictionary, file, message, and settings
- ✅ **Monitoring & operations** - Complete system monitoring and operations tools

### Technology Stack

- **Java 21** - Latest LTS, supports Records, Pattern Matching, and other new features
- **Spring Boot 3.5.5** - Latest stable version
- **MyBatis Plus** - Database ORM framework
- **Redis** - Cache and Token storage
- **OAuth 2.0** - Authentication and authorization protocol
- **Swagger/OpenAPI** - API documentation
- **Undertow** - Lightweight servlet container (alternative to Tomcat)
- **Virtual Threads** - Virtual thread concurrency model

## Project Structure

```
spring-support-api-starter/
├── Foundation Layer
│   ├── spring-api-support-common-entity-starter          # Entity definitions
│   └── spring-api-support-common-configuration-starter   # Configuration definitions
│
├── Core Layer (Common Modules)
│   ├── spring-api-support-common-oauth-starter           # OAuth authentication
│   ├── spring-api-support-common-service-starter         # General services module
│   ├── spring-api-support-common-system-starter          # System services module
│   ├── spring-api-support-common-project-starter         # Project management module
│   └── spring-api-support-common-video-starter           # Video services module
│
└── Application Layer
    ├── spring-api-support-oauth-starter                  # OAuth server application
    ├── spring-api-support-system-starter                 # System management application
    ├── spring-api-support-monitor-starter                # Monitoring application
    ├── spring-api-support-tenanted-starter               # Multi-tenant application
    ├── spring-api-support-scheduler-starter              # Scheduler application
    └── spring-api-support-video-starter                  # Video platform application
```

## Module Description

### 📦 Foundation Layer

| Module                             | Responsibility                        | Docs |
| ---------------------------------- | ------------------------------------ | ---- |
| **common-entity-starter**          | Unified entity definitions, per domain | [view](./spring-api-support-common-entity-starter/README.md) |
| **common-configuration-starter**   | Public configuration classes, interceptors, filters | [view](./spring-api-support-common-configuration-starter/README.md) |

### 🔧 Core Layer (Common)

| Module                       | Responsibility                              | Docs |
| -------------------------- | ----------------------------------------- | ---- |
| **common-oauth-starter**   | OAuth authentication, login, Token management | [view](./spring-api-support-common-oauth-starter/README.md) |
| **common-service-starter** | Dictionary, file, message, settings        | [view](./spring-api-support-common-service-starter/README.md) |
| **common-system-starter**  | User, role, menu, department               | [view](./spring-api-support-common-system-starter/README.md) |
| **common-project-starter** | AI, email, SMS, device project management | [view](./spring-api-support-common-project-starter/README.md) |
| **common-video-starter**   | Video-related services                    | - |

### 🚀 Application Layer

| Module                  | Responsibility              | Port  | Docs |
| --------------------- | --------------------------- | ----- | ---- |
| **oauth-starter**     | OAuth SSO server            | 19180 | [view](./spring-api-support-oauth-starter/README.md) |
| **system-starter**    | System management app       | 8080  | - |
| **monitor-starter**   | Monitoring & operations app | 8081  | - |
| **tenanted-starter**  | Multi-tenant app            | 8082  | - |
| **scheduler-starter** | Scheduler app               | 8083  | - |
| **video-starter**     | Video platform app          | 18172 | [view](./spring-api-support-video-starter/README.md) |

## Dependencies

```
Application Layer
    ↓
Common Layer
    ↓
Foundation Layer

Special dependencies:
- common-oauth-starter → oauth-client-starter (OAuth client SDK)
- common-service-starter → oauth-client-starter (using @UserValue annotation)
- oauth-starter → common-oauth-starter (implements auth interfaces)
- system-starter → common-oauth-starter (uses login service)
```

## Quick Start

### 1. Requirements

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 2. Feature Toggles

Each application module has independent feature toggle configurations, located in `src/main/resources/config/application-plugin.yml`:

**Monitor service:**

```yaml
plugin:
  monitor:
    server:
      enable: true # enabled by default
```

**OAuth service:**

```yaml
plugin:
  oauth:
    server:
      enable: true # enabled by default
```

**System service:**

```yaml
plugin:
  system:
    server:
      enable: true # enabled by default
```

> 💡 **Note:** All application services are enabled by default. To disable one, set the corresponding `enable` to `false`.

### 3. Database Initialization

```sql
-- Create databases
CREATE DATABASE oauth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Run initialization scripts
-- OAuth DB scripts: spring-api-support-oauth-starter/src/main/resources/db/init/
-- System DB scripts: execute according to actual requirements
```

### 4. Config

Copy configuration template:

```bash
cp application-example.yml application.yml
```

Modify configuration:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oauth
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379
```

### 5. Start Services

#### Method One: From IDE

- OAuth server: Run `com.chua.starter.oauth.sso.support.AuthApplication`
- System app: Run the corresponding Application class

#### Method Two: From command line

```bash
# Start OAuth server
cd spring-api-support-oauth-starter
mvn spring-boot:run

# Start system app
cd spring-api-support-system-starter
mvn spring-boot:run
```

## Usage Guide

### Creating a New Application Module

1. **Add dependencies:**

```xml
<dependencies>
    <!-- OAuth authentication -->
    <dependency>
        <groupId>com.chua</groupId>
        <artifactId>spring-api-support-common-oauth-starter</artifactId>
    </dependency>

    <!-- System base functionality -->
    <dependency>
        <groupId>com.chua</groupId>
        <artifactId>spring-api-support-common-system-starter</artifactId>
    </dependency>

    <!-- General services -->
    <dependency>
        <groupId>com.chua</groupId>
        <artifactId>spring-api-support-common-service-starter</artifactId>
    </dependency>
</dependencies>
```

2. **Configure OAuth client:**

```yaml
spring:
  oauth:
    client:
      server-url: http://localhost:19180
      client-id: your-client-id
      client-secret: your-client-secret
      token-name: x-oauth-token
```

3. **Use login functionality:**

All applications automatically have the unified login interface:

```
POST /v2/user/login          # User login
POST /v2/user/refresh-token  # Refresh Token
DELETE /v2/user/logout       # User logout
GET /v2/user/me              # Get current user info
```

## Architecture Design

### Design Principles

1. **Single responsibility** - Each module is responsible for one domain
2. **Clear dependencies** - No circular dependencies, top-down
3. **Unified naming** - `common` prefix means others depend on this
4. **Unified version** - Managed via `${project.version}`

### Common Module Design

**Common modules** are the base modules depended on by non-Common modules:

- ✅ Provide reusable services and functions
- ✅ Do not contain application launch classes
- ✅ Can be depended on by multiple application modules
- ✅ Single responsibility, easy to maintain

### Project Management Design

**project-starter** uses unified project management mode:

Each project type (AI, email, SMS, device) includes:

- **Configuration** - AK/SK, API keys
- **Template management** - Prompt templates, email templates, SMS templates
- **Logging** - Task logs, send logs, event logs
- **Permission control** - Project-level permission management

Advantages:

- ✅ Unified management mode
- ✅ Easy to extend to new project types
- ✅ Easy permission control and audit

## Development Norms

### Version Management

All internal modules use unified version management:

```xml
<!-- In parent POM -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.chua</groupId>
            <artifactId>spring-api-support-common-oauth-starter</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- In child modules (no version specified) -->
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-oauth-starter</artifactId>
</dependency>
```

### Code Norms

- Follow Alibaba Java coding guidelines
- Use Lombok to simplify code
- Write JavaDoc for all public APIs
- Add comments to important logic

### Commit Norms

```
feat: New feature
fix: Fix bug
refactor: Refactor
docs: Update documentation
style: Style adjustment
test: Testing
chore: Build/toolchain
```

## Related Documents

- [OAuth Authentication Module](./spring-api-support-common-oauth-starter/README.md)
- [General Service Module](./spring-api-support-common-service-starter/README.md)
- [System Service Module](./spring-api-support-common-system-starter/README.md)
- [Project Management Module](./spring-api-support-common-project-starter/README.md)
- [OAuth Server Deployment](./spring-api-support-oauth-starter/README.md)

## License

[Fill in according to actual conditions]

## Contact

- Author: CH
- Email: [Fill in according to actual conditions]
