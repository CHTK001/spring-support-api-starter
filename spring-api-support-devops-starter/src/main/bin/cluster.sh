#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
exec "${BIN_DIR}/script/cluster/cluster.sh" "$@"


