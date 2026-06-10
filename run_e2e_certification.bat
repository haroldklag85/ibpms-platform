@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo [1/5] Apagando entorno DEV y purgando volumenes
echo ===================================================
docker-compose down -v
docker-compose -f docker-compose.e2e.yml down -v

echo ===================================================
echo [2/5] Compilando Backend en WSL (Para máximo rendimiento)
echo ===================================================
:: @Traceability: US-005, CA-15
call wsl bash -c "cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/backend && mvn clean compile -Dfile.encoding=UTF-8"
if %errorlevel% neq 0 (
  echo Error en la compilación de Maven en WSL. Abortando.
  exit /b %errorlevel%
)

echo ===================================================
echo [3/5] Levantando Infraestructura E2E en Docker
echo ===================================================
docker-compose -f docker-compose.e2e.yml up -d --build

echo ===================================================
echo [4/5] Levantando Backend (E2E) y Frontend Proxy
echo ===================================================
echo [INFO] Iniciando Backend en WSL (ventana oculta)...
:: @Traceability: US-005, CA-15
start "IBPMS_BACKEND_E2E" /MIN wsl bash -c "cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core && mvn spring-boot:run -Dspring-boot.run.profiles=e2e -Dmaven.test.skip=true > /home/haroltandrsgmezagu/proyectos/ibpms-platform/backend_real_log2.txt 2>&1"
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
echo [INFO] Iniciando Frontend Vite en ventana oculta (vía WSL)...
:: @Traceability: US-005, CA-15
start "IBPMS_FRONTEND_E2E" /MIN wsl bash -c "cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend && npm run dev:e2e"
echo [INFO] Esperando 10 segundos para compilación del proxy frontend...
ping 127.0.0.1 -n 11 > nul

echo ===================================================
echo [5/5] Ejecutando Certificacion E2E (Playwright vía WSL)
echo ===================================================
:: @Traceability: US-005, CA-15
call wsl bash -c "export PLAYWRIGHT_HOST_PLATFORM_OVERRIDE=ubuntu24.04-x64 && cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend && npx playwright test e2e/certification/us005-bpmn-modeler-persistence.e2e.spec.ts -c playwright.e2e.config.ts"
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
