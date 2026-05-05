// CDP - Test Java 8 preset
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

    // Navigate to panel/index and set viewMode
    await page('Runtime.evaluate', { expression: "window.location.hash = '#/panel/index'" });
    await new Promise(r => setTimeout(r, 3000));

    // Find and set viewMode
    await page('Runtime.evaluate', { expression: `
      (function(){
        var app = document.querySelector('#app');
        var vueApp = app.__vue_app__;
        function walk(instance, depth, maxD) {
          if(depth > maxD || !instance) return null;
          var ss = instance.setupState;
          if(ss) { try {
            if(Object.keys(ss).includes('viewMode')) return instance;
          } catch(e) {} }
          var st = instance.subTree;
          if(st) {
            if(st.component) { var f = walk(st.component, depth+1, maxD); if(f) return f; }
            if(st.children && Array.isArray(st.children)) { for(var i=0;i<st.children.length;i++) { if(st.children[i]&&st.children[i].component) { var f=walk(st.children[i].component,depth+1,maxD); if(f) return f; } } }
          }
          return null;
        }
        var inst = walk(vueApp._instance, 0, 15);
        if(inst) inst.setupState.viewMode = 'soft-manage';
      })()
    `});
    await new Promise(r => setTimeout(r, 3000));

    // Click on Java 8 card
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var results = [];
        try {
          // Find Java 8 card
          var cards = document.querySelectorAll('.preset-card');
          for(var i = 0; i < cards.length; i++) {
            var nameEl = cards[i].querySelector('.preset-card-name');
            if(nameEl && nameEl.textContent.trim() === 'Java 8') {
              results.push('Found Java 8 card');
              cards[i].click();
              results.push('Clicked Java 8');
              break;
            }
          }
          if(results.length === 1) {
            results.push('Java 8 card not found by name, listing:');
            cards.forEach(function(c) {
              var n = c.querySelector('.preset-card-name');
              results.push('- ' + (n ? n.textContent.trim() : 'unnamed'));
            });
          }
        } catch(e) { results.push('error: ' + e.message); }
        return results.join('\\\\n');
      })()
    `});
    console.log(r.result?.result?.value);
    await new Promise(r => setTimeout(r, 1500));

    // Now the detail dialog should be open - extract its content
    r = await page('Runtime.evaluate', { expression: `
      (function(){
        var results = [];
        // Get all visible text
        var body = document.body;
        var text = body ? (body.innerText || body.textContent || '') : '';
        results.push('=== FULL TEXT ===');
        results.push(text.substring(0, 5000).replace(/\\\\n/g,' | '));

        // Try to get dialog content specifically
        var dialogs = document.querySelectorAll('.el-dialog, .el-overlay-dialog');
        results.push('\\\\n=== DIALOGS ===');
        dialogs.forEach(function(d, i) {
          results.push('Dialog ' + i + ': ' + (d.textContent || '').trim().substring(0, 1000).replace(/\\\\n/g,' | '));
        });

        return results.join('\\\\n');
      })()
    `});
    console.log(r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
