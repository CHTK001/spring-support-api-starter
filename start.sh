#!/bin/sh

# 默认配置
path='/app/'
env='dev'
name='spring-api-support-monitor-starter'
version='1.0.0'
jarpath=$path$name'-'$version'.jar'
logspath=$path'start.log'

# 初始化变量
port=""
xmx=""
xms=""
heap_dump_on_oom=""
heap_dump_path=""

# 从命令行参数中提取参数
while [[ $# -gt 0 ]]; do
    case "$1" in
        --dev)
            env='dev'
            shift 1
            ;;
        --prod)
            env='prod'
            shift 1
            ;;
        --test)
            env='test'
            shift 1
            ;;
        -port)
            port="-Dserver.port=$2"
            shift 2
            ;;
        -xmx)
            xmx="-Xmx$2"
            shift 2
            ;;
        -xms)
            xms="-Xms$2"
            shift 2
            ;;
        -d)
            heap_dump_on_oom="-XX:+HeapDumpOnOutOfMemoryError"
            shift 1
            ;;
        -dp)
            heap_dump_path="-XX:HeapDumpPath=$2"
            heap_dump_on_oom="-XX:+HeapDumpOnOutOfMemoryError"
            shift 2
            ;;
        *)
            echo "未知参数: $1"
            echo "使用方法: $0 [--dev|--prod|--test] [-port PORT] [-xmx SIZE] [-xms SIZE] [-d] [-dp PATH]"
            exit 1
            ;;
    esac
done

echo "启动环境: $env"

# 查找并杀死进程
pid=`ps aux | grep java | grep $name | grep -v grep | awk '{print $2}'`
echo '进程PID：'$pid

if [ ! "$pid" = "" ]; then
    echo '开始杀掉PID为 '$pid' 的进程！'
    kill -9 $pid
    echo '进程已杀掉！'
else
    echo '没有PID为 '$pid' 的进程'
fi

# 构建启动命令
command="nohup java"

# 添加 port 参数（如果存在）
if [ -n "$port" ]; then
    command="$command $port"
fi

# 添加 xmx 参数（如果存在）
if [ -n "$xmx" ]; then
    command="$command $xmx"
fi

# 添加 xms 参数（如果存在）
if [ -n "$xms" ]; then
    command="$command $xms"
fi

# 添加 HeapDumpOnOutOfMemoryError 参数（如果存在）
if [ -n "$heap_dump_on_oom" ]; then
    command="$command $heap_dump_on_oom"
fi

# 添加 HeapDumpPath 参数（如果存在）
if [ -n "$heap_dump_path" ]; then
    command="$command $heap_dump_path"
fi

# 添加通用参数和 JAR 文件路径
command="$command -jar -Dspring.profiles.active=$env $jarpath > $logspath 2>&1 &"

# 执行启动命令
eval $command

echo '执行'$jarpath'成功，日志目录'$logspath
tail -f $logspath
