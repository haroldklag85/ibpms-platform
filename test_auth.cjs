fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'analista_n1@alpha.com', password: 'alpha123' })
})
.then(res => res.json())
.then(data => {
  console.log("TOKEN:", data.token);
  return fetch('http://localhost:8080/api/v1/workdesk/global-inbox', {
    headers: { 'Authorization': 'Bearer ' + data.token }
  });
})
.then(res => res.json())
.then(data => console.log(JSON.stringify(data, null, 2)))
.catch(err => console.error(err));
