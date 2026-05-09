@echo off
echo ===================================================
echo [1/3] Apagando DEV y purgando volumenes...
echo ===================================================
docker-compose down -v
if %errorlevel% neq 0 (
  echo Error apagando los contenedores.
  exit /b %errorlevel%
)

echo ===================================================
echo [2/3] Compilando el backend (evadiendo test y forzando UTF-8)...
echo ===================================================
cd backend
call ..\maven\apache-maven-3.9.6\bin\mvn.cmd clean package -DskipTests -Dfile.encoding=UTF-8
if %errorlevel% neq 0 (
  echo Error en la compilación de Maven.
  cd ..
  exit /b %errorlevel%
)
cd ..

echo ===================================================
echo [3/3] Levantando UAT / E2E (docker-compose.e2e.yml)...
echo ===================================================
docker-compose -f docker-compose.e2e.yml up -d --build
if %errorlevel% neq 0 (
  echo Error levantando el ambiente E2E.
  exit /b %errorlevel%
)

echo ===================================================
echo AMBIENTE E2E LISTO Y EJECUTANDOSE.
echo ===================================================
