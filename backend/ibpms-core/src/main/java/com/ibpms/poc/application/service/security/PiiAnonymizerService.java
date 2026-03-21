package com.ibpms.poc.application.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CA-05: PII Pseudonymization Service.
 * Actúa como Escudo de Privacidad antes de invocar APIs de Modelos de Lenguaje en Cloud publico.
 */
@Service
public class PiiAnonymizerService {

    private static final Logger log = LoggerFactory.getLogger(PiiAnonymizerService.class);

    // Patrones heurísticos de nombre de variables de negocio que transpiran Identidad Personal
    private static final Pattern PII_PATTERN = Pattern.compile("(?i)(cedula|dni|pasaporte|nombre|cliente|email|telefono|tarjeta|cvv|saldo)");

    /**
     * Reemplaza strings altamente sensibles por Tokens en un diccionario efímero 
     * antes de que el JSON viaje hacia EE.UU/Servicios Cloud NLP.
     * 
     * @param originalXml  Estructura XML/JSON del Modelo que pretendemos autocompletar.
     * @return El mapa dual conteniendo el payload purificado y el diccionario de reversión temporal.
     */
    public Map<String, Object> anonymizePayload(String originalXml) {
        log.info("[APPSEC-PII] Analizando variables estructurales en busca de fugas de Información Personal (PII)...");
        
        Matcher matcher = PII_PATTERN.matcher(originalXml);
        StringBuilder anonymizedPayload = new StringBuilder();
        Map<String, String> tokenDictionary = new HashMap<>();

        while (matcher.find()) {
            String sensitiveWord = matcher.group();
            String safeToken = "sec_var_" + UUID.randomUUID().toString().substring(0, 8);
            
            tokenDictionary.put(safeToken, sensitiveWord);
            matcher.appendReplacement(anonymizedPayload, safeToken);
        }
        matcher.appendTail(anonymizedPayload);

        Map<String, Object> result = new HashMap<>();
        result.put("cleanPayload", anonymizedPayload.toString());
        result.put("translationMap", tokenDictionary);

        log.info("[APPSEC-PII] Análisis concluido. {} variables sensibles fueron enmascaradas.", tokenDictionary.size());
        return result;
    }
}
