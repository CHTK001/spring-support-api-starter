#!/bin/bash
# 覆盖解压（不清空目录）
# Usage: ./extract-overwrite.sh [--strip]
# --strip: 跳过压缩包内的父目录，直接解压内容到当前目录

STRIP_MODE=false
for arg in "$@"; do
    case $arg in
        --strip|-s)
            STRIP_MODE=true
            ;;
    esac
done

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ARCHIVE_NAME="${project.artifactId}-${project.version}"

# 支持 zip 和 tar.gz
if [ -f "$SCRIPT_DIR/$ARCHIVE_NAME.zip" ]; then
    ARCHIVE="$SCRIPT_DIR/$ARCHIVE_NAME.zip"
elif [ -f "$SCRIPT_DIR/$ARCHIVE_NAME.tar.gz" ]; then
    ARCHIVE="$SCRIPT_DIR/$ARCHIVE_NAME.tar.gz"
else
    echo "Error: Archive not found: $ARCHIVE_NAME.zip or $ARCHIVE_NAME.tar.gz"
    exit 1
fi

if [ "$STRIP_MODE" = true ]; then
    TARGET_DIR="$SCRIPT_DIR"
    echo "Extracting (overwrite + strip mode): $ARCHIVE"
    if [[ "$ARCHIVE" == *.zip ]]; then
        TMP_DIR=$(mktemp -d)
        unzip -o -q "$ARCHIVE" -d "$TMP_DIR"
        # 移动父目录内的内容到目标目录
        INNER_DIR=$(find "$TMP_DIR" -mindepth 1 -maxdepth 1 -type d | head -1)
        if [ -n "$INNER_DIR" ]; then
            cp -rf "$INNER_DIR"/* "$SCRIPT_DIR/" 2>/dev/null || true
            cp -rf "$INNER_DIR"/.* "$SCRIPT_DIR/" 2>/dev/null || true
        fi
        rm -rf "$TMP_DIR"
    else
        tar -xzf "$ARCHIVE" -C "$SCRIPT_DIR" --strip-components=1
    fi
else
    TARGET_DIR="$SCRIPT_DIR/$ARCHIVE_NAME"
    echo "Extracting (overwrite mode): $ARCHIVE"
    if [[ "$ARCHIVE" == *.zip ]]; then
        unzip -o -q "$ARCHIVE" -d "$SCRIPT_DIR"
    else
        tar -xzf "$ARCHIVE" -C "$SCRIPT_DIR"
    fi
fi

echo "Done. Extracted to: $TARGET_DIR"
