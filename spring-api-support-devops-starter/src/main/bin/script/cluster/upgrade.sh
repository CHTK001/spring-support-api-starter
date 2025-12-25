#!/bin/bash
# ============================================================
# 集群滚动升级脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}集群滚动升级${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh upgrade <升级包> [选项]"
    echo ""
    echo -e "${YELLOW}说明:${NC}"
    echo "  对已运行的应用进行滚动升级，保证集群可用性"
    echo "  与 deploy 的区别: upgrade 会停止/替换/启动，deploy 仅上传文件"
    echo ""
    echo -e "${YELLOW}执行流程 (每个节点):${NC}"
    echo "  1. 备份当前版本 (JAR + 配置)"
    echo "  2. 停止实例"
    echo "  3. 解压/替换文件 (JAR/libs/bin/config)"
    echo "  4. 启动实例"
    echo "  5. 健康检查 (/actuator/health)"
    echo "  6. 等待间隔 → 下一个节点"
    echo "  ※ 任一节点失败则停止整个滚动升级"
    echo ""
    echo -e "${YELLOW}升级包格式:${NC}"
    echo "  *.tar.gz           完整升级包 (推荐，包含JAR+配置+依赖)"
    echo "  *.jar              仅升级JAR文件"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --local            仅升级本地实例"
    echo "  --remote           仅升级远程节点"
    echo "  --node <name>      升级指定节点"
    echo "  --exclude <node>   排除指定节点"
    echo "  --parallel         并行升级 (不推荐用于生产)"
    echo "  --no-backup        不执行备份"
    echo "  --no-health-check  跳过健康检查"
    echo "  --keep-config      保留原有配置文件 (默认保留)"
    echo "  --overwrite-config 覆盖配置文件"
    echo "  -y, --yes          跳过确认"
    echo "  -h, --help         显示帮助"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  cluster.sh upgrade app-2.0.tar.gz           完整升级 (推荐)"
    echo "  cluster.sh upgrade app-2.0.tar.gz --local   仅升级本地实例"
    echo "  cluster.sh upgrade app-2.0.jar              仅升级JAR文件"
    echo "  cluster.sh upgrade app-2.0.tar.gz --overwrite-config  覆盖配置"
    echo ""
    echo -e "${YELLOW}tar.gz包结构:${NC}"
    echo "  app-2.0/"
    echo "  ├── app.jar              # 主程序"
    echo "  ├── config/              # 配置文件"
    echo "  ├── libs/                # 依赖库"
    echo "  └── bin/                 # 脚本"
    echo ""
}

# ===========================================
# 解析远程节点
# ===========================================
parse_remote_node() {
    local node_str="$1"
    local field="$2"
    
    IFS='|' read -ra parts <<< "$node_str"
    
    case "$field" in
        alias)    echo "${parts[0]}" ;;
        host)     echo "${parts[1]}" ;;
        port)     echo "${parts[2]:-8080}" ;;
        user)     echo "${parts[3]:-${DEFAULT_SSH_USER}}" ;;
        ssh_port) echo "${parts[4]:-${DEFAULT_SSH_PORT}}" ;;
        ssh_key)  echo "${parts[5]:-${DEFAULT_SSH_KEY}}" ;;
    esac
}

# ===========================================
# 健康检查
# ===========================================
wait_for_health() {
    local host="$1"
    local port="$2"
    local retries="${HEALTH_CHECK_RETRIES:-30}"
    local interval="${HEALTH_CHECK_INTERVAL:-2}"
    local path="${HEALTH_CHECK_PATH:-/actuator/health}"
    local protocol="${HEALTH_CHECK_PROTOCOL:-http}"
    
    local url="${protocol}://${host}:${port}${path}"
    
    echo -e "  ${CYAN}→${NC} 健康检查: ${url}"
    
    local count=0
    while [ $count -lt $retries ]; do
        if curl -sf --connect-timeout 2 "$url" > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} 健康检查通过"
            return 0
        fi
        sleep $interval
        count=$((count + 1))
        echo -ne "\r  ${YELLOW}等待健康检查... ${count}/${retries}${NC}"
    done
    
    echo ""
    echo -e "  ${RED}✗${NC} 健康检查超时"
    return 1
}

# ===========================================
# 升级本地实例
# ===========================================
upgrade_local_instance() {
    local port="$1"
    local pkg_file="$2"
    local do_backup="$3"
    local do_health_check="$4"
    local keep_config="$5"
    
    local instance_name="${APP_NAME}-${port}"
    local pid_file="${APP_HOME}/${instance_name}.pid"
    
    echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${WHITE}升级本地实例: ${instance_name}${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
    
    # 备份
    if [ "$do_backup" = "true" ]; then
        echo -e "  ${CYAN}→${NC} 备份当前版本..."
        bash "${SCRIPT_DIR}/../linux/backup.sh" 2>/dev/null || true
    fi
    
    # 停止
    echo -e "  ${CYAN}→${NC} 停止实例..."
    bash "${SCRIPT_DIR}/stop.sh" --node "$port" > /dev/null
    sleep 2
    
    # 根据包类型升级
    if [[ "$pkg_file" == *.tar.gz ]] || [[ "$pkg_file" == *.tgz ]]; then
        # tar.gz 完整升级包
        echo -e "  ${CYAN}→${NC} 解压升级包..."
        local tmp_dir=$(mktemp -d)
        tar -xzf "$pkg_file" -C "$tmp_dir"
        
        # 查找解压后的目录
        local extract_dir=$(ls -d "$tmp_dir"/*/ 2>/dev/null | head -1)
        [ -z "$extract_dir" ] && extract_dir="$tmp_dir"
        
        # 保留原有配置
        if [ "$keep_config" = "true" ] && [ -d "${APP_HOME}/config" ]; then
            echo -e "  ${CYAN}→${NC} 保留原有配置..."
            cp -r "${APP_HOME}/config" "$tmp_dir/config_backup"
        fi
        
        # 替换文件
        echo -e "  ${CYAN}→${NC} 替换应用文件..."
        # 复制JAR文件
        find "$extract_dir" -name "*.jar" -type f | while read jar; do
            cp "$jar" "${APP_HOME}/"
        done
        # 复制libs目录
        [ -d "$extract_dir/libs" ] && cp -r "$extract_dir/libs" "${APP_HOME}/"
        [ -d "$extract_dir/lib" ] && cp -r "$extract_dir/lib" "${APP_HOME}/"
        # 复制bin目录
        [ -d "$extract_dir/bin" ] && cp -r "$extract_dir/bin" "${APP_HOME}/"
        # 复制配置目录
        if [ "$keep_config" != "true" ]; then
            [ -d "$extract_dir/config" ] && cp -r "$extract_dir/config" "${APP_HOME}/"
        else
            # 恢复原有配置
            [ -d "$tmp_dir/config_backup" ] && cp -r "$tmp_dir/config_backup" "${APP_HOME}/config"
        fi
        
        rm -rf "$tmp_dir"
    else
        # 仅JAR文件升级
        echo -e "  ${CYAN}→${NC} 替换 JAR 文件..."
        cp "$pkg_file" "${APP_HOME}/"
    fi
    
    # 启动
    echo -e "  ${CYAN}→${NC} 启动实例..."
    bash "${SCRIPT_DIR}/start.sh" --node "$port" > /dev/null
    sleep 3
    
    # 健康检查
    if [ "$do_health_check" = "true" ]; then
        if ! wait_for_health "localhost" "$port"; then
            log_error "实例 ${instance_name} 健康检查失败"
            return 1
        fi
    fi
    
    echo -e "  ${GREEN}✓${NC} ${instance_name} 升级完成"
    echo ""
    return 0
}

# ===========================================
# 升级远程节点
# ===========================================
upgrade_remote_node() {
    local node_str="$1"
    local pkg_file="$2"
    local do_backup="$3"
    local do_health_check="$4"
    local keep_config="$5"
    
    local alias=$(parse_remote_node "$node_str" "alias")
    local host=$(parse_remote_node "$node_str" "host")
    local port=$(parse_remote_node "$node_str" "port")
    local user=$(parse_remote_node "$node_str" "user")
    local ssh_port=$(parse_remote_node "$node_str" "ssh_port")
    local ssh_key=$(parse_remote_node "$node_str" "ssh_key")
    
    echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${WHITE}升级远程节点: ${alias} (${host})${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
    
    # 构建 SSH/SCP 选项
    local ssh_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=${SSH_TIMEOUT:-10}"
    local scp_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=${SSH_TIMEOUT:-10}"
    [ -n "$ssh_key" ] && [ -f "$ssh_key" ] && ssh_opts="$ssh_opts -i $ssh_key" && scp_opts="$scp_opts -i $ssh_key"
    [ -n "$ssh_port" ] && ssh_opts="$ssh_opts -p $ssh_port" && scp_opts="$scp_opts -P $ssh_port"
    
    # 替换变量
    local remote_home=$(echo "$REMOTE_APP_HOME" | sed "s/\${APP_NAME}/${APP_NAME}/g")
    local remote_script=$(echo "$REMOTE_STARTUP_SCRIPT" | sed "s/\${APP_NAME}/${APP_NAME}/g" | sed "s|\${REMOTE_APP_HOME}|${remote_home}|g")
    local pkg_name=$(basename "$pkg_file")
    
    # 上传升级包
    echo -e "  ${CYAN}→${NC} 上传升级包..."
    if ! scp $scp_opts "$pkg_file" "${user}@${host}:/tmp/${pkg_name}"; then
        log_error "上传失败"
        return 1
    fi
    
    # 备份
    if [ "$do_backup" = "true" ]; then
        echo -e "  ${CYAN}→${NC} 备份当前版本..."
        ssh $ssh_opts "${user}@${host}" "${remote_script} backup" 2>/dev/null || true
    fi
    
    # 停止
    echo -e "  ${CYAN}→${NC} 停止服务..."
    ssh $ssh_opts "${user}@${host}" "${remote_script} stop"
    sleep 2
    
    # 根据包类型升级
    if [[ "$pkg_file" == *.tar.gz ]] || [[ "$pkg_file" == *.tgz ]]; then
        # tar.gz 完整升级包
        echo -e "  ${CYAN}→${NC} 解压并替换文件..."
        local keep_flag=""
        [ "$keep_config" = "true" ] && keep_flag="--keep-config"
        ssh $ssh_opts "${user}@${host}" "cd /tmp && tar -xzf ${pkg_name} && \
            extract_dir=\$(ls -d /tmp/*/  2>/dev/null | grep -v lost | head -1) && \
            [ -n \"\$extract_dir\" ] || extract_dir=/tmp && \
            if [ '$keep_config' = 'true' ] && [ -d '${remote_home}/config' ]; then \
                cp -r '${remote_home}/config' /tmp/config_backup; \
            fi && \
            find \$extract_dir -name '*.jar' -type f -exec cp {} '${remote_home}/' \\; && \
            [ -d \"\$extract_dir/libs\" ] && cp -r \"\$extract_dir/libs\" '${remote_home}/' || true && \
            [ -d \"\$extract_dir/lib\" ] && cp -r \"\$extract_dir/lib\" '${remote_home}/' || true && \
            [ -d \"\$extract_dir/bin\" ] && cp -r \"\$extract_dir/bin\" '${remote_home}/' || true && \
            if [ '$keep_config' != 'true' ]; then \
                [ -d \"\$extract_dir/config\" ] && cp -r \"\$extract_dir/config\" '${remote_home}/' || true; \
            else \
                [ -d /tmp/config_backup ] && cp -r /tmp/config_backup '${remote_home}/config' || true; \
            fi && \
            rm -rf /tmp/${pkg_name} \$extract_dir /tmp/config_backup"
    else
        # 仅JAR文件升级
        echo -e "  ${CYAN}→${NC} 替换 JAR 文件..."
        ssh $ssh_opts "${user}@${host}" "mv /tmp/${pkg_name} ${remote_home}/"
    fi
    
    # 启动
    echo -e "  ${CYAN}→${NC} 启动服务..."
    ssh $ssh_opts "${user}@${host}" "${remote_script} start"
    sleep 3
    
    # 健康检查
    if [ "$do_health_check" = "true" ]; then
        if ! wait_for_health "$host" "$port"; then
            log_error "节点 ${alias} 健康检查失败"
            return 1
        fi
    fi
    
    echo -e "  ${GREEN}✓${NC} ${alias} 升级完成"
    echo ""
    return 0
}

# ===========================================
# 检查是否在排除列表中
# ===========================================
is_excluded() {
    local node="$1"
    for excluded in "${EXCLUDE_NODES[@]}"; do
        [ "$node" = "$excluded" ] && return 0
    done
    return 1
}

# ===========================================
# 参数解析
# ===========================================
PKG_FILE=""
TARGET="all"
TARGET_NODE=""
PARALLEL="false"
DO_BACKUP="${AUTO_BACKUP_BEFORE_UPGRADE:-true}"
DO_HEALTH_CHECK="true"
KEEP_CONFIG="true"
SKIP_CONFIRM="false"
EXCLUDE_NODES=()

while [ $# -gt 0 ]; do
    case "$1" in
        --local)
            TARGET="local"
            shift
            ;;
        --remote)
            TARGET="remote"
            shift
            ;;
        --node)
            TARGET="node"
            TARGET_NODE="$2"
            shift 2
            ;;
        --exclude)
            EXCLUDE_NODES+=("$2")
            shift 2
            ;;
        --parallel)
            PARALLEL="true"
            shift
            ;;
        --no-backup)
            DO_BACKUP="false"
            shift
            ;;
        --no-health-check)
            DO_HEALTH_CHECK="false"
            shift
            ;;
        --keep-config)
            KEEP_CONFIG="true"
            shift
            ;;
        --overwrite-config)
            KEEP_CONFIG="false"
            shift
            ;;
        -y|--yes)
            SKIP_CONFIRM="true"
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            if [ -z "$PKG_FILE" ] && [ -f "$1" ]; then
                PKG_FILE="$1"
                shift
            else
                log_error "未知参数或文件不存在: $1"
                show_help
                exit 1
            fi
            ;;
    esac
done

# ===========================================
# 验证
# ===========================================
if [ -z "$PKG_FILE" ]; then
    log_error "请指定升级包 (*.tar.gz 或 *.jar)"
    show_help
    exit 1
fi

if [ ! -f "$PKG_FILE" ]; then
    log_error "升级包不存在: $PKG_FILE"
    exit 1
fi

# 检查文件格式
PKG_TYPE="jar"
if [[ "$PKG_FILE" == *.tar.gz ]] || [[ "$PKG_FILE" == *.tgz ]]; then
    PKG_TYPE="tarball"
fi

# ===========================================
# 确认
# ===========================================
echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}${YELLOW}                     集群滚动升级                             ${NC}${BLUE}║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}┌─────────────────────────────────────────────────────────────┐${NC}"
echo -e "${CYAN}│${NC} ${WHITE}升级包${NC}      │ ${GREEN}$(basename "$PKG_FILE")${NC}"
echo -e "${CYAN}│${NC} ${WHITE}包类型${NC}      │ ${YELLOW}${PKG_TYPE}${NC}"
echo -e "${CYAN}│${NC} ${WHITE}升级目标${NC}    │ ${YELLOW}${TARGET}${NC}"
echo -e "${CYAN}│${NC} ${WHITE}自动备份${NC}    │ ${MAGENTA}${DO_BACKUP}${NC}"
echo -e "${CYAN}│${NC} ${WHITE}健康检查${NC}    │ ${MAGENTA}${DO_HEALTH_CHECK}${NC}"
echo -e "${CYAN}│${NC} ${WHITE}保留配置${NC}    │ ${MAGENTA}${KEEP_CONFIG}${NC}"
echo -e "${CYAN}│${NC} ${WHITE}升级模式${NC}    │ ${MAGENTA}$([ "$PARALLEL" = "true" ] && echo "并行" || echo "滚动")${NC}"
echo -e "${CYAN}└─────────────────────────────────────────────────────────────┘${NC}"
echo ""

if [ "$SKIP_CONFIRM" != "true" ]; then
    read -p "确认开始升级? [y/N] " confirm
    if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
        log_info "升级已取消"
        exit 0
    fi
fi

echo ""

# ===========================================
# 执行升级
# ===========================================
success_count=0
fail_count=0
interval="${ROLLING_UPGRADE_INTERVAL:-10}"

# 升级本地实例
if [ "$TARGET" = "all" ] || [ "$TARGET" = "local" ]; then
    if [ -n "$LOCAL_PORTS" ]; then
        IFS=',' read -ra ports <<< "$LOCAL_PORTS"
        for port in "${ports[@]}"; do
            port=$(echo "$port" | tr -d ' ')
            if [[ "$port" =~ ^[0-9]+$ ]]; then
                if is_excluded "$port"; then
                    echo -e "  ${YELLOW}⊚${NC} ${APP_NAME}-${port} 已排除"
                    continue
                fi
                if upgrade_local_instance "$port" "$PKG_FILE" "$DO_BACKUP" "$DO_HEALTH_CHECK" "$KEEP_CONFIG"; then
                    success_count=$((success_count + 1))
                else
                    fail_count=$((fail_count + 1))
                    if [ "$PARALLEL" != "true" ]; then
                        log_error "升级失败，停止滚动升级"
                        break
                    fi
                fi
                
                # 滚动间隔
                if [ "$PARALLEL" != "true" ] && [ $((success_count + fail_count)) -lt ${#ports[@]} ]; then
                    echo -e "${YELLOW}等待 ${interval} 秒后继续下一个实例...${NC}"
                    sleep $interval
                fi
            fi
        done
    fi
fi

# 升级远程节点
if [ "$TARGET" = "all" ] || [ "$TARGET" = "remote" ]; then
    if [ ${#REMOTE_NODES[@]} -gt 0 ]; then
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            
            local node_alias=$(parse_remote_node "$node" "alias")
            if is_excluded "$node_alias"; then
                echo -e "  ${YELLOW}⊚${NC} ${node_alias} 已排除"
                continue
            fi
            
            if upgrade_remote_node "$node" "$PKG_FILE" "$DO_BACKUP" "$DO_HEALTH_CHECK" "$KEEP_CONFIG"; then
                success_count=$((success_count + 1))
            else
                fail_count=$((fail_count + 1))
                if [ "$PARALLEL" != "true" ]; then
                    log_error "升级失败，停止滚动升级"
                    break
                fi
            fi
            
            # 滚动间隔
            if [ "$PARALLEL" != "true" ]; then
                echo -e "${YELLOW}等待 ${interval} 秒后继续下一个节点...${NC}"
                sleep $interval
            fi
        done
    fi
fi

# 升级指定节点
if [ "$TARGET" = "node" ] && [ -n "$TARGET_NODE" ]; then
    if [[ "$TARGET_NODE" =~ ^[0-9]+$ ]]; then
        if upgrade_local_instance "$TARGET_NODE" "$PKG_FILE" "$DO_BACKUP" "$DO_HEALTH_CHECK" "$KEEP_CONFIG"; then
            success_count=1
        else
            fail_count=1
        fi
    else
        found="false"
        for node in "${REMOTE_NODES[@]}"; do
            [ -z "$node" ] && continue
            alias=$(parse_remote_node "$node" "alias")
            if [ "$alias" = "$TARGET_NODE" ]; then
                if upgrade_remote_node "$node" "$PKG_FILE" "$DO_BACKUP" "$DO_HEALTH_CHECK" "$KEEP_CONFIG"; then
                    success_count=1
                else
                    fail_count=1
                fi
                found="true"
                break
            fi
        done
        if [ "$found" = "false" ]; then
            log_error "未找到节点: $TARGET_NODE"
            fail_count=1
        fi
    fi
fi

# ===========================================
# 结果统计
# ===========================================
echo ""
echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo -e "${WHITE}升级完成${NC}"
echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo -e "  ${GREEN}成功:${NC} ${success_count}  ${RED}失败:${NC} ${fail_count}"
echo ""

[ $fail_count -gt 0 ] && exit 1
exit 0
