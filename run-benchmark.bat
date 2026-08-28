@echo off
chcp 65001 >nul
cd /d "%~dp0\examples\Benchmark"
call run-benchmark.bat
