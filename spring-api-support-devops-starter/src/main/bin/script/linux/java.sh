#!/bin/bash
# ============================================================
# Java 环境管理脚本
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "${SCRIPT_DIR}/../../common.sh"

# 默认配置
DEFAULT_JAVA_VERSION="21"
DEFAULT_INSTALL_DIR="${BASE_DIR}/java"
ADOPTIUM_API="https://api.adoptium.net/v3"

# ===========================================
# 帮助信息
# ===========================================
show_help() {
    echo -e "${BLUE}Java 环境管理${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [选项]"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  (无参数)           显示当前 Java 信息"
    echo "  -i <version>       下载并安装指定版本 (默认: ${DEFAULT_JAVA_VERSION})"
    echo "  -l                 列出已安装的 Java 版本"
    echo "  -u                 卸载当前配置的 Java"
    echo "  -d <dir>           指定安装目录 (默认: ${DEFAULT_INSTALL_DIR})"
    echo "  -h, --help         显示帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0                 查看当前 Java 信息"
    echo "  $0 -i              安装默认版本 Java ${DEFAULT_JAVA_VERSION}"
    echo "  $0 -i 17           安装 Java 17"
    echo "  $0 -l              列出已安装版本"
    echo "  $0 -u              卸载当前 Java"
    echo ""
}

# ===========================================
# 显示当前 Java 信息
# ===========================================
show_java_info() {
    print_title "Java 环境信息"
    echo ""
    
    # 检查保存的 Java 路径
    local source="系统 PATH"
    load_java_path
    
    if [ -n "$CUSTOM_JAVA" ]; then
        source="配置文件 (.java_path)"
        JAVA_CMD="$CUSTOM_JAVA"
    elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        source="JAVA_HOME 环境变量"
        JAVA_CMD="$JAVA_HOME/bin/java"
    elif command -v java &> /dev/null; then
        JAVA_CMD="java"
    else
        log_warn "未找到 Java 环境"
        return 1
    fi
    
    echo -e "${CYAN}【当前配置】${NC}"
    echo -e "  Java 路径:    ${WHITE}${JAVA_CMD}${NC}"
    echo -e "  路径来源:     ${WHITE}${source}${NC}"
    echo ""
    
    echo -e "${CYAN}【版本信息】${NC}"
    "$JAVA_CMD" -version 2>&1 | while read -r line; do
        echo -e "  ${WHITE}${line}${NC}"
    done
    echo ""
    
    # 显示保存的路径
    if [ -f "${JAVA_CONFIG_FILE}" ]; then
        local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
        echo -e "${CYAN}【已保存配置】${NC}"
        echo -e "  路径: ${WHITE}${saved_path}${NC}"
    fi
    echo ""
}

# ===========================================
# 列出已安装版本
# ===========================================
list_installed() {
    print_title "已安装的 Java 版本"
    echo ""
    
    local found=false
    
    # 检查默认安装目录
    if [ -d "$DEFAULT_INSTALL_DIR" ]; then
        echo -e "${CYAN}【${DEFAULT_INSTALL_DIR}】${NC}"
        for dir in "$DEFAULT_INSTALL_DIR"/*/; do
            if [ -d "$dir" ]; then
                local java_bin="${dir}bin/java"
                if [ -x "$java_bin" ]; then
                    local ver=$("$java_bin" -version 2>&1 | head -1 | grep -oP '"[^"]+"' | tr -d '"')
                    local name=$(basename "$dir")
                    echo -e "  ${GREEN}✓${NC} ${name}: ${WHITE}${ver}${NC}"
                    found=true
                fi
            fi
        done
    fi
    
    # 检查常见位置
    for path in /usr/lib/jvm/*/ /usr/java/*/; do
        if [ -d "$path" ]; then
            local java_bin="${path}bin/java"
            if [ -x "$java_bin" ]; then
                local ver=$("$java_bin" -version 2>&1 | head -1 | grep -oP '"[^"]+"' | tr -d '"')
                local name=$(basename "$path")
                echo -e "  ${GREEN}✓${NC} ${name}: ${WHITE}${ver}${NC} (${path})"
                found=true
            fi
        fi
    done
    
    if [ "$found" = "false" ]; then
        echo -e "  ${YELLOW}未找到已安装的 Java${NC}"
    fi
    echo ""
}

# ===========================================
# 检测系统架构
# ===========================================
detect_arch() {
    local arch=$(uname -m)
    case "$arch" in
        x86_64|amd64) echo "x64" ;;
        aarch64|arm64) echo "aarch64" ;;
        *) log_error "不支持的架构: $arch"; exit 1 ;;
    esac
}

# ===========================================
# 下载安装 Java
# ===========================================
install_java() {
    local java_ver="${1:-$DEFAULT_JAVA_VERSION}"
    local install_dir="${2:-$DEFAULT_INSTALL_DIR}"
    local arch=$(detect_arch)
    local os="linux"
    
    log_step "准备安装 Java ${java_ver}"
    echo ""
    
    # 检查是否已安装
    local target_dir="${install_dir}/jdk-${java_ver}"
    if [ -d "$target_dir" ] && [ -x "${target_dir}/bin/java" ]; then
        log_warn "Java ${java_ver} 已安装在 ${target_dir}"
        echo -e "是否设置为当前 Java? [y/N] "
        read -r answer
        if [[ "$answer" =~ ^[Yy]$ ]]; then
            save_java_path "${target_dir}/bin/java"
        fi
        return 0
    fi
    
    # 创建安装目录
    mkdir -p "$install_dir"
    
    # 获取下载 URL
    log_step "获取下载信息..."
    local api_url="${ADOPTIUM_API}/assets/latest/${java_ver}/hotspot?architecture=${arch}&image_type=jdk&os=${os}&vendor=eclipse"
    local download_info=$(curl -s "$api_url" 2>/dev/null)
    
    if [ -z "$download_info" ]; then
        log_error "无法获取下载信息，请检查网络连接"
        return 1
    fi
    
    local download_url=$(echo "$download_info" | grep -oP '"link":\s*"\K[^"]+\.tar\.gz' | head -1)
    local release_name=$(echo "$download_info" | grep -oP '"release_name":\s*"\K[^"]+' | head -1)
    
    if [ -z "$download_url" ]; then
        log_error "无法获取下载链接"
        return 1
    fi
    
    echo -e "  版本:     ${WHITE}${release_name}${NC}"
    echo -e "  架构:     ${WHITE}${arch}${NC}"
    echo -e "  目录:     ${WHITE}${install_dir}${NC}"
    echo ""
    
    # 下载
    local tmp_file="${install_dir}/jdk-${java_ver}.tar.gz"
    log_step "下载 Java ${java_ver}..."
    
    if command -v curl &> /dev/null; then
        curl -L --progress-bar -o "$tmp_file" "$download_url"
    elif command -v wget &> /dev/null; then
        wget -O "$tmp_file" "$download_url"
    else
        log_error "未找到 curl 或 wget"
        return 1
    fi
    
    if [ ! -f "$tmp_file" ] || [ ! -s "$tmp_file" ]; then
        log_error "下载失败"
        return 1
    fi
    
    # 解压
    log_step "解压安装..."
    tar -xzf "$tmp_file" -C "$install_dir"
    rm -f "$tmp_file"
    
    # 查找解压后的目录
    local extracted_dir=$(ls -d "${install_dir}"/jdk-* 2>/dev/null | grep -v "\.tar\.gz" | tail -1)
    
    if [ -z "$extracted_dir" ] || [ ! -d "$extracted_dir" ]; then
        log_error "解压失败"
        return 1
    fi
    
    local java_bin="${extracted_dir}/bin/java"
    if [ -x "$java_bin" ]; then
        log_info "Java ${java_ver} 安装成功: ${extracted_dir}"
        echo ""
        
        # 保存为当前 Java
        save_java_path "$java_bin"
        
        echo ""
        echo -e "${CYAN}Java 版本:${NC}"
        "$java_bin" -version 2>&1
    else
        log_error "安装异常，Java 可执行文件不存在"
        return 1
    fi
}

# ===========================================
# 卸载 Java
# ===========================================
uninstall_java() {
    if [ ! -f "${JAVA_CONFIG_FILE}" ]; then
        log_warn "未配置 Java 路径"
        return 0
    fi
    
    local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
    
    print_title "卸载 Java"
    echo ""
    echo -e "  当前配置: ${WHITE}${saved_path}${NC}"
    echo ""
    
    # 只删除配置，不删除实际文件
    rm -f "${JAVA_CONFIG_FILE}"
    log_info "已清除 Java 配置"
    
    # 询问是否删除安装目录
    local java_dir=$(dirname "$(dirname "$saved_path")")
    if [[ "$java_dir" == "${DEFAULT_INSTALL_DIR}"/* ]]; then
        echo ""
        echo -e "${YELLOW}是否删除安装目录 ${java_dir}? [y/N]${NC} "
        read -r answer
        if [[ "$answer" =~ ^[Yy]$ ]]; then
            rm -rf "$java_dir"
            log_info "已删除: ${java_dir}"
        fi
    fi
}

# ===========================================
# 参数解析
# ===========================================
ACTION="info"
JAVA_VERSION=""
INSTALL_DIR="$DEFAULT_INSTALL_DIR"

while [ $# -gt 0 ]; do
    case "$1" in
        -i)
            ACTION="install"
            shift
            # 检查下一个参数是否是版本号
            if [ -n "$1" ] && [[ "$1" =~ ^[0-9]+$ ]]; then
                JAVA_VERSION="$1"
                shift
            else
                JAVA_VERSION="$DEFAULT_JAVA_VERSION"
            fi
            ;;
        -l)
            ACTION="list"
            shift
            ;;
        -u)
            ACTION="uninstall"
            shift
            ;;
        -d)
            INSTALL_DIR="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            log_error "未知参数: $1"
            show_help
            exit 1
            ;;
    esac
done

# ===========================================
# 执行
# ===========================================
case "$ACTION" in
    info)      show_java_info ;;
    install)   install_java "$JAVA_VERSION" "$INSTALL_DIR" ;;
    list)      list_installed ;;
    uninstall) uninstall_java ;;
esac
