@echo off
cd /d %~dp0
set JAVA_HOME=%CD%\maven_install\jdk
set PATH=%JAVA_HOME%\bin;%CD%\maven_install\apache-maven-3.9.6\bin;%PATH%
call mvn clean test -pl ibpms-core
