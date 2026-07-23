@echo off
cd /d %~dp0
call maven_bin\apache-maven-3.9.6\bin\mvn.cmd clean test -pl ibpms-core
