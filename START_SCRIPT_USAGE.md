# start.sh 启动脚本使用说明

## 功能说明

start.sh 脚本用于启动 Spring Boot 应用，支持多环境配置和 JVM 参数调优。

## 环境参数

### 环境标识（必选其一）

- `--dev`：开发环境（默认）
- `--prod`：生产环境
- `--test`：测试环境

### JVM 参数（可选）

- `-port <端口号>`：指定应用端口
- `-xmx <内存大小>`：设置最大堆内存（如：2g, 512m）
- `-xms <内存大小>`：设置初始堆内存（如：1g, 256m）
- `-d`：启用 HeapDumpOnOutOfMemoryError
- `-dp <路径>`：指定 HeapDump 文件路径（自动启用 -d）

## 使用示例

### 1. 启动开发环境（默认）

```bash
./start.sh
```

### 2. 启动生产环境

```bash
./start.sh --prod
```

### 3. 启动测试环境

```bash
./start.sh --test
```

### 4. 指定端口启动

```bash
./start.sh --prod -port 8080
```

### 5. 配置 JVM 内存参数

```bash
./start.sh --prod -xmx 2g -xms 1g
```

### 6. 启用内存溢出转储

```bash
./start.sh --prod -d
```

### 7. 指定内存溢出转储路径

```bash
./start.sh --prod -dp /app/dumps/
```

### 8. 组合使用多个参数

```bash
./start.sh --prod -port 8080 -xmx 4g -xms 2g -dp /app/dumps/
```

## 配置说明

在使用脚本前，需要修改脚本中的以下配置：

```bash
path='/app/'                    # JAR 包所在路径
name='spring-api-support-monitor-starter'  # 应用名称
version='1.0.0'                 # 应用版本
```

## 注意事项

1. 脚本会自动查找并杀死已运行的同名进程
2. 应用日志输出到 `start.log` 文件
3. 脚本执行后会自动 tail -f 显示日志
4. 确保脚本有执行权限：`chmod +x start.sh`

## Spring Profile 对应关系

| 参数   | Spring Profile | 说明                           |
| ------ | -------------- | ------------------------------ |
| --dev  | dev            | 开发环境，详细日志，本地数据库 |
| --test | test           | 测试环境，模拟外部服务         |
| --prod | prod           | 生产环境，最小日志，外部配置   |
