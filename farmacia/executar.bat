@echo off
setlocal
cd /d "%~dp0"
set "JAVA_CMD=java"
if exist "dist\runtime\bin\java.exe" set "JAVA_CMD=%~dp0dist\runtime\bin\java.exe"
"%JAVA_CMD%" -version >nul 2>nul
if errorlevel 1 (
  echo Java nao encontrado. Mantenha a pasta dist\runtime ou instale um JDK 17 ou superior.
  pause
  exit /b 1
)
if not exist "config\db.properties" (
  copy "config\db.properties.example" "config\db.properties" >nul
  echo Configuracao criada em config\db.properties.
  echo Confira usuario e senha do MySQL antes de entrar.
)
if not exist "dist\farmacia.jar" (
  echo Arquivo dist\farmacia.jar ausente. Execute compilar.bat primeiro.
  pause
  exit /b 1
)
"%JAVA_CMD%" -jar "dist\farmacia.jar"
if errorlevel 1 pause
