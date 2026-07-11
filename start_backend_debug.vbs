Set WshShell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
Set mylog = fso.OpenTextFile("G:\work\spring-support-api-starter\vbs_debug.log", 2, True)
mylog.WriteLine Now & " Starting..."
cmd = "java -Dspring.profiles.active=h2 -jar G:\work\spring-support-api-starter\spring-api-support-monitor-starter\target\spring-api-support-monitor-starter-4.0.0.41.jar --server.port=19170 --server.servlet.context-path=/monitor/api > G:\work\spring-support-api-starter\backend2.log 2>&1"
mylog.WriteLine Now & " Cmd: " & cmd
rc = WshShell.Run("cmd.exe /c """ & cmd & """", 0, False)
mylog.WriteLine Now & " RC=" & rc
mylog.Close
