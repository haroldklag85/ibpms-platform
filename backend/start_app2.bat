@echo off
cd /d %~dp0
set JAVA_HOME=C:\Users\HaroltAndr?sG?mezAgu\ProyectoAntigravity\ibpms-platform\backend\maven_install\jdk
set PATH=%JAVA_HOME%\bin;%PATH%
cd ibpms-core
call ..\maven_bin\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run > app.log 2>&1

