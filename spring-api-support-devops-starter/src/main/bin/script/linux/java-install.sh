#!/bin/bash
# ============================================================
# Java 下载安装脚本
# 支持从 Adoptium (Eclipse Temurin) 下载指定版本的 JDK
# 作者: DevOps
# 版本: 1.0.0
# ============================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 默认配置
DEFAULT_JAVA_VERSION="21"
DEFAULT_INSTALL_DIR="/opt/java"
ADOPTIUM_API="https://api.adoptium.net/v3"

# 帮助信息
show_help() {
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}              Java 下载安装脚本                              ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${YELLOW}用法:${NC} $0 [命令] [选项]"
    echo ""
    echo -e "${YELLOW}命令:${NC}"
    echo "  install          下载并安装 Java (默认命令)"
    echo "  list             列出可用的 Java 版本"
    echo "  check            检查当前系统的 Java 环境"
    echo "  remove           移除已安装的 Java 版本"
    echo ""
    echo -e "${YELLOW}选项:${NC}"
    echo "  -v, --version    Java 版本号 (默认: ${DEFAULT_JAVA_VERSION})"
    echo "  -d, --dir        安装目录 (默认: ${DEFAULT_INSTALL_DIR})"
    echo "  -a, --arch       架构 (x64/aarch64, 默认: 自动检测)"
    echo "  -s, --set-default 设置为系统默认 Java"
    echo "  -h, --help       显示此帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0 install                      # 安装默认版本 (Java ${DEFAULT_JAVA_VERSION})"
    echo "  $0 install -v 17                # 安装 Java 17"
    echo "  $0 install -v 21 -s             # 安装 Java 21 并设为默认"
    echo "  $0 list                         # 列出可用版本"
    echo "  $0 check                        # 检查当前 Java 环境"
    echo "  $0 remove -v 17                 # 移除 Java 17"
}

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${CYAN}[STEP]${NC} $1"
}

# 检测系统架构
detect_arch() {
    local arch=$(uname -m)
    case "$arch" in
        x86_64|amd64)
            echo "x64"
            ;;
        aarch64|arm64)
            echo "aarch64"
            ;;
        *)
            log_error "不支持的架构: $arch"
            exit 1
            ;;
    esac
}

# 检测操作系统
detect_os() {
    local os=$(uname -s | tr '[:upper:]' '[:lower:]')
    case "$os" in
        linux)
            echo "linux"
            ;;
        darwin)
            echo "mac"
            ;;
        *)
            log_error "不支持的操作系统: $os"
            exit 1
            ;;
    esac
}

# 检查依赖
check_dependencies() {
    local missing=()
    
    if ! command -v curl &> /dev/null; then
        missing+=("curl")
    fi
    
    if ! command -v tar &> /dev/null; then
        missing+=("tar")
    fi
    
    if [ ${#missing[@]} -gt 0 ]; then
        log_error "缺少必要的依赖: ${missing[*]}"
        log_info "请先安装: sudo apt-get install ${missing[*]} 或 sudo yum install ${missing[*]}"
        exit 1
    fi
}

# 获取可用版本列表
list_available_versions() {
    log_step "正在获取可用的 Java LTS 版本..."
    
    local versions=$(curl -s "${ADOPTIUM_API}/info/available_releases" 2>/dev/null)
    
    if [ -z "$versions" ]; then
        log_error "无法获取版本信息，请检查网络连接"
        exit 1
    fi
    
    echo ""
    echo -e "${BLUE}可用的 LTS 版本:${NC}"
    echo "$versions" | grep -oP '"available_lts_releases":\s*\[\K[^\]]+' | tr ',' '\n' | tr -d ' ' | while read ver; do
        if [ -n "$ver" ]; then
            echo "  - Java $ver"
        fi
    done
    
    echo ""
    echo -e "${BLUE}可用的所有版本:${NC}"
    echo "$versions" | grep -oP '"available_releases":\s*\[\K[^\]]+' | tr ',' '\n' | tr -d ' ' | while read ver; do
        if [ -n "$ver" ]; then
            echo "  - Java $ver"
        fi
    done
}

# 检查当前 Java 环境
check_java_env() {
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}${GREEN}              当前 Java 环境信息                            ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # 检查 JAVA_HOME
    if [ -n "$JAVA_HOME" ]; then
        echo -e "${GREEN}JAVA_HOME:${NC} $JAVA_HOME"
    else
        echo -e "${YELLOW}JAVA_HOME:${NC} 未设置"
    fi
    
    # 检查 java 命令
    if command -v java &> /dev/null; then
        echo ""
        echo -e "${GREEN}Java 版本:${NC}"
        java -version 2>&1 | head -3
        echo ""
        echo -e "${GREEN}Java 路径:${NC} $(which java)"
    else
        echo ""
        echo -e "${YELLOW}未找到 java 命令${NC}"
    fi
    
    # 列出已安装的版本
    if [ -d "$DEFAULT_INSTALL_DIR" ]; then
        echo ""
        echo -e "${GREEN}已安装的版本 (${DEFAULT_INSTALL_DIR}):${NC}"
        ls -1 "$DEFAULT_INSTALL_DIR" 2>/dev/null | while read dir; do
            if [ -d "${DEFAULT_INSTALL_DIR}/${dir}" ]; then
                local java_bin="${DEFAULT_INSTALL_DIR}/${dir}/bin/java"
                if [ -x "$java_bin" ]; then
                    local ver=$("$java_bin" -version 2>&1 | head -1 | grep -oP '"\K[^"]+')
                    echo "  - ${dir}: ${ver}"
                fi
            fi
        done
    fi
}

# 下载并安装 Java
install_java() {
    local version="$1"
    local install_dir="$2"
    local arch="$3"
    local set_default="$4"
    local os=$(detect_os)
    
    log_step "准备安装 Java ${version}"
    log_info "操作系统: ${os}, 架构: ${arch}"
    log_info "安装目录: ${install_dir}"
    
    # 创建安装目录
    if [ ! -d "$install_dir" ]; then
        log_step "创建安装目录..."
        sudo mkdir -p "$install_dir" || {
            log_error "无法创建安装目录: $install_dir"
            exit 1
        }
    fi
    
    # 构建下载 URL
    local download_url="${ADOPTIUM_API}/binary/latest/${version}/ga/${os}/${arch}/jdk/hotspot/normal/eclipse"
    
    log_step "正在从 Adoptium 下载 Java ${version}..."
    log_info "URL: ${download_url}"
    
    # 创建临时目录
    local tmp_dir=$(mktemp -d)
    local tmp_file="${tmp_dir}/jdk.tar.gz"
    
    # 下载
    curl -L -o "$tmp_file" "$download_url" --progress-bar 2>&1
    
    if [ ! -f "$tmp_file" ] || [ ! -s "$tmp_file" ]; then
        log_error "下载失败"
        rm -rf "$tmp_dir"
        exit 1
    fi
    
    log_info "下载完成，文件大小: $(du -h "$tmp_file" | cut -f1)"
    
    # 解压
    log_step "正在解压..."
    local extract_dir="${tmp_dir}/extract"
    mkdir -p "$extract_dir"
    tar -xzf "$tmp_file" -C "$extract_dir"
    
    # 查找解压后的目录名
    local jdk_dir=$(ls "$extract_dir" | head -1)
    if [ -z "$jdk_dir" ]; then
        log_error "解压失败，未找到 JDK 目录"
        rm -rf "$tmp_dir"
        exit 1
    fi
    
    # 目标目录
    local target_dir="${install_dir}/jdk-${version}"
    
    # 如果已存在，询问是否覆盖
    if [ -d "$target_dir" ]; then
        log_warn "目标目录已存在: ${target_dir}"
        read -p "是否覆盖? (y/N): " confirm
        if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
            log_info "取消安装"
            rm -rf "$tmp_dir"
            exit 0
        fi
        sudo rm -rf "$target_dir"
    fi
    
    # 移动到安装目录
    log_step "正在安装到 ${target_dir}..."
    sudo mv "${extract_dir}/${jdk_dir}" "$target_dir"
    
    # 清理临时文件
    rm -rf "$tmp_dir"
    
    log_info "Java ${version} 安装成功!"
    log_info "安装路径: ${target_dir}"
    
    # 设置为默认
    if [ "$set_default" = "true" ]; then
        set_default_java "$target_dir"
    fi
    
    # 显示使用提示
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}使用方法:${NC}"
    echo ""
    echo "1. 临时使用 (当前会话):"
    echo "   export JAVA_HOME=${target_dir}"
    echo "   export PATH=\$JAVA_HOME/bin:\$PATH"
    echo ""
    echo "2. 永久生效 (添加到 ~/.bashrc 或 ~/.zshrc):"
    echo "   echo 'export JAVA_HOME=${target_dir}' >> ~/.bashrc"
    echo "   echo 'export PATH=\$JAVA_HOME/bin:\$PATH' >> ~/.bashrc"
    echo "   source ~/.bashrc"
    echo ""
    echo "3. 在 startup.sh 中使用:"
    echo "   ./startup.sh --java ${target_dir}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
}

# 设置默认 Java
set_default_java() {
    local java_home="$1"
    
    log_step "设置为系统默认 Java..."
    
    # 创建符号链接
    local default_link="${DEFAULT_INSTALL_DIR}/default"
    sudo rm -f "$default_link"
    sudo ln -s "$java_home" "$default_link"
    
    log_info "已创建符号链接: ${default_link} -> ${java_home}"
    
    # 提示用户设置环境变量
    echo ""
    log_info "请将以下内容添加到 /etc/profile 或 ~/.bashrc:"
    echo "export JAVA_HOME=${default_link}"
    echo 'export PATH=$JAVA_HOME/bin:$PATH'
}

# 移除 Java
remove_java() {
    local version="$1"
    local install_dir="$2"
    
    local target_dir="${install_dir}/jdk-${version}"
    
    if [ ! -d "$target_dir" ]; then
        log_error "未找到 Java ${version}: ${target_dir}"
        exit 1
    fi
    
    log_warn "即将删除: ${target_dir}"
    read -p "确认删除? (y/N): " confirm
    
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        sudo rm -rf "$target_dir"
        log_info "Java ${version} 已删除"
    else
        log_info "取消删除"
    fi
}

# 主程序
main() {
    local command="install"
    local version="$DEFAULT_JAVA_VERSION"
    local install_dir="$DEFAULT_INSTALL_DIR"
    local arch=""
    local set_default="false"
    
    # 解析参数
    while [ $# -gt 0 ]; do
        case "$1" in
            install|list|check|remove)
                command="$1"
                shift
                ;;
            -v|--version)
                version="$2"
                shift 2
                ;;
            -d|--dir)
                install_dir="$2"
                shift 2
                ;;
            -a|--arch)
                arch="$2"
                shift 2
                ;;
            -s|--set-default)
                set_default="true"
                shift
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
    
    # 自动检测架构
    if [ -z "$arch" ]; then
        arch=$(detect_arch)
    fi
    
    # 执行命令
    case "$command" in
        install)
            check_dependencies
            install_java "$version" "$install_dir" "$arch" "$set_default"
            ;;
        list)
            check_dependencies
            list_available_versions
            ;;
        check)
            check_java_env
            ;;
        remove)
            remove_java "$version" "$install_dir"
            ;;
        *)
            log_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 运行主程序
main "$@"
