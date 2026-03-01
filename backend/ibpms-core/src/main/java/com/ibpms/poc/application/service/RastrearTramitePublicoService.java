package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.PublicTrackingDTO;
import com.ibpms.poc.application.port.in.RastrearTramitePublicoUseCase;
import com.ibpms.poc.application.port.out.ExpedienteRepositoryPort;
import com.ibpms.poc.domain.model.Expediente;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RastrearTramitePublicoService implements RastrearTramitePublicoUseCase {

    private final ExpedienteRepositoryPort expedienteRepositoryPort;

    public RastrearTramitePublicoService(ExpedienteRepositoryPort expedienteRepositoryPort) {
        this.expedienteRepositoryPort = expedienteRepositoryPort;
    }

    @Override
    public PublicTrackingDTO rastrear(String trackingCode) {
        Optional<Expediente> expedienteOpt = expedienteRepositoryPort.findByBusinessKey(trackingCode);

        if (expedienteOpt.isEmpty()) {
            throw new jakarta.persistence.EntityNotFoundException(
                    "Código de rastreo inválido o no encontrado: " + trackingCode);
        }

        Expediente expediente = expedienteOpt.get();
        PublicTrackingDTO dto = new PublicTrackingDTO();
        dto.setTrackingCode(expediente.getBusinessKey());
        dto.setStartedAt(expediente.getCreatedAt());
        dto.setCompleted(expediente.getStatus() == Expediente.ExpedienteStatus.COMPLETED);

        // Mapeo Amigable (Anti-Corruption Layer). Protege variables completas.
        dto.setStatusDescription(mapToFriendlyStep(expediente));

        return dto;
    }

    private String mapToFriendlyStep(Expediente expediente) {
        if (expediente.getStatus() == Expediente.ExpedienteStatus.COMPLETED) {
            return "Trámite finalizado exitosamente.";
        }
        if (expediente.getStatus() == Expediente.ExpedienteStatus.CANCELLED) {
            return "El trámite fue cancelado.";
        }

        // Si hay una variable formalizada de etapa, mostrarla; si no, genérico.
        Object step = expediente.getVariables().get("publicCurrentStep");
        if (step != null) {
            return step.toString();
        }

        return "En evaluación y procesamiento.";
    }
}
