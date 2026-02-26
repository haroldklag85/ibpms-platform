package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.DocumentoDTO;
import java.util.List;

public interface ListarDocumentosUseCase {
    /** Retorna metadatos de los documentos (árbol SGDEA) asociados a un caso */
    List<DocumentoDTO> listarPorCaso(String caseId);
}
