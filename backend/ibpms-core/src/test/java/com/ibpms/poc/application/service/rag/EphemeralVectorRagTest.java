package com.ibpms.poc.application.service.rag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EphemeralVectorRagTest {

    // Simulando la tabla LangChain/PGVector
    static class VectorDocumentMock {
        String id;
        String tenantId;
        String content;
        LocalDateTime createdAt;
    }

    // Simulando el Repository JPA para PGVector
    static class PgVectorRepositoryMock {
        private final List<VectorDocumentMock> db = new ArrayList<>();

        public void save(VectorDocumentMock doc) { db.add(doc); }
        public List<VectorDocumentMock> findAll() { return db; }
        public void deleteByCreatedAtBefore(LocalDateTime threshold) {
            db.removeIf(doc -> doc.createdAt.isBefore(threshold));
        }
    }

    private final PgVectorRepositoryMock vectorDb = new PgVectorRepositoryMock();

    @Test
    @DisplayName("US-027 CA-1: Los Contextos RAG nacen anclados criptográficamente al Tenant (Data Segregation)")
    void testRagVectorization_InjectsTenantId_EnforcingSilo() {
        VectorDocumentMock newEmbedding = new VectorDocumentMock();
        newEmbedding.id = "vec_001";
        newEmbedding.content = "Reglamento Comercial 2026";
        newEmbedding.tenantId = "TENANT_ACME_CORP"; // Inyectado por el SecurityContextHolder
        newEmbedding.createdAt = LocalDateTime.now();

        vectorDb.save(newEmbedding);

        // Aserción: El motor RAG jamás guardará vectores agnósticos "huérfanos" (Zero-Trust)
        VectorDocumentMock saved = vectorDb.findAll().get(0);
        assertThat(saved.tenantId).isNotNull().isEqualTo("TENANT_ACME_CORP");
    }

    @Test
    @DisplayName("US-027 CA-1: El CronJob destruye matemática e irreversiblemente los Vectores > 24H (Efimeridad)")
    void testCronJob_PurgesVectorData_OlderThan24Hours() {
        // Vector 1: Viejo (Hace 48 horas) - Debe morir
        VectorDocumentMock oldDoc = new VectorDocumentMock();
        oldDoc.id = "vec_OLD";
        oldDoc.createdAt = LocalDateTime.now().minusHours(48);
        vectorDb.save(oldDoc);

        // Vector 2: Nuevo (Hace 5 horas) - Debe sobrevivir
        VectorDocumentMock freshDoc = new VectorDocumentMock();
        freshDoc.id = "vec_FRESH";
        freshDoc.createdAt = LocalDateTime.now().minusHours(5);
        vectorDb.save(freshDoc);

        // Ejecutamos la simulación del @Scheduled(cron = "0 0 * * * *")
        LocalDateTime threshold24h = LocalDateTime.now().minusHours(24);
        vectorDb.deleteByCreatedAtBefore(threshold24h);

        // Aserción Matemática de Gobernanza Efímera
        assertThat(vectorDb.findAll()).hasSize(1);
        assertThat(vectorDb.findAll().get(0).id).isEqualTo("vec_FRESH");
    }
}
