#!/usr/bin/env bash

# ==============================================================================
# iBPMS Platform - MS Graph Webhook Simulator
# Descripción: Simula el payload que O365 / MS Graph envía al webhook del backend
#              cuando ingresa un nuevo correo electrónico al buzón de radicación.
# ==============================================================================

set -e

# Configuración de colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # Sin color

API_URL="http://localhost:8080/api/v1/inbound/email-webhook"
SECRET_TOKEN="SimulatedGraphSecretToken2026"

echo -e "${YELLOW}[Simulator] Generando payload de MS Graph (Incoming Email)...${NC}"

# Payload JSON masivo simulando la estructura O365 Graph API
PAYLOAD=$(cat <<EOF
{
  "value": [
    {
      "subscriptionId": "11111111-2222-3333-4444-555555555555",
      "subscriptionExpirationDateTime": "2026-12-31T23:59:59.0000000Z",
      "changeType": "created",
      "resource": "Users/radicacion@ibpms.local/Messages/AQAQ...",
      "resourceData": {
        "@odata.type": "#Microsoft.Graph.Message",
        "@odata.id": "Users/radicacion@ibpms.local/Messages/AQAQ...",
        "@odata.etag": "W/\"CQAAABYAAAAB...\"",
        "id": "AQAQ...",
        "subject": "URGENTE: Reclamo Cobro Indebido - Cliente VIP",
        "sender": {
          "emailAddress": {
            "name": "Cliente Molesto",
            "address": "cliente.enojado@foo.com"
          }
        },
        "from": {
          "emailAddress": {
            "name": "Cliente Molesto",
            "address": "cliente.enojado@foo.com"
          }
        },
        "toRecipients": [
          {
            "emailAddress": {
              "name": "Soporte Radicacion",
              "address": "radicacion@ibpms.local"
            }
          }
        ],
        "hasAttachments": true,
        "bodyPreview": "Señores soporte, exijo la reversión inmediata del cargo...",
        "importance": "high"
      },
      "clientState": "${SECRET_TOKEN}",
      "tenantId": "00000000-0000-0000-0000-000000000000"
    }
  ]
}
EOF
)

echo -e "${YELLOW}[Simulator] Enviando petición HTTP POST a -> ${API_URL}${NC}"

# Enviar CURL al Backend
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${API_URL}" \
  -H "Content-Type: application/json" \
  -H "ClientState: ${SECRET_TOKEN}" \
  -d "${PAYLOAD}")

if [ "$HTTP_STATUS" -eq 202 ] || [ "$HTTP_STATUS" -eq 200 ] || [ "$HTTP_STATUS" -eq 201 ]; then
    echo -e "${GREEN}[ÉXITO] El Backend respondió con HTTP ${HTTP_STATUS}. Payload de MS Graph ingerido correctamente.${NC}"
else
    echo -e "${RED}[FALLO] El Backend respondió con HTTP ${HTTP_STATUS}. Verifica los logs de Spring Boot.${NC}"
    exit 1
fi
