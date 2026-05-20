with open('client.rs', 'r', encoding='utf-8') as f:
    content = f.read()

# Find and replace handle_local_terminal_conn
old_conn = 'fn handle_local_terminal_conn(mut stream: TcpStream, remote_host: &str, remote_port: u16) {'
idx = content.find(old_conn)
if idx >= 0:
    # Find the end of this function (fn handle_local_terminal_ws starts the next function)
    next_fn = content.find('fn handle_local_terminal_ws(stream', idx)
    if next_fn > idx:
        # Build the replacement
        new_conn = '''fn handle_local_terminal_conn(mut stream: TcpStream, remote_host: &str, remote_port: u16) {
    stream.set_read_timeout(Some(Duration::from_millis(50))).ok();
    stream.set_write_timeout(Some(Duration::from_secs(5))).ok();
    let mut buf = [0u8; 4096];
    let n = match stream.peek(&mut buf) { Ok(n) if n > 8 => n, _ => return };
    let request = String::from_utf8_lossy(&buf[..n.min(512)]);
    let lower = request.to_lowercase();
    if lower.contains("upgrade: websocket") && request.contains("GET /ws ") {
        handle_local_desktop_ws(stream, remote_host, remote_port); return;
    }
    if lower.contains("upgrade: websocket") {
        handle_local_terminal_ws(stream, remote_host, remote_port); return;
    }
    if request.starts_with("GET / ") || request.starts_with("GET /?") {
        let html = crate::web::index_html_with_ws_path("/ws");
        let resp = format!("HTTP/1.1 200 OK\\r\\nContent-Type: text/html; charset=utf-8\\r\\nContent-Length: {}\\r\\nConnection: close\\r\\n\\r\\n", html.len());
        let _ = (|| -> std::io::Result<()> { stream.write_all(resp.as_bytes())?; stream.write_all(html.as_bytes())?; stream.shutdown(std::net::Shutdown::Write)?; Ok(()) })();
        return;
    }
    if request.contains("GET /terminal ") || request.contains("GET /terminal?") {
        let html = crate::web::TERMINAL_HTML;
        let resp = format!("HTTP/1.1 200 OK\\r\\nContent-Type: text/html; charset=utf-8\\r\\nContent-Length: {}\\r\\nConnection: close\\r\\n\\r\\n", html.len());
        let _ = (|| -> std::io::Result<()> { stream.write_all(resp.as_bytes())?; stream.write_all(html.as_bytes())?; stream.shutdown(std::net::Shutdown::Write)?; Ok(()) })();
        return;
    }
    let _ = stream.write_all(b"HTTP/1.1 404 Not Found\\r\\nContent-Length: 0\\r\\n\\r\\n");
    let _ = stream.shutdown(std::net::Shutdown::Write);
}

'''
        content = content[:idx] + new_conn + content[next_fn:]
        print('Replaced handle_local_terminal_conn')

# Add handle_local_desktop_ws before handle_local_terminal_ws
old_ws_start = 'fn handle_local_terminal_ws(stream: TcpStream, remote_host: &str, remote_port: u16) {'
idx2 = content.find(old_ws_start)
if idx2 >= 0:
    new_desktop_ws = '''fn handle_local_desktop_ws(stream: TcpStream, remote_host: &str, remote_port: u16) {
    let mut ws = match tungstenite::accept(stream) { Ok(w) => w, Err(e) => { warn!("桌面 WS 握手失败: {}", e); return; } };
    let rt = tokio::runtime::Builder::new_current_thread().enable_all().build().unwrap();
    rt.block_on(async {
        let addr: std::net::SocketAddr = match format!("{}:{}", remote_host, remote_port).parse() { Ok(a) => a, Err(e) => { warn!("地址解析失败: {}", e); return; } };
        let tcp = match tokio::net::TcpStream::connect(addr).await { Ok(s) => s, Err(e) => { warn!("连接服务端失败: {}", e); return; } };
        let (mut r, mut w) = tcp.into_split();
        use crate::protocol::*;
        let _cap = match recv_frame(&mut r).await { Some((FRAME_CAP, d)) => String::from_utf8_lossy(&d).to_string(), _ => String::new() };
        let info_json = match recv_frame(&mut r).await { Some((FRAME_INFO, d)) => String::from_utf8_lossy(&d).to_string(), _ => String::new() };
        let (sw, sh) = info_json.split_once(',').map(|(a,b)| (a.trim().parse().unwrap_or(1280), b.trim().parse().unwrap_or(720))).unwrap_or((1280,720));
        let extradata = match recv_frame(&mut r).await { Some((FRAME_EXTRADATA, d)) => d, _ => Vec::new() };
        let desc_hex: String = extradata.iter().map(|b| format!("{:02X}", b)).collect();
        let config = format!(r#"{{"type":"config","w":{},"h":{},"codec":"avc1.42E01E","fps":60,"description":"{}"}}"#, sw, sh, desc_hex);
        let _ = ws.send(tungstenite::Message::Text(config));
        info!("桌面 WS 已连接: {}x{}", sw, sh);
        loop {
            match ws.read() {
                Ok(Message::Text(text)) => {
                    let parts: Vec<&str> = text.split('|').collect();
                    if parts.len() >= 2 {
                        if parts[0] == "m" && parts.len() >= 5 {
                            let (x, y, btn, action): (i32,i32,u8,u8) = (parts[1].parse().unwrap_or(0), parts[2].parse().unwrap_or(0), parts[3].parse().unwrap_or(0), parts[4].parse().unwrap_or(2));
                            send_frame(&mut w, CTRL_MOUSE, &encode_mouse(x,y,btn,action)).await.ok();
                        } else if parts[0] == "k" && parts.len() >= 3 {
                            let (code, action): (i32, u8) = (parts[1].parse().unwrap_or(0), parts[2].parse().unwrap_or(0));
                            send_frame(&mut w, CTRL_KEY, &encode_key(code, action==0)).await.ok();
                        }
                    }
                }
                Ok(Message::Close(_)) | Ok(Message::Ping(_)) | Ok(Message::Pong(_)) => break,
                Err(tungstenite::Error::Io(ref e)) if e.kind() == std::io::ErrorKind::WouldBlock => {}
                Err(e) => { warn!("桌面 WS 断开: {}", e); break; }
                _ => {}
            }
            loop {
                let mut magic = [0u8; 4];
                match tokio::time::timeout(Duration::from_micros(500), r.read(&mut magic)).await {
                    Ok(Ok(4)) if u32::from_be_bytes(magic) == MAGIC => {
                        let mut typ = [0u8; 1]; if r.read_exact(&mut typ).await.is_err() { break; }
                        let mut len_buf = [0u8; 4]; if r.read_exact(&mut len_buf).await.is_err() { break; }
                        let len = u32::from_be_bytes(len_buf) as usize;
                        let mut payload = vec![0u8; len]; if len > 0 && r.read_exact(&mut payload).await.is_err() { break; }
                        if typ[0] == FRAME_H264 || typ[0] == FRAME_H265 {
                            let is_key = payload.len() > 9 && (payload[9] & 0x1F) == 5;
                            let ts = &payload[..8.min(payload.len())];
                            let es = if payload.len() > 9 { &payload[9..] } else { &[] };
                            let mut avcc_input = Vec::with_capacity(10 + es.len());
                            avcc_input.extend_from_slice(ts);
                            avcc_input.push(typ[0]); avcc_input.push(if is_key { 1 } else { 0 });
                            avcc_input.extend_from_slice(es);
                            let avcc = crate::web::annexb_to_avcc(&avcc_input);
                            if ws.send(Message::Binary(avcc)).is_err() { break; }
                        }
                    }
                    _ => break,
                }
            }
            tokio::time::sleep(Duration::from_millis(1)).await;
        }
    });
}

''' + old_ws_start
    content = content.replace(old_ws_start, new_desktop_ws, 1)
    print('Added handle_local_desktop_ws')

with open('client.rs', 'w', encoding='utf-8') as f:
    f.write(content)
print('Done')
