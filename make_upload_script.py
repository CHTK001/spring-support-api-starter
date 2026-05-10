import base64

with open('ai-face-library2.png', 'rb') as f:
    data = base64.b64encode(f.read()).decode()

js = """async () => {
  const base64 = `""" + data + """`;
  const byteChars = atob(base64);
  const byteNums = new Uint8Array(byteChars.length);
  for (let i = 0; i < byteChars.length; i++) {
    byteNums[i] = byteChars.charCodeAt(i);
  }
  const blob = new Blob([byteNums], { type: 'image/png' });
  const file = new File([blob], 'test-image.png', { type: 'image/png' });
  const fileInput = document.querySelector('input[type="file"].face-library__file-input');
  if (!fileInput) return 'file input not found';
  const dt = new DataTransfer();
  dt.items.add(file);
  Object.defineProperty(fileInput, 'files', { value: dt.files, configurable: true });
  fileInput.dispatchEvent(new Event('change', { bubbles: true }));
  await new Promise(r => setTimeout(r, 500));
  return 'uploaded: ' + file.name + ' size: ' + file.size;
}"""

with open('upload_script.js', 'w', encoding='utf-8') as f:
    f.write(js)

print('script saved, size:', len(js))
