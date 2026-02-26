import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuración de la prueba de carga: 200 usuarios concurrentes
export const options = {
    stages: [
        { duration: '30s', target: 50 },  // Ramp-up a 50 usuarios
        { duration: '1m', target: 200 },  // Pico de 200 usuarios concurrentes
        { duration: '30s', target: 0 },   // Ramp-down
    ],
    thresholds: {
        http_req_duration: ['p(95)<1500'], // 95% de peticiones deben resolverse en < 1.5s
        http_req_failed: ['rate<0.01'],    // Tasa de fallos menor al 1%
    },
};

const BASE_URL = 'http://localhost:8080/api/v1';
const SECRET_TOKEN = 'SimulatedGraphSecretToken2026';

// Dummy payload para MS Graph Webhook
const graphPayload = JSON.stringify({
    "value": [
        {
            "subscriptionId": "11111111-2222-3333-4444-555555555555",
            "changeType": "created",
            "resource": "Users/radicacion@ibpms.local/Messages/AQAQ...",
            "resourceData": {
                "subject": "LOAD TEST: Reclamo Cobro Indebido",
                "bodyPreview": "Este es un correo de prueba de carga masiva...",
                "importance": "high"
            },
            "clientState": SECRET_TOKEN
        }
    ]
});

export default function () {
    // 1. Simular carga del Webhook (MS Graph inyectando casos al motor Camunda)
    const webhookRes = http.post(`${BASE_URL}/inbound/email-webhook`, graphPayload, {
        headers: {
            'Content-Type': 'application/json',
            'ClientState': SECRET_TOKEN
        },
    });

    check(webhookRes, {
        'Webhook - is status 202 or 200': (r) => r.status === 202 || r.status === 200 || r.status === 201,
    });

    // 2. Simular carga del Frontend (Consulta de la Bandeja de Tareas)
    // Asumimos que el motor está procesando e indexando, por lo que las lecturas también causan carga BD
    const tasklistRes = http.get(`${BASE_URL}/tasks?limit=50&offset=0&status=PENDING`, {
        // En un entorno real se pasaría el token JWT
        headers: {
            'Authorization': 'Bearer dummy_jwt_token_for_tests'
        },
    });

    check(tasklistRes, {
        'Tasklist - is status 200': (r) => r.status === 200,
    });

    // Pausa breve simulando tiempo de reflexión del usuario/sistema
    sleep(1);
}
