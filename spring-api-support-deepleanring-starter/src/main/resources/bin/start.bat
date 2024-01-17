@echo off
setlocal enabledelayedexpansion
chcp 65001
echo=
echo springboot服务运行脚本
echo=
echo 注意运行命令前需要配置【java环境变量】
echo=
set /p port=请输入要杀死的端口号[0代表不执行操作]:
set /p jarName=请输入要运行的包名 [xxx.jar]:
set /p memoSize=请输入要分配的内存大小 [128m]:
set /p newPort=请输入要指定运行的端口号:
echo=
echo 包名======================================》 !jarName!
echo 端口名====================================》 !newPort!
echo 内存大小==================================》 !memoSize!
echo=
set pid=0
if "!port!" == "0" (
    echo 不杀死指定端口的进程
) else (
    echo 开始杀死占用指定端口 !port! 进程
    for /f "tokens=5" %%a in ('netstat -ano ^| find ":%port%"') do (
    	set pid=%%a
    )
    echo pid进程ID为：!pid!
    if "!pid!"=="0" (
            echo 端口号【!port!】没有占用
    ) else (
    		taskkill /pid !pid! -t -f
            echo 端口号【!port!】相关进程已杀死
    )
    set pid=0
    )
)
echo=
echo 启动命令==============》 java -jar -Xmx!memoSize! -Xms!memoSize! !jarName! --server.port=!newPort!
echo=
echo=
java -jar -Xmx!memoSize! -Xms!memoSize! !jarName! --server.port=!newPort!
pause