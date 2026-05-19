@echo off
chcp 65001 >nul

echo ========================================
echo    TFind 微服务一键停止脚本
echo ========================================
echo.

:: 查找并停止所有 Java 进程
echo [信息] 正在停止 TFind 相关服务...
echo.

:: 方法1: 通过窗口标题关闭
echo [1/3] 关闭服务窗口...
taskkill /FI "WINDOWTITLE eq TFind-Gateway*" /T /F 2>nul
taskkill /FI "WINDOWTITLE eq TFind-User-Service*" /T /F 2>nul
taskkill /FI "WINDOWTITLE eq TFind-Toilet-Service*" /T /F 2>nul
taskkill /FI "WINDOWTITLE eq TFind-Web*" /T /F 2>nul

timeout /t 2 /nobreak >nul

:: 方法2: 通过进程名关闭（备选）
echo [2/3] 清理残留进程...
taskkill /FI "WINDOWTITLE eq *tfind-gateway*" /T /F 2>nul
taskkill /FI "WINDOWTITLE eq *tfind-user-service*" /T /F 2>nul
taskkill /FI "WINDOWTITLE eq *tfind-toilet-service*" /T /F 2>nul
taskkill /FI "WINDOWTITLE eq *tfind-web*" /T /F 2>nul

timeout /t 2 /nobreak >nul

:: 显示剩余 Java 进程
echo [3/3] 检查是否还有残留进程...
tasklist | findstr "java" >nul
if %errorlevel% equ 0 (
    echo.
    echo 警告: 仍发现 Java 进程运行
    echo 以下是在运行的 Java 进程:
    tasklist | findstr "java"
    echo.
    echo 如需强制关闭所有 Java 进程，请手动执行:
    echo   taskkill /F /IM java.exe
) else (
    echo 未发现 Java 进程
)

echo.
echo ========================================
echo [完成] 服务停止完成
echo ========================================
echo.

pause
