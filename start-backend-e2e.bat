@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo [1/4] Ejecutando Limpieza de Remediacion (Memoria y Puertos)
echo ===================================================
echo Matando procesos Java (puerto 8080)...
taskkill /F /IM java.exe > nul 2>&1

echo Matando procesos Node (puerto 5173/Vite/Playwright)...
taskkill /F /IM node.exe > nul 2>&1

echo ===================================================
echo [2/4] Limpiando Caché (Prevencion ClassNotFoundException)
echo ===================================================
cd /d "%~dp0backend\ibpms-core"
call ..\..\maven\apache-maven-3.9.6\bin\mvn.cmd clean
if %errorlevel% neq 0 (
  echo [ERROR] Fallo en la limpieza de Maven.
  pause
  exit /b %errorlevel%
)

echo ===================================================
echo [3/4] Levantando Servidor con Perfil E2E Estatico
echo ===================================================
echo [INFO] Iniciando backend E2E...
call ..\..\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=e2e" "-Dmaven.test.skip=true"
echo Esperando a que el backend reporte UP (puede tardar ~30s)...
:wait_backend
curl.exe -s -o nul -w "%%{http_code}" http://localhost:8080/actuator/health | findstr "200" > nul
if errorlevel 1 (
    ping 127.0.0.1 -n 6 > nul
    goto wait_backend
)
echo.
echo [EXITO] El servidor Spring Boot Backend esta ARRIBA y conectado a la BD Estatica.
echo Puedes proceder a correr Playwright o npm run dev:e2e
pause
