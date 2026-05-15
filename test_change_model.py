import json, requests

# Update face_recognition settings to use mobilenet model instead
settings = [
    {"sysAiGlobalSettingField": "default_face_detect_model", "sysAiGlobalSettingValue": "local-onnx-face-detect-mobilenet", "sysAiGlobalSettingValueType": "STRING"},
]

resp = requests.post(
    'http://127.0.0.1:8085/v2/ai/setting/face_recognition/replace',
    json=settings,
    timeout=10
)
print(f"Update settings: {resp.status_code}")
print(json.dumps(resp.json(), indent=2, ensure_ascii=False))
