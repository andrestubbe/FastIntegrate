@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ===============================================================
echo   FastIntegrate JMH Microbenchmark Runner
echo ===============================================================

where mvn >nul 2>nul
if %errorlevel% equ 0 (
    set MVN_CMD=mvn
) else if exist "C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN_CMD=C:\Users\andre\tools\apache-maven-3.9.9\bin\mvn.cmd"
) else (
    set MVN_CMD=
)

if not exist "target\benchmarks.jar" (
    if defined MVN_CMD (
        echo [INFO] Building benchmark JAR first...
        call %MVN_CMD% clean package -DskipTests
    ) else (
        echo [ERROR] target\benchmarks.jar missing and Maven not found.
        pause
        exit /b 1
    )
)

java -jar target\benchmarks.jar -f 1 -wi 2 -i 3 -r 2 -w 2
pause
