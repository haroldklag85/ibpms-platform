@echo off
echo ==============================================================
echo 🚀 Iniciando Entorno de DESARROLLO (Nativo Host-First)
echo ==============================================================

echo [1/3] Levantando Infraestructura en Docker (Postgres, RabbitMQ, Redis)...
docker-compose up -d ibpms-postgres ibpms-rabbitmq ibpms-redis

echo [2/3] Esperando a que los servicios esten saludables...
ping 127.0.0.1 -n 6 > nul

echo [3/3] Iniciando el Backend Nativamente con Maven (Perfil: DEV)...
cd backend\ibpms-core
call ..\..\maven\apache-maven-3.9.6\bin\mvn.cmd clean spring-boot:run -Dspring-boot.run.profiles=dev -Dmaven.test.skip=true

cd ..\..
echo ==============================================================
echo 🏁 Aplicacion finalizada.
echo ==============================================================
pause
