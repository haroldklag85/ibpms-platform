const testUrl = async (url, method='GET', body=null) => { 
  console.log(`\n=> ${method} ${url}`);
  try {
    const res = await fetch(url, {
       method, headers: {'Content-Type': 'application/json'}, body: body ? JSON.stringify(body) : undefined 
    });
    console.log(`Status: ${res.status}`);
    const text = await res.text();
    console.log(`Body: ${text.slice(0, 100)}`);
  } catch(e) {
    console.error(`Error: ${e.message}`);
  }
};
(async () => {
  await testUrl('http://127.0.0.1:8080/actuator/health');
  await testUrl('http://127.0.0.1:8080/api/v1/auth/login', 'POST', {email: 'arquitecto.alpha@test.com', password: 'Test_1234'});
  await testUrl('http://127.0.0.1:8080/inbound/email-webhook', 'POST', {});
  await testUrl('http://127.0.0.1:5173/login', 'GET', null);
})();
