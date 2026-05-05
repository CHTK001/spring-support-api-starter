// CDP - Extract detailed page content
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

    // First navigate to panel
    await page('Runtime.evaluate', { expression: "window.location.hash = '#/panel/index'" });
    await new Promise(r => setTimeout(r, 3000));

    // Extract detailed page content
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var results = [];
        results.push('URL: ' + window.location.href);

        // Get ALL visible text from the main content area
        var main = document.querySelector('.main-content') ||
                   document.querySelector('main') ||
                   document.querySelector('.el-main') ||
                   document.querySelector('[class*=content]') ||
                   document.querySelector('#app > div > div > div:last-child');
        if(!main) main = document.body;

        // Get the main content text
        var allEls = main.querySelectorAll('*');
        var visibleTexts = [];
        allEls.forEach(function(el) {
          if(el.children.length === 0 && el.offsetParent !== null) {
            var t = (el.textContent || '').trim();
            if(t.length > 0) visibleTexts.push(t);
          }
        });
        results.push('=== Visible text nodes ===');
        results.push(visibleTexts.join(' | '));

        // Also get innerHTML of main content area (first 5000 chars)
        if(main) {
          results.push('=== Main HTML ===');
          var html = main.innerHTML.substring(0, 5000);
          results.push(html);
        }

        return results.join('\\\\n');
      })()
    `});
    console.log(r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
