@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set "PATH=%JAVA_HOME%\bin;C:\Users\HAROLT~1\PROYEC~1\IBPMS-~1\maven\apache-maven-3.9.6\bin;C:\Windows\system32;C:\Windows"
set MAVEN_OPTS=-Xmx2048m
echo Running clean and DMN tests...
cd /d C:\Users\HAROLT~1\PROYEC~1\IBPMS-~1\backend
call mvn clean test "-Dtest=DmnArchitectureComplianceTest,DmnGovernanceControllerTest,DmnSimulationIntegrationTest,DmnValidationIntegrationTest" -pl :ibpms-poc
echo EXIT_CODE=%ERRORLEVEL%
