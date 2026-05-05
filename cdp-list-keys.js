// CDP - List all keys in the panel component's setupState
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

    // Stay at panel/index (already there from before)
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var app = document.querySelector('#app');
        var vueApp = app.__vue_app__;
        var root = vueApp._instance;

        // Walk the entire component tree and list all component names and their setupState key count
        var components = [];

        function walk(inst, depth) {
          if(!inst || depth > 8) return;
          var type = inst.type;
          var name = type ? (type.name || type.__name || type.__file || 'unnamed') : 'unnamed';
          var keyCount = 0;
          var hasViewMode = false;
          try {
            if(inst.setupState) {
              var keys = Object.keys(inst.setupState);
              keyCount = keys.length;
              hasViewMode = keys.includes('viewMode');
            }
          } catch(e) {}
          components.push({name: name, depth: depth, keys: keyCount, hasViewMode: hasViewMode});

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

        var result = components.map(function(c) {
          return '  '.repeat(c.depth) + c.name + ' (keys:' + c.keys + ', viewMode:' + c.hasViewMode + ')';
        });
        return result.join('\\n');
      })()
    `});
    console.log('COMPONENT TREE:');
    console.log(r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
