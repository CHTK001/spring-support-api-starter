#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  analyze.sh [-a] [-o 输出文件]

参数:
  -a          使用 AI 分析（需要配置 AI API）
  -o 文件      将分析结果输出到文件
  -h, --help  显示帮助信息

说明:
  - 分析当前系统资源和使用情况，推荐最优 JVM 配置
  - 包括 Xms、Xmx、GC 参数等
  - 可以结合 AI 进行更智能的分析
EOF
}

USE_AI="false"
OUTPUT_FILE=""

while [ $# -gt 0 ]; do
  case "$1" in
    -a)
      USE_AI="true"
      shift
      ;;
    -o)
      OUTPUT_FILE="${2:-}"
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

analyze_system() {
  echo "=========================================="
  echo "系统资源分析"
  echo "=========================================="
  
  # 获取系统内存
  if [ -f /proc/meminfo ]; then
    MEM_TOTAL="$(grep "^MemTotal:" /proc/meminfo | awk '{print $2}' || echo "0")"
    MEM_AVAILABLE="$(grep "^MemAvailable:" /proc/meminfo | awk '{print $2}' || echo "0")"
    
    # 使用 awk 验证并转换数值，确保为纯整数
    MEM_TOTAL="$(echo "${MEM_TOTAL}" | awk '{val=int($1+0); if(val<0) val=0; printf "%d", val}')"
    MEM_AVAILABLE="$(echo "${MEM_AVAILABLE}" | awk '{val=int($1+0); if(val<0) val=0; printf "%d", val}')"
    
    # 使用 awk 进行数值比较
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
      
      # 计算推荐堆内存（可用内存的 60-70%）
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
  
  # 获取CPU核心数
  if [ -f /proc/cpuinfo ]; then
    CPU_CORES="$(grep -c "^processor" /proc/cpuinfo 2>/dev/null || echo "1")"
    echo ""
    echo "CPU 核心数: ${CPU_CORES}"
    
    # 推荐GC线程数（CPU核心数的1/4到1/2）
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
  
  # 获取进程内存使用
  if command -v ps >/dev/null 2>&1; then
    RSS="$(ps -p "${PID}" -o rss= 2>/dev/null | tr -d ' ' || echo "0")"
    VSZ="$(ps -p "${PID}" -o vsz= 2>/dev/null | tr -d ' ' || echo "0")"
    MEM="$(ps -p "${PID}" -o %mem= 2>/dev/null | tr -d ' ' || echo "0")"
    
    # 验证 RSS 是否为有效数字
    if [ -z "${RSS}" ] || [ "${RSS}" = "-" ] || ! echo "${RSS}" | grep -qE '^[0-9]+$'; then
      RSS="0"
    fi
    
    if [ "${RSS}" != "0" ] && [ "${RSS}" -gt 0 ] 2>/dev/null; then
      RSS_BYTES=$((RSS * 1024))
      RSS_FORMATTED="$(format_bytes "${RSS_BYTES}")"
      echo "当前物理内存使用: ${RSS_FORMATTED} (${MEM}%)"
      
      # 基于当前使用推荐（当前使用的1.5-2倍）
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
  
  # 获取JVM堆内存使用（需要jstat）
  if command -v jstat >/dev/null 2>&1 && jstat -gc "${PID}" >/dev/null 2>&1; then
    JSTAT_OUTPUT="$(jstat -gc "${PID}" 2>/dev/null | tail -n 1 || true)"
    if [ -n "${JSTAT_OUTPUT}" ]; then
      # 使用 awk 处理浮点数计算
      HEAP_CALC="$(echo "${JSTAT_OUTPUT}" | awk '{
        s0c = $1 + 0; s1c = $2 + 0; ec = $5 + 0; oc = $7 + 0;
        s0u = $3 + 0; s1u = $4 + 0; eu = $6 + 0; ou = $8 + 0;
        heap_total = s0c + s1c + ec + oc;
        heap_used = s0u + s1u + eu + ou;
        printf "%d %d", int(heap_total), int(heap_used);
      }')"
      
      HEAP_TOTAL="$(echo "${HEAP_CALC}" | awk '{print $1}')"
      HEAP_USED="$(echo "${HEAP_CALC}" | awk '{print $2}')"
      
      # 验证数值有效性
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
        
        # 如果使用率超过80%，建议增加堆内存
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

generate_recommendations() {
  echo "=========================================="
  echo "推荐配置"
  echo "=========================================="
  
  # 获取系统信息
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

# 主分析流程
{
  analyze_system
  analyze_running_app || true
  generate_recommendations
  
  if [ "${USE_AI}" = "true" ]; then
    echo "=========================================="
    echo "AI 分析（需要配置 AI API）"
    echo "=========================================="
    echo "AI 分析功能需要配置 AI API，当前版本暂不支持"
    echo "建议手动分析上述结果，或使用专业的 JVM 调优工具"
    echo ""
  fi
} > "${OUTPUT_FILE:-/dev/stdout}"

if [ -n "${OUTPUT_FILE}" ]; then
  info "分析结果已保存到: ${OUTPUT_FILE}"
fi


