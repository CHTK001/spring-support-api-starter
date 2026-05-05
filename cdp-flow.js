// CDP - Full flow: hub -> service panel -> software manage -> click Java 8
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

    // Make sure we're on the panel page
    console.log('URL:', (await page('Runtime.evaluate', { expression: 'window.location.href' })).result?.result?.value);

    // Get ALL text to find what's on the page
    let r = await page('Runtime.evaluate', { expression: '(function(){var t=document.body.innerText||"";return t.replace(/\\xa0/g," ").substring(0,5000);})()' });
    console.log('TEXT:', r.result?.result?.value);

    // Try to find the "服务面板" element by text
    r = await page('Runtime.evaluate', { expression: `
      (function(){
        var all = document.querySelectorAll('*');
        for(var i=0; i<all.length; i++) {
          var el = all[i];
          if(el.children.length === 0 && el.offsetParent !== null) {
            var t = (el.textContent || '').trim();
            if(t === '服务面板') {
              var parent = el.parentElement;
              if(parent && parent.tagName === 'BUTTON') {
                parent.click();
                return 'Clicked BUTTON parent: ' + parent.className;
              }
              el.click();
              return 'Clicked text node: ' + el.tagName + ' ' + el.className;
            }
          }
        }
        return 'NOT FOUND - searching for partial match:';
      })()
    `});
    console.log('CLICK RESULT:', r.result?.result?.value);
    await new Promise(r => setTimeout(r, 3000));

    // Check what happened
    r = await page('Runtime.evaluate', { expression: '(function(){var t=document.body.innerText||"";return t.replace(/\\xa0/g," ").substring(0,3000);})()' });
    console.log('AFTER CLICK:', r.result?.result?.value);

    // Try the second approach - set viewMode directly
    r = await page('Runtime.evaluate', { expression: `
      (function(){
        var app = document.querySelector('#app');
        var vueApp = app.__vue_app__;
        function walk(instance, depth, maxD) {
          if(depth > maxD || !instance) return null;
          var ss = instance.setupState;
          if(ss) { try { if(Object.keys(ss).includes('viewMode')) return {inst:instance,name:(instance.type&&(instance.type.name||instance.type.__name||instance.type.__file))||'unknown'}; } catch(e) {} }
          var st = instance.subTree;
          if(st) {
            if(st.component) { var f=walk(st.component,depth+1,maxD); if(f) return f; }
            if(st.children && Array.isArray(st.children)) { for(var i=0;i<st.children.length;i++) { if(st.children[i]&&st.children[i].component) { var f=walk(st.children[i].component,depth+1,maxD); if(f) return f; } } }
          }
          return null;
        }
        var found = walk(vueApp._instance, 0, 15);
        if(found) {
          return 'Found at component: ' + found.name;
        }
        return 'NOT FOUND in component tree';
      })()
    `});
    console.log('TREE SEARCH:', r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
