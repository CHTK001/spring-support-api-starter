#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  backup.sh [-d 目录] [--jar] [--config] [--logs] [--all] [--comment 备注] [--clean 天数]

参数:
  -d 目录         备份目录（可选，默认 <base>/backup/<时间戳>）
  --jar           备份 jar 文件
  --config        备份 config 目录
  --logs          备份 logs 目录
  --all           备份所有内容（jar、config、logs）
  --comment 备注  添加备份备注信息
  --clean 天数    清理指定天数之前的备份（默认保留 30 天）
EOF
}

BACKUP_DIR=""
BACKUP_JAR="false"
BACKUP_CONFIG="false"
BACKUP_LOGS="false"
BACKUP_ALL="false"
BACKUP_COMMENT=""
CLEAN_DAYS=""

while [ $# -gt 0 ]; do
  case "$1" in
    -d)
      BACKUP_DIR="${2:-}"
      shift 2
      ;;
    --jar)
      BACKUP_JAR="true"
      shift
      ;;
    --config)
      BACKUP_CONFIG="true"
      shift
      ;;
    --logs)
      BACKUP_LOGS="true"
      shift
      ;;
    --all)
      BACKUP_ALL="true"
      shift
      ;;
    --comment)
      BACKUP_COMMENT="${2:-}"
      shift 2
      ;;
    --clean)
      CLEAN_DAYS="${2:-30}"
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
JAR_FILE="$(get_jar_file)"
BACKUP_BASE_DIR="$(get_backup_dir)"

# 如果没有指定任何备份项，默认备份所有
if [ "${BACKUP_JAR}" = "false" ] && [ "${BACKUP_CONFIG}" = "false" ] && [ "${BACKUP_LOGS}" = "false" ] && [ "${BACKUP_ALL}" = "false" ]; then
  BACKUP_ALL="true"
fi

# 如果指定了 --all，则全部备份
if [ "${BACKUP_ALL}" = "true" ]; then
  BACKUP_JAR="true"
  BACKUP_CONFIG="true"
  BACKUP_LOGS="true"
fi

# 生成备份目录
TS="$(date +%Y%m%d%H%M%S)"
if [ -z "${BACKUP_DIR}" ]; then
  BACKUP_DIR="${BACKUP_BASE_DIR}/${TS}"
fi

mkdir -p "${BACKUP_DIR}"

info "开始备份: $(get_app_name) $(get_app_version)"
info "备份目录: ${BACKUP_DIR}"

BACKUP_COUNT=0

# 备份 jar
if [ "${BACKUP_JAR}" = "true" ]; then
  if [ -f "${JAR_FILE}" ]; then
    info "备份 jar: ${JAR_FILE}"
    cp -f "${JAR_FILE}" "${BACKUP_DIR}/" || die "备份 jar 失败"
    BACKUP_COUNT=$((BACKUP_COUNT + 1))
  else
    warn "jar 文件不存在，跳过: ${JAR_FILE}"
  fi
fi

# 备份 config
if [ "${BACKUP_CONFIG}" = "true" ]; then
  if [ -d "${BASE_DIR}/config" ]; then
    info "备份 config 目录"
    cp -R "${BASE_DIR}/config" "${BACKUP_DIR}/" || die "备份 config 失败"
    BACKUP_COUNT=$((BACKUP_COUNT + 1))
  else
    warn "config 目录不存在，跳过: ${BASE_DIR}/config"
  fi
fi

# 备份 logs
if [ "${BACKUP_LOGS}" = "true" ]; then
  LOG_DIR="$(get_log_dir)"
  if [ -d "${LOG_DIR}" ] && [ -n "$(ls -A "${LOG_DIR}" 2>/dev/null || true)" ]; then
    info "备份 logs 目录"
    cp -R "${LOG_DIR}" "${BACKUP_DIR}/" || die "备份 logs 失败"
    BACKUP_COUNT=$((BACKUP_COUNT + 1))
  else
    warn "logs 目录不存在或为空，跳过: ${LOG_DIR}"
  fi
fi

# 计算备份文件校验和
calculate_checksums() {
  var_backup_dir="$1"
  var_checksum_file="${var_backup_dir}/.checksums"
  
  info "计算备份文件校验和..."
  rm -f "${var_checksum_file}"
  
  if command -v md5sum >/dev/null 2>&1; then
    find "${var_backup_dir}" -type f ! -name ".checksums" ! -name ".backup_meta" -exec md5sum {} \; >> "${var_checksum_file}" 2>/dev/null || true
    info "已生成 MD5 校验和文件"
  elif command -v sha256sum >/dev/null 2>&1; then
    find "${var_backup_dir}" -type f ! -name ".checksums" ! -name ".backup_meta" -exec sha256sum {} \; >> "${var_checksum_file}" 2>/dev/null || true
    info "已生成 SHA256 校验和文件"
  else
    warn "未找到 md5sum 或 sha256sum 命令，跳过校验和计算"
  fi
}

# 保存备份元信息
META_FILE="${BACKUP_DIR}/.backup_meta"
JAR_FILE_NAME="$(basename "${JAR_FILE}" 2>/dev/null || echo "")"
LOG_DIR="$(get_log_dir)"
cat > "${META_FILE}" <<EOF
备份时间: $(date '+%Y-%m-%d %H:%M:%S')
应用名称: $(get_app_name)
应用版本: $(get_app_version)
Jar文件: ${JAR_FILE_NAME}
应用端口: ${SERVER_PORT:-未设置}
备份目录: ${BACKUP_DIR}
日志目录: ${LOG_DIR}
备份内容: jar=${BACKUP_JAR} config=${BACKUP_CONFIG} logs=${BACKUP_LOGS}
备份数量: ${BACKUP_COUNT}
备注: ${BACKUP_COMMENT:-无}
EOF

# 计算校验和
calculate_checksums "${BACKUP_DIR}"

info "备份完成，共备份 ${BACKUP_COUNT} 项"
info "备份位置: ${BACKUP_DIR}"

# 清理旧备份
if [ -n "${CLEAN_DAYS}" ]; then
  info "清理 ${CLEAN_DAYS} 天之前的备份"
  CLEAN_COUNT=0
  CLEAN_SIZE=0
  
  # 使用 find 查找旧备份（兼容 busybox）
  if command -v find >/dev/null 2>&1; then
    TMP_FILE="${BACKUP_BASE_DIR}/.clean_tmp_$$"
    find "${BACKUP_BASE_DIR}" -maxdepth 1 -type d -mtime +"${CLEAN_DAYS}" > "${TMP_FILE}" 2>/dev/null || true
    while IFS= read -r OLD_BACKUP_DIR; do
      if [ -n "${OLD_BACKUP_DIR}" ] && [ "${OLD_BACKUP_DIR}" != "${BACKUP_BASE_DIR}" ] && [ -d "${OLD_BACKUP_DIR}" ]; then
        DIR_SIZE=0
        if command -v du >/dev/null 2>&1; then
          DIR_SIZE="$(du -sk "${OLD_BACKUP_DIR}" 2>/dev/null || echo "0 0")"
          DIR_SIZE="$(echo "${DIR_SIZE}" | awk '{print $1}' || echo "0")"
        fi
        info "删除备份: ${OLD_BACKUP_DIR} (${DIR_SIZE}KB)"
        rm -rf "${OLD_BACKUP_DIR}"
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
fi

