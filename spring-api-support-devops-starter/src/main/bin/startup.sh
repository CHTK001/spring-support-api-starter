#!/bin/bash
# ============================================================
# 缂佺喍绔存惔鏃傛暏缁狅紕鎮婇懘姘拱
# 閺€顖涘瘮閸斻劍鈧礁鎮曠粔鐗堟禌閹?(Maven 鐠у嫭绨潻鍥ㄦ姢)
# 娴ｆ粏鈧? DevOps
# 閻楀牊婀? 2.0.0
# ============================================================

# ===========================================
# 閸╄櫣顢呴柊宥囩枂 (Maven 鐠у嫭绨潻鍥ㄦ姢閺囨寧宕?
# ===========================================
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_NAME="${APP_NAME:-@project.artifactId@}"
APP_VERSION="${APP_VERSION:-@project.version@}"
JAR_NAME="${APP_NAME}-${APP_VERSION}.jar"

# 閻╊喖缍嶉柊宥囩枂
CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
LOG_DIR="${APP_HOME}/logs"
PID_FILE="${APP_HOME}/${APP_NAME}.pid"
BACKUP_DIR="${APP_HOME}/backup"

# 閺冦儱绻旈弬鍥︽闁板秶鐤?STARTUP_LOG="${LOG_DIR}/startup.log"
APP_LOG="${LOG_DIR}/${APP_NAME}.log"

# 鐠嬪啳鐦粩顖氬經闁板秶鐤?DEFAULT_DEBUG_PORT=5005

# Docker 闁板秶鐤?DOCKER_REGISTRY="${DOCKER_REGISTRY:-}"
DOCKER_IMAGE_NAME="${DOCKER_IMAGE_NAME:-${APP_NAME}}"
DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG:-${APP_VERSION}}"
DOCKER_CONTAINER_NAME="${DOCKER_CONTAINER_NAME:-${APP_NAME}}"
DOCKER_PORT="${DOCKER_PORT:-8080}"
DOCKER_HOST_PORT="${DOCKER_HOST_PORT:-8080}"

# Skywalking 闁板秶鐤?SKYWALKING_VERSION="${SKYWALKING_VERSION:-9.0.0}"
SKYWALKING_DIR="${APP_HOME}/skywalking"
SKYWALKING_AGENT_JAR="${SKYWALKING_DIR}/skywalking-agent/skywalking-agent.jar"
SW_AGENT_NAME="${SW_AGENT_NAME:-${APP_NAME}}"
SW_AGENT_COLLECTOR_BACKEND_SERVICES="${SW_AGENT_COLLECTOR_BACKEND_SERVICES:-127.0.0.1:11800}"

# JVM闁板秶鐤?JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}"
JAVA_OPTS="${JAVA_OPTS} -Dlogging.file.path=${LOG_DIR}"

# Spring闁板秶鐤?SPRING_PROFILE="${SPRING_PROFILE:-prod}"

# 閺堝秴濮熺粩顖氬經闁板秶鐤?SERVER_PORT="${SERVER_PORT:-8080}"

# 妫版粏澹婄€规矮绠?RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
WHITE='\033[1;37m'
NC='\033[0m'

# ===========================================
# 鐢喖濮穱鈩冧紖
# ===========================================
show_help() {
    echo -e "${BLUE}閳烘柡鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫧${NC}"
    echo -e "${BLUE}閳?{NC}${GREEN}              ${APP_NAME} 缁狅紕鎮婇懘姘拱                    ${NC}${BLUE}閳?{NC}"
    echo -e "${BLUE}閳?{NC}${CYAN}              閻楀牊婀? ${APP_VERSION}                              ${NC}${BLUE}閳?{NC}"
    echo -e "${BLUE}閳烘埃鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ殕${NC}"
    echo ""
    echo -e "${YELLOW}閻劍纭?${NC} $0 [閸涙垝鎶 [闁銆峕"
    echo ""
    echo -e "${YELLOW}閸╄櫣顢呴崨鎴掓姢:${NC}"
    echo "  start              閸氼垰濮╂惔鏃傛暏 (姒涙顓?"
    echo "  stop               閸嬫粍顒涙惔鏃傛暏"
    echo "  restart            闁插秴鎯庢惔鏃傛暏"
    echo "  status             閺屻儳婀呮惔鏃傛暏閻樿埖鈧?
    echo "  info               閺勫墽銇氱拠锔剧矎鏉╂劘顢戞穱鈩冧紖"
    echo ""
    echo -e "${YELLOW}閺冦儱绻旈崨鎴掓姢:${NC}"
    echo "  logs [N]           閺屻儳婀呴張鈧潻?N 閺夆剝妫╄箛?(姒涙顓? 100)"
    echo "  tail               鐎圭偞妞傜捄鐔婚嚋閺冦儱绻旀潏鎾冲毉"
    echo ""
    echo -e "${YELLOW}鐠囧﹥鏌囬崨鎴掓姢:${NC}"
    echo "  jvm                閺屻儳婀匤VM娣団剝浼?
    echo "  gc                 鐟欙箑褰?GC (young/full)"
    echo "  tuning             JVM鐠嬪啩绱楦款唴"
    echo "  analyze            CPU/閸愬懎鐡ㄩ崚鍡樼€?
    echo "  top-cpu            閸掑棙鐎芥妤U缁捐法鈻奸崣濠佸敩閻礁鐖㈤弽?(Top N)"
    echo "  top-mem            閸掑棙鐎芥妯哄敶鐎涙ê顕挒?(Top N)"
    echo "  dump               閻㈢喐鍨氭潪顒€鍋嶉弬鍥︽ (heap/thread/all)"
    echo "  arthas             閸氼垰濮〢rthas鐠囧﹥鏌?
    echo ""
    echo -e "${YELLOW}閺堝秴濮熺粻锛勬倞閸涙垝鎶?${NC}"
    echo "  install-service    鐎瑰顥婃稉?systemd 閺堝秴濮?
    echo "  uninstall-service  閸楁瓕娴?systemd 閺堝秴濮?
    echo "  enable-service     閸氼垳鏁ゅ鈧張楦垮殰閸?
    echo "  disable-service    缁備胶鏁ゅ鈧張楦垮殰閸?
    echo "  service-status     閺屻儳婀呴張宥呭閻樿埖鈧?
    echo ""
    echo -e "${YELLOW}鏉╂劗娣崨鎴掓姢:${NC}"
    echo "  backup             Create backup (config/JAR)"
    echo "  backup list        List all backups with index"
    echo "  backup <N>         Restore to backup N"
    echo "  restore            Restore from latest backup"
    echo "  upgrade            娴犲骸甯囩紓鈺佸瘶閸楀洨楠?(閺€顖涘瘮 zip/tar.gz)"
    echo "  rollback           閸ョ偞绮撮崚棰佺瑐娑撯偓娑擃亣绻曢崢鐔哄仯"
    echo "  rollback-status    閺屻儳婀呮潻妯哄斧閻愬湱濮搁幀?
    echo "  debug              娴犮儴鐨熺拠鏇熌佸蹇撴儙閸?
    echo ""
    echo -e "${YELLOW}閻滎垰顣ㄧ粻锛勬倞閸涙垝鎶?${NC}"
    echo "  java               缁狅紕鎮?Java 閻滎垰顣?(install/list/check/remove)"
    echo "  skywalking         缁狅紕鎮?Skywalking Agent (install/check/clean/info)"
    echo ""
    echo -e "${YELLOW}Docker 閸涙垝鎶?${NC}"
    echo "  docker-build       閺嬪嫬缂?Docker 闂€婊冨剼"
    echo "  docker-deploy      閺嬪嫬缂撻獮鍫曞劥缂冩彃鍩岄張顒佹簚 (娑撯偓闁款喖褰傜敮?"
    echo "  publish            docker-deploy 閻ㄥ嫮鐣濋崘?
    echo "  docker-start       閸氼垰濮?Docker 鐎圭懓娅?
    echo "  docker-stop        閸嬫粍顒?Docker 鐎圭懓娅?
    echo "  docker-restart     闁插秴鎯?Docker 鐎圭懓娅?
    echo "  docker-status      閺屻儳婀呯€圭懓娅掗悩鑸碘偓?
    echo "  docker-logs        閺屻儳婀呯€圭懓娅掗弮銉ョ箶"
    echo "  docker-clean       濞撳懐鎮婇弮褔鏆呴崓?
    echo ""
    echo -e "${YELLOW}闁銆?${NC}"
    echo "  --java [path]         閹稿洤鐣?Java 鐠侯垰绶?(閺冪姴寮弫鐗堟閺勫墽銇氳ぐ鎾冲 Java)"
    echo "  --jvm-opts <opts>     閼奉亜鐣炬稊濉塚M閸欏倹鏆?
    echo "  --debug-port <port>   閹稿洤鐣剧拫鍐槸缁旑垰褰?(姒涙顓? ${DEFAULT_DEBUG_PORT})"
    echo "  --docker-port <port>  閹稿洤鐣剧€圭懓娅掗崘鍛村劥缁旑垰褰?(姒涙顓? 8080)"
    echo "  --host-port <port>    閹稿洤鐣炬稉缁樻簚閺勭姴鐨犵粩顖氬經 (姒涙顓? 8080)"
    echo "  --image-tag <tag>     閹稿洤鐣鹃梹婊冨剼閺嶅洨顒?(姒涙顓? ${APP_VERSION})"
    echo "  --skywalking          閸氼垳鏁?Skywalking APM"
    echo "  --sw-backend <addr>   閹稿洤鐣?Skywalking OAP 閸︽澘娼?
    echo "  --sw-name <name>      閹稿洤鐣?Skywalking 閺堝秴濮熼崥?
    echo "  --package <path>      閸楀洨楠囬崠鍛扮熅瀵?zip/tar.gz)"
    echo "  --no-restart          閸楀洨楠囬崥搴濈瑝閼奉亜濮╅柌宥呮儙"
    echo "  --dev                 娴ｈ法鏁ゅ鈧崣鎴犲箚婢?dev)"
    echo "  --prod                娴ｈ法鏁ら悽鐔堕獓閻滎垰顣?(prod, 姒涙顓?"
    echo "  --test                娴ｈ法鏁ゅù瀣槸閻滎垰顣?(test)"
    echo "  --profile <name>      閹稿洤鐣鹃懛顏勭暰娑斿骞嗘晶?
    echo "  --output <path>       dump 鏉堟挸鍤惄顔肩秿"
    echo "  --dump-path <path>    dump 鏉堟挸鍤惄顔肩秿 (--output 閸掝偄鎮?"
    echo "  --logs <N>            閸氼垰濮╅崥搴㈡▔缁€鐑樻付鏉?N 閺夆剝妫╄箛?
    echo "  --tail                閸氼垰濮╅崥搴＄杽閺冩儼绐￠煪顏呮）韫?
    echo "  --help, -h            閺勫墽銇氱敮顔煎И娣団剝浼?
    echo ""
    echo -e "${YELLOW}缁€杞扮伐:${NC}"
    echo ""
    echo -e "  ${CYAN}閸╄櫣顢呴崨鎴掓姢:${NC}"
    echo "    $0 start                             閸氼垰濮╂惔鏃傛暏"
    echo "    $0 start --dev                       瀵偓閸欐垹骞嗘晶鍐ㄦ儙閸?
    echo "    $0 start --prod                      閻㈢喍楠囬悳顖氼暔閸氼垰濮?
    echo "    $0 start --test                      濞村鐦悳顖氼暔閸氼垰濮?
    echo "    $0 start --profile staging           閼奉亜鐣炬稊澶屽箚婢у啫鎯庨崝?
    echo "    $0 start --tail                      閸氼垰濮╅獮璺虹杽閺冩儼绐￠煪顏呮）韫?
    echo "    $0 start --logs 200                  閸氼垰濮╅崥搴㈡▔缁€鐑樻付鏉?200 閺夆剝妫╄箛?
    echo "    $0 start --java /opt/jdk17/bin/java  閹稿洤鐣?Java 鐠侯垰绶為崥顖氬З (楠炴湹绻氱€?"
    echo "    $0 --java                            閺勫墽銇氳ぐ鎾冲娴ｈ法鏁ら惃?Java 閻楀牊婀?
    echo "    $0 stop                              閸嬫粍顒涙惔鏃傛暏"
    echo "    $0 restart                           闁插秴鎯庢惔鏃傛暏"
    echo "    $0 status                            閺屻儳婀呴悩鑸碘偓?
    echo "    $0 info                              閺屻儳婀呯拠锔剧矎娣団剝浼?
    echo ""
    echo -e "  ${CYAN}閺冦儱绻旈崨鎴掓姢:${NC}"
    echo "    $0 logs                              閺屻儳婀呴張鈧潻?100 閺夆剝妫╄箛?
    echo "    $0 logs 50                           閺屻儳婀呴張鈧潻?50 閺夆剝妫╄箛?
    echo "    $0 tail                              鐎圭偞妞傜捄鐔婚嚋閺冦儱绻?
    echo ""
    echo -e "  ${CYAN}鐠嬪啳鐦崨鎴掓姢:${NC}"
    echo "    $0 debug                             鐠嬪啳鐦Ο鈥崇础閸氼垰濮?(缁旑垰褰?5005)"
    echo "    $0 debug --debug-port 5006           鐠嬪啳鐦Ο鈥崇础閸氼垰濮?(閹稿洤鐣剧粩顖氬經)"
    echo ""
    echo -e "  ${CYAN}JVM 鐠囧﹥鏌?${NC}"
    echo "    $0 jvm                               閺屻儳婀?JVM 娣団剝浼?
    echo "    $0 gc                                鐟欙箑褰?Full GC"
    echo "    $0 gc young                          鐟欙箑褰?Young GC"
    echo "    $0 top-cpu                           妤?CPU 缁捐法鈻奸崚鍡樼€?(Top 10)"
    echo "    $0 top-cpu 20                        妤?CPU 缁捐法鈻奸崚鍡樼€?(Top 20)"
    echo "    $0 top-mem                           妤傛ê鍞寸€涙ê顕挒鈥冲瀻閺?(Top 10)"
    echo "    $0 top-mem 20                        妤傛ê鍞寸€涙ê顕挒鈥冲瀻閺?(Top 20)"
    echo "    $0 tuning                            JVM 鐠嬪啩绱楦款唴"
    echo "    $0 arthas                            閸氼垰濮?Arthas 鐠囧﹥鏌?
    echo ""
    echo -e "  ${CYAN}Dump 鏉烆剙鍋?${NC}"
    echo "    $0 dump                              閻㈢喐鍨氶崼?缁捐法鈻兼潪顒€鍋?
    echo "    $0 dump heap                         娴犲懎鐖㈡潪顒€鍋?
    echo "    $0 dump thread                       娴犲懐鍤庣粙瀣祮閸?
    echo "    $0 dump --output /tmp/dump           閹稿洤鐣炬潏鎾冲毉閻╊喖缍?
    echo ""
    echo -e "  ${CYAN}閺堝秴濮熺粻锛勬倞 (闂団偓 root 閺夊啴妾?:${NC}"
    echo "    $0 install-service                   鐎瑰顥婃稉?systemd 閺堝秴濮?
    echo "    $0 uninstall-service                 閸楁瓕娴囬張宥呭"
    echo "    $0 enable-service                    閸氼垳鏁ゅ鈧張楦垮殰閸?
    echo "    $0 disable-service                   缁備胶鏁ゅ鈧張楦垮殰閸?
    echo "    $0 service-status                    閺屻儳婀呴張宥呭閻樿埖鈧?
    echo ""
    echo -e "  ${CYAN}閸楀洨楠囬崶鐐寸泊:${NC}"
    echo "    $0 upgrade --package /tmp/app.zip    娴犲骸甯囩紓鈺佸瘶閸楀洨楠?
    echo "    $0 upgrade --package /tmp/app.tar.gz --no-restart  閸楀洨楠囨担鍡曠瑝闁插秴鎯?
    echo "    $0 rollback                          閸ョ偞绮撮崚棰佺瑐娑撯偓娑擃亞澧楅張?
    echo "    $0 rollback-status                   閺屻儳婀呮潻妯哄斧閻愬湱濮搁幀?
    echo "    $0 backup                            Create new backup"
    echo "    $0 backup list                       List all backups"
    echo "    $0 backup 1                          Restore to backup #1"
    echo "    $0 restore                           Restore from latest"
    echo ""
    echo -e "  ${CYAN}Docker 闁劎璁?${NC}"
    echo "    $0 docker-build                      閺嬪嫬缂?Docker 闂€婊冨剼"
    echo "    $0 docker-deploy                     閺嬪嫬缂撻獮鍫曞劥缂?(娑撯偓闁款喖褰傜敮?"
    echo "    $0 publish                           docker-deploy 閻ㄥ嫮鐣濋崘?
    echo "    $0 docker-start                      閸氼垰濮╃€圭懓娅?
    echo "    $0 docker-stop                       閸嬫粍顒涚€圭懓娅?
    echo "    $0 docker-restart                    闁插秴鎯庣€圭懓娅?
    echo "    $0 docker-status                     閺屻儳婀呯€圭懓娅掗悩鑸碘偓?
    echo "    $0 docker-logs                       閺屻儳婀呯€圭懓娅掗弮銉ョ箶"
    echo "    $0 docker-clean                      濞撳懐鎮婇弮褔鏆呴崓?
    echo ""
    echo -e "  ${CYAN}Java 閻滎垰顣ㄧ粻锛勬倞:${NC}"
    echo "    $0 java install                      鐎瑰顥?Java (姒涙顓?21)"
    echo "    $0 java install --version 17         鐎瑰顥婇幐鍥х暰閻楀牊婀?Java"
    echo "    $0 java install --dir /opt/java      鐎瑰顥婇崚鐗堝瘹鐎规氨娲拌ぐ?
    echo "    $0 java list                         閸掓鍤崣顖滄暏 Java 閻楀牊婀?
    echo "    $0 java check                        濡偓閺屻儱缍嬮崜?Java 閻滎垰顣?
    echo "    $0 java remove                       閸掔娀娅庡鎻掔暔鐟佸懐娈?Java"
    echo ""
    echo -e "  ${CYAN}Skywalking APM:${NC}"
    echo "    $0 start --skywalking                閸氼垳鏁?Skywalking 閸氼垰濮?
    echo "    $0 start --skywalking --sw-backend 192.168.1.100:11800  閹稿洤鐣?OAP 閸︽澘娼?
    echo "    $0 skywalking install                鐎瑰顥?Skywalking Agent"
    echo "    $0 skywalking check                  濡偓閺?Agent 閻樿埖鈧?
    echo ""
}

# ===========================================
# Java 濡偓濞?# ===========================================

# Java 闁板秶鐤嗛弬鍥︽鐠侯垰绶?(閻劋绨穱婵嗙摠閻劍鍩涢幐鍥х暰閻?Java 鐠侯垰绶?
JAVA_CONFIG_FILE="${APP_HOME}/.java_path"

# 娣囨繂鐡?Java 鐠侯垰绶為崚浼村帳缂冾喗鏋冩禒?save_java_path() {
    local java_path="$1"
    if [ -n "$java_path" ]; then
        echo "$java_path" > "${JAVA_CONFIG_FILE}"
        echo -e "${GREEN}[閹存劕濮沒 Java 鐠侯垰绶炲韫箽鐎? ${java_path}${NC}"
    fi
}

# 娴犲酣鍘ょ純顔芥瀮娴犺泛濮炴潪?Java 鐠侯垰绶?load_java_path() {
    if [ -f "${JAVA_CONFIG_FILE}" ]; then
        local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
        if [ -n "$saved_path" ] && [ -x "$saved_path" ]; then
            CUSTOM_JAVA="$saved_path"
        fi
    fi
}

# 濡偓閺?Java 閻滎垰顣?check_java() {
    # 閸忓牆鐨剧拠鏇炲鏉炴垝绻氱€涙娈?Java 鐠侯垰绶?    [ -z "$CUSTOM_JAVA" ] && load_java_path
    
    if [ -n "$CUSTOM_JAVA" ]; then
        if [ -x "$CUSTOM_JAVA" ]; then
            JAVA_CMD="$CUSTOM_JAVA"
        else
            echo -e "${RED}[闁挎瑨顕 閹稿洤鐣鹃惃?Java 鐠侯垰绶炴稉宥呯摠閸︺劍鍨ㄦ稉宥呭讲閹笛嗩攽: ${CUSTOM_JAVA}${NC}"
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
        echo -e "${RED}[闁挎瑨顕 閺堫亝澹橀崚鐧慳va閻滎垰顣?{NC}"
        echo "鐠囩柉顔曠純?JAVA_HOME 閹存牔濞囬悽?--java 閹稿洤鐣?Java 鐠侯垰绶?
        exit 1
    fi
}

# 閼惧嘲褰?Java 閻楀牊婀版穱鈩冧紖
get_java_version() {
    "$JAVA_CMD" -version 2>&1 | head -n 1 | awk -F '"' '{print $2}'
}

# 閺勫墽銇氳ぐ鎾冲 Java 娣団剝浼?show_java_info() {
    check_java
    
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    echo -e "${GREEN}              瑜版挸澧?Java 閻滎垰顣ㄦ穱鈩冧紖${NC}"
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    echo ""
    
    # 閺勫墽銇?Java 鐠侯垰绶為弶銉︾爱
    local source="缁崵绮?PATH"
    if [ -n "$CUSTOM_JAVA" ]; then
        if [ -f "${JAVA_CONFIG_FILE}" ]; then
            source="闁板秶鐤嗛弬鍥︽ (.java_path)"
        else
            source="閸涙垝鎶ょ悰?--java 閸欏倹鏆?
        fi
    elif [ -n "$JAVA_HOME" ]; then
        source="JAVA_HOME 閻滎垰顣ㄩ崣姗€鍣?
    fi
    
    echo -e "  ${CYAN}Java 鐠侯垰绶?${NC}    ${WHITE}${JAVA_CMD}${NC}"
    echo -e "  ${CYAN}鐠侯垰绶為弶銉︾爱:${NC}    ${WHITE}${source}${NC}"
    echo ""
    
    # 閺勫墽銇氱€瑰本鏆ｉ惃?Java 閻楀牊婀版穱鈩冧紖
    echo -e "  ${CYAN}Java 閻楀牊婀?${NC}"
    "$JAVA_CMD" -version 2>&1 | while read -r line; do
        echo -e "    ${WHITE}${line}${NC}"
    done
    echo ""
    
    # 閺勫墽銇氶柊宥囩枂閺傚洣娆㈤悩鑸碘偓?    if [ -f "${JAVA_CONFIG_FILE}" ]; then
        local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
        echo -e "  ${CYAN}瀹歌弓绻氱€涙娈?Java:${NC} ${WHITE}${saved_path}${NC}"
    else
        echo -e "  ${YELLOW}閹绘劗銇? 娴ｈ法鏁?--java <path> 閹稿洤鐣?Java 鐠侯垰绶為獮鏈电箽鐎?{NC}"
    fi
    echo ""
}

# 閼惧嘲褰囬張顒佹簚 IP 閸︽澘娼?get_local_ip() {
    local ip=""
    # 鐏忔繆鐦径姘鳖潚閺傜懓绱￠懢宄板絿 IP
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

# 娴犲酣鍘ょ純顔芥瀮娴犳儼顕伴崣鏍伂閸?get_server_port() {
    local port="${SERVER_PORT}"
    # 鐏忔繆鐦禒?application.yml 鐠囪褰?    if [ -f "${CONFIG_DIR}/application.yml" ]; then
        local yml_port=$(grep -E '^\s*port:' "${CONFIG_DIR}/application.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$yml_port" ] && port="$yml_port"
    fi
    # 鐏忔繆鐦禒?application-${profile}.yml 鐠囪褰?    if [ -f "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" ]; then
        local profile_port=$(grep -E '^\s*port:' "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$profile_port" ] && port="$profile_port"
    fi
    echo "${port:-8080}"
}

# 閼惧嘲褰?context-path
get_context_path() {
    local ctx_path=""
    # 娴犲酣鍘ょ純顔芥瀮娴犳儼顕伴崣?context-path
    if [ -f "${CONFIG_DIR}/application.yml" ]; then
        ctx_path=$(grep -E 'context-path:' "${CONFIG_DIR}/application.yml" 2>/dev/null | head -1 | awk '{print $2}')
    fi
    if [ -f "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" ]; then
        local profile_ctx=$(grep -E 'context-path:' "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$profile_ctx" ] && ctx_path="$profile_ctx"
    fi
    echo "${ctx_path}"
}

# 濡偓濞村妲搁崥锕€鎯庨悽?Swagger
check_swagger_enabled() {
    local swagger_enabled="false"
    # 濡偓閺屻儵鍘ょ純顔芥瀮娴犳湹鑵戦弰顖氭儊閺?swagger/springdoc/knife4j 闁板秶鐤?    if [ -d "${CONFIG_DIR}" ]; then
        if grep -rq -E '(springdoc|swagger|knife4j)' "${CONFIG_DIR}/" 2>/dev/null; then
            swagger_enabled="true"
        fi
    fi
    # 濡偓閺?JAR 娑擃厽妲搁崥锕€瀵橀崥?swagger 閻╃鍙ф笟婵婄
    if [ -f "${APP_HOME}/${JAR_NAME}" ]; then
        if unzip -l "${APP_HOME}/${JAR_NAME}" 2>/dev/null | grep -qE '(springdoc|swagger|knife4j)'; then
            swagger_enabled="true"
        fi
    fi
    echo "$swagger_enabled"
}

# 閺嬪嫬缂?loader path
build_loader_path() {
    LOADER_PATH="${CONFIG_DIR}"
    [ -d "${LIB_DIR}" ] && LOADER_PATH="${LOADER_PATH},${LIB_DIR}"
}

# ===========================================
# 鏉╂稓鈻肩粻锛勬倞
# ===========================================
get_pid() {
    if [ -f "${PID_FILE}" ]; then
        local pid=$(cat "${PID_FILE}")
        ps -p "$pid" > /dev/null 2>&1 && echo "$pid" && return 0
    fi
    pgrep -f "${JAR_NAME}" 2>/dev/null | head -1
}

# ===========================================
# 閻樿埖鈧焦顥呴弻?# ===========================================
check_status() {
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "${GREEN}[鏉╂劘顢戞稉鐠?{NC} ${APP_NAME} (PID: ${pid})"
        return 0
    else
        echo -e "${YELLOW}[瀹告彃浠犲顣?{NC} ${APP_NAME}"
        return 1
    fi
}

# ===========================================
# 鐠囷妇绮忔穱鈩冧紖閺勫墽銇?# ===========================================
show_info() {
    local pid=$(get_pid)
    
    echo -e "${BLUE}閳烘柡鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫧${NC}"
    echo -e "${BLUE}閳?{NC}${GREEN}                    鎼存梻鏁ゆ潻鎰攽娣団剝浼?                             ${NC}${BLUE}閳?{NC}"
    echo -e "${BLUE}閳烘埃鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ殕${NC}"
    echo ""
    
    echo -e "${CYAN}閵嗘劕鐔€绾偓娣団剝浼呴妴?{NC}"
    echo -e "  鎼存梻鏁ら崥宥囆?     ${WHITE}${APP_NAME}${NC}"
    echo -e "  鎼存梻鏁ら悧鍫熸拱:     ${WHITE}${APP_VERSION}${NC}"
    echo -e "  JAR 閺傚洣娆?     ${WHITE}${JAR_NAME}${NC}"
    echo -e "  鎼存梻鏁ら惄顔肩秿:     ${WHITE}${APP_HOME}${NC}"
    echo -e "  闁板秶鐤嗛惄顔肩秿:     ${WHITE}${CONFIG_DIR}${NC}"
    echo -e "  閺冦儱绻旈惄顔肩秿:     ${WHITE}${LOG_DIR}${NC}"
    echo ""
    
    if [ -n "$pid" ]; then
        echo -e "${CYAN}閵嗘劘绻嶇悰宀€濮搁幀浣碘偓?{NC}"
        echo -e "  閻樿埖鈧?         ${GREEN}鏉╂劘顢戞稉?{NC}"
        echo -e "  鏉╂稓鈻?ID:      ${WHITE}${pid}${NC}"
        
        # 閼惧嘲褰囬崥顖氬З閺冨爼妫?        local start_time=$(ps -o lstart= -p "$pid" 2>/dev/null | xargs)
        [ -n "$start_time" ] && echo -e "  閸氼垰濮╅弮鍫曟？:     ${WHITE}${start_time}${NC}"
        
        # 閼惧嘲褰囨潻鎰攽閺冨爼鏆?        local etime=$(ps -o etime= -p "$pid" 2>/dev/null | xargs)
        [ -n "$etime" ] && echo -e "  鏉╂劘顢戦弮鍫曟毐:     ${WHITE}${etime}${NC}"
        
        # 閼惧嘲褰囬崘鍛摠娴ｈ法鏁?        local mem=$(ps -o rss= -p "$pid" 2>/dev/null | xargs)
        if [ -n "$mem" ]; then
            local mem_mb=$((mem / 1024))
            echo -e "  閸愬懎鐡ㄦ担璺ㄦ暏:     ${WHITE}${mem_mb} MB${NC}"
        fi
        
        # 閼惧嘲褰?CPU 娴ｈ法鏁ら悳?        local cpu=$(ps -o %cpu= -p "$pid" 2>/dev/null | xargs)
        [ -n "$cpu" ] && echo -e "  CPU 娴ｈ法鏁?     ${WHITE}${cpu}%${NC}"
        
        echo ""
        echo -e "${CYAN}閵嗘€濾M 娣団剝浼呴妴?{NC}"
        check_java
        echo -e "  Java 鐠侯垰绶?    ${WHITE}${JAVA_CMD}${NC}"
        echo -e "  Java 閻楀牊婀?    ${WHITE}$(get_java_version)${NC}"
        
        # 鐏忔繆鐦懢宄板絿 JVM 閸欏倹鏆?        local jvm_opts=$(ps -o args= -p "$pid" 2>/dev/null | grep -oE '\-X[^ ]+' | head -5 | tr '\n' ' ')
        [ -n "$jvm_opts" ] && echo -e "  JVM 閸欏倹鏆?     ${WHITE}${jvm_opts}${NC}"
    else
        echo -e "${CYAN}閵嗘劘绻嶇悰宀€濮搁幀浣碘偓?{NC}"
        echo -e "  閻樿埖鈧?         ${YELLOW}瀹告彃浠犲?{NC}"
    fi
    
    echo ""
    echo -e "${CYAN}閵嗘劖妫╄箛妤佹瀮娴犺翰鈧?{NC}"
    if [ -f "${STARTUP_LOG}" ]; then
        local log_size=$(du -h "${STARTUP_LOG}" 2>/dev/null | cut -f1)
        echo -e "  閸氼垰濮╅弮銉ョ箶:     ${WHITE}${STARTUP_LOG} (${log_size})${NC}"
    fi
    if [ -f "${APP_LOG}" ]; then
        local log_size=$(du -h "${APP_LOG}" 2>/dev/null | cut -f1)
        echo -e "  鎼存梻鏁ら弮銉ョ箶:     ${WHITE}${APP_LOG} (${log_size})${NC}"
    fi
    echo ""
}

# ===========================================
# 閸氼垰濮╂穱鈩冧紖閺勫墽銇?# ===========================================
show_startup_banner() {
    local java_version=$(get_java_version)
    local local_ip=$(get_local_ip)
    local server_port=$(get_server_port)
    local context_path=$(get_context_path)
    local swagger_enabled=$(check_swagger_enabled)
    local base_url="http://${local_ip}:${server_port}${context_path}"
    local local_url="http://localhost:${server_port}${context_path}"
    
    echo ""
    echo -e "${BLUE}閳烘柡鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫧${NC}"
    echo -e "${BLUE}閳?{NC}${GREEN}                     鎼存梻鏁ら崥顖氬З娑?..                            ${NC}${BLUE}閳?{NC}"
    echo -e "${BLUE}閳烘埃鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ殕${NC}"
    echo ""
    echo -e "${CYAN}閳瑰备鏀㈤埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞?{NC}"
    echo -e "${CYAN}閳?{NC} ${WHITE}鎼存梻鏁ら崥宥囆?{NC}    閳?${GREEN}${APP_NAME}${NC}"
    echo -e "${CYAN}閳?{NC} ${WHITE}鎼存梻鏁ら悧鍫熸拱${NC}    閳?${GREEN}${APP_VERSION}${NC}"
    echo -e "${CYAN}閳?{NC} ${WHITE}鏉╂劘顢戦悳顖氼暔${NC}    閳?${YELLOW}${SPRING_PROFILE}${NC}"
    echo -e "${CYAN}閳?{NC} ${WHITE}閺堝秴濮熼崷鏉挎絻${NC}    閳?${GREEN}${base_url}${NC}"
    echo -e "${CYAN}閳?{NC} ${WHITE}閺堫剙婀撮崷鏉挎絻${NC}    閳?${GREEN}${local_url}${NC}"
    [ "${swagger_enabled}" = "true" ] && echo -e "${CYAN}閳?{NC} ${WHITE}Swagger${NC}     閳?${GREEN}${local_url}/doc.html${NC}"
    echo -e "${CYAN}閳?{NC} ${WHITE}Java 閻楀牊婀?{NC}   閳?${MAGENTA}${java_version}${NC}"
    echo -e "${CYAN}閳?{NC} ${WHITE}Java 鐠侯垰绶?{NC}   閳?${MAGENTA}${JAVA_CMD}${NC}"
    echo -e "${CYAN}閳?{NC} ${WHITE}JVM 閸欏倹鏆?{NC}    閳?${MAGENTA}${JAVA_OPTS}${NC}"
    [ "${DEBUG_MODE}" = "true" ] && echo -e "${CYAN}閳?{NC} ${WHITE}鐠嬪啳鐦粩顖氬經${NC}    閳?${RED}${DEBUG_PORT}${NC}"
    [ "${SKYWALKING_ENABLED}" = "true" ] && echo -e "${CYAN}閳?{NC} ${WHITE}Skywalking${NC}  閳?${GREEN}瀹告彃鎯庨悽?(${SW_AGENT_NAME})${NC}"
    echo -e "${CYAN}閳?{NC} ${WHITE}闁板秶鐤嗛惄顔肩秿${NC}    閳?${CONFIG_DIR}"
    echo -e "${CYAN}閳?{NC} ${WHITE}閺冦儱绻旈惄顔肩秿${NC}    閳?${LOG_DIR}"
    echo -e "${CYAN}閳?{NC} ${WHITE}閸氼垰濮╅弮鍫曟？${NC}    閳?$(date '+%Y-%m-%d %H:%M:%S')"
    echo -e "${CYAN}閳规柡鏀㈤埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞鈧埞?{NC}"
    echo ""
}

# ===========================================
# 閺冦儱绻旂粻锛勬倞
# ===========================================

# 閺屻儳婀呴張鈧潻?N 閺夆剝妫╄箛?show_logs() {
    local lines="${1:-100}"
    local log_file="${STARTUP_LOG}"
    
    # 娴兼ê鍘涙担璺ㄦ暏鎼存梻鏁ら弮銉ョ箶閿涘苯顩ч弸婊冪摠閸︺劎娈戠拠?    [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
    
    if [ ! -f "${log_file}" ]; then
        echo -e "${YELLOW}[鐠€锕€鎲 閺冦儱绻旈弬鍥︽娑撳秴鐡ㄩ崷? ${log_file}${NC}"
        return 1
    fi
    
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    echo -e "${GREEN}  閺堚偓鏉?${lines} 閺夆剝妫╄箛?- ${log_file}${NC}"
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    tail -n "${lines}" "${log_file}"
}

# 鐎圭偞妞傜捄鐔婚嚋閺冦儱绻?tail_logs() {
    local log_file="${STARTUP_LOG}"
    
    # 娴兼ê鍘涙担璺ㄦ暏鎼存梻鏁ら弮銉ョ箶
    [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
    
    if [ ! -f "${log_file}" ]; then
        echo -e "${YELLOW}[鐠€锕€鎲 閺冦儱绻旈弬鍥︽娑撳秴鐡ㄩ崷? ${log_file}${NC}"
        echo -e "${YELLOW}缁涘绶熼弮銉ョ箶閺傚洣娆㈤悽鐔稿灇...${NC}"
        # 缁涘绶熼弮銉ョ箶閺傚洣娆㈤悽鐔稿灇
        local count=0
        while [ ! -f "${log_file}" ] && [ $count -lt 30 ]; do
            sleep 1
            count=$((count + 1))
            [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
        done
        if [ ! -f "${log_file}" ]; then
            echo -e "${RED}[闁挎瑨顕 缁涘绶熺搾鍛閿涘本妫╄箛妤佹瀮娴犳湹绮涙稉宥呯摠閸?{NC}"
            return 1
        fi
    fi
    
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    echo -e "${GREEN}  鐎圭偞妞傞弮銉ョ箶鐠虹喕閲?- ${log_file}${NC}"
    echo -e "${YELLOW}  閹?Ctrl+C 闁偓閸?{NC}"
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    tail -f "${log_file}"
}

# ===========================================
# Skywalking 閺€顖涘瘮
# ===========================================
ensure_skywalking() {
    if [ ! -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "${YELLOW}[Skywalking] Agent 閺堫亜鐣ㄧ憗鍜冪礉瀵偓婵鍤滈崝銊ょ瑓鏉?..${NC}"
        bash "${SCRIPT_DIR}/script/linux/skywalking.sh" install
        if [ $? -ne 0 ]; then
            echo -e "${RED}[闁挎瑨顕 Skywalking Agent 娑撳娴囨径杈Е${NC}"
            return 1
        fi
    fi
    return 0
}

run_skywalking() {
    bash "${SCRIPT_DIR}/script/linux/skywalking.sh" "$@"
}

# ===========================================
# Java 閻滎垰顣ㄧ粻锛勬倞
# ===========================================
run_java_install() {
    local script_path="${SCRIPT_DIR}/script/linux/java-install.sh"
    if [ -f "$script_path" ]; then
        bash "$script_path" "$@"
    else
        echo -e "${RED}[闁挎瑨顕 Java 鐎瑰顥婇懘姘拱娑撳秴鐡ㄩ崷? ${script_path}${NC}"
        return 1
    fi
}

# ===========================================
# 鎼存梻鏁ら崥顖氬З
# ===========================================
start_app() {
    local pid=$(get_pid)
    [ -n "$pid" ] && echo -e "${YELLOW}[鐠€锕€鎲 ${APP_NAME} 瀹告彃婀潻鎰攽 (PID: ${pid})${NC}" && return 1
    
    mkdir -p "${LOG_DIR}"
    check_java
    build_loader_path
    
    # 閺嬪嫬缂?Spring 閸氼垰濮╅崣鍌涙殶
    local start_opts="--spring.config.location=classpath:/,file:${CONFIG_DIR}/"
    start_opts="${start_opts} --spring.profiles.active=${SPRING_PROFILE}"
    
    # 閺勫墽銇氶崥顖氬З娣団剝浼?    show_startup_banner
    
    local java_opts="${JAVA_OPTS}"
    [ -n "${CUSTOM_JVM_OPTS}" ] && java_opts="${CUSTOM_JVM_OPTS}"
    [ "${DEBUG_MODE}" = "true" ] && java_opts="${java_opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT}"
    
    # Skywalking 閺€顖涘瘮
    if [ "${SKYWALKING_ENABLED}" = "true" ]; then
        ensure_skywalking || return 1
        java_opts="${java_opts} -javaagent:${SKYWALKING_AGENT_JAR}"
        java_opts="${java_opts} -Dskywalking.agent.service_name=${SW_AGENT_NAME}"
        java_opts="${java_opts} -Dskywalking.collector.backend_service=${SW_AGENT_COLLECTOR_BACKEND_SERVICES}"
    fi
    
    # 閸氼垰濮╂惔鏃傛暏
    nohup "$JAVA_CMD" ${java_opts} -Dloader.path="${LOADER_PATH}" -jar "${APP_HOME}/${JAR_NAME}" ${start_opts} > "${STARTUP_LOG}" 2>&1 &
    
    local new_pid=$!
    echo "$new_pid" > "${PID_FILE}"
    sleep 2
    
    if ps -p "$new_pid" > /dev/null 2>&1; then
        echo -e "${GREEN}閳烘柡鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫧${NC}"
        echo -e "${GREEN}閳?                   閴?閸氼垰濮╅幋鎰                                閳?{NC}"
        echo -e "${GREEN}閳烘埃鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ殕${NC}"
        echo ""
        echo -e "  ${WHITE}鏉╂稓鈻?ID:${NC}    ${new_pid}"
        echo -e "  ${WHITE}閺冦儱绻旈弬鍥︽:${NC}   ${STARTUP_LOG}"
        echo ""
        
        # 閺嶈宓侀崣鍌涙殶閺勫墽銇氶弮銉ョ箶
        if [ "${TAIL_LOG}" = "true" ]; then
            echo -e "${CYAN}瀵偓婵鐤勯弮鎯扮闊亝妫╄箛?..${NC}"
            tail_logs
        elif [ -n "${SHOW_LOGS}" ] && [ "${SHOW_LOGS}" -gt 0 ]; then
            echo -e "${CYAN}閺勫墽銇氶張鈧潻?${SHOW_LOGS} 閺夆剝妫╄箛?..${NC}"
            sleep 3  # 缁涘绶熸稉鈧禍娑欐）韫囨绶崙?            show_logs "${SHOW_LOGS}"
        fi
    else
        echo -e "${RED}閳烘柡鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫧${NC}"
        echo -e "${RED}閳?                   閴?閸氼垰濮╂径杈Е                                閳?{NC}"
        echo -e "${RED}閳烘埃鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ殕${NC}"
        echo ""
        echo -e "${YELLOW}闁挎瑨顕ら弮銉ョ箶:${NC}"
        [ -f "${STARTUP_LOG}" ] && tail -30 "${STARTUP_LOG}"
    fi
}

# ===========================================
# 鎼存梻鏁ら崑婊勵剾
# ===========================================
stop_app() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[鐠€锕€鎲 ${APP_NAME} 閺堫亜婀潻鎰攽${NC}" && return 0
    
    echo -e "${BLUE}濮濓絽婀崑婊勵剾 ${APP_NAME} (PID: ${pid})...${NC}"
    kill "$pid" 2>/dev/null
    
    local count=0
    while [ $count -lt 30 ] && ps -p "$pid" > /dev/null 2>&1; do
        sleep 1
        count=$((count + 1))
        echo -ne "\r${YELLOW}缁涘绶熸潻娑氣柤闁偓閸?.. ${count}s${NC}"
    done
    echo ""
    
    ps -p "$pid" > /dev/null 2>&1 && kill -9 "$pid" 2>/dev/null
    rm -f "${PID_FILE}"
    echo -e "${GREEN}[閹存劕濮沒 ${APP_NAME} 瀹告彃浠犲?{NC}"
}

# 闁插秴鎯?restart_app() { 
    stop_app
    sleep 2
    start_app
}

# ===========================================
# JVM 鐠囧﹥鏌?# ===========================================
show_jvm_info() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[鐠€锕€鎲 ${APP_NAME} 閺堫亜婀潻鎰攽${NC}" && return 1
    
    if [ -f "${SCRIPT_DIR}/script/linux/jvm-info.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/jvm-info.sh" "$pid" "$APP_NAME"
    else
        # 閸愬懐鐤嗙粻鈧崡鏇犳畱 JVM 娣団剝浼呴弰鍓с仛
        echo -e "${BLUE}閳烘柡鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫧${NC}"
        echo -e "${BLUE}閳?{NC}${GREEN}                    JVM 鏉╂劘顢戞穱鈩冧紖                              ${NC}${BLUE}閳?{NC}"
        echo -e "${BLUE}閳烘埃鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ殕${NC}"
        echo ""
        echo -e "${CYAN}鏉╂稓鈻兼穱鈩冧紖:${NC}"
        echo -e "  PID: ${pid}"
        
        if command -v jps &> /dev/null; then
            echo ""
            echo -e "${CYAN}JVM 鏉╂稓鈻?${NC}"
            jps -lvm | grep "$pid" || echo "  閺冪姵纭堕懢宄板絿鐠囷妇绮忔穱鈩冧紖"
        fi
        
        if command -v jstat &> /dev/null; then
            echo ""
            echo -e "${CYAN}GC 缂佺喕顓?${NC}"
            jstat -gcutil "$pid" 2>/dev/null || echo "  閺冪姵纭堕懢宄板絿 GC 娣団剝浼?
        fi
    fi
}

show_tuning() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[鐠€锕€鎲 ${APP_NAME} 閺堫亜婀潻鎰攽${NC}" && return 1
    
    if [ -f "${SCRIPT_DIR}/script/linux/jvm-tuning.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/jvm-tuning.sh" "$pid" "$APP_NAME"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 JVM 鐠嬪啩绱懘姘拱娑撳秴鐡ㄩ崷?{NC}"
    fi
}

start_arthas() {
    local pid=$(get_pid)
    if [ -f "${SCRIPT_DIR}/script/linux/arthas-install.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/arthas-install.sh" "$pid"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 Arthas 閼存碍婀版稉宥呯摠閸︻煉绱濈亸婵婄槸閻╁瓨甯撮崥顖氬З...${NC}"
        if [ -f "${APP_HOME}/arthas/as.sh" ]; then
            bash "${APP_HOME}/arthas/as.sh" "$pid"
        else
            echo -e "${RED}[闁挎瑨顕 Arthas 閺堫亜鐣ㄧ憗?{NC}"
        fi
    fi
}

run_analyze() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[鐠€锕€鎲 ${APP_NAME} 閺堫亜婀潻鎰攽${NC}" && return 1
    local mode="${1:-cpu}"
    
    if [ -f "${SCRIPT_DIR}/script/linux/cpu-analysis.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/cpu-analysis.sh" "$pid" "$mode" "$APP_NAME"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 閸掑棙鐎介懘姘拱娑撳秴鐡ㄩ崷?{NC}"
    fi
}

# ===========================================
# Dump 鏉烆剙鍋嶉崝鐔诲厴
# ===========================================
run_dump() {
    local mode="${1:-all}"
    local output_dir="${2:-${LOG_DIR}/dump}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[闁挎瑨顕 ${APP_NAME} 閺堫亜婀潻鎰攽${NC}"
        exit 1
    fi
    
    mkdir -p "${output_dir}"
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    
    echo -e "${BLUE}閳烘柡鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫧${NC}"
    echo -e "${BLUE}閳?{NC}${GREEN}                    閻㈢喐鍨?Dump 鏉烆剙鍋?                           ${NC}${BLUE}閳?{NC}"
    echo -e "${BLUE}閳烘埃鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ殕${NC}"
    echo ""
    echo -e "  PID:        ${pid}"
    echo -e "  濡€崇础:       ${mode}"
    echo -e "  鏉堟挸鍤惄顔肩秿:   ${output_dir}"
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
            echo -e "${RED}[闁挎瑨顕 閺堫亞鐓￠惃?dump 濡€崇础: ${mode}${NC}"
            echo "閺€顖涘瘮閻ㄥ嫭膩瀵? heap, thread, all"
            exit 1
            ;;
    esac
    
    echo -e "\n${GREEN}[鐎瑰本鍨歖 Dump 閺傚洣娆㈠韫箽鐎涙ê鍩? ${output_dir}${NC}"
}

dump_heap() {
    local pid="$1"
    local output_dir="$2"
    local timestamp="$3"
    local jmap_cmd="$4"
    local heap_file="${output_dir}/heapdump_${timestamp}.hprof"
    
    echo -e "${BLUE}[閸棜娴嗛崒鈺?閻㈢喐鍨氭稉?..${NC}"
    
    if ! command -v "${jmap_cmd}" &> /dev/null; then
        echo -e "${RED}[闁挎瑨顕 閺堫亝澹橀崚?jmap 閸涙垝鎶?{NC}"
        return 1
    fi
    
    "${jmap_cmd}" -dump:format=b,file="${heap_file}" "${pid}" 2>&1
    
    if [ $? -eq 0 ] && [ -f "${heap_file}" ]; then
        local size=$(du -h "${heap_file}" | cut -f1)
        echo -e "${GREEN}  閴?閸棜娴嗛崒? ${heap_file} (${size})${NC}"
    else
        echo -e "${RED}  閴?閸棜娴嗛崒銊ャ亼鐠?{NC}"
    fi
}

dump_thread() {
    local pid="$1"
    local output_dir="$2"
    local timestamp="$3"
    local jstack_cmd="$4"
    local thread_file="${output_dir}/threaddump_${timestamp}.txt"
    
    echo -e "${BLUE}[缁捐法鈻兼潪顒€鍋峕 閻㈢喐鍨氭稉?..${NC}"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        echo -e "${RED}[闁挎瑨顕 閺堫亝澹橀崚?jstack 閸涙垝鎶?{NC}"
        return 1
    fi
    
    "${jstack_cmd}" -l "${pid}" > "${thread_file}" 2>&1
    
    if [ $? -eq 0 ] && [ -f "${thread_file}" ]; then
        local lines=$(wc -l < "${thread_file}")
        echo -e "${GREEN}  閴?缁捐法鈻兼潪顒€鍋? ${thread_file} (${lines} 鐞?${NC}"
        
        echo -e "\n${CYAN}[缁捐法鈻奸悩鑸碘偓浣虹埠鐠侇摚${NC}"
        echo -e "  RUNNABLE:      $(grep -c 'java.lang.Thread.State: RUNNABLE' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  WAITING:       $(grep -c 'java.lang.Thread.State: WAITING' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  TIMED_WAITING: $(grep -c 'java.lang.Thread.State: TIMED_WAITING' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  BLOCKED:       $(grep -c 'java.lang.Thread.State: BLOCKED' "${thread_file}" 2>/dev/null || echo 0)"
        
        if grep -q "Found.*deadlock" "${thread_file}" 2>/dev/null; then
            echo -e "\n${RED}[鐠€锕€鎲 濡偓濞村鍩屽濠氭敚!${NC}"
        fi
    else
        echo -e "${RED}  閴?缁捐法鈻兼潪顒€鍋嶆径杈Е${NC}"
    fi
}

# ===========================================
# Backup / Restore
# ===========================================

# List all backups with index numbers
list_backups() {
    if [ ! -d "${BACKUP_DIR}" ]; then
        echo -e "${YELLOW}[Warning] Backup directory does not exist: ${BACKUP_DIR}${NC}"
        return 1
    fi
    
    local backups=($(ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null | grep -v "^$"))
    
    if [ ${#backups[@]} -eq 0 ]; then
        echo -e "${YELLOW}[Info] No backups found${NC}"
        return 1
    fi
    
    echo -e "${BLUE}================================================================${NC}"
    echo -e "${GREEN}                    Backup List${NC}"
    echo -e "${BLUE}================================================================${NC}"
    echo ""
    printf "  ${WHITE}%-6s %-24s %-10s %s${NC}\n" "Index" "Time" "Size" "Path"
    echo -e "${BLUE}----------------------------------------------------------------${NC}"
    
    local index=1
    for backup in "${backups[@]}"; do
        local backup_name=$(basename "$backup")
        local backup_size=$(du -sh "$backup" 2>/dev/null | cut -f1)
        local backup_time=""
        
        # Parse timestamp from directory name (format: YYYYMMDD_HHMMSS)
        if [[ "$backup_name" =~ ^([0-9]{4})([0-9]{2})([0-9]{2})_([0-9]{2})([0-9]{2})([0-9]{2})$ ]]; then
            backup_time="${BASH_REMATCH[1]}-${BASH_REMATCH[2]}-${BASH_REMATCH[3]} ${BASH_REMATCH[4]}:${BASH_REMATCH[5]}:${BASH_REMATCH[6]}"
        else
            backup_time="$backup_name"
        fi
        
        # Check if this is the latest backup
        local latest_marker=""
        if [ -f "${BACKUP_DIR}/latest" ]; then
            local latest_path=$(cat "${BACKUP_DIR}/latest" 2>/dev/null)
            if [ "$backup" = "$latest_path" ] || [ "$backup" = "${latest_path}/" ]; then
                latest_marker=" ${GREEN}(latest)${NC}"
            fi
        fi
        
        printf "  %-6s %-24s %-10s %s%b\n" "$index" "$backup_time" "$backup_size" "$backup" "$latest_marker"
        index=$((index + 1))
    done
    
    echo ""
    echo -e "${CYAN}Usage:${NC}"
    echo "  $0 backup           Create new backup"
    echo "  $0 backup list      List all backups"
    echo "  $0 backup <N>       Restore to backup N"
    echo ""
}

# Get backup path by index
get_backup_by_index() {
    local index="$1"
    local backups=($(ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null | grep -v "^$"))
    
    if [ ${#backups[@]} -eq 0 ]; then
        return 1
    fi
    
    if [ "$index" -lt 1 ] || [ "$index" -gt ${#backups[@]} ]; then
        return 1
    fi
    
    # Array is 0-indexed, but user index is 1-indexed
    echo "${backups[$((index - 1))]}"
}

# Restore from backup path
restore_from_path() {
    local restore_path="$1"
    
    if [ -z "${restore_path}" ] || [ ! -d "${restore_path}" ]; then
        echo -e "${RED}[Error] Invalid backup path: ${restore_path}${NC}"
        return 1
    fi
    
    echo -e "${BLUE}================================================================${NC}"
    echo -e "${GREEN}                    Restore Operation${NC}"
    echo -e "${BLUE}================================================================${NC}"
    echo ""
    echo -e "  Restore from: ${WHITE}${restore_path}${NC}"
    echo ""
    
    # Restore config
    if [ -d "${restore_path}/config" ]; then
        mkdir -p "${CONFIG_DIR}"
        cp -r "${restore_path}/config/"* "${CONFIG_DIR}/" 2>/dev/null
        echo -e "${GREEN}  [OK] Config files restored${NC}"
    else
        echo -e "${YELLOW}  [Skip] No config in backup${NC}"
    fi
    
    # Restore JAR
    local backup_jar="${restore_path}/${JAR_NAME}"
    if [ -f "${backup_jar}" ]; then
        cp "${backup_jar}" "${APP_HOME}/"
        echo -e "${GREEN}  [OK] JAR file restored${NC}"
    else
        echo -e "${YELLOW}  [Skip] No JAR in backup${NC}"
    fi
    
    echo ""
    echo -e "${GREEN}[Done] Restore completed${NC}"
}

run_backup() {
    local subcmd="${1:-}"
    
    # If subcmd is 'list', show all backups
    if [ "$subcmd" = "list" ]; then
        list_backups
        return $?
    fi
    
    # If subcmd is a number, restore to that backup
    if [[ "$subcmd" =~ ^[0-9]+$ ]]; then
        local backup_path=$(get_backup_by_index "$subcmd")
        if [ -z "$backup_path" ]; then
            echo -e "${RED}[Error] Backup #${subcmd} not found${NC}"
            echo -e "${CYAN}Use '$0 backup list' to see available backups${NC}"
            return 1
        fi
        restore_from_path "$backup_path"
        return $?
    fi
    
    # Default: create new backup
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_path="${BACKUP_DIR}/${timestamp}"
    
    mkdir -p "${backup_path}"
    
    echo -e "${BLUE}================================================================${NC}"
    echo -e "${GREEN}                    Backup Operation${NC}"
    echo -e "${BLUE}================================================================${NC}"
    echo ""
    echo -e "  Backup to:   ${WHITE}${backup_path}${NC}"
    echo ""
    
    # Backup config
    if [ -d "${CONFIG_DIR}" ]; then
        cp -r "${CONFIG_DIR}" "${backup_path}/"
        echo -e "${GREEN}  [OK] Config files backed up${NC}"
    else
        echo -e "${YELLOW}  [Skip] Config directory not found${NC}"
    fi
    
    # Backup JAR
    local jar_file="${APP_HOME}/${JAR_NAME}"
    if [ -f "${jar_file}" ]; then
        cp "${jar_file}" "${backup_path}/"
        echo -e "${GREEN}  [OK] JAR file backed up${NC}"
    else
        echo -e "${YELLOW}  [Skip] JAR file not found: ${jar_file}${NC}"
    fi
    
    # Save latest pointer
    echo "${backup_path}" > "${BACKUP_DIR}/latest"
    
    echo ""
    echo -e "${GREEN}[Done] Backup completed: ${backup_path}${NC}"
}

run_restore() {
    # Default: restore from latest
    local restore_path=""
    
    if [ -f "${BACKUP_DIR}/latest" ]; then
        restore_path=$(cat "${BACKUP_DIR}/latest" 2>/dev/null)
    fi
    
    if [ -z "${restore_path}" ] || [ ! -d "${restore_path}" ]; then
        echo -e "${RED}[Error] No backup found to restore${NC}"
        echo -e "${CYAN}Use '$0 backup list' to see available backups${NC}"
        return 1
    fi
    
    restore_from_path "$restore_path"
}

# ===========================================
# 閸楀洨楠囬崶鐐寸泊
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
        echo -e "${YELLOW}[閹绘劗銇歖 閸楀洨楠囬懘姘拱娑撳秴鐡ㄩ崷?{NC}"
    fi
}

run_rollback() {
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" rollback
    else
        echo -e "${YELLOW}[閹绘劗銇歖 閸ョ偞绮撮懘姘拱娑撳秴鐡ㄩ崷?{NC}"
    fi
}

run_rollback_status() {
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" status
    else
        echo -e "${YELLOW}[閹绘劗銇歖 閸ョ偞绮撮懘姘拱娑撳秴鐡ㄩ崷?{NC}"
    fi
}

# ===========================================
# Docker 閻╃鍙ч崙鑺ユ殶
# ===========================================
docker_build() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" build --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 Docker 閼存碍婀版稉宥呯摠閸?{NC}"
    fi
}

docker_deploy() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" deploy --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 Docker 閼存碍婀版稉宥呯摠閸?{NC}"
    fi
}

docker_start_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" start --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 Docker 閼存碍婀版稉宥呯摠閸?{NC}"
    fi
}

docker_stop_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" stop --name "$APP_NAME"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 Docker 閼存碍婀版稉宥呯摠閸?{NC}"
    fi
}

docker_restart_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" restart --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 Docker 閼存碍婀版稉宥呯摠閸?{NC}"
    fi
}

docker_status() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" status --name "$APP_NAME"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 Docker 閼存碍婀版稉宥呯摠閸?{NC}"
    fi
}

docker_logs() {
    local lines="${1:-100}"
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" logs --name "$APP_NAME" --lines "$lines"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 Docker 閼存碍婀版稉宥呯摠閸?{NC}"
    fi
}

docker_clean() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" clean --name "$APP_NAME" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[閹绘劗銇歖 Docker 閼存碍婀版稉宥呯摠閸?{NC}"
    fi
}

# ===========================================
# GC 鐟欙箑褰?# ===========================================
run_gc() {
    local gc_type="${1:-full}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[闁挎瑨顕 ${APP_NAME} 閺堫亜婀潻鎰攽${NC}"
        return 1
    fi
    
    check_java
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    if ! command -v "${jcmd_cmd}" &> /dev/null; then
        echo -e "${RED}[闁挎瑨顕 閺堫亝澹橀崚?jcmd 閸涙垝鎶?{NC}"
        return 1
    fi
    
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    echo -e "${GREEN}                    鐟欙箑褰?GC${NC}"
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    echo ""
    echo -e "  PID:      ${pid}"
    echo -e "  GC 缁鐎? ${gc_type}"
    echo ""
    
    # GC 閸撳秶濮搁幀?    echo -e "${CYAN}[GC 閸撳秶濮搁幀涔?{NC}"
    local jstat_cmd="jstat"
    [ -n "$JAVA_HOME" ] && jstat_cmd="$JAVA_HOME/bin/jstat"
    if command -v "${jstat_cmd}" &> /dev/null; then
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    echo ""
    
    case "${gc_type}" in
        young|minor)
            echo -e "${YELLOW}鐟欙箑褰?Young GC...${NC}"
            "${jcmd_cmd}" "$pid" GC.run 2>/dev/null
            ;;
        full|major)
            echo -e "${YELLOW}鐟欙箑褰?Full GC...${NC}"
            "${jcmd_cmd}" "$pid" GC.run 2>/dev/null
            ;;
        *)
            echo -e "${RED}[闁挎瑨顕 閺堫亞鐓￠惃?GC 缁鐎? ${gc_type}${NC}"
            echo "閺€顖涘瘮閻ㄥ嫮琚崹? young, full"
            return 1
            ;;
    esac
    
    sleep 2
    
    # GC 閸氬海濮搁幀?    echo ""
    echo -e "${CYAN}[GC 閸氬海濮搁幀涔?{NC}"
    if command -v "${jstat_cmd}" &> /dev/null; then
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    
    echo ""
    echo -e "${GREEN}[鐎瑰本鍨歖 GC 瀹歌尪袝閸?{NC}"
}

# ===========================================
# 妤?CPU 缁捐法鈻奸崚鍡樼€?# ===========================================
run_top_cpu() {
    local top_n="${1:-10}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[闁挎瑨顕 ${APP_NAME} 閺堫亜婀潻鎰攽${NC}"
        return 1
    fi
    
    check_java
    local jstack_cmd="jstack"
    [ -n "$JAVA_HOME" ] && jstack_cmd="$JAVA_HOME/bin/jstack"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        echo -e "${RED}[闁挎瑨顕 閺堫亝澹橀崚?jstack 閸涙垝鎶?{NC}"
        return 1
    fi
    
    echo -e "${BLUE}閳烘柡鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫧${NC}"
    echo -e "${BLUE}閳?{NC}${GREEN}                妤?CPU 缁捐法鈻奸崚鍡樼€?(Top ${top_n})                  ${NC}${BLUE}閳?{NC}"
    echo -e "${BLUE}閳烘埃鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ殕${NC}"
    echo ""
    echo -e "  鏉╂稓鈻?PID: ${pid}"
    echo -e "  鎼存梻鏁ら崥宥囆? ${APP_NAME}"
    echo ""
    
    # 閼惧嘲褰囨?CPU 缁捐法鈻?    echo -e "${CYAN}[濮濄儵顎?1] 閼惧嘲褰囨?CPU 缁捐法鈻?..${NC}"
    local tmp_threads=$(mktemp)
    local tmp_jstack=$(mktemp)
    
    # 閼惧嘲褰囩痪璺ㄢ柤 CPU 娴ｈ法鏁ら悳?    top -b -n 1 -H -p "$pid" 2>/dev/null | grep -E "^\s*[0-9]+" | head -n "$top_n" > "$tmp_threads"
    
    if [ ! -s "$tmp_threads" ]; then
        echo -e "${YELLOW}[鐠€锕€鎲 閺冪姵纭堕柅姘崇箖 top 閼惧嘲褰囩痪璺ㄢ柤娣団剝浼呴敍灞界毦鐠?ps...${NC}"
        ps -eLo pid,lwp,%cpu,comm --sort=-%cpu | grep "$pid" | head -n "$top_n" > "$tmp_threads"
    fi
    
    # 閼惧嘲褰囩痪璺ㄢ柤閸棙鐖?    echo -e "${CYAN}[濮濄儵顎?2] 閼惧嘲褰囩痪璺ㄢ柤閸棙鐖?..${NC}"
    "${jstack_cmd}" -l "$pid" > "$tmp_jstack" 2>/dev/null
    
    if [ ! -s "$tmp_jstack" ]; then
        echo -e "${RED}[闁挎瑨顕 閺冪姵纭堕懢宄板絿缁捐法鈻奸崼鍡樼垽${NC}"
        rm -f "$tmp_threads" "$tmp_jstack"
        return 1
    fi
    
    # 閸掑棙鐎介獮鑸垫▔缁€?    echo ""
    echo -e "${CYAN}[濮濄儵顎?3] 閸掑棙鐎芥?CPU 缁捐法鈻奸崼鍡樼垽...${NC}"
    echo -e "${BLUE}閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓${NC}"
    
    local count=0
    while read -r line; do
        # 鐟欙絾鐎界痪璺ㄢ柤 ID 閸?CPU
        local lwp=$(echo "$line" | awk '{print $1}')
        local cpu=$(echo "$line" | awk '{print $9}')
        
        # 婵″倹鐏夐弰?ps 鏉堟挸鍤弽鐓庣础
        if [[ "$line" =~ ^[[:space:]]*[0-9]+[[:space:]]+[0-9]+ ]]; then
            lwp=$(echo "$line" | awk '{print $2}')
            cpu=$(echo "$line" | awk '{print $3}')
        fi
        
        [ -z "$lwp" ] && continue
        
        # 鏉烆剚宕叉稉?16 鏉╂稑鍩?        local nid=$(printf "%x" "$lwp" 2>/dev/null)
        [ -z "$nid" ] && continue
        
        count=$((count + 1))
        echo ""
        echo -e "${YELLOW}#${count} 缁捐法鈻?ID: ${lwp} (0x${nid})  CPU: ${cpu}%${NC}"
        
        # 閸?jstack 娑擃厽鐓￠幍鎯ь嚠鎼存梻娈戠痪璺ㄢ柤閸棙鐖?        local found=false
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
                # 閺勫墽銇氶崼鍡樼垽 (閺堚偓婢?15 鐞?
                if [ $stack_lines -lt 15 ]; then
                    # 妤傛ü瀵掗弰鍓с仛娑撴艾濮熸禒锝囩垳
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
            echo -e "${YELLOW}  閺冪姵纭堕幍鎯у煂缁捐法鈻奸崼鍡樼垽 (nid=0x${nid})${NC}"
        fi
        
        echo -e "${BLUE}閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓${NC}"
        
        [ $count -ge $top_n ] && break
    done < "$tmp_threads"
    
    rm -f "$tmp_threads" "$tmp_jstack"
    
    echo ""
    echo -e "${GREEN}[鐎瑰本鍨歖 閸掑棙鐎界紒鎾存将${NC}"
    echo -e "${CYAN}閹绘劗銇? 缂佽儻澹婃妯瑰瘨閻ㄥ嫭妲告稉姘娴狅絿鐖滈敍宀勨偓姘埗闂団偓鐟曚線鍣搁悙鐟板彠濞?{NC}"
}

# ===========================================
# 妤傛ê鍞寸€涙ê顕挒鈥冲瀻閺?# ===========================================
run_top_mem() {
    local top_n="${1:-10}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[闁挎瑨顕 ${APP_NAME} 閺堫亜婀潻鎰攽${NC}"
        return 1
    fi
    
    check_java
    local jmap_cmd="jmap"
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jmap_cmd="$JAVA_HOME/bin/jmap" && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    echo -e "${BLUE}閳烘柡鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫧${NC}"
    echo -e "${BLUE}閳?{NC}${GREEN}               妤傛ê鍞寸€涙ê顕挒鈥冲瀻閺?(Top ${top_n})                   ${NC}${BLUE}閳?{NC}"
    echo -e "${BLUE}閳烘埃鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ殕${NC}"
    echo ""
    echo -e "  鏉╂稓鈻?PID: ${pid}"
    echo -e "  鎼存梻鏁ら崥宥囆? ${APP_NAME}"
    echo ""
    
    # 鐏忔繆鐦担璺ㄦ暏 jcmd (閺囨潙鐣ㄩ崗?
    local histo_output=""
    if command -v "${jcmd_cmd}" &> /dev/null; then
        echo -e "${CYAN}[濮濄儵顎?1] 娴ｈ法鏁?jcmd 閼惧嘲褰囩€电钖勯惄瀛樻煙閸?..${NC}"
        histo_output=$("${jcmd_cmd}" "$pid" GC.class_histogram 2>/dev/null)
    fi
    
    # 婵″倹鐏?jcmd 婢惰精瑙﹂敍灞界毦鐠?jmap
    if [ -z "$histo_output" ] && command -v "${jmap_cmd}" &> /dev/null; then
        echo -e "${CYAN}[濮濄儵顎?1] 娴ｈ法鏁?jmap 閼惧嘲褰囩€电钖勯惄瀛樻煙閸?..${NC}"
        histo_output=$("${jmap_cmd}" -histo:live "$pid" 2>/dev/null)
    fi
    
    if [ -z "$histo_output" ]; then
        echo -e "${RED}[闁挎瑨顕 閺冪姵纭堕懢宄板絿鐎电钖勯惄瀛樻煙閸?{NC}"
        return 1
    fi
    
    echo ""
    echo -e "${CYAN}[濮濄儵顎?2] 閸掑棙鐎芥妯哄敶鐎涙ê顕挒?..${NC}"
    echo ""
    
    # 閺勫墽銇氶弽鍥暯
    echo -e "${WHITE}  閹烘帒鎮?  鐎圭偘绶ラ弫?         閸楃姷鏁ょ€涙濡?           缁鎮?{NC}"
    echo -e "${BLUE}閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓${NC}"
    
    # 鐟欙絾鐎介獮鑸垫▔缁€?Top N
    local count=0
    echo "$histo_output" | grep -E "^\s*[0-9]+:" | head -n "$top_n" | while read -r line; do
        count=$((count + 1))
        local num=$(echo "$line" | awk '{print $1}' | tr -d ':')
        local instances=$(echo "$line" | awk '{print $2}')
        local bytes=$(echo "$line" | awk '{print $3}')
        local class_name=$(echo "$line" | awk '{print $4}')
        
        # 鏉烆剚宕茬€涙濡稉鍝勫讲鐠囩粯鐗稿?        local size_human=""
        if [ "$bytes" -ge 1073741824 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f GB\", $bytes/1073741824}")
        elif [ "$bytes" -ge 1048576 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f MB\", $bytes/1048576}")
        elif [ "$bytes" -ge 1024 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f KB\", $bytes/1024}")
        else
            size_human="${bytes} B"
        fi
        
        # 妤傛ü瀵掓稉姘缁?        if [[ "$class_name" =~ ^(com\.|cn\.|org\..*\.model|org\..*\.entity|org\..*\.dto) ]]; then
            printf "${GREEN}  %4s   %12s   %12s   %s${NC}\n" "$num" "$instances" "$size_human" "$class_name"
        else
            printf "  %4s   %12s   %12s   %s\n" "$num" "$instances" "$size_human" "$class_name"
        fi
    done
    
    echo -e "${BLUE}閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓閳光偓${NC}"
    
    # 閺勫墽銇氶崘鍛摠濮掑倽顫?    echo ""
    echo -e "${CYAN}[閸愬懎鐡ㄥ鍌濐潔]${NC}"
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
        echo -e "  閹鐤勬笟瀣殶: ${WHITE}${total_instances}${NC}"
        echo -e "  閹宕伴悽?   ${WHITE}${total_human}${NC}"
    fi
    
    echo ""
    echo -e "${GREEN}[鐎瑰本鍨歖 閸掑棙鐎界紒鎾存将${NC}"
    echo -e "${CYAN}閹绘劗銇? 缂佽儻澹婃妯瑰瘨閻ㄥ嫭妲告稉姘缁紮绱濋柅姘埗闂団偓鐟曚線鍣搁悙鐟板彠濞?{NC}"
    echo -e "${CYAN}      婵″倿娓堕弴纾嬵嚊缂佸棗鍨庨弸鎰剁礉鐠囪渹濞囬悽? $0 dump heap${NC}"
}

# ===========================================
# 閺堝秴濮熺粻锛勬倞 (systemd)
# ===========================================
SERVICE_NAME="${APP_NAME}.service"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}"

install_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[闁挎瑨顕 闂団偓鐟?root 閺夊啴妾洪幍褑顢戝銈呮嚒娴?{NC}"
        echo "鐠囪渹濞囬悽? sudo $0 install-service"
        return 1
    fi
    
    if [ -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[鐠€锕€鎲 閺堝秴濮熷鎻掔摠閸? ${SERVICE_NAME}${NC}"
        read -p "閺勵垰鎯佺憰鍡欐磰? (y/N): " confirm
        [ "$confirm" != "y" ] && [ "$confirm" != "Y" ] && return 0
    fi
    
    check_java
    local java_path=$(which "$JAVA_CMD" 2>/dev/null || echo "$JAVA_CMD")
    local run_user=$(stat -c '%U' "${APP_HOME}" 2>/dev/null || echo "root")
    
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    echo -e "${GREEN}              鐎瑰顥?Systemd 閺堝秴濮?{NC}"
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    echo ""
    echo -e "  閺堝秴濮熼崥宥囆? ${SERVICE_NAME}"
    echo -e "  鏉╂劘顢戦悽銊﹀煕: ${run_user}"
    echo -e "  Java 鐠侯垰绶? ${java_path}"
    echo -e "  鎼存梻鏁ら惄顔肩秿: ${APP_HOME}"
    echo ""
    
    # 閻㈢喐鍨?systemd 閺堝秴濮熼弬鍥︽
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
    
    # 闁插秵鏌婇崝鐘烘祰 systemd
    systemctl daemon-reload
    
    echo -e "${GREEN}[閹存劕濮沒 閺堝秴濮熷鎻掔暔鐟? ${SERVICE_NAME}${NC}"
    echo ""
    echo -e "${CYAN}閸欘垳鏁ら崨鎴掓姢:${NC}"
    echo "  systemctl start ${SERVICE_NAME}     # 閸氼垰濮╅張宥呭"
    echo "  systemctl stop ${SERVICE_NAME}      # 閸嬫粍顒涢張宥呭"
    echo "  systemctl restart ${SERVICE_NAME}   # 闁插秴鎯庨張宥呭"
    echo "  systemctl status ${SERVICE_NAME}    # 閺屻儳婀呴悩鑸碘偓?
    echo "  systemctl enable ${SERVICE_NAME}    # 瀵偓閺堥缚鍤滈崥?
    echo "  journalctl -u ${SERVICE_NAME} -f    # 閺屻儳婀呴弮銉ョ箶"
}

uninstall_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[闁挎瑨顕 闂団偓鐟?root 閺夊啴妾洪幍褑顢戝銈呮嚒娴?{NC}"
        echo "鐠囪渹濞囬悽? sudo $0 uninstall-service"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[鐠€锕€鎲 閺堝秴濮熸稉宥呯摠閸? ${SERVICE_NAME}${NC}"
        return 0
    fi
    
    echo -e "${BLUE}閸楁瓕娴囬張宥呭: ${SERVICE_NAME}...${NC}"
    
    # 閸嬫粍顒涢張宥呭
    systemctl stop "${SERVICE_NAME}" 2>/dev/null
    systemctl disable "${SERVICE_NAME}" 2>/dev/null
    
    # 閸掔娀娅庨張宥呭閺傚洣娆?    rm -f "${SERVICE_FILE}"
    systemctl daemon-reload
    
    echo -e "${GREEN}[閹存劕濮沒 閺堝秴濮熷鎻掑祻鏉?{NC}"
}

enable_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[闁挎瑨顕 闂団偓鐟?root 閺夊啴妾洪幍褑顢戝銈呮嚒娴?{NC}"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${RED}[闁挎瑨顕 閺堝秴濮熼張顏勭暔鐟佸拑绱濈拠宄板帥閹笛嗩攽: $0 install-service${NC}"
        return 1
    fi
    
    systemctl enable "${SERVICE_NAME}"
    echo -e "${GREEN}[閹存劕濮沒 瀹告彃鎯庨悽銊ョ磻閺堥缚鍤滈崥?{NC}"
}

disable_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[闁挎瑨顕 闂団偓鐟?root 閺夊啴妾洪幍褑顢戝銈呮嚒娴?{NC}"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[鐠€锕€鎲 閺堝秴濮熼張顏勭暔鐟?{NC}"
        return 0
    fi
    
    systemctl disable "${SERVICE_NAME}"
    echo -e "${GREEN}[閹存劕濮沒 瀹歌尙顩﹂悽銊ョ磻閺堥缚鍤滈崥?{NC}"
}

show_service_status() {
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[鐠€锕€鎲 閺堝秴濮熼張顏勭暔鐟? ${SERVICE_NAME}${NC}"
        echo "娴ｈ法鏁?'$0 install-service' 鐎瑰顥婇張宥呭"
        return 0
    fi
    
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    echo -e "${GREEN}              Systemd 閺堝秴濮熼悩鑸碘偓?{NC}"
    echo -e "${BLUE}閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜閳烘劏鏅查埡鎰ㄦ櫜${NC}"
    echo ""
    systemctl status "${SERVICE_NAME}" --no-pager
}

# ===========================================
# 閸欏倹鏆熺憴锝嗙€?# ===========================================
COMMAND="start"
DEBUG_PORT="${DEFAULT_DEBUG_PORT}"
DEBUG_MODE="false"
CUSTOM_JVM_OPTS=""
CUSTOM_JAVA=""
TAIL_LOG="false"
SKYWALKING_ENABLED="false"
SHOW_LOGS=""
JAVA_CMD_ARGS=()
SKYWALKING_SUBCMD_ARGS=()
BACKUP_CMD_ARGS=()

TOP_N="10"

while [ $# -gt 0 ]; do
    case "$1" in
        start|stop|restart|status|info|jvm|tuning|analyze|dump|arthas|restore|upgrade|rollback|rollback-status|debug)
            COMMAND="$1"; shift ;;
        skywalking)
            COMMAND="skywalking"
            shift
            SKYWALKING_SUBCMD_ARGS=("$@")
            break
            ;;
        backup)
            COMMAND="backup"
            shift
            BACKUP_CMD_ARGS=("$@")
            break
            ;;
        gc)
            COMMAND="gc"
            shift
            # 濡偓閺屻儰绗呮稉鈧稉顏勫棘閺佺増妲搁崥锔芥Ц GC 缁鐎?            if [[ "$1" =~ ^(young|full|minor|major)$ ]]; then
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
            # 濡偓閺屻儰绗呮稉鈧稉顏勫棘閺佺増妲搁崥锔芥Ц閺佹澘鐡?            if [[ "$1" =~ ^[0-9]+$ ]]; then
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
            # 娣囨繂鐡?java 閸涙垝鎶ら惃鍕閺堝鎮楃紒顓炲棘閺?            JAVA_CMD_ARGS=("$@")
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
            # 濡偓閺屻儰绗呮稉鈧稉顏勫棘閺佺増妲搁崥锔芥Ц鐠侯垰绶?(娑撳秳浜?- 瀵偓婢?
            if [ -n "$1" ] && [[ ! "$1" =~ ^- ]]; then
                CUSTOM_JAVA="$1"
                # 娣囨繂鐡?Java 鐠侯垰绶?                if [ -x "$CUSTOM_JAVA" ]; then
                    save_java_path "$CUSTOM_JAVA"
                fi
                shift
            else
                # 閺冪姴寮弫甯礉閺勫墽銇氳ぐ鎾冲 Java 娣団剝浼?                show_java_info
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
            # 濡偓閺屻儲妲搁崥锔芥Ц閺佹澘鐡?(閻劋绨?logs/top-cpu/top-mem 閸涙垝鎶?
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                case "$COMMAND" in
                    logs) SHOW_LOGS="$1" ;;
                    top-cpu|top-mem) TOP_N="$1" ;;
                    *) echo -e "${RED}[闁挎瑨顕 閺堫亞鐓￠崣鍌涙殶: $1${NC}"; show_help; exit 1 ;;
                esac
                shift
            else
                echo -e "${RED}[闁挎瑨顕 閺堫亞鐓￠崣鍌涙殶: $1${NC}"
                show_help
                exit 1
            fi
            ;;
    esac
done

[ "$COMMAND" = "debug" ] && DEBUG_MODE="true" && COMMAND="start"

# ===========================================
# 閸涙垝鎶ら幍褑顢?# ===========================================
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
    backup) run_backup "${BACKUP_CMD_ARGS[@]}" ;;
    restore) run_restore "${EXTRA_MODE:-all}" ;;
    upgrade) run_upgrade "${UPGRADE_PACKAGE}" "${NO_RESTART}" ;;
    rollback) run_rollback ;;
    rollback-status) run_rollback_status ;;
    skywalking) run_skywalking "${SKYWALKING_SUBCMD_ARGS[@]}" ;;
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
