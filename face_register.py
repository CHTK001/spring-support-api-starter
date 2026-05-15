import json

# Read the tool result file for direct fetch
result_path = r"c:\Users\yemen\.claude\projects\h--workspace-2-spring-support-api-parent\27602856-4393-49e5-bf8f-f9e5a9d0f30c\tool-results\call_00_Vb85ryy5ZJM6E4gB2R9eHqB5VouYQn9YYTeGN.json"
with open(result_path, "r") as f:
    result = json.load(f)

text = result[0]["text"]
prefix = '### Result\n"data:image/jpeg;base64,'
start = text.find(prefix) + len(prefix)
end = text.rfind('"')
b64 = text[start:end] if end > start else text[start:]

print(f"Base64 OK, length={len(b64)}")

import requests
data = {
    'name': '测试用户',
    'fullName': '张三',
    'idCard': '110101199001011234',
    'group': 'default',
    'image': b64
}
resp = requests.post('http://127.0.0.1:8085/v2/ai/face-library/register', json=data, timeout=180)
print(f"Status: {resp.status_code}")
print(json.dumps(resp.json(), indent=2, ensure_ascii=False))
