@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo    TFind 微服务一键启动脚本
echo ========================================
echo.

:: 设置项目根目录
set PROJECT_DIR=%~dp0
cd /d "%PROJECT_DIR%\tfind-parent"

:: 检查是否安装了Maven
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到 Maven，请先安装 Maven 并配置环境变量
    pause
    exit /b 1
)

:: 检查是否安装了Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到 Java，请先安装 JDK 并配置环境变量
    pause
    exit /b 1
)

echo [信息] 正在检查环境...
java -version 2>&1 | findstr "version"
mvn -version 2>&1 | findstr "Apache Maven"
echo.

:: 先编译整个项目
echo [信息] 正在编译项目...
echo ========================================
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [错误] 项目编译失败，请检查错误信息
    pause
    exit /b 1
)
echo.

echo ========================================
echo [信息] 开始启动微服务...
echo ========================================
echo.

:: 启动顺序：网关 -> 用户服务 -> 厕所服务 -> Web服务

:: 1. 启动 Gateway
echo [1/4] 启动 Gateway 服务 (端口: 8080)...
start "TFind-Gateway" cmd /k "cd /d %PROJECT_DIR%\tfind-parent\tfind-gateway && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

:: 2. 启动 User Service
echo [2/4] 启动 User Service (端口: 8081)...
start "TFind-User-Service" cmd /k "cd /d %PROJECT_DIR%\tfind-parent\tfind-user-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

:: 3. 启动 Toilet Service
echo [3/4] 启动 Toilet Service (端口: 8082)...
start "TFind-Toilet-Service" cmd /k "cd /d %PROJECT_DIR%\tfind-parent\tfind-toilet-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

:: 4. 启动 Web
echo [4/4] 启动 Web 服务 (端口: 8083)...
start "TFind-Web" cmd /k "cd /d %PROJECT_DIR%\tfind-parent\tfind-web && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo.
echo ========================================
echo [成功] 所有服务正在启动中...
echo ========================================
echo.
echo 服务列表:
echo   - Gateway:        http://localhost:8080
echo   - User Service:   http://localhost:8081
echo   - Toilet Service: http://localhost:8082
echo   - Web 管理后台:   http://localhost:8083
echo.
echo 提示:
echo   - 每个服务会在新窗口中运行
echo   - 关闭窗口可停止对应服务
echo   - 请等待服务完全启动（约30-60秒）
echo.

pause
