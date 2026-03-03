import os
import sys
import json
import time
import requests
from datetime import datetime, timezone
from bs4 import BeautifulSoup

# ==============================================================================
# iBPMS V1 - Sprint 7: Módulo RPA Python (Web Scraping Lex)
# Componente: Contenedor Efímero de Recolección Judicial.
# Este script se ejecuta mediante CronJob, recolecta la data y muere inmediatamente.
# ==============================================================================

# 1. Configuración de Entorno
WEBHOOK_URL = os.getenv("IBPMS_WEBHOOK_URL", "http://ibpms-gateway:8080/api/v1/inbound/rpa-webhook")
ORIGIN_IDENTIFIER = "RPA_RAMA_JUDICIAL"

def scrape_judicial_notifications():
    """
    Simula la extracción de datos desde un portal web estático de la rama judicial.
    En un entorno real, aquí se haría 'requests.get(url_rama_judicial)' y se parsearía el DOM.
    """
    print("🤖 [RPA] Iniciando recolección de notificaciones judiciales...")
    
    # Simulación de respuesta HTML ofuscada de un portal del estado
    html_mock = """
    <html>
        <body>
            <div class="resolucion-item">
                <span class="radicado">RAD-2026-X84-00</span>
                <span class="decision">Auto que admite la demanda ejecutiva y ordena medidas cautelares.</span>
                <span class="demandante">Banco Ejemplo S.A.</span>
                <span class="demandado">Juan Pérez</span>
                <span class="fecha">27/02/2026</span>
            </div>
        </body>
    </html>
    """
    
    soup = BeautifulSoup(html_mock, 'html.parser')
    items = soup.find_all('div', class_='resolucion-item')
    
    notifications = []
    
    for item in items:
        # Extracción segura
        radicado = item.find('span', class_='radicado').text.strip() if item.find('span', class_='radicado') else "DESCONOCIDO"
        decision = item.find('span', class_='decision').text.strip() if item.find('span', class_='decision') else ""
        demandante = item.find('span', class_='demandante').text.strip() if item.find('span', class_='demandante') else ""
        demandado = item.find('span', class_='demandado').text.strip() if item.find('span', class_='demandado') else ""
        
        # Formateo al Contrato JSON Estricto del Webhook (RpaNotificationDTO)
        payload = {
            "origen": ORIGIN_IDENTIFIER,
            "tramiteId": radicado,
            "descripcionNotificacion": decision,
            "partes": [demandante, demandado],
            "fechaPublicacion": datetime.now(timezone.utc).isoformat(), # Formato ISO 8601 exigido por Java
            "metadataAdicional": {
                "tipo_documento": "AUTO_ADMISORIO",
                "requiere_decision_urgente": True,
                "scraper_version": "1.0.0"
            }
        }
        notifications.append(payload)
        
    return notifications

def send_to_webhook(payloads: list) -> bool:
    """
    Envía los resúmenes jurídicos al backend Java mediante solicitudes POST HTTP síncronas.
    """
    if not payloads:
        print("ℹ️ [RPA] No se encontraron nuevas notificaciones en esta ejecución.")
        return True

    success_list = []
    headers = {'Content-Type': 'application/json'}
    
    print(f"🚀 [RPA] Enviando {len(payloads)} notificaciones al Webhook: {WEBHOOK_URL}")
    
    for item in payloads:
        try:
            # POST Timeout corto de 5 segundos para que el script no quede zombie si Java cae
            response = requests.post(WEBHOOK_URL, json=item, headers=headers, timeout=5)
            
            if response.status_code == 202:
                print(f"✅ [RPA] Éxito enviando radicado {item.get('tramiteId')}. TracingID: {response.json().get('tracingId')}")
                success_list.append(1)
            else:
                print(f"⚠️ [RPA] Fallo en API (Status {response.status_code}): {response.text}")
                
        except requests.exceptions.RequestException as e:
            print(f"❌ [RPA] Error fatal de red enviando al Webhook: {e}")
            return False # Falla rápida: abortar si la red está caída
            
    return len(success_list) == len(payloads)

def main():
    """Punto de entrada orquestal del contenedor"""
    try:
        # 1. Scrapear Web
        data = scrape_judicial_notifications()
        
        # 2. Integrar al ecosistema iBPMS
        success = send_to_webhook(data)
        
        # 3. Limpieza de Memoria y Muerte del Contenedor (Política V1)
        if success:
            print("🛑 [RPA] Operación completada exitosamente. Finalizando proceso libreando RAM (Código 0).")
            sys.exit(0)
        else:
            print("🛑 [RPA] Operación finalizada con errores parciales o totales de red. Reporte al orquestador (Código 1).")
            sys.exit(1)
            
    except Exception as e:
        print(f"🔥 [CRÍTICO] Fallo general no controlado en el proceso RPA: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
