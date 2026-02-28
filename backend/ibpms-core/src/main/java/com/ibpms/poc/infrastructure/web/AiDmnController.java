package com.ibpms.poc.infrastructure.web;

import com.ibpms.dmn.DmnTranslationRequestDTO;
import com.ibpms.dmn.DmnTranslationResponseDTO;
import com.ibpms.dmn.GenerarDmnAiUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador Driving: Controlador para utilidades de IA integradas a los
 * Modeladores
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiDmnController {

    private final GenerarDmnAiUseCase generarDmnAiUseCase;

    public AiDmnController(GenerarDmnAiUseCase generarDmnAiUseCase) {
        this.generarDmnAiUseCase = generarDmnAiUseCase;
    }

    /**
     * Endpoint API Rest - POST /api/v1/ai/dmn/translate
     * Recibe reglas de negocio dictadas por operadores y las traduce a XML DMN 1.3
     */
    @PostMapping("/dmn/translate")
    public ResponseEntity<DmnTranslationResponseDTO> translateNaturalLanguageToDmn(
            @RequestBody DmnTranslationRequestDTO request) {
        DmnTranslationResponseDTO response = generarDmnAiUseCase.generarDesdeTexto(request);
        return ResponseEntity.ok(response);
    }
}
