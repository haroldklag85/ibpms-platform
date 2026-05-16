const fs = require('fs');

async function run() {
    console.log("Logging in via 8080...");
    const loginRes = await fetch('http://localhost:8080/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'root@ibpms.local', password: 'admin' })
    });
    const loginData = await loginRes.json();
    console.log("Token received.");
    
    const payload = {
        name: 'Formulario Simple IDE Persistence 2',
        technicalName: 'FORMULARIO_SIMPLE_IDE_PERSISTENCE_' + Date.now(),
        pattern: 'SIMPLE',
        formFields: [
            {
                id: 'FIELD_SEED_1',
                camundaVariable: 'field_seed_1',
                type: 'text',
                label: 'Campo Base (Semilla)',
                required: false,
                stage: 'START_EVENT'
            }
        ]
    };
    
    console.log("Sending POST /api/v1/forms to VITE PROXY (5173)...");
    const formRes = await fetch('http://localhost:5173/api/v1/forms', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + loginData.token
        },
        body: JSON.stringify(payload)
    });
    
    console.log("Status:", formRes.status);
    const text = await formRes.text();
    console.log("Response:", text);
}

run().catch(console.error);
