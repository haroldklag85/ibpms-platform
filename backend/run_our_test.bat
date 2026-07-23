@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set PATH=%JAVA_HOME%\bin;%~dp0..\maven\apache-maven-3.9.6\bin;%PATH%
call mvn test -Dtest=FormDesignActiveProtectionIntegrationTest -pl ibpms-core
