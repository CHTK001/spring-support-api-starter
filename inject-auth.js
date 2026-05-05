// Inject auth state into browser via CDP
const WebSocket = require('ws');
const cdpUrl = process.argv[2];
if (!cdpUrl) { console.error('Usage: node inject-auth.js <cdp-url>'); process.exit(1); }

async function main() {
  const ws = new WebSocket(cdpUrl);

  ws.on('open', async () => {
    try {
      // 1. Get targets (pages)
      const { result: { targetInfos } } = await send(ws, {
        id: 1, method: 'Target.getTargets'
      });

      // Find the page target
      const pageTarget = targetInfos.find(t => t.type === 'page' && t.url.includes('8852'));
      if (!pageTarget) {
        console.error('No page target found');
        ws.close();
        return;
      }

      // 2. Attach to page
      const { result: { sessionId } } = await send(ws, {
        id: 2, method: 'Target.attachToTarget',
        params: { targetId: pageTarget.targetId, flatten: true }
      });

      // Helper to send commands to the page session
      const pageSend = (id, method, params) => {
        return new Promise((resolve, reject) => {
          const msgId = id + 100;
          const handler = (data) => {
            const parsed = JSON.parse(data.toString());
            if (parsed.id === msgId) resolve(parsed);
          };
          ws.on('message', handler);
          ws.send(JSON.stringify({ id: msgId, method: 'Target.sendMessageToTarget', params: {
            sessionId, message: JSON.stringify({ id: msgId, method, params })
          }}));
          setTimeout(() => { ws.removeListener('message', handler); reject(new Error('timeout')); }, 5000);
        });
      };

      // 3. Set localStorage
      const tokenPayload = JSON.stringify({
        accessToken: 'mock-token-cdp-' + Date.now(),
        expires: 0,
        refreshToken: 'mock-refresh-' + Date.now()
      });

      const userInfoPayload = JSON.stringify({
        accessToken: tokenPayload,
        refreshToken: '',
        expires: 0,
        isRemembered: true,
        userInfo: {
          sysUserId: 1,
          sysUserUsername: 'sa',
          sysUserNickname: '超级管理员',
          roles: ['ADMIN', 'SUPER_ADMIN'],
          perms: ['*:*:*']
        },
        roles: ['ADMIN', 'SUPER_ADMIN'],
        perms: ['*:*:*']
      });

      // Set localStorage
      await pageSend(3, 'Runtime.evaluate', {
        expression: `localStorage.setItem('user-info', ${JSON.stringify(userInfoPayload)})`
      });
      console.log('localStorage set');

      // Set cookies via document.cookie (works for same domain)
      await pageSend(4, 'Runtime.evaluate', {
        expression: `document.cookie = "authorized-token=${encodeURIComponent(tokenPayload)}; path=/; max-age=86400"`
      });
      console.log('authorized-token cookie set');

      await pageSend(5, 'Runtime.evaluate', {
        expression: `document.cookie = "multiple-tabs=true; path=/; max-age=86400"`
      });
      console.log('multiple-tabs cookie set');

      // Verify
      await pageSend(6, 'Runtime.evaluate', {
        expression: `console.log('Auth injected via CDP:', document.cookie)`
      });

      console.log('Auth injection complete!');

      // 4. Navigate to soft catalog
      await pageSend(7, 'Runtime.evaluate', {
        expression: `window.location.hash = '#/soft/catalog'`
      });
      console.log('Navigated to /soft/catalog');

    } catch(e) {
      console.error('Error:', e.message);
    }

    setTimeout(() => ws.close(), 3000);
  });

  ws.on('error', (e) => console.error('WS error:', e.message));
}

function send(ws, msg) {
  return new Promise((resolve, reject) => {
    const handler = (data) => {
      const parsed = JSON.parse(data.toString());
      if (parsed.id === msg.id) {
        ws.removeListener('message', handler);
        resolve(parsed);
      }
    };
    ws.on('message', handler);
    ws.send(JSON.stringify(msg));
    setTimeout(() => { ws.removeListener('message', handler); reject(new Error('timeout')); }, 5000);
  });
}

main();
