const http = require('http');

async function testCompleteTask() {
    console.log('--- Iniciando Test US-017 (Completitud de Tarea) ---');
    try {
        // 1. Obtener Token de Login
        const loginData = JSON.stringify({
            username: 'root@ibpms.local',
            password: 'admin'
        });

        const loginOptions = {
            hostname: 'localhost',
            port: 8080,
            path: '/api/v1/auth/login',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(loginData)
            }
        };

        const token = await new Promise((resolve, reject) => {
            const req = http.request(loginOptions, (res) => {
                let data = '';
                res.on('data', chunk => data += chunk);
                res.on('end', () => {
                    if (res.statusCode >= 200 && res.statusCode < 300) {
                        try {
                            const parsed = JSON.parse(data);
                            resolve(parsed.token || parsed.accessToken || data);
                        } catch (e) { resolve(data); }
                    } else {
                        reject(`Login fallback a admin/admin falló. ${res.statusCode} ${data}`);
                    }
                });
            });
            req.on('error', e => reject(e));
            req.write(loginData);
            req.end();
        }).catch(err => {
             console.log("No auth needed or auth failed. Assuming mock token.");
             return "mock-token";
        });

        // 2. Obtener tareas para tener un taskId
        const inboxOptions = {
            hostname: 'localhost',
            port: 8080,
            path: '/api/v1/workdesk/global-inbox?page=0&size=15',
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        };

        let taskId = "test-task-123"; // Fallback task ID

        try {
            const inboxResponse = await new Promise((resolve, reject) => {
                const req = http.request(inboxOptions, (res) => {
                    let data = '';
                    res.on('data', chunk => data += chunk);
                    res.on('end', () => {
                        if (res.statusCode >= 200 && res.statusCode < 300) {
                            try {
                                resolve(JSON.parse(data));
                            } catch (e) { resolve(null); }
                        } else {
                            resolve(null);
                        }
                    });
                });
                req.on('error', e => resolve(null));
                req.end();
            });

            if (inboxResponse && inboxResponse.content && inboxResponse.content.length > 0) {
                // Tomar una tarea para completar
                taskId = inboxResponse.content[0].unifiedId || inboxResponse.content[0].originalTaskId || "test-task-123";
                console.log(`Encontrada tarea en inbox: ${taskId}`);
            }
        } catch (e) {
            console.log("No inbox data fetchable, using fallback taskId");
        }

        // 3. Completar la tarea
        const completeData = JSON.stringify({
            payload: { "approved": true, "comments": "Test from QA Agent" },
            schemaVersion: "v1.0",
            idempotencyKey: "123e4567-e89b-12d3-a456-426614174000",
            gatewayVariables: {}
        });

        const completeOptions = {
            hostname: 'localhost',
            port: 8080,
            path: `/api/v1/workbox/bpmn-tasks/${taskId}/complete`,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
                'Content-Length': Buffer.byteLength(completeData)
            }
        };

        const completeResult = await new Promise((resolve, reject) => {
            const req = http.request(completeOptions, (res) => {
                let data = '';
                res.on('data', chunk => data += chunk);
                res.on('end', () => {
                    resolve({ statusCode: res.statusCode, body: data });
                });
            });
            req.on('error', e => reject(e));
            req.write(completeData);
            req.end();
        });

        console.log(`Status HTTP: ${completeResult.statusCode}`);
        console.log(`Body: ${completeResult.body}`);
        
        let parsedBody;
        try {
            parsedBody = JSON.parse(completeResult.body);
        } catch(e) {}

        if (completeResult.statusCode === 200 && parsedBody && parsedBody.eventReference) {
            console.log(`✅ EVENT REFERENCE DETECTADO: ${parsedBody.eventReference}`);
        } else {
            console.log(`❌ No se detectó eventReference válido o el status no fue 200. Status: ${completeResult.statusCode}`);
        }

    } catch (e) {
        console.error("Error durante el test:", e);
    }
}

testCompleteTask();
