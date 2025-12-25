#!/bin/bash
# ============================================================
# 集群远程部署脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}集群远程部署${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} cluster.sh deploy <jar_file|tar.gz> [选项]"
    echo ""
    echo -e "${YELLOW}说明:${NC}"
    echo "  首次部署应用到远程服务器，上传文件但不启动服务"
    echo "  与 upgrade 的区别: deploy 仅上传文件，upgrade 会停止/替换/启动"
    echo ""
    echo -e "${YELLOW}执行流程:${NC}"
    echo "  1. 创建远程目录 (bin/config/logs/backup/lib)"
    echo "  2. [首次部署] 上传启动脚本 (startup.sh 等)"
    echo "  3. 上传 JAR/tar.gz 文件"
    echo "  4. 上传配置文件 (config/ 目录)"
    echo "  5. 完成 (需手动启动: ssh user@host 'startup.sh start')"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  --node <name>      部署到指定节点"
    echo "  --init             首次部署 (同时上传脚本)"
    echo "  --scripts-only     仅上传脚本 (不上传 JAR)"
    echo "  -y, --yes          跳过确认"
    echo "  -h, --help         显示帮助"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  cluster.sh deploy app-1.0.jar --init       首次部署 (含脚本)"
    echo "  cluster.sh deploy app-1.0.jar              更新JAR文件"
    echo "  cluster.sh deploy app-1.0.jar --node node1 部署到指定节点"
    echo "  cluster.sh deploy --scripts-only           仅更新脚本"
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
# 部署到远程节点
# ===========================================
deploy_to_node() {
    local node_str="$1"
    local jar_file="$2"
    local init_mode="$3"
    local scripts_only="$4"
    
    local alias=$(parse_remote_node "$node_str" "alias")
    local host=$(parse_remote_node "$node_str" "host")
    local port=$(parse_remote_node "$node_str" "port")
    local user=$(parse_remote_node "$node_str" "user")
    local ssh_port=$(parse_remote_node "$node_str" "ssh_port")
    local ssh_key=$(parse_remote_node "$node_str" "ssh_key")
    
    echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${WHITE}部署到: ${alias} (${host})${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
    
    # 构建 SSH/SCP 选项
    local ssh_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=${SSH_TIMEOUT:-10}"
    local scp_opts="-o StrictHostKeyChecking=no -o ConnectTimeout=${SSH_TIMEOUT:-10}"
    [ -n "$ssh_key" ] && [ -f "$ssh_key" ] && ssh_opts="$ssh_opts -i $ssh_key" && scp_opts="$scp_opts -i $ssh_key"
    [ -n "$ssh_port" ] && ssh_opts="$ssh_opts -p $ssh_port" && scp_opts="$scp_opts -P $ssh_port"
    
    # 替换变量
    local remote_home=$(echo "$REMOTE_APP_HOME" | sed "s/\${APP_NAME}/${APP_NAME}/g")
    
    # 创建远程目录
    echo -e "  ${CYAN}→${NC} 创建远程目录..."
    ssh $ssh_opts "${user}@${host}" "mkdir -p ${remote_home}/{bin,config,logs,backup,lib}"
    
    # 上传脚本 (首次部署或仅脚本模式)
    if [ "$init_mode" = "true" ] || [ "$scripts_only" = "true" ]; then
        echo -e "  ${CYAN}→${NC} 上传启动脚本..."
        
        # 上传 bin 目录
        scp $scp_opts -r "${SCRIPT_DIR}/../../"*.sh "${user}@${host}:${remote_home}/bin/"
        scp $scp_opts -r "${SCRIPT_DIR}/../../common.sh" "${user}@${host}:${remote_home}/bin/"
        
        # 创建 script/linux 目录并上传
        ssh $ssh_opts "${user}@${host}" "mkdir -p ${remote_home}/bin/script/linux"
        scp $scp_opts -r "${SCRIPT_DIR}/../linux/"*.sh "${user}@${host}:${remote_home}/bin/script/linux/"
        
        # 设置执行权限
        ssh $ssh_opts "${user}@${host}" "chmod +x ${remote_home}/bin/*.sh ${remote_home}/bin/script/linux/*.sh"
        
        echo -e "  ${GREEN}✓${NC} 脚本上传完成"
    fi
    
    # 上传 JAR
    if [ "$scripts_only" != "true" ] && [ -n "$jar_file" ]; then
        local jar_name=$(basename "$jar_file")
        echo -e "  ${CYAN}→${NC} 上传 JAR 文件..."
        if scp $scp_opts "$jar_file" "${user}@${host}:${remote_home}/${jar_name}"; then
            echo -e "  ${GREEN}✓${NC} JAR 上传完成"
        else
            echo -e "  ${RED}✗${NC} JAR 上传失败"
            return 1
        fi
    fi
    
    # 上传配置文件 (如果存在)
    if [ -d "${CONFIG_DIR}" ] && [ "$(ls -A ${CONFIG_DIR})" ]; then
        echo -e "  ${CYAN}→${NC} 上传配置文件..."
        scp $scp_opts -r "${CONFIG_DIR}/"* "${user}@${host}:${remote_home}/config/" 2>/dev/null || true
    fi
    
    echo -e "  ${GREEN}✓${NC} ${alias} 部署完成"
    echo ""
    return 0
}

# ===========================================
# 参数解析
# ===========================================
JAR_FILE=""
TARGET_NODE=""
INIT_MODE="false"
SCRIPTS_ONLY="false"
SKIP_CONFIRM="false"

while [ $# -gt 0 ]; do
    case "$1" in
        --node)
            TARGET_NODE="$2"
            shift 2
            ;;
        --init)
            INIT_MODE="true"
            shift
            ;;
        --scripts-only)
            SCRIPTS_ONLY="true"
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
            if [ -z "$JAR_FILE" ] && [ -f "$1" ]; then
                JAR_FILE="$1"
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
if [ "$SCRIPTS_ONLY" != "true" ]; then
    if [ -z "$JAR_FILE" ]; then
        log_error "请指定 JAR 文件 (或使用 --scripts-only)"
        show_help
        exit 1
    fi
    
    if [ ! -f "$JAR_FILE" ]; then
        log_error "JAR 文件不存在: $JAR_FILE"
        exit 1
    fi
fi

if [ ${#REMOTE_NODES[@]} -eq 0 ]; then
    log_error "未配置远程节点"
    echo "请在 cluster.conf 中配置 REMOTE_NODES"
    exit 1
fi

# ===========================================
# 确认
# ===========================================
echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║${NC}${GREEN}                     远程部署                                 ${NC}${BLUE}║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}┌─────────────────────────────────────────────────────────────┐${NC}"
[ -n "$JAR_FILE" ] && echo -e "${CYAN}│${NC} ${WHITE}JAR 文件${NC}    │ ${GREEN}$(basename "$JAR_FILE")${NC}"
echo -e "${CYAN}│${NC} ${WHITE}部署模式${NC}    │ ${YELLOW}$([ "$INIT_MODE" = "true" ] && echo "首次部署 (含脚本)" || echo "更新")${NC}"
echo -e "${CYAN}│${NC} ${WHITE}目标节点${NC}    │ ${YELLOW}${TARGET_NODE:-所有远程节点}${NC}"
echo -e "${CYAN}│${NC} ${WHITE}节点数量${NC}    │ ${MAGENTA}${#REMOTE_NODES[@]} 个${NC}"
echo -e "${CYAN}└─────────────────────────────────────────────────────────────┘${NC}"
echo ""

if [ "$SKIP_CONFIRM" != "true" ]; then
    read -p "确认开始部署? [y/N] " confirm
    if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
        log_info "部署已取消"
        exit 0
    fi
fi

echo ""

# ===========================================
# 执行部署
# ===========================================
success_count=0
fail_count=0

if [ -n "$TARGET_NODE" ]; then
    # 部署到指定节点
    found="false"
    for node in "${REMOTE_NODES[@]}"; do
        [ -z "$node" ] && continue
        alias=$(parse_remote_node "$node" "alias")
        if [ "$alias" = "$TARGET_NODE" ]; then
            if deploy_to_node "$node" "$JAR_FILE" "$INIT_MODE" "$SCRIPTS_ONLY"; then
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
        exit 1
    fi
else
    # 部署到所有远程节点
    for node in "${REMOTE_NODES[@]}"; do
        [ -z "$node" ] && continue
        if deploy_to_node "$node" "$JAR_FILE" "$INIT_MODE" "$SCRIPTS_ONLY"; then
            success_count=$((success_count + 1))
        else
            fail_count=$((fail_count + 1))
        fi
    done
fi

# ===========================================
# 结果统计
# ===========================================
echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo -e "${WHITE}部署完成${NC}"
echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo -e "  ${GREEN}成功:${NC} ${success_count}  ${RED}失败:${NC} ${fail_count}"
echo ""

[ $fail_count -gt 0 ] && exit 1
exit 0
