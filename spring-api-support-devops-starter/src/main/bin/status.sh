#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  status.sh [-f] [-i 秒] [-j] [-m] [-d]

参数:
  -f          实时监控模式，无闪屏更新（按 Ctrl+C 退出）
  -i 秒       监控刷新间隔（秒），默认 2 秒，仅在 -f 模式下生效
  -j          显示 JVM 信息（堆内存、GC 等，需要 jstat 支持）
  -m          显示内存详细信息
  -d          显示线程转储信息（需要 jstack 支持）
  -h, --help  显示帮助信息

说明:
  - 默认显示基本状态信息
  - 可以组合使用多个参数来显示详细信息
EOF
}

FOLLOW_MODE="false"
INTERVAL="2"
SHOW_JVM="false"
SHOW_MEMORY="false"
SHOW_THREAD_DUMP="false"

while [ $# -gt 0 ]; do
  case "$1" in
    -f)
      FOLLOW_MODE="true"
      shift
      ;;
    -i)
      INTERVAL="${2:-2}"
      shift 2
      ;;
    -j)
      SHOW_JVM="true"
      shift
      ;;
    -m)
      SHOW_MEMORY="true"
      shift
      ;;
    -d)
      SHOW_THREAD_DUMP="true"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      warn "未知参数: $1"
      usage
      exit 1
      ;;
  esac
done

load_env
PID_FILE="$(get_pid_file)"

format_bytes() {
  var_bytes="${1:-0}"
  # 确保是整数（去除小数点）
  var_bytes="${var_bytes%%.*}"
  if [ -z "${var_bytes}" ] || ! echo "${var_bytes}" | grep -qE '^[0-9]+$'; then
    var_bytes="0"
  fi
  if [ "${var_bytes}" -ge 1073741824 ] 2>/dev/null; then
    var_gb=$((var_bytes / 1073741824))
    var_mb=$(((var_bytes % 1073741824) / 1048576))
    echo "${var_gb}.${var_mb}GB"
  elif [ "${var_bytes}" -ge 1048576 ]; then
    var_mb=$((var_bytes / 1048576))
    var_kb=$(((var_bytes % 1048576) / 1024))
    echo "${var_mb}.${var_kb}MB"
  elif [ "${var_bytes}" -ge 1024 ]; then
    var_kb=$((var_bytes / 1024))
    echo "${var_kb}KB"
  else
    echo "${var_bytes}B"
  fi
}

show_status() {
  if [ ! -f "${PID_FILE}" ]; then
    info "未发现 PID 文件，应用未运行"
    return 1
  fi

  PID="$(cat "${PID_FILE}" || true)"
  if ! is_running "${PID}"; then
    warn "PID 文件存在但进程不存在，PID=${PID}，将清理 PID 文件"
    rm -f "${PID_FILE}"
    return 1
  fi

  APP_NAME="$(get_app_name)"
  APP_VERSION="$(get_app_version)"
  
  echo "=========================================="
  echo "应用状态: ${APP_NAME} ${APP_VERSION}"
  echo "=========================================="
  echo "PID: ${PID}"
  
  if command -v ps >/dev/null 2>&1; then
    # 获取进程详细信息（兼容不同系统的 ps 命令）
    PPID=""
    USER=""
    CPU="0.0"
    MEM="0.0"
    RSS="0"
    VSZ="0"
    ETIME=""
    CMD=""
    
    # 尝试 Linux 标准格式
    if ps -p "${PID}" >/dev/null 2>&1; then
      PPID="$(ps -p "${PID}" -o ppid= 2>/dev/null | tr -d ' ' || true)"
      USER="$(ps -p "${PID}" -o user= 2>/dev/null | tr -d ' ' || true)"
      CPU="$(ps -p "${PID}" -o %cpu= 2>/dev/null | tr -d ' ' || echo "0.0")"
      MEM="$(ps -p "${PID}" -o %mem= 2>/dev/null | tr -d ' ' || echo "0.0")"
      RSS="$(ps -p "${PID}" -o rss= 2>/dev/null | tr -d ' ' || echo "0")"
      VSZ="$(ps -p "${PID}" -o vsz= 2>/dev/null | tr -d ' ' || echo "0")"
      ETIME="$(ps -p "${PID}" -o etime= 2>/dev/null | tr -d ' ' || true)"
      CMD="$(ps -p "${PID}" -o cmd= 2>/dev/null | sed 's/^[[:space:]]*//' || true)"
    fi
    
    # 如果 Linux 格式失败，尝试 BSD/macOS 格式
    if [ -z "${PPID}" ] && ps "${PID}" >/dev/null 2>&1; then
      PPID="$(ps -p "${PID}" -o ppid= 2>/dev/null | tr -d ' ' || true)"
      USER="$(ps -p "${PID}" -o user= 2>/dev/null | tr -d ' ' || true)"
      CPU="$(ps -p "${PID}" -o %cpu= 2>/dev/null | tr -d ' ' || echo "0.0")"
      MEM="$(ps -p "${PID}" -o %mem= 2>/dev/null | tr -d ' ' || echo "0.0")"
      RSS="$(ps -p "${PID}" -o rss= 2>/dev/null | tr -d ' ' || echo "0")"
      VSZ="$(ps -p "${PID}" -o vsz= 2>/dev/null | tr -d ' ' || echo "0")"
      ETIME="$(ps -p "${PID}" -o etime= 2>/dev/null | tr -d ' ' || true)"
      CMD="$(ps -p "${PID}" -o command= 2>/dev/null | sed 's/^[[:space:]]*//' || true)"
    fi
    
    if [ -n "${PPID}" ]; then
      echo "PPID: ${PPID}"
    fi
    
    if [ -n "${USER}" ]; then
      echo "用户: ${USER}"
    fi
    
    if [ -n "${CPU}" ] && [ "${CPU}" != "-" ]; then
      echo "CPU: ${CPU}%"
    fi
    
    if [ -n "${MEM}" ] && [ "${MEM}" != "-" ]; then
      echo "内存: ${MEM}%"
    fi
    
    if [ -n "${RSS}" ] && [ "${RSS}" != "-" ] && [ "${RSS}" != "0" ]; then
      RSS_BYTES=$((RSS * 1024))
      RSS_FORMATTED="$(format_bytes "${RSS_BYTES}")"
      echo "RSS: ${RSS_FORMATTED} (${RSS}KB)"
    fi
    
    if [ -n "${VSZ}" ] && [ "${VSZ}" != "-" ] && [ "${VSZ}" != "0" ]; then
      VSZ_BYTES=$((VSZ * 1024))
      VSZ_FORMATTED="$(format_bytes "${VSZ_BYTES}")"
      echo "VSZ: ${VSZ_FORMATTED} (${VSZ}KB)"
    fi
    
    if [ -n "${ETIME}" ] && [ "${ETIME}" != "-" ]; then
      echo "运行时间: ${ETIME}"
    fi
    
    if [ -n "${CMD}" ]; then
      echo "命令: ${CMD}"
    fi
  fi
  
  CONSOLE_LOG="$(get_console_log)"
  echo "日志文件: ${CONSOLE_LOG}"
  echo "PID 文件: ${PID_FILE}"
  
  # 显示 JVM 信息
  if [ "${SHOW_JVM}" = "true" ]; then
    JAVA_BIN=""
    if [ -n "${JAVA_HOME:-}" ]; then
      JAVA_BIN="${JAVA_HOME}/bin/java"
    elif command -v java >/dev/null 2>&1; then
      JAVA_BIN="$(command -v java)"
    fi
    
    if [ -n "${JAVA_BIN}" ] && [ -x "${JAVA_BIN}" ]; then
      echo ""
      echo "----------------------------------------"
      echo "JVM 信息"
      echo "----------------------------------------"
      
      # 显示 JVM 版本
      if "${JAVA_BIN}" -version >/dev/null 2>&1; then
        JAVA_VERSION="$("${JAVA_BIN}" -version 2>&1 | head -n 1 || true)"
        echo "Java 版本: ${JAVA_VERSION}"
      fi
      
      # 显示JVM启动参数
      if command -v ps >/dev/null 2>&1; then
        JVM_ARGS_FULL="$(ps -p "${PID}" -o args= 2>/dev/null | sed 's/^[[:space:]]*//' || true)"
        if [ -n "${JVM_ARGS_FULL}" ]; then
          echo "JVM 启动参数:"
          echo "${JVM_ARGS_FULL}" | tr ' ' '\n' | grep -E "^-X|^-D|^-javaagent" | sed 's/^/  /' || echo "  无特殊参数"
        fi
      fi
      
      # 显示堆内存使用情况（需要jstat）
      if command -v jstat >/dev/null 2>&1 && jstat -gc "${PID}" >/dev/null 2>&1; then
        JSTAT_OUTPUT="$(jstat -gc "${PID}" 2>/dev/null | tail -n 1 || true)"
        if [ -n "${JSTAT_OUTPUT}" ]; then
          echo ""
          echo "堆内存统计（jstat -gc）:"
          echo "${JSTAT_OUTPUT}" | awk '{printf "  Survivor 0: 容量=%s, 已用=%s\n", $1, $3}'
          echo "${JSTAT_OUTPUT}" | awk '{printf "  Survivor 1: 容量=%s, 已用=%s\n", $2, $4}'
          echo "${JSTAT_OUTPUT}" | awk '{printf "  Eden: 容量=%s, 已用=%s\n", $5, $6}'
          echo "${JSTAT_OUTPUT}" | awk '{printf "  Old: 容量=%s, 已用=%s\n", $7, $8}'
          echo "${JSTAT_OUTPUT}" | awk '{printf "  Metaspace: 容量=%s, 已用=%s\n", $9, $10}'
          echo "${JSTAT_OUTPUT}" | awk '{printf "  Compressed Class Space: 容量=%s, 已用=%s\n", $11, $12}'
          
          # 计算堆使用率（使用 awk 处理浮点数）
          HEAP_CALC="$(echo "${JSTAT_OUTPUT}" | awk '{
            s0c = $1 + 0; s1c = $2 + 0; ec = $5 + 0; oc = $7 + 0;
            s0u = $3 + 0; s1u = $4 + 0; eu = $6 + 0; ou = $8 + 0;
            heap_total = s0c + s1c + ec + oc;
            heap_used = s0u + s1u + eu + ou;
            if (heap_total > 0) {
              heap_usage = (heap_used * 100) / heap_total;
              heap_total_kb = int(heap_total);
              heap_used_kb = int(heap_used);
              printf "%d %d %.1f", heap_total_kb, heap_used_kb, heap_usage;
            } else {
              printf "0 0 0";
            }
          }')"
          
          # 使用 awk 提取并验证数值，确保输出为纯整数
          HEAP_TOTAL_KB="$(echo "${HEAP_CALC}" | awk '{val=int($1+0); if(val<0) val=0; printf "%d", val}')"
          HEAP_USED_KB="$(echo "${HEAP_CALC}" | awk '{val=int($2+0); if(val<0) val=0; printf "%d", val}')"
          HEAP_USAGE_PERCENT="$(echo "${HEAP_CALC}" | awk '{val=int($3+0.5); if(val<0) val=0; if(val>100) val=100; printf "%d", val}')"
          
          # 使用 awk 进行数值比较，避免 shell 浮点数问题
          HEAP_TOTAL_VALID="$(echo "${HEAP_TOTAL_KB}" | awk '{if($1>0 && $1~/^[0-9]+$/) print "1"; else print "0"}')"
          
          if [ "${HEAP_TOTAL_VALID}" = "1" ]; then
            HEAP_TOTAL_BYTES=$((HEAP_TOTAL_KB * 1024))
            HEAP_USED_BYTES=$((HEAP_USED_KB * 1024))
            HEAP_TOTAL_FORMATTED="$(format_bytes "${HEAP_TOTAL_BYTES}")"
            HEAP_USED_FORMATTED="$(format_bytes "${HEAP_USED_BYTES}")"
            echo ""
            echo "堆内存汇总:"
            echo "  总容量: ${HEAP_TOTAL_FORMATTED}"
            echo "  已使用: ${HEAP_USED_FORMATTED} (${HEAP_USAGE_PERCENT}%)"
          fi
        fi
      fi
      
      # 显示GC信息（需要jstat）
      if command -v jstat >/dev/null 2>&1 && jstat -gccapacity "${PID}" >/dev/null 2>&1; then
        GCCAP_OUTPUT="$(jstat -gccapacity "${PID}" 2>/dev/null | tail -n 1 || true)"
        if [ -n "${GCCAP_OUTPUT}" ]; then
          echo ""
          echo "GC 容量信息:"
          echo "${GCCAP_OUTPUT}" | awk '{printf "  NGCMN: %s, NGCMX: %s, NGC: %s\n", $1, $2, $3}'
          echo "${GCCAP_OUTPUT}" | awk '{printf "  OGCMN: %s, OGCMX: %s, OGC: %s\n", $4, $5, $6}'
        fi
      fi
    else
      echo ""
      echo "----------------------------------------"
      echo "JVM 信息（需要 JDK 支持）"
      echo "----------------------------------------"
      echo "  无法获取 JVM 信息，请确保已安装 JDK"
    fi
  fi
  
  # 显示内存详细信息
  if [ "${SHOW_MEMORY}" = "true" ]; then
    echo ""
    echo "----------------------------------------"
    echo "内存详细信息"
    echo "----------------------------------------"
    if command -v ps >/dev/null 2>&1; then
      if ps -p "${PID}" >/dev/null 2>&1; then
        # 尝试获取更详细的内存信息
        MEM_INFO="$(ps -p "${PID}" -o rss=,vsz=,pmem= 2>/dev/null || true)"
        if [ -n "${MEM_INFO}" ]; then
          RSS_DETAIL="$(echo "${MEM_INFO}" | awk '{print $1}')"
          VSZ_DETAIL="$(echo "${MEM_INFO}" | awk '{print $2}')"
          PMEM_DETAIL="$(echo "${MEM_INFO}" | awk '{print $3}')"
          
          if [ -n "${RSS_DETAIL}" ] && [ "${RSS_DETAIL}" != "-" ]; then
            RSS_BYTES=$((RSS_DETAIL * 1024))
            RSS_FORMATTED="$(format_bytes "${RSS_BYTES}")"
            echo "物理内存（RSS）: ${RSS_FORMATTED} (${RSS_DETAIL}KB)"
          fi
          
          if [ -n "${VSZ_DETAIL}" ] && [ "${VSZ_DETAIL}" != "-" ]; then
            VSZ_BYTES=$((VSZ_DETAIL * 1024))
            VSZ_FORMATTED="$(format_bytes "${VSZ_BYTES}")"
            echo "虚拟内存（VSZ）: ${VSZ_FORMATTED} (${VSZ_DETAIL}KB)"
          fi
          
          if [ -n "${PMEM_DETAIL}" ] && [ "${PMEM_DETAIL}" != "-" ]; then
            echo "内存占比: ${PMEM_DETAIL}%"
          fi
        fi
      fi
    fi
  fi
  
  # 显示线程转储
  if [ "${SHOW_THREAD_DUMP}" = "true" ]; then
    JAVA_BIN=""
    if [ -n "${JAVA_HOME:-}" ]; then
      JAVA_BIN="${JAVA_HOME}/bin/java"
    elif command -v java >/dev/null 2>&1; then
      JAVA_BIN="$(command -v java)"
    fi
    
    if [ -n "${JAVA_BIN}" ] && [ -x "${JAVA_BIN}" ] && command -v jstack >/dev/null 2>&1; then
      echo ""
      echo "----------------------------------------"
      echo "线程转储"
      echo "----------------------------------------"
      
      # 显示线程统计
      THREAD_INFO="$(jstack "${PID}" 2>/dev/null | grep -E "^\"|java.lang.Thread.State" | head -n 50 || true)"
      if [ -n "${THREAD_INFO}" ]; then
        THREAD_COUNT="$(echo "${THREAD_INFO}" | grep -c "^\"[^\"]*\"" || echo "0")"
        RUNNABLE_COUNT="$(echo "${THREAD_INFO}" | grep -c "RUNNABLE" || echo "0")"
        WAITING_COUNT="$(echo "${THREAD_INFO}" | grep -c "WAITING" || echo "0")"
        BLOCKED_COUNT="$(echo "${THREAD_INFO}" | grep -c "BLOCKED" || echo "0")"
        TIMED_WAITING_COUNT="$(echo "${THREAD_INFO}" | grep -c "TIMED_WAITING" || echo "0")"
        
        echo "线程统计:"
        echo "  总线程数: ${THREAD_COUNT}"
        echo "  RUNNABLE: ${RUNNABLE_COUNT}"
        echo "  WAITING: ${WAITING_COUNT}"
        echo "  BLOCKED: ${BLOCKED_COUNT}"
        echo "  TIMED_WAITING: ${TIMED_WAITING_COUNT}"
        echo ""
        echo "线程详情（前 20 行）:"
        echo "${THREAD_INFO}" | head -n 20
        echo "..."
        echo "（使用 jstack ${PID} 查看完整线程转储）"
      else
        echo "无法获取线程转储"
      fi
    else
      echo ""
      echo "----------------------------------------"
      echo "线程转储（需要 jstack 支持）"
      echo "----------------------------------------"
      echo "  无法获取线程转储，请确保已安装 JDK 并配置 jstack"
    fi
  fi
  
  # 默认显示更多信息（即使没有指定参数）
  if [ "${SHOW_JVM}" != "true" ] && [ "${SHOW_MEMORY}" != "true" ] && [ "${SHOW_THREAD_DUMP}" != "true" ]; then
    # 显示网络连接数
    if command -v netstat >/dev/null 2>&1; then
      NETSTAT_COUNT="$(netstat -an 2>/dev/null | grep -c "${PID}" || echo "0")"
      if [ "${NETSTAT_COUNT}" -gt 0 ]; then
        echo ""
        echo "----------------------------------------"
        echo "网络连接"
        echo "----------------------------------------"
        echo "连接数: ${NETSTAT_COUNT}"
      fi
    elif command -v ss >/dev/null 2>&1; then
      SS_COUNT="$(ss -anp 2>/dev/null | grep -c "pid=${PID}" || echo "0")"
      if [ "${SS_COUNT}" -gt 0 ]; then
        echo ""
        echo "----------------------------------------"
        echo "网络连接"
        echo "----------------------------------------"
        echo "连接数: ${SS_COUNT}"
      fi
    fi
    
    # 显示文件描述符
    if [ -d "/proc/${PID}/fd" ]; then
      FD_COUNT="$(ls -1 "/proc/${PID}/fd" 2>/dev/null | wc -l || echo "0")"
      if [ "${FD_COUNT}" -gt 0 ]; then
        echo ""
        echo "----------------------------------------"
        echo "文件描述符"
        echo "----------------------------------------"
        echo "打开的文件描述符数: ${FD_COUNT}"
      fi
    fi
  fi
  
  echo "=========================================="
  
  return 0
}

if [ "${FOLLOW_MODE}" = "true" ]; then
  # 实时监控模式
  trap 'echo ""; info "监控已停止"; exit 0' INT TERM
  
  while true; do
    clear
    if ! show_status; then
      echo ""
      info "应用已停止，退出监控"
      exit 0
    fi
    sleep "${INTERVAL}"
  done
else
  # 单次显示模式
  show_status
fi


