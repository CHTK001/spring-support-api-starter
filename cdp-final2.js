// CDP - Set viewMode on PanelPage via proxy for reactivity
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

    // Find PanelPage by >100 keys and use its proxy to set viewMode
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var app = document.querySelector('#app'); var vueApp = app.__vue_app__; var root = vueApp._instance;
        var panelInst = null;
        function walk(inst, depth) { if(!inst || depth > 8 || panelInst) return; try { if(inst.setupState && Object.keys(inst.setupState).length > 100) { panelInst = inst; return; } } catch(e) {} var st = inst.subTree; if(st) { if(st.component) walk(st.component, depth+1); if(st.children) { for(var i=0; i<st.children.length; i++) { if(st.children[i] && st.children[i].component) walk(st.children[i].component, depth+1); } } } }
        walk(root, 0);
        if(!panelInst) return 'NOT FOUND';
        // Use render proxy to set viewMode
        var proxy = panelInst.proxy;
        proxy.viewMode = 'soft-manage';
        return 'OK via proxy, viewMode=' + proxy.viewMode + ', setupState=' + panelInst.setupState.viewMode;
      })()
    `});
    console.log('SET:', r.result?.result?.value);

    await new Promise(r => setTimeout(r, 5000));

    // Get page text
    r = await page('Runtime.evaluate', { expression: "(function(){return(document.body.innerText||'').replace(/\\xa0/g,' ').substring(0,3000);})()" });
    console.log('TEXT:', r.result?.result?.value);

    // Find preset cards
    r = await page('Runtime.evaluate', { expression: "(function(){var c = document.querySelectorAll('.preset-card'); var names = []; c.forEach(function(e){ var n=e.querySelector('.preset-card-name'); if(n) names.push(n.textContent.trim()); }); return 'cards(' + c.length + '): ' + names.join(', ');})()" });
    console.log('CARDS:', r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
