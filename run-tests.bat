@echo off
echo ==============================================================
echo 🛡️ Ejecutando Suite de Pruebas Unitarias y de Integracion
echo ==============================================================

cd backend\ibpms-core
echo [+] Ejecutando tests con Maven nativo...
call ..\..\maven\apache-maven-3.9.6\bin\mvn.cmd clean test

cd ..\..
echo ==============================================================
echo 🏁 Pruebas finalizadas. Revisa los logs arriba.
echo ==============================================================
pause
