// CDP - Debug preset loading
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

    // Set viewMode to soft-manage
    await page('Runtime.evaluate', { expression: "(function(){var app=document.querySelector('#app');var vueApp=app.__vue_app__;function walk(instance,depth,maxD){if(depth>maxD||!instance)return null;var ss=instance.setupState;if(ss){try{if(Object.keys(ss).includes('viewMode'))return instance;}catch(e){}}var st=instance.subTree;if(st){if(st.component){var f=walk(st.component,depth+1,maxD);if(f)return f;}if(st.children&&Array.isArray(st.children)){for(var i=0;i<st.children.length;i++){if(st.children[i]&&st.children[i].component){var f=walk(st.children[i].component,depth+1,maxD);if(f)return f;}}}}return null;}var inst=walk(vueApp._instance,0,15);if(inst){inst.setupState.viewMode='soft-manage';return 'SET';}return 'NOT FOUND';})()" });
    await new Promise(r => setTimeout(r, 5000));

    // Check component state and presets
    let r = await page('Runtime.evaluate', { expression: "(function(){var results=[];results.push('tabPane: '+(document.querySelector('.el-tab-pane')?'found':'not found'));results.push('presetCards: '+document.querySelectorAll('.preset-card').length);results.push('grid: '+(document.querySelector('.manage-grid')?'found':'not found'));results.push('empty: '+(document.querySelector('.manage-empty')?'found':'not found'));results.push('loading: '+(document.querySelector('.manage-loading')?'found':'not found'));return results.join('\\n');})()" });
    console.log('COMPONENT:', r.result?.result?.value);

    // Now try clicking the hub "服务面板" entry instead
    await page('Runtime.evaluate', { expression: "(function(){var app=document.querySelector('#app');var vueApp=app.__vue_app__;function walk(instance,depth,maxD){if(depth>maxD||!instance)return null;var ss=instance.setupState;if(ss){try{if(Object.keys(ss).includes('viewMode'))return instance;}catch(e){}}var st=instance.subTree;if(st){if(st.component){var f=walk(st.component,depth+1,maxD);if(f)return f;}if(st.children&&Array.isArray(st.children)){for(var i=0;i<st.children.length;i++){if(st.children[i]&&st.children[i].component){var f=walk(st.children[i].component,depth+1,maxD);if(f)return f;}}}}return null;}var inst=walk(vueApp._instance,0,15);if(inst){inst.setupState.viewMode='hub';return 'SET HUB';}return 'NOT FOUND';})()" });
    await new Promise(r => setTimeout(r, 2000));

    // Now click the "服务面板" entry
    r = await page('Runtime.evaluate', { expression: "(function(){var entries=document.querySelectorAll('.service-panel-entry');if(entries.length>0){entries[0].click();return 'clicked service panel entry';}return 'no service panel entry found';})()" });
    console.log('CLICK:', r.result?.result?.value);
    await new Promise(r => setTimeout(r, 3000));

    // Check current view
    r = await page('Runtime.evaluate', { expression: "(function(){var text=document.body.innerText||document.body.textContent||'';return text.substring(0,3000).replace(/\\xa0/g,' ');})()" });
    console.log('AFTER CLICK:', r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
