#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  history.sh [--backup] [--list] [--detail 备份目录] [--clean 天数] [--count 数量] [--restore 序号]

参数:
  --backup        查看备份历史（默认）
  --list          列表模式（简洁显示）
  --detail 目录   查看指定备份的详细信息
  --clean 天数    清理指定天数之前的备份（默认保留 30 天）
  --count 数量    显示最近 N 个备份（默认显示全部）
  --restore 序号  恢复指定序号的备份（会先停止应用并备份当前状态）
  --jar-only      仅恢复 jar 文件（需配合 --restore 使用）
  --config-only   仅恢复 config 目录（需配合 --restore 使用）
  --logs-only     仅恢复 logs 目录（需配合 --restore 使用）
EOF
}

MODE="backup"
LIST_MODE="false"
DETAIL_DIR=""
CLEAN_DAYS=""
COUNT_LIMIT=""
RESTORE_INDEX=""
RESTORE_JAR_ONLY="false"
RESTORE_CONFIG_ONLY="false"
RESTORE_LOGS_ONLY="false"

while [ $# -gt 0 ]; do
  case "$1" in
    --backup)
      MODE="backup"
      shift
      ;;
    --list)
      LIST_MODE="true"
      shift
      ;;
    --detail)
      DETAIL_DIR="${2:-}"
      shift 2
      ;;
    --clean)
      CLEAN_DAYS="${2:-30}"
      shift 2
      ;;
    --count)
      COUNT_LIMIT="${2:-}"
      shift 2
      ;;
    --restore)
      RESTORE_INDEX="${2:-}"
      shift 2
      ;;
    --jar-only)
      RESTORE_JAR_ONLY="true"
      shift
      ;;
    --config-only)
      RESTORE_CONFIG_ONLY="true"
      shift
      ;;
    --logs-only)
      RESTORE_LOGS_ONLY="true"
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
BASE_DIR="$(get_base_dir)"
BACKUP_BASE_DIR="$(get_backup_dir)"

# 恢复备份功能
if [ -n "${RESTORE_INDEX}" ]; then
  if [ ! -d "${BACKUP_BASE_DIR}" ]; then
    die "备份目录不存在: ${BACKUP_BASE_DIR}"
  fi
  
  # 获取所有备份目录并排序
  TMP_RESTORE_LIST="${BACKUP_BASE_DIR}/.restore_list_$$"
  rm -f "${TMP_RESTORE_LIST}"
  
  if command -v ls >/dev/null 2>&1; then
    ls -1td "${BACKUP_BASE_DIR}"/*/ 2>/dev/null > "${TMP_RESTORE_LIST}" || true
  else
    for item in "${BACKUP_BASE_DIR}"/*; do
      if [ -d "${item}" ]; then
        echo "${item}" >> "${TMP_RESTORE_LIST}"
      fi
    done
    if command -v sort >/dev/null 2>&1; then
      sort -r "${TMP_RESTORE_LIST}" > "${TMP_RESTORE_LIST}.sorted"
      mv "${TMP_RESTORE_LIST}.sorted" "${TMP_RESTORE_LIST}"
    fi
  fi
  
  if [ ! -s "${TMP_RESTORE_LIST}" ]; then
    rm -f "${TMP_RESTORE_LIST}"
    die "暂无备份记录"
  fi
  
  # 找到指定序号的备份
  COUNT=0
  TARGET_BACKUP_DIR=""
  while IFS= read -r BACKUP_DIR; do
    if [ -n "${BACKUP_DIR}" ] && [ -d "${BACKUP_DIR}" ]; then
      COUNT=$((COUNT + 1))
      if [ "${COUNT}" -eq "${RESTORE_INDEX}" ]; then
        TARGET_BACKUP_DIR="${BACKUP_DIR}"
        break
      fi
    fi
  done < "${TMP_RESTORE_LIST}"
  rm -f "${TMP_RESTORE_LIST}"
  
  if [ -z "${TARGET_BACKUP_DIR}" ]; then
    die "未找到序号为 ${RESTORE_INDEX} 的备份"
  fi
  
  # 确定恢复内容
  RESTORE_JAR="false"
  RESTORE_CONFIG="false"
  RESTORE_LOGS="false"
  
  if [ "${RESTORE_JAR_ONLY}" = "true" ]; then
    RESTORE_JAR="true"
  elif [ "${RESTORE_CONFIG_ONLY}" = "true" ]; then
    RESTORE_CONFIG="true"
  elif [ "${RESTORE_LOGS_ONLY}" = "true" ]; then
    RESTORE_LOGS="true"
  else
    # 默认恢复所有内容，但需要检查备份中实际包含什么
    META_FILE="${TARGET_BACKUP_DIR}/.backup_meta"
    if [ -f "${META_FILE}" ]; then
      BACKUP_CONTENT_LINE="$(grep "^备份内容:" "${META_FILE}" || echo "")"
      if echo "${BACKUP_CONTENT_LINE}" | grep -q "jar=true"; then
        RESTORE_JAR="true"
      fi
      if echo "${BACKUP_CONTENT_LINE}" | grep -q "config=true"; then
        RESTORE_CONFIG="true"
      fi
      if echo "${BACKUP_CONTENT_LINE}" | grep -q "logs=true"; then
        RESTORE_LOGS="true"
      fi
    else
      # 如果没有元信息，则根据备份目录中的实际内容判断
      for jar in "${TARGET_BACKUP_DIR}"/*.jar; do
        if [ -f "${jar}" ]; then
          RESTORE_JAR="true"
          break
        fi
      done
      if [ -d "${TARGET_BACKUP_DIR}/config" ]; then
        RESTORE_CONFIG="true"
      fi
      if [ -d "${TARGET_BACKUP_DIR}/logs" ]; then
        RESTORE_LOGS="true"
      fi
    fi
  fi
  
  if [ "${RESTORE_JAR}" = "false" ] && [ "${RESTORE_CONFIG}" = "false" ] && [ "${RESTORE_LOGS}" = "false" ]; then
    die "备份中未找到可恢复的内容"
  fi
  
  info "准备恢复备份: $(basename "${TARGET_BACKUP_DIR}")"
  info "恢复内容: jar=${RESTORE_JAR} config=${RESTORE_CONFIG} logs=${RESTORE_LOGS}"
  
  # 停止应用（如果正在运行）
  PID_FILE="$(get_pid_file)"
  if [ -f "${PID_FILE}" ]; then
    PID="$(cat "${PID_FILE}" || true)"
    if is_running "${PID}"; then
      info "检测到应用正在运行，先停止应用"
      "${BIN_DIR}/stop.sh" || warn "停止应用失败，继续恢复"
    fi
  fi
  
  # 恢复前先备份当前状态
  info "恢复前先备份当前状态"
  TS="$(date +%Y%m%d%H%M%S)"
  PRE_RESTORE_BACKUP_DIR="${BACKUP_BASE_DIR}/pre-restore-${TS}"
  mkdir -p "${PRE_RESTORE_BACKUP_DIR}"
  
  JAR_FILE="$(get_jar_file)"
  if [ "${RESTORE_JAR}" = "true" ] && [ -f "${JAR_FILE}" ]; then
    cp -f "${JAR_FILE}" "${PRE_RESTORE_BACKUP_DIR}/" || warn "备份当前 jar 失败"
  fi
  
  if [ "${RESTORE_CONFIG}" = "true" ] && [ -d "${BASE_DIR}/config" ]; then
    cp -R "${BASE_DIR}/config" "${PRE_RESTORE_BACKUP_DIR}/" || warn "备份当前 config 失败"
  fi
  
  LOG_DIR="$(get_log_dir)"
  if [ "${RESTORE_LOGS}" = "true" ] && [ -d "${LOG_DIR}" ] && [ -n "$(ls -A "${LOG_DIR}" 2>/dev/null || true)" ]; then
    cp -R "${LOG_DIR}" "${PRE_RESTORE_BACKUP_DIR}/" || warn "备份当前 logs 失败"
  fi
  
  # 保存恢复前备份的元信息
  cat > "${PRE_RESTORE_BACKUP_DIR}/.backup_meta" <<EOF
备份时间: $(date '+%Y-%m-%d %H:%M:%S')
备份类型: 恢复前自动备份
恢复来源: ${TARGET_BACKUP_DIR}
备份内容: jar=${RESTORE_JAR} config=${RESTORE_CONFIG} logs=${RESTORE_LOGS}
备注: 恢复备份 $(basename "${TARGET_BACKUP_DIR}") 前的自动备份
EOF
  
  info "当前状态已备份至: ${PRE_RESTORE_BACKUP_DIR}"
  
  # 开始恢复
  RESTORE_COUNT=0
  
  # 恢复 jar
  if [ "${RESTORE_JAR}" = "true" ]; then
    RESTORE_JAR_FILE=""
    for jar in "${TARGET_BACKUP_DIR}"/*.jar; do
      if [ -f "${jar}" ]; then
        RESTORE_JAR_FILE="${jar}"
        break
      fi
    done
    
    if [ -n "${RESTORE_JAR_FILE}" ]; then
      info "恢复 jar: ${RESTORE_JAR_FILE} -> ${JAR_FILE}"
      cp -f "${RESTORE_JAR_FILE}" "${JAR_FILE}" || die "恢复 jar 失败"
      RESTORE_COUNT=$((RESTORE_COUNT + 1))
    else
      warn "备份中未找到 jar 文件"
    fi
  fi
  
  # 恢复 config
  if [ "${RESTORE_CONFIG}" = "true" ]; then
    if [ -d "${TARGET_BACKUP_DIR}/config" ]; then
      info "恢复 config 目录"
      if [ -d "${BASE_DIR}/config" ]; then
        rm -rf "${BASE_DIR}/config" || warn "删除当前 config 失败"
      fi
      cp -R "${TARGET_BACKUP_DIR}/config" "${BASE_DIR}/" || die "恢复 config 失败"
      RESTORE_COUNT=$((RESTORE_COUNT + 1))
    else
      warn "备份中未找到 config 目录"
    fi
  fi
  
  # 恢复 logs
  if [ "${RESTORE_LOGS}" = "true" ]; then
    if [ -d "${TARGET_BACKUP_DIR}/logs" ]; then
      info "恢复 logs 目录"
      if [ -d "${LOG_DIR}" ]; then
        rm -rf "${LOG_DIR}" || warn "删除当前 logs 失败"
      fi
      cp -R "${TARGET_BACKUP_DIR}/logs" "${BASE_DIR}/" || die "恢复 logs 失败"
      RESTORE_COUNT=$((RESTORE_COUNT + 1))
    else
      warn "备份中未找到 logs 目录"
    fi
  fi
  
  info "恢复完成，共恢复 ${RESTORE_COUNT} 项"
  info "恢复来源: ${TARGET_BACKUP_DIR}"
  info "恢复前备份: ${PRE_RESTORE_BACKUP_DIR}"
  info "提示: 如需回滚，可使用 --restore 恢复恢复前的备份"
  
  exit 0
fi

# 清理旧备份
if [ -n "${CLEAN_DAYS}" ]; then
  if [ ! -d "${BACKUP_BASE_DIR}" ]; then
    info "备份目录不存在，无需清理: ${BACKUP_BASE_DIR}"
    exit 0
  fi
  
  info "清理 ${CLEAN_DAYS} 天之前的备份"
  CLEAN_COUNT=0
  CLEAN_SIZE=0
  
  # 使用 find 查找旧备份（兼容 busybox）
  if command -v find >/dev/null 2>&1; then
    TMP_FILE="${BACKUP_BASE_DIR}/.clean_tmp_$$"
    find "${BACKUP_BASE_DIR}" -maxdepth 1 -type d -mtime +"${CLEAN_DAYS}" > "${TMP_FILE}" 2>/dev/null || true
    while IFS= read -r BACKUP_DIR; do
      if [ -n "${BACKUP_DIR}" ] && [ "${BACKUP_DIR}" != "${BACKUP_BASE_DIR}" ] && [ -d "${BACKUP_DIR}" ]; then
        DIR_SIZE=0
        if command -v du >/dev/null 2>&1; then
          DIR_SIZE="$(du -sk "${BACKUP_DIR}" 2>/dev/null || echo "0 0")"
          DIR_SIZE="$(echo "${DIR_SIZE}" | awk '{print $1}' || echo "0")"
        fi
        info "删除备份: ${BACKUP_DIR} (${DIR_SIZE}KB)"
        rm -rf "${BACKUP_DIR}"
        CLEAN_COUNT=$((CLEAN_COUNT + 1))
        CLEAN_SIZE=$((CLEAN_SIZE + DIR_SIZE))
      fi
    done < "${TMP_FILE}"
    rm -f "${TMP_FILE}"
  fi
  
  if [ "${CLEAN_COUNT}" -eq 0 ]; then
    info "没有需要清理的备份"
  else
    info "清理完成，共删除 ${CLEAN_COUNT} 个备份，释放空间约 ${CLEAN_SIZE}KB"
  fi
  exit 0
fi

# 查看备份详情
if [ -n "${DETAIL_DIR}" ]; then
  DETAIL_DIR="$(resolve_path "${DETAIL_DIR}")"
  if [ ! -d "${DETAIL_DIR}" ]; then
    die "备份目录不存在: ${DETAIL_DIR}"
  fi
  
  META_FILE="${DETAIL_DIR}/.backup_meta"
  if [ -f "${META_FILE}" ]; then
    echo "=========================================="
    echo "备份详情"
    echo "=========================================="
    cat "${META_FILE}"
    echo ""
  else
    warn "未找到备份元信息文件"
  fi
  
  echo "=========================================="
  echo "备份内容"
  echo "=========================================="
  if command -v ls >/dev/null 2>&1; then
    ls -lh "${DETAIL_DIR}" 2>/dev/null || true
  else
    for item in "${DETAIL_DIR}"/*; do
      if [ -e "${item}" ]; then
        echo "${item}"
      fi
    done
  fi
  
  # 显示目录大小
  if command -v du >/dev/null 2>&1; then
    TOTAL_SIZE_RAW="$(du -sh "${DETAIL_DIR}" 2>/dev/null || echo "0 未知")"
    TOTAL_SIZE="$(echo "${TOTAL_SIZE_RAW}" | awk '{print $1}' || echo "未知")"
    echo ""
    echo "总大小: ${TOTAL_SIZE}"
  fi
  
  exit 0
fi

# 查看备份列表
if [ ! -d "${BACKUP_BASE_DIR}" ]; then
  info "备份目录不存在: ${BACKUP_BASE_DIR}"
  exit 0
fi

# 获取所有备份目录并排序
TMP_LIST_FILE="${BACKUP_BASE_DIR}/.list_tmp_$$"
rm -f "${TMP_LIST_FILE}"

if command -v ls >/dev/null 2>&1; then
  ls -1td "${BACKUP_BASE_DIR}"/*/ 2>/dev/null > "${TMP_LIST_FILE}" || true
else
  # 兼容 busybox：手动遍历
  for item in "${BACKUP_BASE_DIR}"/*; do
    if [ -d "${item}" ]; then
      echo "${item}" >> "${TMP_LIST_FILE}"
    fi
  done
  # 简单排序（按目录名，时间戳格式）
  if command -v sort >/dev/null 2>&1; then
    sort -r "${TMP_LIST_FILE}" > "${TMP_LIST_FILE}.sorted"
    mv "${TMP_LIST_FILE}.sorted" "${TMP_LIST_FILE}"
  fi
fi

# 限制数量
if [ -n "${COUNT_LIMIT}" ] && [ "${COUNT_LIMIT}" -gt 0 ]; then
  TMP_HEAD_FILE="${BACKUP_BASE_DIR}/.head_tmp_$$"
  head -n "${COUNT_LIMIT}" "${TMP_LIST_FILE}" > "${TMP_HEAD_FILE}" 2>/dev/null || true
  mv "${TMP_HEAD_FILE}" "${TMP_LIST_FILE}"
fi

if [ ! -s "${TMP_LIST_FILE}" ]; then
  rm -f "${TMP_LIST_FILE}"
  info "暂无备份记录"
  exit 0
fi

TOTAL_COUNT=0
while IFS= read -r line; do
  if [ -n "${line}" ]; then
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
  fi
done < "${TMP_LIST_FILE}"

if [ "${LIST_MODE}" = "true" ]; then
  # 简洁列表模式
  echo "备份目录列表:"
  echo "=========================================="
  COUNT=0
  while IFS= read -r BACKUP_DIR; do
    if [ -n "${BACKUP_DIR}" ] && [ -d "${BACKUP_DIR}" ]; then
      COUNT=$((COUNT + 1))
      BACKUP_NAME="$(basename "${BACKUP_DIR}")"
      META_FILE="${BACKUP_DIR}/.backup_meta"
      if [ -f "${META_FILE}" ]; then
        BACKUP_TIME_LINE="$(grep "^备份时间:" "${META_FILE}" || echo "")"
        if [ -n "${BACKUP_TIME_LINE}" ]; then
          BACKUP_TIME="${BACKUP_TIME_LINE#*:}"
          BACKUP_TIME="$(echo "${BACKUP_TIME}" | sed 's/^[[:space:]]*//' || echo "未知")"
          echo "${COUNT}. ${BACKUP_NAME} - ${BACKUP_TIME}"
        else
          echo "${COUNT}. ${BACKUP_NAME}"
        fi
      else
        echo "${COUNT}. ${BACKUP_NAME}"
      fi
    fi
  done < "${TMP_LIST_FILE}"
else
  # 详细模式
  echo "备份历史:"
  echo "=========================================="
  COUNT=0
  while IFS= read -r BACKUP_DIR; do
    if [ -n "${BACKUP_DIR}" ] && [ -d "${BACKUP_DIR}" ]; then
      COUNT=$((COUNT + 1))
      BACKUP_NAME="$(basename "${BACKUP_DIR}")"
      META_FILE="${BACKUP_DIR}/.backup_meta"
      
      echo ""
      echo "[${COUNT}] ${BACKUP_NAME}"
      echo "----------------------------------------"
      
      if [ -f "${META_FILE}" ]; then
        # 显示元信息（排除备份目录行，避免重复）
        while IFS= read -r meta_line; do
          if [ -n "${meta_line}" ] && echo "${meta_line}" | grep -qv "^备份目录:"; then
            echo "  ${meta_line}"
          fi
        done < "${META_FILE}"
      else
        echo "  (无元信息)"
      fi
      
      # 显示大小
      if command -v du >/dev/null 2>&1; then
        DIR_SIZE_RAW="$(du -sh "${BACKUP_DIR}" 2>/dev/null || echo "0 未知")"
        DIR_SIZE="$(echo "${DIR_SIZE_RAW}" | awk '{print $1}' || echo "未知")"
        echo "  大小: ${DIR_SIZE}"
      fi
    fi
  done < "${TMP_LIST_FILE}"
  
  echo ""
  echo "=========================================="
  echo "共 ${TOTAL_COUNT} 个备份"
  echo ""
  echo "提示: 使用 --detail <备份目录> 查看详细信息"
  echo "提示: 使用 --clean <天数> 清理旧备份"
fi

rm -f "${TMP_LIST_FILE}"

