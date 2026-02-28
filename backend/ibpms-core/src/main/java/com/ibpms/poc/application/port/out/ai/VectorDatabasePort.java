package com.ibpms.poc.application.port.out.ai;

import java.util.List;
import java.util.Map;

/**
 * Puerto de Salida (Secondary Port) - Hexagonal Architecture.
 * 
 * Esta interfaz abstrae el almacenamiento y búsqueda matemática de vectores.
 * El núcleo del iBPMS (Casos de Uso) debe utilizar ESTA interfaz para
 * comunicarse
 * con el Motor RAG, sin importarle qué tecnología existe por detrás.
 * 
 * En la V1 actual: El adaptador que implementa esto usará PostgreSQL +
 * pgvector.
 * En la V2 futura: Se creará un nuevo adaptador que use llamadas HTTP a Qdrant
 * o Milvus.
 * El cambio de tecnología en V2 solo requiere ajustar la inyección de
 * dependencias.
 */
public interface VectorDatabasePort {

    /**
     * Almacena un Vector (Embedding) generado por Vertex AI / OpenAI.
     * 
     * @param businessEntityId El ID de nuestro Expediente o Tarea en la BD
     *                         Relacional.
     * @param embedding        Matriz de números (Float) de 768 / 1536 dimensiones.
     * @param metadata         Contexto adicional (ej. "Tipo: Laboral", "Prioridad:
     *                         Alta") para filtrar.
     */
    void saveVector(String businessEntityId, List<Double> embedding, Map<String, Object> metadata);

    /**
     * Búsqueda por Similitud (Distancia de Coseno o Producto Interno).
     * 
     * @param queryEmbedding El vector de la frase que el usuario está buscando.
     * @param limit          Cantidad de resultados más cercanos a devolver (top K).
     * @return Lista de IDs de la Base Relacional (Expedientes) que convergen
     *         matemáticamente.
     */
    List<String> searchSimilar(List<Double> queryEmbedding, int limit);

    /**
     * Borra el Vector si el Expediente es eliminado o archivado.
     * 
     * @param businessEntityId El ID del expediente.
     */
    void deleteVector(String businessEntityId);
}
