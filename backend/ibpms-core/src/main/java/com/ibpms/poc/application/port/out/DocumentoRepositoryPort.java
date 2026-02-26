package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.Documento;
import java.util.List;

public interface DocumentoRepositoryPort {
    /** Guarda un registro inmutable en el repositorio (ibpms_document) */
    void save(Documento documento);

    /** Lista todos los documentos de un expediente ordenados descendentemente */
    List<Documento> findByCaseId(String caseId);
}
