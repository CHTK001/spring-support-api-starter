#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  upgrade.sh -p 包路径 [--backup] [-b 备份目录] [--no-restart] [--verify] [--no-rollback]

说明:
  - 支持 jar 直接替换
  - 支持 tar.gz / zip（解压后覆盖到当前发布目录）
  - 升级失败时自动回滚到备份（除非使用 --no-rollback）

参数:
  -p 路径         升级包路径（.jar/.tar.gz/.zip）
  --backup        启用备份（不指定 -b 时默认 <base>/backup/<时间戳>）
  -b 目录         备份目录（可选，需配合 --backup）
  --no-restart    升级完成后不自动重启
  --verify        验证升级包完整性（MD5/SHA256，需要提供 .md5 或 .sha256 文件）
  --no-rollback   升级失败时不自动回滚
EOF
}

PACKAGE_PATH=""
BACKUP_DIR=""
BACKUP_ENABLED="false"
NO_RESTART="false"
VERIFY_CHECKSUM="false"
NO_ROLLBACK="false"

while [ $# -gt 0 ]; do
  case "$1" in
    -p)
      PACKAGE_PATH="${2:-}"
      shift 2
      ;;
    --backup)
      BACKUP_ENABLED="true"
      shift
      ;;
    -b)
      BACKUP_DIR="${2:-}"
      shift 2
      ;;
    --no-restart)
      NO_RESTART="true"
      shift
      ;;
    --verify)
      VERIFY_CHECKSUM="true"
      shift
      ;;
    --no-rollback)
      NO_ROLLBACK="true"
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

if [ -z "${PACKAGE_PATH}" ]; then
  usage
  exit 1
fi

PACKAGE_PATH="$(resolve_path "${PACKAGE_PATH}")"

load_env
BASE_DIR="$(get_base_dir)"
JAR_FILE="$(get_jar_file)"

if [ ! -f "${PACKAGE_PATH}" ]; then
  die "升级包不存在: ${PACKAGE_PATH}"
fi

# 验证升级包完整性
verify_package() {
  var_package="$1"
  var_verify="$2"
  
  if [ "${var_verify}" != "true" ]; then
    return 0
  fi
  
  info "验证升级包完整性..."
  
  # 查找校验和文件
  var_md5_file="${var_package}.md5"
  var_sha256_file="${var_package}.sha256"
  
  if [ -f "${var_md5_file}" ]; then
    if command -v md5sum >/dev/null 2>&1; then
      var_expected="$(cat "${var_md5_file}" | awk '{print $1}')"
      var_actual="$(md5sum "${var_package}" | awk '{print $1}')"
      if [ "${var_expected}" != "${var_actual}" ]; then
        die "MD5 校验失败: 期望 ${var_expected}，实际 ${var_actual}"
      fi
      info "MD5 校验通过"
      return 0
    else
      warn "未找到 md5sum 命令，跳过 MD5 校验"
    fi
  fi
  
  if [ -f "${var_sha256_file}" ]; then
    if command -v sha256sum >/dev/null 2>&1; then
      var_expected="$(cat "${var_sha256_file}" | awk '{print $1}')"
      var_actual="$(sha256sum "${var_package}" | awk '{print $1}')"
      if [ "${var_expected}" != "${var_actual}" ]; then
        die "SHA256 校验失败: 期望 ${var_expected}，实际 ${var_actual}"
      fi
      info "SHA256 校验通过"
      return 0
    else
      warn "未找到 sha256sum 命令，跳过 SHA256 校验"
    fi
  fi
  
  warn "未找到校验和文件（.md5 或 .sha256），跳过完整性验证"
}

verify_package "${PACKAGE_PATH}" "${VERIFY_CHECKSUM}"

TS="$(date +%Y%m%d%H%M%S)"
if [ "${BACKUP_ENABLED}" = "true" ] && [ -z "${BACKUP_DIR}" ]; then
  BACKUP_DIR="${BASE_DIR}/backup/${TS}"
fi
if [ "${BACKUP_ENABLED}" = "true" ] && [ -n "${BACKUP_DIR}" ]; then
  mkdir -p "${BACKUP_DIR}"
fi

info "开始升级: $(get_app_name) $(get_app_version)"
info "发布目录: ${BASE_DIR}"
if [ "${BACKUP_ENABLED}" = "true" ]; then
  info "备份目录: ${BACKUP_DIR}"
else
  info "备份: 已关闭"
fi
info "升级包: ${PACKAGE_PATH}"

info "停止应用"
"${BIN_DIR}/stop.sh" || true

# 自动启用备份（用于回滚）
if [ "${NO_ROLLBACK}" != "true" ] && [ "${BACKUP_ENABLED}" != "true" ]; then
  BACKUP_ENABLED="true"
  if [ -z "${BACKUP_DIR}" ]; then
    BACKUP_DIR="${BASE_DIR}/backup/${TS}"
  fi
  info "自动启用备份（用于回滚）"
fi

if [ "${BACKUP_ENABLED}" = "true" ] && [ -n "${BACKUP_DIR}" ]; then
  mkdir -p "${BACKUP_DIR}"
  if [ -f "${JAR_FILE}" ]; then
    info "备份 jar: ${JAR_FILE}"
    cp -f "${JAR_FILE}" "${BACKUP_DIR}/" || die "备份 jar 失败"
  fi
  if [ -d "${BASE_DIR}/config" ]; then
    info "备份 config"
    cp -R "${BASE_DIR}/config" "${BACKUP_DIR}/" || die "备份 config 失败"
  fi
  # 保存备份标记，用于回滚
  echo "${TS}" > "${BACKUP_DIR}/.upgrade_backup"
fi

case "${PACKAGE_PATH}" in
  *.jar)
    info "替换 jar"
    cp -f "${PACKAGE_PATH}" "${JAR_FILE}"
    ;;
  *.tar.gz|*.tgz)
    TMP_DIR="${BASE_DIR}/.upgrade_tmp_${TS}"
    mkdir -p "${TMP_DIR}"
    info "解压 tar.gz 到临时目录: ${TMP_DIR}"
    tar -xzf "${PACKAGE_PATH}" -C "${TMP_DIR}"
    # 默认解压后有一层目录：artifactId-version
    UPGRADE_ROOT="$(find "${TMP_DIR}" -maxdepth 1 -type d | head -n 2 | tail -n 1)"
    if [ -z "${UPGRADE_ROOT}" ] || [ "${UPGRADE_ROOT}" = "${TMP_DIR}" ]; then
      die "解压失败或目录结构异常: ${PACKAGE_PATH}"
    fi
    info "覆盖发布目录（保留原目录结构）"
    cp -R "${UPGRADE_ROOT}/" "${BASE_DIR}/"
    rm -rf "${TMP_DIR}"
    ;;
  *.zip)
    if ! command -v unzip >/dev/null 2>&1; then
      die "缺少 unzip 命令，无法解压: ${PACKAGE_PATH}"
    fi
    TMP_DIR="${BASE_DIR}/.upgrade_tmp_${TS}"
    mkdir -p "${TMP_DIR}"
    info "解压 zip 到临时目录: ${TMP_DIR}"
    unzip -q "${PACKAGE_PATH}" -d "${TMP_DIR}"
    UPGRADE_ROOT="$(find "${TMP_DIR}" -maxdepth 1 -type d | head -n 2 | tail -n 1)"
    if [ -z "${UPGRADE_ROOT}" ] || [ "${UPGRADE_ROOT}" = "${TMP_DIR}" ]; then
      die "解压失败或目录结构异常: ${PACKAGE_PATH}"
    fi
    info "覆盖发布目录（保留原目录结构）"
    cp -R "${UPGRADE_ROOT}/" "${BASE_DIR}/"
    rm -rf "${TMP_DIR}"
    ;;
  *)
    die "不支持的升级包类型: ${PACKAGE_PATH}"
    ;;
esac

info "升级完成"

# 回滚函数
rollback_upgrade() {
  if [ "${NO_ROLLBACK}" = "true" ]; then
    warn "已禁用自动回滚，请手动处理"
    return 1
  fi
  
  if [ ! -d "${BACKUP_DIR}" ] || [ ! -f "${BACKUP_DIR}/.upgrade_backup" ]; then
    warn "备份目录不存在或无效，无法回滚"
    return 1
  fi
  
  warn "升级失败，开始回滚..."
  
  if [ -f "${BACKUP_DIR}/$(basename "${JAR_FILE}")" ]; then
    info "恢复 jar 文件"
    cp -f "${BACKUP_DIR}/$(basename "${JAR_FILE}")" "${JAR_FILE}" || warn "恢复 jar 失败"
  fi
  
  if [ -d "${BACKUP_DIR}/config" ]; then
    info "恢复 config 目录"
    rm -rf "${BASE_DIR}/config"
    cp -R "${BACKUP_DIR}/config" "${BASE_DIR}/" || warn "恢复 config 失败"
  fi
  
  warn "回滚完成"
}

if [ "${NO_RESTART}" = "true" ]; then
  info "已跳过自动重启"
  exit 0
fi

info "启动应用"
if ! "${BIN_DIR}/start.sh"; then
  warn "应用启动失败"
  rollback_upgrade
  exit 1
fi

# 等待应用启动并验证
sleep 3
PID_FILE="$(get_pid_file)"
if [ -f "${PID_FILE}" ]; then
  PID="$(cat "${PID_FILE}" 2>/dev/null || true)"
  if is_running "${PID}"; then
    info "应用启动成功，PID=${PID}"
  else
    warn "应用启动后进程退出"
    rollback_upgrade
    exit 1
  fi
else
  warn "未找到 PID 文件，应用可能启动失败"
  rollback_upgrade
  exit 1
fi


