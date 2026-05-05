// CDP helper to examine page and navigate
const WebSocket = require('ws');
const cdpUrl = process.argv[2];
const action = process.argv[3] || 'nav';
if (!cdpUrl) { console.error('Usage: node cdp-nav.js <cdp-url> <action>'); process.exit(1); }

const ws = new WebSocket(cdpUrl);
let msgId = 1;
let sessionId = null;
const pending = {};

ws.on('message', (raw) => {
  const msg = JSON.parse(raw.toString());
  const id = msg.id;
  if (id && pending[id]) {
    pending[id](msg);
    delete pending[id];
  }
});

function send(method, params) {
  const id = msgId++;
  return new Promise((resolve) => {
    pending[id] = resolve;
    ws.send(JSON.stringify({ id, method, params }));
  });
}

function pageSend(method, params) {
  const id = msgId++;
  return new Promise((resolve) => {
    pending[id] = resolve;
    ws.send(JSON.stringify({ id, method: 'Target.sendMessageToTarget', params: {
      sessionId,
      message: JSON.stringify({ id, method, params })
    }}));
  });
}

ws.on('open', async () => {
  try {
    const targets = await send('Target.getTargets');
    const pageTarget = targets.result.targetInfos.find(t => t.type === 'page' && t.url.includes('8852'));
    if (!pageTarget) { console.error('No page target for 8852'); ws.close(); return; }

    const attached = await send('Target.attachToTarget', { targetId: pageTarget.targetId, flatten: true });
    sessionId = attached.result.sessionId;

    if (action === 'click-menu') {
      const result = await pageSend('Runtime.evaluate', {
        expression: `
          (function() {
            const allEls = document.querySelectorAll('[class*=menu] span, [class*=menu] a, [class*=sidebar] span');
            for (const el of allEls) {
              if (el.textContent.trim() === '客户端面板' && el.offsetParent !== null) {
                el.click();
                return 'Clicked: ' + el.textContent.trim();
              }
            }
            // Try exact text match on any visible leaf element
            const all = document.querySelectorAll('*');
            for (const el of all) {
              if (el.offsetParent !== null && el.textContent.trim() === '客户端面板' && el.children.length === 0) {
                el.click();
                return 'Clicked leaf: ' + el.tagName;
              }
            }
            return 'Not found. Body text chunk: ' + document.body.innerText.substring(0, 1000).replace(/\\n/g,' | ');
          })()
        `
      });
      console.log(result.result?.result?.value || JSON.stringify(result));
      await new Promise(r => setTimeout(r, 2000));
      const urlResult = await pageSend('Runtime.evaluate', { expression: 'window.location.href' });
      console.log('URL:', urlResult.result?.result?.value);
    } else if (action === 'nav-soft') {
      await pageSend('Runtime.evaluate', { expression: "window.location.hash = '#/soft/catalog'" });
      console.log('Navigated');
      await new Promise(r => setTimeout(r, 2000));
      const urlResult = await pageSend('Runtime.evaluate', { expression: 'window.location.href' });
      console.log('URL:', urlResult.result?.result?.value);
    }
  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 1000);
});

ws.on('error', (e) => { console.error('WS error:', e.message); });
