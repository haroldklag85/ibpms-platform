@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set "PATH=%JAVA_HOME%\bin;C:\Users\HAROLT~1\PROYEC~1\IBPMS-~1\maven\apache-maven-3.9.6\bin;%PATH%"
cd /d C:\Users\HAROLT~1\PROYEC~1\IBPMS-~1\backend
echo Running tests using short paths...
call mvn test -Dtest=FormDesignActiveProtectionIntegrationTest -pl :ibpms-poc
