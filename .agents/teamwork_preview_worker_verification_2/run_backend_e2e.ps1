$env:JAVA_HOME="C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\maven_install\jdk"
$env:PATH="$env:JAVA_HOME\bin;C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\maven\apache-maven-3.9.6\bin;$env:PATH"
$env:POSTGRES_PORT="5433"
$env:REDIS_PORT="6380"
$env:RABBITMQ_PORT="5673"
$env:RABBITMQ_PORT_15672="15673"

cd C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core
mvn spring-boot:run > backend_running_e2e.log 2>&1
