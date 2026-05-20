#!/usr/bin/env python3
"""SSH helper: execute commands with long timeout."""
import paramiko, sys, os, json

host = sys.argv[1]
port = int(sys.argv[2]) if len(sys.argv) > 2 else 22
cmd = sys.argv[3] if len(sys.argv) > 3 else "echo ok"
password = os.environ.get("SSH_PASS", "")

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
try:
    client.connect(host, port=port, username=os.environ.get("SSH_USER", "root"), password=password, timeout=30)
    transport = client.get_transport()
    transport.set_keepalive(30)
    chan = transport.open_session(timeout=300)
    chan.settimeout(300)
    chan.exec_command(cmd)
    out = b""
    err = b""
    while True:
        if chan.recv_ready():
            out += chan.recv(4096)
        if chan.recv_stderr_ready():
            err += chan.recv_stderr(4096)
        if chan.exit_status_ready():
            break
    rc = chan.recv_exit_status()
    # Read any remaining data
    while chan.recv_ready():
        out += chan.recv(4096)
    while chan.recv_stderr_ready():
        err += chan.recv_stderr(4096)
    print(json.dumps({"rc": rc, "out": out.decode("utf-8", errors="replace"), "err": err.decode("utf-8", errors="replace")}))
finally:
    client.close()
