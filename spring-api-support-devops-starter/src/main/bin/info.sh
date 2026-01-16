#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  info.sh [-v] [-e] [-f]

参数:
  -v          显示版本信息（应用名称、版本）
  -e          显示环境变量配置
  -f          显示文件路径信息
  -h, --help  显示帮助信息

说明:
  - 默认显示所有信息
  - 可以组合使用多个参数来显示特定信息
EOF
}

SHOW_VERSION="true"
SHOW_ENV="true"
SHOW_FILES="true"

while [ $# -gt 0 ]; do
  case "$1" in
    -v)
      SHOW_VERSION="true"
      SHOW_ENV="false"
      SHOW_FILES="false"
      shift
      ;;
    -e)
      SHOW_ENV="true"
      SHOW_VERSION="false"
      SHOW_FILES="false"
      shift
      ;;
    -f)
      SHOW_FILES="true"
      SHOW_VERSION="false"
      SHOW_ENV="false"
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

# 确保可读取到默认环境配置
ensure_env_file
load_env

BASE_DIR="$(get_base_dir)"
APP_NAME="$(get_app_name)"
APP_VERSION="$(get_app_version)"
JAR_FILE="$(get_jar_file)"
PID_FILE="$(get_pid_file)"
CONSOLE_LOG="$(get_console_log)"

format_bytes() {
  var_bytes="${1:-0}"
  if [ "${var_bytes}" -ge 1073741824 ]; then
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

if [ "${SHOW_VERSION}" = "true" ]; then
  echo "=========================================="
  echo "版本信息"
  echo "=========================================="
  info "应用名称: ${APP_NAME}"
  info "应用版本: ${APP_VERSION}"
  
  # 显示Jar文件信息
  if [ -f "${JAR_FILE}" ]; then
    JAR_SIZE="$(stat -c%s "${JAR_FILE}" 2>/dev/null || stat -f%z "${JAR_FILE}" 2>/dev/null || echo "0")"
    JAR_SIZE_FORMATTED="$(format_bytes "${JAR_SIZE}")"
    JAR_MODIFY_TIME="$(stat -c%y "${JAR_FILE}" 2>/dev/null | cut -d'.' -f1 || stat -f%Sm "${JAR_FILE}" 2>/dev/null || echo "未知")"
    info "Jar 大小: ${JAR_SIZE_FORMATTED}"
    info "Jar 修改时间: ${JAR_MODIFY_TIME}"
  fi
  
  # 显示Java版本
  JAVA_BIN=""
  if [ -n "${JAVA_HOME:-}" ]; then
    JAVA_BIN="${JAVA_HOME}/bin/java"
  elif command -v java >/dev/null 2>&1; then
    JAVA_BIN="$(command -v java)"
  fi
  if [ -n "${JAVA_BIN}" ] && [ -x "${JAVA_BIN}" ]; then
    JAVA_VERSION="$("${JAVA_BIN}" -version 2>&1 | head -n 1 || echo "未知")"
    info "Java 版本: ${JAVA_VERSION}"
  fi
  
  # 显示操作系统信息
  if [ -f /etc/os-release ]; then
    OS_NAME="$(grep "^NAME=" /etc/os-release | cut -d'"' -f2 2>/dev/null || echo "未知")"
    OS_VERSION="$(grep "^VERSION=" /etc/os-release | cut -d'"' -f2 2>/dev/null || echo "未知")"
    info "操作系统: ${OS_NAME} ${OS_VERSION}"
  elif command -v uname >/dev/null 2>&1; then
    OS_INFO="$(uname -s) $(uname -r)"
    info "操作系统: ${OS_INFO}"
  fi
  
  echo ""
fi

if [ "${SHOW_FILES}" = "true" ]; then
  echo "=========================================="
  echo "文件路径"
  echo "=========================================="
  info "发布目录: ${BASE_DIR}"
  info "Jar 路径: ${JAR_FILE}"
  info "PID 文件: ${PID_FILE}"
  info "日志文件: ${CONSOLE_LOG}"
  
  # 显示目录信息
  if [ -d "${BASE_DIR}" ]; then
    BASE_SIZE="$(du -sh "${BASE_DIR}" 2>/dev/null | cut -f1 || echo "未知")"
    info "发布目录大小: ${BASE_SIZE}"
  fi
  
  # 显示日志目录信息
  LOG_DIR="$(dirname "${CONSOLE_LOG}" 2>/dev/null || echo "")"
  if [ -n "${LOG_DIR}" ] && [ -d "${LOG_DIR}" ]; then
    LOG_SIZE="$(du -sh "${LOG_DIR}" 2>/dev/null | cut -f1 || echo "未知")"
    LOG_COUNT="$(find "${LOG_DIR}" -type f 2>/dev/null | wc -l || echo "0")"
    info "日志目录: ${LOG_DIR}"
    info "日志目录大小: ${LOG_SIZE}"
    info "日志文件数量: ${LOG_COUNT}"
  fi
  
  # 显示备份目录信息
  BACKUP_DIR_VAL="${BACKUP_DIR:-${BASE_DIR}/backup}"
  if [ -d "${BACKUP_DIR_VAL}" ]; then
    BACKUP_SIZE="$(du -sh "${BACKUP_DIR_VAL}" 2>/dev/null | cut -f1 || echo "未知")"
    BACKUP_COUNT="$(find "${BACKUP_DIR_VAL}" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | wc -l || echo "0")"
    info "备份目录: ${BACKUP_DIR_VAL}"
    info "备份目录大小: ${BACKUP_SIZE}"
    info "备份数量: ${BACKUP_COUNT}"
  fi
  
  # 显示配置文件信息
  if [ -d "${BASE_DIR}/config" ]; then
    CONFIG_COUNT="$(find "${BASE_DIR}/config" -type f 2>/dev/null | wc -l || echo "0")"
    info "配置文件目录: ${BASE_DIR}/config"
    info "配置文件数量: ${CONFIG_COUNT}"
  fi
  
  echo ""
fi

if [ "${SHOW_ENV}" = "true" ]; then
  echo "=========================================="
  echo "环境配置"
  echo "=========================================="
  info "默认时区: ${TIMEZONE:-未设置}"
  info "默认编码: ${CHARSET:-未设置}"
  info "JVM 参数: ${JVM_ARGS:-未设置}"
  info "SkyWalking: ${SKYWALKING_ENABLED:-false}"
  info "Arthas: ${ARTHAS_ENABLED:-false}"
  
  # 显示系统环境变量
  if [ -n "${JAVA_HOME:-}" ]; then
    info "JAVA_HOME: ${JAVA_HOME}"
  fi
  
  if [ -n "${SERVER_PORT:-}" ]; then
    info "SERVER_PORT: ${SERVER_PORT}"
  fi
  
  # 显示系统资源信息
  echo ""
  echo "----------------------------------------"
  echo "系统资源"
  echo "----------------------------------------"
  
  # 内存信息
  if [ -f /proc/meminfo ]; then
    MEM_TOTAL="$(grep "^MemTotal:" /proc/meminfo | awk '{print $2}' || echo "0")"
    MEM_AVAILABLE="$(grep "^MemAvailable:" /proc/meminfo | awk '{print $2}' || echo "0")"
    if [ "${MEM_TOTAL}" != "0" ]; then
      MEM_TOTAL_KB=$((MEM_TOTAL * 1024))
      MEM_AVAILABLE_KB=$((MEM_AVAILABLE * 1024))
      MEM_USED_KB=$((MEM_TOTAL - MEM_AVAILABLE))
      MEM_USED_BYTES=$((MEM_USED_KB * 1024))
      MEM_TOTAL_FORMATTED="$(format_bytes "${MEM_TOTAL_KB}")"
      MEM_AVAILABLE_FORMATTED="$(format_bytes "${MEM_AVAILABLE_KB}")"
      MEM_USED_FORMATTED="$(format_bytes "${MEM_USED_BYTES}")"
      MEM_USAGE_PERCENT=$((MEM_USED_KB * 100 / MEM_TOTAL))
      info "总内存: ${MEM_TOTAL_FORMATTED}"
      info "可用内存: ${MEM_AVAILABLE_FORMATTED}"
      info "已用内存: ${MEM_USED_FORMATTED} (${MEM_USAGE_PERCENT}%)"
    fi
  fi
  
  # CPU信息
  if [ -f /proc/cpuinfo ]; then
    CPU_CORES="$(grep -c "^processor" /proc/cpuinfo 2>/dev/null || echo "1")"
    CPU_MODEL="$(grep "^model name" /proc/cpuinfo | head -n 1 | cut -d':' -f2 | sed 's/^[[:space:]]*//' || echo "未知")"
    info "CPU 核心数: ${CPU_CORES}"
    info "CPU 型号: ${CPU_MODEL}"
  fi
  
  # 磁盘信息
  DISK_INFO="$(df -h "${BASE_DIR}" 2>/dev/null | tail -n 1 || true)"
  if [ -n "${DISK_INFO}" ]; then
    DISK_TOTAL="$(echo "${DISK_INFO}" | awk '{print $2}')"
    DISK_USED="$(echo "${DISK_INFO}" | awk '{print $3}')"
    DISK_AVAIL="$(echo "${DISK_INFO}" | awk '{print $4}')"
    DISK_USAGE="$(echo "${DISK_INFO}" | awk '{print $5}')"
    info "磁盘总容量: ${DISK_TOTAL}"
    info "磁盘已用: ${DISK_USED} (${DISK_USAGE})"
    info "磁盘可用: ${DISK_AVAIL}"
  fi
  
  echo ""
fi


