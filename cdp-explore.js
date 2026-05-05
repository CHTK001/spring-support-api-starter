// CDP - Explore Vue app routes and sidebar
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

    // Get all sidebar/menu links and their routes
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        try {
          var results = [];
          // Find Vue router instance
          var app = document.querySelector('#app');
          var vueApp = app && app.__vue_app__;
          var router = null;
          if(vueApp) {
            router = vueApp.config.globalProperties.$router;
          }
          if(!router && vueApp && vueApp._instance && vueApp._instance.appContext) {
            router = vueApp._instance.appContext.config.globalProperties.$router;
          }
          if(router) {
            var routes = router.getRoutes();
            results.push('=== VUE ROUTES ===');
            routes.forEach(function(rt) {
              if(rt.path && rt.path !== '/' && !rt.path.includes(':') && rt.path.indexOf('/') >= 0) {
                results.push(rt.path + (rt.meta && rt.meta.title ? ' -> ' + rt.meta.title : ''));
              }
            });
          } else {
            results.push('no router found');
          }

          // Find sidebar menu items
          results.push('=== SIDEBAR ITEMS ===');
          var all = document.querySelectorAll('.first-level-menu-item, .el-menu-item, .sub-menu-item, [class*=menu] a, [class*=sidebar] a, [class*=MenuItem]');
          all.forEach(function(el) {
            var text = (el.textContent || '').trim().substring(0, 50);
            var href = el.getAttribute('href') || '';
            var cls = el.className || '';
            if(text && text.length > 0) {
              results.push(text + ' | href=' + href + ' | class=' + cls.substring(0,60));
            }
          });

          // Also get ALL links in the page
          results.push('=== ALL <a> TAGS ===');
          var links = document.querySelectorAll('a');
          links.forEach(function(a) {
            var text = (a.textContent || '').trim().substring(0, 40);
            var href = a.getAttribute('href') || '';
            if(href || text) {
              results.push(text + ' -> ' + href);
            }
          });

          // Get window hash
          results.push('=== CURRENT ===');
          results.push('hash: ' + window.location.hash);
          results.push('href: ' + window.location.href);
          results.push('title: ' + document.title);

          return results.join('\\n');
        } catch(e) { return 'error: ' + e.message; }
      })()
    `});
    console.log(r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
