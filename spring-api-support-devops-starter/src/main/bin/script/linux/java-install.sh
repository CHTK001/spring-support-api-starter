#!/bin/bash
# ============================================================
# Java 涓嬭浇瀹夎鑴氭湰
# 鏀寔浠?Adoptium (Eclipse Temurin) 涓嬭浇鎸囧畾鐗堟湰鐨?JDK
# 浣滆€? DevOps
# 鐗堟湰: 1.0.0
# ============================================================

# 棰滆壊瀹氫箟
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 榛樿閰嶇疆
DEFAULT_JAVA_VERSION="21"
DEFAULT_INSTALL_DIR="/opt/java"
ADOPTIUM_API="https://api.adoptium.net/v3"

# 甯姪淇℃伅
show_help() {
    echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${BLUE}鈺?{NC}${GREEN}              Java 涓嬭浇瀹夎鑴氭湰                              ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    echo -e "${YELLOW}鐢ㄦ硶:${NC} $0 [鍛戒护] [閫夐」]"
    echo ""
    echo -e "${YELLOW}鍛戒护:${NC}"
    echo "  install              瀹夎鎸囧畾鐗堟湰鐨?Java"
    echo "  list                 鍒楀嚭鍙敤鐨?Java 鐗堟湰"
    echo "  check                妫€鏌ュ綋鍓?Java 鐜"
    echo "  remove               绉婚櫎宸插畨瑁呯殑 Java"
    echo ""
    echo -e "${YELLOW}閫夐」:${NC}"
    echo "  --version <ver>      鎸囧畾 Java 鐗堟湰 (榛樿: ${DEFAULT_JAVA_VERSION})"
    echo "  --dir <path>         鎸囧畾瀹夎鐩綍 (榛樿: ${DEFAULT_INSTALL_DIR})"
    echo "  --arch <arch>        鎸囧畾鏋舵瀯 (x64/aarch64, 榛樿: 鑷姩妫€娴?"
    echo "  --set-default        璁剧疆涓洪粯璁?Java (鍒涘缓绗﹀彿閾炬帴)"
    echo "  --help, -h           鏄剧ず甯姪淇℃伅"
    echo ""
    echo -e "${YELLOW}绀轰緥:${NC}"
    echo "  $0 install                           瀹夎榛樿鐗堟湰 (Java ${DEFAULT_JAVA_VERSION})"
    echo "  $0 install --version 17              瀹夎 Java 17"
    echo "  $0 install --version 21 --set-default 瀹夎骞惰涓洪粯璁?
    echo "  $0 install --dir /home/user/java     瀹夎鍒版寚瀹氱洰褰?
    echo "  $0 list                              鍒楀嚭鍙敤鐗堟湰"
    echo "  $0 check                             妫€鏌ュ綋鍓?Java"
    echo ""
}

# 妫€娴嬬郴缁熸灦鏋?
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
            echo -e "${RED}[閿欒] 涓嶆敮鎸佺殑鏋舵瀯: $arch${NC}" >&2
            exit 1
            ;;
    esac
}

# 妫€娴嬫搷浣滅郴缁?
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
            echo -e "${RED}[閿欒] 涓嶆敮鎸佺殑鎿嶄綔绯荤粺: $os${NC}" >&2
            exit 1
            ;;
    esac
}

# 鍒楀嚭鍙敤鐗堟湰
list_versions() {
    echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${BLUE}鈺?{NC}${GREEN}              鍙敤鐨?Java LTS 鐗堟湰                            ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    echo -e "${CYAN}LTS 鐗堟湰:${NC}"
    echo "  8   - Java 8  (LTS, 闀挎湡鏀寔)"
    echo "  11  - Java 11 (LTS, 闀挎湡鏀寔)"
    echo "  17  - Java 17 (LTS, 闀挎湡鏀寔)"
    echo "  21  - Java 21 (LTS, 闀挎湡鏀寔, 鎺ㄨ崘)"
    echo ""
    echo -e "${CYAN}鏈€鏂扮増鏈?${NC}"
    echo "  22  - Java 22"
    echo "  23  - Java 23"
    echo ""
    echo -e "${YELLOW}鎻愮ず: 鎺ㄨ崘浣跨敤 LTS 鐗堟湰浠ヨ幏寰楅暱鏈熸敮鎸?{NC}"
}

# 妫€鏌ュ綋鍓?Java 鐜
check_java() {
    echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${BLUE}鈺?{NC}${GREEN}              Java 鐜妫€鏌?                                 ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    
    # 妫€鏌?JAVA_HOME
    if [ -n "$JAVA_HOME" ]; then
        echo -e "${CYAN}JAVA_HOME:${NC} $JAVA_HOME"
    else
        echo -e "${YELLOW}JAVA_HOME:${NC} 鏈缃?
    fi
    
    # 妫€鏌?java 鍛戒护
    if command -v java &> /dev/null; then
        echo ""
        echo -e "${CYAN}Java 璺緞:${NC} $(which java)"
        echo -e "${CYAN}Java 鐗堟湰:${NC}"
        java -version 2>&1 | head -3
    else
        echo ""
        echo -e "${YELLOW}[璀﹀憡] 鏈壘鍒?java 鍛戒护${NC}"
    fi
    
    # 妫€鏌ュ凡瀹夎鐨勭増鏈?
    if [ -d "${DEFAULT_INSTALL_DIR}" ]; then
        echo ""
        echo -e "${CYAN}宸插畨瑁呯殑鐗堟湰 (${DEFAULT_INSTALL_DIR}):${NC}"
        ls -1 "${DEFAULT_INSTALL_DIR}" 2>/dev/null | grep -E "^jdk-|^temurin-" | while read dir; do
            echo "  - $dir"
        done
    fi
}

# 涓嬭浇骞跺畨瑁?Java
install_java() {
    local version="${1:-$DEFAULT_JAVA_VERSION}"
    local install_dir="${2:-$DEFAULT_INSTALL_DIR}"
    local arch="${3:-$(detect_arch)}"
    local set_default="${4:-false}"
    local os=$(detect_os)
    
    echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${BLUE}鈺?{NC}${GREEN}              瀹夎 Java ${version}                                  ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    echo -e "  鐗堟湰:     ${WHITE}${version}${NC}"
    echo -e "  鏋舵瀯:     ${WHITE}${arch}${NC}"
    echo -e "  绯荤粺:     ${WHITE}${os}${NC}"
    echo -e "  瀹夎鐩綍: ${WHITE}${install_dir}${NC}"
    echo ""
    
    # 妫€鏌ユ槸鍚︽湁 curl 鎴?wget
    local downloader=""
    if command -v curl &> /dev/null; then
        downloader="curl"
    elif command -v wget &> /dev/null; then
        downloader="wget"
    else
        echo -e "${RED}[閿欒] 闇€瑕?curl 鎴?wget 鏉ヤ笅杞芥枃浠?{NC}"
        exit 1
    fi
    
    # 鍒涘缓瀹夎鐩綍
    if [ ! -d "$install_dir" ]; then
        echo -e "${CYAN}[姝ラ 1] 鍒涘缓瀹夎鐩綍...${NC}"
        sudo mkdir -p "$install_dir" || mkdir -p "$install_dir"
        if [ $? -ne 0 ]; then
            echo -e "${RED}[閿欒] 鏃犳硶鍒涘缓鐩綍: $install_dir${NC}"
            exit 1
        fi
    fi
    
    # 鑾峰彇涓嬭浇閾炬帴
    echo -e "${CYAN}[姝ラ 2] 鑾峰彇涓嬭浇閾炬帴...${NC}"
    local api_url="${ADOPTIUM_API}/assets/latest/${version}/hotspot?architecture=${arch}&image_type=jdk&os=${os}&vendor=eclipse"
    
    local download_url=""
    local file_name=""
    
    if [ "$downloader" = "curl" ]; then
        local response=$(curl -s "$api_url")
        download_url=$(echo "$response" | grep -o '"link":"[^"]*"' | head -1 | cut -d'"' -f4)
        file_name=$(echo "$response" | grep -o '"name":"[^"]*\.tar\.gz"' | head -1 | cut -d'"' -f4)
    else
        local response=$(wget -qO- "$api_url")
        download_url=$(echo "$response" | grep -o '"link":"[^"]*"' | head -1 | cut -d'"' -f4)
        file_name=$(echo "$response" | grep -o '"name":"[^"]*\.tar\.gz"' | head -1 | cut -d'"' -f4)
    fi
    
    if [ -z "$download_url" ]; then
        echo -e "${RED}[閿欒] 鏃犳硶鑾峰彇 Java ${version} 鐨勪笅杞介摼鎺?{NC}"
        echo -e "${YELLOW}鎻愮ず: 璇锋鏌ョ増鏈彿鏄惁姝ｇ‘锛屾垨灏濊瘯浣跨敤 LTS 鐗堟湰 (8, 11, 17, 21)${NC}"
        exit 1
    fi
    
    echo -e "  涓嬭浇閾炬帴: ${download_url}"
    echo -e "  鏂囦欢鍚?   ${file_name}"
    echo ""
    
    # 涓嬭浇鏂囦欢
    local tmp_dir=$(mktemp -d)
    local tmp_file="${tmp_dir}/${file_name}"
    
    echo -e "${CYAN}[姝ラ 3] 涓嬭浇 JDK...${NC}"
    if [ "$downloader" = "curl" ]; then
        curl -L --progress-bar -o "$tmp_file" "$download_url"
    else
        wget --show-progress -O "$tmp_file" "$download_url"
    fi
    
    if [ $? -ne 0 ] || [ ! -f "$tmp_file" ]; then
        echo -e "${RED}[閿欒] 涓嬭浇澶辫触${NC}"
        rm -rf "$tmp_dir"
        exit 1
    fi
    
    local file_size=$(du -h "$tmp_file" | cut -f1)
    echo -e "${GREEN}  涓嬭浇瀹屾垚: ${file_size}${NC}"
    echo ""
    
    # 瑙ｅ帇鏂囦欢
    echo -e "${CYAN}[姝ラ 4] 瑙ｅ帇 JDK...${NC}"
    local extract_dir=$(tar -tzf "$tmp_file" | head -1 | cut -d'/' -f1)
    
    # 妫€鏌ユ槸鍚﹀凡瀛樺湪
    if [ -d "${install_dir}/${extract_dir}" ]; then
        echo -e "${YELLOW}[璀﹀憡] 鐩綍宸插瓨鍦? ${install_dir}/${extract_dir}${NC}"
        read -p "鏄惁瑕嗙洊? (y/N): " confirm
        if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
            sudo rm -rf "${install_dir}/${extract_dir}" 2>/dev/null || rm -rf "${install_dir}/${extract_dir}"
        else
            echo -e "${YELLOW}[鍙栨秷] 瀹夎宸插彇娑?{NC}"
            rm -rf "$tmp_dir"
            exit 0
        fi
    fi
    
    # 瑙ｅ帇鍒板畨瑁呯洰褰?
    sudo tar -xzf "$tmp_file" -C "$install_dir" 2>/dev/null || tar -xzf "$tmp_file" -C "$install_dir"
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}[閿欒] 瑙ｅ帇澶辫触${NC}"
        rm -rf "$tmp_dir"
        exit 1
    fi
    
    echo -e "${GREEN}  瑙ｅ帇瀹屾垚: ${install_dir}/${extract_dir}${NC}"
    
    # 娓呯悊涓存椂鏂囦欢
    rm -rf "$tmp_dir"
    
    local java_home="${install_dir}/${extract_dir}"
    
    # 璁剧疆涓洪粯璁?
    if [ "$set_default" = "true" ]; then
        echo ""
        echo -e "${CYAN}[姝ラ 5] 璁剧疆涓洪粯璁?Java...${NC}"
        
        # 鍒涘缓绗﹀彿閾炬帴
        local default_link="${install_dir}/default"
        sudo rm -f "$default_link" 2>/dev/null || rm -f "$default_link"
        sudo ln -s "$java_home" "$default_link" 2>/dev/null || ln -s "$java_home" "$default_link"
        
        echo -e "${GREEN}  绗﹀彿閾炬帴: ${default_link} -> ${java_home}${NC}"
        
        # 杈撳嚭鐜鍙橀噺閰嶇疆寤鸿
        echo ""
        echo -e "${CYAN}[鎻愮ず] 璇峰皢浠ヤ笅鍐呭娣诲姞鍒?~/.bashrc 鎴?~/.profile:${NC}"
        echo ""
        echo "export JAVA_HOME=${default_link}"
        echo 'export PATH=$JAVA_HOME/bin:$PATH'
        echo ""
    fi
    
    # 楠岃瘉瀹夎
    echo ""
    echo -e "${CYAN}[楠岃瘉] 妫€鏌ュ畨瑁?..${NC}"
    "${java_home}/bin/java" -version 2>&1 | head -3
    
    echo ""
    echo -e "${GREEN}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${GREEN}鈺?                   鉁?瀹夎鎴愬姛                                鈺?{NC}"
    echo -e "${GREEN}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    echo -e "  JAVA_HOME: ${java_home}"
    echo -e "  Java 璺緞: ${java_home}/bin/java"
    echo ""
    echo -e "${YELLOW}浣跨敤鏂规硶:${NC}"
    echo "  1. 涓存椂浣跨敤: export JAVA_HOME=${java_home}"
    echo "  2. 鍚姩鑴氭湰: ./startup.sh start --java ${java_home}/bin/java"
    echo ""
}

# 绉婚櫎宸插畨瑁呯殑 Java
remove_java() {
    local version="${1}"
    local install_dir="${2:-$DEFAULT_INSTALL_DIR}"
    
    if [ -z "$version" ]; then
        echo -e "${RED}[閿欒] 璇锋寚瀹氳绉婚櫎鐨勭増鏈?{NC}"
        echo ""
        echo -e "${CYAN}宸插畨瑁呯殑鐗堟湰:${NC}"
        ls -1 "${install_dir}" 2>/dev/null | grep -E "^jdk-|^temurin-" | while read dir; do
            echo "  - $dir"
        done
        echo ""
        echo "鐢ㄦ硶: $0 remove --version <dir_name>"
        exit 1
    fi
    
    local target_dir="${install_dir}/${version}"
    
    if [ ! -d "$target_dir" ]; then
        echo -e "${RED}[閿欒] 鐩綍涓嶅瓨鍦? $target_dir${NC}"
        exit 1
    fi
    
    echo -e "${YELLOW}鍗冲皢绉婚櫎: $target_dir${NC}"
    read -p "纭绉婚櫎? (y/N): " confirm
    
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        sudo rm -rf "$target_dir" 2>/dev/null || rm -rf "$target_dir"
        echo -e "${GREEN}[鎴愬姛] 宸茬Щ闄? $target_dir${NC}"
    else
        echo -e "${YELLOW}[鍙栨秷] 鎿嶄綔宸插彇娑?{NC}"
    fi
}

# 鍙傛暟瑙ｆ瀽
COMMAND=""
JAVA_VERSION="$DEFAULT_JAVA_VERSION"
INSTALL_DIR="$DEFAULT_INSTALL_DIR"
ARCH=""
SET_DEFAULT="false"

while [ $# -gt 0 ]; do
    case "$1" in
        install|list|check|remove)
            COMMAND="$1"
            shift
            ;;
        --version)
            JAVA_VERSION="$2"
            shift 2
            ;;
        --dir)
            INSTALL_DIR="$2"
            shift 2
            ;;
        --arch)
            ARCH="$2"
            shift 2
            ;;
        --set-default)
            SET_DEFAULT="true"
            shift
            ;;
        --help|-h)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}[閿欒] 鏈煡鍙傛暟: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# 鎵ц鍛戒护
case "$COMMAND" in
    install)
        install_java "$JAVA_VERSION" "$INSTALL_DIR" "$ARCH" "$SET_DEFAULT"
        ;;
    list)
        list_versions
        ;;
    check)
        check_java
        ;;
    remove)
        remove_java "$JAVA_VERSION" "$INSTALL_DIR"
        ;;
    *)
        show_help
        ;;
esac
