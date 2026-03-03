package com.ibpms.poc.application.service.sgdea;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SharePointAdapterService {

    @SuppressWarnings("unused")
    private final WebClient webClient;

    public SharePointAdapterService(WebClient.Builder webClientBuilder) {
        // En prod esto se inyecta con OAuth2 Client Credentials
        this.webClient = webClientBuilder.baseUrl("https://graph.microsoft.com/v1.0").build();
    }

    /**
     * Sube un archivo gigantesco (ej. 50MB) a SharePoint sin cargarlo completamente
     * en RAM (`byte[]`), previniendo Out-Of-Memory (OOM) exceptions. (CA-14 /
     * DataMapping)
     * Utiliza Spring 6.1 `DataBuffer` / `Resource` (WebFlux).
     *
     * @param siteId     Identificador del sitio SharePoint
     * @param fileName   Nombre del fichero a cargar
     * @param fileStream Recurso abierto del FileSystem local (.tmp)
     */
    public void uploadMassiveFileStream(String siteId, String fileName, Resource fileStream) {
        if (fileStream == null || !fileStream.exists()) {
            throw new IllegalArgumentException("El streaming resource no existe o es inválido.");
        }
        // Simulado para V1
    }

    /**
     * Crea una carpeta dinámica en SharePoint basada en el proceso y el ID del caso
     * (CA-2).
     */
    public String createFolder(String processName, String caseId) {
        // Simulación: Graph API POST /drives/{drive-id}/items/{item-id}/children
        // body: { "name": caseId, "folder": { }, "@microsoft.graph.conflictBehavior":
        // "rename" }
        return "folder_" + processName + "_" + caseId;
    }

    /**
     * Inyecta variables del proceso iBPMS como metadatos en las columnas de
     * SharePoint (CA-10).
     */
    public void injectMetadata(String itemId, java.util.Map<String, Object> metadata) {
        // Simulación: Graph API PATCH
        // /sites/{site-id}/lists/{list-id}/items/{item-id}/fields
        // body: { "ProcessName": "Visas", "CaseId": "12345", ... }
    }

    /**
     * Búsqueda delegada en SharePoint usando MS Graph Search API (CA-16).
     */
    public String searchFullText(String query) {
        // Simulación: Graph API POST /search/query
        // body: { "requests": [ { "entityTypes": ["listItem"], "query": {
        // "queryString": query } } ] }
        return "[MOCK] Resultados de MS Graph para: " + query;
    }
}
