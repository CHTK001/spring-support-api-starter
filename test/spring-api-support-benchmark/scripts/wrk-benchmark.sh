#!/usr/bin/env bash
# wrk 负载测试脚本 — Rust vs Tomcat 吞吐量对比
# 需要先安装 wrk: https://github.com/wg/wrk
#
# 用法:
#   bash wrk-benchmark.sh              # 运行所有测试
#   bash wrk-benchmark.sh rust         # 只测试 Rust
#   bash wrk-benchmark.sh tomcat       # 只测试 Tomcat

set -euo pipefail

RUST_URL="${RUST_URL:-http://localhost:18081}"
TOMCAT_URL="${TOMCAT_URL:-http://localhost:18082}"
DURATION="${DURATION:-30}"
CONCURRENCY_LEVELS="${CONCURRENCY_LEVELS:-1 4 8 16 32 64 128}"
SCRIPTS_DIR="$(cd "$(dirname "$0")" && pwd)"
REPORT_DIR="${SCRIPTS_DIR}/../reports"
mkdir -p "${REPORT_DIR}"

run_wrk() {
    local label="$1"
    local url="$2"
    local concurrency="$3"
    local endpoint="$4"
    local report_file="${REPORT_DIR}/wrk-${label}-c${concurrency}-$(echo "${endpoint}" | tr '/' '_').txt"

    echo "=== [${label}] wrk -c${concurrency} -d${DURATION}s ${url}${endpoint} ==="
    wrk -c"${concurrency}" -d"${DURATION}" -t"$(nproc)" \
        "${url}${endpoint}" 2>&1 | tee "${report_file}"
    echo ""
}

# 测试端点列表
declare -A ENDPOINTS
ENDPOINTS["health"]="/api/health"
ENDPOINTS["users"]="/api/users"
ENDPOINTS["echo"]="/api/echo?msg=hello"
ENDPOINTS["delay_100ms"]="/api/delay/100"

TARGET="${1:-all}"

for concurrency in ${CONCURRENCY_LEVELS}; do
    for endpoint_name in "${!ENDPOINTS[@]}"; do
        endpoint="${ENDPOINTS[$endpoint_name]}"
        if [[ "$TARGET" == "all" || "$TARGET" == "rust" ]]; then
            run_wrk "rust" "${RUST_URL}" "${concurrency}" "${endpoint}"
        fi
        if [[ "$TARGET" == "all" || "$TARGET" == "tomcat" ]]; then
            run_wrk "tomcat" "${TOMCAT_URL}" "${concurrency}" "${endpoint}"
        fi
    done
done

echo "=== 所有测试完成！报告目录: ${REPORT_DIR} ==="
