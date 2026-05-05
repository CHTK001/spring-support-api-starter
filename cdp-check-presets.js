// CDP - Navigate to soft-manage and check presets
const ws = new (require('ws'))(process.argv[2]);
let id = 1, sid = null;
function page(m, p) { const msgId=id++; return new Promise(r=>{const h=(raw)=>{const m=JSON.parse(raw.toString());if(m.id===msgId){ws.removeListener('message',h);r(m);}};ws.on('message',h);ws.send(JSON.stringify({id:msgId,sessionId:sid,method:m,params:p}));setTimeout(()=>{ws.removeListener('message',h);r({error:'timeout'});},15000);}); }
function send(m,p) { const msgId=id++; return new Promise(r=>{const h=(raw)=>{const m=JSON.parse(raw.toString());if(m.id===msgId){ws.removeListener('message',h);r(m);}};ws.on('message',h);ws.send(JSON.stringify({id:msgId,method:m,params:p}));setTimeout(()=>{ws.removeListener('message',h);r({error:'timeout'});},5000);}); }

ws.on('open', async () => {
  try {
    const targets = await send('Target.getTargets');
    const t = targets.result.targetInfos.find(t => t.type === 'page' && t.url.includes('8852'));
    if (!t) { console.error('No page'); ws.close(); return; }
    const attach = await send('Target.attachToTarget', { targetId: t.targetId, flatten: true });
    sid = attach.result.sessionId;

    // Navigate to panel/index
    await page('Runtime.evaluate', { expression: "window.location.hash = '#/panel/index'" });
    await new Promise(r => setTimeout(r, 2000));

    // Set viewMode to soft-manage
    await page('Runtime.evaluate', { expression: `
      (function(){
        var app = document.querySelector('#app');
        var vueApp = app.__vue_app__;
        function walk(instance, depth, maxD) {
          if(depth > maxD || !instance) return null;
          var ss = instance.setupState;
          if(ss) { try { if(Object.keys(ss).includes('viewMode')) return instance; } catch(e) {} }
          var st = instance.subTree;
          if(st) {
            if(st.component) { var f = walk(st.component, depth+1, maxD); if(f) return f; }
            if(st.children && Array.isArray(st.children)) { for(var i=0;i<st.children.length;i++) { if(st.children[i]&&st.children[i].component) { var f=walk(st.children[i].component,depth+1,maxD); if(f) return f; } } }
          }
          return null;
        }
        var inst = walk(vueApp._instance, 0, 15);
        if(inst) { inst.setupState.viewMode = 'soft-manage'; return 'SET'; }
        return 'NOT FOUND';
      })()
    `});

    // Wait for Vue to render and presets to load
    await new Promise(r => setTimeout(r, 5000));

    // Get the full page content
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var text = document.body.innerText || document.body.textContent || '';
        return text.replace(/\\xa0/g, ' ').substring(0, 4000);
      })()
    `});
    console.log('=== PAGE TEXT ===');
    console.log(r.result?.result?.value);

    // Also try getting the preset cards if they exist
    r = await page('Runtime.evaluate', { expression: `
      (function(){
        var cards = document.querySelectorAll('.preset-card');
        var info = [];
        cards.forEach(function(c) {
          var name = c.querySelector('.preset-card-name');
          var code = c.querySelector('.preset-card-code');
          var tags = c.querySelectorAll('.el-tag');
          var tagTexts = [];
          tags.forEach(function(t) { tagTexts.push(t.textContent.trim()); });
          info.push((name ? name.textContent.trim() : '?') + ' (' + (code ? code.textContent.trim() : '?') + ') tags: ' + tagTexts.join(', '));
        });
        return info.length > 0 ? info.join(' | ') : 'NO CARDS FOUND';
      })()
    `});
    console.log('=== CARDS ===');
    console.log(r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
