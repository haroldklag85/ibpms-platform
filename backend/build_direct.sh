#!/bin/bash
cd /app/ibpms-core
mkdir -p target/classes
CP=$(cat cp.txt)
javac -J-Xmx2048m -d target/classes -cp "target/classes:$CP" $(find src/main/java -name "*.java") > compile_stdout.log 2>&1
