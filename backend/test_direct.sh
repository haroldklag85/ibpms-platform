#!/bin/bash
cd /app/ibpms-core
CP=$(cat cp.txt)
TEST_CP=$(mvn dependency:build-classpath -Dmdep.outputFile=test_cp.txt -DincludeScope=test -q; cat test_cp.txt)
javac -J-Xmx2048m -d target/test-classes -cp "target/classes:$TEST_CP" $(find src/test/java -name "*.java") > test_stdout.log 2>&1
cat test_stdout.log
