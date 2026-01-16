#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  skywalking.sh <命令> [选项]

命令:
  download [版本]    下载指定版本的 SkyWalking 探针
  install [版本|路径] 安装指定版本或本地包
  list               列出已安装的版本
  list --available   列出可用版本（需要配置 SKYWALKING_REPO_URL）
  status             显示探针状态和版本信息
  config [key] [value] 修改配置项
  config --show      显示当前配置
  config --reset     重置为默认配置
  uninstall [版本]   卸载指定版本
  switch [版本]      切换到指定版本
  enable             启用 SkyWalking（设置 SKYWALKING_ENABLED=true）
  disable            禁用 SkyWalking（设置 SKYWALKING_ENABLED=false）

选项:
  --force            强制操作（覆盖已存在的版本）
  --dir 目录         指定安装目录（默认 <base>/agents/skywalking）

示例:
  skywalking.sh download 9.0.0
  skywalking.sh install 9.0.0
  skywalking.sh list
  skywalking.sh status
  skywalking.sh config SW_AGENT_NAME my-app
  skywalking.sh config --show
  skywalking.sh switch 9.0.0
  skywalking.sh enable
EOF
}

get_skywalking_dir() {
  var_base_dir="$(get_base_dir)"
  if [ -n "${SKYWALKING_DIR:-}" ]; then
    echo "${SKYWALKING_DIR}"
  else
    echo "${SKYWALKING_AGENT_DIR:-${var_base_dir}/agents/skywalking}"
  fi
}

get_installed_version() {
  var_agent_dir="$(get_skywalking_dir)"
  var_version_file="${var_agent_dir}/.version"
  if [ -f "${var_version_file}" ]; then
    cat "${var_version_file}" 2>/dev/null || echo ""
  else
    echo ""
  fi
}

set_installed_version() {
  var_version="$1"
  var_agent_dir="$(get_skywalking_dir)"
  mkdir -p "${var_agent_dir}"
  echo "${var_version}" > "${var_agent_dir}/.version"
}

download_skywalking() {
  var_version="${1:-latest}"
  var_force="${2:-false}"
  var_agent_dir="$(get_skywalking_dir)"
  mkdir -p "${var_agent_dir}"

  if [ "${var_version}" = "latest" ]; then
    if [ -z "${SKYWALKING_DIST_URL:-}" ]; then
      die "未配置 SKYWALKING_DIST_URL，无法下载最新版本"
    fi
    var_url="${SKYWALKING_DIST_URL}"
  else
    var_repo_url="${SKYWALKING_REPO_URL:-https://archive.apache.org/dist/skywalking}"
    var_url="${var_repo_url}/${var_version}/apache-skywalking-java-agent-${var_version}.tgz"
  fi

  var_tmp_file="${var_agent_dir}/skywalking-agent-${var_version}.tgz"
  var_extract_dir="${var_agent_dir}/versions/${var_version}"

  if [ -d "${var_extract_dir}" ] && [ "${var_force}" != "true" ]; then
    info "版本 ${var_version} 已存在，跳过下载（使用 --force 强制重新下载）"
    return 0
  fi

  info "下载 SkyWalking ${var_version}: ${var_url}"
  download_file "${var_url}" "${var_tmp_file}"

  info "解压到: ${var_extract_dir}"
  mkdir -p "${var_extract_dir}"
  tar -xzf "${var_tmp_file}" -C "${var_extract_dir}"

  # 查找解压后的目录（通常是一层目录结构）
  var_agent_jar=""
  for var_file in "${var_extract_dir}"/*/skywalking-agent.jar; do
    if [ -f "${var_file}" ]; then
      var_agent_jar="${var_file}"
      break
    fi
  done

  if [ -z "${var_agent_jar}" ] || [ ! -f "${var_agent_jar}" ]; then
    die "解压后未找到 skywalking-agent.jar"
  fi

  # 移动文件到版本目录
  var_agent_dir_in_version="$(dirname "${var_agent_jar}")"
  if [ "${var_agent_dir_in_version}" != "${var_extract_dir}" ]; then
    mv "${var_agent_dir_in_version}" "${var_extract_dir}/agent"
  fi

  rm -f "${var_tmp_file}"
  set_installed_version "${var_version}"
  info "下载完成: ${var_version}"
}

install_skywalking() {
  var_source="${1:-}"
  var_force="${2:-false}"
  var_agent_dir="$(get_skywalking_dir)"

  if [ -z "${var_source}" ]; then
    var_installed="$(get_installed_version)"
    if [ -n "${var_installed}" ]; then
      var_source="${var_installed}"
    else
      die "请指定要安装的版本或路径"
    fi
  fi

  # 如果是文件路径
  if [ -f "${var_source}" ]; then
    info "从本地文件安装: ${var_source}"
    var_tmp_dir="${var_agent_dir}/.tmp_install"
    rm -rf "${var_tmp_dir}"
    mkdir -p "${var_tmp_dir}"
    tar -xzf "${var_source}" -C "${var_tmp_dir}"

    var_agent_jar=""
    for var_file in "${var_tmp_dir}"/*/skywalking-agent.jar; do
      if [ -f "${var_file}" ]; then
        var_agent_jar="${var_file}"
        break
      fi
    done

    if [ -z "${var_agent_jar}" ] || [ ! -f "${var_agent_jar}" ]; then
      rm -rf "${var_tmp_dir}"
      die "解压后未找到 skywalking-agent.jar"
    fi

    var_version="$(basename "${var_source}" | sed 's/.*-\([0-9.]*\)\.tgz/\1/' || echo "custom")"
    var_version_dir="${var_agent_dir}/versions/${var_version}"
    mkdir -p "${var_version_dir}"
    var_agent_dir_in_tmp="$(dirname "${var_agent_jar}")"
    mv "${var_agent_dir_in_tmp}" "${var_version_dir}/agent"
    rm -rf "${var_tmp_dir}"
    set_installed_version "${var_version}"
    info "安装完成: ${var_version}"
    return 0
  fi

  # 如果是版本号，先下载再安装
  if [ ! -d "${var_agent_dir}/versions/${var_source}" ]; then
    download_skywalking "${var_source}" "${var_force}"
  fi

  # 切换到该版本
  switch_version "${var_source}"
}

list_installed() {
  var_agent_dir="$(get_skywalking_dir)"
  var_current="$(get_installed_version)"

  if [ ! -d "${var_agent_dir}/versions" ]; then
    info "未安装任何版本"
    return 0
  fi

  echo "已安装的版本:"
  for var_version_dir in "${var_agent_dir}/versions"/*; do
    if [ -d "${var_version_dir}" ]; then
      var_version="$(basename "${var_version_dir}")"
      var_marker=""
      if [ "${var_version}" = "${var_current}" ]; then
        var_marker=" (当前)"
      fi
      var_agent_jar="${var_version_dir}/agent/skywalking-agent.jar"
      if [ -f "${var_agent_jar}" ]; then
        var_size="$(du -sh "${var_version_dir}" 2>/dev/null | awk '{print $1}' || echo "未知")"
        echo "  ${var_version}${var_marker} - ${var_size}"
      fi
    fi
  done
}

list_available() {
  warn "列出可用版本功能需要配置 SKYWALKING_REPO_URL，当前未实现"
  info "请访问 https://archive.apache.org/dist/skywalking/ 查看可用版本"
}

show_status() {
  var_agent_dir="$(get_skywalking_dir)"
  var_current="$(get_installed_version)"
  var_enabled="${SKYWALKING_ENABLED:-false}"

  echo "=========================================="
  echo "SkyWalking 探针状态"
  echo "=========================================="
  echo "启用状态: ${var_enabled}"
  echo "安装目录: ${var_agent_dir}"

  if [ -n "${var_current}" ]; then
    var_agent_jar="${var_agent_dir}/agent/skywalking-agent.jar"
    if [ -f "${var_agent_jar}" ]; then
      echo "当前版本: ${var_current}"
      var_size="$(du -sh "${var_agent_dir}/agent" 2>/dev/null | awk '{print $1}' || echo "未知")"
      echo "安装大小: ${var_size}"
    else
      echo "当前版本: ${var_current} (文件不存在)"
    fi
  else
    echo "当前版本: 未安装"
  fi

  echo ""
  echo "配置信息:"
  echo "  SW_AGENT_NAME: ${SW_AGENT_NAME:-未设置}"
  echo "  SW_AGENT_COLLECTOR_BACKEND_SERVICES: ${SW_AGENT_COLLECTOR_BACKEND_SERVICES:-未设置}"
  echo "  SW_AGENT_INSTANCE_NAME: ${SW_AGENT_INSTANCE_NAME:-未设置}"
  echo "=========================================="
}

config_skywalking() {
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
    die "请提供配置值: skywalking.sh config ${var_key} <值>"
  fi

  set_env_var "${var_key}" "${var_value}"
  info "已设置: ${var_key}=${var_value}"
}

show_config() {
  var_base_dir="$(get_base_dir)"
  var_env_file="${var_base_dir}/bin/app.env"

  echo "SkyWalking 配置:"
  if [ -f "${var_env_file}" ]; then
    grep -E "^SW_AGENT_|^SKYWALKING_" "${var_env_file}" 2>/dev/null || echo "  无配置项"
  else
    echo "  配置文件不存在"
  fi
}

reset_config() {
  var_base_dir="$(get_base_dir)"
  var_env_file="${var_base_dir}/bin/app.env"

  if [ -f "${var_env_file}" ]; then
    # 删除 SkyWalking 相关配置
    var_tmp_file="${var_env_file}.tmp.$$"
    grep -vE "^SW_AGENT_|^SKYWALKING_" "${var_env_file}" > "${var_tmp_file}" 2>/dev/null || true
    mv "${var_tmp_file}" "${var_env_file}"
    info "已重置 SkyWalking 配置"
  fi
}

uninstall_version() {
  var_version="${1:-}"
  if [ -z "${var_version}" ]; then
    die "请指定要卸载的版本"
  fi

  var_agent_dir="$(get_skywalking_dir)"
  var_version_dir="${var_agent_dir}/versions/${var_version}"
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

  var_agent_dir="$(get_skywalking_dir)"
  var_version_dir="${var_agent_dir}/versions/${var_version}"
  var_agent_jar="${var_version_dir}/agent/skywalking-agent.jar"

  if [ ! -f "${var_agent_jar}" ]; then
    die "版本 ${var_version} 未安装，请先安装: skywalking.sh install ${var_version}"
  fi

  # 创建当前版本的符号链接或复制
  var_current_agent_dir="${var_agent_dir}/agent"
  rm -rf "${var_current_agent_dir}"
  cp -R "${var_version_dir}/agent" "${var_current_agent_dir}"

  set_installed_version "${var_version}"
  set_env_var "SKYWALKING_AGENT_DIR" "${var_agent_dir}"
  info "已切换到版本: ${var_version}"
}

enable_skywalking() {
  set_env_var "SKYWALKING_ENABLED" "true"
  info "已启用 SkyWalking"
}

disable_skywalking() {
  set_env_var "SKYWALKING_ENABLED" "false"
  info "已禁用 SkyWalking"
}

# 解析参数
COMMAND="${1:-}"
shift || true

FORCE_FLAG="false"
SKYWALKING_DIR_ARG=""

while [ $# -gt 0 ]; do
  case "$1" in
    --force)
      FORCE_FLAG="true"
      shift
      ;;
    --dir)
      SKYWALKING_DIR_ARG="${2:-}"
      shift 2
      ;;
    *)
      break
      ;;
  esac
done

if [ -n "${SKYWALKING_DIR_ARG}" ]; then
  SKYWALKING_DIR="${SKYWALKING_DIR_ARG}"
  export SKYWALKING_DIR
fi

load_env

case "${COMMAND}" in
  download)
    download_skywalking "${1:-latest}" "${FORCE_FLAG}"
    ;;
  install)
    install_skywalking "${1:-}" "${FORCE_FLAG}"
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
    config_skywalking "${1:-}" "${2:-}"
    ;;
  uninstall)
    uninstall_version "${1:-}"
    ;;
  switch)
    switch_version "${1:-}"
    ;;
  enable)
    enable_skywalking
    ;;
  disable)
    disable_skywalking
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

