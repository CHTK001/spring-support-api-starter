with open('client.rs', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Rewrite tray creation with with_id
start_marker = '            // ── 创建系统托盘 ──'
end_marker = '                    *tray_create.borrow_mut() = Some(tray);\n                }\n            }'

start_idx = content.find(start_marker)
end_idx = content.find(end_marker, start_idx)
if end_idx >= 0:
    end_idx += len(end_marker)

if start_idx >= 0 and end_idx > start_idx:
    new_tray = '''            // ── 创建系统托盘 ──
            #[cfg(not(target_os = "linux"))]
            {
                use tray_icon::TrayIconBuilder;
                use tray_icon::menu::{Menu, MenuItem, CheckMenuItem, Submenu};

                macro_rules! id_item {
                    ($id:expr, $text:expr) => {{ Box::leak(Box::new(MenuItem::with_id($id, $text, true, None))) }};
                }
                macro_rules! id_check {
                    ($id:expr, $text:expr, $checked:expr) => {{ Box::leak(Box::new(CheckMenuItem::with_id($id, $text, true, $checked, None))) }};
                }
                macro_rules! sub {
                    ($text:expr) => {{ Box::leak(Box::new(Submenu::new($text, true))) }};
                }

                let q = sub!("画质");
                q.append(id_item!("preset_speed", "极速")).ok();
                q.append(id_item!("preset_balanced", "平衡")).ok();
                q.append(id_item!("preset_quality", "高清")).ok();
                q.append(id_item!("preset_original", "原画")).ok();

                let c = sub!("编码");
                c.append(id_item!("codec_h264", "H.264")).ok();
                c.append(id_item!("codec_h265", "H.265")).ok();

                let mon = sub!("显示器");
                mon.append(id_item!("monitor_0", "显示器0")).ok();
                mon.append(id_item!("monitor_1", "显示器1")).ok();

                let d = sub!("显示");
                d.append(q).ok(); d.append(c).ok(); d.append(mon).ok();

                let met = sub!("指标");
                met.append(id_check!("met_fps", "FPS", true)).ok();
                met.append(id_check!("met_res", "分辨率", true)).ok();
                met.append(id_check!("met_lat", "延迟", true)).ok();
                met.append(id_check!("met_mem", "内存", true)).ok();
                met.append(id_check!("met_codec", "编码信息", true)).ok();

                let hud = sub!("HUD位置");
                hud.append(id_item!("hud_tl", "左上")).ok();
                hud.append(id_item!("hud_tr", "右上")).ok();
                hud.append(id_item!("hud_bl", "左下")).ok();
                hud.append(id_item!("hud_br", "右下")).ok();

                let v = sub!("视图");
                v.append(met).ok(); v.append(hud).ok();

                let rem = sub!("远程");
                rem.append(id_check!("ctrl_enable", "启用远控", true)).ok();
                rem.append(id_check!("ctrl_downscale", "降屏采集", false)).ok();
                rem.append(id_item!("ctrl_web", "打开Web")).ok();

                let root = Menu::new();
                root.append(d).ok();
                root.append(v).ok();
                root.append(rem).ok();
                root.append(id_item!("", "─")).ok();
                root.append(id_item!("disconnect", "断开连接")).ok();
                root.append(id_item!("exit", "退出")).ok();

                if let Ok(tray) = TrayIconBuilder::new()
                    .with_tooltip("远程桌面查看器")
                    .with_menu(Box::new(root))
                    .build()
                {
                    *tray_create.borrow_mut() = Some(tray);
                }
            }'''

    content = content[:start_idx] + new_tray + content[end_idx:]
    print('Replaced tray creation')
else:
    print('Tray creation not found')

# 2. Rewrite event handling with ID matching
old_events = '''        // ── 系统托盘菜单事件 ──
        #[cfg(not(target_os = "linux"))]
        {
            use tray_icon::menu::MenuEvent;
            while let Ok(event) = MenuEvent::receiver().try_recv() {
                info!("托盘菜单事件: id={:?}", event.id.0);
                match event.id.0.as_str() {
                    "退出" => ctx.send_viewport_cmd(egui::ViewportCommand::Close),'''

idx = content.find(old_events)
if idx >= 0:
    # Find the end of this section
    end_events = content.find('        }', idx + 100)
    if end_events >= 0:
        end_events = content.find('        }', end_events + 1)  # second closing brace
        if end_events >= 0:
            end_events += 8  # include the closing brace and newlines

            new_events = '''        // ── 系统托盘菜单事件 ──
        #[cfg(not(target_os = "linux"))]
        {
            use tray_icon::menu::MenuEvent;
            while let Ok(event) = MenuEvent::receiver().try_recv() {
                match event.id.0.as_str() {
                    "exit" => ctx.send_viewport_cmd(egui::ViewportCommand::Close),
                    "disconnect" => {
                        *self.ctrl_tx.lock().unwrap() = None;
                        self.connected = false;
                        self.status = "已断开".to_string();
                        self.latest_frame = None;
                        self.frame_w = 0;
                        self.frame_h = 0;
                        self.video_texture = None;
                        self.video_area_rect = None;
                    }
                    "preset_speed"   => send_ctrl(&self.ctrl_tx, CTRL_PRESET, &[0u8]),
                    "preset_balanced" => send_ctrl(&self.ctrl_tx, CTRL_PRESET, &[1u8]),
                    "preset_quality"  => send_ctrl(&self.ctrl_tx, CTRL_PRESET, &[2u8]),
                    "preset_original" => send_ctrl(&self.ctrl_tx, CTRL_PRESET, &[3u8]),
                    "codec_h264" => send_ctrl(&self.ctrl_tx, CTRL_CODEC, &[1u8]),
                    "codec_h265" => send_ctrl(&self.ctrl_tx, CTRL_CODEC, &[2u8]),
                    "monitor_0" => send_ctrl(&self.ctrl_tx, CTRL_MONITOR, &[0u8]),
                    "monitor_1" => send_ctrl(&self.ctrl_tx, CTRL_MONITOR, &[1u8]),
                    "ctrl_enable" => self.control_enabled = !self.control_enabled,
                    "ctrl_downscale" => {
                        self.downscale = !self.downscale;
                        send_ctrl(&self.ctrl_tx, CTRL_DOWNSCALE, &[if self.downscale { 1u8 } else { 0u8 }]);
                    }
                    "ctrl_web" => {
                        if let Some(wp) = self.web_port {
                            let url = format!("http://127.0.0.1:{}", wp);
                            let _ = std::process::Command::new("cmd").args(["/c", "start", &url]).spawn();
                        }
                    }
                    "met_fps"    => self.show_metrics.fps = !self.show_metrics.fps,
                    "met_res"    => self.show_metrics.resolution = !self.show_metrics.resolution,
                    "met_lat"    => self.show_metrics.latency = !self.show_metrics.latency,
                    "met_mem"    => self.show_metrics.memory = !self.show_metrics.memory,
                    "met_codec"  => self.show_metrics.codec = !self.show_metrics.codec,
                    "hud_tl" => self.hud_position = HudPosition::TopLeft,
                    "hud_tr" => self.hud_position = HudPosition::TopRight,
                    "hud_bl" => self.hud_position = HudPosition::BottomLeft,
                    "hud_br" => self.hud_position = HudPosition::BottomRight,
                    _ => {}
                }
            }
        }

'''
            content = content[:idx] + new_events + content[end_events:]
            print('Replaced event handling')
else:
    print('Event handling start not found')

with open('client.rs', 'w', encoding='utf-8') as f:
    f.write(content)
print('Done')
PYEOF