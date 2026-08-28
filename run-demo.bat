@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ===============================================================
echo   Building and launching FastIntegrate Hero Demo...
echo ===============================================================

where mvn >nul 2>nul
if %errorlevel% equ 0 (
    set MVN_CMD=mvn
) else if exist "C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN_CMD=C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd"
) else (
    echo [ERROR] Maven not found.
    pause
    exit /b 1
)

call %MVN_CMD% test-compile exec:java "-Dexec.mainClass=fastintegrate.Demo" "-Dexec.classpathScope=test"
pause
