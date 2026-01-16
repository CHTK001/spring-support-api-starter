#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  arthas.sh <命令> [选项]

命令:
  download [版本]    下载指定版本的 Arthas
  install [版本|路径] 安装指定版本或本地包
  list               列出已安装的版本
  list --available   列出可用版本
  status             显示 Arthas 状态和版本信息
  config [key] [value] 修改配置项
  config --show      显示当前配置
  config --reset     重置为默认配置
  uninstall [版本]   卸载指定版本
  switch [版本]      切换到指定版本
  enable             启用 Arthas（设置 ARTHAS_ENABLED=true）
  disable            禁用 Arthas（设置 ARTHAS_ENABLED=false）
  attach [PID]       附加到指定进程（需要进程正在运行）

选项:
  --force            强制操作（覆盖已存在的版本）
  --dir 目录         指定安装目录（默认 <base>/agents/arthas）

示例:
  arthas.sh download latest
  arthas.sh install latest
  arthas.sh list
  arthas.sh status
  arthas.sh config ARTHAS_TELNET_PORT 3658
  arthas.sh config --show
  arthas.sh switch latest
  arthas.sh enable
  arthas.sh attach 12345
EOF
}

get_arthas_dir() {
  var_base_dir="$(get_base_dir)"
  if [ -n "${ARTHAS_DIR:-}" ]; then
    echo "${ARTHAS_DIR}"
  else
    echo "${ARTHAS_HOME:-${var_base_dir}/agents/arthas}"
  fi
}

get_installed_version() {
  var_home="$(get_arthas_dir)"
  var_version_file="${var_home}/.version"
  if [ -f "${var_version_file}" ]; then
    cat "${var_version_file}" 2>/dev/null || echo ""
  else
    echo ""
  fi
}

set_installed_version() {
  var_version="$1"
  var_home="$(get_arthas_dir)"
  mkdir -p "${var_home}"
  echo "${var_version}" > "${var_home}/.version"
}

download_arthas() {
  var_version="${1:-latest}"
  var_force="${2:-false}"
  var_home="$(get_arthas_dir)"
  mkdir -p "${var_home}"

  if [ "${var_version}" = "latest" ]; then
    if [ -z "${ARTHAS_BOOT_URL:-}" ]; then
      var_url="https://arthas.aliyun.com/download/arthas-boot.jar"
    else
      var_url="${ARTHAS_BOOT_URL}"
    fi
  else
    var_repo_url="${ARTHAS_REPO_URL:-https://github.com/alibaba/arthas/releases}"
    var_url="${var_repo_url}/download/arthas-all-${var_version}.jar"
    # 注意：实际下载地址可能需要调整
    warn "指定版本下载功能可能需要调整 URL，当前使用: ${var_url}"
  fi

  var_boot_jar="${var_home}/arthas-boot.jar"
  var_version_dir="${var_home}/versions/${var_version}"
  var_version_jar="${var_version_dir}/arthas-boot.jar"

  if [ -f "${var_version_jar}" ] && [ "${var_force}" != "true" ]; then
    info "版本 ${var_version} 已存在，跳过下载（使用 --force 强制重新下载）"
    return 0
  fi

  info "下载 Arthas ${var_version}: ${var_url}"
  mkdir -p "${var_version_dir}"
  download_file "${var_url}" "${var_version_jar}"
  chmod +x "${var_version_jar}"

  set_installed_version "${var_version}"
  info "下载完成: ${var_version}"
}

install_arthas() {
  var_source="${1:-}"
  var_force="${2:-false}"
  var_home="$(get_arthas_dir)"

  if [ -z "${var_source}" ]; then
    var_installed="$(get_installed_version)"
    if [ -n "${var_installed}" ]; then
      var_source="${var_installed}"
    else
      var_source="latest"
    fi
  fi

  # 如果是文件路径
  if [ -f "${var_source}" ]; then
    info "从本地文件安装: ${var_source}"
    var_version="custom-$(date +%Y%m%d%H%M%S)"
    var_version_dir="${var_home}/versions/${var_version}"
    mkdir -p "${var_version_dir}"
    cp "${var_source}" "${var_version_dir}/arthas-boot.jar"
    chmod +x "${var_version_dir}/arthas-boot.jar"
    set_installed_version "${var_version}"
    switch_version "${var_version}"
    info "安装完成: ${var_version}"
    return 0
  fi

  # 如果是版本号，先下载再安装
  if [ ! -f "${var_home}/versions/${var_source}/arthas-boot.jar" ]; then
    download_arthas "${var_source}" "${var_force}"
  fi

  # 切换到该版本
  switch_version "${var_source}"
}

list_installed() {
  var_home="$(get_arthas_dir)"
  var_current="$(get_installed_version)"

  if [ ! -d "${var_home}/versions" ]; then
    info "未安装任何版本"
    return 0
  fi

  echo "已安装的版本:"
  for var_version_dir in "${var_home}/versions"/*; do
    if [ -d "${var_version_dir}" ]; then
      var_version="$(basename "${var_version_dir}")"
      var_marker=""
      if [ "${var_version}" = "${var_current}" ]; then
        var_marker=" (当前)"
      fi
      var_boot_jar="${var_version_dir}/arthas-boot.jar"
      if [ -f "${var_boot_jar}" ]; then
        var_size="$(du -sh "${var_version_dir}" 2>/dev/null | awk '{print $1}' || echo "未知")"
        echo "  ${var_version}${var_marker} - ${var_size}"
      fi
    fi
  done
}

list_available() {
  warn "列出可用版本功能需要访问 GitHub API，当前未实现"
  info "请访问 https://github.com/alibaba/arthas/releases 查看可用版本"
  info "或使用 'arthas.sh download latest' 下载最新版本"
}

show_status() {
  var_home="$(get_arthas_dir)"
  var_current="$(get_installed_version)"
  var_enabled="${ARTHAS_ENABLED:-false}"

  echo "=========================================="
  echo "Arthas 状态"
  echo "=========================================="
  echo "启用状态: ${var_enabled}"
  echo "安装目录: ${var_home}"

  if [ -n "${var_current}" ]; then
    var_boot_jar="${var_home}/arthas-boot.jar"
    if [ -f "${var_boot_jar}" ]; then
      echo "当前版本: ${var_current}"
      var_size="$(du -sh "${var_home}" 2>/dev/null | awk '{print $1}' || echo "未知")"
      echo "安装大小: ${var_size}"
    else
      echo "当前版本: ${var_current} (文件不存在)"
    fi
  else
    echo "当前版本: 未安装"
  fi

  echo ""
  echo "配置信息:"
  echo "  ARTHAS_TELNET_PORT: ${ARTHAS_TELNET_PORT:-3658}"
  echo "  ARTHAS_HTTP_PORT: ${ARTHAS_HTTP_PORT:-8563}"
  echo "=========================================="
}

config_arthas() {
  var_key="${1:-}"
  var_value="${2:-}"

  if [ -z "${var_key}" ]; then
    show_config
    return 0
  fi

  if [ "${var_key}" = "--show" ]; then
    show_config
    return 0
  fi

  if [ "${var_key}" = "--reset" ]; then
    reset_config
    return 0
  fi

  if [ -z "${var_value}" ]; then
    die "请提供配置值: arthas.sh config ${var_key} <值>"
  fi

  set_env_var "${var_key}" "${var_value}"
  info "已设置: ${var_key}=${var_value}"
}

show_config() {
  var_base_dir="$(get_base_dir)"
  var_env_file="${var_base_dir}/bin/app.env"

  echo "Arthas 配置:"
  if [ -f "${var_env_file}" ]; then
    grep -E "^ARTHAS_" "${var_env_file}" 2>/dev/null || echo "  无配置项"
  else
    echo "  配置文件不存在"
  fi
}

reset_config() {
  var_base_dir="$(get_base_dir)"
  var_env_file="${var_base_dir}/bin/app.env"

  if [ -f "${var_env_file}" ]; then
    var_tmp_file="${var_env_file}.tmp.$$"
    grep -vE "^ARTHAS_" "${var_env_file}" > "${var_tmp_file}" 2>/dev/null || true
    mv "${var_tmp_file}" "${var_env_file}"
    info "已重置 Arthas 配置"
  fi
}

uninstall_version() {
  var_version="${1:-}"
  if [ -z "${var_version}" ]; then
    die "请指定要卸载的版本"
  fi

  var_home="$(get_arthas_dir)"
  var_version_dir="${var_home}/versions/${var_version}"
  var_current="$(get_installed_version)"

  if [ ! -d "${var_version_dir}" ]; then
    warn "版本 ${var_version} 不存在"
    return 1
  fi

  if [ "${var_version}" = "${var_current}" ]; then
    warn "无法卸载当前使用的版本，请先切换到其他版本"
    return 1
  fi

  info "卸载版本: ${var_version}"
  rm -rf "${var_version_dir}"
  info "卸载完成: ${var_version}"
}

switch_version() {
  var_version="${1:-}"
  if [ -z "${var_version}" ]; then
    die "请指定要切换的版本"
  fi

  var_home="$(get_arthas_dir)"
  var_version_dir="${var_home}/versions/${var_version}"
  var_boot_jar="${var_version_dir}/arthas-boot.jar"

  if [ ! -f "${var_boot_jar}" ]; then
    die "版本 ${var_version} 未安装，请先安装: arthas.sh install ${var_version}"
  fi

  # 复制到当前目录
  cp -f "${var_boot_jar}" "${var_home}/arthas-boot.jar"
  chmod +x "${var_home}/arthas-boot.jar"

  set_installed_version "${var_version}"
  set_env_var "ARTHAS_HOME" "${var_home}"
  info "已切换到版本: ${var_version}"
}

enable_arthas() {
  set_env_var "ARTHAS_ENABLED" "true"
  info "已启用 Arthas"
}

disable_arthas() {
  set_env_var "ARTHAS_ENABLED" "false"
  info "已禁用 Arthas"
}

attach_arthas() {
  var_pid="${1:-}"
  if [ -z "${var_pid}" ]; then
    # 尝试从 PID 文件获取
    var_pid_file="$(get_pid_file)"
    if [ -f "${var_pid_file}" ]; then
      var_pid="$(cat "${var_pid_file}" 2>/dev/null || true)"
    fi
  fi

  if [ -z "${var_pid}" ]; then
    die "请指定进程 PID 或确保应用正在运行"
  fi

  if ! is_running "${var_pid}"; then
    die "进程 ${var_pid} 不存在或已停止"
  fi

  var_home="$(get_arthas_dir)"
  var_boot_jar="${var_home}/arthas-boot.jar"

  if [ ! -f "${var_boot_jar}" ]; then
    warn "未找到 arthas-boot.jar，尝试自动安装"
    install_arthas "latest" "false"
    var_boot_jar="${var_home}/arthas-boot.jar"
  fi

  var_telnet="${ARTHAS_TELNET_PORT:-3658}"
  var_http="${ARTHAS_HTTP_PORT:-8563}"

  info "附加 Arthas 到进程 ${var_pid}，telnet=${var_telnet}，http=${var_http}"

  JAVA_BIN=""
  if [ -n "${JAVA_HOME:-}" ]; then
    JAVA_BIN="${JAVA_HOME}/bin/java"
  elif command -v java >/dev/null 2>&1; then
    JAVA_BIN="$(command -v java)"
  fi

  if [ -z "${JAVA_BIN}" ] || [ ! -x "${JAVA_BIN}" ]; then
    die "未找到 java 命令，请设置 JAVA_HOME"
  fi

  "${JAVA_BIN}" -jar "${var_boot_jar}" "${var_pid}" \
    --telnet-port "${var_telnet}" \
    --http-port "${var_http}" \
    || warn "Arthas 附加失败"
}

# 解析参数
COMMAND="${1:-}"
shift || true

FORCE_FLAG="false"
ARTHAS_DIR_ARG=""

while [ $# -gt 0 ]; do
  case "$1" in
    --force)
      FORCE_FLAG="true"
      shift
      ;;
    --dir)
      ARTHAS_DIR_ARG="${2:-}"
      shift 2
      ;;
    *)
      break
      ;;
  esac
done

if [ -n "${ARTHAS_DIR_ARG}" ]; then
  ARTHAS_DIR="${ARTHAS_DIR_ARG}"
  export ARTHAS_DIR
fi

load_env

case "${COMMAND}" in
  download)
    download_arthas "${1:-latest}" "${FORCE_FLAG}"
    ;;
  install)
    install_arthas "${1:-}" "${FORCE_FLAG}"
    ;;
  list)
    if [ "${1:-}" = "--available" ]; then
      list_available
    else
      list_installed
    fi
    ;;
  status)
    show_status
    ;;
  config)
    config_arthas "${1:-}" "${2:-}"
    ;;
  uninstall)
    uninstall_version "${1:-}"
    ;;
  switch)
    switch_version "${1:-}"
    ;;
  enable)
    enable_arthas
    ;;
  disable)
    disable_arthas
    ;;
  attach)
    attach_arthas "${1:-}"
    ;;
  -h|--help|"")
    usage
    exit 0
    ;;
  *)
    warn "未知命令: ${COMMAND}"
    usage
    exit 1
    ;;
esac

