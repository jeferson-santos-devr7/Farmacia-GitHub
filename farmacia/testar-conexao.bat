@echo off
setlocal
cd /d "%~dp0"
if not exist "config\db.properties" copy "config\db.properties.example" "config\db.properties" >nul
set "JAVA_CMD=java"
if exist "dist\runtime\bin\java.exe" set "JAVA_CMD=%~dp0dist\runtime\bin\java.exe"
"%JAVA_CMD%" -cp "dist\farmacia.jar" br.com.farmacia.TesteConexao
set "RESULTADO=%ERRORLEVEL%"
pause
exit /b %RESULTADO%
