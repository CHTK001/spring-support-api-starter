#!/usr/bin/env bash
#
# oauth 部署产物打包（在【开发机】执行）。
#
# 产出 dist/oauth-app.tar.gz，内含：
#   spring-api-support-oauth-starter-<ver>.jar   # 应用
#   lib/                                         # 全部运行期依赖（jar 的 Class-Path 指向它）
#   config/                                      # 外置配置（Class-Path 指向它，含 application*.yml）
#   deploy/                                      # 部署脚本与 systemd unit
#
# 上传并部署（示例）：
#   pscp -batch -pw <pass> dist/oauth-app.tar.gz <user>@<host>:/tmp/oauth-app.tar.gz
#   ssh <user>@<host> "sudo BUNDLE=/tmp/oauth-app.tar.gz bash /home/kali/oauth/app/deploy/deploy.sh"
#
# 注意：首次部署时 /home/kali/oauth/app 还不存在，先把 deploy.sh 单独传上去执行，
#       或在目标机上解包到任意目录再执行（见 deploy/README.md 的「首次部署」一节）。
#
set -euo pipefail

MODULE_DIR=$(cd "$(dirname "$0")/.." && pwd)
DIST="$MODULE_DIR/dist"
JAR=spring-api-support-oauth-starter-4.0.0.42.jar

echo "==> 构建 $JAR"
mvn -f "$MODULE_DIR/pom.xml" clean package -DskipTests

[ -f "$MODULE_DIR/target/$JAR" ] || { echo "[错误] 构建未产出 $JAR" >&2; exit 1; }
[ -d "$MODULE_DIR/target/lib" ] || { echo "[错误] 构建未产出 target/lib" >&2; exit 1; }

echo "==> 打包"
STAGE=$(mktemp -d)
trap 'rm -rf "$STAGE"' EXIT
cp "$MODULE_DIR/target/$JAR" "$STAGE/"
cp -r "$MODULE_DIR/target/lib" "$STAGE/"
cp -r "$MODULE_DIR/target/config" "$STAGE/"
cp -r "$MODULE_DIR/deploy" "$STAGE/"

mkdir -p "$DIST"
tar -czf "$DIST/oauth-app.tar.gz" -C "$STAGE" .

echo "==> 完成：$DIST/oauth-app.tar.gz"
echo "    jar=$(basename "$JAR")  lib=$(ls -1 "$MODULE_DIR/target/lib" | wc -l) 个"
if command -v sha256sum >/dev/null; then
    echo "    sha256=$(sha256sum "$DIST/oauth-app.tar.gz" | cut -c1-64)"
fi
echo "    大小=$(du -h "$DIST/oauth-app.tar.gz" | cut -f1)"
