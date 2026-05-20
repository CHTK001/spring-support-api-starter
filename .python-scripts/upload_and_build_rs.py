#!/usr/bin/env python3
"""Upload and compile remote-desktop on server."""
import paramiko, os, sys, time

host = "192.168.110.100"
password = "qiren1818"
local = os.path.join(os.environ.get("TEMP", "/tmp"), "remote-desktop-src.tar.gz")
remote_dir = "/home/qiren/build_rust"

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, port=22, username="qiren", password=password, timeout=30)
sftp = client.open_sftp()

# Upload
remote = "/home/qiren/remote-desktop-src.tar.gz"
sftp.put(local, remote)
print(f"Upload OK ({os.path.getsize(local)} bytes)")
sftp.close()

# Extract and compile
stdin, stdout, stderr = client.exec_command(
    f"rm -rf {remote_dir}/remote-desktop && "
    f"mkdir -p {remote_dir}/remote-desktop && "
    f"cd {remote_dir}/remote-desktop && "
    f"tar xzf /home/qiren/remote-desktop-src.tar.gz && "
    f"cargo build --release --no-default-features 2>&1",
    timeout=300
)
out = stdout.read().decode()
err = stderr.read().decode()
if out.strip():
    print(out.strip()[-2000:])
if err.strip():
    print("STDERR:", err.strip()[-1000:])

client.close()
