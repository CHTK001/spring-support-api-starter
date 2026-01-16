#!/usr/bin/env sh

set -eu

info() {
  echo "[spring-support-api-devops][devops] $*"
}

warn() {
  echo "[spring-support-api-devops][devops] $*" 1>&2
}

die() {
  warn "$*"
  exit 1
}

resolve_path() {
  # 兼容 busybox
  if command -v readlink >/dev/null 2>&1; then
    readlink -f "$1" 2>/dev/null || echo "$1"
  else
    echo "$1"
  fi
}

get_bin_dir() {
  # 当前脚本所在目录（绝对路径）
  # 说明：common.sh 可能会被 bin 子目录下的脚本 source（例如 bin/script/cluster/cluster.sh）。
  # 这类场景下 $0 指向调用脚本而非 bin 目录，为避免定位错误，允许通过 BIN_DIR_OVERRIDE 显式指定。
  if [ -n "${BIN_DIR_OVERRIDE:-}" ]; then
    echo "${BIN_DIR_OVERRIDE}"
    return 0
  fi
  var_dir="$(cd "$(dirname "$0")" && pwd)"
  echo "${var_dir}"
}

get_base_dir() {
  # bin 的上一级是发布根目录
  var_bin_dir="$(get_bin_dir)"
  var_base_dir="$(cd "${var_bin_dir}/.." && pwd)"
  echo "${var_base_dir}"
}

load_env() {
  var_base_dir="$(get_base_dir)"
  var_env_file="${var_base_dir}/bin/app.env"
  if [ -f "${var_env_file}" ]; then
    # shellcheck source=/dev/null
    . "${var_env_file}"
  fi
}

ensure_env_file() {
  # 确保 app.env 存在（首次发布场景），避免启动脚本找不到环境文件
  var_base_dir="$(get_base_dir)"
  var_env_file="${var_base_dir}/bin/app.env"
  if [ -f "${var_env_file}" ]; then
    return 0
  fi
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
  info "已生成默认环境文件: ${var_env_file}"
}

set_env_var() {
  # 设置环境变量到 app.env 文件
  # 用法: set_env_var "变量名" "变量值"
  var_name="${1:-}"
  var_value="${2:-}"
  
  if [ -z "${var_name}" ]; then
    return 1
  fi
  
  var_base_dir="$(get_base_dir)"
  var_env_file="${var_base_dir}/bin/app.env"
  ensure_env_file
  
  # 读取现有文件内容
  TMP_FILE="${var_env_file}.tmp.$$"
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
      # 替换现有值，如果值包含空格或特殊字符，使用引号包裹
      # 使用 printf 避免 Bad substitution 错误，并对引号进行转义
      if echo "${var_value}" | grep -qE '[[:space:]$`"\\]'; then
        # 转义引号：将 " 替换为 \"
        escaped_value="$(printf '%s\n' "${var_value}" | sed 's/"/\\"/g')"
        printf '%s="%s"\n' "${var_name}" "${escaped_value}" >> "${TMP_FILE}"
      else
        printf '%s=%s\n' "${var_name}" "${var_value}" >> "${TMP_FILE}"
      fi
      var_found="true"
    else
      # 保留其他行
      printf '%s\n' "${line}" >> "${TMP_FILE}"
    fi
  done < "${var_env_file}"
  
  # 如果变量不存在，追加到文件末尾
  if [ "${var_found}" = "false" ]; then
    # 如果值包含空格或特殊字符，使用引号包裹
    # 使用 printf 避免 Bad substitution 错误，并对引号进行转义
    if echo "${var_value}" | grep -qE '[[:space:]$`"\\]'; then
      # 转义引号：将 " 替换为 \"
      escaped_value="$(printf '%s\n' "${var_value}" | sed 's/"/\\"/g')"
      printf '%s="%s"\n' "${var_name}" "${escaped_value}" >> "${TMP_FILE}"
    else
      printf '%s=%s\n' "${var_name}" "${var_value}" >> "${TMP_FILE}"
    fi
  fi
  
  mv "${TMP_FILE}" "${var_env_file}"
}

resolve_java() {
  var_java_home="${1:-}"
  if [ -n "${var_java_home}" ]; then
    # 允许直接传 java 可执行文件路径；注意目录也可能满足 -x（可进入），因此需先判断为普通文件
    if [ -f "${var_java_home}" ] && [ -x "${var_java_home}" ]; then
      echo "${var_java_home}"
      return 0
    fi
    if [ -x "${var_java_home}/bin/java" ]; then
      echo "${var_java_home}/bin/java"
      return 0
    fi
    die "未找到 java 可执行文件: ${var_java_home}"
  fi

  if [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
    echo "${JAVA_HOME}/bin/java"
    return 0
  fi

  if command -v java >/dev/null 2>&1; then
    command -v java
    return 0
  fi

  die "未找到 java，请通过 -j 指定 JAVA_HOME 或配置 JAVA_HOME"
}

get_app_name() {
  if [ -n "${APP_NAME:-}" ]; then
    echo "${APP_NAME}"
  else
    echo "app"
  fi
}

get_app_version() {
  if [ -n "${APP_VERSION:-}" ]; then
    echo "${APP_VERSION}"
  else
    echo "unknown"
  fi
}

get_jar_file() {
  var_base_dir="$(get_base_dir)"
  
  # 如果配置了JAR_FILE_NAME，优先使用
  if [ -n "${JAR_FILE_NAME:-}" ]; then
    var_candidate="${var_base_dir}/${JAR_FILE_NAME}"
    if [ -f "${var_candidate}" ]; then
      echo "${var_candidate}"
      return 0
    fi
  fi
  
  var_name="$(get_app_name)"
  var_version="$(get_app_version)"
  # 优先按名称+版本拼接，保证可预测
  var_candidate="${var_base_dir}/${var_name}-${var_version}.jar"
  if [ -f "${var_candidate}" ]; then
    echo "${var_candidate}"
    return 0
  fi

  # 兜底：如果版本信息不可靠，则在目录下自动探测唯一的 jar
  for var_jar in "${var_base_dir}"/*.jar; do
    if [ -f "${var_jar}" ]; then
      echo "${var_jar}"
      return 0
    fi
  done

  # 最后兜底：退回到无版本名，便于兼容自定义发布目录
  echo "${var_base_dir}/${var_name}.jar"
}

get_pid_file() {
  var_base_dir="$(get_base_dir)"
  var_run_dir="${var_base_dir}/run"
  if [ ! -d "${var_run_dir}" ]; then
    mkdir -p "${var_run_dir}"
  fi
  echo "${var_run_dir}/app.pid"
}

get_log_dir() {
  var_base_dir="$(get_base_dir)"
  if [ -n "${LOG_DIR:-}" ]; then
    # 如果配置了绝对路径，直接使用
    if [ "$(echo "${LOG_DIR}" | cut -c1)" = "/" ]; then
      var_log_dir="${LOG_DIR}"
    else
      # 相对路径，相对于 base_dir
      var_log_dir="${var_base_dir}/${LOG_DIR}"
    fi
  else
    # 默认使用 <base>/logs
    var_log_dir="${var_base_dir}/logs"
  fi
  
  if [ ! -d "${var_log_dir}" ]; then
    mkdir -p "${var_log_dir}"
  fi
  echo "${var_log_dir}"
}

get_console_log() {
  var_log_dir="$(get_log_dir)"
  echo "${var_log_dir}/console.log"
}

get_backup_dir() {
  var_base_dir="$(get_base_dir)"
  if [ -n "${BACKUP_DIR:-}" ]; then
    # 如果配置了绝对路径，直接使用
    if [ "$(echo "${BACKUP_DIR}" | cut -c1)" = "/" ]; then
      echo "${BACKUP_DIR}"
      return 0
    fi
    # 相对路径，相对于 base_dir
    echo "${var_base_dir}/${BACKUP_DIR}"
    return 0
  fi
  # 默认使用 <base>/backup
  echo "${var_base_dir}/backup"
}

is_running() {
  var_pid="${1:-}"
  if [ -z "${var_pid}" ]; then
    return 1
  fi
  if kill -0 "${var_pid}" >/dev/null 2>&1; then
    return 0
  fi
  return 1
}

download_file() {
  var_url="$1"
  var_dest="$2"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "${var_url}" -o "${var_dest}"
    return 0
  fi
  if command -v wget >/dev/null 2>&1; then
    wget -qO "${var_dest}" "${var_url}"
    return 0
  fi
  die "缺少 curl/wget，无法下载: ${var_url}"
}

ensure_skywalking_agent() {
  if [ "${SKYWALKING_ENABLED:-false}" != "true" ]; then
    return 0
  fi
  var_base_dir="$(get_base_dir)"
  var_agent_dir="${SKYWALKING_AGENT_DIR:-${var_base_dir}/agents/skywalking}"
  mkdir -p "${var_agent_dir}"

  if [ -z "${SKYWALKING_DIST_URL:-}" ] && [ ! -f "${var_agent_dir}/skywalking-agent.jar" ]; then
    die "SKYWALKING_ENABLED=true 但未配置 SKYWALKING_DIST_URL，且本地未找到 skywalking-agent.jar"
  fi

  if [ ! -f "${var_agent_dir}/skywalking-agent.jar" ]; then
    info "下载 SkyWalking 探针: ${SKYWALKING_DIST_URL}"
    var_tmp="${var_agent_dir}/skywalking-agent.tgz"
    download_file "${SKYWALKING_DIST_URL}" "${var_tmp}"
    tar -xzf "${var_tmp}" -C "${var_agent_dir}"
    rm -f "${var_tmp}"
  fi

  if [ ! -f "${var_agent_dir}/skywalking-agent.jar" ]; then
    die "SkyWalking 安装目录中未找到 skywalking-agent.jar: ${var_agent_dir}"
  fi

  SKYWALKING_AGENT_DIR="${var_agent_dir}"
}

get_skywalking_javaagent_opts() {
  if [ "${SKYWALKING_ENABLED:-false}" != "true" ]; then
    return 0
  fi
  ensure_skywalking_agent

  var_agent_dir="${SKYWALKING_AGENT_DIR}"
  var_agent_jar="${var_agent_dir}/skywalking-agent.jar"
  var_app_name="$(get_app_name)"

  var_agent_name="${SW_AGENT_NAME:-${var_app_name}}"
  var_backend="${SW_AGENT_COLLECTOR_BACKEND_SERVICES:-${SKYWALKING_COLLECTOR_BACKEND_SERVICES:-}}"
  var_instance="${SW_AGENT_INSTANCE_NAME:-}"

  var_opts="-javaagent:${var_agent_jar} -DSW_AGENT_NAME=${var_agent_name}"
  if [ -n "${var_backend}" ]; then
    var_opts="${var_opts} -DSW_AGENT_COLLECTOR_BACKEND_SERVICES=${var_backend}"
  fi
  if [ -n "${var_instance}" ]; then
    var_opts="${var_opts} -DSW_AGENT_INSTANCE_NAME=${var_instance}"
  fi

  echo "${var_opts}"
}

ensure_arthas() {
  if [ "${ARTHAS_ENABLED:-false}" != "true" ]; then
    return 0
  fi

  var_base_dir="$(get_base_dir)"
  var_home="${ARTHAS_HOME:-${var_base_dir}/agents/arthas}"
  mkdir -p "${var_home}"

  var_boot_jar="${var_home}/arthas-boot.jar"
  if [ -f "${var_boot_jar}" ]; then
    ARTHAS_HOME="${var_home}"
    return 0
  fi

  if [ -z "${ARTHAS_BOOT_URL:-}" ]; then
    die "ARTHAS_ENABLED=true 但未配置 ARTHAS_BOOT_URL，且本地未找到 arthas-boot.jar"
  fi

  info "下载 Arthas: ${ARTHAS_BOOT_URL}"
  download_file "${ARTHAS_BOOT_URL}" "${var_boot_jar}"
  chmod +x "${var_boot_jar}"

  ARTHAS_HOME="${var_home}"
}

start_arthas_if_enabled() {
  var_pid="$1"
  var_java_bin="$2"

  if [ "${ARTHAS_ENABLED:-false}" != "true" ]; then
    return 0
  fi

  ensure_arthas

  var_home="${ARTHAS_HOME}"
  var_boot_jar="${var_home}/arthas-boot.jar"
  var_telnet="${ARTHAS_TELNET_PORT:-3658}"
  var_http="${ARTHAS_HTTP_PORT:-8563}"

  if [ ! -f "${var_boot_jar}" ]; then
    warn "未找到 arthas-boot.jar，跳过 Arthas 启动: ${var_boot_jar}"
    return 0
  fi

  info "启动 Arthas，PID=${var_pid}，telnet=${var_telnet}，http=${var_http}"
  nohup "${var_java_bin}" -jar "${var_boot_jar}" "${var_pid}" \
    --telnet-port "${var_telnet}" \
    --http-port "${var_http}" >/dev/null 2>&1 &
}

# 检查应用是否健康（通过HTTP接口）
# 用法: check_health [端口] [路径] [超时秒数]
# 默认端口: 8080，默认路径: /hello，默认超时: 30秒
check_health() {
  var_port="${1:-8080}"
  var_path="${2:-/hello}"
  var_timeout="${3:-30}"
  var_url="http://127.0.0.1:${var_port}${var_path}"
  
  var_elapsed=0
  var_interval=1
  
  while [ ${var_elapsed} -lt ${var_timeout} ]; do
    # 使用 curl 或 wget 检查健康状态
    if command -v curl >/dev/null 2>&1; then
      if curl -sf "${var_url}" >/dev/null 2>&1; then
        return 0
      fi
    elif command -v wget >/dev/null 2>&1; then
      if wget -q --spider "${var_url}" >/dev/null 2>&1; then
        return 0
      fi
    else
      # 如果没有 curl/wget，通过检查进程和日志判断
      var_pid_file="$(get_pid_file)"
      if [ -f "${var_pid_file}" ]; then
        var_pid="$(cat "${var_pid_file}" 2>/dev/null || echo "")"
        if [ -n "${var_pid}" ] && is_running "${var_pid}"; then
          # 检查日志中是否有启动成功的标志（Spring Boot 通常会有 "Started" 日志）
          var_log="$(get_console_log)"
          if [ -f "${var_log}" ]; then
            if grep -q "Started.*in.*seconds" "${var_log}" 2>/dev/null || \
               grep -q "Tomcat started on port" "${var_log}" 2>/dev/null || \
               grep -q "Netty started on port" "${var_log}" 2>/dev/null; then
              return 0
            fi
          fi
        fi
      fi
    fi
    
    sleep ${var_interval}
    var_elapsed=$((var_elapsed + var_interval))
  done
  
  return 1
}

# 等待应用启动完成
# 用法: wait_for_start [端口] [路径] [超时秒数]
wait_for_start() {
  var_port="${1:-8080}"
  var_path="${2:-/hello}"
  var_timeout="${3:-60}"
  
  info "等待应用启动（最多 ${var_timeout} 秒）..."
  
  if check_health "${var_port}" "${var_path}" "${var_timeout}"; then
    info "应用已就绪"
    return 0
  else
    warn "应用启动超时或健康检查失败"
    return 1
  fi
}

# 验证端口是否为有效数字
validate_port() {
  var_port="${1:-}"
  if [ -z "${var_port}" ]; then
    return 1
  fi
  # 检查是否为纯数字（兼容busybox，不使用-E选项）
  if echo "${var_port}" | grep -q '^[0-9][0-9]*$'; then
    # 检查范围（使用expr进行数值比较，兼容性更好）
    if [ "${var_port}" -ge 1 ] 2>/dev/null && [ "${var_port}" -le 65535 ] 2>/dev/null; then
      return 0
    fi
  fi
  return 1
}

# 验证路径是否存在且为目录
validate_directory() {
  var_dir="${1:-}"
  if [ -z "${var_dir}" ]; then
    return 1
  fi
  if [ -d "${var_dir}" ]; then
    return 0
  fi
  return 1
}


