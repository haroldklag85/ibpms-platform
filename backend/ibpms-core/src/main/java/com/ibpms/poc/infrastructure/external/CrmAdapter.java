package com.ibpms.poc.infrastructure.external;

import com.ibpms.poc.application.port.out.CatalogCachePort;
import com.ibpms.poc.application.port.out.CrmFederationPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CrmAdapter implements CrmFederationPort {

    private static final Logger log = LoggerFactory.getLogger(CrmAdapter.class);

    private final RestTemplate restTemplate;
    private final CatalogCachePort cachePort;

    public CrmAdapter(RestTemplate restTemplate, CatalogCachePort cachePort) {
        this.restTemplate = restTemplate;
        this.cachePort = cachePort;
    }

    @Override
    @CircuitBreaker(name = "crmService", fallbackMethod = "fetchCatalogFallback")
    public String fetchCatalogFromCrm(String catalogId) {
        // Ejecución Principal: Intenta ir a la red corporativa.
        // Simulando la URL del CRM Corporativo
        String url = "https://mock-crm.corporativo.net/api/v1/lovs/" + catalogId;
        log.info("Llamando al CRM Corporativo: {}", url);

        String response = restTemplate.getForObject(url, String.class);

        // Si hay éxito, el hilo llega acá. Inyectamos silenciosamente en caché.
        if (response != null) {
            log.info("Llamada exitosa al CRM para {}. Actualizando caché local.", catalogId);
            cachePort.updateCache(catalogId, response);
        }
        return response;
    }

    /**
     * Fallback Method: Se invoca si el Circuit Breaker decide "ABRIR" (Open State)
     * por exceso de fallos, o si la llamada síncrona en fetchCatalogFromCrm lanza
     * excepción.
     */
    @SuppressWarnings("unused")
    public String fetchCatalogFallback(String catalogId, Throwable t) {
        log.warn(
                "CRM Inaccesible. Circuit Breaker actuando para el catálogo: {}. Causa: {}. Entrando a MODO DEGRADADO desde DB Local.",
                catalogId, t.getMessage());

        return cachePort.getCatalogFallback(catalogId)
                .orElseThrow(() -> new RuntimeException(
                        "Falla Crítica: CRM Caído y NO hay caché local previa para el catálogo: " + catalogId));
    }
}
