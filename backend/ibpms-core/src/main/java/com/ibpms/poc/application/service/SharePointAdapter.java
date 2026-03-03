package com.ibpms.poc.application.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SharePointAdapter {

    @SuppressWarnings("unused")
    private final WebClient webClient;

    public SharePointAdapter(WebClient.Builder webClientBuilder) {
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
        // Validación de nulidad
        if (fileStream == null || !fileStream.exists()) {
            throw new IllegalArgumentException("El streaming resource no existe o es inválido.");
        }

        // Simulación: Se arma la subida Graph API "Session Upload"
        // String uploadUrl =
        // String.format("/sites/%s/drive/root:/%s:/createUploadSession", siteId,
        // fileName);

        // En Producción (Real) esto delega el HTTP POST al WebClient (Reactor Netty)
        // el cual absorbe el Resource leyendo en chunks el InputStream subyacente hacia
        // TCP.

        // Simulado:
        // this.webClient.put()
        // .uri(uploadUrl)
        // .body(BodyInserters.fromResource(fileStream))
        // .retrieve()
        // .bodyToMono(String.class)
        // .block();
    }
}
