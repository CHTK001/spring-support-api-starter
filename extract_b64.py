import json, os

# Read the tool result file
result_path = r"C:\Users\yemen\.claude\projects\h--workspace-2-spring-support-api-parent\27602856-4393-49e5-bf8f-f9e5a9d0f30c\tool-results\call_00_WJhRC92JnXlzZat6EnOl4472.json"
with open(result_path, "r") as f:
    result = json.load(f)

# Extract base64 from the result text
text = result[0]["text"]
# The format is: ### Result\n"data:image/jpeg;base64,<base64>"
prefix = '### Result\n"data:image/jpeg;base64,'
suffix = '"'
start = text.find(prefix) + len(prefix)
# Find the last quote
end = text.rfind(suffix)
if end > start:
    b64 = text[start:end]
else:
    # Try alternate format
    b64 = text[start:]

# Write just the raw base64
out_path = r"C:\Users\yemen\AppData\Local\Temp\face_b64_clean.txt"
with open(out_path, "w") as f:
    f.write(b64)

print(f"Base64 length: {len(b64)}")
print(f"Saved to: {out_path}")
