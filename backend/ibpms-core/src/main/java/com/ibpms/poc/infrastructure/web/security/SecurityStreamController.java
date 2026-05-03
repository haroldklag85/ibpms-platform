package com.ibpms.poc.infrastructure.web.security;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CA-11: Endpoint SSE para el listener de revocación de roles en tiempo real.
 * El frontend se suscribe a este stream para recibir el evento [ROLE_REVOKED].
 * V1: Stream operacional con heartbeat — emisión de eventos pendiente de integración
 * con el bus de eventos de seguridad (RabbitMQ / Application Events).
 */
@RestController
@RequestMapping("/api/v1/security")
public class SecurityStreamController {

    // Lista de emitters activos para broadcast futuro
    private final CopyOnWriteArrayList<SseEmitter> activeEmitters = new CopyOnWriteArrayList<>();

    // Executor para heartbeat periódico (evita que el browser cierre la conexión idle)
    private final ScheduledExecutorService heartbeatScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sse-heartbeat");
                t.setDaemon(true);
                return t;
            });

    public SecurityStreamController() {
        // Heartbeat cada 25 segundos para mantener la conexión SSE activa
        heartbeatScheduler.scheduleAtFixedRate(this::broadcastHeartbeat, 15, 25, TimeUnit.SECONDS);
    }

    /**
     * GET /api/v1/security/stream
     * Endpoint SSE — el cliente recibe eventos de seguridad en tiempo real.
     * Timeout de 30 minutos. El frontend reconecta automáticamente vía fetchEventSource.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSecurityEvents() {
        // 30 minutos de timeout — suficiente para una sesión de trabajo estándar
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        activeEmitters.add(emitter);

        // Limpieza automática al completar o tener error
        emitter.onCompletion(() -> activeEmitters.remove(emitter));
        emitter.onTimeout(() -> {
            activeEmitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError(ex -> activeEmitters.remove(emitter));

        // Enviar evento de conexión establecida para validar el handshake
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("[SECURITY_STREAM_ACTIVE]"));
        } catch (IOException e) {
            activeEmitters.remove(emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * Envía heartbeat a todos los emitters activos para mantener las conexiones SSE vivas.
     * Los emitters cerrados se eliminan automáticamente de la lista.
     */
    private void broadcastHeartbeat() {
        activeEmitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
                return false; // Mantener en lista
            } catch (Exception e) {
                return true; // Eliminar emitters fallidos
            }
        });
    }
}
