import json, requests

# Try direct DB update via custom SQL endpoint if available
# Or try to update the settings via the settings replace endpoint

# First check what settings exist
resp = requests.get('http://127.0.0.1:8085/v2/ai/setting/get/face_recognition', timeout=10)
print(f'GET /v2/ai/setting/get/face_recognition: {resp.status_code}')
if resp.status_code == 200:
    print(json.dumps(resp.json(), indent=2, ensure_ascii=False)[:1000])
else:
    print(resp.text[:500])

print()

# Try another endpoint format
resp = requests.get('http://127.0.0.1:8085/v2/ai/setting/face_recognition/get', timeout=10)
print(f'GET /v2/ai/setting/face_recognition/get: {resp.status_code}')
if resp.status_code == 200:
    print(json.dumps(resp.json(), indent=2, ensure_ascii=False)[:1000])
else:
    print(resp.text[:500])
