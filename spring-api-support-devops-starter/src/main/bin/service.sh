#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  service.sh <命令> [选项]

命令:
  mount|install     挂载 systemd 服务
  unmount|uninstall 卸载 systemd 服务
  start             启动服务
  stop              停止服务
  restart           重启服务
  status            查看服务状态
  enable            设置开机自启
  disable           取消开机自启

选项:
  --name 名称          服务名（默认: APP_NAME）
  --desc 描述          服务描述（默认: APP_NAME Service）
  --user 用户          以指定用户运行（默认当前用户）
  --group 用户组       以指定用户组运行（可选）
  --workdir 目录       工作目录（默认发布目录）
  --java-home 路径     写入 JAVA_HOME 环境变量（可选）
  --stop-timeout 秒    停止超时时间，默认 30 秒
  --start             mount/install 后立即启动服务
  -h, --help          显示帮助

示例:
  service.sh mount --name api-monitor --user app --start
  service.sh unmount --name api-monitor
  service.sh status --name api-monitor
EOF
}

ensure_root() {
  if ! command -v id >/dev/null 2>&1; then
    die "当前系统缺少 id 命令，无法判断权限"
  fi
  if [ "$(id -u)" -ne 0 ]; then
    die "该操作需要 root 权限，请使用 root 或 sudo 执行"
  fi
}

is_systemd_available() {
  if ! command -v systemctl >/dev/null 2>&1; then
    return 1
  fi
  if [ ! -d "/run/systemd/system" ]; then
    return 1
  fi
  return 0
}

require_systemd() {
  if ! is_systemd_available; then
    die "当前环境不支持 systemd，无法挂载/卸载服务"
  fi
}

systemctl_service() {
  var_action="${1:-status}"
  shift || true
  systemctl "${var_action}" "$@" "${SERVICE_NAME}.service"
}

write_service_file() {
  var_service_file="${1:-}"
  if [ -z "${var_service_file}" ]; then
    return 1
  fi

  cat > "${var_service_file}" <<EOF
[Unit]
Description=${SERVICE_DESC}
After=network.target

[Service]
Type=forking
WorkingDirectory=${WORK_DIR}
PIDFile=${PID_FILE}
ExecStart=${BIN_DIR}/start.sh
ExecStop=${BIN_DIR}/stop.sh -t ${STOP_TIMEOUT}
ExecReload=${BIN_DIR}/restart.sh -t ${STOP_TIMEOUT}
Restart=on-failure
RestartSec=5
SuccessExitStatus=143
LimitNOFILE=65535
Environment=APP_NAME=${APP_NAME}
Environment=APP_VERSION=${APP_VERSION}
${JAVA_HOME_LINE}
User=${SERVICE_USER}
${GROUP_LINE}

[Install]
WantedBy=multi-user.target
EOF
}

mount_service() {
  ensure_root
  require_systemd

  SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"
  write_service_file "${SERVICE_FILE}"
  chmod 0644 "${SERVICE_FILE}"

  systemctl daemon-reload
  systemctl_service enable

  info "服务挂载成功: ${SERVICE_NAME}.service"
  info "服务文件: ${SERVICE_FILE}"

  if [ "${AUTO_START}" = "true" ]; then
    systemctl_service start
    info "服务已启动: ${SERVICE_NAME}.service"
  fi
}

unmount_service() {
  ensure_root
  require_systemd

  SERVICE_FILE="/etc/systemd/system/${SERVICE_NAME}.service"

  if systemctl_service is-active --quiet >/dev/null 2>&1; then
    systemctl_service stop || true
  fi

  if systemctl_service is-enabled --quiet >/dev/null 2>&1; then
    systemctl_service disable || true
  fi

  if [ -f "${SERVICE_FILE}" ]; then
    rm -f "${SERVICE_FILE}"
  fi

  systemctl daemon-reload
  systemctl reset-failed "${SERVICE_NAME}.service" >/dev/null 2>&1 || true
  info "服务已卸载: ${SERVICE_NAME}.service"
}

ACTION="${1:-status}"
if [ $# -gt 0 ]; then
  shift
fi

ensure_env_file
load_env

APP_NAME="$(get_app_name)"
APP_VERSION="$(get_app_version)"
BASE_DIR="$(get_base_dir)"
PID_FILE="$(get_pid_file)"

SERVICE_NAME="${APP_NAME}"
SERVICE_DESC="${APP_NAME} Service"
SERVICE_USER="$(id -un 2>/dev/null || echo "root")"
SERVICE_GROUP=""
WORK_DIR="${BASE_DIR}"
JAVA_HOME_VALUE="${JAVA_HOME:-}"
STOP_TIMEOUT="30"
AUTO_START="false"

while [ $# -gt 0 ]; do
  case "$1" in
    --name)
      if [ $# -lt 2 ]; then
        die "--name 需要指定服务名"
      fi
      SERVICE_NAME="${2:-}"
      shift 2
      ;;
    --desc)
      if [ $# -lt 2 ]; then
        die "--desc 需要指定服务描述"
      fi
      SERVICE_DESC="${2:-}"
      shift 2
      ;;
    --user)
      if [ $# -lt 2 ]; then
        die "--user 需要指定用户名"
      fi
      SERVICE_USER="${2:-}"
      shift 2
      ;;
    --group)
      if [ $# -lt 2 ]; then
        die "--group 需要指定用户组"
      fi
      SERVICE_GROUP="${2:-}"
      shift 2
      ;;
    --workdir)
      if [ $# -lt 2 ]; then
        die "--workdir 需要指定目录"
      fi
      WORK_DIR="${2:-}"
      shift 2
      ;;
    --java-home)
      if [ $# -lt 2 ]; then
        die "--java-home 需要指定路径"
      fi
      JAVA_HOME_VALUE="${2:-}"
      shift 2
      ;;
    --stop-timeout)
      if [ $# -lt 2 ]; then
        die "--stop-timeout 需要指定秒数"
      fi
      STOP_TIMEOUT="${2:-30}"
      if ! echo "${STOP_TIMEOUT}" | grep -qE '^[0-9]+$'; then
        die "无效的 stop-timeout: ${STOP_TIMEOUT}"
      fi
      shift 2
      ;;
    --start)
      AUTO_START="true"
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

if [ -z "${SERVICE_NAME}" ]; then
  die "服务名不能为空"
fi

if [ ! -d "${WORK_DIR}" ]; then
  die "工作目录不存在: ${WORK_DIR}"
fi

JAVA_HOME_LINE="# JAVA_HOME 未设置"
if [ -n "${JAVA_HOME_VALUE}" ]; then
  JAVA_HOME_LINE="Environment=JAVA_HOME=${JAVA_HOME_VALUE}"
fi

GROUP_LINE="# Group 未设置"
if [ -n "${SERVICE_GROUP}" ]; then
  GROUP_LINE="Group=${SERVICE_GROUP}"
fi

case "${ACTION}" in
  mount|install)
    mount_service
    ;;
  unmount|uninstall)
    unmount_service
    ;;
  start|stop|restart|status|enable|disable)
    if ! is_systemd_available; then
      case "${ACTION}" in
        start|stop|restart|status)
          info "当前环境无 systemd，回退到本地脚本模式: ${ACTION}.sh"
          "${BIN_DIR}/${ACTION}.sh"
          exit $?
          ;;
        *)
          die "当前环境无 systemd，不支持 ${ACTION}"
          ;;
      esac
    fi
    systemctl_service "${ACTION}"
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    warn "未知命令: ${ACTION}"
    usage
    exit 1
    ;;
esac
