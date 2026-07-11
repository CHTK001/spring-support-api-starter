Set WshShell = CreateObject("WScript.Shell")
cmd = "cmd.exe /c java @G:\work\spring-support-api-starter\spring-api-support-monitor-starter\target\backend.args > G:\work\spring-support-api-starter\backend.log 2>&1"
WshShell.Run cmd, 0, False
