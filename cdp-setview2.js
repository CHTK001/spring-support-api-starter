// CDP - Find panel component and set viewMode
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

    // Navigate to panel/index first
    await page('Runtime.evaluate', { expression: "window.location.hash = '#/panel/index'" });
    await new Promise(r => setTimeout(r, 3000));

    // Try to find and set viewMode by walking Vue component tree
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var results = [];
        try {
          var app = document.querySelector('#app');
          var vueApp = app.__vue_app__;
          if(!vueApp) return 'no vue app';

          // Walk all component instances
          function walk(instance, depth, maxDepth) {
            if(depth > maxDepth || !instance) return null;

            // Check setupState for viewMode
            var ss = instance.setupState;
            if(ss) {
              try {
                // Check if setupState has viewMode
                var keys = Object.keys(ss);
                if(keys.includes('viewMode')) {
                  return { inst: instance, depth: depth, keys: keys };
                }
              } catch(e) {}
            }

            // Try proxy
            try {
              var proxy = instance.proxy;
              if(proxy && 'viewMode' in proxy) {
                return { inst: instance, depth: depth, via: 'proxy' };
              }
            } catch(e) {}

            // Check subTree component
            var st = instance.subTree;
            if(st) {
              if(st.component) {
                var found = walk(st.component, depth+1, maxDepth);
                if(found) return found;
              }
              if(st.children && Array.isArray(st.children)) {
                for(var i = 0; i < st.children.length; i++) {
                  if(st.children[i] && st.children[i].component) {
                    var found = walk(st.children[i].component, depth+1, maxDepth);
                    if(found) return found;
                  }
                }
              }
              // Check dynamicSlots
              if(st.dynamicSlots) {
                for(var i = 0; i < st.dynamicSlots.length; i++) {
                  var slot = st.dynamicSlots[i];
                  if(slot && slot.component) {
                    var found = walk(slot.component, depth+1, maxDepth);
                    if(found) return found;
                  }
                }
              }
            }

            return null;
          }

          var found = walk(vueApp._instance, 0, 15);
          if(found) {
            results.push('Found viewMode at depth ' + found.depth + ' ' + (found.via || 'setupState'));
            var inst = found.inst;
            if(found.via === 'proxy') {
              inst.proxy.viewMode = 'soft-manage';
              results.push('Set via proxy');
            } else {
              inst.setupState.viewMode = 'soft-manage';
              results.push('Set via setupState');
            }
          } else {
            results.push('viewMode not found in component tree');
            // List root component subtree
            var root = vueApp._instance;
            results.push('root type: ' + (root.type ? (root.type.name || root.type.__name || 'unnamed') : 'unknown'));
            function listTree(inst, depth, maxD) {
              if(depth > maxD) return '';
              var s = '';
              var type = inst.type;
              var name = type ? (type.name || type.__name || type.__file || 'unnamed') : 'unknown';
              s += '  '.repeat(depth) + name;
              if(inst.setupState) {
                try {
                  var keys = Object.keys(inst.setupState);
                  s += ' [setupKeys: ' + keys.join(',') + ']';
                } catch(e) { s += ' [setupState hidden]'; }
              }
              s += '\\n';
              var st = inst.subTree;
              if(st) {
                if(st.component) {
                  s += listTree(st.component, depth+1, maxD);
                }
                if(st.children && Array.isArray(st.children)) {
                  for(var i = 0; i < st.children.length; i++) {
                    if(st.children[i] && st.children[i].component) {
                      s += listTree(st.children[i].component, depth+1, maxD);
                    }
                  }
                }
              }
              return s;
            }
            results.push(listTree(root, 0, 8));
          }
        } catch(e) { results.push('error: ' + e.message); }
        return results.join('\\n');
      })()
    `});
    console.log(r.result?.result?.value);

    // Wait for render
    await new Promise(r => setTimeout(r, 2000));

    // Get visible text
    r = await page('Runtime.evaluate', { expression: `
      (function(){
        var body = document.body;
        var text = body ? (body.innerText || body.textContent || '') : '';
        return text.substring(0, 2000).replace(/\\n/g,' | ');
      })()
    `});
    console.log('Page text:', r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
