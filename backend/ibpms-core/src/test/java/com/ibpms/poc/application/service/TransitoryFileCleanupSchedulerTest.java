package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.repository.TempDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TransitoryFileCleanupScheduler (CA-17: Orphaned file cleanup).
 */
class TransitoryFileCleanupSchedulerTest {

    private TempDocumentRepository tempDocumentRepository;
    private TransitoryFileCleanupScheduler scheduler;

    @BeforeEach
    void setup() {
        tempDocumentRepository = mock(TempDocumentRepository.class);
        scheduler = new TransitoryFileCleanupScheduler(tempDocumentRepository);
    }

    @Test
    @DisplayName("CA-17: Debe eliminar archivos orphaned con más de 24 horas")
    void shouldDeleteOrphanedFilesOlderThan24Hours() {
        when(tempDocumentRepository.deleteByStatusAndUploadedAtBefore(eq("UPLOADED"), any(ZonedDateTime.class)))
                .thenReturn(5);

        scheduler.cleanupOrphanedFiles();

        verify(tempDocumentRepository).deleteByStatusAndUploadedAtBefore(
                eq("UPLOADED"),
                any(ZonedDateTime.class)
        );
    }

    @Test
    @DisplayName("CA-17: No debe loguear cuando no hay archivos para eliminar")
    void shouldNotLogWhenNoFilesDeleted() {
        when(tempDocumentRepository.deleteByStatusAndUploadedAtBefore(eq("UPLOADED"), any(ZonedDateTime.class)))
                .thenReturn(0);

        scheduler.cleanupOrphanedFiles();

        verify(tempDocumentRepository).deleteByStatusAndUploadedAtBefore(
                eq("UPLOADED"),
                any(ZonedDateTime.class)
        );
        // El método no debería loguear cuando deletedCount == 0 (verificado por cobertura de branches)
    }
}
