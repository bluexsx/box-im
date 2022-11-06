rem 删除旧文件
del /f /s /q  ".\im-platform.jar"
del /f /s /q  ".\im-server.jar"
del /f /s /q  ".\im-ui.zip"
rd  /s /q  ".\im-ui"

rem 编译java项目
call mvn clean package -f ../pom.xml

rem 拷贝jar包
copy  "..\im-platform\target\im-platform.jar"   ".\"
copy  "..\im-server\target\im-server.jar"   ".\"

rem 打包前端资源
call npm run build --prefix  ..\im-ui\
md im-ui
xcopy  "..\im-ui\dist"   ".\im-ui" /e /y 

echo  打包完成..........
pause

