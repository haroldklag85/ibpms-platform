@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set "PATH=%JAVA_HOME%\bin;C:\Users\HAROLT~1\PROYEC~1\IBPMS-~1\maven\apache-maven-3.9.6\bin;%PATH%"
set MAVEN_OPTS=-Xmx2048m
echo Running DmnArchitectureComplianceTest...
cd /d C:\Users\HAROLT~1\PROYEC~1\IBPMS-~1\backend
call mvn test -Dtest=DmnArchitectureComplianceTest -pl :ibpms-poc > compliance_test_output.log 2>&1
echo EXIT_CODE=%ERRORLEVEL%
