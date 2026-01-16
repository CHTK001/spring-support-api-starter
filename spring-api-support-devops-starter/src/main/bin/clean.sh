#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  clean.sh [--force] [--dry-run] [--pid 进程ID]

参数:
  --force         强制清理，不询问确认
  --dry-run       仅显示将被清理的文件，不实际删除
  --pid 进程ID    仅清理指定进程占用的已删除文件

说明:
  - 清理所有被删除但仍被进程占用的文件（通过 lsof 查找）
  - 这些文件通常占用磁盘空间但无法通过正常方式访问
  - 建议在清理前先停止相关应用
EOF
}

FORCE="false"
DRY_RUN="false"
TARGET_PID=""

while [ $# -gt 0 ]; do
  case "$1" in
    --force)
      FORCE="true"
      shift
      ;;
    --dry-run)
      DRY_RUN="true"
      shift
      ;;
    --pid)
      TARGET_PID="${2:-}"
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

# 检查是否有 lsof 命令
if ! command -v lsof >/dev/null 2>&1; then
  die "未找到 lsof 命令，请先安装 lsof"
fi

info "查找被删除但仍被占用的文件..."

# 查找已删除的文件
TMP_FILE="/tmp/clean_deleted_$$"
rm -f "${TMP_FILE}"

if [ -n "${TARGET_PID}" ]; then
  # 仅查找指定进程的已删除文件
  if ! is_running "${TARGET_PID}"; then
    warn "进程 ${TARGET_PID} 不存在或已停止"
    exit 1
  fi
  lsof -p "${TARGET_PID}" 2>/dev/null | grep -E "^\S+\s+\S+\s+\S+\s+\S+\s+.*\s+\(deleted\)" > "${TMP_FILE}" || true
else
  # 查找所有已删除的文件
  lsof 2>/dev/null | grep -E "^\S+\s+\S+\s+\S+\s+\S+\s+.*\s+\(deleted\)" > "${TMP_FILE}" || true
fi

if [ ! -s "${TMP_FILE}" ]; then
  rm -f "${TMP_FILE}"
  info "未找到被删除但仍被占用的文件"
  exit 0
fi

# 解析文件路径（lsof 输出格式：COMMAND PID USER FD TYPE DEVICE SIZE/OFF NODE NAME）
CLEAN_COUNT=0
CLEAN_SIZE=0
CLEAN_LIST=""

while IFS= read -r line || [ -n "${line}" ]; do
  if [ -z "${line}" ]; then
    continue
  fi
  
  # 提取文件路径（最后一列，但需要处理包含空格的情况）
  # lsof 输出格式示例: java 12345 user 1u REG 8,1 123456 12345 /path/to/file (deleted)
  FILE_PATH=""
  # 从后往前找，找到 (deleted) 前面的路径
  if echo "${line}" | grep -q "(deleted)"; then
    # 移除 (deleted) 标记，然后提取路径
    LINE_WITHOUT_DELETED="$(echo "${line}" | sed 's/(deleted)//')"
    # 提取最后一列作为文件路径
    FILE_PATH="$(echo "${LINE_WITHOUT_DELETED}" | awk '{print $NF}')"
  fi
  
  if [ -z "${FILE_PATH}" ] || [ "${FILE_PATH}" = "(deleted)" ]; then
    continue
  fi
  
  # 检查文件是否真的不存在（已删除）
  if [ ! -e "${FILE_PATH}" ]; then
    # 获取进程信息
    PID="$(echo "${line}" | awk '{print $2}')"
    COMMAND="$(echo "${line}" | awk '{print $1}')"
    
    # 获取文件大小（如果可能）
    FILE_SIZE=0
    if [ -n "${PID}" ]; then
      # 尝试从 /proc/PID/fd/ 获取文件大小
      FD_NUM="$(echo "${line}" | awk '{print $4}' | sed 's/[^0-9]//g')"
      if [ -n "${FD_NUM}" ] && [ -r "/proc/${PID}/fd/${FD_NUM}" ]; then
        FILE_SIZE="$(stat -c%s "/proc/${PID}/fd/${FD_NUM}" 2>/dev/null || echo "0")"
      fi
    fi
    
    CLEAN_LIST="${CLEAN_LIST}${CLEAN_LIST:+
}进程 ${PID} (${COMMAND}): ${FILE_PATH} (${FILE_SIZE} 字节)"
    CLEAN_COUNT=$((CLEAN_COUNT + 1))
    CLEAN_SIZE=$((CLEAN_SIZE + FILE_SIZE))
  fi
done < "${TMP_FILE}"

rm -f "${TMP_FILE}"

if [ "${CLEAN_COUNT}" -eq 0 ]; then
  info "未找到需要清理的文件"
  exit 0
fi

# 显示清理列表
echo "=========================================="
echo "找到 ${CLEAN_COUNT} 个被删除但仍被占用的文件"
echo "=========================================="
echo "${CLEAN_LIST}"
echo "=========================================="
echo "总大小: ${CLEAN_SIZE} 字节"

if [ "${DRY_RUN}" = "true" ]; then
  info "（仅预览模式，未实际清理）"
  exit 0
fi

# 确认清理
if [ "${FORCE}" != "true" ]; then
  warn "警告: 清理这些文件可能会影响正在运行的进程"
  warn "建议先停止相关应用后再清理"
  warn "是否继续？(yes/no)"
  read -r CONFIRM
  if [ "${CONFIRM}" != "yes" ]; then
    info "操作已取消"
    exit 0
  fi
fi

# 执行清理
info "开始清理..."
CLEANED_COUNT=0
CLEANED_SIZE=0

# 重新解析并清理
TMP_FILE="/tmp/clean_deleted_$$"
rm -f "${TMP_FILE}"

if [ -n "${TARGET_PID}" ]; then
  lsof -p "${TARGET_PID}" 2>/dev/null | grep -E "^\S+\s+\S+\s+\S+\s+\S+\s+.*\s+\(deleted\)" > "${TMP_FILE}" || true
else
  lsof 2>/dev/null | grep -E "^\S+\s+\S+\s+\S+\s+\S+\s+.*\s+\(deleted\)" > "${TMP_FILE}" || true
fi

while IFS= read -r line || [ -n "${line}" ]; do
  if [ -z "${line}" ]; then
    continue
  fi
  
  if echo "${line}" | grep -q "(deleted)"; then
    LINE_WITHOUT_DELETED="$(echo "${line}" | sed 's/(deleted)//')"
    FILE_PATH="$(echo "${LINE_WITHOUT_DELETED}" | awk '{print $NF}')"
    
    if [ -z "${FILE_PATH}" ] || [ "${FILE_PATH}" = "(deleted)" ]; then
      continue
    fi
    
    if [ ! -e "${FILE_PATH}" ]; then
      PID="$(echo "${line}" | awk '{print $2}')"
      COMMAND="$(echo "${line}" | awk '{print $1}')"
      FD_NUM="$(echo "${line}" | awk '{print $4}' | sed 's/[^0-9]//g')"
      
      # 显示文件信息
      PID="$(echo "${line}" | awk '{print $2}')"
      COMMAND="$(echo "${line}" | awk '{print $1}')"
      FD_NUM="$(echo "${line}" | awk '{print $4}' | sed 's/[^0-9]//g')"
      
      FILE_SIZE=0
      if [ -n "${PID}" ] && [ -n "${FD_NUM}" ] && [ -r "/proc/${PID}/fd/${FD_NUM}" ]; then
        FILE_SIZE="$(stat -c%s "/proc/${PID}/fd/${FD_NUM}" 2>/dev/null || echo "0")"
      fi
      
      info "发现已删除文件: 进程 ${PID} (${COMMAND}) -> ${FILE_PATH} (${FILE_SIZE} 字节)"
      info "  文件描述符: /proc/${PID}/fd/${FD_NUM}"
      info "  建议: 停止进程 ${PID} 或重启应用以释放该文件"
      CLEANED_COUNT=$((CLEANED_COUNT + 1))
      CLEANED_SIZE=$((CLEANED_SIZE + FILE_SIZE))
    fi
  fi
done < "${TMP_FILE}"

rm -f "${TMP_FILE}"

if [ "${CLEANED_COUNT}" -eq 0 ]; then
  info "未发现需要清理的文件"
  exit 0
fi

info "扫描完成，共发现 ${CLEANED_COUNT} 个被删除但仍被占用的文件，占用空间约 ${CLEANED_SIZE} 字节"
info "提示: 这些文件需要停止相关进程才能释放空间"
info "提示: 可以使用 ./bin/stop.sh 停止应用，或手动停止相关进程"

