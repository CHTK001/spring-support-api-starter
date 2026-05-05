// CDP - Force viewMode via setupState (not proxy)
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

    // Navigate to panel
    await page('Runtime.evaluate', { expression: "window.location.hash = '#/panel/index'" });
    await new Promise(r => setTimeout(r, 2000));

    // Set viewMode via setupState ONLY (not proxy)
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var app = document.querySelector('#app');
        var vueApp = app.__vue_app__;
        var root = vueApp._instance;

        function findIndex(inst, depth) {
          if(depth > 10 || !inst) return null;
          var type = inst.type;
          var name = type ? (type.name || type.__name || type.__file || '') : '';
          if(name === 'index') return {inst: inst, name: name};
          var st = inst.subTree;
          if(st) {
            if(st.component) { var f = findIndex(st.component, depth+1); if(f) return f; }
            if(st.children) { for(var i=0; i<st.children.length; i++) { if(st.children[i]&&st.children[i].component) { var f = findIndex(st.children[i].component, depth+1); if(f) return f; } } }
          }
          return null;
        }

        var found = findIndex(root, 0);
        if(!found) return 'not found';
        var inst = found.inst;

        // Use setupState to set viewMode (this goes through proxyRefs)
        var ss = inst.setupState;
        if(!ss) return 'no setupState';

        // Set viewMode through the setupState proxy
        // In Vue3, proxyRefs setter should set ref.value
        ss.viewMode = 'soft-manage';

        // Verify
        var current = ss.viewMode;
        return 'Set via setupState, now: ' + current;
      })()
    `});
    console.log('SET:', r.result?.result?.value);

    await new Promise(r => setTimeout(r, 5000));

    // Check rendered content
    r = await page('Runtime.evaluate', { expression: "(function(){var t=document.body.innerText||'';return t.replace(/\\xa0/g,' ').substring(0,3000);})()" });
    console.log('TEXT:', r.result?.result?.value);

    // Check DOM elements
    r = await page('Runtime.evaluate', { expression: "(function(){return JSON.stringify({cards:document.querySelectorAll('.preset-card').length,loading:document.querySelectorAll('.manage-loading').length,empty:document.querySelectorAll('.manage-empty').length,grid:document.querySelectorAll('.manage-grid').length});})()" });
    console.log('DOM:', r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
