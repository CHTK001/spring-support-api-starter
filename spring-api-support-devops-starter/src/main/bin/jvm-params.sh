#!/usr/bin/env sh

set -eu

BIN_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
. "${BIN_DIR}/script/linux/common.sh"

usage() {
  cat <<EOF
用法:
  jvm-params.sh [版本] [-a] [-c 分类]

参数:
  版本          指定 Java 版本（8、17、21、25），不指定则显示所有版本
  -a, --all     显示所有参数（包括已废弃的）
  -c, --category 分类  按分类筛选（memory、gc、performance、debug、other）
  -h, --help    显示帮助信息

说明:
  - 默认只显示当前版本推荐的参数
  - 使用 -a 可查看所有参数（包括已废弃的）
  - 使用 -c 可按分类查看参数
EOF
}

# 打印表格行（参数名、中文名、用法、描述、适用版本）
print_table_row() {
  var_param="${1}"
  var_name="${2}"
  var_usage="${3}"
  var_desc="${4}"
  var_versions="${5}"
  
  # 使用更宽的列宽以适应中文显示
  # 参数名(40) | 中文名称(18) | 用法(30) | 描述(60) | 适用版本(12)
  printf "| %-40s | %-18s | %-30s | %-60s | %-12s |\n" \
    "${var_param}" \
    "${var_name}" \
    "${var_usage}" \
    "${var_desc}" \
    "${var_versions}"
}

# 打印表格分隔线
print_table_separator() {
  echo "|------------------------------------------|------------------|------------------------------|--------------------------------------------------------------|--------------|"
}

# 打印表格头
print_table_header() {
  print_table_separator
  printf "| %-40s | %-18s | %-30s | %-60s | %-12s |\n" \
    "参数名" \
    "中文名称" \
    "用法" \
    "描述" \
    "适用版本"
  print_table_separator
}

# 打印表格尾
print_table_footer() {
  print_table_separator
}

# 显示 Java 8 参数
show_java8_params() {
  echo ""
  echo "=========================================="
  echo "Java 8 JVM 参数"
  echo "=========================================="
  print_table_header
  
  # 内存相关
  print_table_row \
    "-Xms<size>" \
    "初始堆内存" \
    "-Xms512m" \
    "设置 JVM 初始堆内存大小" \
    "8+"
  
  print_table_row \
    "-Xmx<size>" \
    "最大堆内存" \
    "-Xmx2g" \
    "设置 JVM 最大堆内存大小" \
    "8+"
  
  print_table_row \
    "-Xss<size>" \
    "线程栈大小" \
    "-Xss1m" \
    "设置每个线程的栈大小" \
    "8+"
  
  print_table_row \
    "-XX:MetaspaceSize=<size>" \
    "元空间初始大小" \
    "-XX:MetaspaceSize=256m" \
    "设置元空间初始大小（Java 8 引入，替代 PermGen）" \
    "8+"
  
  print_table_row \
    "-XX:MaxMetaspaceSize=<size>" \
    "元空间最大大小" \
    "-XX:MaxMetaspaceSize=512m" \
    "设置元空间最大大小" \
    "8+"
  
  print_table_row \
    "-XX:+UseCompressedOops" \
    "压缩普通对象指针" \
    "-XX:+UseCompressedOops" \
    "在 64 位 JVM 上压缩对象指针，节省内存" \
    "8+"
  
  print_table_row \
    "-XX:+UseCompressedClassPointers" \
    "压缩类指针" \
    "-XX:+UseCompressedClassPointers" \
    "压缩类指针，进一步减少内存占用" \
    "8+"
  
  # GC 相关
  print_table_row \
    "-XX:+UseG1GC" \
    "启用 G1 垃圾收集器" \
    "-XX:+UseG1GC" \
    "启用 G1 垃圾收集器（推荐用于大堆内存）" \
    "8+"
  
  print_table_row \
    "-XX:MaxGCPauseMillis=<ms>" \
    "最大 GC 暂停时间" \
    "-XX:MaxGCPauseMillis=200" \
    "设置 G1 GC 最大暂停时间目标（毫秒）" \
    "8+"
  
  print_table_row \
    "-XX:+UseConcMarkSweepGC" \
    "启用 CMS 收集器" \
    "-XX:+UseConcMarkSweepGC" \
    "启用并发标记清除垃圾收集器（Java 14 已废弃）" \
    "8-13"
  
  print_table_row \
    "-XX:+UseParallelGC" \
    "启用并行 GC" \
    "-XX:+UseParallelGC" \
    "启用并行垃圾收集器（适合吞吐量优先场景）" \
    "8+"
  
  # 性能相关
  print_table_row \
    "-XX:+HeapDumpOnOutOfMemoryError" \
    "OOM 时生成堆转储" \
    "-XX:+HeapDumpOnOutOfMemoryError" \
    "发生 OOM 时自动生成堆转储文件" \
    "8+"
  
  print_table_row \
    "-XX:HeapDumpPath=<path>" \
    "堆转储文件路径" \
    "-XX:HeapDumpPath=/tmp/heapdump.hprof" \
    "指定堆转储文件的保存路径" \
    "8+"
  
  print_table_row \
    "-XX:+PrintGCDetails" \
    "打印详细 GC 信息" \
    "-XX:+PrintGCDetails" \
    "打印详细的垃圾收集信息（Java 9+ 已废弃，使用 -Xlog:gc）" \
    "8-8"
  
  print_table_row \
    "-XX:+PrintGCDateStamps" \
    "打印 GC 时间戳" \
    "-XX:+PrintGCDateStamps" \
    "在 GC 日志中包含时间戳（Java 9+ 已废弃）" \
    "8-8"
  
  # 其他
  print_table_row \
    "-Duser.timezone=<tz>" \
    "设置时区" \
    "-Duser.timezone=Asia/Shanghai" \
    "设置 JVM 默认时区" \
    "8+"
  
  print_table_row \
    "-Dfile.encoding=<encoding>" \
    "设置文件编码" \
    "-Dfile.encoding=UTF-8" \
    "设置 JVM 默认文件编码" \
    "8+"
  
  print_table_footer
}

# 显示 Java 17 参数
show_java17_params() {
  echo ""
  echo "=========================================="
  echo "Java 17 JVM 参数"
  echo "=========================================="
  print_table_header
  
  # 内存相关（继承 Java 8）
  print_table_row \
    "-Xms<size>" \
    "初始堆内存" \
    "-Xms512m" \
    "设置 JVM 初始堆内存大小" \
    "8+"
  
  print_table_row \
    "-Xmx<size>" \
    "最大堆内存" \
    "-Xmx2g" \
    "设置 JVM 最大堆内存大小" \
    "8+"
  
  print_table_row \
    "-XX:+UseCompressedOops" \
    "压缩普通对象指针" \
    "-XX:+UseCompressedOops" \
    "在 64 位 JVM 上压缩对象指针，节省内存" \
    "8+"
  
  print_table_row \
    "-XX:+UseCompressedClassPointers" \
    "压缩类指针" \
    "-XX:+UseCompressedClassPointers" \
    "压缩类指针，进一步减少内存占用" \
    "8+"
  
  # GC 相关（新增）
  print_table_row \
    "-XX:+UseG1GC" \
    "启用 G1 垃圾收集器" \
    "-XX:+UseG1GC" \
    "启用 G1 垃圾收集器（推荐用于大堆内存）" \
    "8+"
  
  print_table_row \
    "-XX:+UseZGC" \
    "启用 ZGC 收集器" \
    "-XX:+UseZGC" \
    "启用 ZGC 低延迟垃圾收集器（需要 Linux/x64）" \
    "11+"
  
  print_table_row \
    "-XX:+UseShenandoahGC" \
    "启用 Shenandoah GC" \
    "-XX:+UseShenandoahGC" \
    "启用 Shenandoah 低延迟垃圾收集器" \
    "12+"
  
  # 日志相关（统一日志）
  print_table_row \
    "-Xlog:gc*" \
    "统一 GC 日志" \
    "-Xlog:gc*:file=gc.log:time" \
    "使用统一日志框架输出 GC 信息（替代 -XX:+PrintGCDetails）" \
    "9+"
  
  print_table_row \
    "-Xlog:gc:file=<path>" \
    "GC 日志文件" \
    "-Xlog:gc:file=gc.log:time:level=info" \
    "将 GC 日志输出到指定文件" \
    "9+"
  
  # 性能相关
  print_table_row \
    "-XX:+HeapDumpOnOutOfMemoryError" \
    "OOM 时生成堆转储" \
    "-XX:+HeapDumpOnOutOfMemoryError" \
    "发生 OOM 时自动生成堆转储文件" \
    "8+"
  
  print_table_row \
    "-XX:HeapDumpPath=<path>" \
    "堆转储文件路径" \
    "-XX:HeapDumpPath=/tmp/heapdump.hprof" \
    "指定堆转储文件的保存路径" \
    "8+"
  
  # 其他
  print_table_row \
    "-Duser.timezone=<tz>" \
    "设置时区" \
    "-Duser.timezone=Asia/Shanghai" \
    "设置 JVM 默认时区" \
    "8+"
  
  print_table_row \
    "-Dfile.encoding=<encoding>" \
    "设置文件编码" \
    "-Dfile.encoding=UTF-8" \
    "设置 JVM 默认文件编码" \
    "8+"
  
  print_table_footer
}

# 显示 Java 21 参数
show_java21_params() {
  echo ""
  echo "=========================================="
  echo "Java 21 JVM 参数"
  echo "=========================================="
  print_table_header
  
  # 内存相关
  print_table_row \
    "-Xms<size>" \
    "初始堆内存" \
    "-Xms512m" \
    "设置 JVM 初始堆内存大小" \
    "8+"
  
  print_table_row \
    "-Xmx<size>" \
    "最大堆内存" \
    "-Xmx2g" \
    "设置 JVM 最大堆内存大小" \
    "8+"
  
  print_table_row \
    "-XX:+UseCompressedOops" \
    "压缩普通对象指针" \
    "-XX:+UseCompressedOops" \
    "在 64 位 JVM 上压缩对象指针，节省内存" \
    "8+"
  
  print_table_row \
    "-XX:+UseCompressedClassPointers" \
    "压缩类指针" \
    "-XX:+UseCompressedClassPointers" \
    "压缩类指针，进一步减少内存占用" \
    "8+"
  
  # GC 相关
  print_table_row \
    "-XX:+UseG1GC" \
    "启用 G1 垃圾收集器" \
    "-XX:+UseG1GC" \
    "启用 G1 垃圾收集器（推荐用于大堆内存）" \
    "8+"
  
  print_table_row \
    "-XX:+UseZGC" \
    "启用 ZGC 收集器" \
    "-XX:+UseZGC" \
    "启用 ZGC 低延迟垃圾收集器（生产就绪）" \
    "11+"
  
  print_table_row \
    "-XX:+UseShenandoahGC" \
    "启用 Shenandoah GC" \
    "-XX:+UseShenandoahGC" \
    "启用 Shenandoah 低延迟垃圾收集器" \
    "12+"
  
  print_table_row \
    "-XX:+UseG1GC -XX:G1HeapRegionSize=<size>" \
    "G1 堆区域大小" \
    "-XX:G1HeapRegionSize=16m" \
    "设置 G1 堆区域大小（影响 GC 性能）" \
    "8+"
  
  # 性能相关
  print_table_row \
    "-XX:+HeapDumpOnOutOfMemoryError" \
    "OOM 时生成堆转储" \
    "-XX:+HeapDumpOnOutOfMemoryError" \
    "发生 OOM 时自动生成堆转储文件" \
    "8+"
  
  print_table_row \
    "-XX:HeapDumpPath=<path>" \
    "堆转储文件路径" \
    "-XX:HeapDumpPath=/tmp/heapdump.hprof" \
    "指定堆转储文件的保存路径" \
    "8+"
  
  # 日志相关
  print_table_row \
    "-Xlog:gc*" \
    "统一 GC 日志" \
    "-Xlog:gc*:file=gc.log:time" \
    "使用统一日志框架输出 GC 信息" \
    "9+"
  
  # 其他
  print_table_row \
    "-Duser.timezone=<tz>" \
    "设置时区" \
    "-Duser.timezone=Asia/Shanghai" \
    "设置 JVM 默认时区" \
    "8+"
  
  print_table_row \
    "-Dfile.encoding=<encoding>" \
    "设置文件编码" \
    "-Dfile.encoding=UTF-8" \
    "设置 JVM 默认文件编码" \
    "8+"
  
  print_table_footer
}

# 显示 Java 25 参数
show_java25_params() {
  echo ""
  echo "=========================================="
  echo "Java 25 JVM 参数"
  echo "=========================================="
  print_table_header
  
  # 内存相关（新增紧凑对象头）
  print_table_row \
    "-Xms<size>" \
    "初始堆内存" \
    "-Xms512m" \
    "设置 JVM 初始堆内存大小" \
    "8+"
  
  print_table_row \
    "-Xmx<size>" \
    "最大堆内存" \
    "-Xmx2g" \
    "设置 JVM 最大堆内存大小" \
    "8+"
  
  print_table_row \
    "-XX:+UseCompactObjectHeaders" \
    "紧凑对象头" \
    "-XX:+UseCompactObjectHeaders" \
    "使用紧凑对象头格式，减少对象头大小（Java 25 新特性）" \
    "25+"
  
  print_table_row \
    "-XX:+UseCompressedOops" \
    "压缩普通对象指针" \
    "-XX:+UseCompressedOops" \
    "在 64 位 JVM 上压缩对象指针，节省内存" \
    "8+"
  
  print_table_row \
    "-XX:+UseCompressedClassPointers" \
    "压缩类指针" \
    "-XX:+UseCompressedClassPointers" \
    "压缩类指针，进一步减少内存占用" \
    "8+"
  
  # GC 相关
  print_table_row \
    "-XX:+UseG1GC" \
    "启用 G1 垃圾收集器" \
    "-XX:+UseG1GC" \
    "启用 G1 垃圾收集器（推荐用于大堆内存）" \
    "8+"
  
  print_table_row \
    "-XX:+UseZGC" \
    "启用 ZGC 收集器" \
    "-XX:+UseZGC" \
    "启用 ZGC 低延迟垃圾收集器（生产就绪）" \
    "11+"
  
  print_table_row \
    "-XX:+UseShenandoahGC" \
    "启用 Shenandoah GC" \
    "-XX:+UseShenandoahGC" \
    "启用 Shenandoah 低延迟垃圾收集器" \
    "12+"
  
  # 模块系统相关
  print_table_row \
    "--add-modules <modules>" \
    "添加模块" \
    "--add-modules java.base,java.sql" \
    "添加指定的 Java 模块到模块路径" \
    "9+"
  
  print_table_row \
    "--enable-preview" \
    "启用预览特性" \
    "--enable-preview" \
    "启用 Java 预览特性（实验性功能）" \
    "12+"
  
  # 性能相关
  print_table_row \
    "-XX:+HeapDumpOnOutOfMemoryError" \
    "OOM 时生成堆转储" \
    "-XX:+HeapDumpOnOutOfMemoryError" \
    "发生 OOM 时自动生成堆转储文件" \
    "8+"
  
  print_table_row \
    "-XX:HeapDumpPath=<path>" \
    "堆转储文件路径" \
    "-XX:HeapDumpPath=/tmp/heapdump.hprof" \
    "指定堆转储文件的保存路径" \
    "8+"
  
  # 日志相关
  print_table_row \
    "-Xlog:gc*" \
    "统一 GC 日志" \
    "-Xlog:gc*:file=gc.log:time" \
    "使用统一日志框架输出 GC 信息" \
    "9+"
  
  # 其他
  print_table_row \
    "-Duser.timezone=<tz>" \
    "设置时区" \
    "-Duser.timezone=Asia/Shanghai" \
    "设置 JVM 默认时区" \
    "8+"
  
  print_table_row \
    "-Dfile.encoding=<encoding>" \
    "设置文件编码" \
    "-Dfile.encoding=UTF-8" \
    "设置 JVM 默认文件编码" \
    "8+"
  
  print_table_footer
}

# 主逻辑
SHOW_VERSION=""
SHOW_ALL="false"
CATEGORY=""

while [ $# -gt 0 ]; do
  case "$1" in
    8|17|21|25)
      SHOW_VERSION="$1"
      shift
      ;;
    -a|--all)
      SHOW_ALL="true"
      shift
      ;;
    -c|--category)
      CATEGORY="${2:-}"
      if [ -z "${CATEGORY}" ]; then
        die "--category 需要指定分类，例如: -c memory"
      fi
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      warn "未知参数: $1"
      usage
      exit 1
      ;;
  esac
done

# 根据版本显示参数
if [ -z "${SHOW_VERSION}" ]; then
  # 显示所有版本
  show_java8_params
  show_java17_params
  show_java21_params
  show_java25_params
else
  case "${SHOW_VERSION}" in
    8)
      show_java8_params
      ;;
    17)
      show_java17_params
      ;;
    21)
      show_java21_params
      ;;
    25)
      show_java25_params
      ;;
    *)
      die "不支持的 Java 版本: ${SHOW_VERSION}（支持: 8, 17, 21, 25）"
      ;;
  esac
fi

echo ""
info "提示: 使用 -a 参数可查看所有参数（包括已废弃的）"
info "提示: 使用 -c 参数可按分类查看参数"

