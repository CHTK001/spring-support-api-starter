// CDP - Test the API call and diagnose issues
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

    // Navigate to panel and set viewMode
    await page('Runtime.evaluate', { expression: "window.location.hash = '#/panel/index'" });
    await new Promise(r => setTimeout(r, 2000));
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
        if(inst) { inst.setupState.viewMode = 'soft-manage'; return 'OK'; }
        return 'NOT FOUND';
      })()
    `});
    await new Promise(r => setTimeout(r, 2000));

    // Try direct API call from the browser
    let r = await page('Runtime.evaluate', { expression: `
      fetch('/monitor/api/v2/project/container/software/preset/list', { credentials: 'include' })
        .then(function(r) { return r.text(); })
        .then(function(t) { return t.substring(0, 500); })
        .catch(function(e) { return 'error: ' + e.message; })
    `});
    console.log('API result:', r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
