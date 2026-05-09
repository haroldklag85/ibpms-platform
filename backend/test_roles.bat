@echo off
set MAVEN_OPTS=-Dfile.encoding=UTF-8
call ..\maven\apache-maven-3.9.6\bin\mvn.cmd clean test -pl ibpms-core -Dtest=RoleHierarchyServiceTest,RoleServiceIntegrationTest
