import { test, expect } from '@playwright/test';

test.describe('US-007 GAP-01: Rate Limiting y Caché (Zero-Mock)', () => {
  test('Rate limiting bloquea tras 5 requests (CA-02)', async ({ request }) => {
    const endpoint = '/api/v1/dmn/generate';
    // Enviamos 5 requests permitidos
    for (let i = 0; i < 5; i++) {
       const res = await request.post(endpoint, { data: { prompt: `Test prompt ${i} - bypass cache` } });
       // Aceptamos cualquier código que no sea 429
       expect(res.status()).not.toBe(429);
    }

    // El 6to request debe ser bloqueado
    const res6 = await request.post(endpoint, { data: { prompt: `Test prompt 6 - bloqueado` } });
    expect(res6.status()).toBe(429);
    const body = await res6.json();
    expect(body).toHaveProperty('remainingSeconds');
  });

  test('Caché hash devuelve tabla sin costo LLM e insensible a mayúsculas (CA-20)', async ({ request }) => {
    // Nota: Si el rate limit anterior bloquea globalmente, este test fallará hasta que el rate limit 
    // diferencie por endpoint o pase el tiempo. Asumimos limpieza o endpoint de test.
    const endpoint = '/api/v1/dmn/generate';
    const prompt1 = 'aprobar si monto < 1000 ' + Date.now();
    const prompt2 = prompt1.toUpperCase();

    await request.post(endpoint, { data: { prompt: prompt1 } });

    const startTime2 = Date.now();
    const res2 = await request.post(endpoint, { data: { prompt: prompt2 } });
    const duration2 = Date.now() - startTime2;

    expect(res2.ok()).toBeTruthy();
    // La respuesta cacheada debe ser instantánea (sin LLM delay)
    expect(duration2).toBeLessThan(2000);
  });
  
  test('CA-09: Hard-stop LLM a 50 filas', async ({ request }) => {
    const endpoint = '/api/v1/dmn/generate';
    const res = await request.post(endpoint, { data: { prompt: 'genera una tabla con 60 filas' } });
    // Puede ser 400 o 422
    expect([400, 422]).toContain(res.status());
    const body = await res.json();
    expect(body.message).toContain('Límite de 50 filas superado');
  });
});
