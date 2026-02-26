package com.ibpms.poc.application.port.out;

/**
 * Puerto de Salida: Idempotencia.
 * Garantiza que operaciones POST costosas no se ejecuten más de una vez
 * para la misma clave (NFR de Idempotency-Key).
 */
public interface IdempotencyPort {

    /**
     * Devuelve true si ya existe una clave de idempotencia registrada.
     * 
     * @param idempotencyKey valor de la cabecera Idempotency-Key
     */
    boolean existe(String idempotencyKey);

    /**
     * Registra la clave junto con el resultado serializado de la operación.
     * 
     * @param idempotencyKey clave única de la operación
     * @param resultadoJson  respuesta serializada a JSON para devolverla en replay
     */
    void registrar(String idempotencyKey, String resultadoJson);

    /**
     * Recupera el resultado previo asociado a la clave.
     * 
     * @param idempotencyKey clave de idempotencia
     * @return resultado JSON previo o null si no existe
     */
    String obtenerResultado(String idempotencyKey);
}
