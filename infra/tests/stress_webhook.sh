#!/usr/bin/env bash

# ==============================================================================
# iBPMS Platform - Webhook API Stress Test 
# Descripción: Lanza 100 peticiones concurrentes POST al webhook simulando
#              la llegada en masa de correos electrónicos.
# Dependencias: curl (GNU)
# ==============================================================================

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

API_URL="http://localhost:8080/api/v1/inbound/email-webhook"
SECRET_TOKEN="SimulatedGraphSecretToken2026"
REQUEST_COUNT=100

echo -e "${YELLOW}[Stress Test] Iniciando ataque de fuerza bruta controlada al Webhook...${NC}"
echo -e "${YELLOW}[Parametros] Peticiones Concurrentes: ${REQUEST_COUNT}${NC}"

PAYLOAD=$(cat <<EOF
{
  "value": [
    {
      "subscriptionId": "11111111-2222-3333-4444-555555555555",
      "changeType": "created",
      "resource": "Users/radicacion@ibpms.local/Messages/AQAQ...",
      "resourceData": {
        "subject": "PRUEBA ESTRÉS: Alerta Crítica del Sistema",
        "bodyPreview": "Este correo es parte de un test de resistencia de Camunda Thread Pool",
        "importance": "high"
      },
      "clientState": "${SECRET_TOKEN}"
    }
  ]
}
EOF
)

# Guardar payload temporal para evitar cat en cada loop
echo "$PAYLOAD" > /tmp/graph_payload.json

# Ejecutar 100 llamadas curl en background (concurrencia bash nativa)
pids=""
for i in $(seq 1 $REQUEST_COUNT)
do
   curl -s -o /dev/null -w "%{http_code}\n" -X POST "${API_URL}" \
    -H "Content-Type: application/json" \
    -H "ClientState: ${SECRET_TOKEN}" \
    -d @/tmp/graph_payload.json &
   
   pids="$pids $!"
done

echo -e "${YELLOW}[Stress Test] Disparo completado. Esperando que el Thread Pool de Camunda resuelva...${NC}"

# Esperar a que terminen todos los procesos
wait $pids

# Limpieza
rm /tmp/graph_payload.json

echo -e "${GREEN}[Stress Test Finalizado] Revisa los logs de Spring Boot para confirmar que no hay cuellos de botella en HikariCP o Camunda Job Executor.${NC}"
exit 0
