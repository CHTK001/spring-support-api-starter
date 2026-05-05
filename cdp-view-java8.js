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

    // Make sure we're at panel/index with correct viewMode
    await page('Runtime.evaluate', { expression: "window.location.hash = '#/panel/index'" });
    await new Promise(r => setTimeout(r, 2000));

    // Set viewMode on the PanelPage component
    await page('Runtime.evaluate', { expression: `
      (function(){
        var app = document.querySelector('#app'); var vueApp = app.__vue_app__; var root = vueApp._instance;
        function walk(inst, depth) { if(!inst || depth > 8) return null; try { if(inst.setupState && Object.keys(inst.setupState).length > 100) return inst; } catch(e) {} var st = inst.subTree; if(st) { if(st.component) { var f = walk(st.component, depth+1); if(f) return f; } if(st.children) { for(var i=0; i<st.children.length; i++) { if(st.children[i] && st.children[i].component) { var f = walk(st.children[i].component, depth+1); if(f) return f; } } } } return null; }
        var inst = walk(root, 0);
        if(inst) { inst.setupState.viewMode = 'soft-manage'; return 'OK'; }
        return 'NOT FOUND';
      })()
    `});
    await new Promise(r => setTimeout(r, 3000));

    // Click on Java 8 card
    let r = await page('Runtime.evaluate', { expression: `
      (function(){
        var cards = document.querySelectorAll('.preset-card');
        for(var i=0; i<cards.length; i++) {
          var name = cards[i].querySelector('.preset-card-name');
          if(name && name.textContent.trim() === 'Java 8') {
            cards[i].click();
            return 'Clicked Java 8 card';
          }
        }
        return 'Java 8 card not found';
      })()
    `});
    console.log(r.result?.result?.value);
    await new Promise(r => setTimeout(r, 2000));

    // The detail dialog should be open now - get its content
    r = await page('Runtime.evaluate', { expression: `
      (function(){
        var results = [];
        // Find dialog
        var dialogs = document.querySelectorAll('.el-dialog');
        results.push('Dialogs found: ' + dialogs.length);

        // Get dialog text
        for(var i=0; i<dialogs.length; i++) {
          var text = dialogs[i].textContent || '';
          results.push('Dialog ' + i + ' text:');
          results.push(text.trim().substring(0, 2000).replace(/\\xa0/g, ' '));
        }

        // Also get the body text to see detail info
        var body = document.body.innerText || '';
        results.push('=== BODY ===');
        results.push(body.replace(/\\xa0/g, ' ').substring(0, 3000));

        return results.join('\\n');
      })()
    `});
    console.log('DIALOG CONTENT:');
    console.log(r.result?.result?.value);

  } catch(e) { console.error('Error:', e.message); }
  setTimeout(() => ws.close(), 500);
});
ws.on('error', e => { console.error(e.message); process.exit(1); });
