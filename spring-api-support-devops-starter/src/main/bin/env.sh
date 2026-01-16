#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  env.sh [选项] [变量名=值 ...]

选项:
  -l, --list      显示所有环境变量配置
  -s, --set       设置环境变量（格式: 变量名=值）
  -u, --unset     删除环境变量（格式: 变量名）
  -r, --reset     重置环境变量配置为默认值
  -h, --help      显示帮助信息

示例:
  env.sh --list                           # 显示所有配置
  env.sh --set BACKUP_DIR=/data/backup    # 设置备份目录
  env.sh --set JVM_ARGS="-Xms512m -Xmx1024m"  # 设置 JVM 参数
  env.sh --unset SKYWALKING_ENABLED       # 删除配置项
  env.sh --reset                          # 重置为默认配置
EOF
}

ensure_env_file
load_env

ENV_FILE="$(get_base_dir)/bin/app.env"

show_all() {
  if [ ! -f "${ENV_FILE}" ]; then
    info "环境文件不存在: ${ENV_FILE}"
    return 1
  fi
  
  echo "=========================================="
  echo "环境变量配置: ${ENV_FILE}"
  echo "=========================================="
  
  # 读取并显示所有非注释行
  while IFS= read -r line || [ -n "${line}" ]; do
    # 跳过空行和注释行
    trimmed_line="$(echo "${line}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    if [ -z "${trimmed_line}" ] || [ "$(echo "${trimmed_line}" | cut -c1)" = "#" ]; then
      continue
    fi
    
    # 显示配置项
    if echo "${trimmed_line}" | grep -q "="; then
      var_name="$(echo "${trimmed_line}" | cut -d'=' -f1 | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
      var_value="$(echo "${trimmed_line}" | cut -d'=' -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
      
      # 如果值包含 ${project.xxx}，说明是模板变量，显示为未设置
      if echo "${var_value}" | grep -q '\${project\.'; then
        echo "  ${var_name}=未设置（使用默认值）"
      else
        if [ -z "${var_value}" ]; then
          echo "  ${var_name}=（空）"
        else
          echo "  ${var_name}=${var_value}"
        fi
      fi
    fi
  done < "${ENV_FILE}"
  
  echo "=========================================="
  echo ""
  echo "提示: 使用 env.sh --set 变量名=值 来设置配置"
  echo "提示: 使用 env.sh --unset 变量名 来删除配置"
}

set_var() {
  var_pair="${1:-}"
  if [ -z "${var_pair}" ]; then
    warn "请提供变量名=值 格式的参数"
    return 1
  fi
  
  if ! echo "${var_pair}" | grep -q "="; then
    warn "格式错误，应为: 变量名=值"
    return 1
  fi
  
  var_name="$(echo "${var_pair}" | cut -d'=' -f1 | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
  var_value="$(echo "${var_pair}" | cut -d'=' -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
  
  if [ -z "${var_name}" ]; then
    warn "变量名不能为空"
    return 1
  fi
  
  # 检查变量名是否合法（只允许字母、数字、下划线）
  if ! echo "${var_name}" | grep -qE '^[A-Za-z_][A-Za-z0-9_]*$'; then
    warn "变量名格式不正确: ${var_name}"
    return 1
  fi
  
  # 使用已修复的 set_env_var 函数来处理特殊字符
  set_env_var "${var_name}" "${var_value}" "${ENV_FILE}"
  info "已设置: ${var_name}=${var_value}"
}

unset_var() {
  var_name="${1:-}"
  if [ -z "${var_name}" ]; then
    warn "请提供变量名"
    return 1
  fi
  
  # 读取现有文件内容，排除目标变量
  TMP_FILE="${ENV_FILE}.tmp.$$"
  rm -f "${TMP_FILE}"
  var_found="false"
  
  while IFS= read -r line || [ -n "${line}" ]; do
    trimmed_line="$(echo "${line}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    
    # 如果是注释或空行，直接保留
    if [ -z "${trimmed_line}" ] || [ "$(echo "${trimmed_line}" | cut -c1)" = "#" ]; then
      echo "${line}" >> "${TMP_FILE}"
      continue
    fi
    
    # 检查是否是目标变量
    if echo "${trimmed_line}" | grep -q "^${var_name}="; then
      var_found="true"
      # 跳过这一行（删除变量）
    else
      # 保留其他行
      echo "${line}" >> "${TMP_FILE}"
    fi
  done < "${ENV_FILE}"
  
  if [ "${var_found}" = "true" ]; then
    mv "${TMP_FILE}" "${ENV_FILE}"
    info "已删除: ${var_name}"
  else
    rm -f "${TMP_FILE}"
    warn "未找到变量: ${var_name}"
    return 1
  fi
}

reset_env() {
  var_base_dir="$(get_base_dir)"
  var_env_file="${var_base_dir}/bin/app.env"
  
  # 备份现有配置
  if [ -f "${var_env_file}" ]; then
    BACKUP_FILE="${var_env_file}.backup.$(date +%Y%m%d_%H%M%S)"
    cp "${var_env_file}" "${BACKUP_FILE}"
    info "已备份现有配置到: ${BACKUP_FILE}"
  fi
  
  # 生成默认配置
  cat > "${var_env_file}" <<EOF
# 应用名称/版本（默认使用打包时的 artifactId/version）
APP_NAME=${project.artifactId}
APP_VERSION=${project.version}

# JVM 参数（可按需追加，例如 -Xms512m -Xmx1024m）
JVM_ARGS=

# 默认时区/编码（可覆盖 start.sh 参数）
TIMEZONE=Asia/Shanghai
CHARSET=UTF-8

# 备份目录（默认 <base>/backup）
BACKUP_DIR=
EOF
  
  info "已重置环境变量配置为默认值"
  info "提示: 备份文件保存在 ${BACKUP_FILE}"
}

# 解析参数
MODE="list"
VARS_TO_SET=""
VARS_TO_UNSET=""

while [ $# -gt 0 ]; do
  case "$1" in
    -l|--list)
      MODE="list"
      shift
      ;;
    -s|--set)
      MODE="set"
      shift
      ;;
    -u|--unset)
      MODE="unset"
      shift
      ;;
    -r|--reset)
      MODE="reset"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      if [ "${MODE}" = "set" ]; then
        # 直接处理单个参数，支持包含空格的值（通过引号传递）
        if [ -z "${VARS_TO_SET}" ]; then
          VARS_TO_SET="$1"
        else
          # 如果已有值，检查当前参数是否包含等号
          # 如果包含等号，说明是新变量，使用特殊标记分隔
          # 如果不包含等号，说明是前一个变量值的延续（因为shell去掉了引号导致值被分割）
          if echo "$1" | grep -q "="; then
            VARS_TO_SET="${VARS_TO_SET}__ENV_SEP__$1"
          else
            # 追加到前一个变量的值中（用空格连接）
            VARS_TO_SET="${VARS_TO_SET} $1"
          fi
        fi
      elif [ "${MODE}" = "unset" ]; then
        if [ -z "${VARS_TO_UNSET}" ]; then
          VARS_TO_UNSET="$1"
        else
          VARS_TO_UNSET="${VARS_TO_UNSET} $1"
        fi
      else
        warn "未知参数: $1"
        usage
        exit 1
      fi
      shift
      ;;
  esac
done

# 执行操作
if [ "${MODE}" = "list" ]; then
  show_all
elif [ "${MODE}" = "set" ]; then
  if [ -z "${VARS_TO_SET}" ]; then
    warn "请提供要设置的变量（格式: 变量名=值）"
    usage
    exit 1
  fi
  # 处理单个或多个变量设置（使用特殊分隔符）
  # 如果包含分隔符，说明有多个变量
  if echo "${VARS_TO_SET}" | grep -q "__ENV_SEP__"; then
    echo "${VARS_TO_SET}" | sed 's/__ENV_SEP__/\n/g' | while IFS= read -r var_pair || [ -n "${var_pair}" ]; do
      if [ -n "${var_pair}" ]; then
        set_var "${var_pair}"
      fi
    done
  else
    # 单个变量，直接处理（支持包含空格的值）
    set_var "${VARS_TO_SET}"
  fi
elif [ "${MODE}" = "unset" ]; then
  if [ -z "${VARS_TO_UNSET}" ]; then
    warn "请提供要删除的变量名"
    usage
    exit 1
  fi
  for var_name in ${VARS_TO_UNSET}; do
    unset_var "${var_name}"
  done
elif [ "${MODE}" = "reset" ]; then
  reset_env
fi

