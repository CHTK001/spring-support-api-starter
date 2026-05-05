// CDP - Find routes and navigate
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

    await page('Runtime.evaluate', { expression: "window.location.hash = '#/'" });
    await new Promise(r => setTimeout(r, 2000));

    // Try to get Vue app routes
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        try {
          var app = document.querySelector('#app');
          // Try to find Vue instance
          var el = app.__vue_app__;
          if(!el) return 'no vue app';
          var router = el.config.globalProperties.$router;
          if(!router) {
            // Try different Vue 3 approach
            var appEl = document.querySelector('#app');
            var vueApp = appEl.__vue_app__;
            if(vueApp && vueApp._instance && vueApp._instance.appContext) {
              var r = vueApp._instance.appContext.config.globalProperties.$router;
              if(r) return 'Routes: ' + r.getRoutes().map(function(rt){return rt.path}).join(', ');
            }
            return 'no router found';
          }
          return 'Routes: ' + router.getRoutes().map(function(rt){return rt.path}).join(', ');
        } catch(e) { return 'error: ' + e.message; }
      })()
    `});
    console.log(r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
