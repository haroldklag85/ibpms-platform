@echo off
echo ==============================================================
echo 🧪 Iniciando Entorno de PRUEBAS E2E (Nativo Host-First)
echo ==============================================================

echo [1/3] Levantando Infraestructura Aislada en Docker E2E...
docker-compose -f docker-compose.e2e.yml up -d

echo [2/3] Esperando a que Camunda y la BD esten disponibles...
ping 127.0.0.1 -n 11 > nul

echo [3/3] Iniciando el Backend Nativamente con Maven (Perfil: E2E)...
cd backend\ibpms-core
call ..\..\maven\apache-maven-3.9.6\bin\mvn.cmd clean spring-boot:run -Dspring-boot.run.profiles=e2e -Dmaven.test.skip=true

cd ..\..
echo ==============================================================
echo 🏁 Servidor E2E finalizado.
echo ==============================================================
pause
