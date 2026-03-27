#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  analyze.sh [-a] [-o 输出文件] [--api-log 日志文件] [--oom-log 日志文件] [--top 数量]

参数:
  -a              使用 AI 分析（需要配置 AI API）
  -o 文件         将分析结果输出到文件
  --api-log 文件   指定 API 分析日志文件（默认扫描 <base>/logs）
  --oom-log 文件   指定 OOM 分析日志文件（默认扫描 <base>/logs/*.log）
  --top 数量       热点接口输出条数（默认 10）
  -h, --help      显示帮助信息

说明:
  - 分析系统资源和运行态 JVM 使用情况
  - 自动分析 API 热点、可疑刷屏/扫描行为
  - 自动分析 OOM 关键字、堆转储文件和风险建议
EOF
}

USE_AI="false"
OUTPUT_FILE=""
API_LOG_FILE=""
OOM_LOG_FILE=""
API_TOP_N="10"

while [ $# -gt 0 ]; do
  case "$1" in
    -a)
      USE_AI="true"
      shift
      ;;
    -o)
      if [ $# -lt 2 ]; then
        die "-o 需要指定输出文件"
      fi
      OUTPUT_FILE="${2:-}"
      shift 2
      ;;
    --api-log)
      if [ $# -lt 2 ]; then
        die "--api-log 需要指定日志文件"
      fi
      API_LOG_FILE="${2:-}"
      shift 2
      ;;
    --oom-log)
      if [ $# -lt 2 ]; then
        die "--oom-log 需要指定日志文件"
      fi
      OOM_LOG_FILE="${2:-}"
      shift 2
      ;;
    --top)
      if [ $# -lt 2 ]; then
        die "--top 需要指定正整数"
      fi
      API_TOP_N="${2:-10}"
      if ! echo "${API_TOP_N}" | grep -qE '^[1-9][0-9]*$'; then
        die "无效的 --top 参数: ${API_TOP_N}"
      fi
      shift 2
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
BASE_DIR="$(get_base_dir)"
PID_FILE="$(get_pid_file)"
RUN_DIR="${BASE_DIR}/run"
mkdir -p "${RUN_DIR}"

if [ -n "${OUTPUT_FILE}" ]; then
  OUTPUT_FILE="$(resolve_path "${OUTPUT_FILE}")"
  OUTPUT_DIR="$(dirname "${OUTPUT_FILE}")"
  mkdir -p "${OUTPUT_DIR}"
fi

TMP_FILES=""

register_tmp_file() {
  var_tmp_file="${1:-}"
  if [ -n "${var_tmp_file}" ]; then
    TMP_FILES="${TMP_FILES} ${var_tmp_file}"
  fi
}

create_tmp_file() {
  if command -v mktemp >/dev/null 2>&1; then
    var_tmp="$(mktemp "${RUN_DIR}/analyze.XXXXXX" 2>/dev/null || mktemp "/tmp/analyze.XXXXXX")"
  else
    var_tmp="${RUN_DIR}/analyze.$$.$(date +%s).tmp"
    : > "${var_tmp}"
  fi
  register_tmp_file "${var_tmp}"
  echo "${var_tmp}"
}

cleanup_tmp_files() {
  for var_tmp in ${TMP_FILES}; do
    if [ -n "${var_tmp}" ] && [ -f "${var_tmp}" ]; then
      rm -f "${var_tmp}" || true
    fi
  done
}

trap cleanup_tmp_files EXIT INT TERM

format_bytes() {
  var_bytes="${1:-0}"
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

analyze_system() {
  echo "=========================================="
  echo "系统资源分析"
  echo "=========================================="

  if [ -f /proc/meminfo ]; then
    MEM_TOTAL="$(grep "^MemTotal:" /proc/meminfo | awk '{print $2}' || echo "0")"
    MEM_AVAILABLE="$(grep "^MemAvailable:" /proc/meminfo | awk '{print $2}' || echo "0")"

    MEM_TOTAL="$(echo "${MEM_TOTAL}" | awk '{val=int($1+0); if(val<0) val=0; printf "%d", val}')"
    MEM_AVAILABLE="$(echo "${MEM_AVAILABLE}" | awk '{val=int($1+0); if(val<0) val=0; printf "%d", val}')"

    MEM_TOTAL_VALID="$(echo "${MEM_TOTAL}" | awk '{if($1>0 && $1~/^[0-9]+$/) print "1"; else print "0"}')"
    MEM_AVAILABLE_VALID="$(echo "${MEM_AVAILABLE}" | awk '{if($1>0 && $1~/^[0-9]+$/) print "1"; else print "0"}')"

    if [ "${MEM_TOTAL_VALID}" = "1" ]; then
      MEM_TOTAL_KB=$((MEM_TOTAL * 1024))
      MEM_TOTAL_FORMATTED="$(format_bytes "${MEM_TOTAL_KB}")"
      echo "总内存: ${MEM_TOTAL_FORMATTED}"
    else
      echo "总内存: 无法获取"
    fi

    if [ "${MEM_AVAILABLE_VALID}" = "1" ]; then
      MEM_AVAILABLE_KB=$((MEM_AVAILABLE * 1024))
      MEM_AVAILABLE_FORMATTED="$(format_bytes "${MEM_AVAILABLE_KB}")"
      echo "可用内存: ${MEM_AVAILABLE_FORMATTED}"

      RECOMMENDED_HEAP_KB=$((MEM_AVAILABLE * 60 / 100))
      RECOMMENDED_HEAP_MAX_KB=$((MEM_AVAILABLE * 70 / 100))
      RECOMMENDED_HEAP_FORMATTED="$(format_bytes "${RECOMMENDED_HEAP_KB}")"
      RECOMMENDED_HEAP_MAX_FORMATTED="$(format_bytes "${RECOMMENDED_HEAP_MAX_KB}")"

      echo ""
      echo "推荐堆内存配置:"
      echo "  -Xms${RECOMMENDED_HEAP_FORMATTED}"
      echo "  -Xmx${RECOMMENDED_HEAP_MAX_FORMATTED}"
    else
      echo "可用内存: 无法获取"
      echo ""
      echo "推荐堆内存配置: 无法计算（内存信息不可用）"
    fi
  fi

  if [ -f /proc/cpuinfo ]; then
    CPU_CORES="$(grep -c "^processor" /proc/cpuinfo 2>/dev/null || echo "1")"
    echo ""
    echo "CPU 核心数: ${CPU_CORES}"

    GC_THREADS=$((CPU_CORES / 4))
    if [ "${GC_THREADS}" -lt 1 ]; then
      GC_THREADS=1
    fi
    GC_THREADS_MAX=$((CPU_CORES / 2))
    if [ "${GC_THREADS_MAX}" -lt 1 ]; then
      GC_THREADS_MAX=1
    fi

    echo "推荐 GC 线程数: ${GC_THREADS}-${GC_THREADS_MAX}"
  fi

  echo ""
}

analyze_running_app() {
  if [ ! -f "${PID_FILE}" ]; then
    echo "应用未运行，无法分析运行状态"
    return 1
  fi

  PID="$(cat "${PID_FILE}" || true)"
  if ! is_running "${PID}"; then
    echo "应用进程不存在，无法分析运行状态"
    return 1
  fi

  echo "=========================================="
  echo "运行状态分析"
  echo "=========================================="
  echo "PID: ${PID}"

  if command -v ps >/dev/null 2>&1; then
    RSS="$(ps -p "${PID}" -o rss= 2>/dev/null | tr -d ' ' || echo "0")"
    MEM="$(ps -p "${PID}" -o %mem= 2>/dev/null | tr -d ' ' || echo "0")"

    if [ -z "${RSS}" ] || [ "${RSS}" = "-" ] || ! echo "${RSS}" | grep -qE '^[0-9]+$'; then
      RSS="0"
    fi

    if [ "${RSS}" != "0" ] && [ "${RSS}" -gt 0 ] 2>/dev/null; then
      RSS_BYTES=$((RSS * 1024))
      RSS_FORMATTED="$(format_bytes "${RSS_BYTES}")"
      echo "当前物理内存使用: ${RSS_FORMATTED} (${MEM}%)"

      RECOMMENDED_XMS_KB=$((RSS * 150 / 100))
      RECOMMENDED_XMX_KB=$((RSS * 200 / 100))
      RECOMMENDED_XMS_BYTES=$((RECOMMENDED_XMS_KB * 1024))
      RECOMMENDED_XMX_BYTES=$((RECOMMENDED_XMX_KB * 1024))
      RECOMMENDED_XMS_FORMATTED="$(format_bytes "${RECOMMENDED_XMS_BYTES}")"
      RECOMMENDED_XMX_FORMATTED="$(format_bytes "${RECOMMENDED_XMX_BYTES}")"

      echo ""
      echo "基于当前使用推荐:"
      echo "  -Xms${RECOMMENDED_XMS_FORMATTED}"
      echo "  -Xmx${RECOMMENDED_XMX_FORMATTED}"
    fi
  fi

  if command -v jstat >/dev/null 2>&1 && jstat -gc "${PID}" >/dev/null 2>&1; then
    JSTAT_OUTPUT="$(jstat -gc "${PID}" 2>/dev/null | tail -n 1 || true)"
    if [ -n "${JSTAT_OUTPUT}" ]; then
      HEAP_CALC="$(echo "${JSTAT_OUTPUT}" | awk '{
        s0c = $1 + 0; s1c = $2 + 0; ec = $5 + 0; oc = $7 + 0;
        s0u = $3 + 0; s1u = $4 + 0; eu = $6 + 0; ou = $8 + 0;
        heap_total = s0c + s1c + ec + oc;
        heap_used = s0u + s1u + eu + ou;
        printf "%d %d", int(heap_total), int(heap_used);
      }')"

      HEAP_TOTAL="$(echo "${HEAP_CALC}" | awk '{print $1}')"
      HEAP_USED="$(echo "${HEAP_CALC}" | awk '{print $2}')"

      if [ -z "${HEAP_TOTAL}" ] || ! echo "${HEAP_TOTAL}" | grep -qE '^[0-9]+$'; then
        HEAP_TOTAL="0"
      fi
      if [ -z "${HEAP_USED}" ] || ! echo "${HEAP_USED}" | grep -qE '^[0-9]+$'; then
        HEAP_USED="0"
      fi

      if [ "${HEAP_TOTAL}" -gt 0 ] 2>/dev/null; then
        HEAP_USAGE_PERCENT=$((HEAP_USED * 100 / HEAP_TOTAL))
        HEAP_TOTAL_KB=$((HEAP_TOTAL * 1024))
        HEAP_USED_KB=$((HEAP_USED * 1024))
        HEAP_TOTAL_FORMATTED="$(format_bytes "${HEAP_TOTAL_KB}")"
        HEAP_USED_FORMATTED="$(format_bytes "${HEAP_USED_KB}")"

        echo ""
        echo "当前堆内存:"
        echo "  总容量: ${HEAP_TOTAL_FORMATTED}"
        echo "  已使用: ${HEAP_USED_FORMATTED} (${HEAP_USAGE_PERCENT}%)"

        if [ "${HEAP_USAGE_PERCENT}" -gt 80 ] 2>/dev/null; then
          RECOMMENDED_INCREASE=$((HEAP_TOTAL * 120 / 100))
          RECOMMENDED_INCREASE_BYTES=$((RECOMMENDED_INCREASE * 1024))
          RECOMMENDED_INCREASE_FORMATTED="$(format_bytes "${RECOMMENDED_INCREASE_BYTES}")"
          echo ""
          echo "警告: 堆内存使用率较高，建议增加堆内存:"
          echo "  -Xmx${RECOMMENDED_INCREASE_FORMATTED}"
        fi
      fi
    fi
  fi

  echo ""
}

build_log_list() {
  var_mode="${1:-api}"
  var_output_file="${2:-}"
  var_custom_file="${3:-}"

  : > "${var_output_file}"

  if [ -n "${var_custom_file}" ]; then
    var_custom_file="$(resolve_path "${var_custom_file}")"
    if [ -f "${var_custom_file}" ]; then
      echo "${var_custom_file}" >> "${var_output_file}"
    else
      warn "指定日志文件不存在: ${var_custom_file}"
    fi
    return 0
  fi

  if [ ! -d "${BASE_DIR}/logs" ]; then
    return 0
  fi

  if [ "${var_mode}" = "api" ]; then
    find "${BASE_DIR}/logs" -maxdepth 2 -type f \( -name "*.log" -o -name "*.txt" \) 2>/dev/null | sort >> "${var_output_file}" || true
  else
    find "${BASE_DIR}/logs" -maxdepth 2 -type f -name "*.log" 2>/dev/null | sort >> "${var_output_file}" || true
  fi
}

collect_api_events_from_logs() {
  var_log_list_file="${1:-}"
  var_output_file="${2:-}"

  : > "${var_output_file}"

  while IFS= read -r var_log_file; do
    [ -f "${var_log_file}" ] || continue
    grep -hE '(GET|POST|PUT|DELETE|PATCH|OPTIONS|HEAD)[[:space:]]+/[^[:space:]]+' "${var_log_file}" 2>/dev/null || true
  done < "${var_log_list_file}" | awk '
{
  for (i = 1; i <= NF; i++) {
    method_candidate = $i
    gsub(/^[\"\[]+/, "", method_candidate)
    gsub(/[\"\],;]+$/, "", method_candidate)
    if (method_candidate ~ /^(GET|POST|PUT|DELETE|PATCH|OPTIONS|HEAD)$/ && (i + 1) <= NF) {
      method = method_candidate
      path = $(i + 1)
      gsub(/^[\"\[]+/, "", path)
      gsub(/[\"\],;]+$/, "", path)
      sub(/\?.*$/, "", path)
      if (path ~ /^\//) {
        print method " " path
      }
      break
    }
  }
}
' > "${var_output_file}"
}

analyze_api_access() {
  echo "=========================================="
  echo "API 访问分析"
  echo "=========================================="

  API_LOG_LIST_FILE="$(create_tmp_file)"
  API_EVENT_FILE="$(create_tmp_file)"
  API_TOP_FILE="$(create_tmp_file)"
  BOT_UA_FILE="$(create_tmp_file)"

  build_log_list "api" "${API_LOG_LIST_FILE}" "${API_LOG_FILE}"

  if [ ! -s "${API_LOG_LIST_FILE}" ]; then
    echo "未找到可分析的 API 日志文件（默认扫描: ${BASE_DIR}/logs）"
    echo ""
    return 0
  fi

  echo "分析日志源:"
  sed 's/^/  - /' "${API_LOG_LIST_FILE}"
  echo ""

  collect_api_events_from_logs "${API_LOG_LIST_FILE}" "${API_EVENT_FILE}"
  API_TOTAL_EVENT_COUNT="$(wc -l < "${API_EVENT_FILE}" | tr -d '[:space:]')"

  if [ "${API_TOTAL_EVENT_COUNT}" -eq 0 ] 2>/dev/null; then
    echo "未识别到标准 API 请求日志（可通过 --api-log 指定接入日志）"
    echo ""
    return 0
  fi

  API_UNIQUE_EVENT_COUNT="$(awk '{print $1 " " $2}' "${API_EVENT_FILE}" | sort -u | wc -l | tr -d '[:space:]')"
  echo "请求总数: ${API_TOTAL_EVENT_COUNT}"
  echo "唯一接口数: ${API_UNIQUE_EVENT_COUNT}"
  echo ""

  sort "${API_EVENT_FILE}" | uniq -c | sort -nr | head -n "${API_TOP_N}" > "${API_TOP_FILE}" || true
  echo "热点接口 Top ${API_TOP_N}:"
  if [ -s "${API_TOP_FILE}" ]; then
    awk '{printf "  %s 次  %s %s\n", $1, $2, $3}' "${API_TOP_FILE}"
  else
    echo "  无数据"
  fi
  echo ""

  TOP_HIT_COUNT="$(awk 'NR==1 {print $1}' "${API_TOP_FILE}" 2>/dev/null || echo "0")"
  if [ -z "${TOP_HIT_COUNT}" ]; then
    TOP_HIT_COUNT="0"
  fi

  SUSPICIOUS_PATH_COUNT="$( (grep -E '/(actuator|v3/api-docs|swagger|\.git|wp-admin|phpmyadmin|manager/html|\.env|druid|prometheus)' "${API_EVENT_FILE}" || true) | wc -l | tr -d '[:space:]' )"
  while IFS= read -r var_log_file; do
    [ -f "${var_log_file}" ] || continue
    grep -hEi 'curl/|python-requests|wget/|spider|crawler|scan|nmap|sqlmap|masscan|bot' "${var_log_file}" 2>/dev/null || true
  done < "${API_LOG_LIST_FILE}" > "${BOT_UA_FILE}"
  BOT_UA_COUNT="$(wc -l < "${BOT_UA_FILE}" | tr -d '[:space:]')"

  echo "行为风险识别:"
  if [ "${TOP_HIT_COUNT}" -ge 50 ] 2>/dev/null; then
    echo "  - 检测到高频热点接口（单接口 >= 50 次），需评估是否存在刷屏或压测流量"
  else
    echo "  - 未发现明显刷屏型单接口高频请求"
  fi

  if [ "${SUSPICIOUS_PATH_COUNT}" -gt 0 ] 2>/dev/null; then
    echo "  - 发现 ${SUSPICIOUS_PATH_COUNT} 条疑似扫描路径请求（actuator/swagger/.git 等）"
  else
    echo "  - 未发现常见扫描路径请求"
  fi

  if [ "${BOT_UA_COUNT}" -gt 0 ] 2>/dev/null; then
    echo "  - 发现 ${BOT_UA_COUNT} 条疑似爬虫/脚本 User-Agent 记录"
  else
    echo "  - 未发现明显爬虫 User-Agent 记录"
  fi
  echo ""
}

collect_oom_events_from_logs() {
  var_log_list_file="${1:-}"
  var_output_file="${2:-}"
  var_pattern='OutOfMemoryError|Java heap space|GC overhead limit exceeded|Metaspace|unable to create new native thread|Direct buffer memory|Cannot reserve memory'

  : > "${var_output_file}"

  while IFS= read -r var_log_file; do
    [ -f "${var_log_file}" ] || continue
    grep -nE "${var_pattern}" "${var_log_file}" 2>/dev/null | sed "s#^#${var_log_file}:#" || true
  done < "${var_log_list_file}" > "${var_output_file}"
}

analyze_oom_risk() {
  echo "=========================================="
  echo "OOM 风险分析"
  echo "=========================================="

  OOM_LOG_LIST_FILE="$(create_tmp_file)"
  OOM_EVENT_FILE="$(create_tmp_file)"
  OOM_DUMP_FILE="$(create_tmp_file)"

  build_log_list "oom" "${OOM_LOG_LIST_FILE}" "${OOM_LOG_FILE}"

  if [ ! -s "${OOM_LOG_LIST_FILE}" ]; then
    echo "未找到可分析的 OOM 日志文件（默认扫描: ${BASE_DIR}/logs/*.log）"
  else
    collect_oom_events_from_logs "${OOM_LOG_LIST_FILE}" "${OOM_EVENT_FILE}"
    OOM_EVENT_COUNT="$(wc -l < "${OOM_EVENT_FILE}" | tr -d '[:space:]')"

    if [ "${OOM_EVENT_COUNT}" -gt 0 ] 2>/dev/null; then
      HEAP_OOM_COUNT="$( (grep -E 'Java heap space|GC overhead limit exceeded' "${OOM_EVENT_FILE}" || true) | wc -l | tr -d '[:space:]' )"
      META_OOM_COUNT="$( (grep -E 'Metaspace' "${OOM_EVENT_FILE}" || true) | wc -l | tr -d '[:space:]' )"
      THREAD_OOM_COUNT="$( (grep -E 'unable to create new native thread' "${OOM_EVENT_FILE}" || true) | wc -l | tr -d '[:space:]' )"
      DIRECT_OOM_COUNT="$( (grep -E 'Direct buffer memory|Cannot reserve memory' "${OOM_EVENT_FILE}" || true) | wc -l | tr -d '[:space:]' )"

      echo "检测到 OOM/内存异常关键字: ${OOM_EVENT_COUNT} 条"
      echo "  - 堆内存相关: ${HEAP_OOM_COUNT}"
      echo "  - 元空间相关: ${META_OOM_COUNT}"
      echo "  - 线程创建相关: ${THREAD_OOM_COUNT}"
      echo "  - 直接内存相关: ${DIRECT_OOM_COUNT}"
      echo ""
      echo "最近异常样本（最多 10 条）:"
      tail -n 10 "${OOM_EVENT_FILE}" | sed 's/^/  /'
    else
      echo "日志中未发现明显 OOM 关键字"
    fi
  fi

  if [ -d "${BASE_DIR}" ]; then
    find "${BASE_DIR}" -maxdepth 4 -type f \( -name "*.hprof" -o -name "java_pid*.hprof" \) 2>/dev/null > "${OOM_DUMP_FILE}" || true
  fi

  echo ""
  if [ -s "${OOM_DUMP_FILE}" ]; then
    echo "检测到堆转储文件:"
    while IFS= read -r var_dump_file; do
      [ -f "${var_dump_file}" ] || continue
      var_size="$(du -h "${var_dump_file}" 2>/dev/null | awk '{print $1}' || echo "unknown")"
      echo "  - ${var_dump_file} (${var_size})"
    done < "${OOM_DUMP_FILE}"
  else
    echo "未发现堆转储文件（*.hprof）"
  fi

  OOM_DUMP_ENABLED="false"
  if [ "${DUMP_ON_OOM:-false}" = "true" ]; then
    OOM_DUMP_ENABLED="true"
  fi
  if echo "${JVM_ARGS:-}" | grep -q 'HeapDumpOnOutOfMemoryError'; then
    OOM_DUMP_ENABLED="true"
  fi

  echo ""
  if [ "${OOM_DUMP_ENABLED}" = "true" ]; then
    echo "OOM Dump 配置: 已开启（建议定期清理 hprof，避免磁盘被占满）"
  else
    echo "OOM Dump 配置: 未开启（建议在生产开启 -XX:+HeapDumpOnOutOfMemoryError）"
  fi
  echo ""
}

generate_recommendations() {
  echo "=========================================="
  echo "推荐配置"
  echo "=========================================="

  if [ -f /proc/meminfo ]; then
    MEM_AVAILABLE="$(grep "^MemAvailable:" /proc/meminfo | awk '{print $2}' || echo "0")"
    RECOMMENDED_HEAP_KB=$((MEM_AVAILABLE * 60 / 100))
    RECOMMENDED_HEAP_MAX_KB=$((MEM_AVAILABLE * 70 / 100))
    RECOMMENDED_HEAP_FORMATTED="$(format_bytes "${RECOMMENDED_HEAP_KB}")"
    RECOMMENDED_HEAP_MAX_FORMATTED="$(format_bytes "${RECOMMENDED_HEAP_MAX_KB}")"
  else
    RECOMMENDED_HEAP_FORMATTED="512MB"
    RECOMMENDED_HEAP_MAX_FORMATTED="1GB"
  fi

  if [ -f /proc/cpuinfo ]; then
    CPU_CORES="$(grep -c "^processor" /proc/cpuinfo 2>/dev/null || echo "1")"
    GC_THREADS=$((CPU_CORES / 4))
    if [ "${GC_THREADS}" -lt 1 ]; then
      GC_THREADS=1
    fi
  else
    GC_THREADS=1
  fi

  echo "JVM 参数推荐:"
  echo ""
  echo "# 堆内存配置"
  echo "JVM_ARGS=\"-Xms${RECOMMENDED_HEAP_FORMATTED} -Xmx${RECOMMENDED_HEAP_MAX_FORMATTED}\""
  echo ""
  echo "# GC 配置（G1GC，适用于 Java 8+）"
  echo "JVM_ARGS=\"\${JVM_ARGS} -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=${GC_THREADS}\""
  echo ""
  echo "# 元空间配置"
  echo "JVM_ARGS=\"\${JVM_ARGS} -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m\""
  echo ""
  echo "# OOM 时生成堆转储"
  echo "JVM_ARGS=\"\${JVM_ARGS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=\${BASE_DIR}/logs/heapdump.hprof\""
  echo ""
  echo "# 完整配置示例（可添加到 app.env 或 start.sh -jvm 参数）:"
  echo "JVM_ARGS=\"-Xms${RECOMMENDED_HEAP_FORMATTED} -Xmx${RECOMMENDED_HEAP_MAX_FORMATTED} -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=${GC_THREADS} -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=\${BASE_DIR}/logs/heapdump.hprof\""
  echo ""
}

{
  analyze_system
  analyze_running_app || true
  analyze_api_access
  analyze_oom_risk
  generate_recommendations

  if [ "${USE_AI}" = "true" ]; then
    echo "=========================================="
    echo "AI 分析（需要配置 AI API）"
    echo "=========================================="
    echo "AI 分析功能需要配置 AI API，当前版本暂不支持"
    echo "建议结合 API 热点和 OOM 结果，人工复核可疑行为和内存分配路径"
    echo ""
  fi
} > "${OUTPUT_FILE:-/dev/stdout}"

if [ -n "${OUTPUT_FILE}" ]; then
  info "分析结果已保存到: ${OUTPUT_FILE}"
fi
