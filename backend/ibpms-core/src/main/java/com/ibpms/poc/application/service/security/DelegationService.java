package com.ibpms.poc.application.service.security;

import com.ibpms.poc.application.dto.security.DelegationRequestDTO;
import com.ibpms.poc.infrastructure.jpa.entity.security.DelegationEntity;
import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.DelegationRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import com.ibpms.poc.infrastructure.mq.producer.TaskRescueProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CA-9 US-036 — Servicio de Gestión de Cesiones Temporales de Poder.
 *
 * Responsable de crear delegaciones auditables entre usuarios dentro de una
 * ventana temporal explícita. Aplica las siguientes invariantes de negocio:
 *
 *   1. startDate NO puede ser retroactiva (se tolera un margen de 1 minuto).
 *   2. startDate debe ser estrictamente anterior a endDate.
 *   3. Tanto el donante como el receptor deben existir en el sistema.
 *
 * El JwtAuthFilter usa DelegationRepository.findActiveDelegationsForSubstitute()
 * para inyectar delegaciones activas en el contexto de seguridad en tiempo real.
 */
@Service
@Transactional
public class DelegationService {

    private final DelegationRepository delegationRepository;
    private final UserRepository userRepository;
    private final TaskRescueProducer taskRescueProducer;

    public DelegationService(DelegationRepository delegationRepository,
                             UserRepository userRepository,
                             TaskRescueProducer taskRescueProducer) {
        this.delegationRepository = delegationRepository;
        this.userRepository = userRepository;
        this.taskRescueProducer = taskRescueProducer;
    }

    /**
     * Crea y persiste una cesión temporal de poder del donante hacia el receptor.
     *
     * @param donorId UUID del usuario que delega su poder
     * @param dto     DTO con recipientId, startDate, endDate y reason
     * @return La entidad de delegación persistida
     * @throws IllegalArgumentException si las fechas son inválidas o los usuarios no existen
     */
    public DelegationEntity createDelegation(UUID donorId, DelegationRequestDTO dto) {
        LocalDateTime now = LocalDateTime.now();

        // Invariante 1: startDate no puede ser retroactiva (margen de 1 minuto de tolerancia)
        if (dto.getStartDate().isBefore(now.minusMinutes(1))) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser retroactiva. startDate recibida: " + dto.getStartDate());
        }

        // Invariante 2: startDate debe ser estrictamente anterior a endDate
        if (!dto.getStartDate().isBefore(dto.getEndDate())) {
            throw new IllegalArgumentException(
                    "La fecha de inicio debe ser anterior a la fecha de fin. " +
                    "startDate: " + dto.getStartDate() + " — endDate: " + dto.getEndDate());
        }

        // Invariante 3a: El donante debe existir
        UserEntity donor = userRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Donante no encontrado: " + donorId));

        // Invariante 3b: El receptor debe existir y ser distinto del donante
        UserEntity recipient = userRepository.findById(dto.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Receptor no encontrado: " + dto.getRecipientId()));

        if (donorId.equals(dto.getRecipientId())) {
            throw new IllegalArgumentException(
                    "El donante y el receptor no pueden ser el mismo usuario.");
        }

        DelegationEntity delegation = new DelegationEntity(donor, recipient, dto.getStartDate(), dto.getEndDate());
        delegation.setReason(dto.getReason());
        DelegationEntity saved = delegationRepository.save(delegation);

        // CA-07 Trigger massive unclaim to free donor's tasks
        taskRescueProducer.triggerDelegationUnclaim(donorId.toString());

        return saved;
    }
}
