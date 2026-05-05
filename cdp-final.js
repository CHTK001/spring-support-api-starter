// CDP - Target the correct PanelPage component
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

    // Find the PanelPage component (named 'index' with many keys, under RouterView)
    // and set its viewMode to 'soft-manage'
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var app = document.querySelector('#app');
        var vueApp = app.__vue_app__;
        var root = vueApp._instance;

        // Find the PanelPage component (the one with >100 keys)
        var panelInst = null;

        function walk(inst, depth) {
          if(!inst || depth > 8 || panelInst) return;
          try {
            if(inst.setupState) {
              var keys = Object.keys(inst.setupState);
              if(keys.length > 100) {
                panelInst = inst;
                return;
              }
            }
          } catch(e) {}
          var st = inst.subTree;
          if(st) {
            if(st.component) walk(st.component, depth+1);
            if(st.children) {
              for(var i=0; i<st.children.length; i++) {
                if(st.children[i] && st.children[i].component) walk(st.children[i].component, depth+1);
              }
            }
          }
        }

        walk(root, 0);

        if(!panelInst) return 'PanelPage not found';

        var ss = panelInst.setupState;
        ss.viewMode = 'soft-manage';

        // Verify
        var current = ss.viewMode;
        var keyCount = Object.keys(ss).length;
        return 'Set viewMode to ' + current + ' on PanelPage (' + keyCount + ' keys)';
      })()
    `});
    console.log('SET:', r.result?.result?.value);

    await new Promise(r => setTimeout(r, 5000));

    // Check rendered content
    r = await page('Runtime.evaluate', { expression: "(function(){var t=document.body.innerText||'';return t.replace(/\\xa0/g,' ').substring(0,3000);})()" });
    console.log('TEXT:', r.result?.result?.value);

    // Check DOM elements for preset management
    r = await page('Runtime.evaluate', { expression: "(function(){return JSON.stringify({cards:document.querySelectorAll('.preset-card').length,grid:document.querySelectorAll('.manage-grid').length,empty:document.querySelectorAll('.manage-empty').length});})()" });
    console.log('DOM:', r.result?.result?.value);

    // Check for preset card names
    r = await page('Runtime.evaluate', { expression: "(function(){var names=[];document.querySelectorAll('.preset-card-name').forEach(function(e){names.push(e.textContent.trim());});return 'preset names: '+names.join(', ');})()" });
    console.log('NAMES:', r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
