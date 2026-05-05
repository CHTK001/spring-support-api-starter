const ws = new (require('ws'))(process.argv[2]);
let id = 1, sid = null;
function page(m, p) { const msgId=id++; return new Promise(r=>{const h=(raw)=>{const d=JSON.parse(raw.toString());if(d.id===msgId){ws.removeListener('message',h);r(d);}};ws.on('message',h);ws.send(JSON.stringify({id:msgId,sessionId:sid,method:m,params:p}));setTimeout(()=>{ws.removeListener('message',h);r({error:'timeout'});},15000);}); }
function send(m,p) { const msgId=id++; return new Promise(r=>{const h=(raw)=>{const d=JSON.parse(raw.toString());if(d.id===msgId){ws.removeListener('message',h);r(d);}};ws.on('message',h);ws.send(JSON.stringify({id:msgId,method:m,params:p}));setTimeout(()=>{ws.removeListener('message',h);r({error:'timeout'});},5000);}); }

ws.on('open', async () => {
  try {
    const targets = await send('Target.getTargets');
    const t = targets.result.targetInfos.find(ti => ti.type === 'page' && ti.url.includes('8852'));
    if (!t) { console.error('No page'); ws.close(); return; }
    const attach = await send('Target.attachToTarget', { targetId: t.targetId, flatten: true });
    sid = attach.result.sessionId;

    await page('Runtime.evaluate', { expression: "window.location.hash = '#/panel/index'" });
    await new Promise(r => setTimeout(r, 5000));

    // Check component tree for viewMode
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var app = document.querySelector('#app');
        var root = app.__vue_app__._instance;
        var pi = null;
        function w(i,d){if(!i||d>8||pi)return;try{if(i.setupState&&typeof i.setupState.handleServiceNavigate==='function'){pi=i;return;}}catch(e){}var st=i.subTree;if(st){if(st.component)w(st.component,d+1);if(st.children)for(var j=0;j<st.children.length;j++)if(st.children[j]&&st.children[j].component)w(st.children[j].component,d+1);}}
        w(root,0);
        if(!pi)return 'NF';
        var r=[];
        r.push('ssVM:'+pi.setupState.viewMode);
        r.push('upd:'+(typeof pi.update));
        r.push('eff:'+(pi.effect?'Y':'N'));
        if(pi.scope&&pi.scope.effects){for(var i=0;i<pi.scope.effects.length;i++){var e=pi.scope.effects[i];r.push('e'+i+'d:'+e.dirty+'a:'+e.active);}}
        return r.join('|');
      })()
    `});
    console.log('BEFORE:', r.result.result.value);

    // Call handleServiceNavigate
    r = await page('Runtime.evaluate', { expression: `
      (function(){
        var app = document.querySelector('#app');
        var root = app.__vue_app__._instance;
        var pi = null;
        function w(i,d){if(!i||d>8||pi)return;try{if(i.setupState&&typeof i.setupState.handleServiceNavigate==='function'){pi=i;return;}}catch(e){}var st=i.subTree;if(st){if(st.component)w(st.component,d+1);if(st.children)for(var j=0;j<st.children.length;j++)if(st.children[j]&&st.children[j].component)w(st.children[j].component,d+1);}}
        w(root,0);
        pi.setupState.handleServiceNavigate('soft-manage');
        var r=[];
        r.push('afterVM:'+pi.setupState.viewMode);
        if(pi.scope&&pi.scope.effects){for(var i=0;i<pi.scope.effects.length;i++){var e=pi.scope.effects[i];r.push('e'+i+'d:'+e.dirty+'a:'+e.active);}}
        // Also try calling update
        if(typeof pi.update==='function'){pi.update();r.push('calledUpdate');}
        return r.join('|');
      })()
    `});
    console.log('SET:', r.result.result.value);

    await new Promise(r => setTimeout(r, 3000));

    r = await page('Runtime.evaluate', { expression: "(function(){var r=document.querySelector('.panel-root');return r?r.innerHTML.substring(0,300):'NOROOT';})()" });
    console.log('DOM:', r.result.result.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
