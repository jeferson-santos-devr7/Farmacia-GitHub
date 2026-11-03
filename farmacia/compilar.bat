@echo off
setlocal
cd /d "%~dp0"
where mvn >nul 2>nul
if errorlevel 1 (
  echo Maven nao encontrado. Instale o Apache Maven ou importe pom.xml na IDE.
  pause
  exit /b 1
)
call mvn clean verify
if errorlevel 1 (
  pause
  exit /b 1
)
if not exist dist mkdir dist
copy /y "target\farmacia.jar" "dist\farmacia.jar" >nul
echo Compilacao concluida. Execute executar.bat.
