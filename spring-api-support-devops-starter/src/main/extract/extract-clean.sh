#!/bin/bash
# 清空同名目录后解压
# Usage: ./extract-clean.sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ARCHIVE_NAME="${project.artifactId}-${project.version}"

# 支持 zip 和 tar.gz
if [ -f "$SCRIPT_DIR/$ARCHIVE_NAME.zip" ]; then
    ARCHIVE="$SCRIPT_DIR/$ARCHIVE_NAME.zip"
    EXTRACT_CMD="unzip -q"
elif [ -f "$SCRIPT_DIR/$ARCHIVE_NAME.tar.gz" ]; then
    ARCHIVE="$SCRIPT_DIR/$ARCHIVE_NAME.tar.gz"
    EXTRACT_CMD="tar -xzf"
else
    echo "Error: Archive not found: $ARCHIVE_NAME.zip or $ARCHIVE_NAME.tar.gz"
    exit 1
fi

TARGET_DIR="$SCRIPT_DIR/$ARCHIVE_NAME"

echo "Cleaning directory: $TARGET_DIR"
if [ -d "$TARGET_DIR" ]; then
    rm -rf "$TARGET_DIR"
fi

echo "Extracting: $ARCHIVE"
if [[ "$ARCHIVE" == *.zip ]]; then
    unzip -q "$ARCHIVE" -d "$SCRIPT_DIR"
else
    tar -xzf "$ARCHIVE" -C "$SCRIPT_DIR"
fi

echo "Done. Extracted to: $TARGET_DIR"
