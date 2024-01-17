# !/bin/sh
path='/home/'
env='dev'
name='spring-api-support-deepleanring-starter-3.1.0'
pid=`ps aux |grep java |grep $name.jar | grep -v grep | awk '{print $2}'`
echo '进程PID：'$pid
if [ ! "$pid" = "" ]
then
        echo '开始杀掉PID为 '$pid' 的进程！'
        kill -9 $pid
        echo '进程已杀掉！'
else
        echo '没有PID为 '$pid' 的进程'
fi
jarpath=$path$name'.jar'
logspath=$path'start.log'

nohup java -jar   -Dspring.profiles.active=$env  $jarpath > $logspath 2>&1 &
echo '执行'$jarpath'成功，日志目录'$logspath
tail -f  $logspath
