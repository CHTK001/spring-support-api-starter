#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  rollback.sh -p 包路径 [--backup] [-b 备份目录] [--no-restart]

说明:
  - 回滚到指定版本的包
  - 支持 jar 直接替换
  - 支持 tar.gz / zip（解压后覆盖到当前发布目录）

参数:
  -p 路径         回滚包路径（.jar/.tar.gz/.zip）
  --backup        启用备份（不指定 -b 时默认 <base>/backup/<时间戳>）
  -b 目录         备份目录（可选，需配合 --backup）
  --no-restart    回滚完成后不自动重启
EOF
}

PACKAGE_PATH=""
BACKUP_DIR=""
BACKUP_ENABLED="false"
NO_RESTART="false"

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
  die "回滚包不存在: ${PACKAGE_PATH}"
fi

TS="$(date +%Y%m%d%H%M%S)"
if [ "${BACKUP_ENABLED}" = "true" ] && [ -z "${BACKUP_DIR}" ]; then
  BACKUP_DIR="${BASE_DIR}/backup/${TS}"
fi
if [ "${BACKUP_ENABLED}" = "true" ] && [ -n "${BACKUP_DIR}" ]; then
  mkdir -p "${BACKUP_DIR}"
fi

info "开始回滚: $(get_app_name) $(get_app_version)"
info "发布目录: ${BASE_DIR}"
if [ "${BACKUP_ENABLED}" = "true" ]; then
  info "备份目录: ${BACKUP_DIR}"
else
  info "备份: 已关闭"
fi
info "回滚包: ${PACKAGE_PATH}"

info "停止应用"
"${BIN_DIR}/stop.sh" || true

if [ "${BACKUP_ENABLED}" = "true" ]; then
  if [ -f "${JAR_FILE}" ]; then
    info "备份当前 jar: ${JAR_FILE}"
    cp -f "${JAR_FILE}" "${BACKUP_DIR}/" || true
  fi
  if [ -d "${BASE_DIR}/config" ]; then
    info "备份 config"
    cp -R "${BASE_DIR}/config" "${BACKUP_DIR}/" || true
  fi
fi

case "${PACKAGE_PATH}" in
  *.jar)
    info "替换 jar"
    cp -f "${PACKAGE_PATH}" "${JAR_FILE}"
    ;;
  *.tar.gz|*.tgz)
    TMP_DIR="${BASE_DIR}/.rollback_tmp_${TS}"
    mkdir -p "${TMP_DIR}"
    info "解压 tar.gz 到临时目录: ${TMP_DIR}"
    tar -xzf "${PACKAGE_PATH}" -C "${TMP_DIR}"
    # 默认解压后有一层目录：artifactId-version
    ROLLBACK_ROOT="$(find "${TMP_DIR}" -maxdepth 1 -type d | head -n 2 | tail -n 1)"
    if [ -z "${ROLLBACK_ROOT}" ] || [ "${ROLLBACK_ROOT}" = "${TMP_DIR}" ]; then
      die "解压失败或目录结构异常: ${PACKAGE_PATH}"
    fi
    info "覆盖发布目录（保留原目录结构）"
    cp -R "${ROLLBACK_ROOT}/" "${BASE_DIR}/"
    rm -rf "${TMP_DIR}"
    ;;
  *.zip)
    if ! command -v unzip >/dev/null 2>&1; then
      die "缺少 unzip 命令，无法解压: ${PACKAGE_PATH}"
    fi
    TMP_DIR="${BASE_DIR}/.rollback_tmp_${TS}"
    mkdir -p "${TMP_DIR}"
    info "解压 zip 到临时目录: ${TMP_DIR}"
    unzip -q "${PACKAGE_PATH}" -d "${TMP_DIR}"
    ROLLBACK_ROOT="$(find "${TMP_DIR}" -maxdepth 1 -type d | head -n 2 | tail -n 1)"
    if [ -z "${ROLLBACK_ROOT}" ] || [ "${ROLLBACK_ROOT}" = "${TMP_DIR}" ]; then
      die "解压失败或目录结构异常: ${PACKAGE_PATH}"
    fi
    info "覆盖发布目录（保留原目录结构）"
    cp -R "${ROLLBACK_ROOT}/" "${BASE_DIR}/"
    rm -rf "${TMP_DIR}"
    ;;
  *)
    die "不支持的回滚包类型: ${PACKAGE_PATH}"
    ;;
esac

info "回滚完成"
if [ "${NO_RESTART}" = "true" ]; then
  info "已跳过自动重启"
  exit 0
fi

info "启动应用"
"${BIN_DIR}/start.sh"


