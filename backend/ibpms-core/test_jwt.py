import urllib.request
import json

req_login = urllib.request.Request(
    'http://localhost:8080/api/v1/auth/login',
    data=json.dumps({"email": "admin@alpha.com", "password": "password"}).encode('utf-8'),
    headers={'Content-Type': 'application/json'},
    method='POST'
)

try:
    with urllib.request.urlopen(req_login) as response:
        login_res = json.loads(response.read().decode('utf-8'))
        token = login_res.get('token')
        print(f"Token: {token[:10]}...")
except urllib.error.HTTPError as e:
    print(f"Login failed: {e.code}")
    print(e.read().decode('utf-8'))
    exit(1)

req_del = urllib.request.Request(
    'http://localhost:8080/api/v1/ai/copilot/session?sessionId=test',
    headers={'Authorization': f'Bearer {token}'},
    method='DELETE'
)

try:
    with urllib.request.urlopen(req_del) as response:
        print(f"Delete Success: {response.status}")
except urllib.error.HTTPError as e:
    print(f"Delete failed: {e.code}")
    print(e.headers)
    print(e.read().decode('utf-8'))
