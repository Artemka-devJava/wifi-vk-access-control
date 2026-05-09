@echo off
REM WiFi VK Access Control - Startup Script

setlocal enabledelayedexpansion

echo.
echo ============================================
echo WiFi VK Access Control - Launch
echo ============================================
echo.

REM Проверяем наличие Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java not found! Please install Java 11 or higher.
    pause
    exit /b 1
)

REM Проверяем наличие Maven
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven not found! Please install Maven.
    pause
    exit /b 1
)

echo [OK] Java and Maven found
echo.

REM Переходим в директорию проекта
cd /d "%~dp0"

echo [INFO] Building application...
call mvn clean package -q

if %errorlevel% neq 0 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo [OK] Build successful
echo.
echo [INFO] Starting WiFi VK Access Control Service...
echo.

call mvn spring-boot:run -DskipTests

pause

