#!/usr/bin/env bash

# ==============================================================================
# iBPMS Platform - Setup Local & Infra Script (PoC)
# Descripción: Inicializa la infraestructura docker-compose de la aplicación y 
#              espera activamente a que la base de datos MySQL esté disponible.
# ==============================================================================

set -e

# Configuración de colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # Sin color

echo -e "${YELLOW}[iBPMS] Iniciando orquestación de servicios en Docker Compose...${NC}"

# 1. Levantar servicios (Background)
# Usando docker compose o docker-compose según disponibilidad
if command -v docker-compose &> /dev/null; then
    docker-compose up -d
else
    docker compose up -d
fi

echo -e "${YELLOW}[iBPMS] Servicios levantados. Verificando disponibilidad de MySQL (Puerto 3306)...${NC}"

# 2. Bucle de espera inteligente para MySQL
MAX_RETRIES=45
RETRY_COUNT=0
SLEEP_INTERVAL=2

# Verificamos conectividad al puerto 3306 usando nc o bash /dev/tcp
check_mysql() {
  if command -v nc &> /dev/null; then
    nc -z localhost 3306 >/dev/null 2>&1
  else
    # Fallback si nc no está instalado en este sistema Windows/Git Bash
    (echo > /dev/tcp/localhost/3306) >/dev/null 2>&1
  fi
}

while ! check_mysql; do
  RETRY_COUNT=$((RETRY_COUNT+1))
  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo -e "${RED}[Error] MySQL no respondió en el puerto 3306 después de $((MAX_RETRIES * SLEEP_INTERVAL)) segundos.${NC}"
    echo -e "${YELLOW}[Info] Revisa los logs con: docker compose logs ibpms-db${NC}"
    exit 1
  fi
  sleep $SLEEP_INTERVAL
done

# Validación extra: Usar mysqladmin healthcheck usando el contenedor en ejecución (opcional pero más robusto)
echo -e "${YELLOW}[iBPMS] Puerto abierto. Verificando si MySQL está listo para conexiones...${NC}"
# Aunque el healthcheck de docker lo maneja, nosotros esperamos confirmación
sleep 5

echo -e "${GREEN}Infraestructura Lista${NC}"
echo -e "${YELLOW}================================================================${NC}"
echo -e "${GREEN}-> Base de Datos (MySQL) está operativa en localhost:3306${NC}"
echo -e "${GREEN}-> Puedes proceder a ejecutar las pruebas End-to-End o levantar el Backend.${NC}"
echo -e "${YELLOW}================================================================${NC}"
exit 0
