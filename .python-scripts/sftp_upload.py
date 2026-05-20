#!/usr/bin/env python3
"""SFTP upload a tar.gz and extract on remote."""
import paramiko, sys, os, posixpath

host = sys.argv[1]
port = int(sys.argv[2]) if len(sys.argv) > 2 else 22
local_path = sys.argv[3]
remote_dir = sys.argv[4] if len(sys.argv) > 4 else "/home/qiren/"
password = os.environ.get("SSH_PASS", "")

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
try:
    client.connect(host, port=port, username=os.environ.get("SSH_USER", "root"), password=password, timeout=30)

    # Upload via SFTP
    sftp = client.open_sftp()
    remote_path = posixpath.join(remote_dir, os.path.basename(local_path))
    sftp.put(local_path, remote_path)
    sftp.close()
    print(f"Uploaded to {remote_path}")

    # Extract
    stdin, stdout, stderr = client.exec_command(
        f"cd {remote_dir} && tar xzf {os.path.basename(local_path)} && echo EXTRACT_OK",
        timeout=60
    )
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out.strip():
        print(out.strip())
    if err.strip():
        print("STDERR:", err.strip())

    if "EXTRACT_OK" in out:
        print("Extract OK")
finally:
    client.close()
