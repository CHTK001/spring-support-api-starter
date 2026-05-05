// CDP - Force viewMode to soft-manage via Vue internals
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

    // Make sure we're at panel/index
    await page('Runtime.evaluate', { expression: "window.location.hash = '#/panel/index'" });
    await new Promise(r => setTimeout(r, 2000));

    // Find the viewMode ref and set it properly
    let r = await page('Runtime.evaluate', { expression: "(function(){var app=document.querySelector('#app');var vueApp=app.__vue_app__;var root=vueApp._instance;function findViewModeRef(inst,depth){if(depth>10||!inst)return null;var type=inst.type;var name=type?(type.name||type.__name||type.__file||''):'';if(name==='index'){var ss=inst.setupState;if(ss){try{var keys=Object.keys(ss);if(keys.includes('viewMode')){var raw=ss.__v_raw||ss;return{ref:ss.viewMode,keys:keys,raw:raw};}}catch(e){}}}var st=inst.subTree;if(st){if(st.component){var f=findViewModeRef(st.component,depth+1);if(f)return f;}if(st.children){for(var i=0;i<st.children.length;i++){if(st.children[i]&&st.children[i].component){var f=findViewModeRef(st.children[i].component,depth+1);if(f)return f;}}}}return null;}var found=findViewModeRef(root,0);if(found){if(found.ref&&found.ref.__v_isRef){found.ref.value='soft-manage';return'Set ref.value to soft-manage';}return'viewMode not a ref, keys: '+found.keys.join(',');}return'Not found';})()" });
    console.log('RESULT:', r.result?.result?.value);

    await new Promise(r => setTimeout(r, 5000));

    // Check what rendered
    r = await page('Runtime.evaluate', { expression: "(function(){var t=document.body.innerText||'';return t.replace(/\\xa0/g,' ').substring(0,3000);})()" });
    console.log('TEXT:', r.result?.result?.value);

    // Check for preset cards
    r = await page('Runtime.evaluate', { expression: "(function(){return 'cards: '+document.querySelectorAll('.preset-card').length+', loading: '+!!document.querySelector('.manage-loading')+', empty: '+!!document.querySelector('.manage-empty')+', grid: '+!!document.querySelector('.manage-grid');})()" });
    console.log('DOM:', r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
