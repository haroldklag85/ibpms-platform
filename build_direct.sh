#!/bin/bash
cd /app/ibpms-core
CP=$(cat cp.txt)
javac -d target/classes -cp "target/classes:$CP" $(find src/main/java -name "*.java")
