// CDP - Navigate to panel and explore
const ws = new (require('ws'))(process.argv[2]);
let id = 1, sid = null;
function page(m, p) { const msgId=id++; return new Promise(r=>{const h=(raw)=>{const m=JSON.parse(raw.toString());if(m.id===msgId){ws.removeListener('message',h);r(m);}};ws.on('message',h);ws.send(JSON.stringify({id:msgId,sessionId:sid,method:m,params:p}));setTimeout(()=>{ws.removeListener('message',h);r({error:'timeout'});},10000);}); }
function send(m,p) { const msgId=id++; return new Promise(r=>{const h=(raw)=>{const m=JSON.parse(raw.toString());if(m.id===msgId){ws.removeListener('message',h);r(m);}};ws.on('message',h);ws.send(JSON.stringify({id:msgId,method:m,params:p}));setTimeout(()=>{ws.removeListener('message',h);r({error:'timeout'});},10000);}); }

ws.on('open', async () => {
  try {
    const targets = await send('Target.getTargets');
    const t = targets.result.targetInfos.find(t => t.type === 'page' && t.url.includes('8852'));
    if (!t) { console.error('No page'); ws.close(); return; }
    const attach = await send('Target.attachToTarget', { targetId: t.targetId, flatten: true });
    sid = attach.result.sessionId;

    // Navigate to panel index via hash
    await page('Runtime.evaluate', { expression: "window.location.hash = '#/panel/index'" });
    console.log('Navigated to #/panel/index');
    await new Promise(r => setTimeout(r, 3000));

    // Check current state
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var results = [];
        results.push('hash: ' + window.location.hash);
        results.push('title: ' + document.title);
        // Get visible text content (first 2000 chars)
        var body = document.body;
        if(body) {
          var text = body.innerText || body.textContent || '';
          results.push('bodyText: ' + text.substring(0, 2000).replace(/\\\\n/g,' | '));
        }
        return results.join('\\\\n');
      })()
    `});
    console.log(r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
