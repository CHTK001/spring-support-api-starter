#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

TTY_DEVICE="/dev/tty"

usage() {
  cat <<EOF
用法:
  gui.sh [--self-test]

功能:
  通过键盘快捷键执行常用运维操作（F 键 + 字母快捷键）。

快捷键:
  F1 / S     查看状态（status.sh）
  F2 / T     启动应用（start.sh）
  F3 / P     停止应用（stop.sh）
  F4 / R     重启应用（restart.sh）
  F5 / L     查看日志（logs.sh -n 200）
  F6 / E     查看环境（env.sh --list）
  F7 / B     备份（backup.sh --all）
  F8 / C     清理（clean.sh --dry-run）
  F9 / M     挂载服务（service.sh mount）
  F10 / U    卸载服务（service.sh unmount）
  A          资源分析（analyze.sh）
  I          查看信息（info.sh）
  D          调试启动（debug.sh）
  G          升级应用（upgrade.sh，交互输入包路径）
  K          回滚应用（rollback.sh，交互输入包路径）
  H    显示帮助
  Q    退出控制台

测试:
  --self-test   仅执行快捷键映射自检（非交互）
EOF
}

clear_screen() {
  if command -v clear >/dev/null 2>&1; then
    clear
  else
    printf '\033[2J\033[H'
  fi
}

enter_raw_mode() {
  stty -echo -icanon time 0 min 1 < "${TTY_DEVICE}"
}

leave_raw_mode() {
  stty sane < "${TTY_DEVICE}" 2>/dev/null || true
}

cleanup() {
  leave_raw_mode
}

show_menu() {
  clear_screen
  cat <<EOF
==========================================
 DevOps 快捷控制台（键盘快捷方式）
==========================================
 F1/S 状态   F2/T 启动   F3/P 停止   F4/R 重启
 F5/L 日志   F6/E 环境   F7/B 备份   F8/C 清理
 F9/M 挂载   F10/U 卸载  A 分析      I 信息
 D 调试      G 升级      K 回滚      H 帮助  Q 退出
==========================================
EOF
}

read_key_sequence() {
  var_seq="$(dd bs=1 count=1 2>/dev/null < "${TTY_DEVICE}" || true)"
  if [ -z "${var_seq}" ]; then
    return 1
  fi

  if [ "${var_seq}" != "$(printf '\033')" ]; then
    printf '%s' "${var_seq}"
    return 0
  fi

  var_next="$(dd bs=1 count=1 2>/dev/null < "${TTY_DEVICE}" || true)"
  var_seq="${var_seq}${var_next}"

  case "${var_next}" in
    "[")
      while :; do
        var_char="$(dd bs=1 count=1 2>/dev/null < "${TTY_DEVICE}" || true)"
        if [ -z "${var_char}" ]; then
          break
        fi
        var_seq="${var_seq}${var_char}"
        case "${var_char}" in
          "~"|[A-Za-z])
            break
            ;;
        esac
      done
      ;;
    "O")
      var_char="$(dd bs=1 count=1 2>/dev/null < "${TTY_DEVICE}" || true)"
      var_seq="${var_seq}${var_char}"
      ;;
  esac

  printf '%s' "${var_seq}"
  return 0
}

normalize_key() {
  var_seq="${1:-}"
  case "${var_seq}" in
    "$(printf '\033OP')"|"$(printf '\033[11~')"|"1"|"s"|"S")
      echo "STATUS"
      ;;
    "$(printf '\033OQ')"|"$(printf '\033[12~')"|"2"|"t"|"T")
      echo "START"
      ;;
    "$(printf '\033OR')"|"$(printf '\033[13~')"|"3"|"p"|"P")
      echo "STOP"
      ;;
    "$(printf '\033OS')"|"$(printf '\033[14~')"|"4"|"r"|"R")
      echo "RESTART"
      ;;
    "$(printf '\033[15~')"|"5"|"l"|"L")
      echo "LOGS"
      ;;
    "$(printf '\033[17~')"|"6"|"e"|"E")
      echo "ENV"
      ;;
    "$(printf '\033[18~')"|"7"|"b"|"B")
      echo "BACKUP"
      ;;
    "$(printf '\033[19~')"|"8"|"c"|"C")
      echo "CLEAN"
      ;;
    "$(printf '\033[20~')"|"9"|"m"|"M")
      echo "MOUNT"
      ;;
    "$(printf '\033[21~')"|"0"|"u"|"U")
      echo "UNMOUNT"
      ;;
    "a"|"A")
      echo "ANALYZE"
      ;;
    "i"|"I")
      echo "INFO"
      ;;
    "d"|"D")
      echo "DEBUG"
      ;;
    "g"|"G")
      echo "UPGRADE"
      ;;
    "k"|"K")
      echo "ROLLBACK"
      ;;
    "h"|"H")
      echo "H"
      ;;
    "q"|"Q")
      echo "Q"
      ;;
    *)
      echo ""
      ;;
  esac
}

run_self_test() {
  var_failed="0"

  assert_key() {
    var_expected="${1:-}"
    var_input="${2:-}"
    var_actual="$(normalize_key "${var_input}")"
    if [ "${var_actual}" != "${var_expected}" ]; then
      warn "快捷键映射失败: expected=${var_expected}, actual=${var_actual}"
      var_failed="1"
    fi
  }

  assert_key "STATUS" "$(printf '\033OP')"
  assert_key "START" "$(printf '\033OQ')"
  assert_key "STOP" "$(printf '\033OR')"
  assert_key "RESTART" "$(printf '\033OS')"
  assert_key "LOGS" "$(printf '\033[15~')"
  assert_key "ENV" "$(printf '\033[17~')"
  assert_key "BACKUP" "$(printf '\033[18~')"
  assert_key "CLEAN" "$(printf '\033[19~')"
  assert_key "MOUNT" "$(printf '\033[20~')"
  assert_key "UNMOUNT" "$(printf '\033[21~')"
  assert_key "STATUS" "s"
  assert_key "START" "t"
  assert_key "STOP" "p"
  assert_key "RESTART" "r"
  assert_key "LOGS" "l"
  assert_key "ENV" "e"
  assert_key "BACKUP" "b"
  assert_key "CLEAN" "c"
  assert_key "MOUNT" "m"
  assert_key "UNMOUNT" "u"
  assert_key "ANALYZE" "a"
  assert_key "INFO" "i"
  assert_key "DEBUG" "d"
  assert_key "UPGRADE" "g"
  assert_key "ROLLBACK" "k"
  assert_key "H" "h"
  assert_key "Q" "q"

  # 数字回退映射（终端无法发送 F 键时）
  assert_key "STATUS" "1"
  assert_key "START" "2"
  assert_key "STOP" "3"
  assert_key "RESTART" "4"
  assert_key "LOGS" "5"
  assert_key "ENV" "6"
  assert_key "BACKUP" "7"
  assert_key "CLEAN" "8"
  assert_key "MOUNT" "9"
  assert_key "UNMOUNT" "0"

  if [ "${var_failed}" != "0" ]; then
    return 1
  fi

  info "GUI 快捷键映射自检通过"
  return 0
}

pause_and_return_menu() {
  echo ""
  echo "按 Enter 返回控制台..."
  read -r _dummy < "${TTY_DEVICE}" || true
  enter_raw_mode
  show_menu
}

ask_yes_no() {
  var_prompt="${1:-}"
  var_default="${2:-Y}"

  while :; do
    printf '%s' "${var_prompt}"
    read -r var_answer < "${TTY_DEVICE}" || var_answer=""
    if [ -z "${var_answer}" ]; then
      var_answer="${var_default}"
    fi

    case "${var_answer}" in
      y|Y|yes|YES)
        return 0
        ;;
      n|N|no|NO)
        return 1
        ;;
      *)
        echo "请输入 y 或 n"
        ;;
    esac
  done
}

run_upgrade_wizard() {
  leave_raw_mode
  echo ""
  echo ">>> 升级应用（快捷键 G）"
  printf "请输入升级包路径: "
  read -r var_package < "${TTY_DEVICE}" || var_package=""

  if [ -z "${var_package}" ]; then
    warn "未输入升级包路径，已取消升级"
    pause_and_return_menu
    return 0
  fi

  if ask_yes_no "是否启用备份? [Y/n]: " "Y"; then
    var_backup="true"
  else
    var_backup="false"
  fi

  if ask_yes_no "是否校验包完整性(--verify)? [y/N]: " "N"; then
    var_verify="true"
  else
    var_verify="false"
  fi

  if ask_yes_no "升级后立即重启? [Y/n]: " "Y"; then
    var_no_restart="false"
  else
    var_no_restart="true"
  fi

  if (
    set -- "${BIN_DIR}/upgrade.sh" -p "${var_package}"
    if [ "${var_backup}" = "true" ]; then
      set -- "$@" "--backup"
    fi
    if [ "${var_verify}" = "true" ]; then
      set -- "$@" "--verify"
    fi
    if [ "${var_no_restart}" = "true" ]; then
      set -- "$@" "--no-restart"
    fi
    "$@"
  ); then
    info "升级命令执行完成"
  else
    warn "升级命令执行失败"
  fi

  pause_and_return_menu
}

run_rollback_wizard() {
  leave_raw_mode
  echo ""
  echo ">>> 回滚应用（快捷键 K）"
  printf "请输入回滚包路径: "
  read -r var_package < "${TTY_DEVICE}" || var_package=""

  if [ -z "${var_package}" ]; then
    warn "未输入回滚包路径，已取消回滚"
    pause_and_return_menu
    return 0
  fi

  if ask_yes_no "是否启用备份? [Y/n]: " "Y"; then
    var_backup="true"
  else
    var_backup="false"
  fi

  if ask_yes_no "回滚后立即重启? [Y/n]: " "Y"; then
    var_no_restart="false"
  else
    var_no_restart="true"
  fi

  if (
    set -- "${BIN_DIR}/rollback.sh" -p "${var_package}"
    if [ "${var_backup}" = "true" ]; then
      set -- "$@" "--backup"
    fi
    if [ "${var_no_restart}" = "true" ]; then
      set -- "$@" "--no-restart"
    fi
    "$@"
  ); then
    info "回滚命令执行完成"
  else
    warn "回滚命令执行失败"
  fi

  pause_and_return_menu
}

run_and_pause() {
  var_desc="${1:-}"
  shift || true

  leave_raw_mode
  echo ""
  echo ">>> ${var_desc}"
  "$@" || warn "命令执行失败: $*"
  echo ""
  echo "按 Enter 返回控制台..."
  read -r _dummy < "${TTY_DEVICE}" || true
  enter_raw_mode
  show_menu
}

if [ $# -gt 0 ]; then
  case "${1:-}" in
    -h|--help)
      usage
      exit 0
      ;;
    --self-test)
      run_self_test
      exit $?
      ;;
    *)
      warn "未知参数: ${1}"
      usage
      exit 1
      ;;
  esac
fi

if [ ! -r "${TTY_DEVICE}" ] || [ ! -w "${TTY_DEVICE}" ]; then
  die "当前环境不可交互，无法启动 GUI 控制台"
fi

trap cleanup INT TERM EXIT
show_menu
enter_raw_mode

while true; do
  KEY_SEQ="$(read_key_sequence || true)"
  if [ -z "${KEY_SEQ}" ]; then
    continue
  fi

  KEY_NAME="$(normalize_key "${KEY_SEQ}")"
  case "${KEY_NAME}" in
    STATUS)
      run_and_pause "查看状态" "${BIN_DIR}/status.sh"
      ;;
    START)
      run_and_pause "启动应用" "${BIN_DIR}/start.sh"
      ;;
    STOP)
      run_and_pause "停止应用" "${BIN_DIR}/stop.sh"
      ;;
    RESTART)
      run_and_pause "重启应用" "${BIN_DIR}/restart.sh"
      ;;
    LOGS)
      run_and_pause "查看日志（最后 200 行）" "${BIN_DIR}/logs.sh" -n 200
      ;;
    ENV)
      run_and_pause "查看环境配置" "${BIN_DIR}/env.sh" --list
      ;;
    BACKUP)
      run_and_pause "执行备份（--all）" "${BIN_DIR}/backup.sh" --all
      ;;
    CLEAN)
      run_and_pause "执行清理预览（--dry-run）" "${BIN_DIR}/clean.sh" --dry-run
      ;;
    MOUNT)
      run_and_pause "挂载服务（systemd）" "${BIN_DIR}/service.sh" mount
      ;;
    UNMOUNT)
      run_and_pause "卸载服务（systemd）" "${BIN_DIR}/service.sh" unmount
      ;;
    ANALYZE)
      run_and_pause "资源分析" "${BIN_DIR}/analyze.sh"
      ;;
    INFO)
      run_and_pause "查看应用信息" "${BIN_DIR}/info.sh"
      ;;
    DEBUG)
      run_and_pause "调试模式启动（默认参数）" "${BIN_DIR}/debug.sh"
      ;;
    UPGRADE)
      run_upgrade_wizard
      ;;
    ROLLBACK)
      run_rollback_wizard
      ;;
    H)
      leave_raw_mode
      echo ""
      usage
      echo ""
      pause_and_return_menu
      ;;
    Q)
      break
      ;;
    *)
      # 忽略无法识别的按键
      ;;
  esac
done

leave_raw_mode
trap - INT TERM EXIT
info "GUI 控制台已退出"
