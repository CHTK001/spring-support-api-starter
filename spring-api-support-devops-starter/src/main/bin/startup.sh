#!/bin/bash
# ============================================================
# 缂備胶鍠嶇粩瀛樻償閺冨倹鏆忕紒鐙呯磿閹﹪鎳樺顓熸嫳
# 闁衡偓椤栨稑鐦柛鏂诲妽閳ь兛绀侀幃鏇犵矓閻楀牊绂岄柟?(Maven 閻犙冨缁喗娼婚崶銊﹀Б)
# 濞达絾绮忛埀? DevOps
# 闁绘鐗婂﹢? 2.0.0
# ============================================================

# ===========================================
# 闁糕晞娅ｉ、鍛存煀瀹ュ洨鏋?(Maven 閻犙冨缁喗娼婚崶銊﹀Б闁哄洦瀵у畷?
# ===========================================
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_NAME="${APP_NAME:-@project.artifactId@}"
APP_VERSION="${APP_VERSION:-@project.version@}"
JAR_NAME="${APP_NAME}-${APP_VERSION}.jar"

# 闁烩晩鍠栫紞宥夋煀瀹ュ洨鏋?CONFIG_DIR="${APP_HOME}/config"
LIB_DIR="${APP_HOME}/lib"
LOG_DIR="${APP_HOME}/logs"
PID_FILE="${APP_HOME}/${APP_NAME}.pid"
BACKUP_DIR="${APP_HOME}/backup"

# 闁哄啨鍎辩换鏃堝棘閸ワ附顐介梺鏉跨Ф閻?STARTUP_LOG="${LOG_DIR}/startup.log"
APP_LOG="${LOG_DIR}/${APP_NAME}.log"

# 閻犲鍟抽惁顖滅博椤栨艾缍撻梺鏉跨Ф閻?DEFAULT_DEBUG_PORT=5005

# Docker 闂佹澘绉堕悿?DOCKER_REGISTRY="${DOCKER_REGISTRY:-}"
DOCKER_IMAGE_NAME="${DOCKER_IMAGE_NAME:-${APP_NAME}}"
DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG:-${APP_VERSION}}"
DOCKER_CONTAINER_NAME="${DOCKER_CONTAINER_NAME:-${APP_NAME}}"
DOCKER_PORT="${DOCKER_PORT:-8080}"
DOCKER_HOST_PORT="${DOCKER_HOST_PORT:-8080}"

# Skywalking 闂佹澘绉堕悿?SKYWALKING_VERSION="${SKYWALKING_VERSION:-9.0.0}"
SKYWALKING_DIR="${APP_HOME}/skywalking"
SKYWALKING_AGENT_JAR="${SKYWALKING_DIR}/skywalking-agent/skywalking-agent.jar"
SW_AGENT_NAME="${SW_AGENT_NAME:-${APP_NAME}}"
SW_AGENT_COLLECTOR_BACKEND_SERVICES="${SW_AGENT_COLLECTOR_BACKEND_SERVICES:-127.0.0.1:11800}"

# JVM闂佹澘绉堕悿?JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}"
JAVA_OPTS="${JAVA_OPTS} -Dlogging.file.path=${LOG_DIR}"

# Spring闂佹澘绉堕悿?SPRING_PROFILE="${SPRING_PROFILE:-prod}"

# 闁哄牆绉存慨鐔虹博椤栨艾缍撻梺鏉跨Ф閻?SERVER_PORT="${SERVER_PORT:-8080}"

# 濡増绮忔竟濠勨偓瑙勭煯缁?RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
WHITE='\033[1;37m'
NC='\033[0m'

# ===========================================
# 閻㈩垼鍠栨慨顏呯┍閳╁啩绱?# ===========================================
show_help() {
    echo -e "${BLUE}闁崇儤鏌￠弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽${NC}"
    echo -e "${BLUE}闁?{NC}${GREEN}              ${APP_NAME} 缂佺媴绱曢幃濠囨嚇濮橆厽鎷?                   ${NC}${BLUE}闁?{NC}"
    echo -e "${BLUE}闁?{NC}${CYAN}              闁绘鐗婂﹢? ${APP_VERSION}                              ${NC}${BLUE}闁?{NC}"
    echo -e "${BLUE}闁崇儤鍩冮弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽畷${NC}"
    echo ""
    echo -e "${YELLOW}闁活潿鍔嶇涵?${NC} $0 [闁告稒鍨濋幎顦?[闂侇偄顦甸妴宄?
    echo ""
    echo -e "${YELLOW}闁糕晞娅ｉ、鍛村川閹存帗濮?${NC}"
    echo "  start              闁告凹鍨版慨鈺傛償閺冨倹鏆?(濮掓稒顭堥?"
    echo "  stop               闁稿绮嶉娑欐償閺冨倹鏆?
    echo "  restart            闂佹彃绉撮幆搴㈡償閺冨倹鏆?
    echo "  status             闁哄被鍎冲﹢鍛償閺冨倹鏆忛柣妯垮煐閳?
    echo "  info               闁哄嫬澧介妵姘辨嫚閿斿墽鐭庨弶鈺傚姌椤㈡垶绌遍埄鍐х礀"
    echo ""
    echo -e "${YELLOW}闁哄啨鍎辩换鏃堝川閹存帗濮?${NC}"
    echo "  logs [N]           闁哄被鍎冲﹢鍛村嫉閳ь剚娼?N 闁哄鍓濆Λ鈺勭疀?(濮掓稒顭堥? 100)"
    echo "  tail               閻庡湱鍋炲鍌滄崉閻斿鍤嬮柡鍐﹀劚缁绘梹娼忛幘鍐叉瘔"
    echo ""
    echo -e "${YELLOW}閻犲洤锕ラ弻鍥川閹存帗濮?${NC}"
    echo "  jvm                闁哄被鍎冲﹢鍖M濞ｅ洠鍓濇导?
    echo "  gc                 閻熸瑱绠戣ぐ?GC (young/full)"
    echo "  tuning             JVM閻犲鍟╃槐顓烆嚈妤︽鍞?
    echo "  analyze            CPU/闁告劕鎳庨悺銊╁礆閸℃鈧?
    echo "  top-cpu            闁告帒妫欓悗鑺ヮ殗濡ゎ櫀U缂佹崘娉曢埢濂稿矗婵犱礁鏁╅柣顔荤閻栥垽寮?(Top N)"
    echo "  top-mem            闁告帒妫欓悗鑺ヮ殗濡搫鏁堕悗娑櫭顔炬寬?(Top N)"
    echo "  dump               闁汇垻鍠愰崹姘姜椤掆偓閸嬪秹寮崶锔筋偨 (heap/thread/all)"
    echo "  arthas             闁告凹鍨版慨銆thas閻犲洤锕ラ弻?
    echo ""
    echo -e "${YELLOW}闁哄牆绉存慨鐔虹不閿涘嫭鍊為柛娑欏灊閹?${NC}"
    echo "  install-service    閻庣懓顦抽ˉ濠冪▔?systemd 闁哄牆绉存慨?
    echo "  uninstall-service  闁告鐡曞ù?systemd 闁哄牆绉存慨?
    echo "  enable-service     闁告凹鍨抽弫銈咁嚕閳ь剟寮垫ウ鍨闁?
    echo "  disable-service    缂佸倷鑳堕弫銈咁嚕閳ь剟寮垫ウ鍨闁?
    echo "  service-status     闁哄被鍎冲﹢鍛村嫉瀹ュ懎顫ら柣妯垮煐閳?
    echo ""
    echo -e "${YELLOW}閺夆晜鍔楀ǎ顕€宕ㄩ幋鎺撳Б:${NC}"
    echo "  backup             Create backup (config/JAR)"
    echo "  backup list        List all backups with index"
    echo "  restore            Restore from latest backup"
    echo "  restore list       List all backups with index"
    echo "  restore <N>        Restore to backup N"
    echo "  upgrade            濞寸姴楠哥敮鍥╃磽閳轰礁鐦堕柛妤€娲ㄦ?(闁衡偓椤栨稑鐦?zip/tar.gz)"
    echo "  rollback           闁搞儳鍋炵划鎾礆妫颁胶鐟愬☉鎾亾濞戞搩浜ｇ换鏇㈠储閻斿搫浠?
    echo "  rollback-status    闁哄被鍎冲﹢鍛交濡搫鏂ч柣鎰贡婵悂骞€?
    echo "  debug              濞寸姰鍎撮惃鐔烘嫚閺囩唽浣割嚕韫囨挻鍎欓柛?
    echo ""
    echo -e "${YELLOW}闁绘粠鍨伴。銊х不閿涘嫭鍊為柛娑欏灊閹?${NC}"
    echo "  java               缂佺媴绱曢幃?Java 闁绘粠鍨伴。?(install/list/check/remove)"
    echo "  skywalking         缂佺媴绱曢幃?Skywalking Agent (install/check/clean/info)"
    echo ""
    echo -e "${YELLOW}Docker 闁告稒鍨濋幎?${NC}"
    echo "  docker-build       闁哄瀚紓?Docker 闂傗偓濠婂啫鍓?
    echo "  docker-deploy      闁哄瀚紓鎾荤嵁閸洖鍔ョ紓鍐╁絻閸╁矂寮甸浣圭皻 (濞戞挴鍋撻梺娆惧枛瑜板倻鏁?"
    echo "  publish            docker-deploy 闁汇劌瀚悾婵嬪礃?
    echo "  docker-start       闁告凹鍨版慨?Docker 閻庡湱鎳撳▍?
    echo "  docker-stop        闁稿绮嶉?Docker 閻庡湱鎳撳▍?
    echo "  docker-restart     闂佹彃绉撮幆?Docker 閻庡湱鎳撳▍?
    echo "  docker-status      闁哄被鍎冲﹢鍛偓鍦嚀濞呮帡鎮╅懜纰樺亾?
    echo "  docker-logs        闁哄被鍎冲﹢鍛偓鍦嚀濞呮帡寮妷銉х"
    echo "  docker-clean       婵炴挸鎳愰幃濠囧籍瑜旈弳鍛村磽?
    echo ""
    echo -e "${YELLOW}闂侇偄顦甸妴?${NC}"
    echo "  --java [path]         闁圭娲ら悾?Java 閻犱警鍨扮欢?(闁哄啰濮村顒勫极閻楀牊顦ч柡鍕⒔閵囨俺銇愰幘鍐差枀 Java)"
    echo "  --jvm-opts <opts>     闁煎浜滈悾鐐▕婵夊M闁告瑥鍊归弳?
    echo "  --debug-port <port>   闁圭娲ら悾鍓ф嫬閸愵厾妲哥紒鏃戝灠瑜?(濮掓稒顭堥? ${DEFAULT_DEBUG_PORT})"
    echo "  --docker-port <port>  闁圭娲ら悾鍓р偓鍦嚀濞呮帡宕橀崨鏉戝姤缂佹棏鍨拌ぐ?(濮掓稒顭堥? 8080)"
    echo "  --host-port <port>    闁圭娲ら悾鐐▔缂佹ɑ绨氶柡鍕Т閻ㄧ姷绮╅姘稉 (濮掓稒顭堥? 8080)"
    echo "  --image-tag <tag>     闁圭娲ら悾楣冩⒐濠婂啫鍓奸柡宥呮川椤?(濮掓稒顭堥? ${APP_VERSION})"
    echo "  --skywalking          闁告凹鍨抽弫?Skywalking APM"
    echo "  --sw-backend <addr>   闁圭娲ら悾?Skywalking OAP 闁革附婢樺?
    echo "  --sw-name <name>      闁圭娲ら悾?Skywalking 闁哄牆绉存慨鐔煎触?
    echo "  --package <path>      闁告娲ㄦ鍥礌閸涙壆鐔呯€?zip/tar.gz)"
    echo "  --no-restart          闁告娲ㄦ鍥触鎼存繄鐟濋柤濂変簻婵晠鏌屽鍛剻"
    echo "  --dev                 濞达綀娉曢弫銈咁嚕閳ь剟宕ｉ幋鐘茬畾濠?dev)"
    echo "  --prod                濞达綀娉曢弫銈夋偨閻斿爼鐛撻柣婊庡灠椤?(prod, 濮掓稒顭堥?"
    echo "  --test                濞达綀娉曢弫銈吤圭€ｎ厾妲搁柣婊庡灠椤?(test)"
    echo "  --profile <name>      闁圭娲ら悾楣冩嚊椤忓嫮鏆板☉鏂款槺楠炲棙鏅?
    echo "  --output <path>       dump 閺夊牊鎸搁崵顓㈡儎椤旇偐绉?
    echo "  --dump-path <path>    dump 閺夊牊鎸搁崵顓㈡儎椤旇偐绉?(--output 闁告帩鍋勯幃?"
    echo "  --logs <N>            闁告凹鍨版慨鈺呭触鎼淬垺鈻旂紒鈧悜妯讳粯閺?N 闁哄鍓濆Λ鈺勭疀?
    echo "  --tail                闁告凹鍨版慨鈺呭触鎼达紕鏉介柡鍐╁劶缁愶繝鐓鍛級闊?
    echo "  --help, -h            闁哄嫬澧介妵姘辨暜椤旂厧袠濞ｅ洠鍓濇导?
    echo ""
    echo -e "${YELLOW}缂佲偓鏉炴壆浼?${NC}"
    echo ""
    echo -e "  ${CYAN}闁糕晞娅ｉ、鍛村川閹存帗濮?${NC}"
    echo "    $0 start                             闁告凹鍨版慨鈺傛償閺冨倹鏆?
    echo "    $0 start --dev                       鐎殿喒鍋撻柛娆愬灩楠炲棙鏅堕崘銊﹀剻闁?
    echo "    $0 start --prod                      闁汇垻鍠嶆鍥偝椤栨凹鏆旈柛姘煎灠婵?
    echo "    $0 start --test                      婵炴潙顑堥惁顖炴偝椤栨凹鏆旈柛姘煎灠婵?
    echo "    $0 start --profile staging           闁煎浜滈悾鐐▕婢跺苯绠氬褍鍟幆搴ㄥ礉?
    echo "    $0 start --tail                      闁告凹鍨版慨鈺呯嵁鐠鸿櫣鏉介柡鍐╁劶缁愶繝鐓鍛級闊?
    echo "    $0 start --logs 200                  闁告凹鍨版慨鈺呭触鎼淬垺鈻旂紒鈧悜妯讳粯閺?200 闁哄鍓濆Λ鈺勭疀?
    echo "    $0 start --java /opt/jdk17/bin/java  闁圭娲ら悾?Java 閻犱警鍨扮欢鐐哄触椤栨艾袟 (妤犵偞婀圭换姘扁偓?"
    echo "    $0 --java                            闁哄嫬澧介妵姘炽亹閹惧啿顤呭ù锝堟硶閺併倝鎯?Java 闁绘鐗婂﹢?
    echo "    $0 stop                              闁稿绮嶉娑欐償閺冨倹鏆?
    echo "    $0 restart                           闂佹彃绉撮幆搴㈡償閺冨倹鏆?
    echo "    $0 status                            闁哄被鍎冲﹢鍛存偐閼哥鍋?
    echo "    $0 info                              闁哄被鍎冲﹢鍛嫚閿斿墽鐭庡ǎ鍥ｅ墲娴?
    echo ""
    echo -e "  ${CYAN}闁哄啨鍎辩换鏃堝川閹存帗濮?${NC}"
    echo "    $0 logs                              闁哄被鍎冲﹢鍛村嫉閳ь剚娼?100 闁哄鍓濆Λ鈺勭疀?
    echo "    $0 logs 50                           闁哄被鍎冲﹢鍛村嫉閳ь剚娼?50 闁哄鍓濆Λ鈺勭疀?
    echo "    $0 tail                              閻庡湱鍋炲鍌滄崉閻斿鍤嬮柡鍐﹀劚缁?
    echo ""
    echo -e "  ${CYAN}閻犲鍟抽惁顖炲川閹存帗濮?${NC}"
    echo "    $0 debug                             閻犲鍟抽惁顖毼熼垾宕囩闁告凹鍨版慨?(缂佹棏鍨拌ぐ?5005)"
    echo "    $0 debug --debug-port 5006           閻犲鍟抽惁顖毼熼垾宕囩闁告凹鍨版慨?(闁圭娲ら悾鍓х博椤栨艾缍?"
    echo ""
    echo -e "  ${CYAN}JVM 閻犲洤锕ラ弻?${NC}"
    echo "    $0 jvm                               闁哄被鍎冲﹢?JVM 濞ｅ洠鍓濇导?
    echo "    $0 gc                                閻熸瑱绠戣ぐ?Full GC"
    echo "    $0 gc young                          閻熸瑱绠戣ぐ?Young GC"
    echo "    $0 top-cpu                           濡?CPU 缂佹崘娉曢埢濂稿礆閸℃鈧?(Top 10)"
    echo "    $0 top-cpu 20                        濡?CPU 缂佹崘娉曢埢濂稿礆閸℃鈧?(Top 20)"
    echo "    $0 top-mem                           濡ゅ倹锚閸炲鈧稒锚椤曨喚鎸掗垾鍐茬€婚柡?(Top 10)"
    echo "    $0 top-mem 20                        濡ゅ倹锚閸炲鈧稒锚椤曨喚鎸掗垾鍐茬€婚柡?(Top 20)"
    echo "    $0 tuning                            JVM 閻犲鍟╃槐顓烆嚈妤︽鍞?
    echo "    $0 arthas                            闁告凹鍨版慨?Arthas 閻犲洤锕ラ弻?
    echo ""
    echo -e "  ${CYAN}Dump 閺夌儐鍓欓崑?${NC}"
    echo "    $0 dump                              闁汇垻鍠愰崹姘跺醇?缂佹崘娉曢埢鍏兼姜椤掆偓閸?
    echo "    $0 dump heap                         濞寸姴鎳庨悥銏℃姜椤掆偓閸?
    echo "    $0 dump thread                       濞寸姴鎳愰崵搴ｇ矙鐎ｎ厽绁柛?
    echo "    $0 dump --output /tmp/dump           闁圭娲ら悾鐐綇閹惧啿姣夐柣鈺婂枛缂?
    echo ""
    echo -e "  ${CYAN}闁哄牆绉存慨鐔虹不閿涘嫭鍊?(闂傚洠鍋?root 闁哄鍟村?:${NC}"
    echo "    $0 install-service                   閻庣懓顦抽ˉ濠冪▔?systemd 闁哄牆绉存慨?
    echo "    $0 uninstall-service                 闁告鐡曞ù鍥嫉瀹ュ懎顫?
    echo "    $0 enable-service                    闁告凹鍨抽弫銈咁嚕閳ь剟寮垫ウ鍨闁?
    echo "    $0 disable-service                   缂佸倷鑳堕弫銈咁嚕閳ь剟寮垫ウ鍨闁?
    echo "    $0 service-status                    闁哄被鍎冲﹢鍛村嫉瀹ュ懎顫ら柣妯垮煐閳?
    echo ""
    echo -e "  ${CYAN}闁告娲ㄦ鍥炊閻愬娉?${NC}"
    echo "    $0 upgrade --package /tmp/app.zip    濞寸姴楠哥敮鍥╃磽閳轰礁鐦堕柛妤€娲ㄦ?
    echo "    $0 upgrade --package /tmp/app.tar.gz --no-restart  闁告娲ㄦ鍥ㄦ媴閸℃洜鐟濋梺鎻掔Т閹?
    echo "    $0 rollback                          闁搞儳鍋炵划鎾礆妫颁胶鐟愬☉鎾亾濞戞搩浜炴晶妤呭嫉?
    echo "    $0 rollback-status                   闁哄被鍎冲﹢鍛交濡搫鏂ч柣鎰贡婵悂骞€?
    echo "    $0 backup                            Create new backup"
    echo "    $0 backup list                       List all backups"
    echo "    $0 restore                           Restore from latest"
    echo "    $0 restore list                      List all backups"
    echo "    $0 restore 1                         Restore to backup #1"
    echo ""
    echo -e "  ${CYAN}Docker 闂侇喓鍔庣拋?${NC}"
    echo "    $0 docker-build                      闁哄瀚紓?Docker 闂傗偓濠婂啫鍓?
    echo "    $0 docker-deploy                     闁哄瀚紓鎾荤嵁閸洖鍔ョ紓?(濞戞挴鍋撻梺娆惧枛瑜板倻鏁?"
    echo "    $0 publish                           docker-deploy 闁汇劌瀚悾婵嬪礃?
    echo "    $0 docker-start                      闁告凹鍨版慨鈺冣偓鍦嚀濞?
    echo "    $0 docker-stop                       闁稿绮嶉娑氣偓鍦嚀濞?
    echo "    $0 docker-restart                    闂佹彃绉撮幆搴ｂ偓鍦嚀濞?
    echo "    $0 docker-status                     闁哄被鍎冲﹢鍛偓鍦嚀濞呮帡鎮╅懜纰樺亾?
    echo "    $0 docker-logs                       闁哄被鍎冲﹢鍛偓鍦嚀濞呮帡寮妷銉х"
    echo "    $0 docker-clean                      婵炴挸鎳愰幃濠囧籍瑜旈弳鍛村磽?
    echo ""
    echo -e "  ${CYAN}Java 闁绘粠鍨伴。銊х不閿涘嫭鍊?${NC}"
    echo "    $0 java install                      閻庣懓顦抽ˉ?Java (濮掓稒顭堥?21)"
    echo "    $0 java install --version 17         閻庣懓顦抽ˉ濠囧箰閸パ呮毎闁绘鐗婂﹢?Java"
    echo "    $0 java install --dir /opt/java      閻庣懓顦抽ˉ濠囧礆閻楀牆鐦归悗瑙勬皑濞叉媽銇?
    echo "    $0 java list                         闁告帗顨呴崵顓㈠矗椤栨粍鏆?Java 闁绘鐗婂﹢?
    echo "    $0 java check                        婵☆偀鍋撻柡灞诲劚缂嶅宕?Java 闁绘粠鍨伴。?
    echo "    $0 java remove                       闁告帞濞€濞呭骸顔忛幓鎺旀殧閻熶礁鎳愬▓?Java"
    echo ""
    echo -e "  ${CYAN}Skywalking APM:${NC}"
    echo "    $0 start --skywalking                闁告凹鍨抽弫?Skywalking 闁告凹鍨版慨?
    echo "    $0 start --skywalking --sw-backend 192.168.1.100:11800  闁圭娲ら悾?OAP 闁革附婢樺?
    echo "    $0 skywalking install                閻庣懓顦抽ˉ?Skywalking Agent"
    echo "    $0 skywalking check                  婵☆偀鍋撻柡?Agent 闁绘鍩栭埀?
    echo ""
}

# ===========================================
# Java 婵☆偀鍋撴繛?# ===========================================

# Java 闂佹澘绉堕悿鍡涘棘閸ワ附顐介悹渚灠缁?(闁活潿鍔嬬花顒佺┍濠靛棛鎽犻柣顫妽閸╂盯骞愰崶褏鏆伴柣?Java 閻犱警鍨扮欢?
JAVA_CONFIG_FILE="${APP_HOME}/.java_path"

# 濞ｅ洦绻傞悺?Java 閻犱警鍨扮欢鐐哄礆娴兼潙甯崇紓鍐惧枟閺嬪啯绂?save_java_path() {
    local java_path="$1"
    if [ -n "$java_path" ]; then
        echo "$java_path" > "${JAVA_CONFIG_FILE}"
        echo -e "${GREEN}[闁瑰瓨鍔曟慨娌?Java 閻犱警鍨扮欢鐐差啅闊厾绠介悗? ${java_path}${NC}"
    fi
}

# 濞寸姴閰ｉ崢銈囩磾椤旇姤鐎ù鐘烘硾婵偞娼?Java 閻犱警鍨扮欢?load_java_path() {
    if [ -f "${JAVA_CONFIG_FILE}" ]; then
        local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
        if [ -n "$saved_path" ] && [ -x "$saved_path" ]; then
            CUSTOM_JAVA="$saved_path"
        fi
    fi
}

# 婵☆偀鍋撻柡?Java 闁绘粠鍨伴。?check_java() {
    # 闁稿繐鐗嗛惃鍓ф嫚閺囩偛顫ｉ弶鐐村灊缁绘氨鈧稒顭囧▓?Java 閻犱警鍨扮欢?    [ -z "$CUSTOM_JAVA" ] && load_java_path
    
    if [ -n "$CUSTOM_JAVA" ]; then
        if [ -x "$CUSTOM_JAVA" ]; then
            JAVA_CMD="$CUSTOM_JAVA"
        else
            echo -e "${RED}[闂佹寧鐟ㄩ顦?闁圭娲ら悾楣冩儍?Java 閻犱警鍨扮欢鐐寸▔瀹ュ懐鎽犻柛锔哄妽閸ㄣ劍绋夊鍛闁圭瑳鍡╂斀: ${CUSTOM_JAVA}${NC}"
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
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄牜浜濇竟姗€宕氶惂鎱硋a闁绘粠鍨伴。?{NC}"
        echo "閻犲洨鏌夐鏇犵磾?JAVA_HOME 闁瑰瓨鐗旀繛鍥偨?--java 闁圭娲ら悾?Java 閻犱警鍨扮欢?
        exit 1
    fi
}

# 闁兼儳鍢茶ぐ?Java 闁绘鐗婂﹢鐗堢┍閳╁啩绱?get_java_version() {
    "$JAVA_CMD" -version 2>&1 | head -n 1 | awk -F '"' '{print $2}'
}

# 闁哄嫬澧介妵姘炽亹閹惧啿顤?Java 濞ｅ洠鍓濇导?show_java_info() {
    check_java
    
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    echo -e "${GREEN}              鐟滅増鎸告晶?Java 闁绘粠鍨伴。銊︾┍閳╁啩绱?{NC}"
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    echo ""
    
    # 闁哄嫬澧介妵?Java 閻犱警鍨扮欢鐐哄级閵夛妇鐖?    local source="缂侇垵宕电划?PATH"
    if [ -n "$CUSTOM_JAVA" ]; then
        if [ -f "${JAVA_CONFIG_FILE}" ]; then
            source="闂佹澘绉堕悿鍡涘棘閸ワ附顐?(.java_path)"
        else
            source="闁告稒鍨濋幎銈囨偘?--java 闁告瑥鍊归弳?
        fi
    elif [ -n "$JAVA_HOME" ]; then
        source="JAVA_HOME 闁绘粠鍨伴。銊╁矗濮椻偓閸?
    fi
    
    echo -e "  ${CYAN}Java 閻犱警鍨扮欢?${NC}    ${WHITE}${JAVA_CMD}${NC}"
    echo -e "  ${CYAN}閻犱警鍨扮欢鐐哄级閵夛妇鐖?${NC}    ${WHITE}${source}${NC}"
    echo ""
    
    # 闁哄嫬澧介妵姘扁偓鐟版湰閺嗭綁鎯?Java 闁绘鐗婂﹢鐗堢┍閳╁啩绱?    echo -e "  ${CYAN}Java 闁绘鐗婂﹢?${NC}"
    "$JAVA_CMD" -version 2>&1 | while read -r line; do
        echo -e "    ${WHITE}${line}${NC}"
    done
    echo ""
    
    # 闁哄嫬澧介妵姘舵煀瀹ュ洨鏋傞柡鍌氭矗濞嗐垽鎮╅懜纰樺亾?    if [ -f "${JAVA_CONFIG_FILE}" ]; then
        local saved_path=$(cat "${JAVA_CONFIG_FILE}" 2>/dev/null)
        echo -e "  ${CYAN}鐎规瓕寮撶换姘扁偓娑欘焽濞?Java:${NC} ${WHITE}${saved_path}${NC}"
    else
        echo -e "  ${YELLOW}闁圭粯鍔楅妵? 濞达綀娉曢弫?--java <path> 闁圭娲ら悾?Java 閻犱警鍨扮欢鐐虹嵁閺堢數绠介悗?{NC}"
    fi
    echo ""
}

# 闁兼儳鍢茶ぐ鍥嫉椤掍焦绨?IP 闁革附婢樺?get_local_ip() {
    local ip=""
    # 閻忓繑绻嗛惁顖涘緞濮橀硸娼氶柡鍌滄嚀缁憋繝鎳㈠畡鏉跨悼 IP
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

# 濞寸姴閰ｉ崢銈囩磾椤旇姤鐎ù鐘冲劶椤曚即宕ｉ弽顐紓闁?get_server_port() {
    local port="${SERVER_PORT}"
    # 閻忓繑绻嗛惁顖涚?application.yml 閻犲洩顕цぐ?    if [ -f "${CONFIG_DIR}/application.yml" ]; then
        local yml_port=$(grep -E '^\s*port:' "${CONFIG_DIR}/application.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$yml_port" ] && port="$yml_port"
    fi
    # 閻忓繑绻嗛惁顖涚?application-${profile}.yml 閻犲洩顕цぐ?    if [ -f "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" ]; then
        local profile_port=$(grep -E '^\s*port:' "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$profile_port" ] && port="$profile_port"
    fi
    echo "${port:-8080}"
}

# 闁兼儳鍢茶ぐ?context-path
get_context_path() {
    local ctx_path=""
    # 濞寸姴閰ｉ崢銈囩磾椤旇姤鐎ù鐘冲劶椤曚即宕?context-path
    if [ -f "${CONFIG_DIR}/application.yml" ]; then
        ctx_path=$(grep -E 'context-path:' "${CONFIG_DIR}/application.yml" 2>/dev/null | head -1 | awk '{print $2}')
    fi
    if [ -f "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" ]; then
        local profile_ctx=$(grep -E 'context-path:' "${CONFIG_DIR}/application-${SPRING_PROFILE}.yml" 2>/dev/null | head -1 | awk '{print $2}')
        [ -n "$profile_ctx" ] && ctx_path="$profile_ctx"
    fi
    echo "${ctx_path}"
}

# 婵☆偀鍋撴繛鏉戭儐濡叉悂宕ラ敃鈧幆搴ㄦ偨?Swagger
check_swagger_enabled() {
    local swagger_enabled="false"
    # 婵☆偀鍋撻柡灞诲劦閸樸倗绱旈鑺ョ€ù鐘虫构閼垫垿寮伴姘剨闁?swagger/springdoc/knife4j 闂佹澘绉堕悿?    if [ -d "${CONFIG_DIR}" ]; then
        if grep -rq -E '(springdoc|swagger|knife4j)' "${CONFIG_DIR}/" 2>/dev/null; then
            swagger_enabled="true"
        fi
    fi
    # 婵☆偀鍋撻柡?JAR 濞戞搩鍘藉Σ鎼佸触閿曗偓鐎垫﹢宕?swagger 闁烩晝顭堥崣褎绗熷┑濠勵洬
    if [ -f "${APP_HOME}/${JAR_NAME}" ]; then
        if unzip -l "${APP_HOME}/${JAR_NAME}" 2>/dev/null | grep -qE '(springdoc|swagger|knife4j)'; then
            swagger_enabled="true"
        fi
    fi
    echo "$swagger_enabled"
}

# 闁哄瀚紓?loader path
build_loader_path() {
    LOADER_PATH="${CONFIG_DIR}"
    [ -d "${LIB_DIR}" ] && LOADER_PATH="${LOADER_PATH},${LIB_DIR}"
}

# ===========================================
# 閺夆晜绋撻埢鑲╃不閿涘嫭鍊?# ===========================================
get_pid() {
    if [ -f "${PID_FILE}" ]; then
        local pid=$(cat "${PID_FILE}")
        ps -p "$pid" > /dev/null 2>&1 && echo "$pid" && return 0
    fi
    pgrep -f "${JAR_NAME}" 2>/dev/null | head -1
}

# ===========================================
# 闁绘鍩栭埀顑跨劍椤ュ懘寮?# ===========================================
check_status() {
    local pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "${GREEN}[閺夆晜鍔橀、鎴炵▔閻?{NC} ${APP_NAME} (PID: ${pid})"
        return 0
    else
        echo -e "${YELLOW}[鐎瑰憡褰冩禒鐘差潰椤?{NC} ${APP_NAME}"
        return 1
    fi
}

# ===========================================
# 閻犲浄濡囩划蹇旂┍閳╁啩绱栭柡鍕⒔閵?# ===========================================
show_info() {
    local pid=$(get_pid)
    
    echo -e "${BLUE}闁崇儤鏌￠弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽${NC}"
    echo -e "${BLUE}闁?{NC}${GREEN}                    閹煎瓨姊婚弫銈嗘交閹邦垼鏀藉ǎ鍥ｅ墲娴?                             ${NC}${BLUE}闁?{NC}"
    echo -e "${BLUE}闁崇儤鍩冮弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽畷${NC}"
    echo ""
    
    echo -e "${CYAN}闁靛棙鍔曢悢鈧痪顓涘亾濞ｅ洠鍓濇导鍛村Υ?{NC}"
    echo -e "  閹煎瓨姊婚弫銈夊触瀹ュ泦?     ${WHITE}${APP_NAME}${NC}"
    echo -e "  閹煎瓨姊婚弫銈夋偋閸喐鎷?     ${WHITE}${APP_VERSION}${NC}"
    echo -e "  JAR 闁哄倸娲ｅ▎?     ${WHITE}${JAR_NAME}${NC}"
    echo -e "  閹煎瓨姊婚弫銈夋儎椤旇偐绉?     ${WHITE}${APP_HOME}${NC}"
    echo -e "  闂佹澘绉堕悿鍡涙儎椤旇偐绉?     ${WHITE}${CONFIG_DIR}${NC}"
    echo -e "  闁哄啨鍎辩换鏃堟儎椤旇偐绉?     ${WHITE}${LOG_DIR}${NC}"
    echo ""
    
    if [ -n "$pid" ]; then
        echo -e "${CYAN}闁靛棙鍔樼换宥囨偘瀹€鈧慨鎼佸箑娴ｇ鍋?{NC}"
        echo -e "  闁绘鍩栭埀?         ${GREEN}閺夆晜鍔橀、鎴炵▔?{NC}"
        echo -e "  閺夆晜绋撻埢?ID:      ${WHITE}${pid}${NC}"
        
        # 闁兼儳鍢茶ぐ鍥触椤栨艾袟闁哄啫鐖煎Λ?        local start_time=$(ps -o lstart= -p "$pid" 2>/dev/null | xargs)
        [ -n "$start_time" ] && echo -e "  闁告凹鍨版慨鈺呭籍閸洘锛?     ${WHITE}${start_time}${NC}"
        
        # 闁兼儳鍢茶ぐ鍥ㄦ交閹邦垼鏀介柡鍐ㄧ埣閺?        local etime=$(ps -o etime= -p "$pid" 2>/dev/null | xargs)
        [ -n "$etime" ] && echo -e "  閺夆晜鍔橀、鎴﹀籍閸洘姣?     ${WHITE}${etime}${NC}"
        
        # 闁兼儳鍢茶ぐ鍥礃閸涱厾鎽犲ù锝堟硶閺?        local mem=$(ps -o rss= -p "$pid" 2>/dev/null | xargs)
        if [ -n "$mem" ]; then
            local mem_mb=$((mem / 1024))
            echo -e "  闁告劕鎳庨悺銊︽媴鐠恒劍鏆?     ${WHITE}${mem_mb} MB${NC}"
        fi
        
        # 闁兼儳鍢茶ぐ?CPU 濞达綀娉曢弫銈夋偝?        local cpu=$(ps -o %cpu= -p "$pid" 2>/dev/null | xargs)
        [ -n "$cpu" ] && echo -e "  CPU 濞达綀娉曢弫?     ${WHITE}${cpu}%${NC}"
        
        echo ""
        echo -e "${CYAN}闁靛棙鈧烤M 濞ｅ洠鍓濇导鍛村Υ?{NC}"
        check_java
        echo -e "  Java 閻犱警鍨扮欢?    ${WHITE}${JAVA_CMD}${NC}"
        echo -e "  Java 闁绘鐗婂﹢?    ${WHITE}$(get_java_version)${NC}"
        
        # 閻忓繑绻嗛惁顖炴嚔瀹勬澘绲?JVM 闁告瑥鍊归弳?        local jvm_opts=$(ps -o args= -p "$pid" 2>/dev/null | grep -oE '\-X[^ ]+' | head -5 | tr '\n' ' ')
        [ -n "$jvm_opts" ] && echo -e "  JVM 闁告瑥鍊归弳?     ${WHITE}${jvm_opts}${NC}"
    else
        echo -e "${CYAN}闁靛棙鍔樼换宥囨偘瀹€鈧慨鎼佸箑娴ｇ鍋?{NC}"
        echo -e "  闁绘鍩栭埀?         ${YELLOW}鐎瑰憡褰冩禒鐘差潰?{NC}"
    fi
    
    echo ""
    echo -e "${CYAN}闁靛棙鍔栧Λ鈺勭疀濡や焦鐎ù鐘虹堪閳?{NC}"
    if [ -f "${STARTUP_LOG}" ]; then
        local log_size=$(du -h "${STARTUP_LOG}" 2>/dev/null | cut -f1)
        echo -e "  闁告凹鍨版慨鈺呭籍閵夈儳绠?     ${WHITE}${STARTUP_LOG} (${log_size})${NC}"
    fi
    if [ -f "${APP_LOG}" ]; then
        local log_size=$(du -h "${APP_LOG}" 2>/dev/null | cut -f1)
        echo -e "  閹煎瓨姊婚弫銈夊籍閵夈儳绠?     ${WHITE}${APP_LOG} (${log_size})${NC}"
    fi
    echo ""
}

# ===========================================
# 闁告凹鍨版慨鈺傜┍閳╁啩绱栭柡鍕⒔閵?# ===========================================
show_startup_banner() {
    local java_version=$(get_java_version)
    local local_ip=$(get_local_ip)
    local server_port=$(get_server_port)
    local context_path=$(get_context_path)
    local swagger_enabled=$(check_swagger_enabled)
    local base_url="http://${local_ip}:${server_port}${context_path}"
    local local_url="http://localhost:${server_port}${context_path}"
    
    echo ""
    echo -e "${BLUE}闁崇儤鏌￠弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽${NC}"
    echo -e "${BLUE}闁?{NC}${GREEN}                     閹煎瓨姊婚弫銈夊触椤栨艾袟濞?..                            ${NC}${BLUE}闁?{NC}"
    echo -e "${BLUE}闁崇儤鍩冮弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽畷${NC}"
    echo ""
    echo -e "${CYAN}闁崇懓澶囬弨銏ゅ煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘?{NC}"
    echo -e "${CYAN}闁?{NC} ${WHITE}閹煎瓨姊婚弫銈夊触瀹ュ泦?{NC}    闁?${GREEN}${APP_NAME}${NC}"
    echo -e "${CYAN}闁?{NC} ${WHITE}閹煎瓨姊婚弫銈夋偋閸喐鎷?{NC}    闁?${GREEN}${APP_VERSION}${NC}"
    echo -e "${CYAN}闁?{NC} ${WHITE}閺夆晜鍔橀、鎴︽偝椤栨凹鏆?{NC}    闁?${YELLOW}${SPRING_PROFILE}${NC}"
    echo -e "${CYAN}闁?{NC} ${WHITE}闁哄牆绉存慨鐔煎捶閺夋寧绲?{NC}    闁?${GREEN}${base_url}${NC}"
    echo -e "${CYAN}闁?{NC} ${WHITE}闁哄牜鍓欏﹢鎾捶閺夋寧绲?{NC}    闁?${GREEN}${local_url}${NC}"
    [ "${swagger_enabled}" = "true" ] && echo -e "${CYAN}闁?{NC} ${WHITE}Swagger${NC}     闁?${GREEN}${local_url}/doc.html${NC}"
    echo -e "${CYAN}闁?{NC} ${WHITE}Java 闁绘鐗婂﹢?{NC}   闁?${MAGENTA}${java_version}${NC}"
    echo -e "${CYAN}闁?{NC} ${WHITE}Java 閻犱警鍨扮欢?{NC}   闁?${MAGENTA}${JAVA_CMD}${NC}"
    echo -e "${CYAN}闁?{NC} ${WHITE}JVM 闁告瑥鍊归弳?{NC}    闁?${MAGENTA}${JAVA_OPTS}${NC}"
    [ "${DEBUG_MODE}" = "true" ] && echo -e "${CYAN}闁?{NC} ${WHITE}閻犲鍟抽惁顖滅博椤栨艾缍?{NC}    闁?${RED}${DEBUG_PORT}${NC}"
    [ "${SKYWALKING_ENABLED}" = "true" ] && echo -e "${CYAN}闁?{NC} ${WHITE}Skywalking${NC}  闁?${GREEN}鐎瑰憡褰冮幆搴ㄦ偨?(${SW_AGENT_NAME})${NC}"
    echo -e "${CYAN}闁?{NC} ${WHITE}闂佹澘绉堕悿鍡涙儎椤旇偐绉?{NC}    闁?${CONFIG_DIR}"
    echo -e "${CYAN}闁?{NC} ${WHITE}闁哄啨鍎辩换鏃堟儎椤旇偐绉?{NC}    闁?${LOG_DIR}"
    echo -e "${CYAN}闁?{NC} ${WHITE}闁告凹鍨版慨鈺呭籍閸洘锛?{NC}    闁?$(date '+%Y-%m-%d %H:%M:%S')"
    echo -e "${CYAN}闁宠鏌￠弨銏ゅ煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘閳ь剟鍩為埀顒勫煘?{NC}"
    echo ""
}

# ===========================================
# 闁哄啨鍎辩换鏃傜不閿涘嫭鍊?# ===========================================

# 闁哄被鍎冲﹢鍛村嫉閳ь剚娼?N 闁哄鍓濆Λ鈺勭疀?show_logs() {
    local lines="${1:-100}"
    local log_file="${STARTUP_LOG}"
    
    # 濞村吋锚閸樻稒鎷呯捄銊︽殢閹煎瓨姊婚弫銈夊籍閵夈儳绠堕柨娑樿嫰椤┭囧几濠婂啰鎽犻柛锔哄妿濞堟垹鎷?    [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
    
    if [ ! -f "${log_file}" ]; then
        echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 闁哄啨鍎辩换鏃堝棘閸ワ附顐藉☉鎾崇Т閻°劑宕? ${log_file}${NC}"
        return 1
    fi
    
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    echo -e "${GREEN}  闁哄牃鍋撻弶?${lines} 闁哄鍓濆Λ鈺勭疀?- ${log_file}${NC}"
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    tail -n "${lines}" "${log_file}"
}

# 閻庡湱鍋炲鍌滄崉閻斿鍤嬮柡鍐﹀劚缁?tail_logs() {
    local log_file="${STARTUP_LOG}"
    
    # 濞村吋锚閸樻稒鎷呯捄銊︽殢閹煎瓨姊婚弫銈夊籍閵夈儳绠?    [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
    
    if [ ! -f "${log_file}" ]; then
        echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 闁哄啨鍎辩换鏃堝棘閸ワ附顐藉☉鎾崇Т閻°劑宕? ${log_file}${NC}"
        echo -e "${YELLOW}缂佹稑顦欢鐔煎籍閵夈儳绠堕柡鍌氭矗濞嗐垽鎮介悢绋跨亣...${NC}"
        # 缂佹稑顦欢鐔煎籍閵夈儳绠堕柡鍌氭矗濞嗐垽鎮介悢绋跨亣
        local count=0
        while [ ! -f "${log_file}" ] && [ $count -lt 30 ]; do
            sleep 1
            count=$((count + 1))
            [ -f "${APP_LOG}" ] && log_file="${APP_LOG}"
        done
        if [ ! -f "${log_file}" ]; then
            echo -e "${RED}[闂佹寧鐟ㄩ顦?缂佹稑顦欢鐔烘惥閸涱喗顦ч柨娑樻湰濡晞绠涘Δ浣圭€ù鐘虫构缁稒绋夊鍛憼闁?{NC}"
            return 1
        fi
    fi
    
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    echo -e "${GREEN}  閻庡湱鍋炲鍌炲籍閵夈儳绠堕悹铏瑰枙闁?- ${log_file}${NC}"
    echo -e "${YELLOW}  闁?Ctrl+C 闂侇偀鍋撻柛?{NC}"
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    tail -f "${log_file}"
}

# ===========================================
# Skywalking 闁衡偓椤栨稑鐦?# ===========================================
ensure_skywalking() {
    if [ ! -f "${SKYWALKING_AGENT_JAR}" ]; then
        echo -e "${YELLOW}[Skywalking] Agent 闁哄牜浜滈悾銊ф啑閸滃啰绀夌€殿喒鍋撳┑顔碱儓閸ゆ粓宕濋妸銈囩憮閺?..${NC}"
        bash "${SCRIPT_DIR}/script/linux/skywalking.sh" install
        if [ $? -ne 0 ]; then
            echo -e "${RED}[闂佹寧鐟ㄩ顦?Skywalking Agent 濞戞挸顑堝ù鍥ㄥ緞鏉堫偉袝${NC}"
            return 1
        fi
    fi
    return 0
}

run_skywalking() {
    bash "${SCRIPT_DIR}/script/linux/skywalking.sh" "$@"
}

# ===========================================
# Java 闁绘粠鍨伴。銊х不閿涘嫭鍊?# ===========================================
run_java_install() {
    local script_path="${SCRIPT_DIR}/script/linux/java-install.sh"
    if [ -f "$script_path" ]; then
        bash "$script_path" "$@"
    else
        echo -e "${RED}[闂佹寧鐟ㄩ顦?Java 閻庣懓顦抽ˉ濠囨嚇濮橆厽鎷卞☉鎾崇Т閻°劑宕? ${script_path}${NC}"
        return 1
    fi
}

# ===========================================
# 閹煎瓨姊婚弫銈夊触椤栨艾袟
# ===========================================
start_app() {
    local pid=$(get_pid)
    [ -n "$pid" ] && echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 ${APP_NAME} 鐎瑰憡褰冨﹢顏呮交閹邦垼鏀?(PID: ${pid})${NC}" && return 1
    
    mkdir -p "${LOG_DIR}"
    check_java
    build_loader_path
    
    # 闁哄瀚紓?Spring 闁告凹鍨版慨鈺呭矗閸屾稒娈?    local start_opts="--spring.config.location=classpath:/,file:${CONFIG_DIR}/"
    start_opts="${start_opts} --spring.profiles.active=${SPRING_PROFILE}"
    
    # 闁哄嫬澧介妵姘跺触椤栨艾袟濞ｅ洠鍓濇导?    show_startup_banner
    
    local java_opts="${JAVA_OPTS}"
    [ -n "${CUSTOM_JVM_OPTS}" ] && java_opts="${CUSTOM_JVM_OPTS}"
    [ "${DEBUG_MODE}" = "true" ] && java_opts="${java_opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT}"
    
    # Skywalking 闁衡偓椤栨稑鐦?    if [ "${SKYWALKING_ENABLED}" = "true" ]; then
        ensure_skywalking || return 1
        java_opts="${java_opts} -javaagent:${SKYWALKING_AGENT_JAR}"
        java_opts="${java_opts} -Dskywalking.agent.service_name=${SW_AGENT_NAME}"
        java_opts="${java_opts} -Dskywalking.collector.backend_service=${SW_AGENT_COLLECTOR_BACKEND_SERVICES}"
    fi
    
    # 闁告凹鍨版慨鈺傛償閺冨倹鏆?    nohup "$JAVA_CMD" ${java_opts} -Dloader.path="${LOADER_PATH}" -jar "${APP_HOME}/${JAR_NAME}" ${start_opts} > "${STARTUP_LOG}" 2>&1 &
    
    local new_pid=$!
    echo "$new_pid" > "${PID_FILE}"
    sleep 2
    
    if ps -p "$new_pid" > /dev/null 2>&1; then
        echo -e "${GREEN}闁崇儤鏌￠弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽${NC}"
        echo -e "${GREEN}闁?                   闁?闁告凹鍨版慨鈺呭箣閹邦剙顫?                               闁?{NC}"
        echo -e "${GREEN}闁崇儤鍩冮弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽畷${NC}"
        echo ""
        echo -e "  ${WHITE}閺夆晜绋撻埢?ID:${NC}    ${new_pid}"
        echo -e "  ${WHITE}闁哄啨鍎辩换鏃堝棘閸ワ附顐?${NC}   ${STARTUP_LOG}"
        echo ""
        
        # 闁哄秷顫夊畵渚€宕ｉ崒娑欐闁哄嫬澧介妵姘跺籍閵夈儳绠?        if [ "${TAIL_LOG}" = "true" ]; then
            echo -e "${CYAN}鐎殿喒鍋撳┑顔碱儏閻ゅ嫰寮幆鎵€€闂婎亶浜濆Λ鈺勭疀?..${NC}"
            tail_logs
        elif [ -n "${SHOW_LOGS}" ] && [ "${SHOW_LOGS}" -gt 0 ]; then
            echo -e "${CYAN}闁哄嫬澧介妵姘跺嫉閳ь剚娼?${SHOW_LOGS} 闁哄鍓濆Λ鈺勭疀?..${NC}"
            sleep 3  # 缂佹稑顦欢鐔哥▔閳ь剚绂嶅☉娆愶級闊洦顨夌欢顓㈠礄?            show_logs "${SHOW_LOGS}"
        fi
    else
        echo -e "${RED}闁崇儤鏌￠弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽${NC}"
        echo -e "${RED}闁?                   闁?闁告凹鍨版慨鈺傚緞鏉堫偉袝                                闁?{NC}"
        echo -e "${RED}闁崇儤鍩冮弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽畷${NC}"
        echo ""
        echo -e "${YELLOW}闂佹寧鐟ㄩ銈夊籍閵夈儳绠?${NC}"
        [ -f "${STARTUP_LOG}" ] && tail -30 "${STARTUP_LOG}"
    fi
}

# ===========================================
# 閹煎瓨姊婚弫銈夊磻濠婂嫷鍓?# ===========================================
stop_app() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 ${APP_NAME} 闁哄牜浜滃﹢顏呮交閹邦垼鏀?{NC}" && return 0
    
    echo -e "${BLUE}婵繐绲藉﹢顏堝磻濠婂嫷鍓?${APP_NAME} (PID: ${pid})...${NC}"
    kill "$pid" 2>/dev/null
    
    local count=0
    while [ $count -lt 30 ] && ps -p "$pid" > /dev/null 2>&1; do
        sleep 1
        count=$((count + 1))
        echo -ne "\r${YELLOW}缂佹稑顦欢鐔告交濞戞埃鏌ら梺顐熷亾闁?.. ${count}s${NC}"
    done
    echo ""
    
    ps -p "$pid" > /dev/null 2>&1 && kill -9 "$pid" 2>/dev/null
    rm -f "${PID_FILE}"
    echo -e "${GREEN}[闁瑰瓨鍔曟慨娌?${APP_NAME} 鐎瑰憡褰冩禒鐘差潰?{NC}"
}

# 闂佹彃绉撮幆?restart_app() { 
    stop_app
    sleep 2
    start_app
}

# ===========================================
# JVM 閻犲洤锕ラ弻?# ===========================================
show_jvm_info() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 ${APP_NAME} 闁哄牜浜滃﹢顏呮交閹邦垼鏀?{NC}" && return 1
    
    if [ -f "${SCRIPT_DIR}/script/linux/jvm-info.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/jvm-info.sh" "$pid" "$APP_NAME"
    else
        # 闁告劕鎳愰悿鍡欑不閳ь剟宕￠弴鐘崇暠 JVM 濞ｅ洠鍓濇导鍛村及閸撗佷粵
        echo -e "${BLUE}闁崇儤鏌￠弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽${NC}"
        echo -e "${BLUE}闁?{NC}${GREEN}                    JVM 閺夆晜鍔橀、鎴炵┍閳╁啩绱?                             ${NC}${BLUE}闁?{NC}"
        echo -e "${BLUE}闁崇儤鍩冮弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽畷${NC}"
        echo ""
        echo -e "${CYAN}閺夆晜绋撻埢鍏肩┍閳╁啩绱?${NC}"
        echo -e "  PID: ${pid}"
        
        if command -v jps &> /dev/null; then
            echo ""
            echo -e "${CYAN}JVM 閺夆晜绋撻埢?${NC}"
            jps -lvm | grep "$pid" || echo "  闁哄啰濮电涵鍫曟嚔瀹勬澘绲块悹鍥峰缁繑绌遍埄鍐х礀"
        fi
        
        if command -v jstat &> /dev/null; then
            echo ""
            echo -e "${CYAN}GC 缂備胶鍠曢?${NC}"
            jstat -gcutil "$pid" 2>/dev/null || echo "  闁哄啰濮电涵鍫曟嚔瀹勬澘绲?GC 濞ｅ洠鍓濇导?
        fi
    fi
}

show_tuning() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 ${APP_NAME} 闁哄牜浜滃﹢顏呮交閹邦垼鏀?{NC}" && return 1
    
    if [ -f "${SCRIPT_DIR}/script/linux/jvm-tuning.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/jvm-tuning.sh" "$pid" "$APP_NAME"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?JVM 閻犲鍟╃槐顓㈡嚇濮橆厽鎷卞☉鎾崇Т閻°劑宕?{NC}"
    fi
}

start_arthas() {
    local pid=$(get_pid)
    if [ -f "${SCRIPT_DIR}/script/linux/arthas-install.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/arthas-install.sh" "$pid"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?Arthas 闁煎瓨纰嶅﹢鐗堢▔瀹ュ懐鎽犻柛锔荤厜缁辨繄浜稿┑濠勬Ц闁烩晛鐡ㄧ敮鎾触椤栨艾袟...${NC}"
        if [ -f "${APP_HOME}/arthas/as.sh" ]; then
            bash "${APP_HOME}/arthas/as.sh" "$pid"
        else
            echo -e "${RED}[闂佹寧鐟ㄩ顦?Arthas 闁哄牜浜滈悾銊ф啑?{NC}"
        fi
    fi
}

run_analyze() {
    local pid=$(get_pid)
    [ -z "$pid" ] && echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 ${APP_NAME} 闁哄牜浜滃﹢顏呮交閹邦垼鏀?{NC}" && return 1
    local mode="${1:-cpu}"
    
    if [ -f "${SCRIPT_DIR}/script/linux/cpu-analysis.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/cpu-analysis.sh" "$pid" "$mode" "$APP_NAME"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?闁告帒妫欓悗浠嬫嚇濮橆厽鎷卞☉鎾崇Т閻°劑宕?{NC}"
    fi
}

# ===========================================
# Dump 閺夌儐鍓欓崑宥夊礉閻旇鍘?# ===========================================
run_dump() {
    local mode="${1:-all}"
    local output_dir="${2:-${LOG_DIR}/dump}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?${APP_NAME} 闁哄牜浜滃﹢顏呮交閹邦垼鏀?{NC}"
        exit 1
    fi
    
    mkdir -p "${output_dir}"
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    
    echo -e "${BLUE}闁崇儤鏌￠弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽${NC}"
    echo -e "${BLUE}闁?{NC}${GREEN}                    闁汇垻鍠愰崹?Dump 閺夌儐鍓欓崑?                           ${NC}${BLUE}闁?{NC}"
    echo -e "${BLUE}闁崇儤鍩冮弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽畷${NC}"
    echo ""
    echo -e "  PID:        ${pid}"
    echo -e "  婵☆垪鈧磭纭€:       ${mode}"
    echo -e "  閺夊牊鎸搁崵顓㈡儎椤旇偐绉?   ${output_dir}"
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
            echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄牜浜為悡锟犳儍?dump 婵☆垪鈧磭纭€: ${mode}${NC}"
            echo "闁衡偓椤栨稑鐦柣銊ュ鑶╃€? heap, thread, all"
            exit 1
            ;;
    esac
    
    echo -e "\n${GREEN}[閻庣懓鏈崹姝?Dump 闁哄倸娲ｅ▎銏狀啅闊厾绠介悗娑櫭崺? ${output_dir}${NC}"
}

dump_heap() {
    local pid="$1"
    local output_dir="$2"
    local timestamp="$3"
    local jmap_cmd="$4"
    local heap_file="${output_dir}/heapdump_${timestamp}.hprof"
    
    echo -e "${BLUE}[闁割偄妫滃ù鍡涘磼閳?闁汇垻鍠愰崹姘▔?..${NC}"
    
    if ! command -v "${jmap_cmd}" &> /dev/null; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄牜浜濇竟姗€宕?jmap 闁告稒鍨濋幎?{NC}"
        return 1
    fi
    
    "${jmap_cmd}" -dump:format=b,file="${heap_file}" "${pid}" 2>&1
    
    if [ $? -eq 0 ] && [ -f "${heap_file}" ]; then
        local size=$(du -h "${heap_file}" | cut -f1)
        echo -e "${GREEN}  闁?闁割偄妫滃ù鍡涘磼? ${heap_file} (${size})${NC}"
    else
        echo -e "${RED}  闁?闁割偄妫滃ù鍡涘磼閵娿儯浜奸悹?{NC}"
    fi
}

dump_thread() {
    local pid="$1"
    local output_dir="$2"
    local timestamp="$3"
    local jstack_cmd="$4"
    local thread_file="${output_dir}/threaddump_${timestamp}.txt"
    
    echo -e "${BLUE}[缂佹崘娉曢埢鍏兼姜椤掆偓閸嬪硶 闁汇垻鍠愰崹姘▔?..${NC}"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄牜浜濇竟姗€宕?jstack 闁告稒鍨濋幎?{NC}"
        return 1
    fi
    
    "${jstack_cmd}" -l "${pid}" > "${thread_file}" 2>&1
    
    if [ $? -eq 0 ] && [ -f "${thread_file}" ]; then
        local lines=$(wc -l < "${thread_file}")
        echo -e "${GREEN}  闁?缂佹崘娉曢埢鍏兼姜椤掆偓閸? ${thread_file} (${lines} 閻?${NC}"
        
        echo -e "\n${CYAN}[缂佹崘娉曢埢濂告偐閼哥鍋撴担铏瑰煚閻犱緡鎽?{NC}"
        echo -e "  RUNNABLE:      $(grep -c 'java.lang.Thread.State: RUNNABLE' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  WAITING:       $(grep -c 'java.lang.Thread.State: WAITING' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  TIMED_WAITING: $(grep -c 'java.lang.Thread.State: TIMED_WAITING' "${thread_file}" 2>/dev/null || echo 0)"
        echo -e "  BLOCKED:       $(grep -c 'java.lang.Thread.State: BLOCKED' "${thread_file}" 2>/dev/null || echo 0)"
        
        if grep -q "Found.*deadlock" "${thread_file}" 2>/dev/null; then
            echo -e "\n${RED}[閻犫偓閿曗偓閹差摚 婵☆偀鍋撴繛鏉戭儏閸╁苯顫㈡繝姘暁!${NC}"
        fi
    else
        echo -e "${RED}  闁?缂佹崘娉曢埢鍏兼姜椤掆偓閸嬪秵寰勬潏顐バ?{NC}"
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
    local index="${1:-}"
    local restore_path=""
    
    # If 'list', show all backups
    if [ "$index" = "list" ]; then
        list_backups
        return $?
    fi
    
    # If index is a number, restore to that backup
    if [[ "$index" =~ ^[0-9]+$ ]]; then
        restore_path=$(get_backup_by_index "$index")
        if [ -z "$restore_path" ]; then
            echo -e "${RED}[Error] Backup #${index} not found${NC}"
            echo -e "${CYAN}Use '$0 backup list' to see available backups${NC}"
            return 1
        fi
    else
        # Default: restore from latest
        if [ -f "${BACKUP_DIR}/latest" ]; then
            restore_path=$(cat "${BACKUP_DIR}/latest" 2>/dev/null)
        fi
        
        if [ -z "${restore_path}" ] || [ ! -d "${restore_path}" ]; then
            echo -e "${RED}[Error] No backup found to restore${NC}"
            echo -e "${CYAN}Use '$0 backup list' to see available backups${NC}"
            return 1
        fi
    fi
    
    restore_from_path "$restore_path"
}

# ===========================================
# 闁告娲ㄦ鍥炊閻愬娉?# ===========================================
run_upgrade() {
    local package_path="$1"
    local no_restart="$2"
    local opts=""
    
    [ -n "${package_path}" ] && opts="${opts} --package ${package_path}"
    [ "${no_restart}" = "true" ] && opts="${opts} --no-restart"
    
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" upgrade ${opts}
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?闁告娲ㄦ鍥嚇濮橆厽鎷卞☉鎾崇Т閻°劑宕?{NC}"
    fi
}

run_rollback() {
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" rollback
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?闁搞儳鍋炵划鎾嚇濮橆厽鎷卞☉鎾崇Т閻°劑宕?{NC}"
    fi
}

run_rollback_status() {
    if [ -f "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/upgrade-rollback.sh" status
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?闁搞儳鍋炵划鎾嚇濮橆厽鎷卞☉鎾崇Т閻°劑宕?{NC}"
    fi
}

# ===========================================
# Docker 闁烩晝顭堥崣褔宕欓懞銉︽
# ===========================================
docker_build() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" build --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?Docker 闁煎瓨纰嶅﹢鐗堢▔瀹ュ懐鎽犻柛?{NC}"
    fi
}

docker_deploy() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" deploy --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?Docker 闁煎瓨纰嶅﹢鐗堢▔瀹ュ懐鎽犻柛?{NC}"
    fi
}

docker_start_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" start --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?Docker 闁煎瓨纰嶅﹢鐗堢▔瀹ュ懐鎽犻柛?{NC}"
    fi
}

docker_stop_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" stop --name "$APP_NAME"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?Docker 闁煎瓨纰嶅﹢鐗堢▔瀹ュ懐鎽犻柛?{NC}"
    fi
}

docker_restart_container() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" restart --name "$APP_NAME" --version "$APP_VERSION" --host-port "$DOCKER_HOST_PORT" --port "$DOCKER_PORT" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?Docker 闁煎瓨纰嶅﹢鐗堢▔瀹ュ懐鎽犻柛?{NC}"
    fi
}

docker_status() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" status --name "$APP_NAME"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?Docker 闁煎瓨纰嶅﹢鐗堢▔瀹ュ懐鎽犻柛?{NC}"
    fi
}

docker_logs() {
    local lines="${1:-100}"
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" logs --name "$APP_NAME" --lines "$lines"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?Docker 闁煎瓨纰嶅﹢鐗堢▔瀹ュ懐鎽犻柛?{NC}"
    fi
}

docker_clean() {
    if [ -f "${SCRIPT_DIR}/script/linux/docker.sh" ]; then
        bash "${SCRIPT_DIR}/script/linux/docker.sh" clean --name "$APP_NAME" --tag "$DOCKER_IMAGE_TAG"
    else
        echo -e "${YELLOW}[闁圭粯鍔楅妵姝?Docker 闁煎瓨纰嶅﹢鐗堢▔瀹ュ懐鎽犻柛?{NC}"
    fi
}

# ===========================================
# GC 閻熸瑱绠戣ぐ?# ===========================================
run_gc() {
    local gc_type="${1:-full}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?${APP_NAME} 闁哄牜浜滃﹢顏呮交閹邦垼鏀?{NC}"
        return 1
    fi
    
    check_java
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    if ! command -v "${jcmd_cmd}" &> /dev/null; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄牜浜濇竟姗€宕?jcmd 闁告稒鍨濋幎?{NC}"
        return 1
    fi
    
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    echo -e "${GREEN}                    閻熸瑱绠戣ぐ?GC${NC}"
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    echo ""
    echo -e "  PID:      ${pid}"
    echo -e "  GC 缂侇偉顕ч悗? ${gc_type}"
    echo ""
    
    # GC 闁告挸绉舵慨鎼佸箑?    echo -e "${CYAN}[GC 闁告挸绉舵慨鎼佸箑娑?{NC}"
    local jstat_cmd="jstat"
    [ -n "$JAVA_HOME" ] && jstat_cmd="$JAVA_HOME/bin/jstat"
    if command -v "${jstat_cmd}" &> /dev/null; then
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    echo ""
    
    case "${gc_type}" in
        young|minor)
            echo -e "${YELLOW}閻熸瑱绠戣ぐ?Young GC...${NC}"
            "${jcmd_cmd}" "$pid" GC.run 2>/dev/null
            ;;
        full|major)
            echo -e "${YELLOW}閻熸瑱绠戣ぐ?Full GC...${NC}"
            "${jcmd_cmd}" "$pid" GC.run 2>/dev/null
            ;;
        *)
            echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄牜浜為悡锟犳儍?GC 缂侇偉顕ч悗? ${gc_type}${NC}"
            echo "闁衡偓椤栨稑鐦柣銊ュ鐞氼偊宕? young, full"
            return 1
            ;;
    esac
    
    sleep 2
    
    # GC 闁告艾娴锋慨鎼佸箑?    echo ""
    echo -e "${CYAN}[GC 闁告艾娴锋慨鎼佸箑娑?{NC}"
    if command -v "${jstat_cmd}" &> /dev/null; then
        "${jstat_cmd}" -gcutil "$pid" 2>/dev/null | head -2
    fi
    
    echo ""
    echo -e "${GREEN}[閻庣懓鏈崹姝?GC 鐎规瓕灏闁?{NC}"
}

# ===========================================
# 濡?CPU 缂佹崘娉曢埢濂稿礆閸℃鈧?# ===========================================
run_top_cpu() {
    local top_n="${1:-10}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?${APP_NAME} 闁哄牜浜滃﹢顏呮交閹邦垼鏀?{NC}"
        return 1
    fi
    
    check_java
    local jstack_cmd="jstack"
    [ -n "$JAVA_HOME" ] && jstack_cmd="$JAVA_HOME/bin/jstack"
    
    if ! command -v "${jstack_cmd}" &> /dev/null; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄牜浜濇竟姗€宕?jstack 闁告稒鍨濋幎?{NC}"
        return 1
    fi
    
    echo -e "${BLUE}闁崇儤鏌￠弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽${NC}"
    echo -e "${BLUE}闁?{NC}${GREEN}                濡?CPU 缂佹崘娉曢埢濂稿礆閸℃鈧?(Top ${top_n})                  ${NC}${BLUE}闁?{NC}"
    echo -e "${BLUE}闁崇儤鍩冮弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽畷${NC}"
    echo ""
    echo -e "  閺夆晜绋撻埢?PID: ${pid}"
    echo -e "  閹煎瓨姊婚弫銈夊触瀹ュ泦? ${APP_NAME}"
    echo ""
    
    # 闁兼儳鍢茶ぐ鍥殗?CPU 缂佹崘娉曢埢?    echo -e "${CYAN}[婵縿鍎甸?1] 闁兼儳鍢茶ぐ鍥殗?CPU 缂佹崘娉曢埢?..${NC}"
    local tmp_threads=$(mktemp)
    local tmp_jstack=$(mktemp)
    
    # 闁兼儳鍢茶ぐ鍥╃棯鐠恒劉鏌?CPU 濞达綀娉曢弫銈夋偝?    top -b -n 1 -H -p "$pid" 2>/dev/null | grep -E "^\s*[0-9]+" | head -n "$top_n" > "$tmp_threads"
    
    if [ ! -s "$tmp_threads" ]; then
        echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 闁哄啰濮电涵鍫曟焻濮樺磭绠?top 闁兼儳鍢茶ぐ鍥╃棯鐠恒劉鏌ゅǎ鍥ｅ墲娴煎懘鏁嶇仦鐣屾閻?ps...${NC}"
        ps -eLo pid,lwp,%cpu,comm --sort=-%cpu | grep "$pid" | head -n "$top_n" > "$tmp_threads"
    fi
    
    # 闁兼儳鍢茶ぐ鍥╃棯鐠恒劉鏌ら柛顐㈡閻?    echo -e "${CYAN}[婵縿鍎甸?2] 闁兼儳鍢茶ぐ鍥╃棯鐠恒劉鏌ら柛顐㈡閻?..${NC}"
    "${jstack_cmd}" -l "$pid" > "$tmp_jstack" 2>/dev/null
    
    if [ ! -s "$tmp_jstack" ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄啰濮电涵鍫曟嚔瀹勬澘绲跨紒鎹愭硶閳诲ジ宕奸崱妯煎灲${NC}"
        rm -f "$tmp_threads" "$tmp_jstack"
        return 1
    fi
    
    # 闁告帒妫欓悗浠嬬嵁閼稿灚鈻旂紒鈧?    echo ""
    echo -e "${CYAN}[婵縿鍎甸?3] 闁告帒妫欓悗鑺ヮ殗?CPU 缂佹崘娉曢埢濂稿醇閸℃鍨?..${NC}"
    echo -e "${BLUE}闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾${NC}"
    
    local count=0
    while read -r line; do
        # 閻熸瑱绲鹃悗鐣岀棯鐠恒劉鏌?ID 闁?CPU
        local lwp=$(echo "$line" | awk '{print $1}')
        local cpu=$(echo "$line" | awk '{print $9}')
        
        # 濠碘€冲€归悘澶愬及?ps 閺夊牊鎸搁崵顓㈠冀閻撳海纭€
        if [[ "$line" =~ ^[[:space:]]*[0-9]+[[:space:]]+[0-9]+ ]]; then
            lwp=$(echo "$line" | awk '{print $2}')
            cpu=$(echo "$line" | awk '{print $3}')
        fi
        
        [ -z "$lwp" ] && continue
        
        # 閺夌儐鍓氬畷鍙夌▔?16 閺夆晜绋戦崺?        local nid=$(printf "%x" "$lwp" 2>/dev/null)
        [ -z "$nid" ] && continue
        
        count=$((count + 1))
        echo ""
        echo -e "${YELLOW}#${count} 缂佹崘娉曢埢?ID: ${lwp} (0x${nid})  CPU: ${cpu}%${NC}"
        
        # 闁?jstack 濞戞搩鍘介悡锟犲箥閹屽殸閹煎瓨姊诲▓鎴犵棯鐠恒劉鏌ら柛顐㈡閻?        local found=false
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
                # 闁哄嫬澧介妵姘跺醇閸℃鍨?(闁哄牃鍋撳?15 閻?
                if [ $stack_lines -lt 15 ]; then
                    # 濡ゅ倹眉鐎垫帡寮伴崜褋浠涘☉鎾磋壘婵喐绂掗敐鍥╁灣
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
            echo -e "${YELLOW}  闁哄啰濮电涵鍫曞箥閹冪厒缂佹崘娉曢埢濂稿醇閸℃鍨?(nid=0x${nid})${NC}"
        fi
        
        echo -e "${BLUE}闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾${NC}"
        
        [ $count -ge $top_n ] && break
    done < "$tmp_threads"
    
    rm -f "$tmp_threads" "$tmp_jstack"
    
    echo ""
    echo -e "${GREEN}[閻庣懓鏈崹姝?闁告帒妫欓悗鐣岀磼閹惧瓨灏?{NC}"
    echo -e "${CYAN}闁圭粯鍔楅妵? 缂備浇鍎绘竟濠冾殗濡懓鐦ㄩ柣銊ュ濡插憡绋夊顒€顫ゅù鐙呯悼閻栨粓鏁嶅畝鍕ㄥ亾濮橆剛鍩楅梻鍥ｅ亾閻熸洑绶氶崳鎼佹倷閻熸澘褰犳繛?{NC}"
}

# ===========================================
# 濡ゅ倹锚閸炲鈧稒锚椤曨喚鎸掗垾鍐茬€婚柡?# ===========================================
run_top_mem() {
    local top_n="${1:-10}"
    local pid=$(get_pid)
    
    if [ -z "$pid" ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?${APP_NAME} 闁哄牜浜滃﹢顏呮交閹邦垼鏀?{NC}"
        return 1
    fi
    
    check_java
    local jmap_cmd="jmap"
    local jcmd_cmd="jcmd"
    [ -n "$JAVA_HOME" ] && jmap_cmd="$JAVA_HOME/bin/jmap" && jcmd_cmd="$JAVA_HOME/bin/jcmd"
    
    echo -e "${BLUE}闁崇儤鏌￠弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽${NC}"
    echo -e "${BLUE}闁?{NC}${GREEN}               濡ゅ倹锚閸炲鈧稒锚椤曨喚鎸掗垾鍐茬€婚柡?(Top ${top_n})                   ${NC}${BLUE}闁?{NC}"
    echo -e "${BLUE}闁崇儤鍩冮弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽畷${NC}"
    echo ""
    echo -e "  閺夆晜绋撻埢?PID: ${pid}"
    echo -e "  閹煎瓨姊婚弫銈夊触瀹ュ泦? ${APP_NAME}"
    echo ""
    
    # 閻忓繑绻嗛惁顖涙媴鐠恒劍鏆?jcmd (闁哄洦娼欓悾銊╁礂?
    local histo_output=""
    if command -v "${jcmd_cmd}" &> /dev/null; then
        echo -e "${CYAN}[婵縿鍎甸?1] 濞达綀娉曢弫?jcmd 闁兼儳鍢茶ぐ鍥┾偓鐢殿攰閽栧嫰鎯勭€涙ɑ鐓欓柛?..${NC}"
        histo_output=$("${jcmd_cmd}" "$pid" GC.class_histogram 2>/dev/null)
    fi
    
    # 濠碘€冲€归悘?jcmd 濠㈡儼绮剧憴锕傛晬鐏炵晫姣﹂悹?jmap
    if [ -z "$histo_output" ] && command -v "${jmap_cmd}" &> /dev/null; then
        echo -e "${CYAN}[婵縿鍎甸?1] 濞达綀娉曢弫?jmap 闁兼儳鍢茶ぐ鍥┾偓鐢殿攰閽栧嫰鎯勭€涙ɑ鐓欓柛?..${NC}"
        histo_output=$("${jmap_cmd}" -histo:live "$pid" 2>/dev/null)
    fi
    
    if [ -z "$histo_output" ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄啰濮电涵鍫曟嚔瀹勬澘绲块悗鐢殿攰閽栧嫰鎯勭€涙ɑ鐓欓柛?{NC}"
        return 1
    fi
    
    echo ""
    echo -e "${CYAN}[婵縿鍎甸?2] 闁告帒妫欓悗鑺ヮ殗濡搫鏁堕悗娑櫭顔炬寬?..${NC}"
    echo ""
    
    # 闁哄嫬澧介妵姘跺冀閸ヮ剦鏆?    echo -e "${WHITE}  闁圭儤甯掗幃?  閻庡湱鍋樼欢銉╁极?         闁告濮烽弫銈団偓娑欘殙婵?           缂侇偉顕ч幃?{NC}"
    echo -e "${BLUE}闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾${NC}"
    
    # 閻熸瑱绲鹃悗浠嬬嵁閼稿灚鈻旂紒鈧?Top N
    local count=0
    echo "$histo_output" | grep -E "^\s*[0-9]+:" | head -n "$top_n" | while read -r line; do
        count=$((count + 1))
        local num=$(echo "$line" | awk '{print $1}' | tr -d ':')
        local instances=$(echo "$line" | awk '{print $2}')
        local bytes=$(echo "$line" | awk '{print $3}')
        local class_name=$(echo "$line" | awk '{print $4}')
        
        # 閺夌儐鍓氬畷鑼偓娑欘殙婵☆厽绋夐崫鍕閻犲洨绮悧绋款嚕?        local size_human=""
        if [ "$bytes" -ge 1073741824 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f GB\", $bytes/1073741824}")
        elif [ "$bytes" -ge 1048576 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f MB\", $bytes/1048576}")
        elif [ "$bytes" -ge 1024 ]; then
            size_human=$(awk "BEGIN {printf \"%.2f KB\", $bytes/1024}")
        else
            size_human="${bytes} B"
        fi
        
        # 濡ゅ倹眉鐎垫帗绋夊顒€顫ょ紒?        if [[ "$class_name" =~ ^(com\.|cn\.|org\..*\.model|org\..*\.entity|org\..*\.dto) ]]; then
            printf "${GREEN}  %4s   %12s   %12s   %s${NC}\n" "$num" "$instances" "$size_human" "$class_name"
        else
            printf "  %4s   %12s   %12s   %s\n" "$num" "$instances" "$size_human" "$class_name"
        fi
    done
    
    echo -e "${BLUE}闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾闁冲厜鍋撻柍鍏夊亾${NC}"
    
    # 闁哄嫬澧介妵姘跺礃閸涱厾鎽犳慨鎺戝€介～?    echo ""
    echo -e "${CYAN}[闁告劕鎳庨悺銊ヮ潡閸屾繍娼擼${NC}"
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
        echo -e "  闁诡剝顕ч悿鍕瑹鐎ｎ偅娈? ${WHITE}${total_instances}${NC}"
        echo -e "  闁诡剝顕у畷浼存偨?   ${WHITE}${total_human}${NC}"
    fi
    
    echo ""
    echo -e "${GREEN}[閻庣懓鏈崹姝?闁告帒妫欓悗鐣岀磼閹惧瓨灏?{NC}"
    echo -e "${CYAN}闁圭粯鍔楅妵? 缂備浇鍎绘竟濠冾殗濡懓鐦ㄩ柣銊ュ濡插憡绋夊顒€顫ょ紒顐＄串缁辨繈鏌呭顒傚煑闂傚洠鍋撻悷鏇氱窔閸ｆ悂鎮欓悷鏉垮綘婵?{NC}"
    echo -e "${CYAN}      濠碘€冲€垮〒鍫曞即绾惧鍤婄紓浣告閸ㄥ酣寮搁幇鍓佺閻犲洩娓规繛鍥偨? $0 dump heap${NC}"
}

# ===========================================
# 闁哄牆绉存慨鐔虹不閿涘嫭鍊?(systemd)
# ===========================================
SERVICE_NAME="${APP_NAME}.service"
SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}"

install_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闂傚洠鍋撻悷?root 闁哄鍟村娲箥瑜戦、鎴濐潰閵堝懏鍤掑ù?{NC}"
        echo "閻犲洩娓规繛鍥偨? sudo $0 install-service"
        return 1
    fi
    
    if [ -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 闁哄牆绉存慨鐔奉啅閹绘帞鎽犻柛? ${SERVICE_NAME}${NC}"
        read -p "闁哄嫷鍨伴幆浣烘啺閸℃瑦纾? (y/N): " confirm
        [ "$confirm" != "y" ] && [ "$confirm" != "Y" ] && return 0
    fi
    
    check_java
    local java_path=$(which "$JAVA_CMD" 2>/dev/null || echo "$JAVA_CMD")
    local run_user=$(stat -c '%U' "${APP_HOME}" 2>/dev/null || echo "root")
    
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    echo -e "${GREEN}              閻庣懓顦抽ˉ?Systemd 闁哄牆绉存慨?{NC}"
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    echo ""
    echo -e "  闁哄牆绉存慨鐔煎触瀹ュ泦? ${SERVICE_NAME}"
    echo -e "  閺夆晜鍔橀、鎴︽偨閵婏箑鐓? ${run_user}"
    echo -e "  Java 閻犱警鍨扮欢? ${java_path}"
    echo -e "  閹煎瓨姊婚弫銈夋儎椤旇偐绉? ${APP_HOME}"
    echo ""
    
    # 闁汇垻鍠愰崹?systemd 闁哄牆绉存慨鐔煎棘閸ワ附顐?    cat > "${SERVICE_FILE}" << EOF
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
    
    # 闂佹彃绉甸弻濠囧礉閻樼儤绁?systemd
    systemctl daemon-reload
    
    echo -e "${GREEN}[闁瑰瓨鍔曟慨娌?闁哄牆绉存慨鐔奉啅閹绘帞鏆旈悷? ${SERVICE_NAME}${NC}"
    echo ""
    echo -e "${CYAN}闁告瑯鍨抽弫銈夊川閹存帗濮?${NC}"
    echo "  systemctl start ${SERVICE_NAME}     # 闁告凹鍨版慨鈺呭嫉瀹ュ懎顫?
    echo "  systemctl stop ${SERVICE_NAME}      # 闁稿绮嶉娑㈠嫉瀹ュ懎顫?
    echo "  systemctl restart ${SERVICE_NAME}   # 闂佹彃绉撮幆搴ㄥ嫉瀹ュ懎顫?
    echo "  systemctl status ${SERVICE_NAME}    # 闁哄被鍎冲﹢鍛存偐閼哥鍋?
    echo "  systemctl enable ${SERVICE_NAME}    # 鐎殿喒鍋撻柡鍫ョ細閸ゆ粓宕?
    echo "  journalctl -u ${SERVICE_NAME} -f    # 闁哄被鍎冲﹢鍛村籍閵夈儳绠?
}

uninstall_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闂傚洠鍋撻悷?root 闁哄鍟村娲箥瑜戦、鎴濐潰閵堝懏鍤掑ù?{NC}"
        echo "閻犲洩娓规繛鍥偨? sudo $0 uninstall-service"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 闁哄牆绉存慨鐔哥▔瀹ュ懐鎽犻柛? ${SERVICE_NAME}${NC}"
        return 0
    fi
    
    echo -e "${BLUE}闁告鐡曞ù鍥嫉瀹ュ懎顫? ${SERVICE_NAME}...${NC}"
    
    # 闁稿绮嶉娑㈠嫉瀹ュ懎顫?    systemctl stop "${SERVICE_NAME}" 2>/dev/null
    systemctl disable "${SERVICE_NAME}" 2>/dev/null
    
    # 闁告帞濞€濞呭酣寮靛鍛潳闁哄倸娲ｅ▎?    rm -f "${SERVICE_FILE}"
    systemctl daemon-reload
    
    echo -e "${GREEN}[闁瑰瓨鍔曟慨娌?闁哄牆绉存慨鐔奉啅閹绘帒绁婚弶?{NC}"
}

enable_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闂傚洠鍋撻悷?root 闁哄鍟村娲箥瑜戦、鎴濐潰閵堝懏鍤掑ù?{NC}"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄牆绉存慨鐔煎嫉椤忓嫮鏆旈悷浣告嫅缁辨繄鎷犲畡鏉垮弗闁圭瑳鍡╂斀: $0 install-service${NC}"
        return 1
    fi
    
    systemctl enable "${SERVICE_NAME}"
    echo -e "${GREEN}[闁瑰瓨鍔曟慨娌?鐎瑰憡褰冮幆搴ㄦ偨閵娿儳纾婚柡鍫ョ細閸ゆ粓宕?{NC}"
}

disable_service() {
    if [ "$(id -u)" -ne 0 ]; then
        echo -e "${RED}[闂佹寧鐟ㄩ顦?闂傚洠鍋撻悷?root 闁哄鍟村娲箥瑜戦、鎴濐潰閵堝懏鍤掑ù?{NC}"
        return 1
    fi
    
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 闁哄牆绉存慨鐔煎嫉椤忓嫮鏆旈悷?{NC}"
        return 0
    fi
    
    systemctl disable "${SERVICE_NAME}"
    echo -e "${GREEN}[闁瑰瓨鍔曟慨娌?鐎规瓕灏欓々锕傛偨閵娿儳纾婚柡鍫ョ細閸ゆ粓宕?{NC}"
}

show_service_status() {
    if [ ! -f "${SERVICE_FILE}" ]; then
        echo -e "${YELLOW}[閻犫偓閿曗偓閹差摚 闁哄牆绉存慨鐔煎嫉椤忓嫮鏆旈悷? ${SERVICE_NAME}${NC}"
        echo "濞达綀娉曢弫?'$0 install-service' 閻庣懓顦抽ˉ濠囧嫉瀹ュ懎顫?
        return 0
    fi
    
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    echo -e "${GREEN}              Systemd 闁哄牆绉存慨鐔兼偐閼哥鍋?{NC}"
    echo -e "${BLUE}闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳闁崇儤鍔忛弲鏌ュ煛閹般劍娅滈柍鐑樺姀閺呮煡鍩￠幇銊︽珳${NC}"
    echo ""
    systemctl status "${SERVICE_NAME}" --no-pager
}

# ===========================================
# 闁告瑥鍊归弳鐔烘喆閿濆棛鈧?# ===========================================
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
RESTORE_CMD_ARGS=()

TOP_N="10"

while [ $# -gt 0 ]; do
    case "$1" in
        start|stop|restart|status|info|jvm|tuning|analyze|dump|arthas|upgrade|rollback|rollback-status|debug)
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
        restore)
            COMMAND="restore"
            shift
            RESTORE_CMD_ARGS=("$@")
            break
            ;;
        gc)
            COMMAND="gc"
            shift
            # 婵☆偀鍋撻柡灞诲劙缁楀懏绋夐埀顒佺▔椤忓嫬妫橀柡浣哄濡叉悂宕ラ敂鑺バ?GC 缂侇偉顕ч悗?            if [[ "$1" =~ ^(young|full|minor|major)$ ]]; then
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
            # 婵☆偀鍋撻柡灞诲劙缁楀懏绋夐埀顒佺▔椤忓嫬妫橀柡浣哄濡叉悂宕ラ敂鑺バ﹂柡浣规緲閻?            if [[ "$1" =~ ^[0-9]+$ ]]; then
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
            # 濞ｅ洦绻傞悺?java 闁告稒鍨濋幎銈夋儍閸曨剙顣查柡鍫濐槸閹绱掗鐐叉闁?            JAVA_CMD_ARGS=("$@")
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
            # 婵☆偀鍋撻柡灞诲劙缁楀懏绋夐埀顒佺▔椤忓嫬妫橀柡浣哄濡叉悂宕ラ敂鑺バ﹂悹渚灠缁?(濞戞挸绉虫禍?- 鐎殿喒鍋撳?
            if [ -n "$1" ] && [[ ! "$1" =~ ^- ]]; then
                CUSTOM_JAVA="$1"
                # 濞ｅ洦绻傞悺?Java 閻犱警鍨扮欢?                if [ -x "$CUSTOM_JAVA" ]; then
                    save_java_path "$CUSTOM_JAVA"
                fi
                shift
            else
                # 闁哄啰濮村顒勫极鐢喚绀夐柡鍕⒔閵囨俺銇愰幘鍐差枀 Java 濞ｅ洠鍓濇导?                show_java_info
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
            # 婵☆偀鍋撻柡灞诲劜濡叉悂宕ラ敂鑺バ﹂柡浣规緲閻?(闁活潿鍔嬬花?logs/top-cpu/top-mem 闁告稒鍨濋幎?
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                case "$COMMAND" in
                    logs) SHOW_LOGS="$1" ;;
                    top-cpu|top-mem) TOP_N="$1" ;;
                    *) echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄牜浜為悡锟犲矗閸屾稒娈? $1${NC}"; show_help; exit 1 ;;
                esac
                shift
            else
                echo -e "${RED}[闂佹寧鐟ㄩ顦?闁哄牜浜為悡锟犲矗閸屾稒娈? $1${NC}"
                show_help
                exit 1
            fi
            ;;
    esac
done

[ "$COMMAND" = "debug" ] && DEBUG_MODE="true" && COMMAND="start"

# ===========================================
# 闁告稒鍨濋幎銈夊箥瑜戦、?# ===========================================
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
    restore) run_restore "${RESTORE_CMD_ARGS[@]}" ;;
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
