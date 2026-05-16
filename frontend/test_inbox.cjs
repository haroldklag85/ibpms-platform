const axios = require('axios');
axios.get('http://localhost:8080/api/v1/workdesk/global-inbox', {
  headers: { Authorization: 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmFsaXN0YV9uMUBhbHBoYS5jb20iLCJyb2xlcyI6WyJpYnBtc19yb2xfUFJPQ0VTU19BUkNISVRFQ1QiLCJpYnBtc19yb2xfQlBNTl9ERVNJR05FUiIsImlicG1zX3JvbF9VU0VSIl0sInRlbmFudF9pZCI6InRlbmFudF9hbHBoYSIsImlhdCI6MTc3ODMwNjg3NCwiZXhwIjoxNzc4MzA3Nzc0fQ.GZIAjmCj0duWLTrQLtsOdLJtP39EBunqM0j5i9CKJdA' }
}).then(res => console.log(JSON.stringify(res.data, null, 2)))
.catch(err => console.error(err.response ? err.response.data : err.message));
