$env:JAVA_HOME="C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\maven_install\jdk"
$env:PATH="$env:JAVA_HOME\bin;C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\maven\apache-maven-3.9.6\bin;$env:PATH"
cd C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core
mvn spring-boot:run > backend_running.log 2>&1
