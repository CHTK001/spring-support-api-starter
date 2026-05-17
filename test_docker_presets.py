#!/usr/bin/env python3
"""Docker 预设全量生命周期测试脚本 (使用 reconfigure 解决 dockerDefaults 提升)"""
import json, sys, time, urllib.request, urllib.error

BASE = "http://127.0.0.1:58080"
BINDING_URL = f"{BASE}/v2/project/container/software/binding"
PRESET_URL = f"{BASE}/v2/project/container/software/preset"
HEADERS = {"Content-Type": "application/json"}
PROJECT_ID = 1

results = []
preset_results = []

def req(method, url, body=None, timeout=120):
    data = json.dumps(body).encode() if body else None
    r = urllib.request.Request(url, data=data, headers=HEADERS, method=method)
    try:
        resp = urllib.request.urlopen(r, timeout=timeout)
        return json.loads(resp.read())
    except urllib.error.HTTPError as e:
        body = e.read().decode(errors='ignore')
        try: return json.loads(body)
        except: return {"code": "ERROR", "msg": body[:500]}
    except Exception as e:
        return {"code": "ERROR", "msg": str(e)}

def b(path, method="GET", body=None):
    return req(method, f"{BINDING_URL}{path}", body)

# 1. Get Docker presets
print("=" * 60)
print("获取所有预设...")
all_presets = req("GET", f"{PRESET_URL}/list")
presets = [p for p in all_presets["data"]
           if p["softwareCode"] not in ("docker","docker-compose","git","nodejs","python","maven")]
print(f"Docker 预设: {len(presets)} 个\n")

# 2. Full lifecycle test
print("=" * 60)
print("开始 Docker 预设全量生命周期测试")
print("=" * 60)

for idx, p in enumerate(presets, 1):
    rid, code, name = p["sysProjectContainerSoftwarePresetId"], p["softwareCode"], p["softwareName"]
    print(f"\n[{idx}/{len(presets)}] {name} (code={code})")
    steps = []
    failed = False

    # Step 1: Import preset
    print(f"  ▶ 导入...")
    time.sleep(0.3)
    imp = b("/importPreset", "POST", {"sysProjectId": PROJECT_ID, "sysProjectContainerSoftwarePresetId": rid})
    if imp.get("code") != "00000":
        print(f"  ✗ {imp.get('msg','?')[:100]}")
        results.append((name, "导入", "FAIL", imp.get("msg","?")))
        preset_results.append({"name":name,"code":code,"overall":"FAIL","steps":[("导入","FAIL",imp.get("msg","?"))]})
        continue

    bind_id = imp["data"].get("sysProjectContainerSoftwareBindingId")
    remark_str = imp["data"].get("bindingRemark", "{}")
    print(f"  ✓ ID={bind_id}")
    results.append((name, "导入", "PASS", f"ID={bind_id}"))
    steps.append(("导入", "PASS", f"ID={bind_id}"))

    # Step 2: Promote dockerDefaults via reconfigure
    if '"dockerDefaults"' in remark_str:
        try:
            remark = json.loads(remark_str)
            dd = remark.get("dockerDefaults", {})
            config_values = {"installMode": "DOCKER"}
            if dd.get("image"): config_values["dockerImage"] = dd["image"]
            if dd.get("command"): config_values["cmd"] = dd["command"]
            if config_values:
                reconf = b("/reconfigure", "POST", {
                    "sysProjectContainerSoftwareBindingId": bind_id,
                    "configValues": {
                        k: v for k, v in config_values.items()
                    }
                })
                if reconf.get("code") == "00000":
                    print(f"  ✓ dockerDefaults 提升: {list(config_values.keys())}")
                else:
                    print(f"  ⚠ reconfigure 警告: {reconf.get('msg','?')[:80]}")
        except Exception as e:
            print(f"  ⚠ dockerDefaults 解析失败: {e}")
    time.sleep(0.5)

    # Step 3: INSTALL
    print(f"  ▶ 安装...")
    inst = b("/operate", "POST", {"sysProjectContainerSoftwareBindingId": bind_id, "action": "INSTALL"})
    if inst.get("code") == "00000":
        print(f"  ✓ 安装成功")
        results.append((name, "安装", "PASS", ""))
        steps.append(("安装", "PASS", ""))
    else:
        msg = inst.get("msg", inst.get("message", ""))
        print(f"  ✗ {msg[:120]}")
        results.append((name, "安装", "FAIL", msg))
        steps.append(("安装", "FAIL", msg))
        preset_results.append({"name":name,"code":code,"overall":"FAIL","steps":steps})
        # Still try to uninstall for cleanup
        b("/operate", "POST", {"sysProjectContainerSoftwareBindingId": bind_id, "action": "UNINSTALL"})
        continue

    # Wait for container
    print(f"  ⏳ 等待 10s...")
    time.sleep(10)

    # Step 4: STATUS
    print(f"  ▶ 状态...")
    stat = b("/operate", "POST", {"sysProjectContainerSoftwareBindingId": bind_id, "action": "STATUS"})
    if stat.get("code") == "00000":
        cs = "unknown"
        sd = stat.get("data")
        if isinstance(sd, dict): cs = sd.get("containerStatus", sd.get("status", "unknown"))
        elif isinstance(sd, str): cs = sd
        print(f"  ✓ status={cs}")
        results.append((name, "状态", "PASS", f"status={cs}"))
        steps.append(("状态", "PASS", f"status={cs}"))
    else:
        msg = stat.get("msg", stat.get("message", ""))
        print(f"  ✗ {msg[:120]}")
        results.append((name, "状态", "FAIL", msg))
        steps.append(("状态", "FAIL", msg))
        failed = True

    # Step 5: STOP
    print(f"  ▶ 停止...")
    time.sleep(1)
    stop = b("/operate", "POST", {"sysProjectContainerSoftwareBindingId": bind_id, "action": "STOP"})
    if stop.get("code") == "00000":
        print(f"  ✓ 停止成功")
        results.append((name, "停止", "PASS", ""))
        steps.append(("停止", "PASS", ""))
    else:
        msg = stop.get("msg", stop.get("message", ""))
        print(f"  ✗ {msg[:120]}")
        results.append((name, "停止", "FAIL", msg))
        steps.append(("停止", "FAIL", msg))
        failed = True

    time.sleep(2)

    # Step 6: START
    print(f"  ▶ 启动...")
    start = b("/operate", "POST", {"sysProjectContainerSoftwareBindingId": bind_id, "action": "START"})
    if start.get("code") == "00000":
        print(f"  ✓ 启动成功")
        results.append((name, "启动", "PASS", ""))
        steps.append(("启动", "PASS", ""))
    else:
        msg = start.get("msg", start.get("message", ""))
        print(f"  ✗ {msg[:120]}")
        results.append((name, "启动", "FAIL", msg))
        steps.append(("启动", "FAIL", msg))
        failed = True

    time.sleep(2)

    # Step 7: UNINSTALL
    print(f"  ▶ 卸载...")
    uninst = b("/operate", "POST", {"sysProjectContainerSoftwareBindingId": bind_id, "action": "UNINSTALL"})
    if uninst.get("code") == "00000":
        print(f"  ✓ 卸载成功")
        results.append((name, "卸载", "PASS", ""))
        steps.append(("卸载", "PASS", ""))
    else:
        msg = uninst.get("msg", uninst.get("message", ""))
        print(f"  ✗ {msg[:120]}")
        results.append((name, "卸载", "FAIL", msg))
        steps.append(("卸载", "FAIL", msg))
        failed = True

    overall = "PASS" if not failed else "FAIL"
    preset_results.append({"name":name,"code":code,"overall":overall,"steps":steps})
    if overall == "PASS":
        print(f"  ★ 全部通过")

# Summary
print("\n" + "=" * 60)
print("测试完成!")
print("=" * 60)
pass_s = sum(1 for r in results if r[2]=="PASS")
fail_s = sum(1 for r in results if r[2]=="FAIL")
p_pass = sum(1 for pr in preset_results if pr["overall"]=="PASS")
p_fail = sum(1 for pr in preset_results if pr["overall"]=="FAIL")
total_s = pass_s + fail_s
print(f"预设: {p_pass} 通过 / {p_fail} 失败 (共 {len(preset_results)})")
print(f"步骤: {pass_s} 通过 / {fail_s} 失败 (共 {total_s})")
print(f"通过率: {pass_s/total_s*100:.1f}%" if total_s else "N/A")
if fail_s:
    print("\n失败步骤:")
    for name, action, status, detail in results:
        if status == "FAIL":
            d = detail[:120].replace('\n',' ')
            print(f"  [{name}] {action}: {d}")

print("\n\n---JSON_RESULT---")
print(json.dumps({"results":results,"presets":preset_results},ensure_ascii=False,indent=2))
print("---JSON_RESULT_END---")
