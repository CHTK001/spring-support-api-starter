// CDP - Set viewMode to soft-manage and then explore
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

    // Try to set viewMode to 'soft-manage' via Vue internals
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var results = [];
        try {
          // Try Vue 3 approach - get the app instance and find the panel component
          var app = document.querySelector('#app');
          var vueApp = app.__vue_app__;
          if(!vueApp) return 'no vue app';

          // Access the root component instance
          var root = vueApp._instance;
          if(!root) return 'no root instance';

          // Try to find the panel component in the subtree
          // The panel component is the one with viewMode ref
          function findViewMode(subtree, depth) {
            if(depth > 10) return null;
            // Check setupState
            if(subtree.setupState && 'viewMode' in subtree.setupState) {
              return subtree.setupState;
            }
            // Check in subTree
            if(subtree.subTree && subtree.subTree.component) {
              var found = findViewMode(subtree.subTree.component, depth + 1);
              if(found) return found;
            }
            // Check children
            var child = subtree.children;
            if(child) {
              for(var i = 0; i < child.length; i++) {
                if(child[i].component) {
                  var found = findViewMode(child[i].component, depth + 1);
                  if(found) return found;
                }
              }
            }
            // Check vnode children
            if(subtree.vnode && subtree.vnode.children && Array.isArray(subtree.vnode.children)) {
              for(var i = 0; i < subtree.vnode.children.length; i++) {
                var c = subtree.vnode.children[i];
                if(c && c.component) {
                  var found = findViewMode(c.component, depth + 1);
                  if(found) return found;
                }
              }
            }
            return null;
          }

          var state = findViewMode(root, 0);
          if(state) {
            state.viewMode = 'soft-manage';
            results.push('Set viewMode to soft-manage');
          } else {
            results.push('Could not find viewMode state - trying alternative...');
          }
        } catch(e) { results.push('error: ' + e.message); }

        results.push('hash: ' + window.location.hash);
        return results.join('\\\\n');
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
        return text.substring(0, 3000).replace(/\\\\n/g,' | ');
      })()
    `});
    console.log('Page text:', r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
