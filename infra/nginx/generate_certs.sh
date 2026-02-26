#!/usr/bin/env bash

# ==============================================================================
# iBPMS Platform - TLS Certificate Generator (Local Development)
# Descripción: Genera certificados auto-firmados para el gateway NGINX.
# ==============================================================================

set -e

CERT_DIR="infra/nginx/certs"
mkdir -p "$CERT_DIR"

echo "[TLS] Generando llave privada y certificado auto-firmado..."

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "$CERT_DIR/server.key" \
  -out "$CERT_DIR/server.crt" \
  -subj "/C=CO/ST=Antioquia/L=Medellin/O=iBPMS/CN=localhost"

echo "[TLS] Certificados generados en $CERT_DIR"
echo "[TLS] RECUERDA: Al acceder vía HTTPS, el navegador mostrará una advertencia de seguridad."
