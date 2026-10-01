#!/usr/bin/env bash
#
# oauth 服务端一键部署（幂等）。在【目标主机】上执行。
#
# 干什么：
#   1) 解包构建产物到 $APP_DIR/app
#   2) 建库（oauth）并把权限授予环境变量里的账号
#   3) 从产物 jar 里抽出各模块的 db/init 脚本并落库（结构 -> 数据，按文件名排序）
#   4) 安装 systemd unit（配合 /etc/oauth/oauth.env 注入凭据）
#   5) 重启服务并自检（端口 + actuator/health）
#
# 为什么不用框架自带的建库编排（plugin.datasource.script.enable=true）：
#   该切面会遍历**所有** DataSource Bean（实测有 dataSource / master /
#   dynamicDataSource / eduAuthDataSource 四个），其中 eduAuthDataSource 指向
#   eduai 库。一旦打开，oauth 的 ~68 张表会被灌进 eduai 库。
#   编排器没有「排除数据源」的开关，故本脚本改为显式落库：
#   结果确定、可重放，且不依赖运行时扫描语义。
#   代价：不写 sys_database_version 历史表（应用侧 enable=false，该表本就未用）。
#
# 前置：
#   - 以 root（或 sudo）执行（需要建库 / 写 systemd unit）
#   - 构建产物已就位：$BUNDLE（默认 $APP_DIR/oauth-app.tar.gz，由 deploy/package.sh 生成）
#   - 注意：不要把产物放 /tmp —— 本仓库的部署主机 /tmp 常被挂成小容量 tmpfs
#     （实测 2.9G，且占的是内存），大包会直接写不进去（No space left on device）。
#   - 凭据文件已就位：/etc/oauth/oauth.env（模板见 deploy/oauth.env.example）
#
# 用法：
#   sudo BUNDLE=/tmp/oauth-app.tar.gz bash deploy/deploy.sh
#
set -euo pipefail

APP_DIR=${APP_DIR:-/home/kali/oauth}
APP_HOME="$APP_DIR/app"
SQL_DIR="$APP_DIR/sql"
BUNDLE=${BUNDLE:-$APP_DIR/oauth-app.tar.gz}
ENV_FILE=${ENV_FILE:-/etc/oauth/oauth.env}
UNIT_NAME=oauth-server.service
UNIT_SRC="$APP_HOME/deploy/$UNIT_NAME"
UNIT_DST="/etc/systemd/system/$UNIT_NAME"
JAR=spring-api-support-oauth-starter-4.0.0.42.jar
DB_NAME=oauth

log() { printf '  %s\n' "$*"; }
die() { printf '  [错误] %s\n' "$*" >&2; exit 1; }

# ---------- 0. 前置校验 ----------
[ "$(id -u)" -eq 0 ] || die "请以 root（或 sudo）执行：需要建库与写 systemd unit"
[ -f "$BUNDLE" ] || die "缺少构建产物 $BUNDLE（先在开发机执行 deploy/package.sh 并上传）"
[ -f "$ENV_FILE" ] || die "缺少凭据文件 $ENV_FILE（复制 deploy/oauth.env.example 并填真实值）"
command -v unzip >/dev/null || die "缺少 unzip"
command -v mariadb >/dev/null || die "缺少 mariadb 客户端"

log "==> 前置校验通过"
log "    产物=$BUNDLE  凭据=$ENV_FILE  目录=$APP_DIR"

# 读取凭据（只为建库/落库用；服务本身由 systemd EnvironmentFile 读取）
set -a
# shellcheck disable=SC1090
. "$ENV_FILE"
set +a
: "${OAUTH_DB_HOST:=127.0.0.1}"
: "${OAUTH_DB_PORT:=3306}"
: "${OAUTH_DB_USER:?oauth.env 缺少 OAUTH_DB_USER}"
: "${OAUTH_DB_PASSWORD:?oauth.env 缺少 OAUTH_DB_PASSWORD}"

# ---------- 1. 解包 ----------
log "==> 1/6 解包构建产物"
mkdir -p "$APP_HOME"
tar -xzf "$BUNDLE" -C "$APP_HOME"
[ -f "$APP_HOME/$JAR" ] || die "产物里没有 $JAR"
[ -f "$UNIT_SRC" ] || die "产物里没有 deploy/$UNIT_NAME"
log "    jar=$(basename "$APP_HOME/$JAR")  lib=$(ls -1 "$APP_HOME"/lib 2>/dev/null | wc -l) 个依赖"

# ---------- 2. 建库 + 授权 ----------
log "==> 2/6 建库 $DB_NAME 并授权给 $OAUTH_DB_USER"
# 建库需要特权账号：本机用 root 的 unix_socket。库不在本机时请自行预先建库。
mariadb -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
mariadb -e "CREATE USER IF NOT EXISTS '$OAUTH_DB_USER'@'localhost' IDENTIFIED BY '$OAUTH_DB_PASSWORD';"
mariadb -e "GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$OAUTH_DB_USER'@'localhost'; FLUSH PRIVILEGES;"
log "    库已就绪"

# ---------- 3. 落库（结构 -> 数据） ----------
log "==> 3/6 抽出 db/init 脚本并落库"
rm -rf "$SQL_DIR"
mkdir -p "$SQL_DIR/init" "$SQL_DIR/initdata"
for j in "$APP_HOME/$JAR" "$APP_HOME"/lib/*.jar; do
    unzip -o -j "$j" 'db/init/V1.0__init_all_*.sql' -d "$SQL_DIR/init" >/dev/null 2>&1 || true
    unzip -o -j "$j" 'db/init/V1.0__initdata_all_*.sql' -d "$SQL_DIR/initdata" >/dev/null 2>&1 || true
done
INIT_N=$(ls -1 "$SQL_DIR/init"/*.sql 2>/dev/null | wc -l)
DATA_N=$(ls -1 "$SQL_DIR/initdata"/*.sql 2>/dev/null | wc -l)
log "    结构脚本 $INIT_N 个、数据脚本 $DATA_N 个"
[ "$INIT_N" -gt 0 ] || die "没从产物里抽到结构脚本"

# 用临时 defaults 文件传凭据，避免口令出现在 ps 命令行
CNF=$(mktemp)
chmod 600 "$CNF"
trap 'rm -f "$CNF"' EXIT
{
    printf '[client]\n'
    printf 'host=%s\nport=%s\nuser=%s\npassword=%s\n' \
        "$OAUTH_DB_HOST" "$OAUTH_DB_PORT" "$OAUTH_DB_USER" "$OAUTH_DB_PASSWORD"
} > "$CNF"

apply_dir() {
    local dir="$1"
    local f
    # 按文件名排序：api_common_entity -> api_common_service -> api_common_system
    # -> api_oauth -> datasource（DDL 用 CREATE TABLE IF NOT EXISTS / 幂等 ALTER，
    # INSERT 用 IGNORE，故重复执行安全）
    for f in $(ls -1 "$dir"/*.sql 2>/dev/null | sort); do
        if err=$(mariadb --defaults-extra-file="$CNF" "$DB_NAME" < "$f" 2>&1); then
            log "    [OK]   $(basename "$f")"
        else
            log "    [失败] $(basename "$f"): $(echo "$err" | head -2 | tr '\n' '|')"
            die "落库失败：$(basename "$f")"
        fi
    done
}

apply_dir "$SQL_DIR/init"
apply_dir "$SQL_DIR/initdata"

TABLE_N=$(mariadb --defaults-extra-file="$CNF" -N -e \
    "select count(*) from information_schema.tables where table_schema='$DB_NAME';")
log "    $DB_NAME 现有 $TABLE_N 张表"

# ---------- 4. 安装 unit ----------
log "==> 4/6 安装 systemd unit"
install -m 0644 "$UNIT_SRC" "$UNIT_DST"
systemctl daemon-reload
systemctl enable "$UNIT_NAME" >/dev/null 2>&1 || true
log "    $UNIT_DST"

# ---------- 5. 重启 ----------
log "==> 5/6 重启服务"
systemctl restart "$UNIT_NAME"

# ---------- 6. 自检 ----------
log "==> 6/6 自检"
mkdir -p "$APP_DIR/logs"
ok=0
for _ in $(seq 1 24); do
    code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 4 \
        http://127.0.0.1:19181/oauth/actuator/health 2>/dev/null || true)
    if [ "$code" = "200" ]; then ok=1; break; fi
    sleep 5
done
log "    unit=$(systemctl is-active "$UNIT_NAME")  健康 HTTP=$code"
for p in 19180 19181 19182 19185; do
    log "    端口 $p 监听=$(ss -ltn 2>/dev/null | grep -c ":$p ")"
done
[ "$ok" = "1" ] || die "服务未在预期时间内健康，请查 $APP_DIR/logs/oauth.out.log"
log "==> 部署完成"
