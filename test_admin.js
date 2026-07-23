async function testApi() {
    console.log("Logging in via emergency-login...");
    let token = '';
    try {
        const authRes = await fetch('http://127.0.0.1:8080/api/v1/auth/emergency-login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email: 'admin@alpha.com', password: 'admin'})
        });
        
        const data = await authRes.json();
        console.log("Login Response (admin):", authRes.status, data);
        
        const authResReal = await fetch('http://127.0.0.1:8080/api/v1/auth/emergency-login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email: 'admin@alpha.com', password: 'Test123!'})
        });
        
        const dataReal = await authResReal.json();
        console.log("Login Response (Test123!):", authResReal.status, dataReal);
        token = dataReal.token || data.token;
        if (!token) return console.error("Could not obtain token");
    } catch(e) {
        return console.error(e.message);
    }

    const config = {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    };

    const endpoints = [
        { method: 'GET', url: 'http://127.0.0.1:8080/api/v1/analytics/process-health' },
        { method: 'GET', url: 'http://127.0.0.1:8080/api/v1/analytics/ai-metrics' },
        { method: 'GET', url: 'http://127.0.0.1:8080/api/v1/workdesk/global-inbox?page=0&size=50&sort=slaExpirationDate,asc' },
        { method: 'GET', url: 'http://127.0.0.1:8080/api/v1/workdesk/global-inbox/facets' },
        { method: 'GET', url: 'http://127.0.0.1:8080/api/v1/workdesk/feature-toggles/FORCE_ROUTING' }
    ];

    for (const ep of endpoints) {
        console.log(`\n=> ${ep.method.toUpperCase()} ${ep.url}`);
        try {
            const res = await fetch(ep.url, { method: ep.method, ...config });
            const bodyText = await res.text();
            console.log("Status:", res.status);
            console.log("Body:", bodyText.substring(0, 500));
        } catch (e) {
            console.error(e.message);
        }
    }
}

testApi();
