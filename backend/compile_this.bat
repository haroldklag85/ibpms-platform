@echo off
set JAVA_HOME=C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\maven_install\jdk
set PATH=%JAVA_HOME%\bin;C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\maven_bin\apache-maven-3.9.6\bin;%PATH%
call mvn compile -pl ibpms-core
