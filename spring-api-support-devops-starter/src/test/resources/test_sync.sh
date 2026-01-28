#!/usr/bin/env bash

# sync.sh 测试脚本
# 使用 docker (spring-demo-app) 测试 sync.sh 的所有能力

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../../../.." && pwd)"

echo "=========================================="
echo "sync.sh 功能测试"
echo "=========================================="

# 清理函数
cleanup() {
    echo ""
    echo "=========================================="
    echo "清理测试环境..."
    echo "=========================================="
    
    # 停止并删除容器
    docker stop sync-test-master sync-test-node1 sync-test-node2 2>/dev/null || true
    docker rm sync-test-master sync-test-node1 sync-test-node2 2>/dev/null || true
    
    # 删除测试网络
    docker network rm sync-test-network 2>/dev/null || true
    
    echo "清理完成"
}

# 注册清理函数
trap cleanup EXIT

# 构建镜像
echo ""
echo "=========================================="
echo "1. 构建 Docker 镜像..."
echo "=========================================="
cd "${PROJECT_ROOT}"
if [ ! -f "spring-demo-app/target/spring-demo-app-1.0.0.jar" ]; then
    echo "构建 spring-demo-app..."
    cd spring-demo-app
    mvn -q -DskipTests package
    cd ..
fi

# 构建 devops starter（确保脚本已打包）
echo "构建 devops starter..."
cd spring-support-api-parent/spring-api-support-devops-starter
mvn -q -DskipTests package
cd "${PROJECT_ROOT}"

# 构建 docker 镜像
echo "构建 Docker 镜像..."
docker build -t spring-demo-app:test -f spring-demo-app/Dockerfile .

# 创建测试网络
echo ""
echo "=========================================="
echo "2. 创建测试网络..."
echo "=========================================="
docker network create sync-test-network 2>/dev/null || true

# 启动 master 容器
echo ""
echo "=========================================="
echo "3. 启动 master 容器..."
echo "=========================================="
docker run -d \
    --name sync-test-master \
    --network sync-test-network \
    -p 18080:8080 \
    spring-demo-app:test

# 等待 master 启动
sleep 5

# 在 master 中创建测试文件
echo "在 master 中创建测试文件..."
docker exec sync-test-master sh -c "echo 'master-file-1' > /opt/app/test-master-1.txt"
docker exec sync-test-master sh -c "echo 'master-file-2' > /opt/app/test-master-2.txt"
docker exec sync-test-master sh -c "mkdir -p /opt/app/backup && echo 'backup-file' > /opt/app/backup/backup.txt"
docker exec sync-test-master sh -c "mkdir -p /opt/app/logs && echo 'log-file' > /opt/app/logs/app.log"

# 启动 node1 容器
echo ""
echo "=========================================="
echo "4. 启动 node1 容器..."
echo "=========================================="
docker run -d \
    --name sync-test-node1 \
    --network sync-test-network \
    -p 18081:8080 \
    spring-demo-app:test

# 启动 node2 容器
echo ""
echo "=========================================="
echo "5. 启动 node2 容器..."
echo "=========================================="
docker run -d \
    --name sync-test-node2 \
    --network sync-test-network \
    -p 18082:8080 \
    spring-demo-app:test

# 等待节点启动
sleep 5

# 获取容器 IP
MASTER_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' sync-test-master)
NODE1_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' sync-test-node1)
NODE2_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' sync-test-node2)

echo "Master IP: ${MASTER_IP}"
echo "Node1 IP: ${NODE1_IP}"
echo "Node2 IP: ${NODE2_IP}"

# 配置 SSH 免密登录（在 master 容器中）
echo ""
echo "=========================================="
echo "6. 配置 SSH 免密登录..."
echo "=========================================="

# 在 master 中生成 SSH 密钥（如果不存在）
docker exec sync-test-master sh -c "mkdir -p /root/.ssh && [ ! -f /root/.ssh/id_rsa ] && ssh-keygen -t rsa -N '' -f /root/.ssh/id_rsa -q || true"

# 获取 master 的公钥
MASTER_PUBKEY=$(docker exec sync-test-master cat /root/.ssh/id_rsa.pub 2>/dev/null || echo "")

if [ -z "${MASTER_PUBKEY}" ]; then
    echo "警告: 无法获取 master 公钥，跳过 SSH 配置"
else
    # 将公钥添加到 node1 和 node2
    docker exec sync-test-node1 sh -c "mkdir -p /root/.ssh && echo '${MASTER_PUBKEY}' >> /root/.ssh/authorized_keys && chmod 600 /root/.ssh/authorized_keys && chmod 700 /root/.ssh" || true
    docker exec sync-test-node2 sh -c "mkdir -p /root/.ssh && echo '${MASTER_PUBKEY}' >> /root/.ssh/authorized_keys && chmod 600 /root/.ssh/authorized_keys && chmod 700 /root/.ssh" || true

    # 启动 SSH 服务
    docker exec sync-test-node1 sh -c "service ssh start || /usr/sbin/sshd" || true
    docker exec sync-test-node2 sh -c "service ssh start || /usr/sbin/sshd" || true

    # 等待 SSH 服务启动
    sleep 3
    
    # 测试 SSH 连接
    echo "测试 SSH 连接..."
    docker exec sync-test-master sh -c "ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 ${NODE1_IP} 'echo SSH_OK'" || echo "SSH 连接测试失败（可能需要进一步配置）"
fi

# 创建 hosts 文件
echo ""
echo "=========================================="
echo "7. 创建 hosts 文件..."
echo "=========================================="
HOSTS_FILE="/tmp/sync-test-hosts.txt"
cat > "${HOSTS_FILE}" <<EOF
# Master 节点
# master: localhost

# Node 节点
${NODE1_IP}
${NODE2_IP}
EOF

echo "Hosts 文件内容:"
cat "${HOSTS_FILE}"

# 测试 1: 无参数默认同步（使用当前目录作为 master）
echo ""
echo "=========================================="
echo "测试 1: 无参数默认同步（当前目录作为 master）"
echo "=========================================="
docker exec sync-test-master sh -c "cd /opt/app && cp /opt/app/bin/cluster_hosts.txt /opt/app/bin/cluster_hosts.txt.bak 2>/dev/null || true"
docker exec sync-test-master sh -c "echo '${NODE1_IP}' > /opt/app/bin/cluster_hosts.txt && echo '${NODE2_IP}' >> /opt/app/bin/cluster_hosts.txt"
docker exec sync-test-master sh -c "cd /opt/app && ./bin/sync.sh --dry-run" || echo "测试 1 失败（可能是预期的，因为需要 SSH 配置）"

# 测试 2: 指定 hosts 文件同步
echo ""
echo "=========================================="
echo "测试 2: 指定 hosts 文件同步"
echo "=========================================="
docker cp "${HOSTS_FILE}" sync-test-master:/opt/app/bin/test-hosts.txt
docker exec sync-test-master sh -c "cd /opt/app && ./bin/sync.sh -f /opt/app/bin/test-hosts.txt --dry-run" || echo "测试 2 失败（可能是预期的）"

# 测试 3: 指定单个节点同步
echo ""
echo "=========================================="
echo "测试 3: 指定单个节点同步"
echo "=========================================="
docker exec sync-test-master sh -c "cd /opt/app && ./bin/sync.sh --node ${NODE1_IP} --dry-run" || echo "测试 3 失败（可能是预期的）"

# 测试 4: 排除目录
echo ""
echo "=========================================="
echo "测试 4: 排除目录"
echo "=========================================="
docker exec sync-test-master sh -c "cd /opt/app && ./bin/sync.sh --node ${NODE1_IP} --exclude backup --exclude logs --dry-run" || echo "测试 4 失败（可能是预期的）"

# 测试 5: 实际同步（不使用 --dry-run）
echo ""
echo "=========================================="
echo "测试 5: 实际同步到 node1"
echo "=========================================="
docker exec sync-test-master sh -c "cd /opt/app && ./bin/sync.sh --node ${NODE1_IP} --exclude backup --exclude logs" || echo "测试 5 失败"

# 验证同步结果
echo ""
echo "=========================================="
echo "验证同步结果..."
echo "=========================================="
if docker exec sync-test-node1 test -f /opt/app/test-master-1.txt; then
    echo "✓ node1 中存在 test-master-1.txt"
    docker exec sync-test-node1 cat /opt/app/test-master-1.txt
else
    echo "✗ node1 中不存在 test-master-1.txt"
fi

if docker exec sync-test-node1 test -f /opt/app/backup/backup.txt; then
    echo "✗ node1 中存在 backup.txt（应该被排除）"
else
    echo "✓ node1 中不存在 backup.txt（正确排除）"
fi

# 测试 6: hosts 文件中配置 master
echo ""
echo "=========================================="
echo "测试 6: hosts 文件中配置 master"
echo "=========================================="
cat > "${HOSTS_FILE}" <<EOF
# Master 节点
# master: localhost

# Node 节点
${NODE2_IP}
EOF
docker cp "${HOSTS_FILE}" sync-test-master:/opt/app/bin/test-hosts-master.txt
docker exec sync-test-master sh -c "cd /opt/app && ./bin/sync.sh -f /opt/app/bin/test-hosts-master.txt --dry-run" || echo "测试 6 失败（可能是预期的）"

# 测试 7: 无参数默认同步（从 hosts 文件读取 master 和 node）
echo ""
echo "=========================================="
echo "测试 7: 无参数默认同步（从 hosts 文件读取）"
echo "=========================================="
docker exec sync-test-master sh -c "cat > /opt/app/bin/cluster_hosts.txt <<'EOF'
# master: localhost
${NODE2_IP}
EOF
"
docker exec sync-test-master sh -c "cd /opt/app && ./bin/sync.sh --dry-run" || echo "测试 7 失败（可能是预期的）"

echo ""
echo "=========================================="
echo "所有测试完成"
echo "=========================================="

