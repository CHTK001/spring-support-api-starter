#!/usr/bin/env bash
# ab (Apache Bench) 负载测试脚本 — Rust vs Tomcat 延迟分布对比
# 需要先安装 apache2-utils / httpd-tools
#
# 用法:
#   bash ab-benchmark.sh               # 运行所有测试
#   bash ab-benchmark.sh rust          # 只测试 Rust
#   bash ab-benchmark.sh tomcat        # 只测试 Tomcat

set -euo pipefail

RUST_URL="${RUST_URL:-http://localhost:18081}"
TOMCAT_URL="${TOMCAT_URL:-http://localhost:18082}"
REQUESTS="${REQUESTS:-10000}"
CONCURRENCY_LEVELS="${CONCURRENCY_LEVELS:-1 4 8 16 32 64}"
SCRIPTS_DIR="$(cd "$(dirname "$0")" && pwd)"
REPORT_DIR="${SCRIPTS_DIR}/../reports"
mkdir -p "${REPORT_DIR}"

run_ab() {
    local label="$1"
    local url="$2"
    local concurrency="$3"
    local endpoint="$4"
    local report_file="${REPORT_DIR}/ab-${label}-c${concurrency}-$(echo "${endpoint}" | tr '/' '_').txt"

    echo "=== [${label}] ab -c${concurrency} -n${REQUESTS} ${url}${endpoint} ==="
    ab -c "${concurrency}" -n "${REQUESTS}" \
        "${url}${endpoint}" 2>&1 | tee "${report_file}"
    echo ""
}

declare -A ENDPOINTS
ENDPOINTS["health"]="/api/health"
ENDPOINTS["users"]="/api/users"
ENDPOINTS["echo"]="/api/echo?msg=hello"

TARGET="${1:-all}"

for concurrency in ${CONCURRENCY_LEVELS}; do
    for endpoint_name in "${!ENDPOINTS[@]}"; do
        endpoint="${ENDPOINTS[$endpoint_name]}"
        if [[ "$TARGET" == "all" || "$TARGET" == "rust" ]]; then
            run_ab "rust" "${RUST_URL}" "${concurrency}" "${endpoint}"
        fi
        if [[ "$TARGET" == "all" || "$TARGET" == "tomcat" ]]; then
            run_ab "tomcat" "${TOMCAT_URL}" "${concurrency}" "${endpoint}"
        fi
    done
done

echo "=== 所有测试完成！报告目录: ${REPORT_DIR} ==="
