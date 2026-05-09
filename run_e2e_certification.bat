@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo [1/5] Apagando entorno DEV y purgando volumenes
echo ===================================================
docker-compose down -v

echo ===================================================
echo [2/5] Compilando Backend (Forzando UTF-8)
echo ===================================================
cd backend
call ..\maven\apache-maven-3.9.6\bin\mvn.cmd clean package -DskipTests -Dfile.encoding=UTF-8
if %errorlevel% neq 0 (
  echo Error en la compilación de Maven. Abortando.
  exit /b %errorlevel%
)
cd ..

echo ===================================================
echo [3/5] Levantando Infraestructura E2E en Docker
echo ===================================================
docker-compose -f docker-compose.e2e.yml up -d --build

echo ===================================================
echo [4/5] Levantando Backend (E2E) y Frontend Proxy
echo ===================================================
:: Usamos un título de ventana único para matar solo estos procesos al final
echo [INFO] Iniciando Backend en ventana oculta (vía Maven para evitar bug jar:nested)...
start "IBPMS_BACKEND_E2E" /MIN cmd /c "cd backend\ibpms-core && ..\..\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -Dspring-boot.run.profiles=e2e > ..\..\backend_real_log2.txt 2>&1"
:: Polling inteligente: Esperamos hasta que el Backend reporte UP en actuator/health
echo [INFO] Esperando a que el Backend E2E termine su arranque (puede tardar hasta 5 minutos por timeouts de Azure/DMN)...
:wait_backend
curl.exe -s -o nul -w "%%{http_code}" http://localhost:8080/actuator/health | findstr "200" > nul
if errorlevel 1 (
    ping 127.0.0.1 -n 6 > nul
    goto wait_backend
)
echo [INFO] Backend E2E reportado como UP. Procediendo...


cd frontend
echo [INFO] Iniciando Frontend Vite en ventana oculta...
start "IBPMS_FRONTEND_E2E" /MIN cmd /c "npm run dev:e2e"
echo [INFO] Esperando 10 segundos para compilación del proxy frontend...
ping 127.0.0.1 -n 11 > nul

echo ===================================================
echo [5/5] Ejecutando Certificacion E2E (Playwright)
echo ===================================================
call npx playwright test -c playwright.e2e.config.ts
set PLAYWRIGHT_EXIT=%errorlevel%
cd ..

echo ===================================================
echo [LIMPIEZA] Deteniendo procesos locales de prueba...
echo ===================================================
taskkill /FI "WINDOWTITLE eq IBPMS_BACKEND_E2E*" /T /F > nul 2>&1
taskkill /FI "WINDOWTITLE eq IBPMS_FRONTEND_E2E*" /T /F > nul 2>&1
:: Opcional: bajar infraestructura si no se va a revisar despues
:: docker-compose -f docker-compose.e2e.yml down -v

echo ===================================================
if %PLAYWRIGHT_EXIT% equ 0 (
    echo [EXITO] Certificacion E2E superada de forma impecable.
    echo Reporte disponible en: frontend/playwright-e2e-report/index.html
) else (
    echo [ERROR] La suite E2E ha fallado o timeout.
    echo Revisa el reporte en: frontend/playwright-e2e-report/index.html
)
exit /b %PLAYWRIGHT_EXIT%
