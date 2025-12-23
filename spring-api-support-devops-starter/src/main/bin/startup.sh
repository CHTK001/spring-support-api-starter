#!/bin/bash
# ============================================================
# 缁熶竴搴旂敤绠＄悊鑴氭湰
# 鏀寔鍔ㄦ€佸悕绉版浛鎹?(Maven 璧勬簮杩囨护)
# 浣滆€? DevOps
# 鐗堟湰: 2.0.0
# ============================================================

# ===========================================
# 鍩虹閰嶇疆 (Maven 璧勬簮杩囨护鏇挎崲)
# ===========================================
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_NAME="${APP_NAME:-@project.artifactId@}"
APP_VERSION="${APP_VERSION:-@project.version@}"
JAR_NAME="${APP_NAME}-${APP_VERSION}.jar"

# 鐩綍閰嶇疆
CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
LOG_DIR="${APP_HOME}/logs"
PID_FILE="${APP_HOME}/${APP_NAME}.pid"
BACKUP_DIR="${APP_HOME}/backup"

# 鏃ュ織鏂囦欢閰嶇疆
STARTUP_LOG="${LOG_DIR}/startup.log"
APP_LOG="${LOG_DIR}/${APP_NAME}.log"

# 璋冭瘯绔彛閰嶇疆
DEFAULT_DEBUG_PORT=5005

# Docker 閰嶇疆
DOCKER_REGISTRY="${DOCKER_REGISTRY:-}"
DOCKER_IMAGE_NAME="${DOCKER_IMAGE_NAME:-${APP_NAME}}"
DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG:-${APP_VERSION}}"
DOCKER_CONTAINER_NAME="${DOCKER_CONTAINER_NAME:-${APP_NAME}}"
DOCKER_PORT="${DOCKER_PORT:-8080}"
DOCKER_HOST_PORT="${DOCKER_HOST_PORT:-8080}"

# Skywalking 閰嶇疆
SKYWALKING_VERSION="${SKYWALKING_VERSION:-9.0.0}"
SKYWALKING_DIR="${APP_HOME}/skywalking"
SKYWALKING_AGENT_JAR="${SKYWALKING_DIR}/skywalking-agent/skywalking-agent.jar"
SW_AGENT_NAME="${SW_AGENT_NAME:-${APP_NAME}}"
SW_AGENT_COLLECTOR_BACKEND_SERVICES="${SW_AGENT_COLLECTOR_BACKEND_SERVICES:-127.0.0.1:11800}"

# JVM閰嶇疆
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}"
JAVA_OPTS="${JAVA_OPTS} -Dlogging.file.path=${LOG_DIR}"

# Spring閰嶇疆
SPRING_PROFILE="${SPRING_PROFILE:-prod}"

# 鏈嶅姟绔彛閰嶇疆
SERVER_PORT="${SERVER_PORT:-8080}"

# 棰滆壊瀹氫箟
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
WHITE='\033[1;37m'
NC='\033[0m'

# ===========================================
# 甯姪淇℃伅
# ===========================================
show_help() {
    echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${BLUE}鈺?{NC}${GREEN}              ${APP_NAME} 绠＄悊鑴氭湰                    ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺?{NC}${CYAN}              鐗堟湰: ${APP_VERSION}                              ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    echo -e "${YELLOW}鐢ㄦ硶:${NC} $0 [鍛戒护] [閫夐」]"
    echo ""
    echo -e "${YELLOW}鍩虹鍛戒护:${NC}"
    echo "  start              鍚姩搴旂敤 (榛樿)"
    echo "  stop               鍋滄搴旂敤"
    echo "  restart            閲嶅惎搴旂敤"
    echo "  status             鏌ョ湅搴旂敤鐘舵€?
    echo "  info               鏄剧ず璇︾粏杩愯淇℃伅"
    echo ""
    echo -e "${YELLOW}鏃ュ織鍛戒护:${NC}"
    echo "  logs [N]           鏌ョ湅鏈€杩?N 鏉℃棩蹇?(榛樿: 100)"
    echo "  tail               瀹炴椂璺熻釜鏃ュ織杈撳嚭"
    echo ""
    echo -e "${YELLOW}璇婃柇鍛戒护:${NC}"
    echo "  jvm                鏌ョ湅JVM淇℃伅"
    echo "  gc                 瑙﹀彂 GC (young/full)"
    echo "  tuning             JVM璋冧紭寤鸿"
    echo "  analyze            CPU/鍐呭瓨鍒嗘瀽"
    echo "  top-cpu            鍒嗘瀽楂楥PU绾跨▼鍙婁唬鐮佸爢鏍?(Top N)"
    echo "  top-mem            鍒嗘瀽楂樺唴瀛樺璞?(Top N)"
    echo "  dump               鐢熸垚杞偍鏂囦欢 (heap/thread/all)"
    echo "  arthas             鍚姩Arthas璇婃柇"
    echo ""
    echo -e "${YELLOW}鏈嶅姟绠＄悊鍛戒护:${NC}"
    echo "  install-service    瀹夎涓?systemd 鏈嶅姟"
    echo "  uninstall-service  鍗歌浇 systemd 鏈嶅姟"
    echo "  enable-service     鍚敤寮€鏈鸿嚜鍚?
    echo "  disable-service    绂佺敤寮€鏈鸿嚜鍚?
    echo "  service-status     鏌ョ湅鏈嶅姟鐘舵€?
    echo ""
    echo -e "${YELLOW}杩愮淮鍛戒护:${NC}"
    echo "  backup             澶囦唤閰嶇疆/JAR"
    echo "  restore            杩樺師閰嶇疆/JAR"
    echo "  upgrade            浠庡帇缂╁寘鍗囩骇 (鏀寔 zip/tar.gz)"
    echo "  rollback           鍥炴粴鍒颁笂涓€涓繕鍘熺偣"
    echo "  rollback-status    鏌ョ湅杩樺師鐐圭姸鎬?
    echo "  debug              浠ヨ皟璇曟ā寮忓惎鍔?
    echo ""
    echo -e "${YELLOW}鐜绠＄悊鍛戒护:${NC}"
    echo "  java               绠＄悊 Java 鐜 (install/list/check/remove)"
    echo "  skywalking         绠＄悊 Skywalking Agent (install/check/clean/info)"
    echo ""
    echo -e "${YELLOW}Docker 鍛戒护:${NC}"
    echo "  docker-build       鏋勫缓 Docker 闀滃儚"
    echo "  docker-deploy      鏋勫缓骞堕儴缃插埌鏈満 (涓€閿彂甯?"
    echo "  publish            docker-deploy 鐨勭畝鍐?
    echo "  docker-start       鍚姩 Docker 瀹瑰櫒"
    echo "  docker-stop        鍋滄 Docker 瀹瑰櫒"
    echo "  docker-restart     閲嶅惎 Docker 瀹瑰櫒"
    echo "  docker-status      鏌ョ湅瀹瑰櫒鐘舵€?
    echo "  docker-logs        鏌ョ湅瀹瑰櫒鏃ュ織"
    echo "  docker-clean       娓呯悊鏃ч暅鍍?
    echo ""
    echo -e "${YELLOW}閫夐」:${NC}"
    echo "  --java [path]         鎸囧畾 Java 璺緞 (鏃犲弬鏁版椂鏄剧ず褰撳墠 Java)"
    echo "  --jvm-opts <opts>     鑷畾涔塉VM鍙傛暟"
    echo "  --debug-port <port>   鎸囧畾璋冭瘯绔彛 (榛樿: ${DEFAULT_DEBUG_PORT})"
    echo "  --docker-port <port>  鎸囧畾瀹瑰櫒鍐呴儴绔彛 (榛樿: 8080)"
    echo "  --host-port <port>    鎸囧畾涓绘満鏄犲皠绔彛 (榛樿: 8080)"
    echo "  --image-tag <tag>     鎸囧畾闀滃儚鏍囩 (榛樿: ${APP_VERSION})"
    echo "  --skywalking          鍚敤 Skywalking APM"
    echo "  --sw-backend <addr>   鎸囧畾 Skywalking OAP 鍦板潃"
    echo "  --sw-name <name>      鎸囧畾 Skywalking 鏈嶅姟鍚?
    echo "  --package <path>      鍗囩骇鍖呰矾寰?zip/tar.gz)"
    echo "  --no-restart          鍗囩骇鍚庝笉鑷姩閲嶅惎"
    echo "  --dev                 浣跨敤寮€鍙戠幆澧?dev)"
    echo "  --prod                浣跨敤鐢熶骇鐜 (prod, 榛樿)"
    echo "  --test                浣跨敤娴嬭瘯鐜 (test)"
    echo "  --profile <name>      鎸囧畾鑷畾涔夌幆澧?
    echo "  --output <path>       dump 杈撳嚭鐩綍"
    echo "  --dump-path <path>    dump 杈撳嚭鐩綍 (--output 鍒悕)"
    echo "  --logs <N>            鍚姩鍚庢樉绀烘渶杩?N 鏉℃棩蹇?
    echo "  --tail                鍚姩鍚庡疄鏃惰窡韪棩蹇?
    echo "  --help, -h            鏄剧ず甯姪淇℃伅"
    echo ""
    echo -e "${YELLOW}绀轰緥:${NC}"
    echo ""
    echo -e "  ${CYAN}鍩虹鍛戒护:${NC}"
    echo "    $0 start                             鍚姩搴旂敤"
    echo "    $0 start --dev                       寮€鍙戠幆澧冨惎鍔?
    echo "    $0 start --prod                      鐢熶骇鐜鍚姩"
    echo "    $0 start --test                      娴嬭瘯鐜鍚姩"
    echo "    $0 start --profile staging           鑷畾涔夌幆澧冨惎鍔?
    echo "    $0 start --tail                      鍚姩骞跺疄鏃惰窡韪棩蹇?
    echo "    $0 start --logs 200                  鍚姩鍚庢樉绀烘渶杩?200 鏉℃棩蹇?
    echo "    $0 start --java /opt/jdk17/bin/java  鎸囧畾 Java 璺緞鍚姩 (骞朵繚瀛?"
    echo "    $0 --java                            鏄剧ず褰撳墠浣跨敤鐨?Java 鐗堟湰"
    echo "    $0 stop                              鍋滄搴旂敤"
    echo "    $0 restart                           閲嶅惎搴旂敤"
    echo "    $0 status                            鏌ョ湅鐘舵€?
    echo "    $0 info                              鏌ョ湅璇︾粏淇℃伅"
    echo ""
    echo -e "  ${CYAN}鏃ュ織鍛戒护:${NC}"
    echo "    $0 logs                              鏌ョ湅鏈€杩?100 鏉℃棩蹇?
    echo "    $0 logs 50                           鏌ョ湅鏈€杩?50 鏉℃棩蹇?
    echo "    $0 tail                              瀹炴椂璺熻釜鏃ュ織"
    echo ""
    echo -e "  ${CYAN}璋冭瘯鍛戒护:${NC}"
    echo "    $0 debug                             璋冭瘯妯″紡鍚姩 (绔彛 5005)"
    echo "    $0 debug --debug-port 5006           璋冭瘯妯″紡鍚姩 (鎸囧畾绔彛)"
    echo ""
    echo -e "  ${CYAN}JVM 璇婃柇:${NC}"
    echo "    $0 jvm                               鏌ョ湅 JVM 淇℃伅"
    echo "    $0 gc                                瑙﹀彂 Full GC"
    echo "    $0 gc young                          瑙﹀彂 Young GC"
    echo "    $0 top-cpu                           楂?CPU 绾跨▼鍒嗘瀽 (Top 10)"
    echo "    $0 top-cpu 20                        楂?CPU 绾跨▼鍒嗘瀽 (Top 20)"
    echo "    $0 top-mem                           楂樺唴瀛樺璞″垎鏋?(Top 10)"
    echo "    $0 top-mem 20                        楂樺唴瀛樺璞″垎鏋?(Top 20)"
    echo "    $0 tuning                            JVM 璋冧紭寤鸿"
    echo "    $0 arthas                            鍚姩 Arthas 璇婃柇"
    echo ""
    echo -e "  ${CYAN}Dump 杞偍:${NC}"
    echo "    $0 dump                              鐢熸垚鍫?绾跨▼杞偍"
    echo "    $0 dump heap                         浠呭爢杞偍"
    echo "    $0 dump thread                       浠呯嚎绋嬭浆鍌?
    echo "    $0 dump --output /tmp/dump           鎸囧畾杈撳嚭鐩綍"
    echo ""
    echo -e "  ${CYAN}鏈嶅姟绠＄悊 (闇€ root 鏉冮檺):${NC}"
    echo "    $0 install-service                   瀹夎涓?systemd 鏈嶅姟"
    echo "    $0 uninstall-service                 鍗歌浇鏈嶅姟"
    echo "    $0 enable-service                    鍚敤寮€鏈鸿嚜鍚?
    echo "    $0 disable-service                   绂佺敤寮€鏈鸿嚜鍚?
    echo "    $0 service-status                    鏌ョ湅鏈嶅姟鐘舵€?
    echo ""
    echo -e "  ${CYAN}鍗囩骇鍥炴粴:${NC}"
    echo "    $0 upgrade --package /tmp/app.zip    浠庡帇缂╁寘鍗囩骇"
    echo "    $0 upgrade --package /tmp/app.tar.gz --no-restart  鍗囩骇浣嗕笉閲嶅惎"
    echo "    $0 rollback                          鍥炴粴鍒颁笂涓€涓増鏈?
    echo "    $0 rollback-status                   鏌ョ湅杩樺師鐐圭姸鎬?
    echo "    $0 backup                            澶囦唤閰嶇疆/JAR"
    echo "    $0 restore                           杩樺師閰嶇疆/JAR"
    echo ""
    echo -e "  ${CYAN}Docker 閮ㄧ讲:${NC}"
    echo "    $0 docker-build                      鏋勫缓 Docker 闀滃儚"
    echo "    $0 docker-deploy                     鏋勫缓骞堕儴缃?(涓€閿彂甯?"
    echo "    $0 publish                           docker-deploy 鐨勭畝鍐?
    echo "    $0 docker-start                      鍚姩瀹瑰櫒"
    echo "    $0 docker-stop                       鍋滄瀹瑰櫒"
    echo "    $0 docker-restart                    閲嶅惎瀹瑰櫒"
    echo "    $0 docker-status                     鏌ョ湅瀹瑰櫒鐘舵€?
    echo "    $0 docker-logs                       鏌ョ湅瀹瑰櫒鏃ュ織"
    echo "    $0 docker-clean                      娓呯悊鏃ч暅鍍?
    echo ""
    echo -e "  ${CYAN}Java 鐜绠＄悊:${NC}"
    echo "    $0 java install                      瀹夎 Java (榛樿 21)"
    echo "    $0 java install --version 17         瀹夎鎸囧畾鐗堟湰 Java"
    echo "    $0 java install --dir /opt/java      瀹夎鍒版寚瀹氱洰褰?
    echo "    $0 java list                         鍒楀嚭鍙敤 Java 鐗堟湰"
    echo "    $0 java check                        妫€鏌ュ綋鍓?Java 鐜"
    echo "    $0 java remove                       鍒犻櫎宸插畨瑁呯殑 Java"
    echo ""
    echo -e "  ${CYAN}Skywalking APM:${NC}"
    echo "    $0 start --skywalking                鍚敤 Skywalking 鍚姩"
    echo "    $0 start --skywalking --sw-backend 192.168.1.100:11800  鎸囧畾 OAP 鍦板潃"
    echo "    $0 skywalking install                瀹夎 Skywalking Agent"
    echo "    $0 skywalking check                  妫€鏌?Agent 鐘舵€?
    echo ""
}

# ===========================================
# Java 妫€娴?# ===========================================

# Java 閰嶇疆鏂囦欢璺緞 (鐢ㄤ簬淇濆瓨鐢ㄦ埛鎸囧畾鐨?Java 璺緞)
JAVA_CONFIG_FILE="${APP_HOME}/.java_path"

# 淇濆瓨 Java 璺緞鍒伴厤缃枃浠?save_java_path() {
    local java_path="$1"
    if [ -n "$java_path" ]; then
        echo "$java_path" > "${JAVA_CONFIG_FILE}"
        echo -e "${GREEN}[鎴愬姛] Java 璺緞宸蹭繚瀛? ${java_path}${NC}"
    fi
}

# 浠庨厤缃枃浠跺姞杞?Java 璺緞
load_java_path() {
    if [ -f "${JAVA_CONFIG_FILE}" ]; then
        local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
        if [ -n "$saved_path" ] && [ -x "$saved_path" ]; then
            CUSTOM_JAVA="$saved_path"
        fi
    fi
}

# 妫€鏌?Java 鐜
check_java() {
    # 鍏堝皾璇曞姞杞戒繚瀛樼殑 Java 璺緞
    [ -z "$CUSTOM_JAVA" ] && load_java_path
    
    if [ -n "$CUSTOM_JAVA" ]; then
        if [ -x "$CUSTOM_JAVA" ]; then
            JAVA_CMD="$CUSTOM_JAVA"
        else
            echo -e "${RED}[閿欒] 鎸囧畾鐨?Java 璺緞涓嶅瓨鍦ㄦ垨涓嶅彲鎵ц: ${CUSTOM_JAVA}${NC}"
            exit 1
        fi
    elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        JAVA_CMD="$JAVA_HOME/bin/java"
    elif command -v java &> /dev/null; then
        JAVA_CMD="java"
    elif [ -x "/usr/bin/java" ]; then
        JAVA_CMD="/usr/bin/java"
    elif [ -x "/usr/local/bin/java" ]; then
        JAVA_CMD="/usr/local/bin/java"
    else
        echo -e "${RED}[閿欒] 鏈壘鍒癑ava鐜${NC}"
        echo "璇疯缃?JAVA_HOME 鎴栦娇鐢?--java 鎸囧畾 Java 璺緞"
        exit 1
    fi
}

# 鑾峰彇 Java 鐗堟湰淇℃伅
get_java_version() {
    "$JAVA_CMD" -version 2>&1 | head -n 1 | awk -F '"' '{print $2}'
}

# 鏄剧ず褰撳墠 Java 淇℃伅
show_java_info() {
    check_java
    
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    echo -e "${GREEN}              褰撳墠 Java 鐜淇℃伅${NC}"
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    echo ""
    
    # 鏄剧ず Java 璺緞鏉ユ簮
    local source="绯荤粺 PATH"
    if [ -n "$CUSTOM_JAVA" ]; then
        if [ -f "${JAVA_CONFIG_FILE}" ]; then
            source="閰嶇疆鏂囦欢 (.java_path)"
        else
            source="鍛戒护琛?--java 鍙傛暟"
        fi
    elif [ -n "$JAVA_HOME" ]; then
        source="JAVA_HOME 鐜鍙橀噺"
    fi
    
    echo -e "  ${CYAN}Java 璺緞:${NC}    ${WHITE}${JAVA_CMD}${NC}"
    echo -e "  ${CYAN}璺緞鏉ユ簮:${NC}    ${WHITE}${source}${NC}"
    echo ""
    
    # 鏄剧ず瀹屾暣鐨?Java 鐗堟湰淇℃伅
    echo -e "  ${CYAN}Java 鐗堟湰:${NC}"
    "$JAVA_CMD" -version 2>&1 | while read -r line; do
        echo -e "    ${WHITE}${line}${NC}"
    done
    echo ""
    
    # 鏄剧ず閰嶇疆鏂囦欢鐘舵€?    if [ -f "${JAVA_CONFIG_FILE}" ]; then
        local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
        echo -e "  ${CYAN}宸蹭繚瀛樼殑 Java:${NC} ${WHITE}${saved_path}${NC}"
    else
        echo -e "  ${YELLOW}鎻愮ず: 浣跨敤 --java <path> 鎸囧畾 Java 璺緞骞朵繚瀛?{NC}"
    fi
    echo ""
}

# 鑾峰彇鏈満 IP 鍦板潃
get_local_ip() {
    local ip=""
    # 灏濊瘯澶氱鏂瑰紡鑾峰彇 IP
    if command -v ip &> /dev/null; then
        ip=$(ip route get 1 2>/dev/null | awk '{print $7;exit}')
    fi
    if [ -z "$ip" ] && command -v hostname &> /dev/null; then
        ip=$(hostname -I 2>/dev/null | awk '{print $1}')
    fi
    if [ -z "$ip" ] && command -v ifconfig &> /dev/null; then
        ip=$(ifconfig 2>/dev/null | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -1)
    fi
    echo "${ip:-127.0.0.1}"
}

# 浠庨厤缃枃浠惰鍙栫鍙?get_server_port() {
    local port="${SERVER_PORT}"
    # 灏濊瘯浠?application.yml 璇诲彇
    if [ -f "${CONFIG_DIR}/application.yml" ]; then
        local yml_port=$(grep -E '^\s*port:' "${CONFIG_DIR}/application.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$yml_port" ] && port="$yml_port"
    fi
    # 灏濊瘯浠?application-${profile}.yml 璇诲彇
    if [ -f "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" ]; then
        local profile_port=$(grep -E '^\s*port:' "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$profile_port" ] && port="$profile_port"
    fi
    echo "${port:-8080}"
}

# 鑾峰彇 context-path
get_context_path() {
    local ctx_path=""
    # 浠庨厤缃枃浠惰鍙?context-path
    if [ -f "${CONFIG_DIR}/application.yml" ]; then
        ctx_path=$(grep -E 'context-path:' "${CONFIG_DIR}/application.yml" 2>/dev/null | head -1 | awk '{print $2}')
    fi
    if [ -f "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" ]; then
        local profile_ctx=$(grep -E 'context-path:' "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$profile_ctx" ] && ctx_path="$profile_ctx"
    fi
    echo "${ctx_path}"
}

# 妫€娴嬫槸鍚﹀惎鐢?Swagger
check_swagger_enabled() {
    local swagger_enabled="false"
    # 妫€鏌ラ厤缃枃浠朵腑鏄惁鏈?swagger/springdoc/knife4j 閰嶇疆
    if [ -d "${CONFIG_DIR}" ]; then
        if grep -rq -E '(springdoc|swagger|knife4j)' "${CONFIG_DIR}/" 2>/dev/null; then
            swagger_enabled="true"
        fi
    fi
    # 妫€鏌?JAR 涓槸鍚﹀寘鍚?swagger 鐩稿叧渚濊禆
    if [ -f "${APP_HOME}/${JAR_NAME}" ]; then
        if unzip -l "${APP_HOME}/${JAR_NAME}" 2>/dev/null | grep -qE '(springdoc|swagger|knife4j)'; then
            swagger_enabled="true"
        fi
    fi
    echo "$swagger_enabled"
}

# 鏋勫缓 loader path
build_loader_path() {
    LOADER_PATH="${CONFIG_DIR}"
    [ -d "${LIB_DIR}" ] && LOADER_PATH="${LOADER_PATH},${LIB_DIR}"
}

# ===========================================
# 杩涚▼绠＄悊
# ===========================================
get_pid() {
    if [ -f "${PID_FILE}" ]; then
        local pid=$(cat "${PID_FILE}")
        ps -p "$pid" > /dev/null 2>&1 && echo "$pid" && return 0
    fi
    pgrep -f "${JAR_NAME}" 2>/dev/null | head -1
}

# ===========================================
# 鐘舵€佹鏌?# ===========================================
check_status() {
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "${GREEN}[杩愯涓璢${NC} ${APP_NAME} (PID: ${pid})"
        return 0
    else
        echo -e "${YELLOW}[宸插仠姝${NC} ${APP_NAME}"
        return 1
    fi
}

# ===========================================
# 璇︾粏淇℃伅鏄剧ず
# ===========================================
show_info() {
    local pid=$(get_pid)
    
    echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${BLUE}鈺?{NC}${GREEN}                    搴旂敤杩愯淇℃伅                              ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    
    echo -e "${CYAN}銆愬熀纭€淇℃伅銆?{NC}"
    echo -e "  搴旂敤鍚嶇О:     ${WHITE}${APP_NAME}${NC}"
    echo -e "  搴旂敤鐗堟湰:     ${WHITE}${APP_VERSION}${NC}"
    echo -e "  JAR 鏂囦欢:     ${WHITE}${JAR_NAME}${NC}"
    echo -e "  搴旂敤鐩綍:     ${WHITE}${APP_HOME}${NC}"
    echo -e "  閰嶇疆鐩綍:     ${WHITE}${CONFIG_DIR}${NC}"
    echo -e "  鏃ュ織鐩綍:     ${WHITE}${LOG_DIR}${NC}"
    echo ""
    
    if [ -n "$pid" ]; then
        echo -e "${CYAN}銆愯繍琛岀姸鎬併€?{NC}"
        echo -e "  鐘舵€?         ${GREEN}杩愯涓?{NC}"
        echo -e "  杩涚▼ ID:      ${WHITE}${pid}${NC}"
        
        # 鑾峰彇鍚姩鏃堕棿
        local start_time=$(ps -o lstart= -p "$pid" 2>/dev/null | xargs)
        [ -n "$start_time" ] && echo -e "  鍚姩鏃堕棿:     ${WHITE}${start_time}${NC}"
        
        # 鑾峰彇杩愯鏃堕暱
        local etime=$(ps -o etime= -p "$pid" 2>/dev/null | xargs)
        [ -n "$etime" ] && echo -e "  杩愯鏃堕暱:     ${WHITE}${etime}${NC}"
        
        # 鑾峰彇鍐呭瓨浣跨敤
        local mem=$(ps -o rss= -p "$pid" 2>/dev/null | xargs)
        if [ -n "$mem" ]; then
            local mem_mb=$((mem / 1024))
            echo -e "  鍐呭瓨浣跨敤:     ${WHITE}${mem_mb} MB${NC}"
        fi
        
        # 鑾峰彇 CPU 浣跨敤鐜?        local cpu=$(ps -o %cpu= -p "$pid" 2>/dev/null | xargs)
        [ -n "$cpu" ] && echo -e "  CPU 浣跨敤:     ${WHITE}${cpu}%${NC}"
        
        echo ""
        echo -e "${CYAN}銆怞VM 淇℃伅銆?{NC}"
        check_java
        echo -e "  Java 璺緞:    ${WHITE}${JAVA_CMD}${NC}"
        echo -e "  Java 鐗堟湰:    ${WHITE}$(get_java_version)${NC}"
        
        # 灏濊瘯鑾峰彇 JVM 鍙傛暟
        local jvm_opts=$(ps -o args= -p "$pid" 2>/dev/null | grep -oE '\-X[^ ]+' | head -5 | tr '\n' ' ')
        [ -n "$jvm_opts" ] && echo -e "  JVM 鍙傛暟:     ${WHITE}${jvm_opts}${NC}"
    else
        echo -e "${CYAN}銆愯繍琛岀姸鎬併€?{NC}"
        echo -e "  鐘舵€?         ${YELLOW}宸插仠姝?{NC}"
    fi
    
    echo ""
    echo -e "${CYAN}銆愭棩蹇楁枃浠躲€?{NC}"
    if [ -f "${STARTUP_LOG}" ]; then
        local log_size=$(du -h "${STARTUP_LOG}" 2>/dev/null | cut -f1)
        echo -e "  鍚姩鏃ュ織:     ${WHITE}${STARTUP_LOG} (${log_size})${NC}"
    fi
    if [ -f "${APP_LOG}" ]; then
        local log_size=$(du -h "${APP_LOG}" 2>/dev/null | cut -f1)
        echo -e "  搴旂敤鏃ュ織:     ${WHITE}${APP_LOG} (${log_size})${NC}"
    fi
    echo ""
}

# ===========================================
# 鍚姩淇℃伅鏄剧ず
# ===========================================
show_startup_banner() {
    local java_version=$(get_java_version)
    local local_ip=$(get_local_ip)
    local server_port=$(get_server_port)
    local context_path=$(get_context_path)
    local swagger_enabled=$(check_swagger_enabled)
    local base_url="http://${local_ip}:${server_port}${context_path}"
    local local_url="http://localhost:${server_port}${context_path}"
    
    echo ""
    echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${BLUE}鈺?{NC}${GREEN}                     搴旂敤鍚姩涓?..                            ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    echo -e "${CYAN}鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?{NC}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}搴旂敤鍚嶇О${NC}    鈹?${GREEN}${APP_NAME}${NC}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}搴旂敤鐗堟湰${NC}    鈹?${GREEN}${APP_VERSION}${NC}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}杩愯鐜${NC}    鈹?${YELLOW}${SPRING_PROFILE}${NC}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}鏈嶅姟鍦板潃${NC}    鈹?${GREEN}${base_url}${NC}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}鏈湴鍦板潃${NC}    鈹?${GREEN}${local_url}${NC}"
    [ "${swagger_enabled}" = "true" ] && echo -e "${CYAN}鈹?{NC} ${WHITE}Swagger${NC}     鈹?${GREEN}${local_url}/doc.html${NC}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}Java 鐗堟湰${NC}   鈹?${MAGENTA}${java_version}${NC}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}Java 璺緞${NC}   鈹?${MAGENTA}${JAVA_CMD}${NC}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}JVM 鍙傛暟${NC}    鈹?${MAGENTA}${JAVA_OPTS}${NC}"
    [ "${DEBUG_MODE}" = "true" ] && echo -e "${CYAN}鈹?{NC} ${WHITE}璋冭瘯绔彛${NC}    鈹?${RED}${DEBUG_PORT}${NC}"
    [ "${SKYWALKING_ENABLED}" = "true" ] && echo -e "${CYAN}鈹?{NC} ${WHITE}Skywalking${NC}  鈹?${GREEN}宸插惎鐢?(${SW_AGENT_NAME})${NC}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}閰嶇疆鐩綍${NC}    鈹?${CONFIG_DIR}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}鏃ュ織鐩綍${NC}    鈹?${LOG_DIR}"
    echo -e "${CYAN}鈹?{NC} ${WHITE}鍚姩鏃堕棿${NC}    鈹?$(date '+%Y-%m-%d %H:%M:%S')"
    echo -e "${CYAN}鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?{NC}"
    echo ""
}

# ===========================================
# 鏃ュ織绠＄悊
# ===========================================

# 鏌ョ湅鏈€杩?N 鏉℃棩蹇?show_logs() {
    local lines="${1:-100}"
    local log_file="${STARTUP_LOG}"
    
    # 浼樺厛浣跨敤搴旂敤鏃ュ織锛屽鏋滃瓨鍦ㄧ殑璇?    [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
    
    if [ ! -f "${log_file}" ]; then
        echo -e "${YELLOW}[璀﹀憡] 鏃ュ織鏂囦欢涓嶅瓨鍦? ${log_file}${NC}"
        return 1
    fi
    
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    echo -e "${GREEN}  鏈€杩?${lines} 鏉℃棩蹇?- ${log_file}${NC}"
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    tail -n "${lines}" "${log_file}"
}

# 瀹炴椂璺熻釜鏃ュ織
tail_logs() {
    local log_file="${STARTUP_LOG}"
    
    # 浼樺厛浣跨敤搴旂敤鏃ュ織
    [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
    
    if [ ! -f "${log_file}" ]; then
        echo -e "${YELLOW}[璀﹀憡] 鏃ュ織鏂囦欢涓嶅瓨鍦? ${log_file}${NC}"
        echo -e "${YELLOW}绛夊緟鏃ュ織鏂囦欢鐢熸垚...${NC}"
        # 绛夊緟鏃ュ織鏂囦欢鐢熸垚
        local count=0
        while [ ! -f "${log_file}" ] && [ $count -lt 30 ]; do
            sleep 1
            count=$((count + 1))
            [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
        done
        if [ ! -f "${log_file}" ]; then
            echo -e "${RED}[閿欒] 绛夊緟瓒呮椂锛屾棩蹇楁枃浠朵粛涓嶅瓨鍦?{NC}"
            return 1
        fi
    fi
    
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    echo -e "${GREEN}  瀹炴椂鏃ュ織璺熻釜 - ${log_file}${NC}"
    echo -e "${YELLOW}  鎸?Ctrl+C 閫€鍑?{NC}"
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    tail -f "${log_file}"
}

# ===========================================
# Skywalking 鏀寔
# ===========================================
ensure_skywalking() {
    if [ ! -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "${YELLOW}[Skywalking] Agent 鏈畨瑁咃紝寮€濮嬭嚜鍔ㄤ笅杞?..${NC}"
        bash "${SCRIPT_DIR}/script/linux/skywalking.sh" install
        if [ $? -ne 0 ]; then
            echo -e "${RED}[閿欒] Skywalking Agent 涓嬭浇澶辫触${NC}"
            return 1
        fi
    fi
    return 0
}

run_skywalking() {
    local subcmd="${1:-}"
    bash "${SCRIPT_DIR}/script/linux/skywalking.sh" "$subcmd"
}

# ===========================================
# Java 鐜绠＄悊
# ===========================================
run_java_install() {
    local script_path="${SCRIPT_DIR}/script/linux/java-install.sh"
    if [ -f "$script_path" ]; then
        bash "$script_path" "$@"
    else
        echo -e "${RED}[閿欒] Java 瀹夎鑴氭湰涓嶅瓨鍦? ${script_path}${NC}"
        return 1
    fi
}

# ===========================================
# 搴旂敤鍚姩
# ===========================================
start_app() {
    local pid=$(get_pid)
    [ -n "$pid" ] && echo -e "${YELLOW}[璀﹀憡] ${APP_NAME} 宸插湪杩愯 (PID: ${pid})${NC}" && return 1
    
    mkdir -p "${LOG_DIR}"
    check_java
    build_loader_path
    
    # 鏋勫缓 Spring 鍚姩鍙傛暟
    local start_opts="--spring.config.location=classpath:/,file:${CONFIG_DIR}/"
    start_opts="${start_opts} --spring.profiles.active=${SPRING_PROFILE}"
    
    # 鏄剧ず鍚姩淇℃伅
    show_startup_banner
    
    local java_opts="${JAVA_OPTS}"
    [ -n "${CUSTOM_JVM_OPTS}" ] && java_opts="${CUSTOM_JVM_OPTS}"
    [ "${DEBUG_MODE}" = "true" ] && java_opts="${java_opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT}"
    
    # Skywalking 鏀寔
    if [ "${SKYWALKING_ENABLED}" = "true" ]; then
        ensure_skywalking || return 1
        java_opts="${java_opts} -javaagent:${SKYWALKING_AGENT_JAR}"
        java_opts="${java_opts} -Dskywalking.agent.service_name=${SW_AGENT_NAME}"
        java_opts="${java_opts} -Dskywalking.collector.backend_service=${SW_AGENT_COLLECTOR_BACKEND_SERVICES}"
    fi
    
    # 鍚姩搴旂敤
    nohup "$JAVA_CMD" ${java_opts} -Dloader.path="${LOADER_PATH}" -jar "${APP_HOME}/${JAR_NAME}" ${start_opts} > "${STARTUP_LOG}" 2>&1 &
    
    local new_pid=$!
    echo "$new_pid" > "${PID_FILE}"
    sleep 2
    
    if ps -p "$new_pid" > /dev/null 2>&1; then
        echo -e "${GREEN}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
        echo -e "${GREEN}鈺?                   鉁?鍚姩鎴愬姛                                鈺?{NC}"
        echo -e "${GREEN}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
        echo ""
        echo -e "  ${WHITE}杩涚▼ ID:${NC}    ${new_pid}"
        echo -e "  ${WHITE}鏃ュ織鏂囦欢:${NC}   ${STARTUP_LOG}"
        echo ""
        
        # 鏍规嵁鍙傛暟鏄剧ず鏃ュ織
        if [ "${TAIL_LOG}" = "true" ]; then
            echo -e "${CYAN}寮€濮嬪疄鏃惰窡韪棩蹇?..${NC}"
            tail_logs
        elif [ -n "${SHOW_LOGS}" ] && [ "${SHOW_LOGS}" -gt 0 ]; then
            echo -e "${CYAN}鏄剧ず鏈€杩?${SHOW_LOGS} 鏉℃棩蹇?..${NC}"
            sleep 3  # 绛夊緟涓€浜涙棩蹇楄緭鍑?            show_logs "${SHOW_LOGS}"
        fi
    else
        echo -e "${RED}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
        echo -e "${RED}鈺?                   鉁?鍚姩澶辫触                                鈺?{NC}"
        echo -e "${RED}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
        echo ""
        echo -e "${YELLOW}閿欒鏃ュ織:${NC}"
        [ -f "${STARTUP_LOG}" ] && tail -30 "${STARTUP_LOG}"
    fi
}

# ===========================================
# 搴旂敤鍋滄
# ===========================================
stop_app() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[璀﹀憡] ${APP_NAME} 鏈湪杩愯${NC}" && return 0
    
    echo -e "${BLUE}姝ｅ湪鍋滄 ${APP_NAME} (PID: ${pid})...${NC}"
    kill "$pid" 2>/dev/null
    
    local count=0
    while [ $count -lt 30 ] && ps -p "$pid" > /dev/null 2>&1; do
        sleep 1
        count=$((count + 1))
        echo -ne "\r${YELLOW}绛夊緟杩涚▼閫€鍑?.. ${count}s${NC}"
    done
    echo ""
    
    ps -p "$pid" > /dev/null 2>&1 && kill -9 "$pid" 2>/dev/null
    rm -f "${PID_FILE}"
    echo -e "${GREEN}[鎴愬姛] ${APP_NAME} 宸插仠姝?{NC}"
}

# 閲嶅惎
restart_app() { 
    stop_app
    sleep 2
    start_app
}

# ===========================================
# JVM 璇婃柇
# ===========================================
show_jvm_info() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[璀﹀憡] ${APP_NAME} 鏈湪杩愯${NC}" && return 1
    
    if [ -f "${SCRIPT_DIR}/script/linux/jvm-info.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/jvm-info.sh" "$pid" "$APP_NAME"
    else
        # 鍐呯疆绠€鍗曠殑 JVM 淇℃伅鏄剧ず
        echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
        echo -e "${BLUE}鈺?{NC}${GREEN}                    JVM 杩愯淇℃伅                              ${NC}${BLUE}鈺?{NC}"
        echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
        echo ""
        echo -e "${CYAN}杩涚▼淇℃伅:${NC}"
        echo -e "  PID: ${pid}"
        
        if command -v jps &> /dev/null; then
            echo ""
            echo -e "${CYAN}JVM 杩涚▼:${NC}"
            jps -lvm | grep "$pid" || echo "  鏃犳硶鑾峰彇璇︾粏淇℃伅"
        fi
        
        if command -v jstat &> /dev/null; then
            echo ""
            echo -e "${CYAN}GC 缁熻:${NC}"
            jstat -gcutil "$pid" 2>/dev/null || echo "  鏃犳硶鑾峰彇 GC 淇℃伅"
        fi
    fi
}

show_tuning() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[璀﹀憡] ${APP_NAME} 鏈湪杩愯${NC}" && return 1
    
    if [ -f "${SCRIPT_DIR}/script/linux/jvm-tuning.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/jvm-tuning.sh" "$pid" "$APP_NAME"
    else
        echo -e "${YELLOW}[鎻愮ず] JVM 璋冧紭鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

start_arthas() {
    local pid=$(get_pid)
    if [ -f "${SCRIPT_DIR}/script/linux/arthas-install.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/arthas-install.sh" "$pid"
    else
        echo -e "${YELLOW}[鎻愮ず] Arthas 鑴氭湰涓嶅瓨鍦紝灏濊瘯鐩存帴鍚姩...${NC}"
        if [ -f "${APP_HOME}/arthas/as.sh" ]; then
            bash "${APP_HOME}/arthas/as.sh" "$pid"
        else
            echo -e "${RED}[閿欒] Arthas 鏈畨瑁?{NC}"
        fi
    fi
}

run_analyze() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[璀﹀憡] ${APP_NAME} 鏈湪杩愯${NC}" && return 1
    local mode="${1:-cpu}"
    
    if [ -f "${SCRIPT_DIR}/script/linux/cpu-analysis.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/cpu-analysis.sh" "$pid" "$mode" "$APP_NAME"
    else
        echo -e "${YELLOW}[鎻愮ず] 鍒嗘瀽鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

# ===========================================
# Dump 杞偍鍔熻兘
# ===========================================
run_dump() {
    local mode="${1:-all}"
    local output_dir="${2:-${LOG_DIR}/dump}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[閿欒] ${APP_NAME} 鏈湪杩愯${NC}"
        exit 1
    fi
    
    mkdir -p "${output_dir}"
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    
    echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${BLUE}鈺?{NC}${GREEN}                    鐢熸垚 Dump 杞偍                            ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    echo -e "  PID:        ${pid}"
    echo -e "  妯″紡:       ${mode}"
    echo -e "  杈撳嚭鐩綍:   ${output_dir}"
    echo ""
    
    local jmap_cmd="jmap"
    local jstack_cmd="jstack"
    if [ -n "$JAVA_HOME" ]; then
        jmap_cmd="$JAVA_HOME/bin/jmap"
        jstack_cmd="$JAVA_HOME/bin/jstack"
    fi
    
    case "${mode}" in
        heap)
            dump_heap "${pid}" "${output_dir}" "${timestamp}" "${jmap_cmd}"
            ;;
        thread)
            dump_thread "${pid}" "${output_dir}" "${timestamp}" "${jstack_cmd}"
            ;;
        all)
            dump_heap "${pid}" "${output_dir}" "${timestamp}" "${jmap_cmd}"
            dump_thread "${pid}" "${output_dir}" "${timestamp}" "${jstack_cmd}"
            ;;
        *)
            echo -e "${RED}[閿欒] 鏈煡鐨?dump 妯″紡: ${mode}${NC}"
            echo "鏀寔鐨勬ā寮? heap, thread, all"
            exit 1
            ;;
    esac
    
    echo -e "\n${GREEN}[瀹屾垚] Dump 鏂囦欢宸蹭繚瀛樺埌: ${output_dir}${NC}"
}

dump_heap() {
    local pid="$1"
    local output_dir="$2"
    local timestamp="$3"
    local jmap_cmd="$4"
    local heap_file="${output_dir}/heapdump_${timestamp}.hprof"
    
    echo -e "${BLUE}[鍫嗚浆鍌╙ 鐢熸垚涓?..${NC}"
    
    if ! command -v "${jmap_cmd}" &> /dev/null; then
        echo -e "${RED}[閿欒] 鏈壘鍒?jmap 鍛戒护${NC}"
        return 1
    fi
    
    "${jmap_cmd}" -dump:format=b,file="${heap_file}" "${pid}" 2>&1
    
    if [ $? -eq 0 ] && [ -f "${heap_file}" ]; then
        local size=$(du -h "${heap_file}" | cut -f1)
        echo -e "${GREEN}  鉁?鍫嗚浆鍌? ${heap_file} (${size})${NC}"
    else
        echo -e "${RED}  鉁?鍫嗚浆鍌ㄥけ璐?{NC}"
    fi
}

dump_thread() {
    local pid="$1"
    local output_dir="$2"
    local timestamp="$3"
    local jstack_cmd="$4"
    local thread_file="${output_dir}/threaddump_${timestamp}.txt"
    
    echo -e "${BLUE}[绾跨▼杞偍] 鐢熸垚涓?..${NC}"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        echo -e "${RED}[閿欒] 鏈壘鍒?jstack 鍛戒护${NC}"
        return 1
    fi
    
    "${jstack_cmd}" -l "${pid}" > "${thread_file}" 2>&1
    
    if [ $? -eq 0 ] && [ -f "${thread_file}" ]; then
        local lines=$(wc -l < "${thread_file}")
        echo -e "${GREEN}  鉁?绾跨▼杞偍: ${thread_file} (${lines} 琛?${NC}"
        
        echo -e "\n${CYAN}[绾跨▼鐘舵€佺粺璁${NC}"
        echo -e "  RUNNABLE:      $(grep -c 'java.lang.Thread.State: RUNNABLE' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  WAITING:       $(grep -c 'java.lang.Thread.State: WAITING' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  TIMED_WAITING: $(grep -c 'java.lang.Thread.State: TIMED_WAITING' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  BLOCKED:       $(grep -c 'java.lang.Thread.State: BLOCKED' "${thread_file}" 2>/dev/null || echo 0)"
        
        if grep -q "Found.*deadlock" "${thread_file}" 2>/dev/null; then
            echo -e "\n${RED}[璀﹀憡] 妫€娴嬪埌姝婚攣!${NC}"
        fi
    else
        echo -e "${RED}  鉁?绾跨▼杞偍澶辫触${NC}"
    fi
}

# ===========================================
# 澶囦唤杩樺師
# ===========================================
run_backup() {
    if [ -f "${SCRIPT_DIR}/script/linux/backup-restore.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/backup-restore.sh" backup "${1:-all}" "$APP_NAME" "$JAR_NAME"
    else
        echo -e "${YELLOW}[鎻愮ず] 澶囦唤鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

run_restore() {
    if [ -f "${SCRIPT_DIR}/script/linux/backup-restore.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/backup-restore.sh" restore "${1:-all}" "$APP_NAME" "$JAR_NAME"
    else
        echo -e "${YELLOW}[鎻愮ず] 杩樺師鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

# ===========================================
# 鍗囩骇鍥炴粴
# ===========================================
run_upgrade() {
    local package_path="$1"
    local no_restart="$2"
    local opts=""
    
    [ -n "${package_path}" ] && opts="${opts} --package ${package_path}"
    [ "${no_restart}" = "true" ] && opts="${opts} --no-restart"
    
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" upgrade ${opts}
    else
        echo -e "${YELLOW}[鎻愮ず] 鍗囩骇鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

run_rollback() {
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" rollback
    else
        echo -e "${YELLOW}[鎻愮ず] 鍥炴粴鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

run_rollback_status() {
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" status
    else
        echo -e "${YELLOW}[鎻愮ず] 鍥炴粴鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

# ===========================================
# Docker 鐩稿叧鍑芥暟
# ===========================================
docker_build() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" build --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[鎻愮ず] Docker 鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

docker_deploy() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" deploy --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[鎻愮ず] Docker 鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

docker_start_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" start --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[鎻愮ず] Docker 鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

docker_stop_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" stop --name "$APP_NAME"
    else
        echo -e "${YELLOW}[鎻愮ず] Docker 鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

docker_restart_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" restart --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[鎻愮ず] Docker 鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

docker_status() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" status --name "$APP_NAME"
    else
        echo -e "${YELLOW}[鎻愮ず] Docker 鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

docker_logs() {
    local lines="${1:-100}"
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" logs --name "$APP_NAME" --lines "$lines"
    else
        echo -e "${YELLOW}[鎻愮ず] Docker 鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

docker_clean() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" clean --name "$APP_NAME" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[鎻愮ず] Docker 鑴氭湰涓嶅瓨鍦?{NC}"
    fi
}

# ===========================================
# GC 瑙﹀彂
# ===========================================
run_gc() {
    local gc_type="${1:-full}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[閿欒] ${APP_NAME} 鏈湪杩愯${NC}"
        return 1
    fi
    
    check_java
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    if ! command -v "${jcmd_cmd}" &> /dev/null; then
        echo -e "${RED}[閿欒] 鏈壘鍒?jcmd 鍛戒护${NC}"
        return 1
    fi
    
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    echo -e "${GREEN}                    瑙﹀彂 GC${NC}"
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    echo ""
    echo -e "  PID:      ${pid}"
    echo -e "  GC 绫诲瀷: ${gc_type}"
    echo ""
    
    # GC 鍓嶇姸鎬?    echo -e "${CYAN}[GC 鍓嶇姸鎬乚${NC}"
    local jstat_cmd="jstat"
    [ -n "$JAVA_HOME" ] && jstat_cmd="$JAVA_HOME/bin/jstat"
    if command -v "${jstat_cmd}" &> /dev/null; then
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    echo ""
    
    case "${gc_type}" in
        young|minor)
            echo -e "${YELLOW}瑙﹀彂 Young GC...${NC}"
            "${jcmd_cmd}" "$pid" GC.run 2>/dev/null
            ;;
        full|major)
            echo -e "${YELLOW}瑙﹀彂 Full GC...${NC}"
            "${jcmd_cmd}" "$pid" GC.run 2>/dev/null
            ;;
        *)
            echo -e "${RED}[閿欒] 鏈煡鐨?GC 绫诲瀷: ${gc_type}${NC}"
            echo "鏀寔鐨勭被鍨? young, full"
            return 1
            ;;
    esac
    
    sleep 2
    
    # GC 鍚庣姸鎬?    echo ""
    echo -e "${CYAN}[GC 鍚庣姸鎬乚${NC}"
    if command -v "${jstat_cmd}" &> /dev/null; then
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    
    echo ""
    echo -e "${GREEN}[瀹屾垚] GC 宸茶Е鍙?{NC}"
}

# ===========================================
# 楂?CPU 绾跨▼鍒嗘瀽
# ===========================================
run_top_cpu() {
    local top_n="${1:-10}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[閿欒] ${APP_NAME} 鏈湪杩愯${NC}"
        return 1
    fi
    
    check_java
    local jstack_cmd="jstack"
    [ -n "$JAVA_HOME" ] && jstack_cmd="$JAVA_HOME/bin/jstack"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        echo -e "${RED}[閿欒] 鏈壘鍒?jstack 鍛戒护${NC}"
        return 1
    fi
    
    echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${BLUE}鈺?{NC}${GREEN}                楂?CPU 绾跨▼鍒嗘瀽 (Top ${top_n})                  ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    echo -e "  杩涚▼ PID: ${pid}"
    echo -e "  搴旂敤鍚嶇О: ${APP_NAME}"
    echo ""
    
    # 鑾峰彇楂?CPU 绾跨▼
    echo -e "${CYAN}[姝ラ 1] 鑾峰彇楂?CPU 绾跨▼...${NC}"
    local tmp_threads=$(mktemp)
    local tmp_jstack=$(mktemp)
    
    # 鑾峰彇绾跨▼ CPU 浣跨敤鐜?    top -b -n 1 -H -p "$pid" 2>/dev/null | grep -E "^\s*[0-9]+" | head -n "$top_n" > "$tmp_threads"
    
    if [ ! -s "$tmp_threads" ]; then
        echo -e "${YELLOW}[璀﹀憡] 鏃犳硶閫氳繃 top 鑾峰彇绾跨▼淇℃伅锛屽皾璇?ps...${NC}"
        ps -eLo pid,lwp,%cpu,comm --sort=-%cpu | grep "$pid" | head -n "$top_n" > "$tmp_threads"
    fi
    
    # 鑾峰彇绾跨▼鍫嗘爤
    echo -e "${CYAN}[姝ラ 2] 鑾峰彇绾跨▼鍫嗘爤...${NC}"
    "${jstack_cmd}" -l "$pid" > "$tmp_jstack" 2>/dev/null
    
    if [ ! -s "$tmp_jstack" ]; then
        echo -e "${RED}[閿欒] 鏃犳硶鑾峰彇绾跨▼鍫嗘爤${NC}"
        rm -f "$tmp_threads" "$tmp_jstack"
        return 1
    fi
    
    # 鍒嗘瀽骞舵樉绀?    echo ""
    echo -e "${CYAN}[姝ラ 3] 鍒嗘瀽楂?CPU 绾跨▼鍫嗘爤...${NC}"
    echo -e "${BLUE}鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€${NC}"
    
    local count=0
    while read -r line; do
        # 瑙ｆ瀽绾跨▼ ID 鍜?CPU
        local lwp=$(echo "$line" | awk '{print $1}')
        local cpu=$(echo "$line" | awk '{print $9}')
        
        # 濡傛灉鏄?ps 杈撳嚭鏍煎紡
        if [[ "$line" =~ ^[[:space:]]*[0-9]+[[:space:]]+[0-9]+ ]]; then
            lwp=$(echo "$line" | awk '{print $2}')
            cpu=$(echo "$line" | awk '{print $3}')
        fi
        
        [ -z "$lwp" ] && continue
        
        # 杞崲涓?16 杩涘埗
        local nid=$(printf "%x" "$lwp" 2>/dev/null)
        [ -z "$nid" ] && continue
        
        count=$((count + 1))
        echo ""
        echo -e "${YELLOW}#${count} 绾跨▼ ID: ${lwp} (0x${nid})  CPU: ${cpu}%${NC}"
        
        # 鍦?jstack 涓煡鎵惧搴旂殑绾跨▼鍫嗘爤
        local found=false
        local in_thread=false
        local stack_lines=0
        
        while IFS= read -r stack_line; do
            if [[ "$stack_line" =~ nid=0x${nid}[[:space:]] ]]; then
                in_thread=true
                found=true
                echo -e "${WHITE}$stack_line${NC}"
            elif [ "$in_thread" = true ]; then
                if [[ "$stack_line" =~ ^[[:space:]]*$ ]] || [[ "$stack_line" =~ ^\".*\".*nid= ]]; then
                    break
                fi
                # 鏄剧ず鍫嗘爤 (鏈€澶?15 琛?
                if [ $stack_lines -lt 15 ]; then
                    # 楂樹寒鏄剧ず涓氬姟浠ｇ爜
                    if [[ "$stack_line" =~ at\ com\. ]] || [[ "$stack_line" =~ at\ cn\. ]] || [[ "$stack_line" =~ at\ org\..*\.service ]] || [[ "$stack_line" =~ at\ org\..*\.controller ]]; then
                        echo -e "${GREEN}$stack_line${NC}"
                    else
                        echo "$stack_line"
                    fi
                    stack_lines=$((stack_lines + 1))
                fi
            fi
        done < "$tmp_jstack"
        
        if [ "$found" = false ]; then
            echo -e "${YELLOW}  鏃犳硶鎵惧埌绾跨▼鍫嗘爤 (nid=0x${nid})${NC}"
        fi
        
        echo -e "${BLUE}鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€${NC}"
        
        [ $count -ge $top_n ] && break
    done < "$tmp_threads"
    
    rm -f "$tmp_threads" "$tmp_jstack"
    
    echo ""
    echo -e "${GREEN}[瀹屾垚] 鍒嗘瀽缁撴潫${NC}"
    echo -e "${CYAN}鎻愮ず: 缁胯壊楂樹寒鐨勬槸涓氬姟浠ｇ爜锛岄€氬父闇€瑕侀噸鐐瑰叧娉?{NC}"
}

# ===========================================
# 楂樺唴瀛樺璞″垎鏋?# ===========================================
run_top_mem() {
    local top_n="${1:-10}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[閿欒] ${APP_NAME} 鏈湪杩愯${NC}"
        return 1
    fi
    
    check_java
    local jmap_cmd="jmap"
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jmap_cmd="$JAVA_HOME/bin/jmap" && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    echo -e "${BLUE}鈺斺晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晽${NC}"
    echo -e "${BLUE}鈺?{NC}${GREEN}               楂樺唴瀛樺璞″垎鏋?(Top ${top_n})                   ${NC}${BLUE}鈺?{NC}"
    echo -e "${BLUE}鈺氣晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨暆${NC}"
    echo ""
    echo -e "  杩涚▼ PID: ${pid}"
    echo -e "  搴旂敤鍚嶇О: ${APP_NAME}"
    echo ""
    
    # 灏濊瘯浣跨敤 jcmd (鏇村畨鍏?
    local histo_output=""
    if command -v "${jcmd_cmd}" &> /dev/null; then
        echo -e "${CYAN}[姝ラ 1] 浣跨敤 jcmd 鑾峰彇瀵硅薄鐩存柟鍥?..${NC}"
        histo_output=$("${jcmd_cmd}" "$pid" GC.class_histogram 2>/dev/null)
    fi
    
    # 濡傛灉 jcmd 澶辫触锛屽皾璇?jmap
    if [ -z "$histo_output" ] && command -v "${jmap_cmd}" &> /dev/null; then
        echo -e "${CYAN}[姝ラ 1] 浣跨敤 jmap 鑾峰彇瀵硅薄鐩存柟鍥?..${NC}"
        histo_output=$("${jmap_cmd}" -histo:live "$pid" 2>/dev/null)
    fi
    
    if [ -z "$histo_output" ]; then
        echo -e "${RED}[閿欒] 鏃犳硶鑾峰彇瀵硅薄鐩存柟鍥?{NC}"
        return 1
    fi
    
    echo ""
    echo -e "${CYAN}[姝ラ 2] 鍒嗘瀽楂樺唴瀛樺璞?..${NC}"
    echo ""
    
    # 鏄剧ず鏍囬
    echo -e "${WHITE}  鎺掑悕   瀹炰緥鏁?         鍗犵敤瀛楄妭            绫诲悕${NC}"
    echo -e "${BLUE}鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€${NC}"
    
    # 瑙ｆ瀽骞舵樉绀?Top N
    local count=0
    echo "$histo_output" | grep -E "^\s*[0-9]+:" | head -n "$top_n" | while read -r line; do
        count=$((count + 1))
        local num=$(echo "$line" | awk '{print $1}' | tr -d ':')
        local instances=$(echo "$line" | awk '{print $2}')
        local bytes=$(echo "$line" | awk '{print $3}')
        local class_name=$(echo "$line" | awk '{print $4}')
        
        # 杞崲瀛楄妭涓哄彲璇绘牸寮?        local size_human=""
        if [ "$bytes" -ge 1073741824 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f GB\", $bytes/1073741824}")
        elif [ "$bytes" -ge 1048576 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f MB\", $bytes/1048576}")
        elif [ "$bytes" -ge 1024 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f KB\", $bytes/1024}")
        else
            size_human="${bytes} B"
        fi
        
        # 楂樹寒涓氬姟绫?        if [[ "$class_name" =~ ^(com\.|cn\.|org\..*\.model|org\..*\.entity|org\..*\.dto) ]]; then
            printf "${GREEN}  %4s   %12s   %12s   %s${NC}\n" "$num" "$instances" "$size_human" "$class_name"
        else
            printf "  %4s   %12s   %12s   %s\n" "$num" "$instances" "$size_human" "$class_name"
        fi
    done
    
    echo -e "${BLUE}鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€${NC}"
    
    # 鏄剧ず鍐呭瓨姒傝
    echo ""
    echo -e "${CYAN}[鍐呭瓨姒傝]${NC}"
    local total_instances=$(echo "$histo_output" | grep -E "^Total" | awk '{print $2}')
    local total_bytes=$(echo "$histo_output" | grep -E "^Total" | awk '{print $3}')
    if [ -n "$total_instances" ]; then
        local total_human=""
        if [ "$total_bytes" -ge 1073741824 ]; then
            total_human=$(awk "BEGIN {printf \"%.2f GB\", $total_bytes/1073741824}")
        elif [ "$total_bytes" -ge 1048576 ]; then
            total_human=$(awk "BEGIN {printf \"%.2f MB\", $total_bytes/1048576}")
        else
            total_human=$(awk "BEGIN {printf \"%.2f KB\", $total_bytes/1024}")
        fi
        echo -e "  鎬诲疄渚嬫暟: ${WHITE}${total_instances}${NC}"
        echo -e "  鎬诲崰鐢?   ${WHITE}${total_human}${NC}"
    fi
    
    echo ""
    echo -e "${GREEN}[瀹屾垚] 鍒嗘瀽缁撴潫${NC}"
    echo -e "${CYAN}鎻愮ず: 缁胯壊楂樹寒鐨勬槸涓氬姟绫伙紝閫氬父闇€瑕侀噸鐐瑰叧娉?{NC}"
    echo -e "${CYAN}      濡傞渶鏇磋缁嗗垎鏋愶紝璇蜂娇鐢? $0 dump heap${NC}"
}

# ===========================================
# 鏈嶅姟绠＄悊 (systemd)
# ===========================================
SERVICE_NAME="${APP_NAME}.service"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}"

install_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[閿欒] 闇€瑕?root 鏉冮檺鎵ц姝ゅ懡浠?{NC}"
        echo "璇蜂娇鐢? sudo $0 install-service"
        return 1
    fi
    
    if [ -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[璀﹀憡] 鏈嶅姟宸插瓨鍦? ${SERVICE_NAME}${NC}"
        read -p "鏄惁瑕嗙洊? (y/N): " confirm
        [ "$confirm" != "y" ] && [ "$confirm" != "Y" ] && return 0
    fi
    
    check_java
    local java_path=$(which "$JAVA_CMD" 2>/dev/null || echo "$JAVA_CMD")
    local run_user=$(stat -c '%U' "${APP_HOME}" 2>/dev/null || echo "root")
    
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    echo -e "${GREEN}              瀹夎 Systemd 鏈嶅姟${NC}"
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    echo ""
    echo -e "  鏈嶅姟鍚嶇О: ${SERVICE_NAME}"
    echo -e "  杩愯鐢ㄦ埛: ${run_user}"
    echo -e "  Java 璺緞: ${java_path}"
    echo -e "  搴旂敤鐩綍: ${APP_HOME}"
    echo ""
    
    # 鐢熸垚 systemd 鏈嶅姟鏂囦欢
    cat > "${SERVICE_FILE}" << EOF
[Unit]
Description=${APP_NAME} Spring Boot Application
After=network.target

[Service]
Type=simple
User=${run_user}
Group=${run_user}
WorkingDirectory=${APP_HOME}
Environment="JAVA_HOME=$(dirname $(dirname $java_path))"
Environment="SPRING_PROFILE=${SPRING_PROFILE}"
ExecStart=${SCRIPT_DIR}/startup.sh start
ExecStop=${SCRIPT_DIR}/startup.sh stop
ExecReload=/bin/kill -HUP \$MAINPID
Restart=on-failure
RestartSec=10
SuccessExitStatus=143
TimeoutStopSec=30
KillMode=process

[Install]
WantedBy=multi-user.target
EOF
    
    # 閲嶆柊鍔犺浇 systemd
    systemctl daemon-reload
    
    echo -e "${GREEN}[鎴愬姛] 鏈嶅姟宸插畨瑁? ${SERVICE_NAME}${NC}"
    echo ""
    echo -e "${CYAN}鍙敤鍛戒护:${NC}"
    echo "  systemctl start ${SERVICE_NAME}     # 鍚姩鏈嶅姟"
    echo "  systemctl stop ${SERVICE_NAME}      # 鍋滄鏈嶅姟"
    echo "  systemctl restart ${SERVICE_NAME}   # 閲嶅惎鏈嶅姟"
    echo "  systemctl status ${SERVICE_NAME}    # 鏌ョ湅鐘舵€?
    echo "  systemctl enable ${SERVICE_NAME}    # 寮€鏈鸿嚜鍚?
    echo "  journalctl -u ${SERVICE_NAME} -f    # 鏌ョ湅鏃ュ織"
}

uninstall_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[閿欒] 闇€瑕?root 鏉冮檺鎵ц姝ゅ懡浠?{NC}"
        echo "璇蜂娇鐢? sudo $0 uninstall-service"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[璀﹀憡] 鏈嶅姟涓嶅瓨鍦? ${SERVICE_NAME}${NC}"
        return 0
    fi
    
    echo -e "${BLUE}鍗歌浇鏈嶅姟: ${SERVICE_NAME}...${NC}"
    
    # 鍋滄鏈嶅姟
    systemctl stop "${SERVICE_NAME}" 2>/dev/null
    systemctl disable "${SERVICE_NAME}" 2>/dev/null
    
    # 鍒犻櫎鏈嶅姟鏂囦欢
    rm -f "${SERVICE_FILE}"
    systemctl daemon-reload
    
    echo -e "${GREEN}[鎴愬姛] 鏈嶅姟宸插嵏杞?{NC}"
}

enable_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[閿欒] 闇€瑕?root 鏉冮檺鎵ц姝ゅ懡浠?{NC}"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${RED}[閿欒] 鏈嶅姟鏈畨瑁咃紝璇峰厛鎵ц: $0 install-service${NC}"
        return 1
    fi
    
    systemctl enable "${SERVICE_NAME}"
    echo -e "${GREEN}[鎴愬姛] 宸插惎鐢ㄥ紑鏈鸿嚜鍚?{NC}"
}

disable_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[閿欒] 闇€瑕?root 鏉冮檺鎵ц姝ゅ懡浠?{NC}"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[璀﹀憡] 鏈嶅姟鏈畨瑁?{NC}"
        return 0
    fi
    
    systemctl disable "${SERVICE_NAME}"
    echo -e "${GREEN}[鎴愬姛] 宸茬鐢ㄥ紑鏈鸿嚜鍚?{NC}"
}

show_service_status() {
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[璀﹀憡] 鏈嶅姟鏈畨瑁? ${SERVICE_NAME}${NC}"
        echo "浣跨敤 '$0 install-service' 瀹夎鏈嶅姟"
        return 0
    fi
    
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    echo -e "${GREEN}              Systemd 鏈嶅姟鐘舵€?{NC}"
    echo -e "${BLUE}鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲鈺愨晲${NC}"
    echo ""
    systemctl status "${SERVICE_NAME}" --no-pager
}

# ===========================================
# 鍙傛暟瑙ｆ瀽
# ===========================================
COMMAND="start"
DEBUG_PORT="${DEFAULT_DEBUG_PORT}"
DEBUG_MODE="false"
CUSTOM_JVM_OPTS=""
CUSTOM_JAVA=""
TAIL_LOG="false"
SKYWALKING_ENABLED="false"
SHOW_LOGS=""
JAVA_CMD_ARGS=()

TOP_N="10"

while [ $# -gt 0 ]; do
    case "$1" in
        start|stop|restart|status|info|jvm|tuning|analyze|dump|arthas|backup|restore|upgrade|rollback|rollback-status|debug|skywalking)
            COMMAND="$1"; shift ;;
        gc)
            COMMAND="gc"
            shift
            # 妫€鏌ヤ笅涓€涓弬鏁版槸鍚︽槸 GC 绫诲瀷
            if [[ "$1" =~ ^(young|full|minor|major)$ ]]; then
                GC_TYPE="$1"
                shift
            else
                GC_TYPE="full"
            fi
            ;;
        top-cpu)
            COMMAND="top-cpu"
            shift
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                TOP_N="$1"
                shift
            fi
            ;;
        top-mem)
            COMMAND="top-mem"
            shift
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                TOP_N="$1"
                shift
            fi
            ;;
        install-service|uninstall-service|enable-service|disable-service|service-status)
            COMMAND="$1"; shift ;;
        logs)
            COMMAND="logs"
            shift
            # 妫€鏌ヤ笅涓€涓弬鏁版槸鍚︽槸鏁板瓧
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                SHOW_LOGS="$1"
                shift
            else
                SHOW_LOGS="100"
            fi
            ;;
        tail)
            COMMAND="tail"; shift ;;
        docker-build|docker-deploy|docker-start|docker-stop|docker-restart|docker-status|docker-logs|docker-clean|publish)
            COMMAND="$1"; shift ;;
        java)
            COMMAND="java"
            shift
            # 淇濆瓨 java 鍛戒护鐨勬墍鏈夊悗缁弬鏁?            JAVA_CMD_ARGS=("$@")
            break
            ;;
        young|full|minor|major)
            GC_TYPE="$1"; shift ;;
        cpu|mem|all|config|jar|heap|thread)
            EXTRA_MODE="$1"; shift ;;
        --dev) SPRING_PROFILE="dev"; shift ;;
        --prod) SPRING_PROFILE="prod"; shift ;;
        --test) SPRING_PROFILE="test"; shift ;;
        --profile) SPRING_PROFILE="$2"; shift 2 ;;
        --java)
            shift
            # 妫€鏌ヤ笅涓€涓弬鏁版槸鍚︽槸璺緞 (涓嶄互 - 寮€澶?
            if [ -n "$1" ] && [[ ! "$1" =~ ^- ]]; then
                CUSTOM_JAVA="$1"
                # 淇濆瓨 Java 璺緞
                if [ -x "$CUSTOM_JAVA" ]; then
                    save_java_path "$CUSTOM_JAVA"
                fi
                shift
            else
                # 鏃犲弬鏁帮紝鏄剧ず褰撳墠 Java 淇℃伅
                show_java_info
                exit 0
            fi
            ;;
        --jvm-opts) CUSTOM_JVM_OPTS="$2"; shift 2 ;;
        --debug-port) DEBUG_PORT="$2"; shift 2 ;;
        --docker-port) DOCKER_PORT="$2"; shift 2 ;;
        --host-port) DOCKER_HOST_PORT="$2"; shift 2 ;;
        --image-tag) DOCKER_IMAGE_TAG="$2"; shift 2 ;;
        --skywalking) SKYWALKING_ENABLED="true"; shift ;;
        --sw-backend) SW_AGENT_COLLECTOR_BACKEND_SERVICES="$2"; shift 2 ;;
        --sw-name) SW_AGENT_NAME="$2"; shift 2 ;;
        --package) UPGRADE_PACKAGE="$2"; shift 2 ;;
        --no-restart) NO_RESTART="true"; shift ;;
        --output|--dump-path) DUMP_OUTPUT="$2"; shift 2 ;;
        --logs)
            if [[ "$2" =~ ^[0-9]+$ ]]; then
                SHOW_LOGS="$2"
                shift 2
            else
                SHOW_LOGS="100"
                shift
            fi
            ;;
        --tail) TAIL_LOG="true"; shift ;;
        --help|-h) show_help; exit 0 ;;
        *)
            # 妫€鏌ユ槸鍚︽槸鏁板瓧 (鐢ㄤ簬 logs/top-cpu/top-mem 鍛戒护)
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                case "$COMMAND" in
                    logs) SHOW_LOGS="$1" ;;
                    top-cpu|top-mem) TOP_N="$1" ;;
                    *) echo -e "${RED}[閿欒] 鏈煡鍙傛暟: $1${NC}"; show_help; exit 1 ;;
                esac
                shift
            else
                echo -e "${RED}[閿欒] 鏈煡鍙傛暟: $1${NC}"
                show_help
                exit 1
            fi
            ;;
    esac
done

[ "$COMMAND" = "debug" ] && DEBUG_MODE="true" && COMMAND="start"

# ===========================================
# 鍛戒护鎵ц
# ===========================================
case "$COMMAND" in
    start) start_app ;;
    stop) stop_app ;;
    restart) restart_app ;;
    status) check_status ;;
    info) show_info ;;
    logs) show_logs "${SHOW_LOGS:-100}" ;;
    tail) tail_logs ;;
    jvm) show_jvm_info ;;
    gc) run_gc "${GC_TYPE:-full}" ;;
    tuning) show_tuning ;;
    arthas) start_arthas ;;
    analyze) run_analyze "${EXTRA_MODE:-cpu}" ;;
    top-cpu) run_top_cpu "${TOP_N:-10}" ;;
    top-mem) run_top_mem "${TOP_N:-10}" ;;
    dump) run_dump "${EXTRA_MODE:-all}" "${DUMP_OUTPUT}" ;;
    backup) run_backup "${EXTRA_MODE:-all}" ;;
    restore) run_restore "${EXTRA_MODE:-all}" ;;
    upgrade) run_upgrade "${UPGRADE_PACKAGE}" "${NO_RESTART}" ;;
    rollback) run_rollback ;;
    rollback-status) run_rollback_status ;;
    skywalking) run_skywalking "${EXTRA_MODE:-}" ;;
    java) run_java_install "${JAVA_CMD_ARGS[@]}" ;;
    install-service) install_service ;;
    uninstall-service) uninstall_service ;;
    enable-service) enable_service ;;
    disable-service) disable_service ;;
    service-status) show_service_status ;;
    docker-build) docker_build ;;
    docker-deploy|publish) docker_deploy ;;
    docker-start) docker_start_container ;;
    docker-stop) docker_stop_container ;;
    docker-restart) docker_restart_container ;;
    docker-status) docker_status ;;
    docker-logs) docker_logs "${EXTRA_MODE:-100}" ;;
    docker-clean) docker_clean ;;
    *) show_help; exit 1 ;;
esac
