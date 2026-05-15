import json, base64, io
from PIL import Image

# Read the Unsplash photo base64
result_path = r"c:\Users\yemen\.claude\projects\h--workspace-2-spring-support-api-parent\27602856-4393-49e5-bf8f-f9e5a9d0f30c\tool-results\call_00_hxmHKiOPzndiM2UVJWR29950.json"
with open(result_path, "r") as f:
    result = json.load(f)

text = result[0]["text"]
prefix = '### Result\n"data:image/jpeg;base64,'
start = text.find(prefix) + len(prefix)
end = text.rfind('"')
b64 = text[start:end] if end > start else text[start:]

# Decode and resize to 640x640 (common face detection input size)
img_data = base64.b64decode(b64)
img = Image.open(io.BytesIO(img_data))
print(f"Original: {img.size}")

# Try different resize approaches
# Approach 1: 640x640
img_resized = img.resize((640, 640), Image.LANCZOS)
buf = io.BytesIO()
img_resized.save(buf, format='JPEG', quality=95)
b64_640 = base64.b64encode(buf.getvalue()).decode('ascii')

import requests

# Try with mobilenet model
data = {
    'name': '测试640',
    'fullName': '张三',
    'idCard': '110101199001011237',
    'group': 'default',
    'image': b64_640
}
resp = requests.post('http://127.0.0.1:8085/v2/ai/face-library/register', json=data, timeout=180)
print(f"640x640 attempt - Status: {resp.status_code}")
print(json.dumps(resp.json(), indent=2, ensure_ascii=False))
