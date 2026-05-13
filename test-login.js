fetch('http://localhost:8080/api/v1/auth/emergency-login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({email: 'root@ibpms.local', password: 'Root#Temp4Sys'})
}).then(async r => {
  console.log("Status:", r.status);
  console.log(await r.text());
}).catch(console.error);
